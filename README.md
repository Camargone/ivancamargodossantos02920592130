# API de Artistas e Álbuns

## Projeto Prático - Desenvolvedor Back End Sênior

API REST para gerenciamento de artistas e álbuns musicais, desenvolvida como projeto prático para a vaga de Desenvolvedor Back End Sênior - SEPLAG/MT.

---

## Dados do Candidato

- **Nome:** Ivan Camargo dos Santos
- **Vaga:** Desenvolvedor Back End Sênior
- **Inscrição:** 16470

---

## Tecnologias Utilizadas

### Backend
- Java 21
- Spring Boot 3.2.0
- Spring Web
- Spring Data JPA
- Spring Security
- Spring WebSocket
- Spring Actuator

### Banco de Dados
- PostgreSQL 16
- Flyway Migrations

### Autenticação
- JWT (jjwt 0.12.3)
- Access Token: 5 minutos
- Refresh Token: 30 minutos

### Storage
- MinIO (API S3 compatível)
- URLs pré-assinadas com expiração de 30 minutos

### Rate Limiting
- Bucket4j
- 10 requisições/minuto por usuário

### Documentação
- Springdoc OpenAPI (Swagger)

### Testes
- JUnit 5
- Mockito
- H2 Database (testes)

### Containerização
- Docker
- Docker Compose

---

## Arquitetura

O projeto segue uma arquitetura em camadas (Layered Architecture), amplamente utilizada em aplicações Spring Boot, favorecendo separação de responsabilidades, manutenibilidade, testabilidade e evolução do código.

A aplicação foi projetada para suportar dois modos de execução:

- Execução local (API na IDE + Banco e MinIO via Docker)

- Execução FULL DOCKER (API + Banco + MinIO orquestrados via Docker Compose)
```
src/main/java/br/gov/mt/seplag/artistas/
├── config/           # Configurações (Security, MinIO, WebSocket, OpenAPI)
├── controller/       # Controllers REST (API v1)
├── domain/
│   └── entity/       # Entidades JPA
├── dto/              # Data Transfer Objects
│   └── auth/         # DTOs de autenticação
├── exception/        # Exceções customizadas e handlers
├── repository/       # Repositórios JPA
├── security/         # Filtros e serviços de segurança
└── service/          # Serviços de negócio
```

### Modelo de Dados

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────┐
│   ARTISTA   │     │  ARTISTA_ALBUM   │     │    ALBUM    │
├─────────────┤     ├──────────────────┤     ├─────────────┤
│ id          │──┐  │ artista_id (FK)  │  ┌──│ id          │
│ nome        │  └──│ album_id (FK)    │──┘  │ titulo      │
│ tipo        │     └──────────────────┘     │ data_criacao│
└─────────────┘                              └─────────────┘
                                                    │
                                                    │ 1:N
                                                    ▼
                                            ┌──────────────┐
                                            │ ALBUM_IMAGEM │
                                            ├──────────────┤
                                            │ id           │
                                            │ album_id     │
                                            │ nome_arquivo │
                                            │ object_key   │
                                            └──────────────┘

┌─────────────┐     ┌─────────────┐
│   USUARIO   │     │   REGIONAL  │
├─────────────┤     ├─────────────┤
│ id          │     │ id          │
│ username    │     │ id_externo  │
│ password    │     │ nome        │
│ enabled     │     │ ativo       │
└─────────────┘     └─────────────┘
```

---

## Como Executar

O projeto suporta **dois modos de execução** através de Spring Profiles:


| Modo | Profile | Arquivo Docker | Descricao |
|------|---------|----------------|-----------|
| **Modo 1** | `local` | `docker-compose-infra.yml` | API na IDE + Banco/MinIO no Docker |
| **Modo 2** | `docker` | `docker-compose.yml` | Tudo no Docker |

### Pré-requisitos
- Docker e Docker Compose instalados
- Portas disponiveis:
    - **Profile local**: 8080 (API), 5435 (PostgreSQL), 9000 e 9001 (MinIO)
    - **Profile docker**: 8081 (API), 5433 (PostgreSQL), 9100 e 9101 (MinIO)
- Java 21 (apenas para Modo 1)
- Maven 3.9+ (apenas para Modo 1)
---
## Modo 1: API Local + Banco/MinIO no Docker

Este modo e ideal para **desenvolvimento e debug** na IDE.

```
┌─────────────────────────────────────────────────────────────┐
│                        SEU COMPUTADOR                       │
│  ┌─────────────────┐     ┌─────────────────────────────┐   │
│  │                 │     │         DOCKER              │   │
│  │   IntelliJ /    │     │  ┌─────────┐  ┌─────────┐  │   │
│  │   Eclipse /     │────▶│  │Postgres │  │  MinIO  │  │   │
│  │   VSCode        │     │  │ :5435   │  │ :9000   │  │   │
│  │                 │     │  └─────────┘  └─────────┘  │   │
│  │  (API :8080)    │     │                            │   │
│  └─────────────────┘     └─────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Passo 1: Subir infraestrutura (PostgreSQL + MinIO)

