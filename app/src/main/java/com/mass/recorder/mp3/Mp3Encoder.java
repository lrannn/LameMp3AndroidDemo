package com.mass.recorder.mp3;

/**
 * Created by lrannn on 2018/2/26.
 *
 * @email lran7master@gmail.com
 */

public class Mp3Encoder {

    static {
        System.loadLibrary("native-lib");
    }

    public static native int native_encoder_init(int inSampleRate, int outSampleRate, int outChannel,
                                                  int outBitRate, int quality);

    public static native int native_encoder_process(short[] inBuffer, int numOfSamples,
                                                     byte[] outBuffer, int size);

    public static native void native_encoder_flush(byte[] mp3Buffer);

    public static native void native_encoder_close();
}
