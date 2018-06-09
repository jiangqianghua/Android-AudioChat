package com.jqh.audiochat.view;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by jiangqianghua on 18/6/9.
 */

public class AudioManager {

    private MediaRecorder mMediaRecorder ;
    private String mDir ;
    private String mCurrentFilePath ;

    private boolean isPrepared = false ;

    public interface AudioStateListener{
        void wellPrepared();
    }

    private AudioStateListener mListener ;

    public void setOnAudioStateListener(AudioStateListener listener){
        mListener = listener ;
    }

    private static AudioManager mInstance ;

    private AudioManager(String dir){
        this.mDir = dir ;
    };

    public static AudioManager getInstance(String dir){
        if(mInstance == null){
            synchronized (AudioManager.class){
                if(mInstance == null)
                    mInstance =new AudioManager(dir);
            }
        }
        return mInstance;
    }

    public void prepareAudio(){
        try {
            isPrepared = false ;
            File dir = new File(mDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = generateFileName();
            File file = new File(dir, fileName);
            mCurrentFilePath = file.getAbsolutePath();
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setOutputFile(file.getAbsolutePath());

            //set audio source
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // set audio format
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            // set audio encoder
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mMediaRecorder.prepare();

            mMediaRecorder.start();
            isPrepared = true ;
            if(mListener != null)
                mListener.wellPrepared();
        }catch (IllegalStateException | IOException e){
            e.printStackTrace();
        }



    }

    public int getVoiceLevel(int maxLevel){
        if(isPrepared){
            try {
                //mMediaRecorder.getMaxAmplitude()  范围1 到 32767
                return maxLevel * mMediaRecorder.getMaxAmplitude()/32768 + 1;
            }catch (Exception e){

            }

        }
        return 1;
    }

    public void release(){
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null ;
    }

    public void cancel(){
        release();
        if(mCurrentFilePath != null){
            File file = new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath = null ;
        }
    }

    private String generateFileName(){
        return UUID.randomUUID().toString()+".amr";
    }


    public String getCurrentFilePath(){
        return mCurrentFilePath ;
    }
}
