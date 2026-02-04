package br.gov.mt.seplag.artistas.repository;

import br.gov.mt.seplag.artistas.domain.entity.Artista;
import br.gov.mt.seplag.artistas.domain.entity.TipoArtista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, Long> {

    List<Artista> findByNomeContainingIgnoreCase(String nome);

    Page<Artista> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    List<Artista> findByTipo(TipoArtista tipo);

    Page<Artista> findByTipo(TipoArtista tipo, Pageable pageable);

    @Query("SELECT a FROM Artista a WHERE LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Artista> buscarPorNome(@Param("nome") String nome);

    boolean existsByNomeIgnoreCase(String nome);
}
