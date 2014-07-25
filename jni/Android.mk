LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ffmpeg
LOCAL_SRC_FILES := ffmpeg.c cmdutils.c ffmpeg_opt.c ffmpeg_filter.c  
LOCAL_LDLIBS := -llog -ljnigraphics -lz 
LOCAL_C_INCLUDES := config.h ffmpeg.h cmdutils.h cmdutils_common_opts.h libavutil/*.h libavformat/*.h compat/*.h libavresample/*.h libpostproc/*.h libavcodec/*.h libavdevice/*.h libavfilter/*.h libavswscale/*.h
LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil libswresample libavfilter libavdevice libpostproc
include $(BUILD_SHARED_LIBRARY)
$(call import-module,FFmpeg/android/arm)
