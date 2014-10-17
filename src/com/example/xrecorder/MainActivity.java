package com.example.xrecorder;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	
	public static final int STOPPED = 0;
	public static final int RECORDING = 1;
	
	PcmRecorder recorderInstance = null;
	MyPublisher publisherInstance = null;

	Button startButton = null;
	Button stopButton = null;
	Button exitButon = null;
	TextView textView = null;
	int status = STOPPED;
	
	public void onClick(View v) {
		if (v == startButton) {
			this.setTitle("start");
			
			if(recorderInstance == null){
				recorderInstance = new PcmRecorder();
				Thread th = new Thread(recorderInstance);
				th.start();
			}
			recorderInstance.setRecording(true);
			/*
			if (publisherInstance == null) {
				publisherInstance = new MyPublisher();
				publisherInstance.connect(Config.SERVER_URL, Config.APP_NAME);
				publisherInstance.publish();
			}*/
		}
		if (v == stopButton) {
			this.setTitle("stop");
			
			if (recorderInstance != null) {
				recorderInstance.setRecording(false);
			}
			
			if (publisherInstance != null) {
				publisherInstance.disConnect();
			}
		}
		if (v == exitButon) {
			if (recorderInstance != null) {
				recorderInstance.setRecording(false);
			}
			
			if (publisherInstance != null) {
				publisherInstance.disConnect();
			}
			
			System.exit(0);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		startButton = new Button(this);
		stopButton = new Button(this);
		exitButon = new Button(this);
		textView = new TextView(this);
		
		startButton.setText("Start");
		stopButton.setText("Stop");
		exitButon.setText("Exit");
		textView.setText("xiaojun");
		
		startButton.setOnClickListener(this);
		stopButton.setOnClickListener(this);
		exitButon.setOnClickListener(this);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(textView);
		layout.addView(startButton);
		layout.addView(stopButton);
		layout.addView(exitButon);
		this.setContentView(layout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
