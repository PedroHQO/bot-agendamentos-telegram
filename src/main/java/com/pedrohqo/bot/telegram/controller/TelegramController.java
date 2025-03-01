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
import com.pedrohqo.bot.telegram.model.BotService;
import com.pedrohqo.bot.telegram.model.UserState;
import com.pedrohqo.bot.telegram.repository.AppointmentRepository;
import com.pedrohqo.bot.telegram.repository.ServiceRepository;

@RestController
public class TelegramController extends TelegramWebhookBot {

	@Autowired
	private AppointmentRepository appointmentRepository;

	private Map<Long, UserState> userState = new HashMap<>(); // Armazena o estado do usuário

	@Autowired
	private ServiceRepository serviceRepository;
	
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
					userStateObj.setState("AGUARDANDO_SERVICO");
					
					StringBuilder response = new StringBuilder("Ótimo! Agora, escolha um serviço pelo o Número:\n\n");
					for(BotService botService : serviceRepository.findAll()) {
						response.append("🔹 ").append(botService.getId())
						.append(": ").append(botService.getNome())
						.append(" - ").append(botService.getDescricao())
						.append(" - R$ ").append(botService.getPreco()).append("\n");
					}
					return sendMessage(chatId, response.toString());
					
					} catch (DateTimeParseException e) {
					return sendMessage(chatId, "Formato de data inválido. Por favor, use o formato dd/MM/yyyy HH:mm!");
				}
			}else if(userStateObj != null && "AGUARDANDO_SERVICO".equalsIgnoreCase(userStateObj.getState())) {
				try {
					Long serviceId = Long.parseLong(text);
					BotService selectedService = serviceRepository.findById(serviceId).orElse(null);
					
					if(selectedService == null) {
						return sendMessage(chatId, "⚠️ Número do serviço inválido. Por favor, escolha um Número da lista.");
					}
					
					userStateObj.setServiceId(serviceId);
					userStateObj.setState("AGUARDANDO_CONFIRMACAO");
					
					return sendMessage(chatId, "🔍 Você escolheu o serviço: " + selectedService.getNome() +
							"\n💰 Preço: R$ " + selectedService.getPreco() +
							"\n\nConfirme seu agendamento:\n" +
							"1️⃣ Confirmar\n" +
							"2️⃣ Corrigir data\n" +
							"3️⃣ Cancelar");	
				}catch (NumberFormatException e) {
					return sendMessage(chatId, "⚠️ Entrada inválida. Informe o Número do serviço corretamente.");
				}
			}
			else if(userStateObj != null && "AGUARDANDO_CONFIRMACAO".equals(userStateObj.getState())) {
					if("1".equals(text)) {
						Appointment appointment = new Appointment();
						appointment.setNomeCliente(userStateObj.getName());
						appointment.setDateTime(userStateObj.getDateTime());
						Long serviceId = userStateObj.getServiceId();
						BotService botService = serviceRepository.findById(serviceId)
								.orElseThrow(() -> new RuntimeException("Servico não encontrado com o ID: " + serviceId));
						
						appointment.setBotService(botService);
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
			}else if(text.equalsIgnoreCase("/servicos")){
				return listarServicos(chatId);
			}
			else {
				return sendMessage(chatId, "Olá! Para ver nossos serviços digite /servicos \n\n"
						+ " Para agendar uma consulta, digite /agendar");
			}
		}

		return null;
	}
	
	private BotApiMethod<?> listarServicos(Long chatId){
		StringBuilder response = new StringBuilder("📋 Nossos Serviços Disponíveis:\n\n");
		
		for(BotService botService : serviceRepository.findAll()) {
			response.append("🔹 ").append(botService.getId())
			.append(": ").append(botService.getNome())
			.append(" - ").append(botService.getDescricao())
			.append(" - R$ ").append(botService.getPreco()).append("\n");
		}
		
		return sendMessage(chatId, response.toString());
		
	}

	private SendMessage sendMessage(Long chatId, String text) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(text);
		return message;
	}
}