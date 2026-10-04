package com.astra.call.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IceServer(List<String> urls, String username, String credential) {
}
