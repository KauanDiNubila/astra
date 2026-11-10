package com.astra.chat.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

record AddGroupMemberRequest(@NotNull UUID userId) {
}
