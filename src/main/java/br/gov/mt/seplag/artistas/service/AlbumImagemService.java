package br.gov.mt.seplag.artistas.service;

import br.gov.mt.seplag.artistas.domain.entity.Album;
import br.gov.mt.seplag.artistas.domain.entity.AlbumImagem;
import br.gov.mt.seplag.artistas.dto.AlbumImagemDTO;
import br.gov.mt.seplag.artistas.exception.ResourceNotFoundException;
import br.gov.mt.seplag.artistas.repository.AlbumImagemRepository;
import br.gov.mt.seplag.artistas.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumImagemService {

    private final AlbumImagemRepository albumImagemRepository;
    private final AlbumRepository albumRepository;
    private final MinioService minioService;

    @Transactional
    public List<AlbumImagemDTO> uploadImagens(Long albumId, MultipartFile[] files) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com id: " + albumId));

        List<AlbumImagem> imagensSalvas = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            // Validar tipo de arquivo
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Arquivo ignorado por não ser imagem: {}", file.getOriginalFilename());
                continue;
            }

            // Upload para MinIO
            String objectKey = minioService.uploadFile(file, albumId);

            // Salvar referência no banco
            AlbumImagem imagem = AlbumImagem.builder()
                    .album(album)
                    .nomeArquivo(file.getOriginalFilename())
                    .objectKey(objectKey)
                    .build();

            imagensSalvas.add(albumImagemRepository.save(imagem));
            log.info("Imagem salva para álbum {}: {}", albumId, file.getOriginalFilename());
        }

        return imagensSalvas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlbumImagemDTO> listarImagensDoAlbum(Long albumId) {
        if (!albumRepository.existsById(albumId)) {
            throw new ResourceNotFoundException("Álbum não encontrado com id: " + albumId);
        }

        return albumImagemRepository.findByAlbumId(albumId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private AlbumImagemDTO toDTO(AlbumImagem imagem) {
        String url = null;
        try {
            url = minioService.getPresignedUrl(imagem.getObjectKey());
        } catch (Exception e) {
            log.warn("Erro ao gerar URL para imagem: {}", imagem.getObjectKey());
        }

        return AlbumImagemDTO.builder()
                .id(imagem.getId())
                .albumId(imagem.getAlbum().getId())
                .nomeArquivo(imagem.getNomeArquivo())
                .url(url)
                .build();
    }
}
