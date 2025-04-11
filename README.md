# Bot de Agendamento Telegram - AgendaAí

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Telegram Bot API](https://img.shields.io/badge/Telegram%20Bot%20API-6.9.2-lightgrey)

Um bot de agendamento para Telegram desenvolvido em Java com Spring Boot que permite aos usuários agendar serviços, consultar horários já agendados e obter informações sobre serviços oferecidos.

## 📌 Funcionalidades Principais

- **Agendamento de serviços** com confirmação em tempo real
- **Listagem de serviços** disponíveis com preços e descrições
- **Consulta de disponibilidade** de horários
- **FAQ automático** para dúvidas frequentes
- **API REST** para gerenciamento administrativo
- **Notificações** via Telegram para confirmações e cancelamentos

## 🛠️ Tecnologias Utilizadas

- **Backend**: Java 17, Spring Boot 3.1.5
- **Banco de Dados**: PostgreSQL
- **API Telegram**: TelegramBots 6.9.2
- **Documentação**: OpenAPI 3 (Swagger)
- **Build**: Maven

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- PostgreSQL 15+
- Conta de bot Telegram (obter token com @BotFather)

### Configuração
1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/bot-agendamento-telegram.git
   
2. Configure o banco de dados no arquivo application.properties:
    ```bash
    spring.datasource.url=jdbc:postgresql://localhost:5432/database_bot
    spring.datasource.username=seu_usuario
    spring.datasource.password=sua_senha
    
3. Configure o token do bot Telegram:
   ```bash
   // Em TelegramController.java
    @Override
    public String getBotToken() {
      return "SEU_TOKEN_AQUI";
    }
4. Execute a aplicação:
   ```bash
   mvn spring-boot:run
   
## 📚 Documentação da API
A API administrativa está documentada com Swagger/OpenAPI. Acesse:
    ```bash
     http://localhost:8080/swagger-ui.html

ndpoints disponíveis:

* GET / - Lista todos agendamentos
* GET /filtro - Filtra agendamentos por data, nome ou serviço
* PUT /{id}/confirmar - Confirma um agendamento
* PUT /{id}/cancelar - Cancela um agendamento

## 🤖 Fluxo do Bot
1. Menu Principal:
    ```bash
    /start → Mostra opções: Agendar, Serviços, Disponibilidade, Dúvidas
2. Agendamento:
     ```bash
     Agendar → Informe nome → Informe data (dd/MM/yyyy HH:mm) → Escolha serviço → Confirmação
3. Serviços:
      ```bash
      Serviços → Lista todos serviços com IDs, descrições e preços
4. Disponibilidade:
      ```bash
      Disponibilidade → Mostra horários já agendados

## 🧩 Estrutura do Código
### Principais classes:

TelegramController: Lógica principal do bot

AgendamentoController: API REST para gerenciamento

Appointment: Entidade de agendamento

BotService: Entidade de serviços

UserState: Gerencia estado da conversa

## 📊 Modelo de Dados
    ```bash
    erDiagram
    APPOINTMENT ||--o{ BOT_SERVICE : has
    APPOINTMENT {
        Long id PK
        String nomeCliente
        LocalDateTime dateTime
        boolean confirmado
        Long chatId
    }
    BOT_SERVICE {
        Long id PK
        String nome
        BigDecimal preco
        String descricao
    }

##  📝 Licença
Este projeto está licenciado sob a MIT License - veja o arquivo LICENSE para detalhes.

## ✉️ Contato
Pedro Henrique - [GitHub](https://github.com/PedroHQO) | [LinkedIn](https://www.linkedin.com/in/pedro-henrique-a07564207/)
