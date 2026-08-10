#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_ch_schmidlins_mealdiary_AnalysisEngine_getPatternResult(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Pattern Analysis Result from C++";
    return env->NewStringUTF(hello.c_str());
}
