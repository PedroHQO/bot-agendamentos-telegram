package com.pedrohqo.bot.telegram.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Appointment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String nomeCliente;
	private LocalDateTime dateTime;
	private boolean confirmado;
	private Long chatId;
	
	@ManyToOne
	@JoinColumn(name = "service_id", nullable=false)
	private BotService botService;
	
	
	
	public BotService getBotService() {
		return botService;
	}
	public void setBotService(BotService botService) {
		this.botService = botService;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNomeCliente() {
		return nomeCliente;
	}
	public void setNomeCliente(String nomeCliente) {
		this.nomeCliente = nomeCliente;
	}
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
	public boolean isConfirmado() {
		return confirmado;
	}
	public void setConfirmado(boolean confirmado) {
		this.confirmado = confirmado;
	}
	public Long getChatId() {
		return chatId;
	}
	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}
	

}