```bash
docker-compose -f docker-compose-infra.yml up -d

docker-compose -f docker-compose-infra.yml ps
```

### Passo 2: Executar a API na IDE

#### IntelliJ IDEA
1. Abra o projeto (File > Open > selecione a pasta)
2. Aguarde o Maven importar as dependencias
3. Localize `ArtistasAlbunsApplication.java`
4. Clique direito > Run 'ArtistasAlbunsApplication'

**Configuracao alternativa (Run Configuration):**
1. Run > Edit Configurations
2. Clique em "+" > Spring Boot
3. Main class: `br.gov.mt.seplag.artistas.ArtistasAlbunsApplication`
4. Active profiles: `local` (opcional, ja e o padrao)
5. Apply > Run

#### Via Terminal (Maven)
```bash
# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

### Parar Modo 1

```bash
# Parar a API: Ctrl+C no terminal ou Stop na IDE

# Parar infraestrutura
docker-compose -f docker-compose-infra.yml down

# Parar e remover volumes (apaga dados)
docker-compose -f docker-compose-infra.yml down -v
```

---

## Modo 2: Tudo no Docker

Este modo e ideal para **testes de integracao** e **deploy**.

```
┌─────────────────────────────────────────────────────────────┐
│                          DOCKER                             │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐       │
│  │     API     │──▶│  PostgreSQL │   │    MinIO    │       │
│  │   :8081     │   │   :5433     │   │ :9100/:9101 │       │
│  └─────────────┘   └─────────────┘   └─────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### Subir toda a stack

```bash
git clone https://github.com/Camargone/ivancamargodossantos02920592130.git
cd ivancamargodossantos02920592130

docker-compose up -d

docker-compose logs -f api

docker-compose ps
```

### Rebuild da API (apos alteracoes no codigo)

```bash
docker-compose up -d --build api
```

### Parar Modo 2

```bash
docker-compose down

# Parar e remover volumes (apaga dados)
docker-compose down -v
```

---

## Verificar se esta funcionando

```bash
# Health check
curl http://localhost:8080/actuator/health

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```

---

## Resumo dos Comandos

| Acao | Modo 1 (Local + Docker) | Modo 2 (Tudo Docker) |
|------|-------------------------|----------------------|
| Subir | `docker-compose -f docker-compose-infra.yml up -d` + IDE | `docker-compose up -d` |
| Logs | IDE Console | `docker-compose logs -f api` |
| Parar | IDE Stop + `docker-compose -f docker-compose-infra.yml down` | `docker-compose down` |
| Limpar | `docker-compose -f docker-compose-infra.yml down -v` | `docker-compose down -v` |

---

## Como Testar

### Executar Testes Unitários

```bash
# Com Maven instalado localmente
mvn test

# Windows
mvnw.cmd test
```

## Acessando Swagger UI e MinIO

### Profile local (banco + MinIO em Docker)

Após subir a aplicação, acesse: (alterne as portas conforme necessário)
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs
- **MinIO Console:** http://localhost:9001
- **Usuário:** minioadmin
- **Senha:** minioadmin

### Profile full Docker (API + banco + MinIO)

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8081/api-docs
- **MinIO Console:** http://localhost:9101
- **Usuário:** minioadmin
- **Senha:** minioadmin
---

