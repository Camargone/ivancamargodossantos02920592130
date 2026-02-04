package br.gov.mt.seplag.artistas.controller;

import br.gov.mt.seplag.artistas.domain.entity.TipoArtista;
import br.gov.mt.seplag.artistas.dto.ArtistaDTO;
import br.gov.mt.seplag.artistas.service.ArtistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artistas")
@RequiredArgsConstructor
@Tag(name = "Artistas", description = "API para gerenciamento de artistas (cantores e bandas)")
@SecurityRequirement(name = "bearerAuth")
public class ArtistaController {

    private final ArtistaService artistaService;

    @GetMapping
    @Operation(summary = "Listar todos os artistas", description = "Retorna uma lista paginada de artistas com opções de filtro e ordenação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de artistas retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<Page<ArtistaDTO>> listarTodos(
            @Parameter(description = "Número da página (começando em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Direção da ordenação (asc ou desc)") @RequestParam(defaultValue = "asc") String sort,
            @Parameter(description = "Filtrar por nome do artista") @RequestParam(required = false) String nome,
            @Parameter(description = "Filtrar por tipo (CANTOR ou BANDA)") @RequestParam(required = false) String tipo) {

        Page<ArtistaDTO> artistas;

        if (nome != null && !nome.isEmpty()) {
            artistas = artistaService.buscarPorNome(nome, page, size, sort);
        } else if (tipo != null && !tipo.isEmpty()) {
            TipoArtista tipoArtista = TipoArtista.valueOf(tipo.toUpperCase());
            artistas = artistaService.buscarPorTipo(tipoArtista, page, size, sort);
        } else {
            artistas = artistaService.listarTodos(page, size, sort);
        }

        return ResponseEntity.ok(artistas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar artista por ID", description = "Retorna um artista específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artista encontrado"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<ArtistaDTO> buscarPorId(
            @Parameter(description = "ID do artista") @PathVariable Long id) {
        return ResponseEntity.ok(artistaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo artista", description = "Cria um novo artista no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Artista criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<ArtistaDTO> criar(
            @Parameter(description = "Dados do artista") @Valid @RequestBody ArtistaDTO artistaDTO) {
        ArtistaDTO criado = artistaService.criar(artistaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar artista", description = "Atualiza os dados de um artista existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artista atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<ArtistaDTO> atualizar(
            @Parameter(description = "ID do artista") @PathVariable Long id,
            @Parameter(description = "Dados atualizados do artista") @Valid @RequestBody ArtistaDTO artistaDTO) {
        return ResponseEntity.ok(artistaService.atualizar(id, artistaDTO));
    }
}
