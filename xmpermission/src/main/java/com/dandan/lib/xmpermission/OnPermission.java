package com.dandan.lib.xmpermission;

import java.util.List;

/**
 * Created by Tanzhenxing
 * Date: 2018/9/6 上午10:27
 * Description:
 */
public interface OnPermission {
    /**
     * 本次申请的权限全部通过
     */
    void hasPermission();

    /**
     * 本次申请的权限没有全部通过
     * @param granteds
     */
    void noPermission(List<PermissionState> granteds);
}
