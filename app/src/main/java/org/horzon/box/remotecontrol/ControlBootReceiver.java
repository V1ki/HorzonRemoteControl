package org.horzon.box.remotecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ControlBootReceiver extends BroadcastReceiver {

    private static final String TAG = "ControlBootReceiver" ;
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive: "+intent);
        Intent activityIntent = new Intent(context, MainActivity.class);

        context.startActivity(activityIntent);
    }
}
