package com.abubaca.viss.messeme;

import android.app.Activity;
import android.content.Context;
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

    void makeSuccessToast(Activity activity , String text){
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View toastView = layoutInflater.inflate(R.layout.success_toast_view, (ViewGroup)activity.findViewById(R.id.toastLL));
        TextView textView = (TextView)toastView.findViewById(R.id.toastTV);
        textView.setText(text);
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setView(toastView);
        toast.show();
    }

    void makeWarningToast(Activity activity , String text){
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View toastView = layoutInflater.inflate(R.layout.warning_toast_view, (ViewGroup)activity.findViewById(R.id.toastLL));
        TextView textView = (TextView)toastView.findViewById(R.id.toastTV);
        textView.setText(text);
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setView(toastView);
        toast.show();
    }
}
