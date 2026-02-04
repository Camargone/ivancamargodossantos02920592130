package br.gov.mt.seplag.artistas.repository;

import br.gov.mt.seplag.artistas.domain.entity.Regional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionalRepository extends JpaRepository<Regional, Long> {

    Optional<Regional> findByIdExternoAndAtivoTrue(Integer idExterno);

    List<Regional> findByAtivoTrue();

    List<Regional> findAllByAtivoTrue();

    @Modifying
    @Query("UPDATE Regional r SET r.ativo = false WHERE r.idExterno = :idExterno AND r.ativo = true")
    void inativarPorIdExterno(@Param("idExterno") Integer idExterno);

    boolean existsByIdExternoAndAtivoTrue(Integer idExterno);

    Optional<Regional> findByIdExternoAndNomeAndAtivoTrue(Integer idExterno, String nome);
}
