package br.gov.mt.seplag.artistas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumImagemDTO {

    private Long id;
    private Long albumId;
    private String nomeArquivo;
    private String url;
}
