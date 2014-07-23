package nightq.ffmpeg.command;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
 

import helper.DateHelper;
import helper.StorageUtils;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 

public class FfmpegTranscodeVideoService {

    public static boolean isRunning = false;

    public static boolean isGetInfo = false;

	private native void transcodeVideoForTimehutJni(String[] array);

    public static String currentTranscodeVideoId = null;
    public static String error = null;

	public static TranscodeVideoLogListener transcodeVideoLogListener;
	private static Vector<TranscodeVideoNotifyListener> transcodeVideoNotifyListenerList = new Vector<TranscodeVideoNotifyListener>();

	
	public static Context context;
	
    /**
     * just for uploadervideocontroller，必须
     * @param transcodeVideoLogListener
     */
	public static void setTranscodeVideoLogListener(
			TranscodeVideoLogListener transcodeVideoLogListener) {
		FfmpegTranscodeVideoService.transcodeVideoLogListener = transcodeVideoLogListener;
	}

    /**
     * 其他activity等界面的进度通知，非必需
     * @param transcodeVideoNotifyListener
     */
    public static void addTranscodeVideoNotifyListener(
            TranscodeVideoNotifyListener transcodeVideoNotifyListener) {
        if (!transcodeVideoNotifyListenerList.contains(transcodeVideoNotifyListener)) {
            transcodeVideoNotifyListenerList.add(transcodeVideoNotifyListener);
        }
    }

    /**
     * 其他activity等界面的进度通知，非必需
     * @param transcodeVideoNotifyListener
     */
    public static void removeTranscodeVideoNotifyListener(
            TranscodeVideoNotifyListener transcodeVideoNotifyListener) {
        if (transcodeVideoNotifyListenerList.contains(transcodeVideoNotifyListener)) {
            transcodeVideoNotifyListenerList.remove(transcodeVideoNotifyListener);
        }
    }

    /**
     * 调用注册的进度监听进行
     * @param uploadId
     * @param progress
     */
    private static void notifyTranscodeProgress (String uploadId, int progress) {
        for (TranscodeVideoNotifyListener transcodeVideoNotifyListener : transcodeVideoNotifyListenerList) {
            transcodeVideoNotifyListener.onProgress(uploadId, progress);
        }
    }


    static {

        try {
            //顺序一定不能变，不然会崩溃。。。
            System.loadLibrary("avutil-52");
            System.loadLibrary("swscale-2");
            System.loadLibrary("swresample-0");
            System.loadLibrary("avcodec-55");
            System.loadLibrary("avformat-55");
            System.loadLibrary("avfilter-4");
            System.loadLibrary("avdevice-55");
            System.loadLibrary("postproc-52");
            System.loadLibrary("ffmpeg");
        	MainActivity.canFFmpeg = true;
//        	throw new UnsatisfiedLinkError("sdfsdf");
        } catch (UnsatisfiedLinkError e) {
        	MainActivity.canFFmpeg = false;
        	error = e.getMessage();
            Log.e("UnsatisfiedLinkError", "Unsatisfied Link error: " + e.toString() + Thread.currentThread().getName());
        }
	}

    public static int oldWidth = 0;
    public static int oldHeight = 0;

    public static int oldOrientation = 0;

    public static int bitrate = 0;

    public static String exeCmd;
    public static String localPath;

    public static StringBuffer cmdBuffer = new StringBuffer();
    public static boolean startDuration = false;

    public static String CMD_GET_INFO = "ffmpeg -i %s";

    public static String CMD_FRONT = "ffmpeg -y -i %s -strict experimental -c:a copy ";
    public static String DEST_TMP_VIDEO_BEH = ".tmp.mp4";
    public static String DEST_VIDEO_BEH = ".mp4";

    private static final int VIDEO_MAX_BITRATE = 2097;
    public static final int VIDEO_MAX_SIDE = 720;
    public static boolean setTranscodeCmd (String localPath) {
        FfmpegTranscodeVideoService.localPath = localPath;
        exeCmd = String.format(CMD_FRONT, localPath);
        if ((oldWidth > oldHeight && oldWidth > VIDEO_MAX_SIDE)) {
            exeCmd = exeCmd + "-vf scale=" + "720:" + getEvenLength(oldHeight, oldWidth) + " ";
        } else if ((oldWidth < oldHeight && oldHeight > VIDEO_MAX_SIDE)) {
            exeCmd = exeCmd + "-vf scale=" + getEvenLength(oldHeight, oldWidth) + ":720 ";
        }
        
        String output = getDestTmpVideo(localPath);
        if (TextUtils.isEmpty(output)) {
            exeCmd = null;
            return false;
        }
        //-b:v 2097k
        if (bitrate > VIDEO_MAX_BITRATE) {
            exeCmd += "-b:v " + VIDEO_MAX_BITRATE + "k ";
        }
        //有人说：桢速率（可以改，确认非标准桢率会导致音画不同步，所以只能设定为15或者29.97）
        //所以不改 + "-r 29.97 "
        exeCmd += getCMDOrientationOption() + output;
        Log.e("nightq", "exeCmd = " + exeCmd);

        return true;
    }

