//
// Created by lrannn on 2018/2/26.
//
#include <lame.h>
#include "jni.h"
#include "malloc.h"
#include "lame.h"
#include "android/log.h"

#define  INIT_FAILED -1
#define  INIT_SUCCESSFUL 0


#define func(RETURN_TYPE, NAME, ...) \
  extern "C" { \
   JNIEXPORT RETURN_TYPE \
    Java_com_mass_recorder_mp3_Mp3Encoder_ ## NAME \
       (JNIEnv* env, jobject thiz, ##__VA_ARGS__);\
   } \
   JNIEXPORT RETURN_TYPE \
     Java_com_mass_recorder_mp3_Mp3Encoder_ ## NAME \
      (JNIEnv* env, jobject thiz, ##__VA_ARGS__)\

static lame_global_flags *pGlobalStruct;


//mp3buffer_size (in bytes) = 1.25*num_samples + 7200.
func(int, native_1encoder_1process, jshortArray inBuffer, jint numOfSamples,
     jbyteArray outBuffer, jint size) {

    jshort *pInterleavedInBuffer = env->GetShortArrayElements(inBuffer, NULL);
    jbyte *pByteArray = env->GetByteArrayElements(outBuffer, NULL);

    int result = lame_encode_buffer_interleaved(pGlobalStruct, pInterleavedInBuffer, numOfSamples,
                                                (unsigned char *) pByteArray, size);

    env->ReleaseShortArrayElements(inBuffer, pInterleavedInBuffer, 0);
    env->ReleaseByteArrayElements(outBuffer, pByteArray, 0);
    return result;
}

//mp3buffer_size (in bytes) = 1.25*num_samples + 7200.
func(void, native_1encoder_1flush, jbyteArray inBuffer) {
    jbyte *pInterleavedInBuffer = env->GetByteArrayElements(inBuffer, NULL);
    jsize size = env->GetArrayLength(inBuffer);
    int result = lame_encode_flush(pGlobalStruct, (unsigned char *) pInterleavedInBuffer, size);
    env->ReleaseByteArrayElements(inBuffer, pInterleavedInBuffer, 0);
}


func(int, native_1encoder_1init, jint inSampleRate, jint outSampleRate, jint outChannel,
     jint outBitRate, jint quality) {

    pGlobalStruct = lame_init();
    if (!pGlobalStruct) {
        return INIT_FAILED;
    }

    lame_set_in_samplerate(pGlobalStruct, inSampleRate);
    lame_set_out_samplerate(pGlobalStruct, outSampleRate);
    lame_set_num_channels(pGlobalStruct, outChannel);
    lame_set_brate(pGlobalStruct, outBitRate);
    lame_set_quality(pGlobalStruct, quality);
    int result = lame_init_params(pGlobalStruct);
    if (result < 0) {
        // Init failed
        return INIT_FAILED;
    }
    return INIT_SUCCESSFUL;

}

func(void, native_1encoder_1close) {
    if (pGlobalStruct) {
        lame_close(pGlobalStruct);
    }
}





