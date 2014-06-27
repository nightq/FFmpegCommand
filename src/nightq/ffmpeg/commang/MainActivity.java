package nightq.ffmpeg.commang;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ffmpegcommand.R;


public class MainActivity extends Activity {

	private native void transcodeVideoForTimehutJni(String[] array);
//	private native void setJNIEnv();
	
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
        	Log.e("Nightq", "transcode success");
    	} else if ("ANDROID_FOR_TIMEHUT_TRANSCODE_FAIL".equalsIgnoreCase(log)) {
        	Log.e("Nightq", "transcode fail" );
    	}
    	Log.e("Nightq", Thread.currentThread().getName() + Thread.currentThread().getId() + "log:" + log);// Thread.currentThread().getName() + 
    }
	
	EditText editText;
	TextView textView;
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
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				backHandler.post(new Runnable() {
//
//					@Override
//					public void run() {
				
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
						String[] array = editText.getText().toString()
								.split(" ");
						Log.e("onclick", "" + Thread.currentThread().getName() + Thread.currentThread().getId());
						transcodeVideoForTimehutLocal(array);
//					}
//				}).start();
//					}
//				});
			}
		});
//		setJNIEnv();
	}
	
	private void transcodeVideoForTimehutLocal(String[] array) {
		transcodeVideoForTimehutJni(array);
	}
}
