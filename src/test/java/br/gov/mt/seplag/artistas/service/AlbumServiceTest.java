package br.gov.mt.seplag.artistas.service;

import br.gov.mt.seplag.artistas.domain.entity.Album;
import br.gov.mt.seplag.artistas.domain.entity.Artista;
import br.gov.mt.seplag.artistas.domain.entity.TipoArtista;
import br.gov.mt.seplag.artistas.dto.AlbumDTO;
import br.gov.mt.seplag.artistas.exception.ResourceNotFoundException;
import br.gov.mt.seplag.artistas.repository.AlbumRepository;
import br.gov.mt.seplag.artistas.repository.ArtistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private MinioService minioService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AlbumService albumService;

    private Album album;
    private Artista artista;
    private AlbumDTO albumDTO;

    @BeforeEach
    void setUp() {
        artista = Artista.builder()
                .id(1L)
                .nome("Serj Tankian")
                .tipo(TipoArtista.CANTOR)
                .build();

        album = Album.builder()
                .id(1L)
                .titulo("Harakiri")
                .dataCriacao(LocalDateTime.now())
                .artistas(new HashSet<>(Collections.singletonList(artista)))
                .imagens(new ArrayList<>())
                .build();

        albumDTO = AlbumDTO.builder()
                .titulo("Harakiri")
                .artistaIds(Set.of(1L))
                .build();
    }

    @Test
    @DisplayName("Deve listar todos os álbuns com paginação")
    void deveListarTodosAlbuns() {
        List<Album> albuns = Collections.singletonList(album);
        Page<Album> page = new PageImpl<>(albuns);

        when(albumRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<AlbumDTO> resultado = albumService.listarTodos(0, 10, "asc");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Harakiri");

        verify(albumRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar álbum por ID com sucesso")
    void deveBuscarAlbumPorId() {
        when(albumRepository.findByIdWithArtistas(1L)).thenReturn(Optional.of(album));

        AlbumDTO resultado = albumService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitulo()).isEqualTo("Harakiri");

        verify(albumRepository, times(1)).findByIdWithArtistas(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando álbum não encontrado")
    void deveLancarExcecaoQuandoAlbumNaoEncontrado() {
        when(albumRepository.findByIdWithArtistas(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Álbum não encontrado");

        verify(albumRepository, times(1)).findByIdWithArtistas(999L);
    }

    @Test
    @DisplayName("Deve buscar álbuns por tipo de artista")
    void deveBuscarAlbunsPorTipoArtista() {
        List<Album> albuns = Collections.singletonList(album);
        Page<Album> page = new PageImpl<>(albuns);

        when(albumRepository.findByTipoArtista(eq(TipoArtista.CANTOR), any(Pageable.class)))
                .thenReturn(page);

        Page<AlbumDTO> resultado = albumService.buscarPorTipoArtista("CANTOR", 0, 10, "asc");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);

        verify(albumRepository, times(1)).findByTipoArtista(eq(TipoArtista.CANTOR), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar álbuns por nome do artista")
    void deveBuscarAlbunsPorNomeArtista() {
        List<Album> albuns = Collections.singletonList(album);
        Page<Album> page = new PageImpl<>(albuns);

        when(albumRepository.findByArtistaNome(eq("Serj"), any(Pageable.class)))
                .thenReturn(page);

        Page<AlbumDTO> resultado = albumService.buscarPorNomeArtista("Serj", 0, 10, "asc");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);

        verify(albumRepository, times(1)).findByArtistaNome(eq("Serj"), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve criar álbum e notificar via WebSocket")
    void deveCriarAlbumENotificarWebSocket() {
        when(artistaRepository.findAllById(anySet())).thenReturn(Collections.singletonList(artista));
        when(albumRepository.save(any(Album.class))).thenReturn(album);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(AlbumDTO.class));

        AlbumDTO resultado = albumService.criar(albumDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("Harakiri");

        verify(artistaRepository, times(1)).findAllById(anySet());
        verify(albumRepository, times(1)).save(any(Album.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/novo-album"), any(AlbumDTO.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar álbum sem artistas válidos")
    void deveLancarExcecaoAoCriarAlbumSemArtistas() {
        when(artistaRepository.findAllById(anySet())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> albumService.criar(albumDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nenhum artista encontrado");

        verify(artistaRepository, times(1)).findAllById(anySet());
        verify(albumRepository, never()).save(any(Album.class));
    }

    @Test
    @DisplayName("Deve atualizar álbum com sucesso")
    void deveAtualizarAlbum() {
        AlbumDTO atualizacaoDTO = AlbumDTO.builder()
                .titulo("Harakiri Updated")
                .artistaIds(Set.of(1L))
                .build();

        Album albumAtualizado = Album.builder()
                .id(1L)
                .titulo("Harakiri Updated")
                .artistas(new HashSet<>(Collections.singletonList(artista)))
                .imagens(new ArrayList<>())
                .build();

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(artistaRepository.findAllById(anySet())).thenReturn(Collections.singletonList(artista));
        when(albumRepository.save(any(Album.class))).thenReturn(albumAtualizado);

        AlbumDTO resultado = albumService.atualizar(1L, atualizacaoDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("Harakiri Updated");

        verify(albumRepository, times(1)).findById(1L);
        verify(albumRepository, times(1)).save(any(Album.class));
    }
}
