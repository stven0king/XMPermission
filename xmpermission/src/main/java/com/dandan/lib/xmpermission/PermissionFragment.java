package com.dandan.lib.xmpermission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.subjects.PublishSubject;

/**
 * Created by Tanzhenxing
 * Date: 2018/9/6 上午10:28
 * Description:
 */
public final class PermissionFragment extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 0X12;

    private final static SparseArray<OnPermission> sContainer = new SparseArray<>();
    private PublishSubject<List<PermissionState>> mSubjects;
    private Map<String, Boolean> resultMap = new HashMap<>();
    private ArrayList<String> permissions;
    private OnPermission call;
    private boolean constant;

    public PublishSubject<List<PermissionState>> getmSubjects() {
        return mSubjects;
    }

    public static PermissionFragment newInstant(ArrayList<String> permissions, boolean constant) {
        PermissionFragment fragment = new PermissionFragment();
        fragment.permissions = permissions;
        fragment.constant = constant;
        return fragment;
    }

    /**
     * 准备请求
     */
    public void prepareRequest(FragmentActivity activity, OnPermission callBack) {
        //将当前的请求码和对象添加到集合中
        this.call = callBack;
        if (null == call) {
            mSubjects = PublishSubject.create();
        }
        activity.getSupportFragmentManager().beginTransaction().add(this, activity.getClass().getName()).commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (permissions == null) return;
        for (String s: permissions) {
            resultMap.put(s, false);
        }
        if ((permissions.contains(Permission.REQUEST_INSTALL_PACKAGES) && !PermissionUtils.isHasInstallPermission(getActivity()))
                || (permissions.contains(Permission.SYSTEM_ALERT_WINDOW) && !PermissionUtils.isHasOverlaysPermission(getActivity()))) {

            if (permissions.contains(Permission.REQUEST_INSTALL_PACKAGES) && !PermissionUtils.isHasInstallPermission(getActivity())) {
                //跳转到允许安装未知来源设置页面
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getActivity().getPackageName()));
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }

            if (permissions.contains(Permission.SYSTEM_ALERT_WINDOW) && !PermissionUtils.isHasOverlaysPermission(getActivity())) {
                //跳转到悬浮窗设置页面
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }

        } else {
            requestPermission();
        }
    }

    /**
     * 请求权限
     */
    public void requestPermission() {
        if (PermissionUtils.isOverMarshmallow()) {
            requestPermissions(permissions.toArray(new String[permissions.size() - 1]), PERMISSION_REQUEST_CODE);
        } else {
            //代表申请的所有的权限都授予了
            if (null != call) {
                call.hasPermission();
            } else {
                mSubjects.onNext(PermissionUtils.getGrantedPermissions(permissions));
                mSubjects.onCompleted();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (Permission.REQUEST_INSTALL_PACKAGES.equals(permissions[i])) {
                    if (PermissionUtils.isHasInstallPermission(getActivity())) {
                        grantResults[i] = PackageManager.PERMISSION_GRANTED;
                    } else {
                        grantResults[i] = PackageManager.PERMISSION_DENIED;
                    }
                }

                if (Permission.SYSTEM_ALERT_WINDOW.equals(permissions[i])) {
                    if (PermissionUtils.isHasOverlaysPermission(getActivity())) {
                        grantResults[i] = PackageManager.PERMISSION_GRANTED;
                    } else {
                        grantResults[i] = PackageManager.PERMISSION_DENIED;
                    }
                }
            }
            //获取授予权限
            List<String> succeedPermissions = PermissionUtils.getSucceedPermissions(permissions, grantResults);
            for (String s : permissions) {
                resultMap.put(s, succeedPermissions.contains(s));
            }
            //如果请求成功的权限集合大小和请求的数组一样大时证明权限已经全部授予
            if (succeedPermissions.size() == permissions.length) {
                //代表申请的所有的权限都授予了
                if (null != call) {
                    call.hasPermission();
                } else {
                    mSubjects.onNext(PermissionUtils.getGrantedPermissions(permissions));
                    mSubjects.onCompleted();
                }
            } else {

                //获取拒绝权限
                List<String> failPermissions = PermissionUtils.getFailPermissions(permissions, grantResults);
                //检查是否开启了继续申请模式，如果是则检查没有授予的权限是否还能继续申请
                if (constant && PermissionUtils.isRequestDeniedPermission(getActivity(), failPermissions)) {
                    //如果有的话就继续申请权限，直到用户授权或者永久拒绝
                    requestPermission();
                    return;
                }
                //证明还有一部分权限被成功授予，回调成功接口
                if (null != call) {
                    call.noPermission(PermissionUtils.getGrantedPermissions(resultMap));
                } else {
                    mSubjects.onNext(PermissionUtils.getGrantedPermissions(resultMap));
                    mSubjects.onCompleted();
                }
            }
            //权限回调结束后要删除集合中的对象，避免重复请求
            sContainer.remove(requestCode);
            getFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    private boolean isBackCall;//是否已经回调了，避免安装权限和悬浮窗同时请求导致的重复回调

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!isBackCall && requestCode == PERMISSION_REQUEST_CODE) {
            isBackCall = true;
            //需要延迟执行，不然有些华为机型授权了但是获取不到权限
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    //请求其他危险权限
                    requestPermission();
                }
            }, 500);
        }
    }
}
