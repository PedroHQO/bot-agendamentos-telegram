package com.pedrohqo.bot.telegram.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.pedrohqo.bot.telegram.model.Appointment;
import com.pedrohqo.bot.telegram.model.UserState;
import com.pedrohqo.bot.telegram.repository.AppointmentRepository;

@RestController
public class TelegramController extends TelegramWebhookBot {

	@Autowired
	private AppointmentRepository appointmentRepository;

	private Map<Long, UserState> userState = new HashMap<>(); // Armazena o estado do usuário

	@Override
	public String getBotUsername() {
		return "Bot-Agendador";
	}

	@Override
	public String getBotToken() {
		return "7737113823:AAHKcf6wEUtaNhb5AIE-7wMq52BB5ldnAY0";
	}

	@Override
	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		return handleWebhook(update);
	}

	@Override
	public String getBotPath() {
		return "/webhook";
	}

	@PostMapping("/webhook")
	public BotApiMethod<?> handleWebhook(@RequestBody Update update) {
		// Verifica se a atualização contém uma mensagem
		if (update.hasMessage()) {
			Message message = update.getMessage();
			Long chatId = message.getChatId();
			String text = message.getText();

			// Recupera o estado do usuário
			UserState userStateObj = userState.get(chatId);

			if (text.equalsIgnoreCase("/agendar")) {
				userStateObj = new UserState(); // Inicializa o estado do usuário
				userStateObj.setState("AGUARDANDO_NOME");
				userState.put(chatId, userStateObj);
				return sendMessage(chatId, "Por gentileza informe seu nome:");
			} else if (userStateObj != null && "AGUARDANDO_NOME".equals(userStateObj.getState())) {
				userStateObj.setName(text); // Salva o nome do cliente
				userStateObj.setState("AGUARDANDO_DATA");
				return sendMessage(chatId,
						"Ótimo, " + text + "! Agora, informe a data e horário (formato: dd/MM/yyyy HH:mm):");
			} else if (userStateObj != null && "AGUARDANDO_DATA".equals(userStateObj.getState())) {
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
					LocalDateTime dateTime = LocalDateTime.parse(text, formatter);		

					userStateObj.setDateTime(dateTime);
					userStateObj.setState("AGUARDANDO_CONFIRMACAO");
					
					return sendMessage(chatId, "Por favor, confirme seu agendamentto:\n\n" +
							"Nome: " + userStateObj.getName() + "\n" +
							"Data e Hora: " + dateTime.format(formatter) + "\n\n" +
							 "1️⃣ Confirmar\n" +
		                     "2️⃣ Corrigir data\n" +
		                     "3️⃣ Cancelar");
					
					} catch (DateTimeParseException e) {
					return sendMessage(chatId, "Formato de data inválido. Por favor, use o formato dd/MM/yyyy HH:mm!");
				}
			} 
			else if(userStateObj != null && "AGUARDANDO_CONFIRMACAO".equals(userStateObj.getState())) {
					if("1".equals(text)) {
						Appointment appointment = new Appointment();
						appointment.setNomeCliente(userStateObj.getName());
						appointment.setDateTime(userStateObj.getDateTime());
						appointmentRepository.save(appointment);
						
						userState.remove(chatId);
						return sendMessage(chatId, "✅ Agendamento confirmado com sucesso!");
					}else if("2".equals(text)) {
						userStateObj.setState("AGUARDANDO_DATA");
						return sendMessage(chatId, "🔄 Ok! Informe novamente a data e horário (formato: dd/MM/yyyy HH:mm):");
						
					}else if("3".equals(text)) {
						userState.remove(chatId);
						return sendMessage(chatId, "❌ Agendamento cancelado. Caso queira tentar novamente, digite /agendar.");
					}else {
						return sendMessage(chatId, "⚠️ Opção inválida. Por favor, escolha:\n\n" +
		                        "1️⃣ Confirmar\n" +
		                        "2️⃣ Corrigir data\n" +
		                        "3️⃣ Cancelar");
					}
			}
			else {
				return sendMessage(chatId, "Olá! Para agendar uma consulta, digite /agendar");
			}
		}

		return null;
	}

	private SendMessage sendMessage(Long chatId, String text) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(text);
		return message;
	}
}