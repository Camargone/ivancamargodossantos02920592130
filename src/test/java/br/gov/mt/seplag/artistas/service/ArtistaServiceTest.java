package br.gov.mt.seplag.artistas.service;

import br.gov.mt.seplag.artistas.domain.entity.Artista;
import br.gov.mt.seplag.artistas.domain.entity.TipoArtista;
import br.gov.mt.seplag.artistas.dto.ArtistaDTO;
import br.gov.mt.seplag.artistas.exception.ResourceNotFoundException;
import br.gov.mt.seplag.artistas.repository.ArtistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistaServiceTest {

    @Mock
    private ArtistaRepository artistaRepository;

    @InjectMocks
    private ArtistaService artistaService;

    private Artista artista;
    private ArtistaDTO artistaDTO;

    @BeforeEach
    void setUp() {
        artista = Artista.builder()
                .id(1L)
                .nome("Serj Tankian")
                .tipo(TipoArtista.CANTOR)
                .build();

        artistaDTO = ArtistaDTO.builder()
                .nome("Serj Tankian")
                .tipo(TipoArtista.CANTOR)
                .build();
    }

    @Test
    @DisplayName("Deve listar todos os artistas com paginação")
    void deveListarTodosArtistas() {
        List<Artista> artistas = Arrays.asList(artista);
        Page<Artista> page = new PageImpl<>(artistas);

        when(artistaRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ArtistaDTO> resultado = artistaService.listarTodos(0, 10, "asc");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Serj Tankian");

        verify(artistaRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar artista por ID com sucesso")
    void deveBuscarArtistaPorId() {
        when(artistaRepository.findById(1L)).thenReturn(Optional.of(artista));

        ArtistaDTO resultado = artistaService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Serj Tankian");
        assertThat(resultado.getTipo()).isEqualTo(TipoArtista.CANTOR);

        verify(artistaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando artista não encontrado")
    void deveLancarExcecaoQuandoArtistaNaoEncontrado() {
        when(artistaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistaService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artista não encontrado");

        verify(artistaRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve buscar artistas por nome")
    void deveBuscarArtistasPorNome() {
        List<Artista> artistas = Arrays.asList(artista);
        Page<Artista> page = new PageImpl<>(artistas);

        when(artistaRepository.findByNomeContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(page);

        Page<ArtistaDTO> resultado = artistaService.buscarPorNome("Serj", 0, 10, "asc");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).contains("Serj");

        verify(artistaRepository, times(1)).findByNomeContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar artistas por tipo")
    void deveBuscarArtistasPorTipo() {
        List<Artista> artistas = Arrays.asList(artista);
        Page<Artista> page = new PageImpl<>(artistas);

        when(artistaRepository.findByTipo(eq(TipoArtista.CANTOR), any(Pageable.class)))
                .thenReturn(page);

        Page<ArtistaDTO> resultado = artistaService.buscarPorTipo(TipoArtista.CANTOR, 0, 10, "asc");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getTipo()).isEqualTo(TipoArtista.CANTOR);

        verify(artistaRepository, times(1)).findByTipo(eq(TipoArtista.CANTOR), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve criar artista com sucesso")
    void deveCriarArtista() {
        when(artistaRepository.save(any(Artista.class))).thenReturn(artista);

        ArtistaDTO resultado = artistaService.criar(artistaDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Serj Tankian");
        assertThat(resultado.getTipo()).isEqualTo(TipoArtista.CANTOR);

        verify(artistaRepository, times(1)).save(any(Artista.class));
    }

    @Test
    @DisplayName("Deve atualizar artista com sucesso")
    void deveAtualizarArtista() {
        ArtistaDTO atualizacaoDTO = ArtistaDTO.builder()
                .nome("Serj Tankian Updated")
                .tipo(TipoArtista.CANTOR)
                .build();

        Artista artistaAtualizado = Artista.builder()
                .id(1L)
                .nome("Serj Tankian Updated")
                .tipo(TipoArtista.CANTOR)
                .build();

        when(artistaRepository.findById(1L)).thenReturn(Optional.of(artista));
        when(artistaRepository.save(any(Artista.class))).thenReturn(artistaAtualizado);

        ArtistaDTO resultado = artistaService.atualizar(1L, atualizacaoDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Serj Tankian Updated");

        verify(artistaRepository, times(1)).findById(1L);
        verify(artistaRepository, times(1)).save(any(Artista.class));
    }

    @Test
    @DisplayName("Deve ordenar artistas em ordem descendente")
    void deveOrdenarArtistasDescendente() {
        List<Artista> artistas = Arrays.asList(artista);
        Page<Artista> page = new PageImpl<>(artistas);

        when(artistaRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ArtistaDTO> resultado = artistaService.listarTodos(0, 10, "desc");

        assertThat(resultado).isNotNull();
        verify(artistaRepository, times(1)).findAll(any(Pageable.class));
    }
}
