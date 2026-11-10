package com.astra.privacy.adapter.in.web;

import com.astra.privacy.application.port.in.ExportUserDataQuery;
import com.astra.shared.CurrentUserProvider;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DataExportController {

    private static final ZoneId FILENAME_ZONE = ZoneId.of("America/Sao_Paulo");

    private final ExportUserDataQuery exportUserDataQuery;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    DataExportController(ExportUserDataQuery exportUserDataQuery, CurrentUserProvider currentUserProvider,
                         Clock clock) {
        this.exportUserDataQuery = exportUserDataQuery;
        this.currentUserProvider = currentUserProvider;
        this.clock = clock;
    }

    @GetMapping("/me/export")
    ResponseEntity<Map<String, Object>> export() {
        String filename = "astra-meus-dados-" + LocalDate.now(clock.withZone(FILENAME_ZONE)) + ".json";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.APPLICATION_JSON)
                .body(exportUserDataQuery.export(currentUserProvider.currentUserId()));
    }
}
