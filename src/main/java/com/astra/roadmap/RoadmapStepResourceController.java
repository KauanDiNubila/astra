package com.astra.roadmap;

import com.astra.roadmap.dto.AddResourceRequest;
import com.astra.roadmap.dto.ResourceResponse;
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
@RequestMapping("/steps/{stepId}/resources")
public class RoadmapStepResourceController {

    private final RoadmapStepResourceService resourceService;

    public RoadmapStepResourceController(RoadmapStepResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public List<ResourceResponse> list(@PathVariable UUID stepId) {
        return resourceService.list(stepId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse add(@PathVariable UUID stepId, @Valid @RequestBody AddResourceRequest request) {
        return resourceService.add(stepId, request);
    }

    @DeleteMapping("/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID stepId, @PathVariable UUID resourceId) {
        resourceService.remove(stepId, resourceId);
    }
}
