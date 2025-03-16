INSERT INTO bot_service (id, nome, descricao, preco) VALUES (1, 'Serviço A', 'Descrição do Serviço A', 100.0)
ON CONFLICT (id) DO NOTHING;
INSERT INTO bot_service (id, nome, descricao, preco) VALUES (2, 'Serviço B', 'Descrição do Serviço B', 150.0)
ON CONFLICT (id) DO NOTHING;;
