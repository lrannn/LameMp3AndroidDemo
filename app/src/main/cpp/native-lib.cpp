#include <jni.h>
#include <string>
#include "lame.h"

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_mass_recorder_mp3_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
