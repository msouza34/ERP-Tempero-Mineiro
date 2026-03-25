# Banco de dados

## Visao geral

O sistema usa PostgreSQL como banco principal. Em testes automatizados, o perfil `test` utiliza H2 em memoria no modo compativel com PostgreSQL.

Configuracao atual:

- desenvolvimento local sem Docker: `jdbc:postgresql://localhost:5432/tempero_mineiro_erp`
- Docker Compose: `jdbc:postgresql://postgres:5432/tempero_mineiro`
- testes: `jdbc:h2:mem:temperomineiro`

## Estrategia de schema

O projeto usa `spring.jpa.hibernate.ddl-auto=update` no ambiente principal. Isso permite que o Hibernate crie e ajuste tabelas automaticamente conforme as entidades.

No perfil de testes:

- `ddl-auto=create-drop`

Observacao importante:

- o projeto nao possui, neste momento, migracoes versionadas com Flyway ou Liquibase

## Entidades principais

### BaseEntity

Todas as entidades operacionais herdam:

- `id`
- `createdAt`
- `updatedAt`

Esses campos sao preenchidos via `@PrePersist` e `@PreUpdate`.

## Tabelas

### `restaurantes`

| Campo        | Tipo logico | Observacao                         |
| ------------ | ----------- | ---------------------------------- |
| `id`         | Long        | PK                                 |
| `nome`       | String      | nome comercial                     |
| `slug`       | String      | unico, usado em endpoints publicos |
| `ativo`      | boolean     | status do restaurante              |
| `created_at` | timestamp   | herdado                            |
| `updated_at` | timestamp   | herdado                            |

### `roles`

| Campo         | Tipo logico | Observacao          |
| ------------- | ----------- | ------------------- |
| `id`          | Long        | PK                  |
| `name`        | enum        | unico               |
| `description` | String      | descricao do perfil |
| `created_at`  | timestamp   | herdado             |
| `updated_at`  | timestamp   | herdado             |

### `users`

| Campo            | Tipo logico | Observacao                |
| ---------------- | ----------- | ------------------------- |
| `id`             | Long        | PK                        |
| `restaurante_id` | FK          | referencia `restaurantes` |
| `nome`           | String      | nome do usuario           |
| `email`          | String      | unico                     |
| `password`       | String      | hash BCrypt               |
| `ativo`          | boolean     | status do usuario         |
| `created_at`     | timestamp   | herdado                   |
| `updated_at`     | timestamp   | herdado                   |

Relacionamento adicional:

- muitos para muitos com `roles` via `user_roles`

### `user_roles`

| Campo     | Tipo logico | Observacao         |
| --------- | ----------- | ------------------ |
| `user_id` | FK          | referencia `users` |
| `role_id` | FK          | referencia `roles` |

### `mesas`

| Campo            | Tipo logico | Observacao                       |
| ---------------- | ----------- | -------------------------------- |
| `id`             | Long        | PK                               |
| `restaurante_id` | FK          | referencia `restaurantes`        |
| `nome`           | String      | nome exibido                     |
| `capacidade`     | Integer     | lugares                          |
| `status`         | enum        | `LIVRE`, `OCUPADA`, `RESERVADA`  |
| `public_token`   | String      | unico, usado no cardapio publico |
| `aberta_em`      | datetime    | inicio da ocupacao               |
| `ativa`          | boolean     | disponibilidade operacional      |
| `created_at`     | timestamp   | herdado                          |
| `updated_at`     | timestamp   | herdado                          |

### `categorias`

| Campo            | Tipo logico | Observacao                |
| ---------------- | ----------- | ------------------------- |
| `id`             | Long        | PK                        |
| `restaurante_id` | FK          | referencia `restaurantes` |
| `nome`           | String      | nome da categoria         |
| `descricao`      | String      | ate 500 caracteres        |
| `ordem_exibicao` | Integer     | ordenacao do cardapio     |
| `ativa`          | boolean     | exclusao logica           |
| `created_at`     | timestamp   | herdado                   |
| `updated_at`     | timestamp   | herdado                   |

### `produtos`

| Campo            | Tipo logico   | Observacao                       |
| ---------------- | ------------- | -------------------------------- |
| `id`             | Long          | PK                               |
| `restaurante_id` | FK            | referencia `restaurantes`        |
| `categoria_id`   | FK            | referencia `categorias`          |
| `nome`           | String        | nome do produto                  |
| `descricao`      | String        | ate 1200 caracteres              |
| `preco`          | decimal(12,2) | valor de venda                   |
| `imagem_url`     | text          | URL externa ou data URL validada |
| `disponivel`     | boolean       | exclusao logica operacional      |
| `sku`            | String        | unico                            |
| `created_at`     | timestamp     | herdado                          |
| `updated_at`     | timestamp     | herdado                          |

### `receitas_produto`

| Campo                  | Tipo logico   | Observacao                  |
| ---------------------- | ------------- | --------------------------- |
| `id`                   | Long          | PK                          |
| `produto_id`           | FK            | referencia `produtos`       |
| `estoque_id`           | FK            | referencia `estoques`       |
| `quantidade_consumida` | decimal(12,3) | consumo por unidade vendida |
| `created_at`           | timestamp     | herdado                     |
| `updated_at`           | timestamp     | herdado                     |

### `estoques`

