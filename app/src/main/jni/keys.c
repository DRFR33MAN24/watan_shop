#include <jni.h>
JNIEXPORT jstring JNICALL
Java_com_sabeal_watanshop_SplashScreen_placeInitialize(JNIEnv *env, jobject instance) {
return (*env)-> NewStringUTF(env, "AIzaSyD_tT3afDTRExMQ4TKL9qw8fgAZtxcUx_4");
}

JNIEXPORT jstring JNICALL
Java_com_sabeal_watanshop_messages_WhizzChat_Adapters_ChatAdapter_placeInitialize(JNIEnv *env,
                                                                                    jobject instance) {
    return (*env)-> NewStringUTF(env, "AIzaSyD_tT3afDTRExMQ4TKL9qw8fgAZtxcUx_4");
}
JNIEXPORT jstring JNICALL
Java_com_sabeal_watanshop_App_placeInitialize(JNIEnv *env, jobject instance) {
return (*env)-> NewStringUTF(env, "AIzaSyD_tT3afDTRExMQ4TKL9qw8fgAZtxcUx_4");
}

