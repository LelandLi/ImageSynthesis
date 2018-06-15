package com.lelandli.imagesynthesis;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * 当前类注释:
 * 项目名:NewFace
 * 包名:com.newface.android
 * 作者:think on 2017/12/18
 * 邮箱:android_liang@163.com
 * QQ:320335698
 * 公司:山东兴旺电力线路器材有限公司
 */

public class PermissionRequestBroken implements ActivityCompat.OnRequestPermissionsResultCallback{
    private Activity mActivity;
    private PermissionRequestBack mPermissionRequestBack;
    public PermissionRequestBroken(Activity mActivity) {
        this.mActivity = mActivity;
    }
    /**
     * 判断权限是都全部通过
     * @param permissions
     * @return
     */
    public boolean isPermissionAllGranted(String[] permissions){
        //判断权限是否通过了
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    /**
     * 请求权限
     * @param permissions
     */
    public void askPermissions(String[] permissions){
        ActivityCompat.requestPermissions(mActivity,permissions,100);
    }

    public void onPermissionsResult(PermissionRequestBack mPermissionRequestBack){
        this.mPermissionRequestBack =mPermissionRequestBack;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                // 如果所有的权限都授予了, 则执行备份代码
                mPermissionRequestBack.onPermissionRequest(true);
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                mPermissionRequestBack.onPermissionRequest(false);
            }
        }
    }
}
