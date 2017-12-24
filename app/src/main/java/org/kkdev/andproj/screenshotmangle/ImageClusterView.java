package org.kkdev.andproj.screenshotmangle;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by shelikhoo on 12/2/17.
 */

public class ImageClusterView extends ConstraintLayout {
    final static String TAG = "ImageCluster";

    final ImageClusterView me = this;
    public List<Map<String,String>> Data = new LinkedList<>();
    AD ad = new AD();

    private List<Map<String,String>>  GenStubData() {
        List<Map<String,String>> stubData= new LinkedList<>();
        Map<String,String> Item;

        Item = new HashMap<String, String>();
        Item.put("ImageClusterSrc",getURIByRID(R.raw.stubimage1).toString());
        stubData.add(Item);
        Item = new HashMap<String, String>();
        Item.put("ImageClusterSrc",getURIByRID(R.raw.stubimage2).toString());
        stubData.add(Item);
        Item = new HashMap<String, String>();
        Item.put("ImageClusterSrc",getURIByRID(R.raw.stubimage3).toString());
        stubData.add(Item);
        Item = new HashMap<String, String>();
        Item.put("ImageClusterSrc",getURIByRID(R.raw.stubimage4).toString());
        stubData.add(Item);
        Item = new HashMap<String, String>();
        Item.put("ImageClusterSrc",getURIByRID(R.raw.stubimage5).toString());
        stubData.add(Item);
        return stubData;
    }
    public final static String DisplayTarget[]=new String[]{
            "ImageClusterSrc"
    };
    public final static int DisplayTargetR[]=new int[]{
            R.id.image_cluster_represent
    };
    public ImageClusterView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        View view;
        LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.imagecluster, this);
        RecyclerView imageCluster=view.findViewById(R.id.imageClusterRecycler);

        imageCluster.setLayoutManager(new GridLayoutManager(this.getContext(), 2));

        imageCluster.setAdapter(ad);



    }

    public void SetDataSet(List<Map<String,String>> data){
        Data = data;
        ad.notifyDataSetChanged();
    }

    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap", e);
        }
        return bm;
    }

    private Uri getURIByRID(int resId){
        Resources resources = me.getContext().getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
    }

    class AD extends RecyclerView.Adapter{
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(me.getContext()).inflate(R.layout.imageclusteritem,parent,false);

            return new VH(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(Data.get(position).get("ImageClusterSrc").startsWith("/")){
                ((VH)holder).iv.setImageURI(Uri.parse("file://"+Data.get(position).get("ImageClusterSrc")));
            }else{
                ((VH)holder).iv.setImageURI(Uri.parse(Data.get(position).get("ImageClusterSrc")));
            }

        }



        @Override
        public int getItemCount() {
            return Data.size();
        }
        class VH extends RecyclerView.ViewHolder implements View.OnClickListener{

            public ImageView iv;


            public VH(View itemView) {
                super(itemView);
                iv = (ImageView) itemView.findViewById(R.id.image_cluster_represent);
                iv.setOnClickListener(this);
            }


            @Override
            public void onClick(View view) {
                    int selposi = getLayoutPosition();
                    if(ClusterSelected!=null){
                        ClusterSelected.onEvent(Data.get(selposi));
                    }
            }
        }

    }

    interface OnClusterSelected{
        void onEvent(Map<String,String> item);
    }
    public  OnClusterSelected ClusterSelected;
}
