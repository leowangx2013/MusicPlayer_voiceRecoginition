package com.example.musicboxredo;

import android.app.Activity;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;

public class musicListMenu extends Activity implements OnGestureListener {
	GestureDetector detector; // 手势检测
	private static final int FLING_MIN_DISTANCE = 80; // 滑动的最短距离
	private static final int FLING_MIN_VELOCITY = 150; // 滑动的最小速度

	public void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_list_menu);
	}

	// 手势识别
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return detector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		// 若从左向右的滑行距离大于最小识别距离，触发切换Activity时间
		if (e2.getX() - e1.getX() >= FLING_MIN_DISTANCE ) {
//			Intent intent = new Intent();
//			intent.setAction("android.intent.action.MAINACTIVITY");
//			startActivity(intent);
			setContentView(R.layout.activity_musix_box);
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}
