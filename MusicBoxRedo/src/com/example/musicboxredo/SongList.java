package com.example.musicboxredo;

import android.util.Log;

public class SongList {
	private Songs songList[] = new Songs[100];
	private int songNum = 0; //歌曲数量
	
	//添加新的歌曲
	public void addSong(String title, String artist, int id) {
		if(songNum >= 100) {
			System.out.println("List is Full");
			return;
		}
		songList[songNum] = new Songs();
		songList[songNum].setTitle(title);
		songList[songNum].setArtist(artist);
		songList[songNum].setId(id);
		songNum++;
	}
	
	//匹配歌曲名称,返回歌曲Id
	public int MatchTitle(String title) {
		for (int i = 0; i < songNum; i++) {
			if (songList[i].getTitle() == title) {
				return i;
			}
		}
		System.out.println("--->Fail to match");
		return -1;
	}
	
	public String retTitle(int Id) {
		return songList[Id].getTitle();
	}
	
	public String retArtist(int Id) {
		return songList[Id].getArtist();
	}
	
	public int getSongNum() {
		return songNum;
	}
}
