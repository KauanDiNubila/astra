package com.astra.social;

import com.astra.social.dto.FriendshipResponse;
import com.astra.social.dto.SendFriendRequest;
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
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FriendshipResponse sendRequest(@Valid @RequestBody SendFriendRequest request) {
        return friendshipService.sendRequest(request);
    }

    @GetMapping
    public List<FriendshipResponse> myFriends() {
        return friendshipService.myFriends();
    }

    @GetMapping("/requests")
    public List<FriendshipResponse> pendingRequests() {
        return friendshipService.pendingRequests();
    }

    @PostMapping("/{id}/accept")
    public FriendshipResponse accept(@PathVariable UUID id) {
        return friendshipService.accept(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID id) {
        friendshipService.remove(id);
    }
}
