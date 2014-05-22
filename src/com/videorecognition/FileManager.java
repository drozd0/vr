package com.videorecognition;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Common on 22.05.2014.
 */
public class FileManager {
    private final File originals;
    private final File samples;
    private final File result;

    public FileManager(String pathToOriginals, String pathToSamples, String pathToResult){
        this.originals = new File (pathToOriginals);
        this.samples = new File (pathToSamples);
        this.result = new File (pathToResult);
    }

    public synchronized void copyDirs(String pathFromDir, String pathToDir) throws IOException {
        File dirFrom = new File(pathFromDir);
        File dirTo = new File(pathToDir);
        FileUtils.copyDirectory(dirFrom, dirTo);
    }

    public synchronized void copyFilesToDir(File[] moveFilesToDir, String pathToDir) throws IOException {
        File dir = new File(pathToDir);
        for(File f : moveFilesToDir)
            FileUtils.moveFileToDirectory(f, dir, true);
    }

    public File[] listOfFiles(File dir){
        if(dir.isDirectory())
            return dir.listFiles();
        else
            return new File[]{};
    }


    public File getOriginals() {
        return originals;
    }

    public File getSamples() {
        return samples;
    }

    public File getResult() {
        return result;
    }
}
