# Decisoes Tecnicas

## Visao geral

Este documento registra as principais decisoes tecnicas observadas no projeto atual, com os motivos e os trade-offs envolvidos.

## Decisoes

### 1. Spring Boot como base do backend

Decisao:

- usar Spring Boot 3.3.5 com Java 17

Motivos:

- ecossistema maduro para APIs REST
- integracao nativa com seguranca, validacao, JPA e actuator
- acelera desenvolvimento de um ERP operacional

Trade-offs:

- stack mais opinativa
- maior consumo de memoria que alternativas mais enxutas

### 2. Monolito modular em vez de microservicos

Decisao:

- manter todos os modulos operacionais no mesmo backend

Motivos:

- menor custo operacional
- transacoes de negocio ficam mais simples
- dominio ainda cabe bem em uma unica aplicacao

Trade-offs:

- menor isolamento entre modulos
- escalabilidade continua acoplada ao deploy do monolito

### 3. Arquitetura em camadas

Decisao:

- separar `controller`, `service`, `repository`, `model`, `dto`, `security` e `config`

Motivos:

- deixa responsabilidades claras
- facilita manutencao e onboarding
- melhora testabilidade das regras de negocio

Trade-offs:

- mais classes e mapeamentos
- risco de verbosidade em operacoes simples

### 4. JWT stateless para autenticacao

Decisao:

- usar token Bearer sem sessao no servidor

Motivos:

- combina bem com API REST
- simplifica escalabilidade horizontal
- remove dependencia de sessao compartilhada

Trade-offs:

- logout e revogacao nao sao imediatos sem mecanismo adicional
- exige maior cuidado com expiracao e segredo

### 5. BCrypt para persistencia de senha

Decisao:

- armazenar senhas com `BCryptPasswordEncoder(12)`

Motivos:

- protecao adequada para credenciais
- implementacao consolidada no ecossistema Spring

Trade-offs:

- custo computacional maior no login
- precisa balancear seguranca e desempenho

### 6. PostgreSQL como banco principal

Decisao:

- usar PostgreSQL em ambiente principal e Docker Compose

Motivos:

- bom suporte a dados relacionais
- compatibilidade forte com JPA/Hibernate
- operacao simples em VPS e containers

Trade-offs:

- dependencia de banco externo fora do perfil de testes

### 7. H2 para testes

Decisao:

- usar H2 em memoria no perfil `test`

Motivos:

- rapidez de execucao
- independencia de infraestrutura externa

Trade-offs:

- diferencas sutis podem existir entre H2 e PostgreSQL real

### 8. Multi-tenancy por `restaurante_id`

Decisao:

- isolar dados por restaurante na propria modelagem de dominio

Motivos:

- simplicidade operacional
- sem necessidade de multiplos bancos ou schemas
- boa aderencia ao contexto atual do produto

Trade-offs:

- requer disciplina em todas as consultas
- qualquer falha de filtro pode gerar vazamento de dados entre tenants

### 9. Swagger como interface principal de operacao tecnica

Decisao:

- manter OpenAPI e Swagger UI como principal porta de exploracao funcional

Motivos:

- o projeto esta sem frontend ativo
- facilita QA, homologacao e suporte tecnico

Trade-offs:

- experiencia operacional limitada para usuarios finais
- dependente de conhecimento tecnico minimo

### 10. SSE para notificacoes em tempo real

Decisao:

- usar Server-Sent Events para cozinha e notificacoes

Motivos:

- menor complexidade que WebSocket para fluxo unidirecional
- adequado para eventos simples de operacao

Trade-offs:

- canal apenas servidor -> cliente
- menos flexivel que WebSocket para interacoes bidirecionais

### 11. Desativacao logica para categorias e produtos

Decisao:

- `DELETE` de categoria marca `ativa=false`
- `DELETE` de produto marca `disponivel=false`

Motivos:

- preserva historico operacional
- evita quebra de referencias em pedidos antigos

Trade-offs:

- cresce o volume de registros inativos
- consultas precisam respeitar filtros corretos

### 12. Baixa de estoque no fechamento da conta

Decisao:

- consumir estoque quando a conta e fechada

Motivos:

- o consumo financeiro passa a refletir a venda concluida
- evita baixa prematura de itens em pedidos cancelados

Trade-offs:

- o saldo fisico pode ficar defasado enquanto a mesa ainda esta aberta

### 13. DDL automatico do Hibernate

Decisao:

- usar `spring.jpa.hibernate.ddl-auto=update`

Motivos:

- velocidade de desenvolvimento
- onboarding simplificado
- menos atrito para ambientes demo e locais

Trade-offs:

- menor controle fino sobre evolucao do schema
- nao substitui migracoes versionadas em producao

### 14. Seed de demonstracao embutido

Decisao:

- inicializar dados demo via `DataSeeder`

Motivos:

- facilita validacao rapida da API
- reduz tempo de setup inicial

Trade-offs:

- nao deve permanecer ativo em producao
- a senha demo nao segue a politica forte do cadastro normal

### 15. Protecao focada no login antes de rate limiting global

Decisao:

- implementar bloqueio temporario de tentativas de login

Motivos:

- trata primeiro o vetor mais obvio de brute force
- baixo custo de implementacao

Trade-offs:

- ainda nao existe rate limiting abrangente para toda a API

## Itens que merecem reavaliacao futura

- adocao de migracoes com Flyway ou Liquibase
- rate limiting global
- trilha de auditoria
- refresh token ou estrategia de revogacao
- pipeline CI/CD
- frontend operacional dedicado
