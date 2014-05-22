/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.videorecognition;

import java.io.File;

/**
 *
 * @author Mike
 */
public class JavaSift extends Thread {
    private final File processingDir;
    private final File originalsDir;
    private final FileManager fm;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Test2("c:\\tmp\\1.png","c:\\tmp\\2.jpg").test(null, null);
    }

    public JavaSift(String pathToProcessingDir, String pathToOriginals, FileManager fm){
        this.processingDir = new File(pathToProcessingDir);
        this.originalsDir = new File(pathToOriginals);
        this.fm = fm;
    }

    @Override
    public void run(){
        
    }
}
