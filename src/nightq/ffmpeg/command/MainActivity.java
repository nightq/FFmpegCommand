package nightq.ffmpeg.command;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
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
//		final Handler backHandler = new Handler(backThread.getLooper());
		
		getCacheDir();
		getExternalCacheDir();
		
		FfmpegTranscodeVideoService.setTranscodeVideoLogListener(new TranscodeVideoLogListener() {
			
			@Override
			public void logTranscodeVideo(final String log) {
				// TODO Auto-generated method stub
				content = log;
//				handler.
			}
		});
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				backHandler.post(new Runnable() {
//
//					@Override
//					public void run() {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						String[] array = editText.getText().toString()
								.split(" ");
						if (FfmpegTranscodeVideoService.isFinished()) {
							Log.e("onclick", "" + Thread.currentThread().getName() + Thread.currentThread().getId());
							if (!new FfmpegTranscodeVideoService()
									.transcodeVideoForTimehutLocal(array)) {
								Toast.makeText(MainActivity.this, "��ûת����",
										Toast.LENGTH_SHORT);
							}
						} else {
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									Toast.makeText(MainActivity.this, "��ûת����", Toast.LENGTH_SHORT);
								}
							});
						}
					}
				}).start();
//					}
//				});
			}
		});
//		setJNIEnv();
	}

	final Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			textView.setText(content);
		};
	};
}