package br.gov.mt.seplag.artistas.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {
    private String refreshToken;
}
