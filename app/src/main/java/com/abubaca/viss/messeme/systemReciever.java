package com.abubaca.viss.messeme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by viss on 2/7/17.
 */

public class systemReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getAction().equalsIgnoreCase(
                    Intent.ACTION_BOOT_COMPLETED)) {
                Intent i = new Intent(context , FusedBackground.class);
                context.startService(i);
            }
        }
    }
}