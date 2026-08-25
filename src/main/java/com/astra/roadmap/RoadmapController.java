package com.astra.roadmap;

import com.astra.roadmap.dto.CreateRoadmapRequest;
import com.astra.roadmap.dto.CreateStepRequest;
import com.astra.roadmap.dto.PinResponse;
import com.astra.roadmap.dto.RoadmapDetailResponse;
import com.astra.roadmap.dto.RoadmapResponse;
import com.astra.roadmap.dto.StepResponse;
import com.astra.roadmap.dto.UpdateStepRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final PinService pinService;

    public RoadmapController(RoadmapService roadmapService, PinService pinService) {
        this.roadmapService = roadmapService;
        this.pinService = pinService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoadmapResponse create(@Valid @RequestBody CreateRoadmapRequest request) {
        return roadmapService.create(request);
    }

    @GetMapping
    public List<RoadmapResponse> list() {
        return roadmapService.list();
    }

    @GetMapping("/{id}")
    public RoadmapDetailResponse get(@PathVariable UUID id) {
        return roadmapService.get(id);
    }

    @GetMapping("/{id}/pins")
    public Map<UUID, List<PinResponse>> pins(@PathVariable UUID id) {
        return pinService.listForRoadmap(id);
    }

    @PostMapping("/{id}/steps")
    @ResponseStatus(HttpStatus.CREATED)
    public StepResponse addStep(@PathVariable UUID id, @Valid @RequestBody CreateStepRequest request) {
        return roadmapService.addStep(id, request);
    }

    @PatchMapping("/{id}/steps/{stepId}")
    public StepResponse updateStep(@PathVariable UUID id, @PathVariable UUID stepId,
                                   @RequestBody UpdateStepRequest request) {
        return roadmapService.setStepStatus(id, stepId, request.status());
    }
}
