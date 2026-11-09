package com.astra.user.adapter.in.web;

import com.astra.shared.CurrentUserProvider;
import com.astra.user.application.port.in.AdminUserResponse;
import com.astra.user.application.port.in.BanUserUseCase;
import com.astra.user.application.port.in.ChangeUserRoleUseCase;
import com.astra.user.application.port.in.DeleteUserUseCase;
import com.astra.user.application.port.in.ListUsersQuery;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
class AdminUserController {

    private final ListUsersQuery listUsersQuery;
    private final BanUserUseCase banUserUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final CurrentUserProvider currentUserProvider;

    AdminUserController(ListUsersQuery listUsersQuery, BanUserUseCase banUserUseCase,
                        ChangeUserRoleUseCase changeUserRoleUseCase, DeleteUserUseCase deleteUserUseCase,
                        CurrentUserProvider currentUserProvider) {
        this.listUsersQuery = listUsersQuery;
        this.banUserUseCase = banUserUseCase;
        this.changeUserRoleUseCase = changeUserRoleUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    List<AdminUserResponse> list() {
        return listUsersQuery.list();
    }

    @PostMapping("/{id}/ban")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void ban(@PathVariable UUID id) {
        banUserUseCase.ban(currentUserProvider.currentUserId(), id);
    }

    @PostMapping("/{id}/unban")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unban(@PathVariable UUID id) {
        banUserUseCase.unban(currentUserProvider.currentUserId(), id);
    }

    @PostMapping("/{id}/promote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void promote(@PathVariable UUID id) {
        changeUserRoleUseCase.promote(currentUserProvider.currentUserId(), id);
    }

    @PostMapping("/{id}/demote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void demote(@PathVariable UUID id) {
        changeUserRoleUseCase.demote(currentUserProvider.currentUserId(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) {
        deleteUserUseCase.delete(currentUserProvider.currentUserId(), id);
    }
}
