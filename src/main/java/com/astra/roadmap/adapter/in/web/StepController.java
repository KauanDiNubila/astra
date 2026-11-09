package com.astra.roadmap.adapter.in.web;

import com.astra.roadmap.application.port.in.AddStepResourceUseCase;
import com.astra.roadmap.application.port.in.GetStepGithubEvidenceQuery;
import com.astra.roadmap.application.port.in.ListStepResourcesQuery;
import com.astra.roadmap.application.port.in.PinCourseUseCase;
import com.astra.roadmap.application.port.in.PinResponse;
import com.astra.roadmap.application.port.in.RemoveStepResourceUseCase;
import com.astra.roadmap.application.port.in.ResourceResponse;
import com.astra.roadmap.application.port.in.StepEvidenceResponse;
import com.astra.roadmap.application.port.in.UnpinCourseUseCase;
import com.astra.shared.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/steps/{stepId}")
class StepController {

    private final ListStepResourcesQuery listStepResourcesQuery;
    private final AddStepResourceUseCase addStepResourceUseCase;
    private final RemoveStepResourceUseCase removeStepResourceUseCase;
    private final PinCourseUseCase pinCourseUseCase;
    private final UnpinCourseUseCase unpinCourseUseCase;
    private final GetStepGithubEvidenceQuery getStepGithubEvidenceQuery;
    private final CurrentUserProvider currentUserProvider;

    StepController(ListStepResourcesQuery listStepResourcesQuery, AddStepResourceUseCase addStepResourceUseCase,
                   RemoveStepResourceUseCase removeStepResourceUseCase, PinCourseUseCase pinCourseUseCase,
                   UnpinCourseUseCase unpinCourseUseCase, GetStepGithubEvidenceQuery getStepGithubEvidenceQuery,
                   CurrentUserProvider currentUserProvider) {
        this.listStepResourcesQuery = listStepResourcesQuery;
        this.addStepResourceUseCase = addStepResourceUseCase;
        this.removeStepResourceUseCase = removeStepResourceUseCase;
        this.pinCourseUseCase = pinCourseUseCase;
        this.unpinCourseUseCase = unpinCourseUseCase;
        this.getStepGithubEvidenceQuery = getStepGithubEvidenceQuery;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/resources")
    List<ResourceResponse> resources(@PathVariable UUID stepId) {
        return listStepResourcesQuery.list(currentUserProvider.currentUserId(), stepId);
    }

    @PostMapping("/resources")
    @ResponseStatus(HttpStatus.CREATED)
    ResourceResponse addResource(@PathVariable UUID stepId, @Valid @RequestBody AddResourceRequest request) {
        return addStepResourceUseCase.add(request.toCommand(currentUserProvider.currentUserId(), stepId));
    }

    @DeleteMapping("/resources/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeResource(@PathVariable UUID stepId, @PathVariable UUID resourceId) {
        removeStepResourceUseCase.remove(currentUserProvider.currentUserId(), stepId, resourceId);
    }

    @PostMapping("/pins")
    @ResponseStatus(HttpStatus.CREATED)
    PinResponse pin(@PathVariable UUID stepId, @Valid @RequestBody PinRequest request) {
        return pinCourseUseCase.pin(request.toCommand(currentUserProvider.currentUserId(), stepId));
    }

    @DeleteMapping("/pins/{pinId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unpin(@PathVariable UUID stepId, @PathVariable UUID pinId) {
        unpinCourseUseCase.unpin(currentUserProvider.currentUserId(), stepId, pinId);
    }

    @GetMapping("/github-evidence")
    StepEvidenceResponse githubEvidence(@PathVariable UUID stepId) {
        return getStepGithubEvidenceQuery.evidence(currentUserProvider.currentUserId(), stepId);
    }
}
