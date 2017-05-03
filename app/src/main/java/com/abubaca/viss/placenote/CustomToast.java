package com.abubaca.viss.placenote;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by viss on 4/8/17.
 */

class CustomToast {

    void makeToast(Activity activity , int state , String text){
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View toastView = layoutInflater.inflate(R.layout.custom_toast_view, (ViewGroup)activity.findViewById(R.id.toastLL));
        TextView textView = (TextView)toastView.findViewById(R.id.toastTV);
        textView.setText(text);
        switch (state){
            case Constants.SUCCESS_TOAST:
                textView.setBackgroundResource(R.color.toastSuccess);
                break;
            case Constants.WARNING_TOAST:
                textView.setBackgroundResource(R.color.toastWarning);
                break;
        }
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setView(toastView);
        toast.show();
    }
}