## Endpoints da API

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/auth/login` | Realizar login |
| POST | `/api/v1/auth/refresh` | Renovar token |

**Login (CMD):**
```cmd
curl.exe -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```
**Login (Linux/Mac):**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### Artistas



| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/api/v1/artistas` | Listar artistas (paginado) |
| GET | `/api/v1/artistas/{id}` | Buscar por ID |
| POST | `/api/v1/artistas` | Criar artista |
| PUT | `/api/v1/artistas/{id}` | Atualizar artista |

**Parametros de consulta:**
- `page` - Numero da pagina (default: 0)
- `size` - Tamanho da pagina (default: 10)
- `sort` - Ordenacao: `asc` ou `desc` (default: asc)
- `nome` - Filtrar por nome
- `tipo` - Filtrar por tipo: `CANTOR` ou `BANDA`

### Álbuns


| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/api/v1/albuns` | Listar albuns (paginado) |
| GET | `/api/v1/albuns/{id}` | Buscar por ID |
| POST | `/api/v1/albuns` | Criar album |
| PUT | `/api/v1/albuns/{id}` | Atualizar album |
| POST | `/api/v1/albuns/{id}/imagens` | Upload de imagens |
| GET | `/api/v1/albuns/{id}/imagens` | Listar imagens |

**Parametros de consulta:**
- `page`, `size`, `sort` - Paginacao
- `tipo` - Filtrar por tipo de artista: `CANTOR` ou `BANDA`
- `artista` - Filtrar por nome do artista

### Regionais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v1/regionais` | Listar todas |
| GET | `/api/v1/regionais/ativas` | Listar ativas |
| POST | `/api/v1/regionais/sincronizar` | Sincronizar com API externa |

---

## Exemplos de Uso

### Via Postman (Recomendado)

Uma colecao Postman esta disponivel na pasta `/postman/Artistas-Albuns-API.postman_collection.json`.
Importe no Postman e todos os endpoints ja estarão configurados.

#### 1. Login
```
POST http://localhost:8080/api/v1/auth/login
Body (raw - JSON):
{
    "username": "admin",
    "password": "admin123"
}
```
Copie o `accessToken` da resposta para usar nos proximos requests.

#### 2. Autenticar demais requests
Em cada request, va na aba **Authorization**:
- Type: **Bearer Token**
- Token: cole o `accessToken`

#### 3. Criar Artista
```
POST http://localhost:8080/api/v1/artistas
Authorization: Bearer SEU_TOKEN
Body (raw - JSON):
{
    "nome": "Linkin Park",
    "tipo": "BANDA"
}
```

#### 4. Listar Artistas (com paginacao e filtros)
```
GET http://localhost:8080/api/v1/artistas?page=0&size=10&sort=asc
GET http://localhost:8080/api/v1/artistas?nome=Linkin&tipo=BANDA
Authorization: Bearer SEU_TOKEN
```

#### 5. Criar Album
```
POST http://localhost:8080/api/v1/albuns
Authorization: Bearer SEU_TOKEN
Body (raw - JSON):
{
    "titulo": "Hybrid Theory",
    "artistaIds": [5]
}
```

#### 6. Upload de Imagem de Capa
```
POST http://localhost:8080/api/v1/albuns/{id}/imagens
Authorization: Bearer SEU_TOKEN
Body: form-data
  Key: files   (Tipo: File - clique no dropdown e troque de Text para File)
  Value: selecione o arquivo de imagem
```

#### 7. Listar Imagens de um Album
```
GET http://localhost:8080/api/v1/albuns/{id}/imagens
Authorization: Bearer SEU_TOKEN
```
Retorna URLs pre-assinadas (validas por 30 minutos) para cada imagem.

#### 8. Refresh Token (quando access token expirar)
```
POST http://localhost:8080/api/v1/auth/refresh
Body (raw - JSON):
{
    "refreshToken": "SEU_REFRESH_TOKEN"
}
```

#### 9. Sincronizar Regionais
```
POST http://localhost:8080/api/v1/regionais/sincronizar
Authorization: Bearer SEU_TOKEN
```

#### 10. Listar Regionais Ativas
```
GET http://localhost:8080/api/v1/regionais/ativas
Authorization: Bearer SEU_TOKEN
```
---

