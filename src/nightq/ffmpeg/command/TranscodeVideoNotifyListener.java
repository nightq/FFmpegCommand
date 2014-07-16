package nightq.ffmpeg.command;

/**
* Created by Nightq on 14-7-2.
*/
public interface TranscodeVideoNotifyListener {

/**
* 转码进度
* @param progress
*/
public void onProgress(String upoadId, int progress);
}
