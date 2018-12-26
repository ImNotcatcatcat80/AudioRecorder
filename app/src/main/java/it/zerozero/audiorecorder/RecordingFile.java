package it.zerozero.audiorecorder;

import android.util.Log;

import java.io.File;

/**
 * Created by David on 30/04/2018.
 */

public class RecordingFile {

    private static File directory;
    private File file;
    private String fileName;
    private long size;
    private int duration;
    private long lastModified;

    public RecordingFile(File file) {
        this.file = file;
        this.fileName = file.getName();
        this.size = file.length();
        this.lastModified = file.lastModified();
    }

    public static void setDirectory(File dir) {
        if (dir.isDirectory()) {
            directory = dir;
        }
        else Log.e("RecordingFile", "argument of setDirectory(File) is not a directory!");
    }

    public static File getDirectory() {
        return directory;
    }

    public void delete() {
        if (file != null) {
            this.file.delete();
        }
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }

    public long getLastModified() {
        return lastModified;
    }

}
