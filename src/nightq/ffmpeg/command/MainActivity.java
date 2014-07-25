package nightq.ffmpeg.command;

import helper.StorageUtils;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ffmpegcommand.R;


public class MainActivity extends Activity {
 
	EditText editText;
	TextView textView;
	String content;
	Dialog progressDlg;
	TextView dlgTextView;

	long start;
	long duration = 0;;
	int height = 0;
	
	public static boolean canFFmpeg = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button button = (Button) findViewById(R.id.button);
		editText = (EditText) findViewById(R.id.editText);
		textView = (TextView) findViewById(R.id.textView);
		HandlerThread backThread = new HandlerThread("trans");
		backThread.start();
		final Handler backHandler = new Handler(backThread.getLooper());
		getCacheDir();
		getExternalCacheDir();
		
		// 屏幕相关
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		height = metrics.heightPixels;
		
		progressDlg = new Dialog(MainActivity.this);
		dlgTextView = new TextView(getBaseContext());
		progressDlg.setContentView(dlgTextView);
		
		FfmpegTranscodeVideoService.context = MainActivity.this;
		FfmpegTranscodeVideoService.setTranscodeVideoLogListener(new TranscodeVideoLogListener() {
			
			@Override
			public void logTranscodeVideo(final String log) {
				// TODO Auto-generated method stub
				content = log;
				Message msg = handler.obtainMessage();
				msg.obj = log;
				handler.sendMessage(msg);
			}

			@Override
			public void onCompleted(boolean log) {
				// TODO Auto-generated method stub
				duration = System.currentTimeMillis() - start;
				Log.e("NIGHTQ", "onCompleted log = " + log);
				Message msg = handler.obtainMessage();
				msg.what = 1;
				msg.obj = "onCompleted log = " + log + duration;
				handler.sendMessage(msg);
			}

			@Override
			public void onProgress(int progress) {
				// TODO Auto-generated method stub
				Log.e("NIGHTQ", "progress = " + progress);
				Message msg = handler.obtainMessage();
				msg.obj = "progress = " + progress;
				handler.sendMessage(msg);
			}
		});
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			}
		});
		
		{

			backHandler.post(new Runnable() {

				@Override
				public void run() {
					if (!canFFmpeg) {
						progressDlg.setTitle("can not ffmpeg");
						dlgTextView.setText(FfmpegTranscodeVideoService.error);
						progressDlg.show();
						return;
					}
					
					String destPath = new File(StorageUtils.getCacheDirectory(MainActivity.this), "test.mp4").getAbsolutePath();
					if (StorageUtils.assetsCopyData(MainActivity.this, "test.mp4", destPath)) {
						String tmpCmd = "ffmpeg -y -i " + destPath + " -strict experimental -c:a copy -vf scale=640:360 -r 30 -b:v 2097k " + Environment.getExternalStorageDirectory() + "/out.mp4";
						String[] array = //editText.getText().toString()
								tmpCmd.split(" ");
						if (FfmpegTranscodeVideoService.isFinished()) {
							Log.e("onclick", ""
									+ Thread.currentThread().getName()
									+ Thread.currentThread().getId());
							start = System.currentTimeMillis();
							if (new FfmpegTranscodeVideoService()
									.transcodeVideoForTimehutLocal("nothing", destPath) == FfmpegTranscodeVideoService.RESULT_START_TRANSCODE_SUCCESS) {
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										Toast.makeText(MainActivity.this,
												" 成功开始转码", Toast.LENGTH_SHORT)
												.show();
										textView.setText("");
									}
								});
							} else {
								progressDlg.setTitle("can not start ffmpeg");
								progressDlg.show();
								return;
							}
						} else {
							Toast.makeText(MainActivity.this, " 失败开始",
									Toast.LENGTH_SHORT).show();
						}
					}
					
				}
			});
		}
		
//		getTeleState();

	}

 

	final Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			try {
				progressDlg.hide();
//				progressDlg.setTitle("start ffmpeg");
//				progressDlg.show();
				if (msg.what != 0) {
					progressDlg.setTitle("ffmpeg duration = " + (duration/1000f) + "s");
					dlgTextView.setText("ffmpeg duration = " + (duration/1000f) + "s");
					progressDlg.show();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

			if (textView.getHeight() > height*2/3) {
				textView.setText((String)msg.obj);
			} else {
				textView.append((String)msg.obj);
			}
            showToast(MainActivity.this, "transcode progress=" + (String)msg.obj);
		};
	};
	
	private static Toast toast = null;

	public static void showToast(Context context, CharSequence text) {
		try {
			if (toast == null) {
				toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
			} else {
				toast.setText(text);
			}
			toast.show();
		} catch (Exception e) {
            try {
//                LogForServer.e("@ishowToast", "\n first Thread current = " + Thread.currentThread().getName());
                toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                toast.show();
            } catch (Exception ee) {
//                LogForServer.e("@ishowToast", "\n second Thread current = " + Thread.currentThread().getName());
            }
		}
	}
 
}
