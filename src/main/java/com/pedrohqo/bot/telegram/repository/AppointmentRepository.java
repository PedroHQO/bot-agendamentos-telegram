package com.pedrohqo.bot.telegram.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pedrohqo.bot.telegram.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
	boolean existsByDateTime(LocalDateTime dateTime);
	
	List<Appointment> findByDateTime(LocalDateTime dateTime);
	List<Appointment> findByBotServiceId(Long serviceId);
	List<Appointment> findByNomeClienteContainingIgnoreCase(String nomeCliente);

}
