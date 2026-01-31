package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.auth.ChangePasswordRequest;
import com.hygor.makeup_api.dto.user.UserResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.UserMapper;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class UserService extends BaseService<User, UserRepository> {

    private final PasswordEncoder passwordEncoder;
    private final MfaService mfaService;
    private final UserMapper userMapper;
    private final EmailService emailService; // Injeção do Serviço de E-mail

    public UserService(UserRepository repository,
                       PasswordEncoder passwordEncoder,
                       MfaService mfaService,
                       UserMapper userMapper,
                       EmailService emailService) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
        this.mfaService = mfaService;
        this.userMapper = userMapper;
        this.emailService = emailService;
    }

    /**
     * Retorna o perfil do utilizador atualmente logado.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User currentUser = getAuthenticatedUser();
        // O Mapper resolve as Roles e Endereços automaticamente 🪄
        return userMapper.toResponse(currentUser);
    }

    /**
     * Altera a senha do utilizador logado com validação da senha antiga.
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getAuthenticatedUser();

        // 1. Verifica se a senha antiga está correta
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("A senha atual está incorreta.");
        }

        // 2. Validação extra: nova senha igual à antiga
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
             throw new BusinessException("A nova senha não pode ser igual à anterior.");
        }

        // 3. Criptografa e guarda a nova senha
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
        
        log.info("Senha alterada com sucesso para o utilizador: {}", user.getEmail());
    }

    /**
     * Inicia o fluxo de recuperação de senha (Esqueci a Senha).
     */
    @Transactional
    public void forgotPassword(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("E-mail não encontrado no sistema."));

        // Gera um token numérico de 6 dígitos
        String token = String.format("%06d", (int) (Math.random() * 999999));
        
        // --- AGORA FUNCIONA! ---
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15)); // Expira em 15 min
        repository.save(user);
        // -----------------------

        // Envia o e-mail (Async) 📧
        emailService.sendRecoveryEmail(user, token);
        
        log.info("Token de recuperação enviado para: {}", email);
    }

    /**
     * Gera o QR Code para ativar o MFA.
     */
    @Transactional
    public String generateMfaQrCode() {
        User user = getAuthenticatedUser();
        
        // Gera e guarda o segredo no utilizador
        String secret = mfaService.generateNewSecret();
        user.setSecretMfa(secret);
        repository.save(user);
        
        log.info("Novo segredo MFA gerado para: {}", user.getEmail());
        return mfaService.getQrCodeUrl(secret, user.getEmail());
    }

    /**
     * Ativa o MFA após validar o código.
     */
    @Transactional
    public void enableMfa(int code) {
        User user = getAuthenticatedUser();
        
        // Verifica se o código que o user digitou está correto 🕵️‍♀️
        if (mfaService.verifyCode(user.getSecretMfa(), code)) {
            user.setMfaEnabled(true);
            repository.save(user);
            log.info("MFA ativado com sucesso para: {}", user.getEmail());
        } else {
            throw new BusinessException("Código MFA inválido. Tente novamente.");
        }
    }

    /**
     * Método auxiliar para obter o utilizador do contexto de segurança.
     */
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado no contexto de segurança."));
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = repository.findByResetToken(token) // Precisas adicionar findByResetToken no UserRepository
                .orElseThrow(() -> new BusinessException("Token inválido."));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException("O token expirou. Solicite um novo.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Limpa o token após uso
        user.setResetTokenExpiry(null);
        repository.save(user);
        
        log.info("Senha redefinida com sucesso via token para: {}", user.getEmail());
    }
}