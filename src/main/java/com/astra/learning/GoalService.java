package com.astra.learning;

import com.astra.learning.dto.GoalResponse;
import com.astra.learning.dto.SetGoalRequest;
import com.astra.shared.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final CurrentUserProvider currentUserProvider;

    public GoalService(GoalRepository goalRepository, CurrentUserProvider currentUserProvider) {
        this.goalRepository = goalRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public GoalResponse setGoal(SetGoalRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        Goal goal = goalRepository.findByUserIdAndType(userId, request.type())
                .orElseGet(() -> new Goal(userId, request.type(), request.targetHours()));
        goal.setTargetHours(request.targetHours());
        Goal saved = goalRepository.save(goal);
        return new GoalResponse(saved.getType(), saved.getTargetHours());
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> listForCurrentUser() {
        UUID userId = currentUserProvider.currentUserId();
        return goalsForUser(userId).stream()
                .map(v -> new GoalResponse(v.type(), v.targetHours()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GoalView> goalsForUser(UUID userId) {
        return goalRepository.findByUserId(userId).stream()
                .map(g -> new GoalView(g.getType(), g.getTargetHours()))
                .toList();
    }
}
