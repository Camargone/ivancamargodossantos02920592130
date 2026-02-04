package br.gov.mt.seplag.artistas.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "album_imagem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumImagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "nome_arquivo", length = 255)
    private String nomeArquivo;

    @Column(name = "object_key", length = 500)
    private String objectKey;
}
