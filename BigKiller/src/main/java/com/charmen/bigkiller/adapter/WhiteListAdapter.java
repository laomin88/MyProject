package com.charmen.bigkiller.adapter;

import android.content.Context;
import android.widget.CheckBox;

import com.charmen.bigkiller.R;
import com.charmen.bigkiller.bean.DataModel;
import com.charmen.bigkiller.core.WhiteListProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by charmenli on 14-1-26.
 */
public class WhiteListAdapter extends QuickSelectAdapter<DataModel> {

    public WhiteListAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    public WhiteListAdapter(Context context, int layoutResId, List<DataModel> data) {
        super(context, layoutResId, data);
    }

    @Override
    protected void convert(BaseAdapterHelper helper, DataModel item) {
        if (item.getAppName() != null) {
            helper.setText(R.id.textView, item.getAppName().toString());
        } else {
            helper.setText(R.id.textView, item.getProcessName());
        }
        if (item.getIcon() != null) {
            helper.setImageDrawable(R.id.imageView, item.getIcon());
        }
        CheckBox cb = helper.getView(R.id.selected_checkbox);
        if ( isSelected(helper, item) ) {
            cb.setChecked(true);
            setSelected(item, true, false);
        } else {
            cb.setChecked(false);
            setSelected(item, false, false);
        }
    }

    private boolean isSelected(BaseAdapterHelper helper, DataModel item) {
        Set<String> whiteList = WhiteListProvider.getInstance(getContext()).getWhiteList();
        if (whiteList.contains(item.getPackageName())) {
            return true;
        }
        return false;
    }

    public void saveWhiteList() {
        List<DataModel> selectedKeys = getSelectKeys();
        Set<String> whiteList = new HashSet<String>();
        for (DataModel model : selectedKeys) {
            whiteList.add(model.getPackageName());
        }
        WhiteListProvider.getInstance(getContext()).save(whiteList);
    }


}
