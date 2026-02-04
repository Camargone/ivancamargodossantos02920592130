package br.gov.mt.seplag.artistas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumDTO {

    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    private String titulo;

    private LocalDateTime dataCriacao;

    @NotEmpty(message = "Deve ter pelo menos um artista associado")
    private Set<Long> artistaIds;

    private List<ArtistaDTO> artistas;

    private List<AlbumImagemDTO> imagens;
}
