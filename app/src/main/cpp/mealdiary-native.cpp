#include <jni.h>
#include <string>
#include <vector>
#include <map>
#include <algorithm>
#include <android/log.h>

#define LOG_TAG "MealDiaryNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_ch_schmidlins_mealdiary_AnalysisEngine_getPatternResultNative(
        JNIEnv* env,
        jobject /* this */) {
    LOGI("getPatternResult called from JNI");
    std::string result = "Analyze your entries to see patterns.";
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_ch_schmidlins_mealdiary_AnalysisEngine_analyzeCorrelations(
        JNIEnv* env,
        jobject /* this */,
        jobjectArray mealDescriptions,
        jlongArray mealTimestamps,
        jlongArray bmTimestamps) {

    int mealCount = env->GetArrayLength(mealDescriptions);
    int bmCount = env->GetArrayLength(bmTimestamps);

    jlong* mTs = env->GetLongArrayElements(mealTimestamps, nullptr);
    jlong* bTs = env->GetLongArrayElements(bmTimestamps, nullptr);

    std::map<std::string, int> acceleratorScores;
    std::map<std::string, int> totalCounts;
    const long long fourHours = 4 * 60 * 60 * 1000;

    for (int i = 0; i < mealCount; ++i) {
        auto descObj = static_cast<jstring>(env->GetObjectArrayElement(mealDescriptions, i));
        const char* descChars = env->GetStringUTFChars(descObj, nullptr);
        std::string desc(descChars);
        env->ReleaseStringUTFChars(descObj, descChars);
        env->DeleteLocalRef(descObj);

        totalCounts[desc]++;
        long long mTime = mTs[i];

        for (int j = 0; j < bmCount; ++j) {
            long long bTime = bTs[j];
            if (bTime > mTime && (bTime - mTime) < fourHours) {
                acceleratorScores[desc]++;
                break;
            }
        }
    }

    std::vector<std::string> results;
    for (auto const& [food, total] : totalCounts) {
        int accCount = acceleratorScores[food];
        if (total >= 3 && accCount >= 2) {
            results.push_back(food + " might be an accelerator");
        } else if (total >= 5 && accCount == 0) {
            results.push_back(food + " might be a decelerator");
        }
    }

    env->ReleaseLongArrayElements(mealTimestamps, mTs, JNI_ABORT);
    env->ReleaseLongArrayElements(bmTimestamps, bTs, JNI_ABORT);

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray ret = env->NewObjectArray(static_cast<jsize>(results.size()), stringClass, nullptr);
    for (size_t i = 0; i < results.size(); ++i) {
        env->SetObjectArrayElement(ret, static_cast<jsize>(i), env->NewStringUTF(results[i].c_str()));
    }
    return ret;
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
