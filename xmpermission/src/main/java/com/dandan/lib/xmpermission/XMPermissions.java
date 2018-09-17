package com.dandan.lib.xmpermission;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Tanzhenxing
 * Date: 2018/9/6 上午10:31
 * Description:
 */
public final class XMPermissions {

    private FragmentActivity mActivity;
    private List<String> mPermissions = new ArrayList<>();
    private boolean mConstant;
    static final Object TRIGGER = new Object();
    private XMPermissions(FragmentActivity activity) {
        mActivity = activity;
    }

    /**
     * 设置请求的对象
     */
    public static XMPermissions with(FragmentActivity activity) {
        return new XMPermissions(activity);
    }

    /**
     * 设置权限组
     */
    public XMPermissions permission(String... permissions) {
        mPermissions.addAll(Arrays.asList(permissions));
        return this;
    }

    /**
     * 设置权限组
     */
    public Observable<List<PermissionState>> request(final String[]... permissions) {
        for (String[] group : permissions) {
            mPermissions.addAll(Arrays.asList(group));
        }
        return create();
    }

    /**
     * 设置权限组
     */
    public Observable<List<PermissionState>> request(final String... permissions) {
        mPermissions.addAll(Arrays.asList(permissions));
        return create();
    }

    /**
     * 创建可观察对象
     * @return
     */
    private Observable<List<PermissionState>> create() {
        return Observable.just(TRIGGER)
                .flatMap(new Func1<Object, Observable<List<PermissionState>>>() {
                    @Override
                    public Observable<List<PermissionState>> call(Object o) {
                        return requestImplementation();
                    }
                });
    }


    /**
     * 申请观察对象
     * @return
     */
    private Observable<List<PermissionState>> requestImplementation() {
        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(mActivity, mPermissions);
        final List<PermissionState> permissionStateList = PermissionUtils.getGrantedPermissions(mPermissions);
        if (failPermissions == null || failPermissions.size() == 0) {
            //证明权限已经全部授予过
            return Observable.just(permissionStateList);
        } else {
            //申请没有授予过的权限
            PermissionFragment permissionFragment = PermissionFragment.newInstant((new ArrayList<>(mPermissions)), mConstant);
            //检测权限有没有在清单文件中注册
            PermissionUtils.checkPermissions(mActivity, mPermissions);
            permissionFragment.prepareRequest(mActivity, null);
            return permissionFragment.getmSubjects();
        }
    }

    /**
     * 设置权限组
     */
    public XMPermissions permission(String[]... permissions) {
        for (String[] group : permissions) {
            mPermissions.addAll(Arrays.asList(group));
        }
        return this;
    }

    /**
     * 设置权限组
     */
    public XMPermissions permission(List<String> permissions) {
        mPermissions.addAll(permissions);
        return this;
    }

    /**
     * 被拒绝后继续申请，直到授权或者永久拒绝
     */
    public XMPermissions constantRequest() {
        mConstant = true;
        return this;
    }

    /**
     * 请求权限
     */
    public void request(OnPermission call) {
        checkNull(call);
        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(mActivity, mPermissions);

        if (failPermissions == null || failPermissions.size() == 0) {
            //证明权限已经全部授予过
            call.hasPermission();
        } else {
            //检测权限有没有在清单文件中注册
            PermissionUtils.checkPermissions(mActivity, mPermissions);
            //申请没有授予过的权限
            PermissionFragment.newInstant((new ArrayList<>(mPermissions)), mConstant).prepareRequest(mActivity, call);
        }
    }

    /**
     * 检验参数
     * @param call
     */
    public void checkNull(OnPermission call) {
        if (mPermissions == null || mPermissions.size() == 0) {
            throw new IllegalArgumentException("The requested permission cannot be empty");
        }
        //使用isFinishing方法Activity在熄屏状态下会导致崩溃
        //if (mActivity == null || mActivity.isFinishing()) throw new IllegalArgumentException("Illegal Activity was passed in");
        if (mActivity == null) {
            throw new IllegalArgumentException("The activity is empty");
        }
        if (call == null) {
            throw new IllegalArgumentException("The permission request callback interface must be implemented");
        }
    }

    /**
     * 检查某些权限是否全部授予了
     *
     * @param context     上下文对象
     * @param permissions 需要请求的权限组
     */
    public static boolean isHasPermission(Context context, String... permissions) {
        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(context, Arrays.asList(permissions));
        return failPermissions == null || failPermissions.size() == 0;
    }

    /**
     * 检查某些权限是否全部授予了
     *
     * @param context     上下文对象
     * @param permissions 需要请求的权限组
     */
    public static boolean isHasPermission(Context context, String[]... permissions) {
        List<String> permissionList = new ArrayList<>();
        for (String[] group : permissions) {
            permissionList.addAll(Arrays.asList(group));
        }
        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(context, permissionList);
        return failPermissions == null || failPermissions.size() == 0;
    }

    /**
     * 跳转到应用权限设置页面
     *
     * @param context 上下文对象
     */
    public static void gotoPermissionSettings(Context context) {
        PermissionSettingPage.start(context, false);
    }

    /**
     * 跳转到应用权限设置页面
     *
     * @param context 上下文对象
     * @param newTask 是否使用新的任务栈启动
     */
    public static void gotoPermissionSettings(Context context, boolean newTask) {
        PermissionSettingPage.start(context, newTask);
    }
}
