package br.gov.mt.seplag.artistas.controller;

import br.gov.mt.seplag.artistas.dto.RegionalDTO;
import br.gov.mt.seplag.artistas.service.RegionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/regionais")
@RequiredArgsConstructor
@Tag(name = "Regionais", description = "API para gerenciamento de regionais (sincronização com API externa)")
@SecurityRequirement(name = "bearerAuth")
public class RegionalController {

    private final RegionalService regionalService;

    @GetMapping
    @Operation(summary = "Listar todas as regionais", description = "Retorna todas as regionais cadastradas no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de regionais retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<RegionalDTO>> listarTodas() {
        return ResponseEntity.ok(regionalService.listarTodas());
    }

    @GetMapping("/ativas")
    @Operation(summary = "Listar regionais ativas", description = "Retorna apenas as regionais com status ativo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de regionais ativas retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<RegionalDTO>> listarAtivas() {
        return ResponseEntity.ok(regionalService.listarAtivas());
    }

    @PostMapping("/sincronizar")
    @Operation(summary = "Sincronizar com API externa",
            description = "Sincroniza as regionais com a API externa. Regras: " +
                    "1) Novo no endpoint → inserir; " +
                    "2) Ausente no endpoint → inativar; " +
                    "3) Atributo alterado → inativar antigo e criar novo registro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sincronização realizada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao sincronizar com API externa"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<Map<String, Object>> sincronizar() {
        RegionalService.SyncResult result = regionalService.sincronizarComApiExterna();
        return ResponseEntity.ok(Map.of(
                "message", "Sincronização realizada com sucesso",
                "inseridos", result.inseridos(),
                "atualizados", result.atualizados(),
                "inativados", result.inativados()
        ));
    }
}
