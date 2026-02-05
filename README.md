# API de Artistas e Álbuns

## Projeto Prático - Desenvolvedor Back End Sênior

API REST para gerenciamento de artistas e álbuns musicais, desenvolvida como projeto prático para a vaga de Desenvolvedor Back End Sênior - SEPLAG/MT.

---

## Dados do Candidato

- **Nome:** Ivan Camargo dos Santos
- **Vaga:** Desenvolvedor Back End Sênior

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

O projeto suporta dois modos de execução através de **Spring Profiles**:

| Profile | Uso | Descricao                                 | Arquivo Docker |
|---------|-----|-------------------------------------------| ---------|
| `local` | IDE (IntelliJ, Eclipse, VSCode) | Profile padrão                            | docker-compose-infra.yml |
| `docker` | Container Docker | Ativado automaticamente no docker-compose | docker-compose.yml |

### Pré-requisitos
- Docker e Docker Compose instalados
- Portas disponíveis:
- **Profile local**: 8080 (API), 5435 (PostgreSQL), 9000 e 9001 (MinIO)
- **Profile docker**: 8081 (API), 5433 (PostgreSQL), 9100 e 9101 (MinIO)
- Java 21 (apenas para Modo local)
- Maven 3.9+ (apenas para Modo local)
---

### Opção 1: Executar via Docker (Recomendado)

```
┌─────────────────────────────────────────────────────────────┐
│                          DOCKER                             │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐       │
│  │     API     │──▶│  PostgreSQL │   │    MinIO    │       │
│  │   :8081     │   │   :5433     │   │ :9100/:9101 │       │
│  └─────────────┘   └─────────────┘   └─────────────┘       │
└─────────────────────────────────────────────────────────────┘
```
Este modo e ideal para **testes de integracao** e **deploy**.

Sobe toda a stack (API + PostgreSQL + MinIO) em containers:

```bash
# Clonar o repositório
git clone https://github.com/Camargone/ivancamargodossantos02920592130.git
cd ivancamargodossantos02920592130

# Subir todos os containers
docker-compose up -d

# Acompanhar logs
docker-compose logs -f api

# Verificar status
docker-compose ps
```

---

### Opção 2: Executar na IDE (IntelliJ / Eclipse / VSCode)
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

Para desenvolvimento local, você pode rodar a aplicacao na IDE enquanto os servicos de infraestrutura (banco e storage) rodam no Docker:

**Passo 1:** Subir apenas PostgreSQL e MinIO:
```bash
docker-compose -f docker-compose-infra.yml up -d

# Verificar se os containers estao rodando
docker-compose -f docker-compose-infra.yml ps
```

**Passo 2:** Executar a aplicacao na IDE:

#### IntelliJ IDEA
1. Abra o projeto (File > Open > selecione a pasta do projeto)
2. Aguarde o Maven importar as dependencias
3. Localize a classe `ArtistasAlbunsApplication.java`
4. Clique com botao direito > Run 'ArtistasAlbunsApplication'

**Ou configure o Run Configuration:**
1. Run > Edit Configurations
2. Clique em "+" > Spring Boot
3. Main class: `br.gov.mt.seplag.artistas.ArtistasAlbunsApplication`
4. Active profiles: `local` (opcional, ja e o padrao)
5. Clique em Apply e Run

#### Eclipse / STS
1. Import > Existing Maven Projects
2. Selecione a pasta do projeto
3. Clique direito no projeto > Run As > Spring Boot App

#### VSCode
1. Instale a extensao "Spring Boot Extension Pack"
2. Abra a pasta do projeto
3. Pressione F5 ou use o painel "Spring Boot Dashboard"

#### Via Maven (Terminal)
```bash
# Certifique-se de que postgres e minio estao rodando
docker-compose up -d postgres minio

# Execute a aplicacao com profile local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Ou no Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

---

### Verificar se esta funcionando

```bash
# Health check
curl http://localhost:8080/actuator/health

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```

### Parar a Aplicacao

```bash
# Parar todos os containers
docker-compose down

# Para remover volumes tambem (apaga dados do banco)
docker-compose down -v

# Parar apenas infra (se estiver rodando API na IDE)
docker-compose stop postgres minio
```

---

## Como Testar

### Executar Testes Unitários

```bash
# Com Maven instalado localmente
mvn test

