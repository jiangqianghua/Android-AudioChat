package com.jqh.audiochat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.jqh.audiochat.R;

/**
 * Created by jiangqianghua on 18/6/9.
 */

public class AudioRecorderButton extends Button{

    private static final int DISTANCE_Y_CANCEL = 50 ;
    private static final int STATE_NORMAL = 1 ;
    private static final int STATE_RECORDING = 2 ;
    private static final int STATE_WANT_TO_CANCEL = 3 ;
    private int mCurState = STATE_NORMAL;
    private boolean isRecording = false ;

    public AudioRecorderButton(Context context) {
        this(context,null);
    }


    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                isRecording = true ;
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if(isRecording){
                    if(wantToCancel(x,y)){
                        changeState(STATE_WANT_TO_CANCEL);
                    }else{
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mCurState == STATE_RECORDING){
                    // release
                }else if(mCurState == STATE_WANT_TO_CANCEL){
                    // cancel
                }
                reset();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void changeState(int state){
        if(mCurState != state){
            mCurState = state;
            switch (state){
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.btn_recorder_normal);
                    setText(R.string.str_recorder_normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.btn_recording);
                    setText(R.string.str_recorder_recording);
                    if(isRecording){
                        //TODO Dialog.recording
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.btn_recording);
                    setText(R.string.str_recorder_wantcancel);
                    //TODO Dialog.wantCacel
                    break;
            }
        }
    }

    private boolean wantToCancel(int x, int y){
        if(x < 0 || x > getWidth()){
            return true ;
        }
        if( y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL){
            return true ;
        }
        return false ;
    }

    private void reset(){
        isRecording = false ;
        changeState(STATE_NORMAL);
    }
}
