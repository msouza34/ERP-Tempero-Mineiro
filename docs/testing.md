# Testing

## Visao geral

O projeto possui testes automatizados cobrindo autenticacao, fluxo de pedido, cardapio publico e geracao de QR Code.

O conjunto atual combina:

- testes de integracao com contexto Spring Boot
- testes de integracao HTTP com MockMvc
- teste unitario para servico isolado

## Tecnologias de teste

- JUnit 5
- Spring Boot Test
- Spring Security Test
- MockMvc
- H2 em memoria

## Perfil de testes

O arquivo `backend/src/test/resources/application-test.yml` configura:

- banco H2 em memoria
- `ddl-auto=create-drop`
- seed desativado
- segredo JWT de teste

Essa configuracao reduz dependencias externas e permite execucao local dos testes sem PostgreSQL.

## Suites atuais

### `AuthControllerIntegrationTest`

Cobertura:

- cadastro de restaurante e admin
- login bem-sucedido
- rejeicao de senha fraca
- bloqueio temporario apos excesso de tentativas de login

### `PedidoFluxoIntegrationTest`

Cobertura:

- criacao de categoria
- criacao de estoque
- criacao de produto com ficha tecnica
- abertura de mesa
- criacao de pedido
- transicao para `PRONTO` e `ENTREGUE`
- registro de pagamento
- fechamento da conta
- baixa automatica de estoque

### `PublicMenuServiceIntegrationTest`

Cobertura:

- ordenacao do cardapio publico
- priorizacao de categorias como pratos, bebidas e sobremesas
- preservacao de imagem validada no menu publico

### `QrCodeServiceTest`

Cobertura:

- geracao de bytes PNG para QR Code

## Como rodar os testes

No diretorio `backend`:

```bash
mvn test
```

## Como validar apenas o build

Quando o objetivo for apenas empacotar a aplicacao:

```bash
mvn -DskipTests package
```

## O que os testes cobrem bem hoje

- autenticacao principal
- politica de senha no cadastro inicial
- protecao contra repeticao de tentativas de login
- fluxo central de venda ate fechamento
- integracao entre pedido, pagamento e estoque
- cardapio publico
- geracao de QR Code

## O que ainda pode evoluir

- testes dedicados para todos os controllers
- testes especificos para autorizacao por role
- testes de erro para fechamento com saldo pendente
- testes para SSE e eventos emitidos
- testes de repositorio para queries agregadas
- testes de carga para operacoes de cozinha e notificacoes

## Estrategia recomendada para evolucao

1. manter testes de integracao para fluxos criticos
2. adicionar testes de autorizacao por endpoint
3. cobrir cenarios negativos de negocio
4. incluir validacao de relatorios e dashboard
5. automatizar execucao em pipeline de CI quando houver esteira
