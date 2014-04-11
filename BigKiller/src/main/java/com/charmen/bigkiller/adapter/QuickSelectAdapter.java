package com.charmen.bigkiller.adapter;

import android.content.Context;

import com.charmen.bigkiller.bean.DataModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by charmenli on 14-1-26.
 */
public abstract class QuickSelectAdapter<T> extends QuickAdapter<T> {

    private Map<T, Boolean> selectedMap = new HashMap<T, Boolean>();

    public QuickSelectAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
        selectedMap.clear();
    }

    public QuickSelectAdapter(Context context, int layoutResId, List data) {
        super(context, layoutResId, data);
        selectedMap.clear();
    }


    public void setSelected(T item) {
        boolean selected = !selectedMap.containsKey(item) ? false : selectedMap.get(item);
        setSelected(item, !selected);
    }

    public void setSelected(T item, boolean selected) {
        setSelected(item, selected, true);
    }

    public void setSelected(T item, boolean selected, boolean notify) {
        selectedMap.put(item, selected);
        if (notify) {
            notifyDataSetChanged();
        }
    }

    public void selectAll() {
        selectedMap.clear();
        for(T data : getData()) {
            selectedMap.put(data, true);
        }
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        super.clear();
        selectedMap.clear();
    }

    protected boolean isSelected(BaseAdapterHelper helper, T item, boolean defaultVal) {
        boolean ret = defaultVal;
        if (selectedMap.containsKey(item)) {
            ret = selectedMap.get(item);
        } else {
            selectedMap.put(item, ret);
        }
        return ret;
    }

    public Map<T, Boolean> getSelectedMap() {
        return selectedMap;
    }

    public List<T> getSelectKeys() {
        Set<T> keys = selectedMap.keySet();
        List<T> selectedKeys = new ArrayList<T>();
        for (T k : keys) {
            if (selectedMap.get(k)) {
                selectedKeys.add(k);
            }
        }
        return selectedKeys;
    }
}
