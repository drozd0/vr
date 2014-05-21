/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.videorecognition;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import mpi.cbg.fly.*;

import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.Vector;

/**
 *
 * @author Mike
 */
public class Test {

    //ImagePlus im1 = new ImagePlus("C:\\Users\\Mike\\Desktop\\магистерская\\wallpaper-2068954.jpg");
    ImagePlus im1 = new ImagePlus("C:\\Users\\Mike\\Desktop\\магистерская\\2.jpg");
    ImagePlus im2 = new ImagePlus("C:\\Users\\Mike\\Desktop\\магистерская\\1.jpg");
    
   private int fdsize = 4;
            private int fdbins = 8;
            private boolean upscale = true;
                    private int steps = 3; // or 0
            private float initial_sigma = 1.6f;
                    private int min_size = 64;
                    private int max_size = 1024;
                    //show_info
                      //      vis_scale

    public void test() {
        
        
        Vector< Feature > fs1;
	    Vector< Feature > fs2;
        
        ImageProcessor ip1;
        ImageProcessor ip2;
        
        ip2 = im1.getProcessor().convertToFloat();

        AffineTransform at = new AffineTransform();

        FloatArray2DSIFT sift = new FloatArray2DSIFT(fdsize, fdbins);

        FloatArray2D fa = ImageArrayConverter.ImageToFloatArray2D(ip2);
        Filter.enhance(fa, 1.0f);

        if (upscale) {
            FloatArray2D fat = new FloatArray2D(fa.width * 2 - 1, fa.height * 2 - 1);
            FloatArray2DScaleOctave.upsample(fa, fat);
            fa = fat;
            fa = Filter.computeGaussianFastMirror(fa, (float) Math.sqrt(initial_sigma * initial_sigma - 1.0));
        } else {
            fa = Filter.computeGaussianFastMirror(fa, (float) Math.sqrt(initial_sigma * initial_sigma - 0.25));
        }

        long start_time = System.currentTimeMillis();
        System.out.print("processing SIFT ...");
        sift.init(fa, steps, initial_sigma, min_size, max_size);
        fs2 = sift.run(max_size);
        Collections.sort(fs2);
        System.out.println(" took " + (System.currentTimeMillis() - start_time) + "ms");

        System.out.println(fs2.size() + " features identified and processed");

        // downscale ip2 for visualisation purposes
        /*if (show_info) {
            ip2 = downScale((FloatProcessor) ip2, vis_scale);
        }*/
//***************************************************
            ip1 = ip2;
            ip2 = im2.getProcessor().convertToFloat();
            fa = ImageArrayConverter.ImageToFloatArray2D(ip2);
            Filter.enhance(fa, 1.0f);

            if (upscale) {
                FloatArray2D fat = new FloatArray2D(fa.width * 2 - 1, fa.height * 2 - 1);
                FloatArray2DScaleOctave.upsample(fa, fat);
                fa = fat;
                fa = Filter.computeGaussianFastMirror(fa, (float) Math.sqrt(initial_sigma * initial_sigma - 1.0));
            } else {
                fa = Filter.computeGaussianFastMirror(fa, (float) Math.sqrt(initial_sigma * initial_sigma - 0.25));
            }

            fs1 = fs2;

            start_time = System.currentTimeMillis();
            System.out.print("processing SIFT ...");
            sift.init(fa, steps, initial_sigma, min_size, max_size);
            fs2 = sift.run(max_size);
            Collections.sort(fs2);
            System.out.println(" took " + (System.currentTimeMillis() - start_time) + "ms");

            System.out.println(fs2.size() + " features identified and processed");

            start_time = System.currentTimeMillis();
            System.out.print("identifying correspondences using brute force ...");
            Vector< PointMatch> candidates =
                    FloatArray2DSIFT.createMatches(fs2, fs1, 1.5f, null, Float.MAX_VALUE);
            System.out.println(" took " + (System.currentTimeMillis() - start_time) + "ms");

            System.out.println(candidates.size() + " potentially corresponding features identified");
        }
    }
