package com.astra.learning.adapter.in.web;

import com.astra.learning.application.port.in.GoalResponse;
import com.astra.learning.application.port.in.ListGoalsQuery;
import com.astra.learning.application.port.in.SetGoalUseCase;
import com.astra.shared.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goals")
class GoalController {

    private final SetGoalUseCase setGoalUseCase;
    private final ListGoalsQuery listGoalsQuery;
    private final CurrentUserProvider currentUserProvider;

    GoalController(SetGoalUseCase setGoalUseCase, ListGoalsQuery listGoalsQuery,
                   CurrentUserProvider currentUserProvider) {
        this.setGoalUseCase = setGoalUseCase;
        this.listGoalsQuery = listGoalsQuery;
        this.currentUserProvider = currentUserProvider;
    }

    @PutMapping
    GoalResponse set(@Valid @RequestBody SetGoalRequest request) {
        return setGoalUseCase.set(request.toCommand(currentUserProvider.currentUserId()));
    }

    @GetMapping
    List<GoalResponse> list() {
        return listGoalsQuery.list(currentUserProvider.currentUserId());
    }
}
