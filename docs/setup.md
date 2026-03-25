# Setup

## Pre-requisitos

Para executar o projeto localmente, voce precisa de:

- Java 17
- Maven 3.9+ para execucao sem Docker
- Docker e Docker Compose para execucao containerizada
- PostgreSQL, caso nao use Docker para o banco

## Variaveis de ambiente

As configuracoes principais estao em `backend/src/main/resources/application.yml` e podem ser sobrescritas por variaveis de ambiente.

| Variavel                     | Obrigatoria                            | Exemplo                                            | Finalidade                     |
| ---------------------------- | -------------------------------------- | -------------------------------------------------- | ------------------------------ |
| `SPRING_DATASOURCE_URL`      | Sim                                    | `jdbc:postgresql://localhost:5432/tempero_mineiro` | URL do banco                   |
| `SPRING_DATASOURCE_USERNAME` | Sim                                    | `postgres`                                         | Usuario do banco               |
| `SPRING_DATASOURCE_PASSWORD` | Sim                                    | `postgres`                                         | Senha do banco                 |
| `APP_JWT_SECRET`             | Sim                                    | `chave-com-32-bytes-ou-mais`                       | Assinatura do JWT              |
| `APP_JWT_EXPIRATION_MS`      | Nao                                    | `86400000`                                         | Validade do token              |
| `APP_PUBLIC_BASE_URL`        | Sim em uso publico                     | `http://localhost:8080`                            | Base usada em links e QR Codes |
| `APP_CORS_ALLOWED_ORIGINS`   | Sim em ambientes com clientes externos | `http://localhost,http://localhost:8080`           | Origens permitidas             |
| `APP_SEED_ENABLED`           | Nao                                    | `true`                                             | Habilita seed demo             |
| `SERVER_PORT`                | Nao                                    | `8080`                                             | Porta HTTP da API              |

## Arquivos de apoio

- `.env.example`: exemplo de configuracao
- `.env`: configuracao local atual
- `docker-compose.yml`: orquestracao de backend e PostgreSQL

## Opcao 1: rodar com Docker

### Passo a passo

1. Na raiz do repositorio, confirme as variaveis em `.env`.
2. Rode:

```bash
docker compose up -d --build
```

3. Aguarde a inicializacao dos containers.
4. Valide a saude da API:

```bash
curl http://localhost:8080/actuator/health
```

5. Abra o Swagger:

```text
http://localhost:8080/swagger-ui.html
```

### Portas usadas no Compose

- `5432`: PostgreSQL
- `8080`: backend

### Comandos uteis

Parar os containers:

```bash
docker compose down
```

Parar e remover orfaos:

```bash
docker compose down --remove-orphans
```

Subir novamente com rebuild:

```bash
docker compose up -d --build
```

## Opcao 2: rodar sem Docker

### Banco de dados

Crie ou disponibilize um PostgreSQL local. O `application.yml` usa por padrao:

```text
jdbc:postgresql://localhost:5432/tempero_mineiro_erp
```

Se preferir outro nome de banco, sobrescreva `SPRING_DATASOURCE_URL`.

### Execucao

No diretorio `backend`:

```bash
mvn spring-boot:run
```

## Primeiro acesso

Com a aplicacao no ar:

1. Abra `http://localhost:8080/swagger-ui.html`
2. Execute `POST /auth/login`
3. Use as credenciais demo, se o seed estiver ativo:

```json
{
  "email": "admin@temperomineiro.com",
  "password": "123456"
}
```

4. Copie o campo `token`
5. Clique em `Authorize`
6. Informe:

```text
Bearer SEU_TOKEN
```

## Seed de dados

Quando `APP_SEED_ENABLED=true`, o `DataSeeder` cria:

- restaurante demo
- roles padrao
- usuarios demo
- mesas demo
- categorias demo
- produtos demo
- itens de estoque demo

## Observacoes importantes de configuracao

- `APP_PUBLIC_BASE_URL` define a URL usada para montar o link de cardapio publico e o QR Code das mesas
- o CORS padrao permite `http://localhost` e `http://localhost:8080`
- o projeto usa `ddl-auto=update`, entao o schema e gerado/atualizado automaticamente
- em testes automatizados, o seed e desativado

## Troubleshooting basico

### Swagger nao abre

Verifique:

- se o backend esta rodando na porta `8080`
- se `GET /actuator/health` responde `UP`
- se voce esta acessando `http://localhost:8080/swagger-ui.html`

### Login demo nao funciona

Verifique:

- se `APP_SEED_ENABLED=true`
- se o banco nao contem dados anteriores conflitantes
- se o ambiente atual esta apontando para o banco correto

### QR Code aponta para URL errada

Revise:

- `APP_PUBLIC_BASE_URL`

Em ambiente publico, essa variavel deve usar o dominio ou IP publico real da API.
