CREATE TABLE IF NOT EXISTS BOT_SERVICE (
    id BIGINT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao VARCHAR(255),
    preco DECIMAL(10,2)
);
