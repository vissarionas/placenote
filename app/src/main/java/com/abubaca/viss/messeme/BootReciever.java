package com.abubaca.viss.messeme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by viss on 2/7/17.
 */

public class BootReciever extends BroadcastReceiver {

    private static final String TAG = "BOOT_RECIEVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getAction().equalsIgnoreCase(
                    Intent.ACTION_BOOT_COMPLETED)) {
                Log.i(TAG, "Device boot completed");
                Intent i = new Intent(context , LocationBackground.class);
                context.startService(i);
            }
        }
    }
}