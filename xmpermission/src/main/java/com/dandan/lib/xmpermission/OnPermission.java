package com.dandan.lib.xmpermission;

import java.util.List;

/**
 * Created by Tanzhenxing
 * Date: 2018/9/6 上午10:27
 * Description:
 */
public interface OnPermission {
    void hasPermission();

    void noPermission(List<PermissionState> granteds);
}
