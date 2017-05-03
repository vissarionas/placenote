package com.abubaca.viss.placenote;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by viss on 4/9/17.
 */

class Preferences {

    Boolean getBatterySaverState(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("battery_saver" , false);
    }
}
