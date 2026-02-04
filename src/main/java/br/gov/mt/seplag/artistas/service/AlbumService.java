package br.gov.mt.seplag.artistas.service;

import br.gov.mt.seplag.artistas.domain.entity.Album;
import br.gov.mt.seplag.artistas.domain.entity.Artista;
import br.gov.mt.seplag.artistas.domain.entity.TipoArtista;
import br.gov.mt.seplag.artistas.dto.AlbumDTO;
import br.gov.mt.seplag.artistas.dto.AlbumImagemDTO;
import br.gov.mt.seplag.artistas.dto.ArtistaDTO;
import br.gov.mt.seplag.artistas.exception.ResourceNotFoundException;
import br.gov.mt.seplag.artistas.repository.AlbumRepository;
import br.gov.mt.seplag.artistas.repository.ArtistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;
    private final MinioService minioService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public Page<AlbumDTO> listarTodos(int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by("titulo").descending()
                : Sort.by("titulo").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return albumRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public AlbumDTO buscarPorId(Long id) {
        Album album = albumRepository.findByIdWithArtistas(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com id: " + id));
        return toDTO(album);
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> buscarPorTipoArtista(String tipo, int page, int size, String sortDir) {
        TipoArtista tipoArtista = TipoArtista.valueOf(tipo.toUpperCase());
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by("titulo").descending()
                : Sort.by("titulo").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return albumRepository.findByTipoArtista(tipoArtista, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> buscarPorNomeArtista(String nomeArtista, int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by("titulo").descending()
                : Sort.by("titulo").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return albumRepository.findByArtistaNome(nomeArtista, pageable).map(this::toDTO);
    }

    @Transactional
    public AlbumDTO criar(AlbumDTO dto) {
        Set<Artista> artistas = new HashSet<>(artistaRepository.findAllById(dto.getArtistaIds()));

        if (artistas.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum artista encontrado com os IDs fornecidos");
        }

        Album album = Album.builder()
                .titulo(dto.getTitulo())
                .artistas(artistas)
                .build();

        album = albumRepository.save(album);
        log.info("Álbum criado: {}", album.getTitulo());

        AlbumDTO albumDTO = toDTO(album);

        // Notificar via WebSocket
        messagingTemplate.convertAndSend("/topic/novo-album", albumDTO);
        log.info("Notificação WebSocket enviada para novo álbum: {}", album.getTitulo());

        return albumDTO;
    }

    @Transactional
    public AlbumDTO atualizar(Long id, AlbumDTO dto) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com id: " + id));

        Set<Artista> artistas = new HashSet<>(artistaRepository.findAllById(dto.getArtistaIds()));

        if (artistas.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum artista encontrado com os IDs fornecidos");
        }

        album.setTitulo(dto.getTitulo());
        album.setArtistas(artistas);
        album = albumRepository.save(album);
        log.info("Álbum atualizado: {}", album.getTitulo());
        return toDTO(album);
    }

    private AlbumDTO toDTO(Album album) {
        List<ArtistaDTO> artistas = album.getArtistas().stream()
                .map(a -> ArtistaDTO.builder()
                        .id(a.getId())
                        .nome(a.getNome())
                        .tipo(a.getTipo())
                        .build())
                .collect(Collectors.toList());

        List<AlbumImagemDTO> imagens = album.getImagens().stream()
                .map(img -> {
                    String url = null;
                    try {
                        url = minioService.getPresignedUrl(img.getObjectKey());
                    } catch (Exception e) {
                        log.warn("Erro ao gerar URL para imagem: {}", img.getObjectKey());
                    }
                    return AlbumImagemDTO.builder()
                            .id(img.getId())
                            .albumId(album.getId())
                            .nomeArquivo(img.getNomeArquivo())
                            .url(url)
                            .build();
                })
                .collect(Collectors.toList());

        return AlbumDTO.builder()
                .id(album.getId())
                .titulo(album.getTitulo())
                .dataCriacao(album.getDataCriacao())
                .artistas(artistas)
                .imagens(imagens)
                .artistaIds(album.getArtistas().stream().map(Artista::getId).collect(Collectors.toSet()))
                .build();
    }
}
