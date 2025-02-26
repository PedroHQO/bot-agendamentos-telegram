package com.pedrohqo.bot.telegram.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pedrohqo.bot.telegram.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

}
