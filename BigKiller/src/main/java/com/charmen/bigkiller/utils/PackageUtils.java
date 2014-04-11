package com.charmen.bigkiller.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.charmen.bigkiller.bean.DataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charmenli on 14-1-26.
 */
public class PackageUtils {
    public static PackageInfo getPackageInfo(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static Drawable getIcon(Context context, String packageName) {
        PackageInfo info = getPackageInfo(context, packageName);
        Drawable icon = null;
        if (info != null) {
            icon = info.applicationInfo.loadIcon(context.getPackageManager());
        }
        return icon;
    }

    public static CharSequence getAppName(Context context, String packageName) {
        PackageInfo info = getPackageInfo(context, packageName);
        CharSequence appName = null;
        if (info != null) {
            appName = info.applicationInfo.loadLabel(context.getPackageManager());
        }
        return appName;
    }

    public static List<DataModel> getInstallPackageModels(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> infos = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        if (infos != null) {
            List<DataModel> models = new ArrayList<DataModel>(infos.size());
            for (PackageInfo info : infos) {
                if((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    continue;
                }
                DataModel model = new DataModel();
                model.setPackageName(info.packageName);
                model.setAppName(info.applicationInfo.loadLabel(pm));
                model.setIcon(info.applicationInfo.loadIcon(pm));
                models.add(model);
            }
            return models;
        }
        return new ArrayList<DataModel>();
    }
}
