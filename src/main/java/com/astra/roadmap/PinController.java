package com.astra.roadmap;

import com.astra.roadmap.dto.PinRequest;
import com.astra.roadmap.dto.PinResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/steps/{stepId}/pins")
public class PinController {

    private final PinService pinService;

    public PinController(PinService pinService) {
        this.pinService = pinService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PinResponse pin(@PathVariable UUID stepId, @Valid @RequestBody PinRequest request) {
        return pinService.pin(stepId, request);
    }

    @GetMapping
    public List<PinResponse> list(@PathVariable UUID stepId) {
        return pinService.listForStep(stepId);
    }
}
