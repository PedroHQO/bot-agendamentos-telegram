package com.pedrohqo.bot.telegram.model;

import java.time.LocalDateTime;

public class UserState {
	private String state;
	private String name;
	private String aguardando_confirmacao;
	private LocalDateTime dateTime;
	
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAguardando_confirmacao() {
		return aguardando_confirmacao;
	}
	public void setAguardando_confirmacao(String aguardando_confirmacao) {
		this.aguardando_confirmacao = aguardando_confirmacao;
	}
	
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
	
}
