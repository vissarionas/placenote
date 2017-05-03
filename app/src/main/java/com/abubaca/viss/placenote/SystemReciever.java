package com.abubaca.viss.placenote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by viss on 2/7/17.
 */

public class SystemReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                Intent i = new Intent(context, LocationService.class);
                context.startService(i);
            }
        }
    }
}