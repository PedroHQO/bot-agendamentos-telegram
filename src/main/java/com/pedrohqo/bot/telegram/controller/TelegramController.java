package com.pedrohqo.bot.telegram.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

			if (text.equalsIgnoreCase("/start") || text.equalsIgnoreCase("Menu")) {
				return sendMessageWithKeyboard(chatId, "Olá! Escolha uma opção:");
			}
			
			else if(text.equalsIgnoreCase("Agendar")) {
				userStateObj = new UserState(); // Inicializa o estado do usuário
				userStateObj.setState("AGUARDANDO_NOME");
				userState.put(chatId, userStateObj);
				return sendMessageWithKeyboard(chatId, "Por gentileza informe seu nome:");
			}else if(text.equalsIgnoreCase("Serviços")) {
				return listarServicos(chatId);
			}else if(text.equalsIgnoreCase("Disponibilidade")) {
				return listaDatasDisponiveis(chatId);
			}else if(text.equalsIgnoreCase("Dúvidas")) {
				return listarDuvidas(chatId);
			}
			
			else if (userStateObj != null && "AGUARDANDO_NOME".equals(userStateObj.getState())) {
				userStateObj.setName(text); // Salva o nome do cliente
				userStateObj.setState("AGUARDANDO_DATA");
				return sendMessageWithKeyboard(chatId,
						"Ótimo, " + text + "! Agora, informe a data e horário (formato: dd/MM/yyyy HH:mm):");
			} else if (userStateObj != null && "AGUARDANDO_DATA".equals(userStateObj.getState())) {
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
					LocalDateTime dateTime = LocalDateTime.parse(text, formatter);
					
					boolean isDateTaken = appointmentRepository.existsByDateTime(dateTime);
					if(isDateTaken) {
						return sendMessageWithKeyboard(chatId, "⚠️ Este horário já está ocupado. Por favor, escolha outro horário!");
					}

					userStateObj.setDateTime(dateTime);
					userStateObj.setState("AGUARDANDO_SERVICO");

					StringBuilder response = new StringBuilder("Ótimo! Agora, escolha um serviço pelo o Número:\n\n");
					for (BotService botService : serviceRepository.findAll()) {
						response.append("🔹 ").append(botService.getId()).append(": ").append(botService.getNome())
								.append(" - ").append(botService.getDescricao()).append(" - R$ ")
								.append(botService.getPreco()).append("\n");
					}
					return sendMessageWithKeyboard(chatId, response.toString());

				} catch (DateTimeParseException e) {
					return sendMessageWithKeyboard(chatId, "Formato de data inválido. Por favor, use o formato dd/MM/yyyy HH:mm!");
				}
			} else if (userStateObj != null && "AGUARDANDO_SERVICO".equalsIgnoreCase(userStateObj.getState())) {
				try {
					Long serviceId = Long.parseLong(text);
					BotService selectedService = serviceRepository.findById(serviceId).orElse(null);

					if (selectedService == null) {
						return sendMessageWithKeyboard(chatId,
								"⚠️ Número do serviço inválido. Por favor, escolha um Número da lista.");
					}

					userStateObj.setServiceId(serviceId);
					userStateObj.setState("AGUARDANDO_CONFIRMACAO");

					return sendMessageWithKeyboard(chatId,
							"🔍 Dados do agendamento: \n" + "Nome: " + userStateObj.getName() + "\nServico: "
									+ selectedService.getNome() + "\nData Agendamento: " + userStateObj.getDateTime()
									+ "\nPreço: R$ " + selectedService.getPreco() + "\n\nConfirme seu agendamento:\n"
									+ "1️⃣ Confirmar\n" + "2️⃣ Corrigir data\n" + "3️⃣ Cancelar");
				} catch (NumberFormatException e) {
					return sendMessageWithKeyboard(chatId, "⚠️ Número do serviço inválido. Por favor, escolha um Número da lista.");
				}
			} else if (userStateObj != null && "AGUARDANDO_CONFIRMACAO".equals(userStateObj.getState())) {
				if ("1".equals(text)) {
					Appointment appointment = new Appointment();
					appointment.setNomeCliente(userStateObj.getName());
					appointment.setDateTime(userStateObj.getDateTime());
					appointment.setChatId(chatId);
					
					Long serviceId = userStateObj.getServiceId();
					BotService botService = serviceRepository.findById(serviceId)
							.orElseThrow(() -> new RuntimeException("Servico não encontrado com o ID: " + serviceId));

					appointment.setBotService(botService);
					appointmentRepository.save(appointment);

					userState.remove(chatId);
					return sendMessageWithKeyboard(chatId, "✅ Agendamento confirmado com sucesso!");
				} else if ("2".equals(text)) {
					userStateObj.setState("AGUARDANDO_DATA");
					return sendMessageWithKeyboard(chatId,
							"🔄 Ok! Informe novamente a data e horário (formato: dd/MM/aaaa HH:mm):");

				} else if ("3".equals(text)) {
					userState.remove(chatId);
					return sendMessageWithKeyboard(chatId,
							"❌ Agendamento cancelado. Caso queira tentar novamente, digite /agendar.");
				} else {
					return sendMessageWithKeyboard(chatId, "⚠️ Opção inválida. Por favor, escolha:\n\n" + "1️⃣ Confirmar\n"
							+ "2️⃣ Corrigir data\n" + "3️⃣ Cancelar");
				}
			} else if (text.equalsIgnoreCase("/servicos")) {
				return listarServicos(chatId);
			}else if(text.equalsIgnoreCase("/disponibilidade")) {
				return listaDatasDisponiveis(chatId);
				
			}else if(text.equalsIgnoreCase("/duvidas")) {
				return listarDuvidas(chatId);
				
			} else {
				return sendMessageWithKeyboard(chatId, "Olá! Para ver nossos serviços\ndigite /servicos \n\n"
						+ " Para agendar uma consulta,\ndigite /agendar"
						+ "\n\nPara ver datas e horários já agendados\ndigite /disponibilidade"
						+ "\n\nPara ver as perguntas frequentes\ndigite /duvidas");
			}
		}

		return null;
	}

	private SendMessage sendMessageWithKeyboard(Long chatId, String text) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(text);
		
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		keyboardMarkup.setResizeKeyboard(true);
		keyboardMarkup.setOneTimeKeyboard(true);
		
		List<KeyboardRow> keyboard = new ArrayList<>();
		
		KeyboardRow row1 = new KeyboardRow();
		row1.add("Agendar");
		row1.add("Serviços");
		keyboard.add(row1);
		
		KeyboardRow row2 = new KeyboardRow();
		row2.add("Disponibilidade");
		row2.add("Dúvidas");
		keyboard.add(row2);
		
		keyboardMarkup.setKeyboard(keyboard);
		message.setReplyMarkup(keyboardMarkup);
		
		return message;
	}
	
	private SendMessage sendMessageWothoutKeyboard(Long chatId, String text) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(text);
		
		ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
		keyboardRemove.setRemoveKeyboard(true);
		message.setReplyMarkup(keyboardRemove);
		
		return message;
		
	}
	
	private BotApiMethod<?> listarServicos(Long chatId) {
		StringBuilder response = new StringBuilder("📋 Nossos Serviços Disponíveis:\n\n");

		for (BotService botService : serviceRepository.findAll()) {
			response.append("🔹 ").append(botService.getId()).append(": ").append(botService.getNome()).append(" - ")
					.append(botService.getDescricao()).append(" - R$ ").append(botService.getPreco()).append("\n");
		}

		return sendMessageWithKeyboard(chatId, response.toString());

	}
	
	private BotApiMethod<?> listaDatasDisponiveis(Long chatId){
		StringBuilder response = new StringBuilder("📅 Dias e horários já agendados:\n\n");
		
		for(Appointment appointment : appointmentRepository.findAll()) {
			response.append("📌 ").append(appointment.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
				.append(" - ").append(appointment.getNomeCliente()).append("\n");
			
		}
		
		return sendMessageWithKeyboard(chatId, response.toString());
	}

	private BotApiMethod<?> listarDuvidas(Long chatId) {
		StringBuilder response = new StringBuilder("❓ Perguntas Frequentes ❓\n\n");

		response.append("1- Como faço para agendar um serviço?\n")
				.append("👉 Digite /agendar e siga as instruções para escolher um serviço e definir a data.\n\n");

		response.append("2- Quais serviços vocês oferecem?\n")
				.append("👉 Digite /servicos para visualizar a lista completa dos serviços disponíveis.\n\n");

		response.append("3- Posso cancelar um agendamento?\n")
				.append("👉 Sim! Durante o processo de confirmação, escolha a opção 3️⃣ para cancelar.\n\n");

		response.append("4- Como posso entrar em contato?\n").append(
				"👉 Para mais informações, entre em contato pelo nosso suporte no WhatsApp: (XX) XXXX-XXXX.\n\n");
		
		response.append("5- Como faço para corrigir um agendamento?\n"
				+ "👉 Assim que preencher todos os dados, será apresentado 3 opções, escolha a opção 2️⃣ para "
				+ "Corrigir data(Com esta opção é possível inserir novamente data e serviço desejado!)\n\n");
		response.append("6- Erro: 'Formato de data inválido. Por favor, use o formato dd/MM/yyyy HH:mm!' O que fazer?"
				+ "\nCertifique-se de que preencheu a data no seguinte formato: dia/mês/ano Horas:minutos"
				+ "\nEx:01/01/2025 09:30(Lembre-se de colocar as barras!\n\n)");
		response.append("-7 Erro: 'Este horário já está ocupado. Por favor, escolha outro horário!'\n"
				+ "Basta digitar '/disponibilidade' que aparecerá as datas e horários já preenchidos.\n"
				+ "Após isto escolha um outro horário!");

		return sendMessageWithKeyboard(chatId, response.toString());
	}
	
	protected void enviarNotificacaoTelegram(Long chatId, String mensagem) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(mensagem);
		
		try {
			execute(message);
		}catch(TelegramApiException e) {
			e.printStackTrace();
		}
	}
}