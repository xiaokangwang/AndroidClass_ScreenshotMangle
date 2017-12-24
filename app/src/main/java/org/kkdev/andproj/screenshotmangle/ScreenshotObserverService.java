package org.kkdev.andproj.screenshotmangle;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.UiAutomation;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class ScreenshotObserverService extends Service {
    final static String TAG = "ScreenshotObserver";
    @Override
    public void onCreate() {
        preparegetWindowsContent();
        listenforscreenshot();
        super.onCreate();
    }

    public void listenforscreenshot() {
        if(screenshotfo!=null){
            screenshotfo.startWatching();
        }
        String path = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_PICTURES
                + File.separator + "Screenshots" + File.separator;
        Log.i(TAG, path);
        screenshotfo = new FileObserver(path) {
            @Override
            public void onEvent(int i, @Nullable String s) {
                if(i==FileObserver.CREATE){
                    String o="";
                    try{
                        CollectedInformation info;
                        if(AssistedInformationCaptureAvalible){
                            info = CollectWindowsContent();
                        }else{
                            info = getCollectedInformationWithF();
                        }

                        //o=pkg[0]+pkg[1];
                        try{
                            DB snappydb = DBFactory.open(getBaseContext());
                            //put file to info index
                            snappydb.put("File:"+s+":CapturePackage", info.packageName);
                            snappydb.put("File:"+s+":CaptureClass", info.className);
                            if(info.Content!=null){
                                snappydb.put("File:"+s+":Content", info.Content);
                            }
                            //put package/class to file index
                            //generate Unique increasing value
                            long timestamp = System.currentTimeMillis();
                            snappydb.put("Package:"+info.packageName+":"+String.valueOf(timestamp),s);
                            snappydb.put("Package2Class:"+info.packageName+":"+info.className,1);
                            snappydb.put("Class:"+info.className+":"+String.valueOf(timestamp),s);
                            Log.i(TAG, "onEvent: "+info.className);
                            //increase counter
                            String PackagCount="PackageCount:"+info.packageName;
                            long pkgcnt = 0;
                            String ClassCount="ClassCount:"+info.className;
                            long classcnt = 0;
                            if(snappydb.exists(PackagCount)){
                                pkgcnt = snappydb.getLong(PackagCount);

                            }
                            if(snappydb.exists(ClassCount)){
                                classcnt = snappydb.getLong(ClassCount);
                            }
                            pkgcnt++;
                            classcnt++;
                            snappydb.putLong(PackagCount,pkgcnt);
                            snappydb.putLong(ClassCount,classcnt);

                            snappydb.close();
                        } catch (SnappydbException e) {
                            e.printStackTrace();
                        }
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }


                }
            }
        };
        screenshotfo.startWatching();
    }

    @NonNull
    private CollectedInformation getCollectedInformationWithF() {
        CollectedInformation info;
        String pkg[] = getForegroundPackageNameClassNameByUsageStats();
        info = new CollectedInformation();
        info.packageName=pkg[0];
        info.className=pkg[1];
        return info;
    }

    private static final String HANDLER_THREAD_NAME = "UiAutomatorHandlerThread";
    private final HandlerThread mHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
    private boolean AssistedInformationCaptureAvalible = false;

    private void preparegetWindowsContent() {

        try {
            @SuppressLint("PrivateApi")
            Class c = Class.forName("android.app.UiAutomationConnection");

            Object uic = c.getConstructor().newInstance();
            mHandlerThread.start();
            Looper lo = mHandlerThread.getLooper();
            Log.i(TAG, "CollectWindowsContent: "+lo.toString());
            Class d = Class.forName("android.app.IUiAutomationConnection");
            aiu= UiAutomation.class.getConstructor(lo.getClass(),d).newInstance(lo,uic);
            Log.i(TAG, "CollectWindowsContent: "+aiu.toString());
            aiu.getClass().getMethod("connect",int.class).invoke(aiu,0x00000001);
            AssistedInformationCaptureAvalible = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


        //IUiAutomationConnection.Stub.asInterface();
        //Looper lo = getBaseContext().getMainLooper();

        //Log.i(TAG, "getWindowsContent: ");
    }

    UiAutomation aiu;

    class CollectedInformation{
        String packageName;
        String className;
        String Content;
    }

    private CollectedInformation CollectWindowsContent() {
        try {
            AccessibilityNodeInfo ni = aiu.getRootInActiveWindow();
            if(ni==null){
                AssistedInformationCaptureAvalible = false;
                return getCollectedInformationWithF();
            }
            CollectedInformation info = getCollectedInformationWithF();
            StringBuilder sb = new StringBuilder();
            foreach_This_Access_Node(ni,sb);
            info.Content=sb.toString();
            //Log.i(TAG, "CollectWindowsContent: "+info.Content);
            Log.i(TAG, "CollectWindowsContent: "+info.Content);

            return info;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private void foreach_This_Access_Node(AccessibilityNodeInfo ni, StringBuilder sb){
        CharSequence textin = ni.getText();
        if(textin!=null&&textin.length()!=0){
            sb.append(textin);
            sb.append("\n");
        }
        for (int i = 0; i < ni.getChildCount(); i++) {
            foreach_This_Access_Node(ni.getChild(i),sb);
        }
    }

    final static String INTENT_communicate = "org.kkdev.andproj.screenshotmangle_683f14bd-e075-4349-8d60-aa5eeae27015";
    final static int Message_returnself = 0xff01;

    public ScreenshotObserverService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getAction().equals(INTENT_communicate)){
            final Messenger mMessenger = new Messenger(new IncomingHandler());
            return mMessenger.getBinder();
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    FileObserver screenshotfo = null;

    final ScreenshotObserverService me = this;
    Messenger master=null;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg)  {
            switch (msg.what) {
                case 0:
                    Message resp = Message.obtain(null, Message_returnself);
                    resp.obj=me;
                    try {
                        master=msg.replyTo;
                        msg.replyTo.send(resp);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                default:
                    super.handleMessage(msg);
            }
        }
    }


    public String[] getForegroundPackageNameClassNameByUsageStats() {
        String packageNameByUsageStats = null;
        String classByUsageStats = null;
            UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
            final long INTERVAL = 10000;
            int i = 180;
            long end = System.currentTimeMillis();
            long begin = end - INTERVAL;
            while (i>=0){
                UsageEvents usageEvents = mUsageStatsManager.queryEvents(begin, end);
                while (usageEvents.hasNextEvent()) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    usageEvents.getNextEvent(event);
                    if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        packageNameByUsageStats = event.getPackageName();
                        classByUsageStats = event.getClassName();
                        Log.d(TAG, "packageNameByUsageStats is" + packageNameByUsageStats + ", classByUsageStats is " + classByUsageStats);
                    }
                }
                if (packageNameByUsageStats != null) {
                    break;
                }
                end = begin;
                begin -= INTERVAL;
            }

        return new String[]{packageNameByUsageStats,classByUsageStats};
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            aiu.getClass().getMethod("disconnect").invoke(aiu);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
