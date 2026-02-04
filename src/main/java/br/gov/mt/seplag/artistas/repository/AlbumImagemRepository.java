package br.gov.mt.seplag.artistas.repository;

import br.gov.mt.seplag.artistas.domain.entity.AlbumImagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumImagemRepository extends JpaRepository<AlbumImagem, Long> {

    List<AlbumImagem> findByAlbumId(Long albumId);

    void deleteByAlbumId(Long albumId);
}
