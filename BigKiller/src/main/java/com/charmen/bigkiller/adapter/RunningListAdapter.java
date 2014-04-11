package com.charmen.bigkiller.adapter;

import android.content.Context;
import android.widget.CheckBox;

import com.charmen.bigkiller.R;
import com.charmen.bigkiller.bean.DataModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by charmenli on 14-1-26.
 */
public class RunningListAdapter extends QuickSelectAdapter<DataModel> {

    public RunningListAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    public RunningListAdapter(Context context, int layoutResId, List<DataModel> data) {
        super(context, layoutResId, data);
    }

    @Override
    public void convert(BaseAdapterHelper helper, DataModel item) {
        if (item.getAppName() != null) {
            helper.setText(R.id.textView, item.getAppName().toString());
        } else {
            helper.setText(R.id.textView, item.getProcessName());
        }
        if (item.getIcon() != null) {
            helper.setImageDrawable(R.id.imageView, item.getIcon());
        }
        CheckBox cb = helper.getView(R.id.selected_checkbox);
        if ( isSelected(helper, item, true) ) {
            cb.setChecked(true);
        } else {
            cb.setChecked(false);
        }
    }
}
