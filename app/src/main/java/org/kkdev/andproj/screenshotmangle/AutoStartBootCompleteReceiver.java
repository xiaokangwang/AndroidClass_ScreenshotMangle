package org.kkdev.andproj.screenshotmangle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStartBootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentb = new Intent(context,ScreenshotObserverService.class);
        context.startService(intentb);
        Log.i("Autostart", "started");
    }
}
