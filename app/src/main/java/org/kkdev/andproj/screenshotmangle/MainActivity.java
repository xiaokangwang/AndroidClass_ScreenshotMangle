package org.kkdev.andproj.screenshotmangle;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            if (islasttimehavedata = DatabaseHaveData()) {
                // Create a new Fragment to be placed in the activity layout
                //AppSLFragment firstFragment = AppSLFragment.newInstance("1", "2");

                // In case this activity was started with special instructions from an
                // Intent, pass the Intent's extras to the fragment as arguments
                AppSLFrag.setArguments(getIntent().getExtras());

                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, AppSLFrag).commit();
            } else {
                // Create a new Fragment to be placed in the activity layout
                //EmptySetFragment firstFragment = EmptySetFragment.newInstance("1", "2");

                // In case this activity was started with special instructions from an
                // Intent, pass the Intent's extras to the fragment as arguments
                EmptySetFrag.setArguments(getIntent().getExtras());

                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, EmptySetFrag).commit();
            }
            //Start Screenshot Observer Service

        }
        startService();


        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }

    }

    private AppSLFragment AppSLFrag = AppSLFragment.newInstance("1", "2");
    private EmptySetFragment EmptySetFrag = EmptySetFragment.newInstance("1", "2");
    private HelpFragment HelpFrag = HelpFragment.newInstance("1", "2");
    private boolean islasttimehavedata;

    private void doMySearch(String query) {
        List<String> data = new LinkedList<>();
        try {
            DB snappydb = DBFactory.open(this);
            String Keys[] = snappydb.findKeys("File:");
            for (String key :
                    Keys) {
                if (key.endsWith(":Content")) {
                    String ctx = snappydb.get(key);
                    if (ctx.contains(query)) {
                        data.add(key.split(":")[1]);
                    }
                }

            }
            if (data.size() == 0) {
                new AlertDialog.Builder(this).setMessage("No result found.")
                        .setOnCancelListener(v -> finish())
                        .setCancelable(true).setNegativeButton("Return", (v, c) -> finish()).create()
                        .show();
            } else {
                final ImageViewOverlay overlayView = new ImageViewOverlay(this);
                new ImageViewer.Builder<>(this, data).setFormatter(new ImageViewer.Formatter<String>() {
                    @Override
                    public String format(String s) {
                        String path = "file://" + Environment.getExternalStorageDirectory()
                                + File.separator + Environment.DIRECTORY_PICTURES
                                + File.separator + "Screenshots" + File.separator;
                        return path + s;
                    }
                }).setOverlayView(overlayView).setImageChangeListener(new ImageViewer.OnImageChangeListener() {
                    @Override
                    public void onImageChange(int position) {
                        String path = Environment.getExternalStorageDirectory()
                                + File.separator + Environment.DIRECTORY_PICTURES
                                + File.separator + "Screenshots" + File.separator;
                        overlayView.setShareText(path + data.get(position));
                        overlayView.setElementName(data.get(position));
                    }
                }).setOnDismissListener(new ImageViewer.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        finish();
                    }
                }).show();

            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gallary(false);

    }

    private void gallary(boolean force) {
        boolean thistimehavedata = DatabaseHaveData();
        if(thistimehavedata!=islasttimehavedata||force){
            if(!thistimehavedata){
                //Not possible
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, EmptySetFrag).commit();
            }else{
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, AppSLFrag).commit();
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        startService();
    }

    private void startService() {
        Intent intentb = new Intent(this, ScreenshotObserverService.class);
        this.startService(intentb);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_search) {
            onSearchRequested();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*
        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        if (id == R.id.nav_search){
            onSearchRequested();
        }else if(id == R.id.nav_help){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, HelpFrag).commit();
        }else if(id == R.id.nav_gallery){
            gallary(true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private boolean DatabaseHaveData() {
        List<Map<String, String>> dbData = new LinkedList<>();
        try {
            DB snappydb = DBFactory.open(this);
            String recordedPackages[] = snappydb.findKeys("PackageCount:");
            snappydb.close();
            if (recordedPackages.length != 0) {
                return true;
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return false;
    }

}