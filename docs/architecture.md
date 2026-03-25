# Arquitetura

## Estilo arquitetural

O projeto segue uma arquitetura em camadas sobre Spring Boot, com orientacao a dominio e exposicao via API REST. Na pratica, a organizacao atual combina um estilo MVC para a borda HTTP com uma camada de servicos para regras de negocio:

- `controller`: entrada HTTP, validacao de parametros e mapeamento de endpoints
- `service`: regras de negocio, autorizacao contextual e orquestracao
- `repository`: acesso a dados via Spring Data JPA
- `model`: entidades JPA e enums de dominio
- `dto`: contratos de entrada e saida da API
- `security`: autenticacao JWT e integracao com Spring Security
- `config`: configuracoes da aplicacao, Swagger, seed e seguranca
- `exception`: tratamento padronizado de erros

O resultado e um monolito modular, com responsabilidades separadas por pacote e com baixa complexidade operacional para um ERP de pequeno e medio porte.

## Principios observados no codigo

- sessao stateless com JWT
- segregacao multi-restaurante por `restaurante_id`
- regras de negocio centralizadas em servicos
- controllers finos e sem logica de negocio relevante
- DTOs explicitos para requests e responses
- persistencia relacional com JPA/Hibernate

## Fluxo geral da aplicacao

### Fluxo autenticado

1. O cliente chama um endpoint HTTP.
2. O `JwtAuthenticationFilter` extrai o token Bearer.
3. O `JwtService` valida o token.
4. O `SecurityContext` e preenchido com `CustomUserDetails`.
5. O controller delega a operacao para um service.
6. O service usa `AuthContextService` para obter usuario e restaurante correntes.
7. O service acessa repositories, aplica regras de negocio e devolve DTOs.
8. O `GlobalExceptionHandler` transforma falhas em respostas padronizadas.

### Fluxo publico do cardapio digital

1. O cliente acessa `GET /public/{restaurantSlug}/menu?mesaToken=...`.
2. O `PublicController` delega ao `PublicMenuService`.
3. O service valida mesa e restaurante pelo `mesaToken`.
4. Os produtos disponiveis sao carregados e agrupados por categoria.
5. O cliente pode enviar um pedido em `POST /public/{restaurantSlug}/orders`.
6. O pedido e criado com origem `CARDAPIO_DIGITAL` e entra na fila normal da cozinha.

## Diagrama textual

```text
Cliente HTTP
    |
    v
Spring MVC Controller
    |
    v
Spring Security Filter Chain
    |
    +--> JwtAuthenticationFilter
    |        |
    |        v
    |    JwtService
    |
    v
Service Layer
    |
    +--> AuthContextService
    +--> Regras de negocio
    +--> NotificationService
    |
    v
Repository Layer
    |
    v
PostgreSQL
```

## Diagrama textual de modulos

```text
Autenticacao
    |- AuthController
    |- AuthService
    |- JwtService
    |- LoginAttemptService

Operacao interna
    |- MesaController / MesaService
    |- CategoriaController / CategoriaService
    |- ProdutoController / ProdutoService
    |- PedidoController / PedidoService
    |- CozinhaController / NotificationService
    |- CaixaController / CaixaService
    |- EstoqueController / EstoqueService
    |- RelatorioController / ReportService
    |- UserController / UserService

Canal publico
    |- PublicController
    |- PublicMenuService
    |- QrCodeService
```

## Multi-tenancy

O sistema foi modelado para operar mais de um restaurante. Essa separacao nao usa schemas distintos nem bancos separados. O isolamento e feito na aplicacao, principalmente por:

- relacionamento entre entidades operacionais e `Restaurante`
- claims do JWT com `restaurantId`
- `AuthContextService` como fonte do contexto autenticado
- consultas filtradas por `restauranteId`

Esse desenho simplifica a infraestrutura, mas exige disciplina nas consultas para impedir vazamento entre restaurantes.

## Fluxos de negocio relevantes

### Pedido ate fechamento

1. A mesa recebe um pedido e passa para `OCUPADA`.
2. O pedido nasce com status `EM_PREPARO`.
3. A cozinha ou operacao atualiza o status para `PRONTO`.
4. A operacao atualiza o status para `ENTREGUE`.
5. O caixa registra pagamentos.
6. Ao fechar a conta, o sistema:
   - impede fechamento com pedidos ainda em preparo ou prontos
   - impede fechamento com saldo pendente
   - consome estoque com base na ficha tecnica
   - marca pedidos como `FECHADO`
   - libera a mesa

### Cadastro de produto

1. O produto referencia uma categoria do mesmo restaurante.
2. A imagem e validada por `ProductImageService`.
3. A receita tecnica pode associar o produto a itens de estoque.
4. Na venda concluida, a receita e usada para baixar insumos.

## Tempo real

O projeto usa Server-Sent Events em vez de WebSocket. Os canais expostos sao:

- `GET /cozinha/stream`
- `GET /notifications/stream`

O `NotificationService` envia:

- evento inicial `connected`
- heartbeat periodico
- eventos de dominio como `PEDIDO_ENVIADO`, `PEDIDO_PRONTO`, `PEDIDO_ENTREGUE`, `PAGAMENTO_REGISTRADO` e `CONTA_FECHADA`

## Observacoes arquiteturais

- O projeto nao esta dividido em microservicos.
- Nao ha camada de mensageria externa.
- Nao ha migracao versionada de banco com Flyway ou Liquibase no estado atual.
- O Swagger funciona como principal interface de exploracao funcional enquanto nao existe frontend ativo.
