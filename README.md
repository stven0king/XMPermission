# XMPermission
简单易用的处理Android M运行时权限的库, `XMPermission` 意味对 [XXPermissions](https://github.com/getActivity/XXPermissions) 项目的二次开发。

在原有的项目的基础之上优化：

- 支持Rxjava
- 修改回调方法，更加清晰

## 依赖

```java
implementation 'com.tzx.lib:xmpermission:1.0.0'
```

## 使用

```java
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
```

### RxJava


```java
XMPermissions.with(this)
    .request(Permission.Group.STORAGE, Permission.Group.PHONE)
    .subscribe(new SimpleSubscriber<List<PermissionState>>() {
        @Override
        public void onNext(List<PermissionState> permissionStates) {
            for (PermissionState s: permissionStates) {
                Log.d("tanzhenxing:", s.toString());
            }
        }
    });
```

### 链式调用

```java
XMPermissions.with(this)
                //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE, Permission.Group.CALENDAR) 
                .request(new OnPermission() {

                    @Override
                    public void hasPermission() {
                        Toast.makeText(MainActivity.this, "获取权限成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void noPermission(List<PermissionState> granteds) {
                        for (PermissionState s: granteds) {
                            Log.d("tzx:", s.toString());
                        }
                    }

                });
```

### 跳转到应用权限设置页面

```java
XMPermissions.gotoPermissionSettings(content);
```

## LICENSE

```lis
Copyright 2018 stven0king, All right reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

