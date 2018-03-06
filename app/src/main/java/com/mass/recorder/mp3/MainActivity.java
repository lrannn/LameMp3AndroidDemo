package com.mass.recorder.mp3;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.mass.recorder.mp3.Mp3Encoder.native_encoder_process;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int SAMPLE_COUNT = 512;
    private static final int SAMPLE_RATE = 44100;
    private static final String TEST_FILE_PATH = Environment.getExternalStorageDirectory() + "/test.mp3";
    private static final String TEST_PCM_PATH = Environment.getExternalStorageDirectory() + "/output.pcm";

    private static final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private int mp3BufferSize = (int) (1.25 * SAMPLE_COUNT + 7200);
    private byte[] mp3Buffer = new byte[mp3BufferSize];
    private FileOutputStream mStream;
    private boolean isRecording;
    private AudioRecord mAudioRecord;
    private MediaDecoder mediaDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(PERMISSIONS, 1);
        }

//        // Example of a call to a native method
//        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mAudioRecord == null) {
//                    setupAudioRecord();
//                    createOutputFile();
//                }
//                int state = native_encoder_init(SAMPLE_RATE, SAMPLE_RATE, 2, 196, 2);
//                Toast.makeText(MainActivity.this, "" + state, Toast.LENGTH_SHORT).show();
//                mAudioRecord.startRecording();
//                new Thread(mRecordRunnable).start();
//                isRecording = true;
//            }
//        });
//
//        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
//                    mAudioRecord.stop();
//                    isRecording = false;
//                    native_encoder_flush(mp3Buffer);
//                    try {
//                        mStream.write(mp3Buffer);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Mp3Encoder.native_encoder_close();
//                }
//            }
//        });


        mediaDecoder = new MediaDecoder();
        mediaDecoder.setDataSrcPath(TEST_FILE_PATH);
        mediaDecoder.setDataDestPath(TEST_PCM_PATH);
        mediaDecoder.start();

    }

    private void setupAudioRecord() {
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Toast.makeText(this, "Audio record is uninitialized.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean createOutputFile() {
        try {
            mStream = new FileOutputStream(new File(TEST_FILE_PATH));
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Runnable mRecordRunnable = new Runnable() {
        @Override
        public void run() {
            short[] buffer = new short[SAMPLE_COUNT];
            while (isRecording) {
                mAudioRecord.read(buffer, 0, SAMPLE_COUNT);
                int result = native_encoder_process(buffer, SAMPLE_COUNT, mp3Buffer, mp3BufferSize);
                try {
                    mStream.write(mp3Buffer, 0, result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
