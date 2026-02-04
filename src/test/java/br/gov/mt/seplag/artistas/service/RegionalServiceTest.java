package br.gov.mt.seplag.artistas.service;

import br.gov.mt.seplag.artistas.domain.entity.Regional;
import br.gov.mt.seplag.artistas.dto.RegionalDTO;
import br.gov.mt.seplag.artistas.dto.RegionalExternaDTO;
import br.gov.mt.seplag.artistas.repository.RegionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegionalServiceTest {

    @Mock
    private RegionalRepository regionalRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RegionalService regionalService;

    private Regional regional;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(regionalService, "regionaisApiUrl", "https://api.example.com/regionais");

        regional = Regional.builder()
                .id(1L)
                .idExterno(100)
                .nome("Regional Norte")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve listar todas as regionais")
    void deveListarTodasRegionais() {
        when(regionalRepository.findAll()).thenReturn(Collections.singletonList(regional));

        List<RegionalDTO> resultado = regionalService.listarTodas();

        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Regional Norte");

        verify(regionalRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve listar apenas regionais ativas")
    void deveListarRegionaisAtivas() {
        when(regionalRepository.findByAtivoTrue()).thenReturn(Collections.singletonList(regional));

        List<RegionalDTO> resultado = regionalService.listarAtivas();

        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getAtivo()).isTrue();

        verify(regionalRepository, times(1)).findByAtivoTrue();
    }

    @Test
    @DisplayName("Deve inserir nova regional quando não existe no banco")
    void deveInserirNovaRegional() {
        RegionalExternaDTO externa = RegionalExternaDTO.builder()
                .id(200)
                .nome("Regional Sul")
                .build();

        ResponseEntity<List<RegionalExternaDTO>> response = 
                new ResponseEntity<>(Collections.singletonList(externa), HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                isNull(), 
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        when(regionalRepository.findAllByAtivoTrue()).thenReturn(Collections.emptyList());
        when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegionalService.SyncResult resultado = regionalService.sincronizarComApiExterna();

        assertThat(resultado.inseridos()).isEqualTo(1);
        assertThat(resultado.atualizados()).isEqualTo(0);
        assertThat(resultado.inativados()).isEqualTo(0);

        verify(regionalRepository, times(1)).save(any(Regional.class));
    }

    @Test
    @DisplayName("Deve inativar regional quando ausente na API externa")
    void deveInativarRegionalAusente() {
        ResponseEntity<List<RegionalExternaDTO>> response = 
                new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                isNull(), 
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        when(regionalRepository.findAllByAtivoTrue()).thenReturn(Collections.singletonList(regional));
        when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegionalService.SyncResult resultado = regionalService.sincronizarComApiExterna();

        assertThat(resultado.inseridos()).isEqualTo(0);
        assertThat(resultado.atualizados()).isEqualTo(0);
        assertThat(resultado.inativados()).isEqualTo(1);

        verify(regionalRepository, times(1)).save(argThat(r -> !r.getAtivo()));
    }

    @Test
    @DisplayName("Deve inativar antiga e criar nova quando nome muda")
    void deveAtualizarQuandoNomeMuda() {
        RegionalExternaDTO externa = RegionalExternaDTO.builder()
                .id(100)
                .nome("Regional Norte Atualizada")
                .build();

        ResponseEntity<List<RegionalExternaDTO>> response = 
                new ResponseEntity<>(Collections.singletonList(externa), HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                isNull(), 
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        when(regionalRepository.findAllByAtivoTrue()).thenReturn(Collections.singletonList(regional));
        when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegionalService.SyncResult resultado = regionalService.sincronizarComApiExterna();

        assertThat(resultado.inseridos()).isEqualTo(0);
        assertThat(resultado.atualizados()).isEqualTo(1);
        assertThat(resultado.inativados()).isEqualTo(0);

        // Deve salvar duas vezes: uma para inativar, outra para criar nova
        verify(regionalRepository, times(2)).save(any(Regional.class));
    }

    @Test
    @DisplayName("Deve não alterar quando dados são iguais")
    void deveNaoAlterarQuandoDadosIguais() {
        RegionalExternaDTO externa = RegionalExternaDTO.builder()
                .id(100)
                .nome("Regional Norte")
                .build();

        ResponseEntity<List<RegionalExternaDTO>> response = 
                new ResponseEntity<>(Collections.singletonList(externa), HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                isNull(), 
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        when(regionalRepository.findAllByAtivoTrue()).thenReturn(Collections.singletonList(regional));

        RegionalService.SyncResult resultado = regionalService.sincronizarComApiExterna();

        assertThat(resultado.inseridos()).isEqualTo(0);
        assertThat(resultado.atualizados()).isEqualTo(0);
        assertThat(resultado.inativados()).isEqualTo(0);

        verify(regionalRepository, never()).save(any(Regional.class));
    }

    @Test
    @DisplayName("Deve processar múltiplas operações na sincronização")
    void deveProcessarMultiplasOperacoes() {
        // Regional existente que será inativada (id_externo=100)
        Regional regionalExistente = Regional.builder()
                .id(1L)
                .idExterno(100)
                .nome("Regional Norte")
                .ativo(true)
                .build();

        // Regional existente que terá nome alterado (id_externo=200)
        Regional regionalAlterada = Regional.builder()
                .id(2L)
                .idExterno(200)
                .nome("Regional Sul")
                .ativo(true)
                .build();

        // Dados da API externa
        List<RegionalExternaDTO> externas = Arrays.asList(
                RegionalExternaDTO.builder().id(200).nome("Regional Sul Atualizada").build(),
                RegionalExternaDTO.builder().id(300).nome("Regional Oeste Nova").build()
        );

        ResponseEntity<List<RegionalExternaDTO>> response = 
                new ResponseEntity<>(externas, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                isNull(), 
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        when(regionalRepository.findAllByAtivoTrue())
                .thenReturn(Arrays.asList(regionalExistente, regionalAlterada));
        when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegionalService.SyncResult resultado = regionalService.sincronizarComApiExterna();

        // id_externo=300 é nova (1 inserção)
        // id_externo=200 teve nome alterado (1 atualização = inativar + criar)
        // id_externo=100 não está na API (1 inativação)
        assertThat(resultado.inseridos()).isEqualTo(1);
        assertThat(resultado.atualizados()).isEqualTo(1);
        assertThat(resultado.inativados()).isEqualTo(1);
    }
}
