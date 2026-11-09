package com.astra.roadmap.adapter.in.web;

import com.astra.roadmap.application.port.in.AddStepUseCase;
import com.astra.roadmap.application.port.in.CreateRoadmapUseCase;
import com.astra.roadmap.application.port.in.GetRoadmapQuery;
import com.astra.roadmap.application.port.in.ListRoadmapPinsQuery;
import com.astra.roadmap.application.port.in.ListRoadmapsQuery;
import com.astra.roadmap.application.port.in.PinResponse;
import com.astra.roadmap.application.port.in.RoadmapDetailResponse;
import com.astra.roadmap.application.port.in.RoadmapResponse;
import com.astra.roadmap.application.port.in.SetStepStatusUseCase;
import com.astra.roadmap.application.port.in.StepResponse;
import com.astra.shared.CurrentUserProvider;
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
class RoadmapController {

    private final CreateRoadmapUseCase createRoadmapUseCase;
    private final AddStepUseCase addStepUseCase;
    private final SetStepStatusUseCase setStepStatusUseCase;
    private final ListRoadmapsQuery listRoadmapsQuery;
    private final GetRoadmapQuery getRoadmapQuery;
    private final ListRoadmapPinsQuery listRoadmapPinsQuery;
    private final CurrentUserProvider currentUserProvider;

    RoadmapController(CreateRoadmapUseCase createRoadmapUseCase, AddStepUseCase addStepUseCase,
                      SetStepStatusUseCase setStepStatusUseCase, ListRoadmapsQuery listRoadmapsQuery,
                      GetRoadmapQuery getRoadmapQuery, ListRoadmapPinsQuery listRoadmapPinsQuery,
                      CurrentUserProvider currentUserProvider) {
        this.createRoadmapUseCase = createRoadmapUseCase;
        this.addStepUseCase = addStepUseCase;
        this.setStepStatusUseCase = setStepStatusUseCase;
        this.listRoadmapsQuery = listRoadmapsQuery;
        this.getRoadmapQuery = getRoadmapQuery;
        this.listRoadmapPinsQuery = listRoadmapPinsQuery;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RoadmapResponse create(@Valid @RequestBody CreateRoadmapRequest request) {
        return createRoadmapUseCase.create(request.toCommand(currentUserProvider.currentUserId()));
    }

    @GetMapping
    List<RoadmapResponse> list() {
        return listRoadmapsQuery.list(currentUserProvider.currentUserId());
    }

    @GetMapping("/{id}")
    RoadmapDetailResponse get(@PathVariable UUID id) {
        return getRoadmapQuery.get(currentUserProvider.currentUserId(), id);
    }

    @GetMapping("/{id}/pins")
    Map<UUID, List<PinResponse>> pins(@PathVariable UUID id) {
        return listRoadmapPinsQuery.pins(currentUserProvider.currentUserId(), id);
    }

    @PostMapping("/{id}/steps")
    @ResponseStatus(HttpStatus.CREATED)
    StepResponse addStep(@PathVariable UUID id, @Valid @RequestBody CreateStepRequest request) {
        return addStepUseCase.addStep(request.toCommand(currentUserProvider.currentUserId(), id));
    }

    @PatchMapping("/{id}/steps/{stepId}")
    StepResponse updateStep(@PathVariable UUID id, @PathVariable UUID stepId,
                            @RequestBody UpdateStepRequest request) {
        return setStepStatusUseCase.setStatus(currentUserProvider.currentUserId(), id, stepId, request.status());
    }
}
