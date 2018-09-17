package com.dandan.lib.xmpermission;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tanzhenxing
 * Date: 2018/9/12 下午5:13
 * Description: 申请权限状态
 */
public class PermissionState implements Parcelable {

    private String name;
    private boolean granted;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeByte(this.granted ? (byte) 1 : (byte) 0);
    }

    public PermissionState() {
    }

    public PermissionState(String name, boolean granted) {
        this.name = name;
        this.granted = granted;
    }

    protected PermissionState(Parcel in) {
        this.name = in.readString();
        this.granted = in.readByte() != 0;
    }

    public static final Parcelable.Creator<PermissionState> CREATOR = new Parcelable.Creator<PermissionState>() {
        @Override
        public PermissionState createFromParcel(Parcel source) {
            return new PermissionState(source);
        }

        @Override
        public PermissionState[] newArray(int size) {
            return new PermissionState[size];
        }
    };

    @Override
    public String toString() {
        return "permissionName:" + name + "  " + granted;
    }

    public static List<PermissionState> getGrantes(List<PermissionState> list) {
       return filter(list, true);
    }

    public static List<PermissionState> getDenieds(List<PermissionState> list) {
        return filter(list, false);
    }

    private static List<PermissionState> filter(List<PermissionState> list, boolean granted) {
        List<PermissionState> permissionStateList = null;
        if (null != list && list.size() > 0) {
            permissionStateList = new ArrayList<>();
            for (PermissionState state: list) {
                if (state.granted == granted) {
                    permissionStateList.add(state);
                }
            }
        }
        return permissionStateList;
    }
}
