package com.charmenli.scalephone.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.charmenli.scalephone.activity.AppSensorSettingActivity;
import com.charmenli.scalephone.activity.OperationWindowSettingActivity;
import com.charmenli.scalephone.R;
import com.charmenli.scalephone.adapter.IndicatorSizeAdapter;
import com.charmenli.scalephone.manager.IndicatorManager;
import com.charmenli.scalephone.manager.MonkeyManager;
import com.charmenli.scalephone.manager.SensorController;
import com.charmenli.scalephone.preference.ExitDialogPreference;
import com.charmenli.scalephone.preference.ImageListPreference;
import com.charmenli.scalephone.receiver.ScreenEventReceiver;
import com.charmenli.scalephone.service.CoreService;

public class SettingsFragment extends PreferenceFragment {


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 从xml文件加载选项
        addPreferencesFromResource(R.xml.pref_settings);

        initialPreferences();
    }

    private void initialPreferences() {

        Preference operationWindowSettingPreference = findPreference("pref_operation_window_setting");
        operationWindowSettingPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity().getApplicationContext(), OperationWindowSettingActivity.class);
                startActivity(intent);
                return false;
            }
        });

        Preference appSensorSettingPreference = findPreference("pref_app_sensor_setting");
        appSensorSettingPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AppSensorSettingActivity.class);
                startActivity(intent);
                return false;
            }
        });

        ImageListPreference imageListPreference = (ImageListPreference) findPreference("pref_indicator_size");
        imageListPreference.setListAdapter(new IndicatorSizeAdapter(getActivity().getApplicationContext(),
                imageListPreference, R.array.indicator_size, R.array.indicator_size_values));

        imageListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                float scale = Float.parseFloat(String.valueOf(newValue));
                IndicatorManager.getInstance().setIndicatorViewSize(scale);
                return true;
            }
        });

        ExitDialogPreference exitPreference = (ExitDialogPreference) findPreference("pref_exit");
        exitPreference.setOnExitListener(new ExitDialogPreference.OnExitListener() {
            @Override
            public void doExit() {
                IndicatorManager.getInstance().removeIndicator();
                CoreService.stopService(getActivity().getApplicationContext());
                ScreenEventReceiver.unregister(getActivity().getApplicationContext());
                MonkeyManager.get().stopMonkeyServer();
                SensorController.get().stop();
                getActivity().finish();
            }
        });

    }




}
