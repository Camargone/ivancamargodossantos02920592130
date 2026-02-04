package br.gov.mt.seplag.artistas.service;

import br.gov.mt.seplag.artistas.domain.entity.Regional;
import br.gov.mt.seplag.artistas.dto.RegionalDTO;
import br.gov.mt.seplag.artistas.dto.RegionalExternaDTO;
import br.gov.mt.seplag.artistas.repository.RegionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionalService {

    private final RegionalRepository regionalRepository;
    private final RestTemplate restTemplate;

    @Value("${external.regionais-api-url}")
    private String regionaisApiUrl;

    @Transactional(readOnly = true)
    public List<RegionalDTO> listarTodas() {
        return regionalRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegionalDTO> listarAtivas() {
        return regionalRepository.findByAtivoTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Sincroniza as regionais com a API externa conforme as regras:
     * 1) Novo no endpoint → inserir
     * 2) Ausente no endpoint → inativar
     * 3) Atributo alterado → inativar antigo e criar novo registro
     */
    @Transactional
    public SyncResult sincronizarComApiExterna() {
        log.info("Iniciando sincronização com API externa: {}", regionaisApiUrl);

        List<RegionalExternaDTO> regionaisExternas;
        try {
            ResponseEntity<List<RegionalExternaDTO>> response = restTemplate.exchange(
                    regionaisApiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RegionalExternaDTO>>() {}
            );
            regionaisExternas = response.getBody();

            if (regionaisExternas == null) {
                regionaisExternas = new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Erro ao buscar regionais da API externa: {}", e.getMessage());
            throw new RuntimeException("Erro ao sincronizar com API externa", e);
        }

        List<Regional> regionaisAtivas = regionalRepository.findAllByAtivoTrue();

        // Mapear regionais ativas por idExterno
        Map<Integer, Regional> regionaisAtivasMap = regionaisAtivas.stream()
                .collect(Collectors.toMap(Regional::getIdExterno, r -> r, (a, b) -> a));

        // Mapear regionais externas por id
        Set<Integer> idsExternos = regionaisExternas.stream()
                .map(RegionalExternaDTO::getId)
                .collect(Collectors.toSet());

        int inseridos = 0;
        int inativados = 0;
        int atualizados = 0;

        // Processar regionais da API externa
        for (RegionalExternaDTO externa : regionaisExternas) {
            Regional existente = regionaisAtivasMap.get(externa.getId());

            if (existente == null) {
                // Regra 1: Novo no endpoint → inserir
                Regional nova = Regional.builder()
                        .idExterno(externa.getId())
                        .nome(externa.getNome())
                        .ativo(true)
                        .build();
                regionalRepository.save(nova);
                inseridos++;
                log.info("Regional inserida: id_externo={}, nome={}", externa.getId(), externa.getNome());
            } else if (!existente.getNome().equals(externa.getNome())) {
                // Regra 3: Atributo alterado → inativar antigo e criar novo registro
                existente.setAtivo(false);
                regionalRepository.save(existente);

                Regional nova = Regional.builder()
                        .idExterno(externa.getId())
                        .nome(externa.getNome())
                        .ativo(true)
                        .build();
                regionalRepository.save(nova);
                atualizados++;
                log.info("Regional atualizada (inativada antiga e criada nova): id_externo={}, nome_antigo={}, nome_novo={}",
                        externa.getId(), existente.getNome(), externa.getNome());
            }
        }

        // Regra 2: Ausente no endpoint → inativar
        for (Regional regional : regionaisAtivas) {
            if (!idsExternos.contains(regional.getIdExterno())) {
                regional.setAtivo(false);
                regionalRepository.save(regional);
                inativados++;
                log.info("Regional inativada (ausente na API): id_externo={}, nome={}",
                        regional.getIdExterno(), regional.getNome());
            }
        }

        log.info("Sincronização concluída: inseridos={}, atualizados={}, inativados={}",
                inseridos, atualizados, inativados);

        return new SyncResult(inseridos, atualizados, inativados);
    }

    private RegionalDTO toDTO(Regional regional) {
        return RegionalDTO.builder()
                .id(regional.getId())
                .idExterno(regional.getIdExterno())
                .nome(regional.getNome())
                .ativo(regional.getAtivo())
                .build();
    }

    public record SyncResult(int inseridos, int atualizados, int inativados) {}
}
