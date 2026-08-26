package com.astra.user;

import com.astra.shared.CurrentUserProvider;
import com.astra.shared.event.UserRegisteredEvent;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import com.astra.shared.exception.UnauthorizedException;
import com.astra.user.dto.AdminUserResponse;
import com.astra.user.dto.AuthInfo;
import com.astra.user.dto.AvatarData;
import com.astra.user.dto.LoginRequest;
import com.astra.user.dto.RegisterRequest;
import com.astra.user.dto.UpdateProfileRequest;
import com.astra.user.dto.UserResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordBreachChecker passwordBreachChecker;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       CurrentUserProvider currentUserProvider,
                       ApplicationEventPublisher eventPublisher,
                       PasswordBreachChecker passwordBreachChecker) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserProvider = currentUserProvider;
        this.eventPublisher = eventPublisher;
        this.passwordBreachChecker = passwordBreachChecker;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (passwordBreachChecker.isBreached(request.password())) {
            throw new ConflictException("Essa senha já apareceu em vazamentos conhecidos - escolha outra");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email already registered");
        }
        User user = new User(request.name(), request.email(), passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId()));
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return toDto(user);
    }

    // Usado por /me e por /auth/refresh (pra devolver o usuário junto do
    // token novo, sem o front precisar de uma segunda chamada a /me).
    @Transactional(readOnly = true)
    public UserResponse get(UUID userId) {
        return userRepository.findSummaryById(userId)
                .map(v -> new UserResponse(v.getId(), v.getName(), v.getEmail(), v.getBio(), v.getRole()))
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return get(currentUserProvider.currentUserId());
    }

    @Transactional(readOnly = true)
    public Map<UUID, String> namesByIds(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userRepository.findNameBioByIdIn(ids).stream()
                .collect(Collectors.toMap(UserRepository.NameBioView::getId, UserRepository.NameBioView::getName));
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
        user.setName(request.name());
        user.setBio(request.bio());
        return toDto(user);
    }

    @Transactional
    public void updateAvatar(MultipartFile file) {
        UUID userId = currentUserProvider.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ConflictException("Arquivo precisa ser uma imagem");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ConflictException("Imagem precisa ter até 2MB");
        }

        try {
            user.setAvatar(file.getBytes());
            user.setAvatarContentType(contentType);
        } catch (IOException ex) {
            throw new ConflictException("Não foi possível ler o arquivo");
        }
    }

    @Transactional(readOnly = true)
    public AvatarData avatar(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Não encontrado"));
        if (user.getAvatar() == null) {
            throw new NotFoundException("Não encontrado");
        }
        return new AvatarData(user.getAvatar(), user.getAvatarContentType());
    }

    @Transactional(readOnly = true)
    public AuthInfo authInfo(UUID userId) {
        return userRepository.findAuthInfoById(userId)
                .map(v -> new AuthInfo(v.getRole(), v.getBannedAt() != null))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAllAdminSummaries().stream()
                .map(v -> new AdminUserResponse(v.getId(), v.getName(), v.getEmail(), v.getRole(),
                        v.getBannedAt() != null, v.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void ban(UUID targetId) {
        UUID me = currentUserProvider.currentUserId();
        if (targetId.equals(me)) {
            throw new ConflictException("Não é possível banir a própria conta");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        target.setBannedAt(OffsetDateTime.now());
    }

    @Transactional
    public void unban(UUID targetId) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        target.setBannedAt(null);
    }

    @Transactional
    public void delete(UUID targetId) {
        UUID me = currentUserProvider.currentUserId();
        if (targetId.equals(me)) {
            throw new ConflictException("Não é possível excluir a própria conta");
        }
        if (!userRepository.existsById(targetId)) {
            throw new NotFoundException("Usuário não encontrado");
        }
        userRepository.deleteById(targetId);
    }

    private UserResponse toDto(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getBio(), user.getRole());
    }
}
