package com.astra.call.adapter.in.web;

import com.astra.call.application.port.in.ActiveCallResponse;
import com.astra.call.application.port.in.GetIceServersQuery;
import com.astra.call.application.port.in.IceServersResponse;
import com.astra.call.application.port.in.ListActiveCallsQuery;
import com.astra.shared.CurrentUserProvider;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/call")
class CallController {

    private final GetIceServersQuery getIceServersQuery;
    private final ListActiveCallsQuery listActiveCallsQuery;
    private final CurrentUserProvider currentUserProvider;

    CallController(GetIceServersQuery getIceServersQuery, ListActiveCallsQuery listActiveCallsQuery,
                   CurrentUserProvider currentUserProvider) {
        this.getIceServersQuery = getIceServersQuery;
        this.listActiveCallsQuery = listActiveCallsQuery;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/ice-servers")
    IceServersResponse iceServers() {
        return getIceServersQuery.iceServers(currentUserProvider.currentUserId());
    }

    @GetMapping("/active")
    List<ActiveCallResponse> active() {
        return listActiveCallsQuery.activeFor(currentUserProvider.currentUserId());
    }
}
