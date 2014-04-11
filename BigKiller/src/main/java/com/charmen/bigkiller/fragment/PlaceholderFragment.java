package com.charmen.bigkiller.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.charmen.bigkiller.R;
import com.charmen.bigkiller.adapter.QuickAdapter;
import com.charmen.bigkiller.bean.DataModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private Handler mRefreshHandler;

    public PlaceholderFragment(int sectionNumber, Handler handler) {
        mRefreshHandler = handler;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int msgWhat = getArguments().getInt(ARG_SECTION_NUMBER);
        View rootView = null;
        switch (msgWhat) {
            case 0:
                rootView = inflater.inflate(R.layout.fragment_running, container, false);
                break;
            case 1:
                rootView = inflater.inflate(R.layout.fragment_white_list, container, false);
                break;
        }
        mRefreshHandler.sendEmptyMessage(msgWhat);
        return rootView;
    }

    public QuickAdapter getAdapter() {
        ListView listView = (ListView) getView().findViewById(R.id.section_list);
        return (QuickAdapter) listView.getAdapter();
    }

    public void setAdapter(QuickAdapter<DataModel> adapter) {
        ListView listView = (ListView) getView().findViewById(R.id.section_list);
        listView.setAdapter(adapter);
    }

    public void setOnListItemClick(AdapterView.OnItemClickListener listener) {
        ListView listView = (ListView) getView().findViewById(R.id.section_list);
        listView.setOnItemClickListener(listener);
    }

    public void setOnClickListener(int id, View.OnClickListener listener) {
        View view = getView().findViewById(id);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }
}