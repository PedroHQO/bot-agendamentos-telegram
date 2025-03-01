package com.pedrohqo.bot.telegram.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pedrohqo.bot.telegram.model.BotService;

public interface ServiceRepository extends JpaRepository<BotService, Long> {

}
