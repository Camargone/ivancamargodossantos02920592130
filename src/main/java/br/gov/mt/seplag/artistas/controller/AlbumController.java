package br.gov.mt.seplag.artistas.controller;

import br.gov.mt.seplag.artistas.dto.AlbumDTO;
import br.gov.mt.seplag.artistas.dto.AlbumImagemDTO;
import br.gov.mt.seplag.artistas.service.AlbumService;
import br.gov.mt.seplag.artistas.service.AlbumImagemService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albuns")
@RequiredArgsConstructor
@Tag(name = "Álbuns", description = "API para gerenciamento de álbuns musicais")
@SecurityRequirement(name = "bearerAuth")
public class AlbumController {

    private final AlbumService albumService;
    private final AlbumImagemService albumImagemService;

    @GetMapping
    @Operation(summary = "Listar todos os álbuns", description = "Retorna uma lista paginada de álbuns com opções de filtro por tipo de artista")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de álbuns retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<Page<AlbumDTO>> listarTodos(
            @Parameter(description = "Número da página (começando em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Direção da ordenação (asc ou desc)") @RequestParam(defaultValue = "asc") String sort,
            @Parameter(description = "Filtrar por tipo de artista (CANTOR ou BANDA)") @RequestParam(required = false) String tipo,
            @Parameter(description = "Filtrar por nome do artista") @RequestParam(required = false) String artista) {

        Page<AlbumDTO> albuns;

        if (tipo != null && !tipo.isEmpty()) {
            albuns = albumService.buscarPorTipoArtista(tipo, page, size, sort);
        } else if (artista != null && !artista.isEmpty()) {
            albuns = albumService.buscarPorNomeArtista(artista, page, size, sort);
        } else {
            albuns = albumService.listarTodos(page, size, sort);
        }

        return ResponseEntity.ok(albuns);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar álbum por ID", description = "Retorna um álbum específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Álbum encontrado"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<AlbumDTO> buscarPorId(
            @Parameter(description = "ID do álbum") @PathVariable Long id) {
        return ResponseEntity.ok(albumService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo álbum", description = "Cria um novo álbum no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Álbum criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<AlbumDTO> criar(
            @Parameter(description = "Dados do álbum") @Valid @RequestBody AlbumDTO albumDTO) {
        AlbumDTO criado = albumService.criar(albumDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar álbum", description = "Atualiza os dados de um álbum existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Álbum atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<AlbumDTO> atualizar(
            @Parameter(description = "ID do álbum") @PathVariable Long id,
            @Parameter(description = "Dados atualizados do álbum") @Valid @RequestBody AlbumDTO albumDTO) {
        return ResponseEntity.ok(albumService.atualizar(id, albumDTO));
    }

    @PostMapping(value = "/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de imagens de capa", description = "Faz upload de uma ou mais imagens de capa para o álbum")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Imagens enviadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<AlbumImagemDTO>> uploadImagens(
            @Parameter(description = "ID do álbum") @PathVariable Long id,
            @Parameter(description = "Arquivos de imagem") @RequestParam("files") MultipartFile[] files) {
        List<AlbumImagemDTO> imagens = albumImagemService.uploadImagens(id, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(imagens);
    }

    @GetMapping("/{id}/imagens")
    @Operation(summary = "Listar imagens do álbum", description = "Retorna todas as imagens de capa de um álbum com URLs pré-assinadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de imagens retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<AlbumImagemDTO>> listarImagens(
            @Parameter(description = "ID do álbum") @PathVariable Long id) {
        return ResponseEntity.ok(albumImagemService.listarImagensDoAlbum(id));
    }
}
