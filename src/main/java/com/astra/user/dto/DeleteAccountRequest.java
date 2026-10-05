package com.astra.user.dto;

import jakarta.validation.constraints.Size;

public record DeleteAccountRequest(@Size(max = 100) String password, @Size(max = 180) String confirmation) {
}