| Campo               | Tipo logico   | Observacao                      |
| ------------------- | ------------- | ------------------------------- |
| `id`                | Long          | PK                              |
| `restaurante_id`    | FK            | referencia `restaurantes`       |
| `nome`              | String        | insumo                          |
| `unidade_medida`    | enum          | `UNIDADE`, `KG`, `G`, `L`, `ML` |
| `quantidade_atual`  | decimal(12,3) | saldo                           |
| `quantidade_minima` | decimal(12,3) | limite de alerta                |
| `custo_unitario`    | decimal(12,2) | custo medio informado           |
| `ativo`             | boolean       | status do item                  |
| `created_at`        | timestamp     | herdado                         |
| `updated_at`        | timestamp     | herdado                         |

### `movimentacoes_estoque`

| Campo            | Tipo logico   | Observacao                   |
| ---------------- | ------------- | ---------------------------- |
| `id`             | Long          | PK                           |
| `restaurante_id` | FK            | referencia `restaurantes`    |
| `estoque_id`     | FK            | referencia `estoques`        |
| `tipo`           | enum          | `ENTRADA`, `SAIDA`, `AJUSTE` |
| `quantidade`     | decimal(12,3) | volume movimentado           |
| `motivo`         | String        | motivo operacional           |
| `referencia`     | String        | contexto adicional           |
| `created_at`     | timestamp     | herdado                      |
| `updated_at`     | timestamp     | herdado                      |

### `pedidos`

| Campo            | Tipo logico   | Observacao                                                 |
| ---------------- | ------------- | ---------------------------------------------------------- |
| `id`             | Long          | PK                                                         |
| `restaurante_id` | FK            | referencia `restaurantes`                                  |
| `mesa_id`        | FK            | referencia `mesas`                                         |
| `usuario_id`     | FK            | referencia `users`, opcional para pedido publico           |
| `status`         | enum          | `EM_PREPARO`, `PRONTO`, `ENTREGUE`, `FECHADO`, `CANCELADO` |
| `origem`         | enum          | `SALAO` ou `CARDAPIO_DIGITAL`                              |
| `observacoes`    | String        | ate 1200 caracteres                                        |
| `subtotal`       | decimal(12,2) | soma dos itens                                             |
| `desconto`       | decimal(12,2) | desconto aplicado                                          |
| `taxa_servico`   | decimal(12,2) | valor adicional                                            |
| `total`          | decimal(12,2) | subtotal + taxa - desconto                                 |
| `aberto_em`      | datetime      | abertura                                                   |
| `pronto_em`      | datetime      | preparo concluido                                          |
| `entregue_em`    | datetime      | entrega concluida                                          |
| `fechado_em`     | datetime      | encerramento financeiro                                    |
| `created_at`     | timestamp     | herdado                                                    |
| `updated_at`     | timestamp     | herdado                                                    |

### `itens_pedido`

| Campo            | Tipo logico   | Observacao                          |
| ---------------- | ------------- | ----------------------------------- |
| `id`             | Long          | PK                                  |
| `pedido_id`      | FK            | referencia `pedidos`                |
| `produto_id`     | FK            | referencia `produtos`               |
| `quantidade`     | Integer       | quantidade vendida                  |
| `preco_unitario` | decimal(12,2) | preco congelado no momento da venda |
| `total`          | decimal(12,2) | subtotal do item                    |
| `observacoes`    | String        | observacoes do item                 |
| `created_at`     | timestamp     | herdado                             |
| `updated_at`     | timestamp     | herdado                             |

### `pagamentos`

| Campo            | Tipo logico   | Observacao                           |
| ---------------- | ------------- | ------------------------------------ |
| `id`             | Long          | PK                                   |
| `restaurante_id` | FK            | referencia `restaurantes`            |
| `mesa_id`        | FK            | referencia `mesas`                   |
| `metodo`         | enum          | `DINHEIRO`, `CARTAO`, `PIX`          |
| `status`         | enum          | `PENDENTE`, `CONCLUIDO`, `CANCELADO` |
| `valor`          | decimal(12,2) | valor pago                           |
| `observacoes`    | String        | observacoes do pagamento             |
| `pago_em`        | datetime      | momento do pagamento                 |
| `created_at`     | timestamp     | herdado                              |
| `updated_at`     | timestamp     | herdado                              |

## Relacionamentos

```text
Restaurante 1 --- N User
Restaurante 1 --- N Mesa
Restaurante 1 --- N Categoria
Restaurante 1 --- N Produto
Restaurante 1 --- N Estoque
Restaurante 1 --- N MovimentacaoEstoque
Restaurante 1 --- N Pedido
Restaurante 1 --- N Pagamento

User N --- N Role
Categoria 1 --- N Produto
Produto 1 --- N ReceitaProduto
Estoque 1 --- N ReceitaProduto
Mesa 1 --- N Pedido
Mesa 1 --- N Pagamento
Pedido 1 --- N ItemPedido
Produto 1 --- N ItemPedido
```

## Regras de dados importantes

- `slug` de restaurante e unico e usado nas rotas publicas
- `publicToken` da mesa e unico e identifica a mesa no cardapio digital
- `sku` do produto e unico
- pedidos publicos podem ter `usuario_id` nulo
- categorias e produtos usam desativacao logica em vez de remocao fisica
- a baixa de estoque ocorre no fechamento da conta, com base em `receitas_produto`

## Consultas e agregacoes relevantes

O modelo de dados tambem atende consultas operacionais importantes:

- dashboard do dia e do mes com base em pedidos `FECHADO`
- ranking de produtos mais vendidos
- busca de estoque baixo com `quantidade_atual <= quantidade_minima`
- resumo de caixa por mesa com soma de pagamentos

## Seed de demonstracao

Com `APP_SEED_ENABLED=true`, a aplicacao cria:

- um restaurante demo
- roles padrao
- usuarios iniciais
- 10 mesas
- categorias demo
- itens de estoque demo
- produtos demo
- fichas tecnicas demo
