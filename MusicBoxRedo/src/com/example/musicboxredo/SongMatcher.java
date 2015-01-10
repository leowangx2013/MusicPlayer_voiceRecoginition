package com.example.musicboxredo;

public class SongMatcher {
	// 匹配SD卡上音乐文件名
	// 匹配成功返回音乐ID,失败返回-1
	public int matchSong (String songTitle) {
		int songId = -1;
		int eligible[]; // 存放模糊查找得出的歌曲ID
		
		// 筛选步骤1：是否包含songTitle完整字符
		// 如：songTitle = "夜曲", 搜索结果包括"小夜曲"; "泡沫"-“泡沫现场版”  
		// 筛选步骤2：是否包含
		System.out.println("songTitle = " + songTitle);
		for (int i = 0; i < MainActivity.songListLen; i++) {
			String compare = (String)MainActivity.songList.get(i).get("musicTitle");
			compare.replace(".mp3", "");
			System.out.println("compare = " + compare);
			if (compare.equals(songTitle)) {
				songId = i;
			}
		}
		System.out.println("songId = " + songId);
		return songId;
	}
}