    /**
     * 返回转码后的tmp文件
     * @param localPath
     * @return
     */
    public static String getDestTmpVideo (String localPath) {
    	String folder = StorageUtils.getCacheDirectory(context).getAbsolutePath();
    	return folder + "/" + localPath.hashCode() + DEST_TMP_VIDEO_BEH;
//        return SC.getTmpUploadFilePathWithoutBeh(String
//                .format("%s",
//                        StringHelper.MD5(localPath))) + DEST_TMP_VIDEO_BEH;
    }

    /**
     * 返回转码成功后的目标文件
     * @param localPath
     * @return
     */
    public static String getDestVideo (String localPath) {
    	String folder = StorageUtils.getCacheDirectory(context).getAbsolutePath();
    	return folder + "/" + localPath.hashCode() + DEST_VIDEO_BEH;
//        return SC.getTmpUploadFilePathWithoutBeh(String
//                .format("%s",
//                        StringHelper.MD5(localPath))) + DEST_VIDEO_BEH;
    }

    /**
     * 返回是否成功转码
     * @param localPath
     * @return
     */
    public static boolean haveTranscoded (String localPath) {
        String destPath = getDestVideo(localPath);
        if (TextUtils.isEmpty(destPath)) {
            return false;
        } else {
            File file = new File(destPath);
            if (file.exists() && destPath.length() > 0) {
                return true;
            }
        }
        return false;
    }

    private static int getEvenLength (int min, int max) {
        float destFloat = VIDEO_MAX_SIDE * min / (float)max;
        int dest = (int)destFloat;
        if (dest % 2 == 0) {
            return dest;
        } else {
            return dest + 1;
        }
    }

    private static String getCMDOrientationOption () {
//        switch (oldOrientation % 360) {
//            case 0:
//                return "";
//            case 90:
//                return "transpose=1 ";
//            case 180:
//                return "hflip,vflip ";
//            case 270:
//                return "transpose=2 ";
//        }
        return "";
    }

	public static void logTranscode(String log) {
//		Log.e("Nightq", "static log:" + log);
	}


    public static long currentTranscodeDuration = 1;
    public static long currentTranscodeProgress = 0;

    Pattern patternForDuration = Pattern.compile("Duration:.*");
    Pattern patternForProgress = Pattern.compile("time=.* bitrate=");
    Pattern patternForWH = Pattern.compile("Video:.*");
    Pattern patternForWHDetail = Pattern.compile(", [0-9]+x[0-9]+[^0-9]");

    /**
     * 这个是在通过jni调用的c语言里面调用的方法进行log的。
     * @param log
     */
	public void log(String log) {
        Log.e("nightq", "log = " + log);
        
        if (transcodeVideoLogListener != null) {
            transcodeVideoLogListener.logTranscodeVideo(log);
        }
        
        if (isGetInfo) {
            //取信息
            if (startDuration) {
                cmdBuffer.append(log);
                if (log.indexOf("Stream") >= 0) {
                    String tmp = cmdBuffer.toString();
                    Log.e("nightq", "duration all = " + tmp);
                    String durationStr = tmp.substring(0, tmp.indexOf(","));
                    Log.e("nightq", "duration = " + durationStr);
                    String bitrateStr = tmp.substring(tmp.indexOf("bitrate: ") + 9, tmp.indexOf(" kb/s"));
                    Log.e("nightq", "bitrateStr = " + bitrateStr);
                    bitrate = Integer.valueOf(bitrateStr);
//                    currentTranscodeDuration = me.acen.foundation.helper.DateHelper.getMillSecondFormHMC(durationStr);
                    Log.e("nightq", "duration mill second = " + durationStr);
                    startDuration = false;
                }
            } else {
                Matcher matcher = patternForDuration.matcher(log);
                StringBuffer bufferForDuration = new StringBuffer();
                if(matcher.find()){
                    startDuration = true;
                    cmdBuffer = new StringBuffer();
                    Log.e("nightq", "match duration = " + bufferForDuration.toString());
                } else {
                    matcher = patternForWH.matcher(log);
                    if(matcher.find()){
                        matcher = patternForWHDetail.matcher(matcher.group());
                        if (matcher.find()) {
                            String dura = matcher.group();
                            oldWidth = Integer.valueOf(dura.substring(2, dura.indexOf("x")));
                            oldHeight = Integer.valueOf(dura.substring(dura.indexOf("x") + 1, dura.length() - 1));
                            Log.e("nightq", "oldWidth = " + oldWidth + "  oldHeight = " + oldHeight);
                        }
                    }
                }
            }
            if (isFfmpegFinished(log)) {
                synchronized (getInfoLock) {
                    //信息获取完成就通知结束
                    getInfoLock.notify();
                }
            }
        } else {
            //转码开始
            if (!TextUtils.isEmpty(log)) {
                Matcher matcher = patternForProgress.matcher(log);
                if(matcher.find()){
                    String tmp = matcher.group().split(" ")[0];
                    tmp = tmp.substring(5);
                    Log.e("nightq", "match progress = " + tmp);
                    currentTranscodeProgress = DateHelper.getMillSecondFormHMC(tmp);
                    int progress = (int)(currentTranscodeProgress * 10000/currentTranscodeDuration);
                    //通知控制流程
                    if (transcodeVideoLogListener != null) {
                        transcodeVideoLogListener.onProgress(progress);
                    }
                    //通知界面部分
                    notifyTranscodeProgress(currentTranscodeVideoId, progress);
                    Log.e("nightq", "transcode progress = " + (currentTranscodeProgress/currentTranscodeDuration));
                }
            }

            if (isFfmpegFinished(log)) {
                isRunning = false;

                if (transcodeVideoLogListener != null) {
                    transcodeVideoLogListener.onCompleted(isFfmpegFinishedSuccess(log));
                }
            }
        }

	}

