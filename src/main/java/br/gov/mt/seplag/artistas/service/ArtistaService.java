package br.gov.mt.seplag.artistas.service;

import br.gov.mt.seplag.artistas.domain.entity.Artista;
import br.gov.mt.seplag.artistas.domain.entity.TipoArtista;
import br.gov.mt.seplag.artistas.dto.ArtistaDTO;
import br.gov.mt.seplag.artistas.exception.ResourceNotFoundException;
import br.gov.mt.seplag.artistas.repository.ArtistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistaService {

    private final ArtistaRepository artistaRepository;

    @Transactional(readOnly = true)
    public Page<ArtistaDTO> listarTodos(int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by("nome").descending()
                : Sort.by("nome").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return artistaRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ArtistaDTO buscarPorId(Long id) {
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com id: " + id));
        return toDTO(artista);
    }

    @Transactional(readOnly = true)
    public Page<ArtistaDTO> buscarPorNome(String nome, int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by("nome").descending()
                : Sort.by("nome").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return artistaRepository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ArtistaDTO> buscarPorTipo(TipoArtista tipo, int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by("nome").descending()
                : Sort.by("nome").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return artistaRepository.findByTipo(tipo, pageable).map(this::toDTO);
    }

    @Transactional
    public ArtistaDTO criar(ArtistaDTO dto) {
        Artista artista = Artista.builder()
                .nome(dto.getNome())
                .tipo(dto.getTipo())
                .build();
        artista = artistaRepository.save(artista);
        log.info("Artista criado: {}", artista.getNome());
        return toDTO(artista);
    }

    @Transactional
    public ArtistaDTO atualizar(Long id, ArtistaDTO dto) {
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com id: " + id));

        artista.setNome(dto.getNome());
        artista.setTipo(dto.getTipo());
        artista = artistaRepository.save(artista);
        log.info("Artista atualizado: {}", artista.getNome());
        return toDTO(artista);
    }

    @Transactional(readOnly = true)
    public List<Artista> buscarPorIds(List<Long> ids) {
        return artistaRepository.findAllById(ids);
    }

    private ArtistaDTO toDTO(Artista artista) {
        return ArtistaDTO.builder()
                .id(artista.getId())
                .nome(artista.getNome())
                .tipo(artista.getTipo())
                .build();
    }
}
