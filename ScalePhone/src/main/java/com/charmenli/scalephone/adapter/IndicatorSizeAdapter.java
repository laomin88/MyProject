package com.charmenli.scalephone.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.charmenli.scalephone.R;
import com.charmenli.scalephone.preference.ImageListPreference;
import com.charmenli.scalephone.util.ImageUtils;

/**
 * Created by charmenli on 2014/4/2.
 */
public class IndicatorSizeAdapter extends BaseAdapter {

    private Context mContext;
    private int mItemId;
    private String[] mEntries;
    private String[] mEntryValues;
    private float density;
    private ImageListPreference mPreference;
    public IndicatorSizeAdapter(Context context, ImageListPreference preference, int entriesId, int entryValues) {
        this.mContext = context;
        this.mItemId = R.layout.item_indicator_size;
        this.mEntries = context.getResources().getStringArray(entriesId);
        this.mEntryValues = context.getResources().getStringArray(entryValues);
        density = context.getResources().getDisplayMetrics().density;
        this.mPreference = preference;
    }

    @Override
    public int getCount() {
        return this.mEntries.length;
    }

    @Override
    public Object getItem(int position) {
        return mEntries[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(this.mItemId, null);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_indicator);
        TextView textView = (TextView) convertView.findViewById(R.id.tv_size);
        CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(R.id.ctb_choose);

        imageView.setImageDrawable(getScaleDrawable(imageView, getScale(position)));
        textView.setText(getText(position));
        checkedTextView.setChecked(isChecked(position));

        return convertView;
    }

    private Drawable getScaleDrawable(ImageView imageView, float scale) {
        return ImageUtils.zoomDrawable(imageView.getDrawable(), scale*density);
    }

    private float getScale(int pos) {
        float scale = 1;
        try {
            scale = Float.parseFloat(this.mEntryValues[pos]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return scale;
    }

    private String getText(int pos) {
        return this.mEntries[pos];
    }

    private boolean isChecked(int position) {
        return mPreference.findIndexOfValue(mPreference.getValue()) == position;
    }


}
