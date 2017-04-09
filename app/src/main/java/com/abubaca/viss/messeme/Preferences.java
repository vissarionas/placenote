package com.abubaca.viss.messeme;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by viss on 4/9/17.
 */

class Preferences {

    Boolean getBatterySaverState(Activity activity){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return preferences.getBoolean("battery_saver" , true);
    }

}
