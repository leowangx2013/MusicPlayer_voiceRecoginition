package com.example.musicboxredo;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	TextView title, author;
	//TextView feedback;
	Button play;
	Button stop, next, prev;
	Button record;
	static SeekBar progress; // ���϶�������,�������ֲ��Ž���
	//SeekBar volumn; // ���϶���������������
	static public ArrayList<HashMap<String, Object>> songList; // sd���������ļ��б�
	static public int songListLen; // sd���������ļ�����
	
	AudioManager audioManager; // ��ض������
	boolean isHeadsetOn; // ��ض������

	static public boolean plugHeadset = false; // ���ڼ�¼�����Ƿ���

	String commandText; // �ƶ˷��ص�����ָ���ı�
	
	//GestureDetector detector;5 // ���Ƽ��
	private static final int FLING_MIN_DISTANCE = 80;  // ��������̾���
	private static final int FLING_MIN_VELOCITY = 150; // ��������С�ٶ�
	int layoutNum = 0; // �����ţ�0Ϊ�����棬 1Ϊ�����б����

	ActivityReceiver activityReceiver = new ActivityReceiver();
	public static SongList musicList = new SongList();// ��ʼ�������б�SongList

	SongList customList = new SongList(); // �û��Զ��岥���б�
	
	public static final String CTL_ACTION = "android.intent.action.MUSICSERVICE";
	public static final String UPDATE_ACTION = "android.intent.action.MAIN";
	private static String TAG = "MusicBox";// ��ӡ��־��ʶ
	private static String REC = "Record";// ��������ʶ����ز���
	// ����ʶ�����
	private SpeechRecognizer mAsr;
	// ����ʶ���������ã��ƶ�����
	private String mEngineType = "cloud";
	// ����
	private SharedPreferences mSharedPreferences;
	// �ַ�������
	stringAnalyser sa = new stringAnalyser();

	// 0x11--->stop
	// 0x12--->play
	// 0x13--->pause
	int status = 0x11;

	// ����ר��
	int onResultNum = 0;
	int onEndOfSpeechNum = 0;

	ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_musix_box);

		play = (Button) findViewById(R.id.play);
		stop = (Button) findViewById(R.id.stop);
		next = (Button) findViewById(R.id.next);
		prev = (Button) findViewById(R.id.prev);
		title = (TextView) findViewById(R.id.title);
		author = (TextView) findViewById(R.id.author);

		record = (Button) findViewById(R.id.record);
		//feedback = (TextView) findViewById(R.id.feedback);

		progress = (SeekBar) findViewById(R.id.progress);
		//volumn = (SeekBar) findViewById(R.id.volumn);
		
		play.setOnClickListener(this);
		stop.setOnClickListener(this);
		next.setOnClickListener(this);
		prev.setOnClickListener(this);
		record.setOnClickListener(this);
		//feedback.setOnClickListener(this);
		audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		isHeadsetOn = audioManager.isWiredHeadsetOn();

		songList = scanAllAudioFiles(); // ��ȡSD���������ļ��б�
		songListLen = songList.size();
		// musicId:����ID
		// musicTitle�����ֱ���
		// musicFileUrl:url����
		// music_file_name:�ļ���
		// musicArtist:������
		// musicAlbum:ר����

		// ��ʼ��SpeechUtility
		SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5449becd");

		// ��ʼ��InitListener,
		InitListener mInitListener = new InitListener() {

			@Override
			public void onInit(int code) {
				Log.d(TAG, "SpeechRecognizer init() code = " + code);
				if (code != ErrorCode.SUCCESS) {
					Log.d(TAG, "ErrorCode");
				}
				Log.d(TAG, "--->onInit succeed");
				// System.out.println("--->onInit succeed");
			}
		};
		Handler handler = new Handler();  
	    Runnable updateThread = new Runnable(){  
	        public void run() {  
	            //��ø������ڲ���λ�ò����óɲ��Ž�������ֵ  
	        progress.setProgress(MusicService.mediaPlayer.getCurrentPosition());  
	        }  
	    }; 
		progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// �����û��ı�Ļ����ֵ�ı䲥�Ž���
				if (fromUser == true) {
					MusicService.mediaPlayer.seekTo(progress);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		});
		
		//detector = new GestureDetector(this,this); // �������Ƽ����
		
		 
		// ��ʼ��ʶ�����
		mAsr = SpeechRecognizer.createRecognizer(this, mInitListener);

		// ��ʼ���ƶ�����
		mEngineType = SpeechConstant.TYPE_CLOUD;

		mSharedPreferences = getSharedPreferences(getPackageName(),
				MODE_PRIVATE);

		// Broadcastɸѡintent����
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		filter.addAction("android.intent.action.HEADSET_PLUG");
		registerReceiver(activityReceiver, filter); 
		Intent intent1 = new Intent(this, MusicService.class);
		startService(intent1);
		Intent intent2 = new Intent(this, stringAnalyser.class);
		startService(intent2);
		// ��ʼ��������������
		title.setText((String) songList.get(0).get("musicTitle"));
		author.setText((String) songList.get(0)
				.get("musicArtist"));

	}
	
	// ����ʶ��
	/*@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "touch", Toast.LENGTH_SHORT).show();;
		return detector.onTouchEvent(event);
	}*/
	/*
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
	     		float velocityY) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "onFling" + (e2.getX() - e1.getX()), Toast.LENGTH_LONG).show();
		// ����������Ļ��о��������Сʶ����룬�����л�Activityʱ��
		if (e1.getX() - e2.getX() >= FLING_MIN_DISTANCE && layoutNum == 0) {
			//Intent intent = new Intent();
			//intent.setAction("android.intent.action.MUSICLISTMENU");
			//startActivity(intent);
			setContentView(R.layout.music_list_menu);
			layoutNum = 1;
		} else if (e2.getX() - e1.getX() >= FLING_MIN_DISTANCE && layoutNum == 1) {
			setContentView(R.layout.activity_musix_box);
			layoutNum = 0;
		}
		return false;
	}
	*/
	public class ActivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int update = intent.getIntExtra("update", -1);
			int current = intent.getIntExtra("current", -1);
			System.out.println("----------current = " + current + "-------------");  
			if (current >= 0) {
				title.setText((String) songList.get(current).get("musicTitle"));
				author.setText((String) songList.get(current)
						.get("musicArtist"));
			}
			switch (update) {
			case 0x11:
				play.setText("play");
				status = 0x11;
				break;
			case 0x12:
				play.setText("pause");
				status = 0x12;
				break;
			case 0x13:
				play.setText("play");
				status = 0x13;
				break;
			}

		}
	}

	int ret = 0;// �������÷���ֵ

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// System.out.println("onClickListener");
		Intent buttonIntent = new Intent(CTL_ACTION);
		switch (v.getId()) {
		// �������Ʒ�����intent������������
		// 1. ��command��-����ָ��
		// 2. ��source��-��Դ��0Ϊ������1Ϊ����ָ��
		case R.id.play:
			buttonIntent.putExtra("command", 1);
			buttonIntent.putExtra("source", 0);
			sendBroadcast(buttonIntent);
			System.out.println("play button sentIntent");
			System.out.println(buttonIntent.getAction());
			break;
		case R.id.stop:
			buttonIntent.putExtra("command", 2);
			buttonIntent.putExtra("source", 0);
			sendBroadcast(buttonIntent);
			break;
		case R.id.next:
			buttonIntent.putExtra("command", 3);
			buttonIntent.putExtra("source", 0);
			sendBroadcast(buttonIntent);
			break;
		case R.id.prev:
			buttonIntent.putExtra("command", 4);
			buttonIntent.putExtra("source", 0);
			sendBroadcast(buttonIntent);
			break;
		case R.id.record:
			// ���ò���
			System.out.println("---flag1---");
			Toast.makeText(this, "��ʼ¼��", Toast.LENGTH_SHORT).show();
			if (!setParam()) {
				showTip("���ȹ����﷨��");
				return;
			}

			isHeadsetOn = audioManager.isWiredHeadsetOn();
			// ������δ���ϣ���ͣ���ֲ���ֱ������ָ��ʶ�����
			if (!isHeadsetOn) {
				MusicService.mediaPlayer.pause();
			}
			commandText = "";
			onResultNum = 0;
			onEndOfSpeechNum = 0;
			ret = mAsr.startListening(mRecognizerListener);
			System.out.println("---flag6---");
			if (ret != ErrorCode.SUCCESS) {
				System.out.println("--->showTip");
				showTip("ʶ��ʧ��,������: " + ret);
				System.out.println("--->showTip succeed");
			}
			break;
		}
	}

	RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onVolumeChanged(int volume) {
			showTip("��ǰ����˵����������С��" + volume);
		}

		@Override
		public void onResult(final RecognizerResult result, boolean isLast) {

			System.out.println("---flag2---");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (null != result) {
						onResultNum++;
						// �����ƶ˽��
						commandText += JsonParser.parseGrammarResult(result
								.getResultString());

						System.out.println(commandText);
						//feedback.setText(commandText);
						// ���ƶ˷��ؽ������ָ���ƥ��,ƥ������ֱ��ͨ��Intent����MusicService�����ֿ��Ʒ���
						Intent intent = new Intent(
								"android.intent.action.STRINGANALYSER");
						intent.putExtra("voiceCommand", commandText);
						sendBroadcast(intent);
						// ������δ���ϣ��ָ����ֲ���
						if (!isHeadsetOn && status == 0x12) {
							MusicService.mediaPlayer.start();
						}
	 
					} else {
						Log.d(TAG, "recognizer result : null");
					}
				}
			});

		}

		@Override
		public void onEndOfSpeech() {

			showTip("����˵��");
			onEndOfSpeechNum++;
		}

		@Override
		public void onBeginOfSpeech() {
			showTip("��ʼ˵��");
		}

		@Override
		public void onError(SpeechError error) {
			showTip("onError Code��" + error.getErrorCode());
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// TODO Auto-generated method stub
		}

	};

	private void showTip(final String str) {
		Log.d(TAG, "--->enter showTip");
		Log.d(TAG, "-->str = " + str);
		// System.out.println("--->enter showTip");
		// System.out.println("--->str = " + str);
	}

	/**
	 * ��������
	 * 
	 * @param param
	 * @return
	 */
	// tag3
	public boolean setParam() {
		boolean result = false;
		Log.d(TAG, "--->in setParam");
		// System.out.println("--->in setParam");
		// ����ʶ������
		mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// System.out.println("--->setParameter succeed");
		if ("cloud".equalsIgnoreCase(mEngineType)) {
			// System.out.println("--->cloud is the engine type");

			// �����ƶ˷��ؽ��Ϊjson��ʽ
			mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
			// System.out.println("set result is json");
			// �����ƶ�ʶ��ʹ�õ��﷨id
			// String grammarId =
			// mSharedPreferences.getString(KEY_GRAMMAR_ABNF_ID, null);
			// System.out.println("--->grammarId=" + grammarId);
			// mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId);
			result = true;
		}
		return result;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// �˳�ʱ�ͷ�����
		mAsr.cancel();
		mAsr.destroy();
	}

	// ��ȡSD���е������ļ�������ArrayList<HashMap<String, Object>>��
	public ArrayList<HashMap<String, Object>> scanAllAudioFiles() {
		// ���ɶ�̬���飬����ת������
		ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();

		// ��ѯý�����ݿ�
		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		// ����ý�����ݿ�
		int count = 0; // ��¼�������
		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {

				// �������
				// int id =
				// cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
				int id = count;
				// ��������
				String tilte = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
	
				// ������ר������MediaStore.Audio.Media.ALBUM
				String album = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
				
				// �����ĸ������� MediaStore.Audio.Media.ARTIST
				String artist = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				// �����ļ���·�� ��MediaStore.Audio.Media.DATA
				String url = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				// �������ܲ���ʱ�� ��MediaStore.Audio.Media.DURATION
				int duration = cursor
						.getInt(cursor
								.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				// �����ļ��Ĵ�С ��MediaStore.Audio.Media.SIZE
				Long size = cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

				if (size > 1024 * 800) {// ����800K
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("musicId", id);
					map.put("musicTitle", tilte);
					map.put("musicFileUrl", url);
					map.put("music_file_name", tilte);
					map.put("musicArtist", artist);
					map.put("musicAlbum", album);
					mylist.add(map);
				}
				cursor.moveToNext();
				count = 0;
			}
		}
		return mylist;
	}
 

}
