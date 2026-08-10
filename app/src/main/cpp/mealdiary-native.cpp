#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "MealDiaryNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_ch_schmidlins_mealdiary_AnalysisEngine_getPatternResult(
        JNIEnv* env,
        jobject /* this */) {
    LOGI("getPatternResult called from JNI");
    std::string result = "No patterns detected yet.";
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ch_schmidlins_mealdiary_AnalysisEngine_isAccelerator(
        JNIEnv* env,
        jobject /* this */,
        jstring description) {
    const char *nativeString = env->GetStringUTFChars(description, 0);
    std::string desc(nativeString);
    env->ReleaseStringUTFChars(description, nativeString);

    // Simple heuristic: coffee is an accelerator
    if (desc.find("coffee") != std::string::npos || desc.find("Coffee") != std::string::npos) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}
