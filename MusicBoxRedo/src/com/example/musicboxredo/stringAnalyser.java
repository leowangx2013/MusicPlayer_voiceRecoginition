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
	private int status = 0; // �����û�����ָ�������Ӧ����ɵ�״̬��1-���ţ�2-��ͣ��3-ֹͣ��4-��һ�ף�5-��һ�ף�-1-�쳣�����䣩
	private int songNum = -1; // �����û�����ָ����ŵ���Ŀ��-1Ϊ����

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

	// ���ƶ˷��ص�����ָ���ı�����ƥ�䣬����ָ����
	public void analyseString(String control) {

		String play = "����";
		String pause = "��ͣ";
		String stop = "ֹͣ";
		String next = "��һ��";
		String prev = "��һ��";

		System.out.println("--->enter analyseString");

		Intent intent = new Intent();
		// intent�а�����extra���������ԣ�
		// 1. command: ָ�����ͣ�1-���ţ�2-��ͣ��3-ֹͣ��4-��һ�ף�5-��һ��, -1-�쳣
		// 2. songNum: ָ�����Ÿ���������δָ����Ϊ-1
		// 3. source����Դ��0Ϊ����������intent��1Ϊ����ָ�����intent
		intent.setAction("android.intent.action.MUSICSERVICE");
		intent.putExtra("songNum", -1);
		intent.putExtra("source", 1);
		// System.out.println(control);

		// ȥ�������ַ����ж����ǩ��ո�
		control = control.replace("�������", "");
		control = control.replace("�����Ŷȡ�", "");
		control = control.replace("\n", "");
		control = control.replace("0", "");
		control = control.replace("��", "");
		control = control.replace("��", "");
		System.out.println("control = " + control);
		Toast.makeText(this, control, Toast.LENGTH_SHORT).show();
		if (control.contains(play)) {
			// ��ʶ�𵽲���ָ��ж��Ƿ����Ӹ����������ǣ��򲥷���Ӧ����
			control = control.replaceAll(play, "");
			intent.putExtra("command", 1);
			System.out.println("Start to match title");
			// ��ƥ�䵽������
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
