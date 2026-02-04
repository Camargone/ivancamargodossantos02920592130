package br.gov.mt.seplag.artistas.security;

import br.gov.mt.seplag.artistas.domain.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Configurar valores de teste
        ReflectionTestUtils.setField(jwtService, "secretKey", 
                "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tMjU2LWJpdHMtbWluaW1vLWZvci10ZXN0aW5n");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 300000L); // 5 minutos
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 1800000L); // 30 minutos

        usuario = Usuario.builder()
                .id(1L)
                .username("admin")
                .password("$2a$10$encoded")
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Deve gerar access token válido")
    void deveGerarAccessTokenValido() {
        String token = jwtService.generateAccessToken(usuario);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("Deve gerar refresh token válido")
    void deveGerarRefreshTokenValido() {
        String token = jwtService.generateRefreshToken(usuario);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.isRefreshToken(token)).isTrue();
    }

    @Test
    @DisplayName("Deve identificar corretamente refresh token")
    void deveIdentificarRefreshToken() {
        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        assertThat(jwtService.isRefreshToken(accessToken)).isFalse();
        assertThat(jwtService.isRefreshToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("Deve validar token corretamente")
    void deveValidarToken() {
        String token = jwtService.generateAccessToken(usuario);

        boolean isValid = jwtService.isTokenValid(token, usuario);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve extrair username do token")
    void deveExtrairUsername() {
        String token = jwtService.generateAccessToken(usuario);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("admin");
    }

    @Test
    @DisplayName("Deve retornar false para token inválido")
    void deveRetornarFalseParaTokenInvalido() {
        String tokenInvalido = "invalid.token.here";

        boolean isValid = jwtService.isTokenValid(tokenInvalido, usuario);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando username não corresponde")
    void deveRetornarFalseQuandoUsernameNaoCorresponde() {
        String token = jwtService.generateAccessToken(usuario);

        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .username("outro")
                .password("$2a$10$encoded")
                .enabled(true)
                .build();

        boolean isValid = jwtService.isTokenValid(token, outroUsuario);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve retornar tempo de expiração correto")
    void deveRetornarTempoExpiracaoCorreto() {
        long expiration = jwtService.getAccessTokenExpiration();

        assertThat(expiration).isEqualTo(300000L);
    }

    @Test
    @DisplayName("Access token não deve ser identificado como refresh token")
    void accessTokenNaoDeveSerRefreshToken() {
        String accessToken = jwtService.generateAccessToken(usuario);

        assertThat(jwtService.isRefreshToken(accessToken)).isFalse();
    }
}
