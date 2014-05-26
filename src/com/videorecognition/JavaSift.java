/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.videorecognition;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Mike
 */
public class JavaSift extends Thread {
    private final File processingDir;
    private final File originalsDir;
    private final FileManager fm;

    public JavaSift(String pathToProcessingDir, String pathToOriginals, FileManager fm) {
        this.processingDir = new File(pathToProcessingDir);
        this.originalsDir = new File(pathToOriginals);
        this.fm = fm;
    }

    @Override
    public void run() {
        int counter = 0;
        while(counter < 4){
            if(this.processingDir.list().length == 0){
                try {
                    TimeUnit.SECONDS.sleep(30);
                    counter++;
                } catch (InterruptedException e) {
                    counter = 4;
                    continue;
                }
            }
            File[] processingFiles = this.processingDir.listFiles();
            File[] originalsFiles = this.originalsDir.listFiles();
            for(File processingFile : processingFiles){
                for (File originFile : originalsFiles){
                    try {
                        new SiftProcessor(processingFile, originFile, fm, 0).processing(null, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                processingFile.delete();
            }
        }

    }
}
