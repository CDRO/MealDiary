#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_ch_schmidlins_mealdiary_AnalysisEngine_getPatternResult(
        JNIEnv* env,
        jobject /* this */) {
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
