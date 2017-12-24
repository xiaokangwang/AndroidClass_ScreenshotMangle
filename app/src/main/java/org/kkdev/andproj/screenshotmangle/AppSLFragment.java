package org.kkdev.andproj.screenshotmangle;


import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.stfalcon.frescoimageviewer.ImageViewer;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AppSLFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppSLFragment extends Fragment {
    final static String TAG = "ImageCluster";

    final AppSLFragment me = this;
    AD ad = new AD();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public AppSLFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AppSLFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AppSLFragment newInstance(String param1, String param2) {
        AppSLFragment fragment = new AppSLFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Fresco.initialize(this.getContext());


    }
    RecyclerView appsl;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_app_sl, container, false);
        appsl=view.findViewById(R.id.recview_appsl);
        appsl.setAdapter(ad);
        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        appsl.setLayoutManager(llm);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(appsl.getContext(),
                llm.getOrientation());
        appsl.addItemDecoration(dividerItemDecoration);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Data = readDataFromDatabase();
        appsl.swapAdapter(ad,false);
    }

    public List<Map<String,String>> Data = new LinkedList<>();
    public Map<String,String[]> PkgClassToFiles = new HashMap<>();
    public Map<String,String[]> PkgToClass = new HashMap<>();

    private List<Map<String,String>>  GenStubData() {
        List<Map<String,String>> stubData= new LinkedList<>();
        Map<String,String> Item;

        Item = new HashMap<String, String>();
        Item.put("AppName","Demo2");
        stubData.add(Item);

        Item = new HashMap<String, String>();
        Item.put("AppName","DemoApp2");
        stubData.add(Item);

        return stubData;
    }

    class AD extends RecyclerView.Adapter{
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(me.getContext()).inflate(R.layout.appscreenshotitem,parent,false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            //((VH)holder).icon.setImageURI(Uri.parse(Data.get(position).get("AppIcon")));
            ((VH)holder).BriefText.setText(Data.get(position).get("count")+" Images");

            try
            {
                ((VH)holder).Appname.setText(Data.get(position).get("AppName"));
                Drawable d = getContext().getPackageManager().getApplicationIcon(Data.get(position).get("AppID"));
                ((VH)holder).icon.setImageDrawable(d);

            }
            catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
            }

            ((VH)holder).cluster.SetDataSet(getClusterDataFromPackageName(Data.get(position).get("AppID")));
            ((VH)holder).cluster.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
            ((VH)holder).exp.collapse(false);


        }

        @Override
        public int getItemCount() {

            return Data.size();
        }
        class VH extends RecyclerView.ViewHolder implements View.OnClickListener{

            public ImageView icon;
            public TextView Appname;
            public TextView BriefText;
            public ImageClusterView cluster;
            public ExpandableLayout exp;

            public VH(View itemView) {
                super(itemView);
                icon = (ImageView) itemView.findViewById(R.id.appiconview);
                Appname = (TextView) itemView.findViewById(R.id.appnametext);
                BriefText = itemView.findViewById(R.id.briefText);
                cluster = itemView.findViewById(R.id.cluster);
                exp = itemView.findViewById(R.id.expandable_layout);

                cluster.ClusterSelected=new ImageClusterView.OnClusterSelected() {
                    @Override
                    public void onEvent(final Map<String, String> item) {
                        //Toast.makeText(,Toast.LENGTH_LONG).show();
                        final String ClassName = item.get("classname");
                        final String packageName = item.get("packagename");
                        final ImageViewOverlay overlayView = new ImageViewOverlay(me.getContext());

                        new ImageViewer.Builder<>(getContext(),PkgClassToFiles.get(packageName+ClassName)).setFormatter(new ImageViewer.Formatter<String>() {
                            @Override
                            public String format(String s) {
                                String path = "file://"+Environment.getExternalStorageDirectory()
                                        + File.separator + Environment.DIRECTORY_PICTURES
                                        + File.separator + "Screenshots" + File.separator;
                                return path+s;
                            }
                        }).setOverlayView(overlayView).setImageChangeListener(new ImageViewer.OnImageChangeListener() {
                            @Override
                            public void onImageChange(int position) {
                                String path = Environment.getExternalStorageDirectory()
                                        + File.separator + Environment.DIRECTORY_PICTURES
                                        + File.separator + "Screenshots" + File.separator;
                                overlayView.setShareText(path+PkgClassToFiles.get(packageName+ClassName)[position]);
                                overlayView.setElementName(PkgClassToFiles.get(packageName+ClassName)[position]);
                            }
                        }).show();
                    }
                };
                itemView.setOnClickListener(this);

            }

            @Override
            public void onClick(View view) {
                if(!exp.isExpanded()){
                    exp.expand(true);
                }
            }
        }


    }

    public String GetAppNameById(String packageName){
        PackageManager packageManager= getContext().getPackageManager();
        try {
            String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    private List<Map<String,String>> readDataFromDatabase(){
        List<Map<String,String>> dbData= new LinkedList<>();
        PkgClassToFiles = new HashMap<>();
        PkgToClass = new HashMap<>();
        try{
            DB snappydb = DBFactory.open(getContext());
            String recordedPackages[] = snappydb.findKeys("PackageCount:");
            Map<String,String> Item;


            for (String PackageCount:
            recordedPackages) {
                Item = new HashMap<String, String>();
                String packname = PackageCount.substring("PackageCount:".length());
                Item.put("count",String.valueOf(snappydb.getLong(PackageCount)));
                Item.put("AppID",packname);
                Item.put("AppName",GetAppNameById(packname));
                List<String> PackagToClass = new LinkedList<>();
                String classassoc = "Package2Class:"+packname+":";
                String AssocClass[] = snappydb.findKeys(classassoc);
                for (String AssocClas:
                        AssocClass) {
                    String classname = AssocClas.substring(classassoc.length());
                    //Query Associated file
                    String classfileprefix ="Class:"+classname+":";
                    PackagToClass.add(classname);
                    String classfiles[] = snappydb.findKeys(classfileprefix);
                    List<String> Classfstr = new LinkedList<String>();
                    for (String cf:
                    classfiles) {
                        Classfstr.add(snappydb.get(cf));
                    }
                    PkgClassToFiles.put(packname+classname, (String[]) Classfstr.toArray(new String[Classfstr.size()]));
                }
                PkgToClass.put(packname,(String[])PackagToClass.toArray(new String[PackagToClass.size()]));

                dbData.add(Item);
            }

            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return dbData;
    }

    private List<Map<String,String>> getClusterDataFromPackageName(String packageName){
        String path = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_PICTURES
                + File.separator + "Screenshots" + File.separator;
        List<Map<String,String>> Cd = new LinkedList<>();
        for (String ClassName:
             PkgToClass.get(packageName)) {
            Map<String,String> item = new HashMap<>();
            item.put("packagename",packageName);
            item.put("classname",ClassName);
            item.put("ImageClusterSrc",path+PkgClassToFiles.get(packageName+ClassName)[0]);
            Cd.add(item);
        }
        return Cd;
    }
}
