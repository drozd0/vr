/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.videorecognition;

import com.xuggle.xuggler.IContainer;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Mike
 */
public class XugglerVideoSeparator {
    private final static String filename = "C:\\tmp\\00012.MTS";
    private final static File outdir = new File("C:\\tmp\\12");
    public static void main(String[] args){
        if(!new File(filename).exists()){
            throw new IllegalArgumentException("File["
                    + filename + "] does not exists!");
        }

        if(!outdir.exists())
            outdir.mkdir();
        IContainer container = IContainer.make();

        if (container.open(filename, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("could not open file: "
                    + filename);
        }
        System.out.println(container.getDuration());
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new VideoSeparator(0l, container.getDuration(), filename, outdir));///8, filename, outdir));
        /*executor.submit(new VideoSeparator(((container.getDuration()/8)+1), container.getDuration()/4, filename, outdir));
        executor.submit(new VideoSeparator(((container.getDuration()/4) + 1), 3*(container.getDuration()/8), filename, outdir));
        executor.submit(new VideoSeparator((3*(container.getDuration()/8) + 1), container.getDuration()/2, filename, outdir));
        executor.submit(new VideoSeparator(container.getDuration()/2, 5*(container.getDuration()/8), filename, outdir));
        executor.submit(new VideoSeparator(((5*(container.getDuration()/8))+1), 6*(container.getDuration()/8), filename, outdir));
        executor.submit(new VideoSeparator(((6*(container.getDuration()/8)) + 1), 7*(container.getDuration()/8), filename, outdir));
        executor.submit(new VideoSeparator((7*(container.getDuration()/8) + 1), container.getDuration(), filename, outdir));*/
        executor.shutdown();
    }
}
