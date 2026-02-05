package br.gov.mt.seplag.artistas.config;

import br.gov.mt.seplag.artistas.domain.entity.Usuario;
import br.gov.mt.seplag.artistas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        usuarioRepository.findByUsername("admin").ifPresent(admin -> {
            String novaSenha = passwordEncoder.encode("admin123");
            admin.setPassword(novaSenha);
            usuarioRepository.save(admin);
            log.info("[INIT] Senha do usuario admin atualizada com BCrypt valido");
        });
    }
}