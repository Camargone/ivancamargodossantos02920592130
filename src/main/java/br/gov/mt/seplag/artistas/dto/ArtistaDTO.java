package br.gov.mt.seplag.artistas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistaDTO {

    private Long id;
    private String nome;
    private String tipo; // "CANTOR" ou "BANDA"
}