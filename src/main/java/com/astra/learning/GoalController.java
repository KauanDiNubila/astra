package com.astra.learning;

import com.astra.learning.dto.GoalResponse;
import com.astra.learning.dto.SetGoalRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PutMapping
    public GoalResponse set(@Valid @RequestBody SetGoalRequest request) {
        return goalService.setGoal(request);
    }

    @GetMapping
    public List<GoalResponse> list() {
        return goalService.listForCurrentUser();
    }
}
