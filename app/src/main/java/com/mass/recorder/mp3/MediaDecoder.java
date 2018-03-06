package com.mass.recorder.mp3;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lrannn on 2018/3/2.
 *
 * @e-mail lran7master@gmail.com
 */

public class MediaDecoder {

    private static final String TAG = "MediaDecoder";
    private static final int TIMEOUT_US = 1000;

    private MediaExtractor mMediaExtractor;

    private String mSourcePath;
    private String mRawFile;

    private MediaInfo mMediaInfo;
    private MediaFormat mTrackFormat;
    private FileOutputStream outputStream;

    private OnReadRawDataListener mListener;

    public MediaDecoder() {
    }

    public void setDataSrcPath(String src) {
        mSourcePath = src;
    }

    public void setDataDestPath(String destPath) {
        this.mRawFile = destPath;
        initialRawFile();
    }

    public void setOnReadRawDataListener(OnReadRawDataListener listener) {
        this.mListener = listener;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void start() {
        mMediaExtractor = new MediaExtractor();
        try {
            mMediaExtractor.setDataSource(mSourcePath);
            mMediaInfo = parseMediaFormat(mMediaExtractor);

            new Thread(mDecodingRunnable).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialRawFile() {
        File mOutFile = new File(mRawFile);
        try {
            outputStream = new FileOutputStream(mOutFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private MediaInfo parseMediaFormat(MediaExtractor extractor) {
        mTrackFormat = extractor.getTrackFormat(0);

        MediaInfo info = new MediaInfo();

        if (mTrackFormat.containsKey(MediaFormat.KEY_MIME)) {
            info.mime = mTrackFormat.getString(MediaFormat.KEY_MIME);
        }
        if (mTrackFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
            info.sampleRate = mTrackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        }
        if (mTrackFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
            info.channels = mTrackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        }
        if (mTrackFormat.containsKey(MediaFormat.KEY_DURATION)) {
            info.duration = mTrackFormat.getLong(MediaFormat.KEY_DURATION);
        }
        if (mTrackFormat.containsKey(MediaFormat.KEY_BIT_RATE)) {
            info.bitRate = mTrackFormat.getInteger(MediaFormat.KEY_BIT_RATE);
        }
        return info;
    }

    private Runnable mDecodingRunnable = new Runnable() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {
            try {
                mMediaExtractor.selectTrack(0);
                MediaCodec codec = MediaCodec.createDecoderByType(mMediaInfo.mime);
                codec.configure(mTrackFormat, null, null, 0);
                codec.start();

                ByteBuffer[] inBuffers = codec.getInputBuffers();
                ByteBuffer[] outBuffers = codec.getOutputBuffers();

                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                boolean eos = false;
                for (; ; ) {
                    if (!eos) {
                        int inBufferIndex = codec.dequeueInputBuffer(TIMEOUT_US);
                        if (inBufferIndex >= 0) {
                            ByteBuffer buffer = inBuffers[inBufferIndex];
                            buffer.clear();
                            int readSampleData = mMediaExtractor.readSampleData(buffer, 0);
                            if (readSampleData < 0) {
                                eos = true;
                                codec.queueInputBuffer(inBufferIndex, 0, 0, 0,
                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            } else {
                                codec.queueInputBuffer(inBufferIndex, 0, readSampleData,
                                        mMediaExtractor.getSampleTime(), 0);
                                mMediaExtractor.advance();
                            }
                        }
                    }

                    int res = codec.dequeueOutputBuffer(info, TIMEOUT_US);
                    if (res >= 0) {
                        ByteBuffer outBuffer = outBuffers[res];
                        byte[] chunk = new byte[info.size];
                        outBuffer.get(chunk);
                        outBuffer.clear();
                        if (mListener != null) {
                            mListener.onRawData(chunk);
                        }
                        if (outputStream != null) {
                            writePCMData(chunk);
                        }
                        codec.releaseOutputBuffer(res, false);
                    } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat outputFormat = codec.getOutputFormat();
                        Log.d(TAG, "run: OutputFormat has change to " + outputFormat);
                    } else if (res == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        Log.d(TAG, "run: Info try again later");
                    }

                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break;
                    }
                }

                codec.stop();
                codec.release();

                mMediaExtractor.release();
                mMediaExtractor = null;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void writePCMData(byte data[]) {
        try {
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static class MediaInfo {
        String mime;
        int sampleRate, channels, bitRate;
        long duration;
    }

    interface OnReadRawDataListener {
        void onRawData(byte[] data);
    }
}
