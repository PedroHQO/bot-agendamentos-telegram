package com.pedrohqo.bot.telegram.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SpringDocConfig {
	
	@Bean
	public OpenAPI refactorOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("API GERENCIAMENTO DE AGENDAMENTO")
						.description("API para gerenciamento de agendamentos de serviços feitos via telegram")
						.version("1.0.0"));
	}

}
