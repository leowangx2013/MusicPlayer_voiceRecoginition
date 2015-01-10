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
	static SeekBar progress; // 可拖动进度条,控制音乐播放进度
	//SeekBar volumn; // 可拖动进度条控制音量
	static public ArrayList<HashMap<String, Object>> songList; // sd卡内音乐文件列表
	static public int songListLen; // sd卡内音乐文件数量
	
	AudioManager audioManager; // 监控耳机插拔
	boolean isHeadsetOn; // 监控耳机插拔

	static public boolean plugHeadset = false; // 用于记录耳机是否插拔

	String commandText; // 云端返回的所有指令文本
	
	//GestureDetector detector;5 // 手势检测
	private static final int FLING_MIN_DISTANCE = 80;  // 滑动的最短距离
	private static final int FLING_MIN_VELOCITY = 150; // 滑动的最小速度
	int layoutNum = 0; // 界面编号：0为主界面， 1为歌曲列表界面

	ActivityReceiver activityReceiver = new ActivityReceiver();
	public static SongList musicList = new SongList();// 初始化歌曲列表SongList

	SongList customList = new SongList(); // 用户自定义播放列表
	
	public static final String CTL_ACTION = "android.intent.action.MUSICSERVICE";
	public static final String UPDATE_ACTION = "android.intent.action.MAIN";
	private static String TAG = "MusicBox";// 打印日志标识
	private static String REC = "Record";// 用于语音识别相关测试
	// 语音识别对象
	private SpeechRecognizer mAsr;
	// 语音识别引擎设置，云端引擎
	private String mEngineType = "cloud";
	// 缓存
	private SharedPreferences mSharedPreferences;
	// 字符分析类
	stringAnalyser sa = new stringAnalyser();

	// 0x11--->stop
	// 0x12--->play
	// 0x13--->pause
	int status = 0x11;

	// 测试专用
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

		songList = scanAllAudioFiles(); // 获取SD卡内音乐文件列表
		songListLen = songList.size();
		// musicId:音乐ID
		// musicTitle：音乐标题
		// musicFileUrl:url链接
		// music_file_name:文件名
		// musicArtist:艺术家
		// musicAlbum:专辑名

		// 初始化SpeechUtility
		SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5449becd");

		// 初始化InitListener,
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
	            //获得歌曲现在播放位置并设置成播放进度条的值  
	        progress.setProgress(MusicService.mediaPlayer.getCurrentPosition());  
	        }  
	    }; 
		progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// 根据用户改变的滑块的值改变播放进度
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
		
		//detector = new GestureDetector(this,this); // 创建手势检测器
		
		 
		// 初始化识别对象
		mAsr = SpeechRecognizer.createRecognizer(this, mInitListener);

		// 初始化云端引擎
		mEngineType = SpeechConstant.TYPE_CLOUD;

		mSharedPreferences = getSharedPreferences(getPackageName(),
				MODE_PRIVATE);

		// Broadcast筛选intent设置
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		filter.addAction("android.intent.action.HEADSET_PLUG");
		registerReceiver(activityReceiver, filter); 
		Intent intent1 = new Intent(this, MusicService.class);
		startService(intent1);
		Intent intent2 = new Intent(this, stringAnalyser.class);
		startService(intent2);
		// 初始化歌曲名、歌手
		title.setText((String) songList.get(0).get("musicTitle"));
		author.setText((String) songList.get(0)
				.get("musicArtist"));

	}
	
	// 手势识别
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
		// 若从右向左的滑行距离大于最小识别距离，触发切换Activity时间
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

	int ret = 0;// 函数调用返回值

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// System.out.println("onClickListener");
		Intent buttonIntent = new Intent(CTL_ACTION);
		switch (v.getId()) {
		// 按键控制发出的intent共有两个属性
		// 1. “command”-具体指令
		// 2. “source”-来源，0为按键，1为语音指令
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
			// 设置参数
			System.out.println("---flag1---");
			Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show();
			if (!setParam()) {
				showTip("请先构建语法。");
				return;
			}

			isHeadsetOn = audioManager.isWiredHeadsetOn();
			// 若耳机未插上，暂停音乐播放直至语音指令识别完毕
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
				showTip("识别失败,错误码: " + ret);
				System.out.println("--->showTip succeed");
			}
			break;
		}
	}

	RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onVolumeChanged(int volume) {
			showTip("当前正在说话，音量大小：" + volume);
		}

		@Override
		public void onResult(final RecognizerResult result, boolean isLast) {

			System.out.println("---flag2---");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (null != result) {
						onResultNum++;
						// 解析云端结果
						commandText += JsonParser.parseGrammarResult(result
								.getResultString());

						System.out.println(commandText);
						//feedback.setText(commandText);
						// 对云端返回结果进行指令的匹配,匹配结果将直接通过Intent调控MusicService的音乐控制方法
						Intent intent = new Intent(
								"android.intent.action.STRINGANALYSER");
						intent.putExtra("voiceCommand", commandText);
						sendBroadcast(intent);
						// 若耳机未插上，恢复音乐播放
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

			showTip("结束说话");
			onEndOfSpeechNum++;
		}

		@Override
		public void onBeginOfSpeech() {
			showTip("开始说话");
		}

		@Override
		public void onError(SpeechError error) {
			showTip("onError Code：" + error.getErrorCode());
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
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	// tag3
	public boolean setParam() {
		boolean result = false;
		Log.d(TAG, "--->in setParam");
		// System.out.println("--->in setParam");
		// 设置识别引擎
		mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// System.out.println("--->setParameter succeed");
		if ("cloud".equalsIgnoreCase(mEngineType)) {
			// System.out.println("--->cloud is the engine type");

			// 设置云端返回结果为json格式
			mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
			// System.out.println("set result is json");
			// 设置云端识别使用的语法id
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
		// 退出时释放连接
		mAsr.cancel();
		mAsr.destroy();
	}

	// 读取SD卡中的音乐文件，返回ArrayList<HashMap<String, Object>>类
	public ArrayList<HashMap<String, Object>> scanAllAudioFiles() {
		// 生成动态数组，并且转载数据
		ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();

		// 查询媒体数据库
		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		// 遍历媒体数据库
		int count = 0; // 记录歌曲编号
		if (cursor.moveToFirst()) {

			while (!cursor.isAfterLast()) {

				// 歌曲编号
				// int id =
				// cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
				int id = count;
				// 歌曲标题
				String tilte = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
	
				// 歌曲的专辑名：MediaStore.Audio.Media.ALBUM
				String album = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
				
				// 歌曲的歌手名： MediaStore.Audio.Media.ARTIST
				String artist = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				// 歌曲文件的路径 ：MediaStore.Audio.Media.DATA
				String url = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				// 歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
				int duration = cursor
						.getInt(cursor
								.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				// 歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
				Long size = cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

				if (size > 1024 * 800) {// 大于800K
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
