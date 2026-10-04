package com.astra.call.controller;

import com.astra.call.dto.ActiveCallResponse;
import com.astra.call.dto.IceServersResponse;
import com.astra.call.service.CallService;
import com.astra.call.service.IceServerService;
import com.astra.shared.CurrentUserProvider;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/call")
public class CallController {

    private final IceServerService iceServerService;
    private final CallService callService;
    private final CurrentUserProvider currentUserProvider;

    public CallController(IceServerService iceServerService, CallService callService,
            CurrentUserProvider currentUserProvider) {
        this.iceServerService = iceServerService;
        this.callService = callService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/ice-servers")
    public IceServersResponse iceServers() {
        return iceServerService.forUser(currentUserProvider.currentUserId());
    }

    @GetMapping("/active")
    public List<ActiveCallResponse> active() {
        return callService.activeFor(currentUserProvider.currentUserId());
    }
}