### Via CMD Windows

> **Nota:** No Windows, use `cmd.exe` (Prompt de Comando) para os exemplos com curl.
> No PowerShell, use acento grave para escapar: `{`"username`":`"admin`"}`.
> Nos exemplos abaixo, substitua `SEU_TOKEN` pelo accessToken retornado no login.

### 1. Login

```cmd
curl.exe -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

### 2. Refresh Token

```cmd
curl.exe -X POST http://localhost:8080/api/v1/auth/refresh -H "Content-Type: application/json" -d "{\"refreshToken\":\"SEU_REFRESH_TOKEN\"}"
```

### 3. Criar Artista

```cmd
curl.exe -X POST http://localhost:8080/api/v1/artistas -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN" -d "{\"nome\":\"System of a Down\",\"tipo\":\"BANDA\"}"
```

### 4. Listar Artistas

```cmd
curl.exe -X GET "http://localhost:8080/api/v1/artistas?page=0&size=10&sort=asc" -H "Authorization: Bearer SEU_TOKEN"
```

### 5. Criar Album

```cmd
curl.exe -X POST http://localhost:8080/api/v1/albuns -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN" -d "{\"titulo\":\"Toxicity\",\"artistaIds\":[5]}"
```

### 6. Upload de Imagem (usar Postman)

```
POST http://localhost:8080/api/v1/albuns/{id}/imagens
Authorization: Bearer SEU_TOKEN
Body: form-data
  Key: files (tipo: File)
  Value: selecione a imagem
