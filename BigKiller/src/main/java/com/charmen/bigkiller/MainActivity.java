package com.charmen.bigkiller;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.charmen.bigkiller.adapter.QuickAdapter;
import com.charmen.bigkiller.adapter.QuickSelectAdapter;
import com.charmen.bigkiller.adapter.RunningListAdapter;
import com.charmen.bigkiller.adapter.WhiteListAdapter;
import com.charmen.bigkiller.bean.DataModel;
import com.charmen.bigkiller.core.WhiteListProvider;
import com.charmen.bigkiller.fragment.PlaceholderFragment;
import com.charmen.bigkiller.utils.CommandUtils;
import com.charmen.bigkiller.utils.PackageUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * 刷新ListView的handler
     */
    Handler mRefreshHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CommandUtils.gainRoot();

        //Set up refresh handler
        mRefreshHandler = new RefreshHandler();

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar running_list_item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment currentFragment;
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments != null && fragments.size() > position) {
                currentFragment = fragments.get(position);
            } else {
                currentFragment = new PlaceholderFragment(position, mRefreshHandler);
            }
            return currentFragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_running).toUpperCase(l);
                case 1:
                    return getString(R.string.title_white_list).toUpperCase(l);
            }
            return null;
        }
    }

    private class RefreshHandler extends Handler {
        public static final int MSG_REFRESH_RUNNING = 0;
        public static final int MSG_REFRESH_WHITE_LIST = 1;
        @Override
        public void handleMessage(Message msg) {
            PlaceholderFragment currentFragment = (PlaceholderFragment) getSupportFragmentManager().getFragments().get(msg.what);
            QuickAdapter adapter = currentFragment.getAdapter();
            List<DataModel> models = null;
            switch (msg.what) {
                case MSG_REFRESH_RUNNING:
                    models = getRunningModels();
                    if (adapter == null) {
                        adapter = new RunningListAdapter(getApplicationContext(), R.layout.running_list_item, models);
                        currentFragment.setAdapter(adapter);
                        initRunningFragmentListener(currentFragment);
                        selectExceptWhiteList(currentFragment);
                    } else {
                        adapter.clear();
                        adapter.addAll(models);
                    }
                    break;
                case MSG_REFRESH_WHITE_LIST:
                    models = PackageUtils.getInstallPackageModels(getApplicationContext());
                    if (adapter == null) {
                        adapter = new WhiteListAdapter(getApplicationContext(), R.layout.white_list_item, models);
                        currentFragment.setAdapter(adapter);
                        initWhiteListListener(currentFragment);
                    } else {
                        adapter.clear();
                        adapter.addAll(models);
                    }
                    break;
            }
            adapter.notifyDataSetChanged();
        }

        private List<DataModel> getRunningModels() {
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            Set<String> handledPkgSet = new HashSet<String>();
            List<DataModel> models = new ArrayList<DataModel>();

            fillRunningApps(activityManager, handledPkgSet, models);
            fillRunningServices(activityManager, handledPkgSet, models);

            return models;
        }

        private void fillRunningApps(ActivityManager activityManager, Set<String> handledPkgSet, List<DataModel> models) {
            List<ActivityManager.RunningAppProcessInfo> infoList = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo info : infoList) {
                if ("system".equals(info.processName)) {
                    continue;
                }
                for (String pkg : info.pkgList) {
                    if (handledPkgSet.contains(pkg)) {
                        continue;
                    }
                    handledPkgSet.add(pkg);
                    DataModel model = new DataModel();
                    model.setPackageName(pkg);
                    model.setPid(info.pid);
                    model.setProcessName(info.processName);
                    model.setPackageInfo(PackageUtils.getPackageInfo(getApplicationContext(), pkg));
                    if((model.getPackageInfo().applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        continue;
                    }
                    model.setAppName(PackageUtils.getAppName(getApplication(), pkg));
                    model.setIcon(PackageUtils.getIcon(getApplication(), pkg));
                    models.add(model);
                }
            }
        }

        private void fillRunningServices(ActivityManager activityManager, Set<String> handledPkgSet, List<DataModel> models) {
            List<ActivityManager.RunningServiceInfo> infoList = activityManager.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo info : infoList) {
                if ("system".equals(info.process)) {
                    continue;
                }
                String pkg = info.service == null ? null : info.service.getPackageName();

                if (pkg == null || handledPkgSet.contains(pkg)) {
                    continue;
                }
                handledPkgSet.add(pkg);
                DataModel model = new DataModel();
                model.setPackageName(pkg);
                model.setPid(info.pid);
                model.setProcessName(info.process);
                model.setPackageInfo(PackageUtils.getPackageInfo(getApplicationContext(), pkg));
                if((model.getPackageInfo().applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    continue;
                }
                model.setAppName(PackageUtils.getAppName(getApplication(), pkg));
                model.setIcon(PackageUtils.getIcon(getApplication(), pkg));
                models.add(model);
            }
        }
    }

    private void initWhiteListListener(PlaceholderFragment fragment) {
        fragment.setOnListItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WhiteListAdapter adapter = (WhiteListAdapter) adapterView.getAdapter();
                DataModel item = adapter.getItem(i);
                adapter.setSelected(item);
                adapter.saveWhiteList();
            }
        });
    }

    private void initRunningFragmentListener(final PlaceholderFragment fragment) {
        fragment.setOnListItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DataModel item = (DataModel) adapterView.getItemAtPosition(i);
                ((QuickSelectAdapter) adapterView.getAdapter()).setSelected(item);
            }
        });

        fragment.setOnClickListener(R.id.select_except_white_list_btn, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectExceptWhiteList(fragment);
            }
        });

        fragment.setOnClickListener(R.id.select_all_btn, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((QuickSelectAdapter) fragment.getAdapter()).selectAll();
            }
        });

        fragment.setOnClickListener(R.id.kill_process_btn, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final RunningListAdapter adapter = (RunningListAdapter) fragment.getAdapter();
                new Thread(){
                    @Override
                    public void run() {
                        List<DataModel> models = adapter.getSelectKeys();
                        List<String> pkgNames = new ArrayList<String>();
                        List<Integer> pids = new ArrayList<Integer>();
                        for (DataModel model : models) {
                            if (model.getPackageName().equals(getPackageName())) {
                                continue;
                            }
//                            CommandUtils.forceStop(model.getPackageName(), model.getProcessInfo().pid);
                            pkgNames.add(model.getPackageName());
                            pids.add(model.getPid());
                        }
                        String[] pkgNameArray = new String[pkgNames.size()];
                        Integer[] pidArray = new Integer[pids.size()];
                        pkgNames.toArray(pkgNameArray);
                        pids.toArray(pidArray);
                        CommandUtils.forceStop(pkgNameArray, pidArray);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), getString(R.string.kill_process_finish), Toast.LENGTH_SHORT).show();
                                CommandUtils.forceStop(getPackageName(), android.os.Process.myPid());
                            }
                        });
                    }
                }.start();
            }
        });
    }

    private void selectExceptWhiteList(PlaceholderFragment fragment) {
        WhiteListAdapter whiteListAdapter = (WhiteListAdapter)((PlaceholderFragment)getSupportFragmentManager().getFragments().get(1)).getAdapter();
        if (whiteListAdapter != null && whiteListAdapter.getSelectKeys().size() > 0) {
            whiteListAdapter.saveWhiteList();
        }
        RunningListAdapter adapter = (RunningListAdapter) fragment.getAdapter();
        List<DataModel> models = fragment.getAdapter().getData();
        Set<String> whiteList = WhiteListProvider.getInstance(getApplicationContext()).getWhiteList();
        for (DataModel model : models) {
            String pkgName = model.getPackageName();
            if (whiteList.contains(pkgName)) {
                adapter.setSelected(model, false, false);
            } else {
                adapter.setSelected(model, true, false);
            }
        }
        adapter.notifyDataSetChanged();
    }

}
