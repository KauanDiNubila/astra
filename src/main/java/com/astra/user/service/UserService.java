package com.astra.user.service;

import com.astra.shared.CurrentUserProvider;
import com.astra.shared.event.UserRegisteredEvent;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import com.astra.shared.exception.UnauthorizedException;
import com.astra.user.dto.AdminUserResponse;
import com.astra.user.dto.AuthInfo;
import com.astra.user.dto.AvatarData;
import com.astra.user.TermsPolicy;
import com.astra.user.dto.ChangePasswordRequest;
import com.astra.user.dto.DeleteAccountRequest;
import com.astra.user.dto.LoginRequest;
import com.astra.user.dto.RegisterRequest;
import com.astra.user.dto.UpdateProfileRequest;
import com.astra.user.dto.UserResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.astra.user.entity.OAuthConnection;
import com.astra.user.entity.User;
import com.astra.user.repository.OAuthConnectionRepository;
import com.astra.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OAuthConnectionRepository oAuthConnectionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordBreachChecker passwordBreachChecker;
    private final UserTagGenerator userTagGenerator;

    public UserService(UserRepository userRepository, OAuthConnectionRepository oAuthConnectionRepository,
                       PasswordEncoder passwordEncoder,
                       CurrentUserProvider currentUserProvider,
                       ApplicationEventPublisher eventPublisher,
                       PasswordBreachChecker passwordBreachChecker,
                       UserTagGenerator userTagGenerator) {
        this.userRepository = userRepository;
        this.oAuthConnectionRepository = oAuthConnectionRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserProvider = currentUserProvider;
        this.eventPublisher = eventPublisher;
        this.passwordBreachChecker = passwordBreachChecker;
        this.userTagGenerator = userTagGenerator;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (passwordBreachChecker.isBreached(request.password())) {
            throw new ConflictException("Essa senha já apareceu em vazamentos conhecidos - escolha outra");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email already registered");
        }
        String tag = userTagGenerator.generate(request.name());
        User user = new User(request.name(), request.email(), passwordEncoder.encode(request.password()), tag);
        if (Boolean.TRUE.equals(request.acceptTerms())) {
            user.acceptTerms(TermsPolicy.CURRENT_VERSION);
        }
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId()));
        return toDto(saved);
    }

    @Transactional
    public UUID oauthLogin(String provider, String providerUserId, String email, String name) {
        Optional<OAuthConnection> existingConnection =
                oAuthConnectionRepository.findByProviderAndProviderUserId(provider, providerUserId);
        if (existingConnection.isPresent()) {
            return existingConnection.get().getUserId();
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String tag = userTagGenerator.generate(name);
            User fromOAuth = User.createFromOAuth(name, email, tag);
            fromOAuth.acceptTerms(TermsPolicy.CURRENT_VERSION);
            User created = userRepository.save(fromOAuth);
            eventPublisher.publishEvent(new UserRegisteredEvent(created.getId()));
            return created;
        });

        oAuthConnectionRepository.save(new OAuthConnection(user.getId(), provider, providerUserId));
        return user.getId();
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
                .map(v -> new UserResponse(v.getId(), v.getName(), v.getEmail(), v.getBio(), v.getRole(), v.getTag(),
                        v.getHasPassword(), TermsPolicy.CURRENT_VERSION.equals(v.getTermsVersion())))
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return get(currentUserProvider.currentUserId());
    }

    @Transactional(readOnly = true)
    public Map<UUID, UserRepository.NameBioView> nameBioByIds(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userRepository.findNameBioByIdIn(ids).stream()
                .collect(Collectors.toMap(UserRepository.NameBioView::getId, v -> v));
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
        String newName = request.name();
        if (!newName.equals(user.getName())) {
            user.setTag(userTagGenerator.generateOrKeep(newName, user.getTag()));
        }
        user.setName(newName);
        user.setBio(request.bio());
        return toDto(user);
    }

    @Transactional
    public UserResponse acceptTerms() {
        User user = loadActor();
        user.acceptTerms(TermsPolicy.CURRENT_VERSION);
        return toDto(user);
    }

    @Transactional
    public void deleteOwnAccount(DeleteAccountRequest request) {
        User user = loadActor();
        if (User.ROLE_OWNER.equals(user.getRole())) {
            throw new ConflictException("A conta owner não pode ser excluída por aqui");
        }
        if (user.getPasswordHash() != null) {
            if (request.password() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new ConflictException("Senha incorreta");
            }
        } else if (request.confirmation() == null
                || !request.confirmation().trim().equalsIgnoreCase(user.getEmail())) {
            throw new ConflictException("Digite o e-mail da sua conta para confirmar");
        }
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportData(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("tag", user.getTag());
        profile.put("bio", user.getBio());
        profile.put("role", user.getRole());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("hasPassword", user.getPasswordHash() != null);
        profile.put("termsAcceptedAt", user.getTermsAcceptedAt());
        profile.put("termsVersion", user.getTermsVersion());
        profile.put("loginProviders", oAuthConnectionRepository.findByUserId(userId).stream()
                .map(connection -> Map.of("provider", connection.getProvider(),
                        "connectedAt", connection.getCreatedAt()))
                .toList());
        if (user.getAvatar() != null) {
            profile.put("avatar", Map.of("contentType", user.getAvatarContentType(),
                    "base64", Base64.getEncoder().encodeToString(user.getAvatar())));
        }
        return profile;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
        if (user.getPasswordHash() == null) {
            throw new ConflictException("Essa conta não tem senha definida (entrou via Google/GitHub)");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ConflictException("Senha atual incorreta");
        }
        if (passwordBreachChecker.isBreached(request.newPassword())) {
            throw new ConflictException("Essa senha já apareceu em vazamentos conhecidos - escolha outra");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
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
    public Optional<AvatarData> avatar(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getAvatar() != null)
                .map(user -> new AvatarData(user.getAvatar(), user.getAvatarContentType()));
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
        User actor = loadActor();
        if (targetId.equals(actor.getId())) {
            throw new ConflictException("Não é possível banir a própria conta");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        requireStrictlyHigherRank(actor, target, "banir");
        target.setBannedAt(OffsetDateTime.now());
    }

    @Transactional
    public void unban(UUID targetId) {
        User actor = loadActor();
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        requireStrictlyHigherRank(actor, target, "desbanir");
        target.setBannedAt(null);
    }

    @Transactional
    public void delete(UUID targetId) {
        User actor = loadActor();
        if (targetId.equals(actor.getId())) {
            throw new ConflictException("Não é possível excluir a própria conta");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        requireStrictlyHigherRank(actor, target, "excluir");
        userRepository.deleteById(targetId);
    }

    @Transactional
    public void promote(UUID targetId) {
        User actor = loadActor();
        if (!User.ROLE_OWNER.equals(actor.getRole())) {
            throw new ConflictException("Apenas o owner pode promover usuários a admin");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (!User.ROLE_USER.equals(target.getRole())) {
            throw new ConflictException("Só é possível promover usuários comuns a admin");
        }
        target.setRole(User.ROLE_ADMIN);
    }

    @Transactional
    public void demote(UUID targetId) {
        User actor = loadActor();
        if (!User.ROLE_OWNER.equals(actor.getRole())) {
            throw new ConflictException("Apenas o owner pode remover admins");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (!User.ROLE_ADMIN.equals(target.getRole())) {
            throw new ConflictException("Só é possível remover privilégios de administradores");
        }
        target.setRole(User.ROLE_USER);
    }

    private User loadActor() {
        UUID me = currentUserProvider.currentUserId();
        return userRepository.findById(me)
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }

    private void requireStrictlyHigherRank(User actor, User target, String action) {
        if (User.rankOf(actor.getRole()) <= User.rankOf(target.getRole())) {
            throw new ConflictException("Você não tem permissão para " + action + " esse usuário");
        }
    }

    private UserResponse toDto(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getBio(), user.getRole(),
                user.getTag(), user.getPasswordHash() != null,
                TermsPolicy.CURRENT_VERSION.equals(user.getTermsVersion()));
    }
}
