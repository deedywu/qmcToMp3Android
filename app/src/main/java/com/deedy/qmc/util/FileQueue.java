package com.deedy.qmc.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FileQueue {

    private static final ConcurrentLinkedQueue<String> FILE_LIST = new ConcurrentLinkedQueue<>();

    public static void addFile(String fileName) {
        FILE_LIST.add(fileName);
    }

    public synchronized static String getFile() {
        return FILE_LIST.poll();
    }

    public static void reset() {
        max = 0;
        current = 0;
        FILE_LIST.clear();
    }

    public synchronized static int getSize() {
        return FILE_LIST.size();
    }

    private static Integer max = 0;

    public static void setMax() {
        max = FILE_LIST.size();
    }

    public static Integer getMax() {
        return max;
    }

    private volatile static Integer current = 0;

    public synchronized static int getCurrent() {
        return current;
    }

    public synchronized static void addCurrent() {
        current += 1;
    }


}
