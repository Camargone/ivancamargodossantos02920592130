package br.gov.mt.seplag.artistas.dto;

import br.gov.mt.seplag.artistas.domain.entity.TipoArtista;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistaDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nome;

    @NotNull(message = "Tipo é obrigatório (CANTOR ou BANDA)")
    private TipoArtista tipo;
}
