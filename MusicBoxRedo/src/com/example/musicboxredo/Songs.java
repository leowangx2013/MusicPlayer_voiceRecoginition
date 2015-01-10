package com.example.musicboxredo;



public class Songs {
	private String title;
	private String artist;
	private int id;
	public Songs () {
		
	}
	public Songs (String title,String artist, int id) {
		this.title = title;
		this.artist = artist;
		this.id = id;
	}
 
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getArtist() {
		return artist;
	}
	
	public int getId() {
		return id;
	}
}
