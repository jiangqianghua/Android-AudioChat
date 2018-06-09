package com.jqh.audiochat;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jqh.audiochat.view.AudioRecorderButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mListView ;
    private ArrayAdapter<Recorder> mAdapter ;
    private List<Recorder> mDatas = new ArrayList<>();

    private AudioRecorderButton mAudioRecorderButton ;
    View animView = null ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView)findViewById(R.id.id_listview);
        mAudioRecorderButton = (AudioRecorderButton)findViewById(R.id.id_recorder_button);
        mAudioRecorderButton.setOnAudioFinishRecorderListener(new AudioRecorderButton.OnAudioFinishRecorderListener() {
            @Override
            public void onFinish(float seconds, String filePath) {
                Recorder recorder = new Recorder(seconds,filePath);
                mDatas.add(recorder);
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(mDatas.size()-1);
            }
        });

        mAdapter = new RecorderAdapter(this,mDatas);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 播放动画
                if(animView != null){
                    animView.setBackgroundResource(R.mipmap.adj);
                    animView = null ;
                }
                animView = view.findViewById(R.id.id_recorder_amin);
                animView.setBackgroundResource(R.drawable.play_anim);
                AnimationDrawable anim = (AnimationDrawable)animView.getBackground();
                anim.start();
                // 播放音频
                MediaManager.playSound(mDatas.get(position).filePath,new MediaPlayer.OnCompletionListener(){
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        animView.setBackgroundResource(R.mipmap.adj);
                    }
                });
            }
        });
    }

    class Recorder{
        float time ;
        String filePath ;

        public Recorder(float time, String filePath) {
            this.time = time;
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public float getTime() {
            return time;
        }

        public void setTime(float time) {
            this.time = time;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaManager.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaManager.release();
    }
}
