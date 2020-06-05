package com.example.ggxiaozhi.store.the_basket.mvp.view.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.utils.ToastUtils;
import com.example.ggxiaozhi.store.the_basket.R;
import com.example.ggxiaozhi.store.the_basket.api.CategorySubjectApi;
import com.example.ggxiaozhi.store.the_basket.api.DownLoadApkApi;
import com.example.ggxiaozhi.store.the_basket.api.HttpGetService;
import com.example.ggxiaozhi.store.the_basket.base.BaseActivity;
import com.zhxu.library.api.BaseApi;
import com.zhxu.library.download.DownInfo;
import com.zhxu.library.download.DownLoadListener.DownloadProgressListener;
import com.zhxu.library.download.DownState;
import com.zhxu.library.download.HttpDownManager;
import com.zhxu.library.http.HttpManager;
import com.zhxu.library.listener.HttpDownOnNextListener;
import com.zhxu.library.listener.HttpOnNextListener;

import java.io.File;

import butterknife.BindView;

public class uploadeApkActivity extends BaseActivity {
    String[] PERMISSION_STORAGES = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//写内存卡的权限
            Manifest.permission.READ_EXTERNAL_STORAGE,//读内存卡的权限
    };
    private static final int REQUEST_CODE_STORAGE = 1;

    @BindView(R.id.btn_upload)
    Button btn_upload;
    private File outFile;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private Notification notification;

    @Override
    protected void initLayout() {
        setContentView(R.layout.activity_upload);
    }

    @Override
    protected void initView() {
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    requestStoragePermission(uploadeApkActivity.this);
                } else {
                    downloadApk();
                }
            }
        });
    }

    private void requestStoragePermission(Activity activity) {
        int permission = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //没有权限 则申请权限  弹出对话框
            ActivityCompat.requestPermissions(activity, PERMISSION_STORAGES, REQUEST_CODE_STORAGE);
        } else {
            downloadApk();
        }
    }

    /**
     * 申请权限结果回调
     *
     * @param requestCode  请求码
     * @param permissions  申请权限的数组
     * @param grantResults 申请权限成功与否的结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //申请成功
                    downloadApk();
                } else {
                    //申请失败
                    ToastUtils.showShortToast("授权SD卡权限失败 可能会影响应用的使用");
                }
                break;
        }

    }

    private void downloadApk() {
        String packageName = "海量";
        outFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), packageName + ".apk");
        HttpDownManager mHttpDownManager = HttpDownManager.getInstance();
        DownInfo downInfo = new DownInfo("http://dldir1.qq.com/weixin/android/weixin667android1320.apk");
        downInfo.setListener(downLoadListener);
        downInfo.setId((long) packageName.hashCode());
        downInfo.setSavePath(outFile.getAbsolutePath());
        downInfo.setState(DownState.START);
        mHttpDownManager.startDown(downInfo);
    }

    private HttpDownOnNextListener downLoadListener = new HttpDownOnNextListener() {
        @Override
        public void onNext(Object o) {
            downloadSuccess();
        }

        @Override
        public void onStart() {
            initNotification();
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
            super.onError(e);
            ToastUtils.showLongToast("下载失败！");
        }

        @Override
        public void updateProgress(long readLength, long countLength) {
            int progress = (int) ((readLength * 100) / countLength);
            builder.setProgress(100, progress, false);
            builder.setContentText("下载进度:" + progress + "%");
            notification = builder.build();
            notificationManager.notify(1, notification);
        }
    };

    private void initNotification() {
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            //只在Android O之上需要渠道
            NotificationChannel notificationChannel = new NotificationChannel("channelid","channelname",NotificationManager.IMPORTANCE_HIGH);
            //如果这里用IMPORTANCE_NOENE就需要在系统的设置里面开启渠道，通知才能正常弹出
            notificationManager.createNotificationChannel(notificationChannel);
        }
        builder = new NotificationCompat.Builder(this,"channelid");
        builder.setContentTitle("正在更新...") //设置通知标题
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)) //设置通知的大图标
                .setDefaults(Notification.DEFAULT_LIGHTS) //设置通知的提醒方式： 呼吸灯
                .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级：最大
                .setAutoCancel(false)//设置通知被点击一次是否自动取消
                .setContentText("下载进度:" + "0%")
                .setProgress(100, 0, false);
        notification = builder.build();//构建通知对象
    }

    private void downloadSuccess () {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            boolean installAllowed = getPackageManager().canRequestPackageInstalls();//是否允许安装包
            if (installAllowed) {
                installApk(outFile);//允许，安装
            } else {
                //跳转到设置页面，设置成允许安装
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
//                installApk(file);
                return;
            }

        } else { //版本低于8.0
            installApk(outFile);
        }
    }

    private void installApk(File file) {
        Uri uri = null;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//为intent 设置特殊的标志，会覆盖 intent 已经设置的所有标志。
            if (Build.VERSION.SDK_INT >= 24) {//7.0 以上版本利用FileProvider进行访问私有文件
                uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//为intent 添加特殊的标志，不会覆盖，只会追加。
            } else {
                //直接访问文件
                uri = Uri.fromFile(file);
                intent.setAction(Intent.ACTION_VIEW);
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
