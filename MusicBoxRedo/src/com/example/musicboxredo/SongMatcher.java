package com.example.musicboxredo;

public class SongMatcher {
	// ƥ��SD���������ļ���
	// ƥ��ɹ���������ID,ʧ�ܷ���-1
	public int matchSong (String songTitle) {
		int songId = -1;
		int eligible[]; // ���ģ�����ҵó��ĸ���ID
		
		// ɸѡ����1���Ƿ����songTitle�����ַ�
		// �磺songTitle = "ҹ��", �����������"Сҹ��"; "��ĭ"-����ĭ�ֳ��桱  
		// ɸѡ����2���Ƿ����
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