    private static final String TRANSCODE_SUCCESS = "ANDROID_FOR_TIMEHUT_TRANSCODE_SUCCESS";
    private static final String TRANSCODE_FAILED = "ANDROID_FOR_TIMEHUT_TRANSCODE_FAIL";

    private static boolean isFfmpegFinishedSuccess (String log) {
        if (TRANSCODE_SUCCESS.equalsIgnoreCase(log)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isFfmpegFinishedFailed (String log) {
        if (TRANSCODE_FAILED.equalsIgnoreCase(log)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isFfmpegFinished (String log) {
        if (isFfmpegFinishedSuccess(log)) {
            return true;
        } else if (isFfmpegFinishedFailed(log)) {
            return true;
        }
        return false;
    }

    public static final Object getInfoLock = new Object();

	public static boolean isFinished () {
		return !isRunning;
	}


    /**
     * 返回当前uploadid是否正在转码。
     * @param uploadId
     * @return
     */
    public static boolean isTranscodingVideo (String uploadId) {
        return !TextUtils.isEmpty(uploadId) && uploadId.equalsIgnoreCase(currentTranscodeVideoId) && isRunning;
    }

    public static final int RESULT_START_TRANSCODE_SUCCESS = 1;
    public static final int RESULT_START_TRANSCODE_FAILED = 2;
    public static final int RESULT_NOT_TRANSCODE = 3;

    private static long startTranscodeTime = 0;

    /**
     * 返回值说明是否成功的开始获取信息可以不转码 或者成功的开始转码
     * @param uploadInterfaceId
     * @param localPath
     * @return
     */
	public synchronized int transcodeVideoForTimehutLocal(String uploadInterfaceId, String localPath) {
		if (isRunning || !MainActivity.canFFmpeg) {
			return RESULT_START_TRANSCODE_FAILED;
		}
		isRunning = true;
        //初始化当前的转码video id
        currentTranscodeVideoId = uploadInterfaceId;
        String[] array;
        //first: get info
        String getInfoCmd = String.format(CMD_GET_INFO, localPath);
        array = getInfoCmd.split(" ");

        synchronized (getInfoLock) {
            isGetInfo = true;
            transcodeVideoForTimehutJni(array);
            try {
                //等待取信息完成
                getInfoLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isGetInfo = false;
        }
        //从log里面获取了bitrate和duration
        //如果尺寸或者bitrate太小就不转码了。直接返回。
        if ((oldWidth < VIDEO_MAX_SIDE && oldHeight < VIDEO_MAX_SIDE)  || (bitrate > 0 && bitrate < VIDEO_MAX_BITRATE)) {
            isRunning = false;
            return RESULT_NOT_TRANSCODE;
        } else {
            boolean isSuccess = FfmpegTranscodeVideoService.setTranscodeCmd(
                    localPath);
            //成功的开始或者失败转码
            array = exeCmd.split(" ");
            if (isSuccess) {
                startTranscodeTime = System.currentTimeMillis();
                transcodeVideoForTimehutJni(array);
                return RESULT_START_TRANSCODE_SUCCESS;
            } else {
                return RESULT_START_TRANSCODE_FAILED;
            }
        }
	}
}
