/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.videorecognition;

import com.xuggle.xuggler.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mike
 */
public class VideoSeparator extends Thread{
    private final Long startTimestamp;
    private final Long endTimestamp;
    private final String pathToMovie;
    private final String pathToFrames;
    private final FileManager fm;
    
    public VideoSeparator(){
        throw new RuntimeException("You cannot invoke VideoSeparator constructor without params!");
    }
    
    public VideoSeparator(Long startTimestamp, Long endTimestamp, final String pathToMovie, final String pathToFrames, FileManager fm){
        if(null == startTimestamp || null == endTimestamp || null == pathToMovie || null == pathToFrames)
            throw new RuntimeException("startTimestamp, endTimestamp or pathToMovie must not be null!");
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.pathToMovie = pathToMovie;
        this.pathToFrames = pathToFrames;
        this.fm = fm;
    }

    @Override
    public void run() {
        if(!pathToFrames.trim().isEmpty()){
            File f = new File(this.pathToFrames);
            if(f.exists())
                f.delete();
            f.mkdir();
        }
        System.out.println("Thread name:" + this.getName());
        IContainer container = IContainer.make();
        if (container.open(pathToMovie, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("could not open file: "
                    + pathToMovie);
        }
        int numStreams = container.getNumStreams();
        int videoStreamId = -1;
        IStreamCoder videoCoder = null;

        // find video stream
        for (int i = 0; i < numStreams; i++) {
            IStream stream = container.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();
            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                videoStreamId = i;
                videoCoder = coder;
                break;
            }
        }
        if (videoStreamId == -1) // video stream for the file does not exist
        {
            throw new RuntimeException("could not find video stream in container: "
                    + pathToMovie);
        }

        // open video codec
        if (videoCoder.open() < 0) {
            throw new RuntimeException(
                    "could not open video decoder for container: " + pathToMovie);
        }

        IPacket packet = IPacket.make();
        END:
        while (container.readNextPacket(packet) >= 0) {

            if (packet.getStreamIndex() == videoStreamId) {
                IVideoPicture picture = IVideoPicture.make(
                        videoCoder.getPixelType(), videoCoder.getWidth(),
                        videoCoder.getHeight());
                int offset = 0;
                while (offset < packet.getSize()) {
                    int bytesDecoded = videoCoder.decodeVideo(picture, packet,
                            offset);
                    //System.out.println(videoCoder.getFrameRate().getValue());
                    // Если что-то пошло не так
                    if (bytesDecoded < 0) {
                        throw new RuntimeException("got error decoding video in: "
                                + pathToMovie);
                    }
                    offset += bytesDecoded;
                    if (picture.isComplete()) {
                        IVideoPicture newPic = picture;
                        // microseconds
                        long timestamp = picture.getTimeStamp();
                        if (timestamp > this.startTimestamp) {
                            BufferedImage javaImage = Utils
                                    .videoPictureToImage(newPic);
                            String fileName = String.format("%07d.png",
                                    timestamp);
                            try {
                                synchronized (fm) {
                                    ImageIO.write(javaImage, "PNG", new File(pathToFrames,
                                            fileName));
                                }
                                System.out.println("Thread: " + this.getName());
                            } catch (IOException ex) {
                                Logger.getLogger(VideoSeparator.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        if (timestamp > this.endTimestamp) {
                            break END;
                        }
                    }
                }
            }
        }
        if (videoCoder != null) {
            videoCoder.close();
        }
        if (container != null) {
            container.close();
        }

    }
  }
   

