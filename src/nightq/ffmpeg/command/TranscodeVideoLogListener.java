package nightq.ffmpeg.command;

/**
* Created by Nightq on 14-7-2.
*/
public interface TranscodeVideoLogListener {
public void logTranscodeVideo(String log);

/**
* 转码结束
* @param log
*/
public void onCompleted(boolean log);

/**
* 转码进度
* @param progress
*/
public void onProgress(int progress);
}
