package com.dandan.lib.xmpermission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.PermissionChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Tanzhenxing
 * Date: 2018/9/6 上午10:30
 * Description:
 */
public final class PermissionUtils {

    /**
     * 是否是6.0以上版本
     */
    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 是否是8.0以上版本
     */
    public static boolean isOverOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * 返回应用程序在清单文件中注册的权限
     */
    public static List<String> getManifestPermissions(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return Arrays.asList(pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 是否有安装权限
     */
    public static boolean isHasInstallPermission(Context context) {
        if (isOverOreo()) {
            //必须设置目标SDK为26及以上才能正常检测安装权限
            if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.O) {
                throw new RuntimeException("The targetSdkVersion SDK must be 26 or more");
            }

            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    /**
     * 是否有悬浮窗权限
     */
    public static boolean isHasOverlaysPermission(Context context) {

        if (isOverMarshmallow()) {

            //必须设置目标SDK为23及以上才能正常检测安装权限
            if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.M) {
                throw new RuntimeException("The targetSdkVersion SDK must be 23 or more");
            }

            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * 获取没有授予的权限
     *
     * @param context     上下文对象
     * @param permissions 需要请求的权限组
     */
    public static ArrayList<String> getFailPermissions(Context context, List<String> permissions) {

        //必须设置目标SDK为23及以上才能正常检测安装权限
        if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.M) {
            throw new RuntimeException("The targetSdkVersion SDK must be 23 or more");
        }

        //如果是安卓6.0以下版本就返回null
        if (!PermissionUtils.isOverMarshmallow()) {
            return null;
        }

        ArrayList<String> failPermissions = null;
        for (String permission : permissions) {
            //检测安装权限
            if (permission.equals(Permission.REQUEST_INSTALL_PACKAGES)) {

                if (!isHasInstallPermission(context)) {
                    if (failPermissions == null) failPermissions = new ArrayList<>();
                    failPermissions.add(permission);
                }
                continue;
            }

            //检测悬浮窗权限
            if (permission.equals(Permission.SYSTEM_ALERT_WINDOW)) {
                if (!isHasOverlaysPermission(context)) {
                    if (failPermissions == null) failPermissions = new ArrayList<>();
                    failPermissions.add(permission);
                }
                continue;
            }

            //把没有授予过的权限加入到集合中
            if (PermissionChecker.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) {
                if (failPermissions == null) {
                    failPermissions = new ArrayList<>();
                }
                failPermissions.add(permission);
            }
        }

        return failPermissions;
    }

    /**
     * 是否还能继续申请没有授予的权限
     *
     * @param activity              Activity对象
     * @param failPermissions       失败的权限
     */
    public static boolean isRequestDeniedPermission(Activity activity, List<String> failPermissions) {
        for (String permission : failPermissions) {
            //检查是否还有权限还能继续申请的（这里指没有被授予的权限但是也没有被永久拒绝的）
            if (!checkSinglePermissionPermanentDenied(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在权限组检查某个权限是否被永久拒绝
     *
     * @param activity              Activity对象
     * @param permissions            请求的权限
     */
    public static List<Boolean> checkMorePermissionPermanentDenied(Activity activity, List<String> permissions) {
        List<Boolean> booleans = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSinglePermissionPermanentDenied(activity, permission)) {
                booleans.add(true);
            } else {
                booleans.add(false);
            }
        }
        return booleans;
    }

    /**
     * 检查某个权限是否被永久拒绝
     *
     * @param activity              Activity对象
     * @param permission            请求的权限
     */
    public static boolean checkSinglePermissionPermanentDenied(Activity activity, String permission) {

        //安装权限和浮窗权限不算，本身申请方式和危险权限申请方式不同，因为没有永久拒绝的选项，所以这里返回false
        if (permission.equals(Permission.REQUEST_INSTALL_PACKAGES) || permission.equals(Permission.SYSTEM_ALERT_WINDOW)) {
            return false;
        }

        if (PermissionUtils.isOverMarshmallow()) {
            if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                if (!activity.shouldShowRequestPermissionRationale(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取没有授予的权限
     *
     * @param permissions  需要请求的权限组
     * @param grantResults 允许结果组
     */
    public static List<String> getFailPermissions(String[] permissions, int[] grantResults) {
        List<String> failPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {

            //把没有授予过的权限加入到集合中，-1表示没有授予，0表示已经授予
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                failPermissions.add(permissions[i]);
            }
        }
        return failPermissions;
    }

    /**
     * 获取已授予的权限
     *
     * @param permissions  需要请求的权限组
     * @param grantResults 允许结果组
     */
    public static List<String> getSucceedPermissions(String[] permissions, int[] grantResults) {

        List<String> succeedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {

            //把授予过的权限加入到集合中，-1表示没有授予，0表示已经授予
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                succeedPermissions.add(permissions[i]);
            }
        }
        return succeedPermissions;
    }

    /**
     * 检测权限有没有在清单文件中注册
     *
     * @param activity           Activity对象
     * @param requestPermissions 请求的权限组
     */
    public static void checkPermissions(FragmentActivity activity, List<String> requestPermissions) {
        List<String> manifest = PermissionUtils.getManifestPermissions(activity);
        if (manifest != null && manifest.size() != 0) {
            for (String permission : requestPermissions) {
                if (!manifest.contains(permission)) {
                    throw new ManifestRegisterException(permission);
                }
            }
        } else {
            throw new ManifestRegisterException(null);
        }
    }

    public static List<PermissionState> getGrantedPermissions(String... permissions) {
        return getGrantedPermissions(true, permissions);
    }


    public static List<PermissionState> getGrantedPermissions(boolean granted, String... permissions) {
        return getGrantedPermissions(Arrays.asList(permissions), granted);
    }


    public static List<PermissionState> getGrantedPermissions(List<String> permissions) {
        return getGrantedPermissions(permissions, true);
    }

    public static List<PermissionState> getGrantedPermissions(List<String> permissions, boolean granted) {
        List<PermissionState> permissionStateList = new ArrayList<>();
        for (String s: permissions) {
            permissionStateList.add(new PermissionState(s, granted));
        }
        return permissionStateList;
    }

    public static List<PermissionState> getGrantedPermissions(Map<String, Boolean> stringMap) {
        List<PermissionState> permissionStateList = new ArrayList<>();
        if (null != stringMap && stringMap.size() > 0) {
            for (Map.Entry<String, Boolean> m: stringMap.entrySet()) {
                permissionStateList.add(new PermissionState(m.getKey(), m.getValue()));
            }
        }
        return permissionStateList;
    }
}
