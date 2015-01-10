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
	// int number = 3; // �赥�ĸ�����Ŀ
	AssetManager am;
	MyReceiver serviceReceiver;
	static MediaPlayer mediaPlayer;

	int status = 0x11;
	int current = 0;
	// 1. command: ָ�����ͣ�1-���ţ�2-��ͣ��3-ֹͣ��4-��һ�ף�5-��һ��, -1-�쳣
	// 2. songNum: ָ�����Ÿ���������δָ����Ϊ-1
	// 3. ��source��-��Դ��0Ϊ������1Ϊ����ָ��
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

			// ��������MainActivity��Intent������
			// int control = intent.getIntExtra("control", 1);

			command = intent.getIntExtra("command", -1);
			songNum = intent.getIntExtra("songNum", -1);
			source = intent.getIntExtra("source", 0);

			// �ж�intent��Դ
			if (source == 0) {
				buttonAnalyse(command); // ����command����
			} else {
				System.out.println("source = " + source);
				voiceAnalyse(command, songNum);
			}
			// ��Intent����������Ӧ�ĵ���

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
			
			MainActivity.progress.setMax(mediaPlayer.getDuration()); // ������������Ϊ����ʱ�䳤��
			MainActivity.progress.setProgress(0);
			mediaPlayer.start();
			System.out.println("flag3");
		} catch (IOException e) {
			System.out.println("--->prepareAndPlay Error");
			e.printStackTrace();
		}

	}

	// ���ڷ����������͵�intent
	// 1. command: ָ�����ͣ�1-���ţ�2-��ͣ��3-ֹͣ��4-��һ�ף�5-��һ��, -1-�쳣
	// 2. songNum: ָ�����Ÿ���������δָ����Ϊ-1
	// 0x11--->stop
	// 0x12--->play
	// 0x13--->pause
	public void buttonAnalyse(int extraContent) {
		System.out.println("buttonAnalyse enter");
		switch (extraContent) {
		// ����
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
		// ֹͣ
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
		// ����ָ����Чʱ
		case -1:
			System.out.println("--->Invalid Command");
			break;
		}

	}

	// ���ڷ�������ʶ���͵�intent
	// 1. command: ָ�����ͣ�1-���ţ�2-��ͣ��3-ֹͣ��4-��һ�ף�5-��һ��, -1-�쳣
	// 2. songNum: ָ�����Ÿ���������δָ����Ϊ-1
	// 0x11--->stop
	// 0x12--->play
	// 0x13--->pause
	public void voiceAnalyse(int command, int songNum) {
		// ������ָ����Ŀ����ֱ�Ӳ��Ÿø���
		System.out.println("command = " + command);
		System.out.println("songNum = " + songNum);
		if (songNum != -1) {
			prepareAndPlay((String) MainActivity.songList.get(songNum).get(
					"musicFileUrl"));
			status = 0x12;
			current = songNum;
		} else {
			switch (command) {
			// ����
			// ������ֹͣ����ͣ״̬ʱ��ָ����Ч
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
			// ��ͣ
			case 2:
				if (status == 0x12) {
					mediaPlayer.pause();
					status = 0x13;
				}
				// 1. command: ָ�����ͣ�1-���ţ�2-��ͣ��3-ֹͣ��4-��һ�ף�5-��һ��, -1-�쳣
				// 2. songNum: ָ�����Ÿ���������δָ����Ϊ-1
				// 0x11--->stop
				// 0x12--->play
				// 0x13--->pause
				// ֹͣ
			case 3:
				System.out.println("ֹͣ");
				if (status == 0x12 || status == 0x13) {
					mediaPlayer.stop();
					status = 0x11;
				}
				break;
			// ��һ��
			case 4:
				current++;
				if (current >= MainActivity.songListLen) {
					current = 0;
				}
				prepareAndPlay((String) MainActivity.songList.get(current).get(
						"musicFileUrl"));
				status = 0x12;
				break;
			// ��һ��
			case 5:
				current--;
				if (current < 0) {
					current = MainActivity.songListLen - 1;
				}
				prepareAndPlay((String) MainActivity.songList.get(current).get(
						"musicFileUrl"));
				status = 0x12;
				break;
			// ����ָ����Чʱ
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
