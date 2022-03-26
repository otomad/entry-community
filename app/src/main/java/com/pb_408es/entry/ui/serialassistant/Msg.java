package com.pb_408es.entry.ui.serialassistant;

public class Msg {
	public enum Type { RECEIVED, SEND };
	private String content;
	private Type type;

	public Msg(String content,Type type){
		this.content = content;
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public Type getType() {
		return type;
	}
}
