package com.videorecognition;

import com.xuggle.xuggler.IContainer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final String pathToRoot = "c:\\tmp\\vr";
    private static final String orig = "\\origin";
    private static final String fileName = "\\test_1.mov";
    private static final String sampleDirName = "\\samples";
    private static final String processedFilesDirName = "\\processedFiles";
    private static final String pathToSamples = pathToRoot + sampleDirName;
    private static final String pathToProcessedFiles = pathToRoot + processedFilesDirName;
    private static FileManager fm = null;

    public static void main(String[] args) throws InterruptedException, IOException {
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
        //createThreadsForSeparating(1, pathToVideoFile, pathToSamples);
        //TimeUnit.SECONDS.sleep(20);
        // Create 2 threads for comparing and add to executor
        createThreadsForRecognition(1, pathToSamples, pathToOriginFiles);
        executor.isShutdown();


    }

    private static void createThreadsForRecognition(Integer countOfThreads, String pathToSamples, String pathToOrigin) throws InterruptedException, IOException {
        File samplesDir = new File(pathToSamples);
        int counterOFAttempt = 0;
        Map<Integer, List<File>> deviderOfFiles = new HashMap<Integer, List<File>>();
        List<String> pathOfSamplesForThreads = new LinkedList<String>();
        Boolean isFirstLoop = true;
         int total = 0;

        while(counterOFAttempt < 4) {
            System.out.println("START LOOP!!!");
            if(fm.listOfFiles(samplesDir).length == 0){
                counterOFAttempt++;
                TimeUnit.SECONDS.sleep(10);
                continue;
            }
            File[] sampleFiles = fm.listOfFiles(samplesDir);
            int counter = 1;
            for (File f : sampleFiles) {
                if(counter > countOfThreads){
                    counter = 1;
                }
                if(deviderOfFiles.containsKey(counter))
                    deviderOfFiles.get(counter).add(f);
                else{
                    List<File> lst = new LinkedList<File>();
                    lst.add(f);
                    deviderOfFiles.put(counter, lst);
                }
                counter++;

            }
            if(isFirstLoop){
                isFirstLoop = false;
                for(Map.Entry<Integer, List<File>> entry : deviderOfFiles.entrySet()){
                    String pathForThreadSamples = pathToRoot + "\\thread_JS_" + entry.getKey().toString();
                    FileUtils.deleteDirectory(new File(pathForThreadSamples));

                    pathOfSamplesForThreads.add(pathForThreadSamples);
                    fm.moveFilesToDir(entry.getValue(), pathForThreadSamples);
                    System.out.println("entry with number " + entry.getKey() + " contains " + entry.getValue());
                    fm.deleteFiles(entry.getValue());
                    total += entry.getValue().size();
                    executor.execute(new JavaSift(pathForThreadSamples, pathToOrigin,fm ));
                }

            }else{
                for(Map.Entry<Integer, List<File>> entry : deviderOfFiles.entrySet()){
                    String pathForThreadSamples = pathToRoot + "\\thread_JS_" + entry.getKey().toString();
                    System.out.println("entry with number " + entry.getKey() + " contains " + entry.getValue());
                    fm.moveFilesToDir(entry.getValue(), pathForThreadSamples);
                    total += entry.getValue().size();
                    fm.deleteFiles(entry.getValue());
                }
            }
            deviderOfFiles.clear();
            TimeUnit.SECONDS.sleep(5);
        }
        System.out.println("TOTAL - " + total);

        /* TEST
        int total = 0;
        for(Map.Entry<Integer, List<File>> entry : deviderOfFiles.entrySet()){
            System.out.println("INTEGER - " + entry.getKey());
            System.out.println("COUNTER - " + entry.getValue().size());

        }
        System.out.println("TOTAL - " + total);*/
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
            Thread th = new VideoSeparator(startTimestamp, startTimestamp + period, pathToVideoFile, pathToOutputDir, fm);
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
