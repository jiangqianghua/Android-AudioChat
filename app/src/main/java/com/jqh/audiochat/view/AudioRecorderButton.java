package com.jqh.audiochat.view;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.jqh.audiochat.R;
import com.jqh.audiochat.dialog.DialogManager;

/**
 * Created by jiangqianghua on 18/6/9.
 */

public class AudioRecorderButton extends Button implements AudioManager.AudioStateListener{

    private static final int DISTANCE_Y_CANCEL = 50 ;
    private static final int STATE_NORMAL = 1 ;
    private static final int STATE_RECORDING = 2 ;
    private static final int STATE_WANT_TO_CANCEL = 3 ;
    private int mCurState = STATE_NORMAL;
    private boolean isRecording = false ;

    private DialogManager mDialogManager;

    private AudioManager mAudioManager ;

    private float mTime ;

    //是否触发longclick
    private boolean mReady;


    public AudioRecorderButton(Context context) {
        this(context,null);
    }


    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDialogManager = new DialogManager(context);
        String dir = Environment.getExternalStorageDirectory()+"/audiorecord";
        mAudioManager = AudioManager.getInstance(dir);
        mAudioManager.setOnAudioStateListener(this);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 真正显示自audio perpare后
                mReady = true;
                mAudioManager.prepareAudio();
                return false;
            }
        });
    }

    public static final int MSG_AUDIO_PREPARED = 0X110;
    public static final int MSG_VOICE_CHANGE = 0X111;
    public static final int MSG_DIALOG_DIMISS = 0X112;

    // 录音完成后的回调
    public interface OnAudioFinishRecorderListener{
        void onFinish(float seconds , String filePath);
    }

    private OnAudioFinishRecorderListener mOnAudioFinishRecorderListener;

    public void setOnAudioFinishRecorderListener(OnAudioFinishRecorderListener onAudioFinishRecorderListener) {
        this.mOnAudioFinishRecorderListener = onAudioFinishRecorderListener;
    }

    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording){
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_AUDIO_PREPARED:
                    mDialogManager.showRecordingDialog();
                    isRecording = true ;
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case MSG_DIALOG_DIMISS:
                    mDialogManager.dimissDialog();
                    break;
                case MSG_VOICE_CHANGE:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;
            }
        }
    };
    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
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
                if(!mReady){  // 很没启动状态
                    reset();
                    return super.onTouchEvent(event);
                }
                if(!isRecording){  // 点击录音按钮，但录音很美准备完
                    mDialogManager.tooShot();
                    mAudioManager.cancel();
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS,1300);
                }else if( mTime < 0.6f){  // 开始录音拉，但录音时间很短
                    mDialogManager.tooShot();
                    mAudioManager.cancel();
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS,1300);
                }else if(mCurState == STATE_RECORDING){
                    // release
                    mDialogManager.dimissDialog();
                    // 正常结束
                    if(mOnAudioFinishRecorderListener != null){
                        mOnAudioFinishRecorderListener.onFinish(mTime,mAudioManager.getCurrentFilePath());
                    }
                    mAudioManager.release();
                }else if(mCurState == STATE_WANT_TO_CANCEL){
                    // cancel
                    mDialogManager.dimissDialog();
                    mAudioManager.cancel();
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
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.btn_recording);
                    setText(R.string.str_recorder_wantcancel);
                    //TODO Dialog.wantCacel
                    mDialogManager.wangToCancel();
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
        mReady = false ;
        changeState(STATE_NORMAL);
        mTime = 0f;
    }
}
