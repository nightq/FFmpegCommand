package nightq.ffmpeg.command;

import android.util.Log;

public class FfmpegTranscodeVideoService {

	public static boolean isRunning = false;
	private native void transcodeVideoForTimehutJni(String[] array);

	public static TranscodeVideoLogListener transcodeVideoLogListener;
	
	public static void setTranscodeVideoLogListener(
			TranscodeVideoLogListener transcodeVideoLogListener) {
		FfmpegTranscodeVideoService.transcodeVideoLogListener = transcodeVideoLogListener;
	}
	
	public interface TranscodeVideoLogListener {
		public void logTranscodeVideo (String log);
	}	
	// private native void setJNIEnv();

	static {
		System.loadLibrary("avutil-52");
		System.loadLibrary("swresample-0");
		System.loadLibrary("avcodec-55");
		System.loadLibrary("avformat-55");
		System.loadLibrary("swscale-2");
		System.loadLibrary("avfilter-4");
		System.loadLibrary("ffmpeg");
	}

	public static void logTranscode(String log) {
		Log.e("Nightq", "static log:" + log);
	}

	public void log(String log) {
		if ("ANDROID_FOR_TIMEHUT_TRANSCODE_SUCCESS".equalsIgnoreCase(log)) {
			isRunning = false;	
			Log.e("Nightq", "transcode success");
		} else if ("ANDROID_FOR_TIMEHUT_TRANSCODE_FAIL".equalsIgnoreCase(log)) {
			isRunning = false;	
			Log.e("Nightq", "transcode fail");
		}
		if (transcodeVideoLogListener != null) {
			Log.e("Nightq", "log:" + log); 
			transcodeVideoLogListener.logTranscodeVideo(log);
		}
	}

	/**
	 * 转码结束，返回结果，（由c调用）
	 */
	public void finishTranscode(int result) {
		 //
		
		
	}
	
	public static boolean isFinished () {
		return !isRunning;
	}
	
	public synchronized boolean transcodeVideoForTimehutLocal(String[] array) {
		if (isRunning) {
			return false;
		}
		isRunning = true;
		transcodeVideoForTimehutJni(array);
		return true;
	}
}
