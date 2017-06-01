package com.abubaca.viss.placenote;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by viss on 2/24/17.
 */

public class Starter {

    private static final String TAG = "STARTER";

    private Activity activity;

    Starter(Activity activity){
        this.activity = activity;
    }

    void startLocationService(){
        Intent i = new Intent(activity , LocationService.class);
        activity.startService(i);
    }

    void startMapActivity() {
        Intent intent = new Intent(activity, MapActivity.class);
        activity.startActivity(intent);
    }

    void startViewPlaceActivity(String placeName){
        Intent intent = new Intent(activity, ViewPlaceActivity.class);
        intent.putExtra("PLACENAME" , placeName);
        activity.startActivity(intent);
    }

    void startHelpActivity(){
        Intent intent = new Intent(activity , HelpActivity.class);
        activity.startActivity(intent);
    }

    void startPrivacyPolicyWeb(){
        String url = "http://vissariontsitoglou.com/notepin.html";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        activity.startActivity(intent);
    }

    void startEmailClient(){
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", activity.getResources().getString(R.string.email), null));
        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
        activity.startActivity(Intent.createChooser(intent, "Contact via email"));
    }
}
