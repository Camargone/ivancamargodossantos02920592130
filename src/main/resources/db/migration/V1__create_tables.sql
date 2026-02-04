-- V1__create_tables.sql
-- Criação das tabelas principais do sistema

-- Tabela de usuários para autenticação JWT
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabela de artistas (cantores e bandas)
CREATE TABLE artista (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('CANTOR', 'BANDA'))
);

-- Tabela de álbuns
CREATE TABLE album (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de relacionamento N:N entre artistas e álbuns
CREATE TABLE artista_album (
    artista_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    PRIMARY KEY (artista_id, album_id),
    CONSTRAINT fk_artista_album_artista FOREIGN KEY (artista_id) REFERENCES artista(id) ON DELETE CASCADE,
    CONSTRAINT fk_artista_album_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE
);

-- Tabela de imagens de capa dos álbuns
CREATE TABLE album_imagem (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL,
    nome_arquivo VARCHAR(255),
    object_key VARCHAR(500),
    CONSTRAINT fk_album_imagem_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE
);

-- Tabela de regionais (sincronização com API externa)
CREATE TABLE regional (
    id BIGSERIAL PRIMARY KEY,
    id_externo INTEGER,
    nome VARCHAR(200),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Índices para otimização de consultas
CREATE INDEX idx_artista_nome ON artista(nome);
CREATE INDEX idx_artista_tipo ON artista(tipo);
CREATE INDEX idx_album_titulo ON album(titulo);
CREATE INDEX idx_album_data_criacao ON album(data_criacao);
CREATE INDEX idx_regional_id_externo ON regional(id_externo);
CREATE INDEX idx_regional_ativo ON regional(ativo);
