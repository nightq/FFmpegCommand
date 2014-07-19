package nightq.ffmpeg.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.telephony.TelephonyManager;
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
				Log.e("NIGHTQ", "onCompleted log = " + log);
				Message msg = handler.obtainMessage();
				msg.obj = "onCompleted log = " + log;
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
						new Dialog(MainActivity.this).show();
						return;
					}
					String destPath = Environment.getExternalStorageDirectory() + "/test.mp4";
					String tmpCmd = "ffmpeg -i "
							+ destPath
							+ " -strict experimental";
					String[] array = // editText.getText().toString()
					tmpCmd.split(" ");
					if (FfmpegTranscodeVideoService.isFinished()) {
						Log.e("onclick", "" + Thread.currentThread().getName()
								+ Thread.currentThread().getId());
						if (new FfmpegTranscodeVideoService()
								.transcodeVideoForTimehutLocal("nothing",
										destPath) == FfmpegTranscodeVideoService.RESULT_START_TRANSCODE_SUCCESS) {
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
							// textView.setText("");
						} else {

							new Dialog(MainActivity.this).show();
							return;
							// textView.setText("");
						}
					} else {
						Toast.makeText(MainActivity.this, " 失败开始",
								Toast.LENGTH_SHORT).show();
					}

				}
			});
		}
		
//		getTeleState();

	}

 

	final Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			textView.append((String)msg.obj);
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
