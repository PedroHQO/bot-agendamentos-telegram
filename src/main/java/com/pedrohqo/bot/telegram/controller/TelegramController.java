package com.pedrohqo.bot.telegram.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class TelegramController {
	
	@PostMapping("/webhook")
	public void handleMessage(@RequestBody Update update) {
		//Processar mensagem recebida
		System.out.println("Mensagem recebida: " + update);
	}
	
}
