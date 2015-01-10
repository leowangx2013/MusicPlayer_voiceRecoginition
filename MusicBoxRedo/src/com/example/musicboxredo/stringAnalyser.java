package com.example.musicboxredo;

import java.net.Inet4Address;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class stringAnalyser extends Service {
	private String control;
	private SongMatcher matcher;
	MyReceiver receiver;
	private int status = 0; // 根据用户语音指令，播放器应变更成的状态，1-播放，2-暂停，3-停止，4-下一首，5-上一首，-1-异常（不变）
	private int songNum = -1; // 根据用户语音指令，播放的曲目，-1为不变

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("stringAnalyser onCreate()");
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.STRINGANALYSER");
		registerReceiver(receiver, filter);
		matcher = new SongMatcher();
	}

	// 对云端返回的语音指令文本进行匹配，返回指令编号
	public void analyseString(String control) {

		String play = "播放";
		String pause = "暂停";
		String stop = "停止";
		String next = "下一首";
		String prev = "上一首";

		System.out.println("--->enter analyseString");

		Intent intent = new Intent();
		// intent中包含的extra含三个属性：
		// 1. command: 指令类型，1-播放，2-暂停，3-停止，4-下一首，5-上一首, -1-异常
		// 2. songNum: 指定播放歌曲名，若未指定则为-1
		// 3. source：来源，0为按键发出的intent，1为语音指令发出的intent
		intent.setAction("android.intent.action.MUSICSERVICE");
		intent.putExtra("songNum", -1);
		intent.putExtra("source", 1);
		// System.out.println(control);

		// 去除返回字符串中多余标签与空格
		control = control.replace("【结果】", "");
		control = control.replace("【置信度】", "");
		control = control.replace("\n", "");
		control = control.replace("0", "");
		control = control.replace("。", "");
		control = control.replace("，", "");
		System.out.println("control = " + control);
		Toast.makeText(this, control, Toast.LENGTH_SHORT).show();
		if (control.contains(play)) {
			// 若识别到播放指令，判断是否连接歌曲名，若是，则播放相应歌曲
			control = control.replaceAll(play, "");
			intent.putExtra("command", 1);
			System.out.println("Start to match title");
			// 若匹配到歌曲名
			int songNum = matcher.matchSong(control);
			
			if (songNum != -1) {
				intent.putExtra("songNum", songNum);
			}
			sendBroadcast(intent);

		} else if (control.contains(pause)) {
			intent.putExtra("command", 2);
			sendBroadcast(intent);
		} else if (control.contains(stop)) {
			intent.putExtra("command", 3);
			sendBroadcast(intent);
		} else if (control.contains(next)) {
			intent.putExtra("command", 4);
			sendBroadcast(intent);
		} else if (control.contains(prev)) {
			intent.putExtra("command", 5);
			sendBroadcast(intent);

		} else {
			// Exception occur
			intent.putExtra("command", -1);
			sendBroadcast(intent);
		}
	}

	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			control = intent.getStringExtra("voiceCommand");
			System.out.println("!!!!!!!!!!!control = " + control
					+ "!!!!!!!!!!!!!!!!!");
			analyseString(control);
		
		}
	}

}
