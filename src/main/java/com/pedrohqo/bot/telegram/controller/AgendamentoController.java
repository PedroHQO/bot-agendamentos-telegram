package com.pedrohqo.bot.telegram.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.pedrohqo.bot.telegram.exception.FiltroAgendamentoException;
import com.pedrohqo.bot.telegram.model.Appointment;
import com.pedrohqo.bot.telegram.model.UserState;
import com.pedrohqo.bot.telegram.repository.AppointmentRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
public class AgendamentoController {

	@Autowired
	private AppointmentRepository appointmentRepository;
	
	@Autowired
	private TelegramController telegramController;
	
	@GetMapping
	@Operation(summary = "Listar agendamentos", description = "Retorna uma lista com todos os agendamentos")
	@ApiResponse(responseCode = "200", description = "Agendamentos encontrados")
	public List<Appointment> listarAgendamentos(){
		return appointmentRepository.findAll();
	}
	
	@GetMapping("/filtro")
	@Operation(summary = "Filtrar agendamentos", description = "Retorna uma lista de agendamentos filtrados por data, "
			+ "nome do cliente ou serviço.")
	@ApiResponse(responseCode = "200", description = "Agendamentos encontrados")
	public List<Appointment> filtrarAgendamentos(
		@Parameter(description = "Data e hora do agendamento", example = "2025-10-05T10:09:00")
		@RequestParam(required = false) LocalDateTime dateTime,
		
		@Parameter(description = "Nome cliente", example = "Pedro Henrique")
		@RequestParam(required = false) String nomeCliente,
		
		@Parameter(description = "ID do serviço", example = "1")
		@RequestParam(required = false) Long botService){
		
		try {
			if(dateTime != null) {
				return appointmentRepository.findByDateTime(dateTime);
			}else if(botService != null) {
				return appointmentRepository.findByBotServiceId(botService);
			}else if(nomeCliente != null) {
				return appointmentRepository.findByNomeClienteContainingIgnoreCase(nomeCliente);
			}else {
				return appointmentRepository.findAll();
			}
		}catch(Exception e) {
		throw new FiltroAgendamentoException("Erro ao filtrar agendamento, verifique informações digitadas no filtro "
				+ "e tente novamente!" + e.getMessage());
		}
	}
	
	
	@PutMapping("/{id}/confirmar")
	@Operation(summary = "Confirmar agendamento", description = "Disponibiliza para o prestador confirmar o "
			+ "agendamento ou cancelar, a escolha dispara uma mensagem para o cliente")
	@ApiResponse(responseCode = "200", description = "Agendamento confirmado com sucesso")
	public ResponseEntity<String> confirmarAgendamento(@PathVariable Long id){
		try {
			Appointment appointment = appointmentRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Ops. Agendamento não encontrado com o ID: " + id));
			
			appointment.setConfirmado(true);
			appointmentRepository.save(appointment);
			
			if(appointment.getChatId() != null) {
				telegramController.enviarNotificacaoTelegram(appointment.getChatId(), "Olá " + appointment.getNomeCliente() + 
						", seu agendamento foi confirmado! ✅");
			}else {
				System.out.println("ChatId não pode ser nulo para o agendamento com ID: " + id);
			}
			
			return ResponseEntity.ok("Agendamento confirmado com sucesso!");
		}catch(Exception e) {
			return ResponseEntity.status(500).body("Erro ao confirmar agendamento: " + e.getMessage());
		}
	}
	
	@PutMapping("/{id}/cancelar")
	@Operation(summary = "Cancelar agendamento", description = "Cancela um agendamento pelo ID.")
    @ApiResponse(responseCode = "200", description = "Agendamento cancelado com sucesso")
	public ResponseEntity<String> cancelarAgendamento(@PathVariable Long id){
		try {
			Appointment appointment = appointmentRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Ops. Agendamento não encontrado com o ID: " + id));
			
			appointmentRepository.delete(appointment);
			
			if(appointment.getChatId() != null) {
				telegramController.enviarNotificacaoTelegram(appointment.getChatId(), "Olá " + appointment.getNomeCliente() + 
						". Lamento seu agendamento foi cancelado! ❌");
			}else {
				System.out.println("ChatId não pode ser nulo para o agendamento com ID: " + id);
			}
			
			return ResponseEntity.ok("Agendamento cancelado com sucesso!");
		}catch(Exception e) {
			return ResponseEntity.status(500).body("Erro ao cancelar agendamento: " + e.getMessage());
		}
		
	}
	
	
}
