package com.dandan.lib.xmpermission;

/**
 * Created by Tanzhenxing
 * Date: 2018/9/6 上午10:26
 * Description:
 */
class ManifestRegisterException extends RuntimeException {

    ManifestRegisterException(String permission) {
        super(permission == null ?
                "No permissions are registered in the manifest file" :
                (permission + ": Permissions are not registered in the manifest file"));
    }
}