```

### 7. Buscar Albuns de Bandas

```cmd
curl.exe -X GET "http://localhost:8080/api/v1/albuns?tipo=BANDA&page=0&size=10&sort=asc" -H "Authorization: Bearer SEU_TOKEN"
```

### 8. Sincronizar Regionais

```cmd
curl.exe -X POST http://localhost:8080/api/v1/regionais/sincronizar -H "Authorization: Bearer SEU_TOKEN"
```

---
## WebSocket - Notificação de Novos Álbuns

Quando um novo album e cadastrado via POST, todos os clientes conectados ao WebSocket recebem uma notificacao em tempo real.

### Pagina de Teste

Acesse no navegador:
- **Profile local:** http://localhost:8080/websocket-test.html
- **Profile Docker:** http://localhost:8081/websocket-test.html

### Conexão via STOMP (referencia para integracao)

```javascript
const socket = new SockJS('http://localhost:8080/ws/albuns');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    stompClient.subscribe('/topic/novo-album', function(message) {
        const album = JSON.parse(message.body);
        console.log('Novo álbum cadastrado:', album);
    });
});
```
### Fluxo da Notificacao

```
1. Cliente conecta em ws://localhost:8080/ws/albuns (WebSocket)
2. Cliente assina /topic/novo-album (STOMP SUBSCRIBE)
3. Alguem cria album via POST /api/v1/albuns
4. AlbumService envia notificacao via messagingTemplate.convertAndSend()
5. Todos os clientes inscritos recebem o JSON do album criado
```
```
┌───────────────────────────────────────────────────────────────────────┐
│                    FLUXO COMPLETO DO SISTEMA                          │
├───────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌─────────────┐                                                      │
│  │  NAVEGADOR  │──── http://localhost:3000 ────┐                      │
│  │ (Frontend)  │                               │                      │
│  └─────────────┘                               │                      │
│        │                                       │                      │
│        │ Header: Origin                        │                      │
│        ▼                                       ▼                      │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                         CORS CHECK                               │ │
│  │     Origem permitida? (localhost:3000, localhost:8080)          │ │
│  │     SIM ──▶ Continua    NAO ──▶ Bloqueia                        │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│        │                                                              │
│        ▼                                                              │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                      JWT AUTHENTICATION                          │ │
│  │     Header: Authorization: Bearer <ACCESS_TOKEN>                 │ │
│  │     Token valido? SIM ──▶ Continua    NAO ──▶ 401 Unauthorized  │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│        │                                                              │
│        ▼                                                              │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                       RATE LIMIT                                 │ │
│  │     Menos de 10 req/min? SIM ──▶ Continua   NAO ──▶ 429 Error   │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│        │                                                              │
│        ▼                                                              │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                      CONTROLLER / SERVICE                        │ │
│  │     Processa a requisicao e retorna resposta                    │ │
│  │                                                                  │ │
│  │     Se criar Album ──▶ WebSocket.send("/topic/novo-album")      │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                    │                                  │
│                                    ▼                                  │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                        WEBSOCKET                                 │ │
│  │     Todos clientes conectados em /topic/novo-album recebem      │ │
│  │     a notificacao em tempo real                                 │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```


---

## Decisões Técnicas


### 1. Arquitetura em Camadas
Optei por uma arquitetura em camadas para facilitar a manutencao, testabilidade e separacao de responsabilidades.

### 2. Relacionamento N:N com Tabela Intermediaria
Utilizei uma tabela intermediaria `artista_album` para o relacionamento muitos-para-muitos, permitindo que um album tenha multiplos artistas (colaboracoes).

### 3. Flyway para Migracoes + DataInitializer
O Flyway versiona o schema do banco e popula os dados iniciais (V1: tabelas, V2: dados).
O `DataInitializer` (CommandLineRunner) complementa garantindo que o hash BCrypt do usuario padrao
esteja sempre compativel com o `PasswordEncoder` do Spring Security em runtime, independentemente
da versao ou strength configurada. Ele verifica com `matches()` antes de atualizar, evitando
escritas desnecessarias a cada reinicializacao.

### 4. MinIO para Storage
MinIO foi escolhido por ser compativel com S3 e permitir execucao local via Docker, simulando um ambiente de producao.

### 5. URLs Pre-assinadas
As imagens sao acessadas via URLs pre-assinadas com expiracao de 30 minutos, garantindo seguranca no acesso aos arquivos.

### 6. Rate Limiting por Usuario
Implementei rate limit de 10 requisicoes/minuto por usuario autenticado, usando Bucket4j com armazenamento em memoria.

### 7. Sincronizacao de Regionais
A sincronizacao segue as regras especificadas:
- Novo registro na API externa -> INSERT
- Registro ausente -> Inativar (soft delete)
- Atributo alterado -> Inativar antigo e criar novo

### 8. JWT com Refresh Token
Implementei dois tipos de token para maior seguranca:
- Access Token: curta duracao (5 min) - usado em todas as requisicoes protegidas
- Refresh Token: maior duracao (30 min) - usado apenas para obter um novo access token

### 9. CORS - Bloqueio de Dominios Externos
O `SecurityConfig` restringe requisicoes via CORS, permitindo apenas origens configuradas
em `cors.allowed-origins`. Requisicoes de dominios nao autorizados sao bloqueadas pelo navegador.

### 10. Spring Profiles para Ambientes
Dois profiles (`local` e `docker`) com portas e hosts distintos permitem execucao
simultanea sem conflito, alem de facilitar o desenvolvimento local com debug na IDE.

---

## O que foi implementado

- [x] API REST versionada (v1)
- [x] CRUD de Artistas (POST, PUT, GET)
- [x] CRUD de Albuns (POST, PUT, GET)
- [x] Relacionamento N:N Artista-Album
- [x] Paginação na consulta de álbuns
- [x] Consultas parametrizadas (tipo cantor/banda)
- [x] Consultas por nome do artista com ordenação
- [x] Upload de multiplas imagens de capa
- [x] Armazenamento no MinIO
- [x] URLs pre-assinadas com expiração de 30 min
- [x] Autenticação JWT
- [x] Token com expiracao de 5 minutos
- [x] Refresh token
- [x] CORS configuravel
- [x] Rate limiting (10 req/min por usuario)
- [x] Flyway Migrations
- [x] DataInitializer para validação de senha em runtime
- [x] Swagger/OpenAPI
- [x] Health Checks (Liveness/Readiness)
- [x] WebSocket para notificação de novos álbuns
- [x] Sincronizacao de regionais com API externa
- [x] Testes unitarios
- [x] Docker e Docker Compose
- [x] README com documentação completa

---

## Licença

Este projeto foi desenvolvido exclusivamente para fins de avaliação técnica.
