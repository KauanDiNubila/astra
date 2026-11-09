package com.astra.user.adapter.in.web;

import com.astra.user.application.port.in.UserResponse;

record AuthResponse(String accessToken, UserResponse user) {
}
