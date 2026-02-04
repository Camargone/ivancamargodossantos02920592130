package br.gov.mt.seplag.artistas.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType; // "Bearer"
    private Long expiresIn;
}
