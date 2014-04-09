package com.charmenli.scalephone.fragment;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.charmenli.scalephone.R;

import java.util.List;

public class AppSensorSettingFragment extends PreferenceFragment {


    public AppSensorSettingFragment() {
        // Required empty public constructor
    }

    private PreferenceScreen mScreen;
    private ProgressDialog mProgressDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_app_sensor_settings);
        mScreen = getPreferenceScreen();
        showProgressDialog();
        initialPreferences();
    }

    private void showProgressDialog() {
        mProgressDialog = ProgressDialog.show(getActivity(),
                getString(R.string.title_wait), getString(R.string.content_wait));
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void initialPreferences() {
        new Handler(){
            @Override
            public void handleMessage(Message msg) {
                PackageManager pm = getActivity().getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(0);
                SharedPreferences pref = getPreferenceManager().getSharedPreferences();
                for (ApplicationInfo app : apps) {
                    ListPreference preference = new ListPreference(getActivity());
                    preference.setTitle(pm.getApplicationLabel(app));
                    preference.setIcon(pm.getApplicationIcon(app));
                    preference.setKey(app.packageName);
                    preference.setEntries(R.array.turn_page_direction_entries);
                    preference.setEntryValues(R.array.turn_page_direction_entry_values);
                    preference.setValueIndex(Integer.parseInt(pref.getString(app.packageName, "0")));
                    mScreen.addPreference(preference);
                }
                dismissProgressDialog();
            }
        }.sendEmptyMessageDelayed(0, 100);
    }




}
