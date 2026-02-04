package br.gov.mt.seplag.artistas.repository;

import br.gov.mt.seplag.artistas.domain.entity.Album;
import br.gov.mt.seplag.artistas.domain.entity.TipoArtista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("SELECT DISTINCT a FROM Album a LEFT JOIN FETCH a.artistas WHERE a.id = :id")
    Optional<Album> findByIdWithArtistas(@Param("id") Long id);

    @Query("SELECT DISTINCT a FROM Album a JOIN a.artistas art WHERE art.tipo = :tipo")
    Page<Album> findByTipoArtista(@Param("tipo") TipoArtista tipo, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Album a JOIN a.artistas art WHERE LOWER(art.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<Album> findByArtistaNome(@Param("nome") String nomeArtista, Pageable pageable);

    Page<Album> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    boolean existsByTituloIgnoreCase(String titulo);
}