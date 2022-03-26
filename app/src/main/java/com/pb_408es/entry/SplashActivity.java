package com.pb_408es.entry;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SplashActivity extends AppCompatActivity {
	private final int SPLASH_DISPLAY_LENGTH = 1000; // 四秒后进入系统
//	private VideoView logoVideo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);
		new android.os.Handler().postDelayed(() -> {
			Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
			SplashActivity.this.startActivity(mainIntent);
			overridePendingTransition(R.anim.zoom_open_in, R.anim.zoom_open_out);
			SplashActivity.this.finish();
		}, SPLASH_DISPLAY_LENGTH);
//		logoVideo = findViewById(R.id.videoView);
//		play_mp4();
//		findViewById(R.id.videoMask).setVisibility(View.INVISIBLE);
	}

	private void play_mp4(VideoView logoVideo) {
		String uri = "android.resource://" + getPackageName() + "/" + R.raw.c_408es;
		logoVideo.setVideoURI(Uri.parse((uri)));
		logoVideo.start();
	}
}