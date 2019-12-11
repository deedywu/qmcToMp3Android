package com.deedy.qmc.util;

import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.deedy.qmc.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ThreadRun extends MainActivity implements Runnable {

    private CountDownLatch cl;

    private volatile boolean isRun = true;

    public ThreadRun(CountDownLatch cl) {
        this.cl = cl;
    }

    @Override
    public void run() {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            while (isRun) {
                final String fileName = FileQueue.getFile();
                if (fileName == null) {
                    isRun = false;
                } else {
                    Util.writeLog("正在转换", "当前线程:" + Thread.currentThread().getName() + "正在执行:" + fileName);
                    final String newFileName = Util.formatName(new File(fileName));
                    fis = new FileInputStream(new File(fileName));
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    QMFDecode de = new QMFDecode();
                    for (int i = 0; i < buffer.length; ++i) {
                        buffer[i] = (byte) (de.NextMask() ^ buffer[i]);
                    }
                    fos = new FileOutputStream(new File(newFileName));
                    fos.write(buffer);
                    fos.flush();
                    FileQueue.addCurrent();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            Message msg = handler.obtainMessage();
                            msg.what = 1;
                            msg.arg1 = FileQueue.getCurrent();
                            handler.sendMessage(msg);
                            String saveLog = newFileName + " 已转换完成\n总任务(" + FileQueue.getCurrent() + "/" + FileQueue.getMax() + ")";
                            Util.writeLog("转换成功", saveLog);
                            Toast.makeText(MainActivity.getInstance(), saveLog, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    };
                    thread.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            cl.countDown();
        }
    }
}
