package com.deedy.qmc.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

    public static String formatName(File file) {
        String fileName = file.getName();
        String allPath = file.getPath();
        String dir = allPath.replace("qqmusic/song/" + fileName, "deedy");
//        File newDir = new File(dir);
//        if (!newDir.exists()) {
//            newDir.mkdir();
//            File logDir = new File(dir + "/log");
//            logDir.mkdir();
//        }
        fileName = dir + "/" + fileName;
        if (fileName.endsWith(".qmcflac")) {
            return fileName.replace(".qmcflac", ".mp3");
        } else if (fileName.endsWith(".qmc3")) {
            return fileName.replace(".qmc3", ".mp3");
        } else if (fileName.endsWith(".qmc0")) {
            return fileName.replace(".qmc0", ".mp3");
        }
        return null;
    }

    public static void addFile(File[] files) {
        for (File file : files) {
            String newFileName = formatName(file);
            if (newFileName != null && !new File(newFileName).exists())
                FileQueue.addFile(file.getAbsolutePath());
        }
    }

    public static String getTimeStr(Long time) {
        if (time < 1000) {
            return time + "毫秒";
        }
        return ((int) (time / 1000)) + "秒" + (time % 1000 > 0 ? time % 1000 + "毫秒" : "");
    }

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String deedyDir = "";

    private static String getDatetimeStr() {
        return sdf.format(new Date());
    }

    public static void writeLog(String err, String msg) {
        File file = new File(deedyDir + "/log/log.txt");
        FileWriter fw = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            //设置为:True,表示写入的时候追加数据
            fw = new FileWriter(file, true);
            String writeDate = getDatetimeStr() + "---" + err + "---" + msg;
            //回车并换行
            fw.write(writeDate + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
