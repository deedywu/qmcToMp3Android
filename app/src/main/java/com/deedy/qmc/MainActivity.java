package com.deedy.qmc;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.deedy.qmc.util.CatchException;
import com.deedy.qmc.util.FileQueue;
import com.deedy.qmc.util.Permission;
import com.deedy.qmc.util.ThreadRun;
import com.deedy.qmc.util.Util;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    protected Button b1;

    private static MainActivity instance;

    public static Context getInstance() {
        return instance;
    }

    private static ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setTitle(R.string.label_name);
        Permission.isGrantExternalRW(this, 1);
        setContentView(R.layout.activity_main);
        b1 = findViewById(R.id.b1);
        pd = new ProgressDialog(this);
        CatchException mException = CatchException.getInstance();
        Context context = getApplicationContext();
        mException.init(context);  //注册
        Util.deedyDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/deedy";
        // 创建保存文件夹
        File deedyDir = new File(Util.deedyDir);
        if (!deedyDir.exists()) {
            deedyDir.mkdir();
            File logDir = new File(deedyDir + "/log");
            logDir.mkdir();
        }
        b1.setOnClickListener(this);
    }

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
//                    MainActivity.pd.setProgress(msg.arg1);
                    break;
                }
                case 2: {
                    b1.setEnabled(true);
                    b1.setText(R.string.button01);
                    pd.dismiss();
                    break;
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/qqmusic/song";
        File MDir = new File(dir);
        if (MDir.exists() && MDir.isDirectory()) {
            File[] files = MDir.listFiles();
            if (files != null) {
                Util.addFile(files);
            }
            FileQueue.setMax();
            if (FileQueue.getMax() > 0) {
                b1.setEnabled(false);
                b1.setText("正在转换...");
                pd.setTitle("请稍等");
                //设置对话进度条样式为水平
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                //设置提示信息
                pd.setMessage("正在玩命转换中......");
                pd.setCancelable(false);//点击屏幕和按返回键都不能取消加载框
                //设置对话进度条显示在屏幕顶部（方便截图）
                pd.getWindow().setGravity(Gravity.CENTER);
                pd.show();//调用show方法显示进度条对话框
                String saveLog = "任务开始,有" + FileQueue.getMax() + "个文件待执行,请不要退出程序";
                Util.writeLog("正在转换", saveLog);
                Toast.makeText(MainActivity.getInstance(), saveLog, Toast.LENGTH_SHORT).show();
                final Thread thread = new Thread() {
                    @Override
                    public void run() {
                        final CountDownLatch latch = new CountDownLatch(4);
                        Looper.prepare();
                        Long start = System.currentTimeMillis();
                        for (int i = 0; i < 4; i++) {
                            new Thread(new ThreadRun(latch)).start();
                        }
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            Long end = System.currentTimeMillis();
                            Message msg = handler.obtainMessage();
                            msg.what = 2;
                            msg.arg2 = 2;
                            handler.sendMessage(msg);
                            String saveLog = "成功转换" + FileQueue.getMax() + "个文件,共耗费" + Util.getTimeStr(end - start);
                            Util.writeLog("转换成功", saveLog);
                            Toast.makeText(MainActivity.getInstance(), saveLog, Toast.LENGTH_SHORT).show();
                            FileQueue.reset();
                        }
                        Looper.loop();
                    }
                };
                thread.start();
            } else {
                Toast.makeText(MainActivity.getInstance(), "没有找到需要转换的文件", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.getInstance(), "qqmusic/song文件夹不存在", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //检验是否获取权限，如果获取权限，外部存储会处于开放状态，会弹出一个toast提示获得授权
                    String sdCard = Environment.getExternalStorageState();
                    if (sdCard.equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(this, "成功获得存储授权", Toast.LENGTH_LONG).show();
                        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "未获得存储权限,app将退出!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
