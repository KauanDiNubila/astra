package com.astra.social.adapter.in.web;

import com.astra.shared.CurrentUserProvider;
import com.astra.social.application.port.in.AcceptFriendRequestUseCase;
import com.astra.social.application.port.in.FriendshipResponse;
import com.astra.social.application.port.in.ListFriendshipsQuery;
import com.astra.social.application.port.in.RemoveFriendshipUseCase;
import com.astra.social.application.port.in.SendFriendRequestUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/friends")
class FriendshipController {

    private final SendFriendRequestUseCase sendFriendRequestUseCase;
    private final AcceptFriendRequestUseCase acceptFriendRequestUseCase;
    private final RemoveFriendshipUseCase removeFriendshipUseCase;
    private final ListFriendshipsQuery listFriendshipsQuery;
    private final CurrentUserProvider currentUserProvider;

    FriendshipController(SendFriendRequestUseCase sendFriendRequestUseCase,
                         AcceptFriendRequestUseCase acceptFriendRequestUseCase,
                         RemoveFriendshipUseCase removeFriendshipUseCase, ListFriendshipsQuery listFriendshipsQuery,
                         CurrentUserProvider currentUserProvider) {
        this.sendFriendRequestUseCase = sendFriendRequestUseCase;
        this.acceptFriendRequestUseCase = acceptFriendRequestUseCase;
        this.removeFriendshipUseCase = removeFriendshipUseCase;
        this.listFriendshipsQuery = listFriendshipsQuery;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    FriendshipResponse send(@Valid @RequestBody SendFriendRequestRequest request) {
        return sendFriendRequestUseCase.send(request.toCommand(currentUserProvider.currentUserId()));
    }

    @GetMapping
    List<FriendshipResponse> friends() {
        return listFriendshipsQuery.friends(currentUserProvider.currentUserId());
    }

    @GetMapping("/requests")
    List<FriendshipResponse> pendingRequests() {
        return listFriendshipsQuery.pendingRequests(currentUserProvider.currentUserId());
    }

    @PostMapping("/{id}/accept")
    FriendshipResponse accept(@PathVariable UUID id) {
        return acceptFriendRequestUseCase.accept(currentUserProvider.currentUserId(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@PathVariable UUID id) {
        removeFriendshipUseCase.remove(currentUserProvider.currentUserId(), id);
    }
}