# Ou via Docker
docker run --rm -v $(pwd):/app -w /app maven:3.9.6-eclipse-temurin-21-alpine mvn test
```

### Acessar Swagger UI

Após subir a aplicação, acesse:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

### Acessar MinIO Console

- **URL:** http://localhost:9001
- **Usuário:** minioadmin
- **Senha:** minioadmin

---

## Endpoints da API

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/auth/login` | Realizar login |
| POST | `/api/v1/auth/refresh` | Renovar token |

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### Artistas

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v1/artistas` | Listar artistas (paginado) |
| GET | `/api/v1/artistas/{id}` | Buscar por ID |
| POST | `/api/v1/artistas` | Criar artista |
| PUT | `/api/v1/artistas/{id}` | Atualizar artista |

**Parâmetros de consulta:**
- `page` - Número da página (default: 0)
- `size` - Tamanho da página (default: 10)
- `sort` - Ordenação: `asc` ou `desc` (default: asc)
- `nome` - Filtrar por nome
- `tipo` - Filtrar por tipo: `CANTOR` ou `BANDA`

### Álbuns

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v1/albuns` | Listar álbuns (paginado) |
| GET | `/api/v1/albuns/{id}` | Buscar por ID |
| POST | `/api/v1/albuns` | Criar álbum |
| PUT | `/api/v1/albuns/{id}` | Atualizar álbum |
| POST | `/api/v1/albuns/{id}/imagens` | Upload de imagens |
| GET | `/api/v1/albuns/{id}/imagens` | Listar imagens |

**Parâmetros de consulta:**
- `page`, `size`, `sort` - Paginação
- `tipo` - Filtrar por tipo de artista: `CANTOR` ou `BANDA`
- `artista` - Filtrar por nome do artista

### Regionais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v1/regionais` | Listar todas |
| GET | `/api/v1/regionais/ativas` | Listar ativas |
| POST | `/api/v1/regionais/sincronizar` | Sincronizar com API externa |

---

## Exemplos de Uso (Postman)

### 1. Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

### 2. Criar Artista

```http
POST /api/v1/artistas
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "nome": "System of a Down",
  "tipo": "BANDA"
}
```

### 3. Criar Álbum

```http
POST /api/v1/albuns
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "titulo": "Toxicity",
  "artistaIds": [5]
}
```

### 4. Upload de Imagem

```http
POST /api/v1/albuns/1/imagens
Authorization: Bearer {access_token}
Content-Type: multipart/form-data

files: [arquivo1.jpg, arquivo2.png]
```

### 5. Buscar Álbuns de Bandas

```http
GET /api/v1/albuns?tipo=BANDA&page=0&size=10&sort=asc
Authorization: Bearer {access_token}
```

### 6. Buscar Artistas por Nome

```http
GET /api/v1/artistas?nome=Mike&sort=asc
Authorization: Bearer {access_token}
```

### 7. Sincronizar Regionais

```http
POST /api/v1/regionais/sincronizar
Authorization: Bearer {access_token}
```

---

## WebSocket - Notificação de Novos Álbuns

Para receber notificações em tempo real quando um novo álbum é cadastrado:

### Conexão via STOMP

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

---

## Decisões Técnicas

### 1. Arquitetura em Camadas
Optei por uma arquitetura em camadas para facilitar a manutenção, testabilidade e separação de responsabilidades.

### 2. Relacionamento N:N com Tabela Intermediária
Utilizei uma tabela intermediária `artista_album` para o relacionamento muitos-para-muitos, permitindo que um álbum tenha múltiplos artistas (colaborações).

### 3. Flyway para Migrações 
Flyway para versionamento do schema do banco, garantindo consistência entre ambientes.

### 4. MinIO para Storage
MinIO foi escolhido por ser compatível com S3 e permitir execução local via Docker, simulando um ambiente de produção.

### 5. URLs Pré-assinadas
As imagens são acessadas via URLs pré-assinadas com expiração de 30 minutos, garantindo segurança no acesso aos arquivos.

### 6. Rate Limiting por Usuário
Implementei rate limit de 10 requisições/minuto por usuário autenticado, usando Bucket4j com armazenamento em memória.

### 7. Sincronização de Regionais
A sincronização segue as regras especificadas:
- Novo registro na API externa → INSERT
- Registro ausente → Inativar (soft delete)
- Atributo alterado → Inativar antigo e criar novo

### 8. JWT com Refresh Token
Implementei dois tipos de token para maior segurança:
- Access Token: curta duração (5 min)
- Refresh Token: maior duração (30 min)

---

## O que foi implementado

- [x] API REST versionada (v1)
- [x] CRUD de Artistas (POST, PUT, GET)
- [x] CRUD de Álbuns (POST, PUT, GET)
- [x] Relacionamento N:N Artista-Álbum
- [x] Paginação na consulta de álbuns
- [x] Consultas parametrizadas (tipo cantor/banda)
- [x] Consultas por nome do artista com ordenação
- [x] Upload de múltiplas imagens de capa
- [x] Armazenamento no MinIO
- [x] URLs pré-assinadas com expiração de 30 min
- [x] Autenticação JWT
- [x] Token com expiração de 5 minutos
- [x] Refresh token
- [x] CORS configurável
- [x] Rate limiting (10 req/min por usuário)
- [x] Flyway Migrations
- [x] Swagger/OpenAPI
- [x] Health Checks (Liveness/Readiness)
- [x] WebSocket para notificação de novos álbuns
- [x] Sincronização de regionais com API externa
- [x] Testes unitários
- [x] Docker e Docker Compose
- [x] README com documentação completa

---

## Licença

Este projeto foi desenvolvido exclusivamente para fins de avaliação técnica.
