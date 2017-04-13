package com.abubaca.viss.messeme;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by viss on 4/11/17.
 */

public class SpinnerAdapter extends BaseAdapter {

    List<Drawable> drawables;
    LayoutInflater layoutInflater;

    public SpinnerAdapter(List<Drawable> drawables , Context context){
        this.drawables = drawables;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return drawables.size();
    }

    @Override
    public Object getItem(int position) {
        return drawables.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.icon_spinner_item , parent , false);
        ImageView iconIV = (ImageView)convertView.findViewById(R.id.place_icon);
        iconIV.setImageDrawable((Drawable) getItem(position));
        return convertView;
    }
}
