package com.videorecognition;

import com.xuggle.xuggler.IContainer;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final String pathToRoot = "c:\\tmp\\vr";
    private static final String orig = "\\origin";
    private static final String fileName = "\\00012.MTS";
    private static final String sampleDirName = "\\samples";
    private static final String processedFilesDirName = "\\processedFiles";
    private static final String pathToSamples = pathToRoot + sampleDirName;
    private static final String pathToProcessedFiles = pathToRoot + processedFilesDirName;
    private static FileManager fm = null;

    public static void main(String[] args) {
        String pathToVideoFile = pathToRoot + fileName;
        if(!isExists(pathToVideoFile, true)){
            System.out.println("Please, specify pathToRoot and fileName of video file! Operation abort!");
            return;
        }
        String pathToOriginFiles = pathToRoot + orig;
        if(!isExists(pathToOriginFiles, false)){
            System.out.println("Please, specify pathToRoot and name of file that contains original files! Operation abort!");
            return;
        }
        fm = new FileManager(pathToOriginFiles, pathToSamples, pathToProcessedFiles);
        // Create 2 threads for separating and add to executor
        createThreadsForSeparating(2, pathToVideoFile, pathToSamples);
        // Create 2 threads for comparing and add to executor
        //createThreadsForRecognition();
        executor.isShutdown();


    }

    private static void createThreadsForRecognition(Integer countOfThreads, String pathToSamples, String pathToOrigin) {
        File samplesDir = new File(pathToSamples);
        File[] sampleFiles = fm.listOfFiles(samplesDir);
        LinkedList<File> first = new LinkedList<File>();
        LinkedList<File> second = new LinkedList<File>();
        LinkedList<File> third = new LinkedList<File>();
        int counter = 1;
        for(File f : sampleFiles){
            if (counter == 1) {
                first.add(f);
                counter++;
                continue;
            }else if (counter == 2) {
                second.add(f);
                counter++;
                continue;
            }else if (counter == 3) {
                third.add(f);
                counter = 1;
            }
        }
    }

    private static boolean isExists(String pathToVideoFile, boolean isFile) {
        File f = new File(pathToVideoFile);
        if(isFile)
            return f.isFile();
        else
            return f.isDirectory();
    }

    private static void createThreadsForSeparating(Integer countOfThreads, String pathToVideoFile, String pathToOutputDir){
        //check if countthread > 0 and string is not empty and path is exists
        Long startTimestamp = 0L;
        Long period = calculatePeriod(pathToVideoFile, countOfThreads);
        for(int i = 0; i < countOfThreads; i++){
            Thread th = new VideoSeparator(startTimestamp, startTimestamp + period, pathToVideoFile, pathToOutputDir);
            th.setName("videoseparator_" + i);
            startTimestamp += period;
            executor.submit(th);
        }
    }

    private static Long calculatePeriod(String pathToVideoFile, Integer threadCounter){
        IContainer container = IContainer.make();
        if (container.open(pathToVideoFile, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("could not open file: "
                    + pathToVideoFile);
        }
        return container.getDuration()/threadCounter;
    }
}
