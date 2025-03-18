package com.pedrohqo.bot.telegram.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
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
	@Schema(description = "ID do agendamento", example = "48")
	private Long id;
	
	@Schema(description = "Nome do cliente", example = "Pedro")
	private String nomeCliente;
	
	@Schema(description = "Data e hora do agendamento", example = "2023-10-01T10:00:00")
	private LocalDateTime dateTime;
	
	@Schema(description = "Indica se o agendamento foi confirmado", example = "true")
	private boolean confirmado;
	
	@Schema(description = "ID do chat no Telegram", example = "123456789")
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
