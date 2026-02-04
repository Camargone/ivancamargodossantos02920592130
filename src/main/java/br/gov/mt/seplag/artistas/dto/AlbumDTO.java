package br.gov.mt.seplag.artistas.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumDTO {
    
    private Long id;
    private String titulo;
    private LocalDateTime dataCriacao;
    private Set<ArtistaDTO> artistas;
}
