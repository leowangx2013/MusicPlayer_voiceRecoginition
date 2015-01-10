package com.example.musicboxredo;

import java.io.IOException;

import android.R.layout;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;

public class MusicService extends Service {
	// int number = 3; // 歌单的歌曲书目
	AssetManager am;
	MyReceiver serviceReceiver;
	static MediaPlayer mediaPlayer;

	int status = 0x11;
	int current = 0;
	// 1. command: 指令类型，1-播放，2-暂停，3-停止，4-下一首，5-上一首, -1-异常
	// 2. songNum: 指定播放歌曲名，若未指定则为-1
	// 3. “source”-来源，0为按键，1为语音指令
	int command = -1;
	int songNum = -1;
	int source = 0;

	@Override
	public void onCreate() { // TODO Auto-generated method stub

		// TODO Auto-generated method stub
		System.out.println("MusicService onCreate");
		am = getAssets();
		serviceReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.MUSICSERVICE");
		registerReceiver(serviceReceiver, filter);
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				current++;
				if (current >= MainActivity.songListLen) {
					current = 0;
				}
				Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
				sendIntent.putExtra("current", current);
				sendBroadcast(sendIntent);
				prepareAndPlay((String) MainActivity.songList.get(current).get(
						"musicFileUrl"));
			}
		});

		super.onCreate();
	}

	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			System.out.println("service intent received");

			// 接收来自MainActivity的Intent的内容
			// int control = intent.getIntExtra("control", 1);

			command = intent.getIntExtra("command", -1);
			songNum = intent.getIntExtra("songNum", -1);
			source = intent.getIntExtra("source", 0);

			// 判断intent来源
			if (source == 0) {
				buttonAnalyse(command); // 分析command即可
			} else {
				System.out.println("source = " + source);
				voiceAnalyse(command, songNum);
			}
			// 对Intent内容做出相应的调控

			Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
			sendIntent.putExtra("update", status);
			sendIntent.putExtra("current", current);
			sendBroadcast(sendIntent);

		}
	}

	private void prepareAndPlay(String uml) {

		// TODO Auto-generated method stub
		try {

			mediaPlayer.reset();
			mediaPlayer.setDataSource(uml);
			System.out.println("flag1");
			mediaPlayer.prepare();
			System.out.println("flag2");
			
			MainActivity.progress.setMax(mediaPlayer.getDuration()); // 将进度条设置为歌曲时间长度
			MainActivity.progress.setProgress(0);
			mediaPlayer.start();
			System.out.println("flag3");
		} catch (IOException e) {
			System.out.println("--->prepareAndPlay Error");
			e.printStackTrace();
		}

	}

	// 用于分析按键发送的intent
	// 1. command: 指令类型，1-播放，2-暂停，3-停止，4-下一首，5-上一首, -1-异常
	// 2. songNum: 指定播放歌曲名，若未指定则为-1
	// 0x11--->stop
	// 0x12--->play
	// 0x13--->pause
	public void buttonAnalyse(int extraContent) {
		System.out.println("buttonAnalyse enter");
		switch (extraContent) {
		// 播放
		case 1:
			if (status == 0x11) {
				System.out.println("--->status is 0x11, preparing for playing");

				System.out.println((String) MainActivity.songList.get(current)
						.get("musicFileUrl"));
				prepareAndPlay((String) MainActivity.songList.get(current).get(
						"musicFileUrl"));
				status = 0x12;
			} else if (status == 0x12) {
				System.out.println("--->status is 0x12, pause");
				mediaPlayer.pause();
				status = 0x13;
			} else if (status == 0x13) {
				System.out.println("--->status is 0x13, preparing for playing");
				mediaPlayer.start();
				status = 0x12;
			}
			break;
		// 停止
		case 2:
			System.out.println("status = " + status);
			if (status == 0x12) {
				mediaPlayer.stop();
				status = 0x11;
			}
			break;
		// next
		case 3:
			current++;
			if (current >= MainActivity.songListLen ) {
				current = 0;
			}
			System.out.println("next song is: "
					+ (String) MainActivity.songList.get(current).get(
							"musicFileUrl"));
			prepareAndPlay((String) MainActivity.songList.get(current).get(
					"musicFileUrl"));
			status = 0x12;
			break;
		// prev
		case 4:
			current--;
			if (current < 0) {
				current = MainActivity.songListLen - 1;
			}
			System.out.println("last song is: "
					+ (String) MainActivity.songList.get(current).get(
							"musicFileUrl"));

			prepareAndPlay((String) MainActivity.songList.get(current).get(
					"musicFileUrl"));
			status = 0x12;
			break;
		// 语音指令无效时
		case -1:
			System.out.println("--->Invalid Command");
			break;
		}

	}

	// 用于分析语音识别发送的intent
	// 1. command: 指令类型，1-播放，2-暂停，3-停止，4-下一首，5-上一首, -1-异常
	// 2. songNum: 指定播放歌曲名，若未指定则为-1
	// 0x11--->stop
	// 0x12--->play
	// 0x13--->pause
	public void voiceAnalyse(int command, int songNum) {
		// 若存在指定曲目，则直接播放该歌曲
		System.out.println("command = " + command);
		System.out.println("songNum = " + songNum);
		if (songNum != -1) {
			prepareAndPlay((String) MainActivity.songList.get(songNum).get(
					"musicFileUrl"));
			status = 0x12;
			current = songNum;
		} else {
			switch (command) {
			// 播放
			// 当处于停止或暂停状态时此指令有效
			case 1:
				if (status == 0x11) {
					/*
					 * System.out
					 * .println("--->status is 0x11, preparing for playing");
					 * System.out.println("--->" +
					 * MainActivity.musicList.retTitle(current));
					 */
					prepareAndPlay((String) MainActivity.songList.get(current)
							.get("musicFileUrl"));
					status = 0x12;
				} else if (status == 0x13) {
					System.out
							.println("--->status is 0x13, preparing for playing");
					mediaPlayer.start();
					status = 0x12;
				}
				break;
			// 暂停
			case 2:
				if (status == 0x12) {
					mediaPlayer.pause();
					status = 0x13;
				}
				// 1. command: 指令类型，1-播放，2-暂停，3-停止，4-下一首，5-上一首, -1-异常
				// 2. songNum: 指定播放歌曲名，若未指定则为-1
				// 0x11--->stop
				// 0x12--->play
				// 0x13--->pause
				// 停止
			case 3:
				System.out.println("停止");
				if (status == 0x12 || status == 0x13) {
					mediaPlayer.stop();
					status = 0x11;
				}
				break;
			// 下一首
			case 4:
				current++;
				if (current >= MainActivity.songListLen) {
					current = 0;
				}
				prepareAndPlay((String) MainActivity.songList.get(current).get(
						"musicFileUrl"));
				status = 0x12;
				break;
			// 上一首
			case 5:
				current--;
				if (current < 0) {
					current = MainActivity.songListLen - 1;
				}
				prepareAndPlay((String) MainActivity.songList.get(current).get(
						"musicFileUrl"));
				status = 0x12;
				break;
			// 语音指令无效时
			case -1:
				System.out.println("--->Invalid Command");
				break;
			}
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
