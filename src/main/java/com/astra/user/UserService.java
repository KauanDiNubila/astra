package com.astra.user;

import com.astra.shared.CurrentUserProvider;
import com.astra.shared.event.UserRegisteredEvent;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import com.astra.shared.exception.UnauthorizedException;
import com.astra.shared.security.JwtService;
import com.astra.user.dto.AvatarData;
import com.astra.user.dto.LoginRequest;
import com.astra.user.dto.RegisterRequest;
import com.astra.user.dto.UpdateProfileRequest;
import com.astra.user.dto.UserResponse;
import java.io.IOException;
import java.util.Collection;
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
    private final JwtService jwtService;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, CurrentUserProvider currentUserProvider,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUserProvider = currentUserProvider;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email already registered");
        }
        User user = new User(request.name(), request.email(), passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId()));
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return jwtService.generateToken(user.getId());
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        UUID userId = currentUserProvider.currentUserId();
        return userRepository.findById(userId)
                .map(this::toDto)
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }

    @Transactional(readOnly = true)
    public Map<UUID, String> namesByIds(Collection<UUID> ids) {
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getName));
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

    private UserResponse toDto(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getBio());
    }
}
