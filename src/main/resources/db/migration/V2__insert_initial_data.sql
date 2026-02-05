-- V2__insert_initial_data.sql
-- Carga inicial de dados conforme especificado no edital

-- Inserir usuário padrão para testes (senha: admin123 - BCrypt encoded)
-- Hash gerado com BCryptPasswordEncoder: new BCryptPasswordEncoder().encode("admin123")
INSERT INTO usuario (username, password, enabled)
VALUES ('admin', '$2a$10$8KxKBEWBJPBGgQLgm3GkHOiZBXjVNa0MQ5HOmCSmu/BK4FFvKTcLG', true);

-- Inserir artistas
INSERT INTO artista (nome, tipo) VALUES
('Serj Tankian', 'CANTOR'),
('Mike Shinoda', 'CANTOR'),
('Michel Teló', 'CANTOR'),
('Guns N'' Roses', 'BANDA');

-- Inserir álbuns do Serj Tankian
INSERT INTO album (titulo, data_criacao) VALUES
('Harakiri', '2012-07-06 00:00:00'),
('Black Blooms', '2019-09-27 00:00:00'),
('The Rough Dog', '2011-11-29 00:00:00');

-- Inserir álbuns do Mike Shinoda
INSERT INTO album (titulo, data_criacao) VALUES
('The Rising Tied', '2005-11-21 00:00:00'),
('Post Traumatic', '2018-06-15 00:00:00'),
('Post Traumatic EP', '2018-01-25 00:00:00'),
('Where''d You Go', '2006-03-14 00:00:00');

-- Inserir álbuns do Michel Teló
INSERT INTO album (titulo, data_criacao) VALUES
('Bem Sertanejo', '2014-11-18 00:00:00'),
('Bem Sertanejo - O Show (Ao Vivo)', '2015-12-04 00:00:00'),
('Bem Sertanejo - (1ª Temporada) - EP', '2015-02-24 00:00:00');

-- Inserir álbuns do Guns N' Roses
INSERT INTO album (titulo, data_criacao) VALUES
('Use Your Illusion I', '1991-09-17 00:00:00'),
('Use Your Illusion II', '1991-09-17 00:00:00'),
('Greatest Hits', '2004-03-23 00:00:00');

-- Relacionamentos Artista-Álbum (N:N)
-- Serj Tankian (id=1) com seus álbuns (ids 1, 2, 3)
INSERT INTO artista_album (artista_id, album_id) VALUES
(1, 1), (1, 2), (1, 3);

-- Mike Shinoda (id=2) com seus álbuns (ids 4, 5, 6, 7)
INSERT INTO artista_album (artista_id, album_id) VALUES
(2, 4), (2, 5), (2, 6), (2, 7);

-- Michel Teló (id=3) com seus álbuns (ids 8, 9, 10)
INSERT INTO artista_album (artista_id, album_id) VALUES
(3, 8), (3, 9), (3, 10);

-- Guns N' Roses (id=4) com seus álbuns (ids 11, 12, 13)
INSERT INTO artista_album (artista_id, album_id) VALUES
(4, 11), (4, 12), (4, 13);
