package org.kkdev.andproj.screenshotmangle;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

public class ViewTextActivity extends AppCompatActivity {
    final String TAG = "ViewTextActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_text);
        Init(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Init(intent);

    }

    private void Init(Intent intent) {
        (findViewById(R.id.floatingActionButton_viewDone)).setOnClickListener(v->finish());
        try {
            DB snappydb = DBFactory.open(this);
            Log.i(TAG, "onNewIntent: "+"File:"+intent.getStringExtra("Target")+":Content");
            if (!snappydb.exists("File:"+intent.getStringExtra("Target")+":Content")){
                showerr();
            }
            String data =snappydb.get("File:"+intent.getStringExtra("Target")+":Content");
            if (null == data||data.isEmpty()) {
                showerr();
            }
            ((EditText)findViewById(R.id.editText)).setText(data);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    private void showerr(){
        new AlertDialog.Builder(this).setMessage("There is no text avalible for this image.")
                .setOnCancelListener(v->finish())
                .setCancelable(true).setNegativeButton("Return",(v,c)->finish()).create()
                .show();
    }
}
