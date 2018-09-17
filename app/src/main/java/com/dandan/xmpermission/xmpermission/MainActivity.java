package com.dandan.xmpermission.xmpermission;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dandan.lib.xmpermission.OnPermission;
import com.dandan.lib.xmpermission.Permission;
import com.dandan.lib.xmpermission.PermissionState;
import com.dandan.lib.xmpermission.SimpleSubscriber;
import com.dandan.lib.xmpermission.XMPermissions;

import java.io.File;
import java.util.List;

import rx.Subscriber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.requestPermission).setOnClickListener(v -> requestPermissionRx(v));
        findViewById(R.id.sdcard).setOnClickListener(v -> {
            searchFile("d");
        });
    }

    public void requestPermission(View view) {
        XMPermissions.with(this)
                //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                //.permission(Permission.SYSTEM_ALERT_WINDOW, Permission.REQUEST_INSTALL_PACKAGES) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.Group.STORAGE, Permission.Group.CALENDAR) //不指定权限则自动获取清单中的危险权限
                .request(new OnPermission() {

                    @Override
                    public void hasPermission() {
                        Toast.makeText(MainActivity.this, "获取权限成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void noPermission(List<PermissionState> granteds) {
                        for (PermissionState s: granteds) {
                            Log.d("tanzhenxing:", s.toString());
                        }
                    }

                });
    }

    public void requestPermissionRx(View view) {
        XMPermissions.with(this)
                .request(Permission.Group.STORAGE, Permission.Group.CALENDAR)
                .subscribe(new SimpleSubscriber<List<PermissionState>>() {
                    @Override
                    public void onNext(List<PermissionState> permissionStates) {
                        for (PermissionState s: permissionStates) {
                            Log.d("tanzhenxing:", s.toString());
                        }
                    }
                });
    }

    public void isHasPermission(View view) {
        if (XMPermissions.isHasPermission(MainActivity.this, Permission.Group.STORAGE)) {
            Toast.makeText(MainActivity.this, "已经获取到权限，不需要再次申请了", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(MainActivity.this, "还没有获取到权限或者部分权限未授予", Toast.LENGTH_SHORT).show();
        }
    }

    public void gotoPermissionSettings(View view) {
        XMPermissions.gotoPermissionSettings(MainActivity.this);
    }

    private String searchFile(String keyword) {
        String result = "";
        File[] files = Environment.getExternalStorageDirectory().listFiles();
        for (File file : files) {
            if (file.getName().indexOf(keyword) >= 0) {
                result += file.getPath() + "\n";
            }
        }
        if (result.equals("")){
            result = "找不到文件!!";
        }
        return result;
    }
}
