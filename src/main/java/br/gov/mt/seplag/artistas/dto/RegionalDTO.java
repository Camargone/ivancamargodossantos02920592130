package br.gov.mt.seplag.artistas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionalDTO {

    private Long id;
    private Integer idExterno;
    private String nome;
    private Boolean ativo;
}
