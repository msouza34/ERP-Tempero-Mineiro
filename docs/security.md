# Seguranca

## Visao geral

O projeto aplica seguranca em varias camadas: autenticacao stateless com JWT, autorizacao por perfil, protecao de credenciais com BCrypt, validacao de entrada, isolamento por restaurante, cabecalhos de seguranca e bloqueio temporario de tentativas de login.

## Controles implementados

### Spring Security

O acesso HTTP e centralizado no `SecurityConfig` com as seguintes caracteristicas:

- sessao stateless
- autenticacao por filtro JWT
- `@EnableMethodSecurity` para protecao por endpoint
- liberacao apenas dos endpoints publicos e da documentacao
- exigencia de autenticacao para as demais rotas

Rotas publicas atualmente permitidas:

- `/auth/**`
- `/public/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/actuator/health`

## JWT

### Como funciona

- o token e emitido por `JwtService`
- a assinatura usa chave simetrica HMAC
- a validade e configurada por `APP_JWT_EXPIRATION_MS`
- o filtro `JwtAuthenticationFilter` valida o token em cada request autenticado

### Claims atualmente emitidas

- `sub`: e-mail do usuario
- `uid`: id do usuario
- `restaurantId`: id do restaurante
- `name`: nome do usuario
- `roles`: autoridades do usuario

### Requisitos da chave

O projeto exige que `APP_JWT_SECRET` tenha pelo menos 32 bytes. Se isso nao for atendido, a aplicacao falha na inicializacao.

## Autorizacao

O controle de acesso e baseado em roles:

- `ADMIN`
- `GERENTE`
- `GARCOM`
- `COZINHA`
- `CAIXA`

A autorizacao e aplicada principalmente com `@PreAuthorize` nos controllers. Isso reduz o risco de exposicao acidental de operacoes sensiveis.

## Protecao de senhas

### BCrypt

As senhas sao armazenadas com `BCryptPasswordEncoder(12)`.

Impactos dessa escolha:

- nao ha persistencia de senha em texto puro
- o custo `12` aumenta o trabalho computacional de ataques offline
- o projeto reutiliza o encoder tanto no seed quanto no cadastro de usuarios

### Politica de senha

O `CredentialPolicyService` exige:

- minimo de 8 caracteres
- maximo de 72 caracteres
- ao menos uma letra maiuscula
- ao menos uma letra minuscula
- ao menos um numero
- ao menos um simbolo

Observacao importante:

- essa politica e aplicada no cadastro inicial do restaurante
- o seed de demonstracao usa a senha `123456`, que nao atende a politica forte e deve ser tratado apenas como ambiente de demo

## Rate limiting e protecao contra brute force

O projeto nao possui rate limiting global por IP, rota ou tenant.

O mecanismo atualmente implementado e focado em autenticacao:

- `LoginAttemptService`
- bloqueio apos `5` tentativas falhas
- duracao do bloqueio: `15` minutos
- chave de controle: e-mail normalizado

Isso ajuda a mitigar brute force no login, mas nao substitui um rate limiter completo para producao.

### Recomendacao para producao

Adicionar rate limiting no proxy reverso ou gateway, por exemplo:

- Nginx
- Traefik
- API Gateway
- biblioteca de rate limiting na aplicacao

## Validacao de entrada

O projeto usa Bean Validation nos DTOs com anotacoes como:

- `@NotBlank`
- `@NotNull`
- `@Email`
- `@DecimalMin`
- `@Min`
- `@Valid`

O `GlobalExceptionHandler` converte falhas de validacao em respostas HTTP `400`.

## Protecao de imagens de produto

`ProductImageService` valida dois cenarios:

- URL `http` ou `https`
- Data URL de imagem `png`, `jpeg`, `jpg` ou `webp`

Restricoes atuais:

- tamanho maximo de imagem embutida: `1.5 MB`
- comprimento maximo de URL: `2048`

Esse controle reduz o risco de payloads invalidos ou excessivos no cadastro de produtos.

## Headers de seguranca

Os seguintes headers sao configurados:

- `X-Content-Type-Options`
- `X-Frame-Options: DENY`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `Permissions-Policy: camera=(), microphone=(), geolocation=()`

## CORS

O CORS e configurado por `APP_CORS_ALLOWED_ORIGINS`.

Comportamento atual:

- metodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
- headers permitidos: `Authorization`, `Content-Type`, `Accept`, `Origin`
- `allowCredentials=false`

Em producao, o ideal e restringir a lista apenas aos dominios necessarios.

## Isolamento por restaurante

O sistema adota isolamento logico entre restaurantes:

- o JWT carrega `restaurantId`
- `AuthContextService` expoe o contexto autenticado
- repositories e services filtram dados por `restauranteId`

Esse ponto e central para impedir que um usuario consulte ou altere dados de outro restaurante.

## Tratamento de erros

O `GlobalExceptionHandler` padroniza respostas para:

- `400` para regras de negocio e validacao
- `403` para acesso negado
- `404` para recurso nao encontrado
- `500` para erro interno

O retorno evita stack traces no payload HTTP.

## Alinhamento com OWASP

### Controles presentes

- A01 Broken Access Control: protecao por roles e filtragem por restaurante
- A02 Cryptographic Failures: uso de BCrypt para senhas e JWT assinado
- A03 Injection: persistencia via JPA e validacao de payloads reduzem superficie de erro
- A05 Security Misconfiguration: headers de seguranca, CORS configuravel e sessao stateless
- A07 Identification and Authentication Failures: JWT, bloqueio de login e politica de senha

### Lacunas atuais

- nao ha rate limiting global
- nao ha refresh token
- nao ha rotacao automatica de segredos
- nao ha trilha de auditoria dedicada
- nao ha configuracao de TLS no proprio repositorio

## Recomendacoes para producao

- trocar a chave JWT padrao por segredo forte e privado
- ativar HTTPS no proxy reverso
- restringir `APP_CORS_ALLOWED_ORIGINS`
- desativar `APP_SEED_ENABLED`
- revisar credenciais demo e remover dados de seed
- adicionar rate limiting fora ou dentro da aplicacao
- criar rotina de backup do PostgreSQL
- adicionar observabilidade e logs centralizados
