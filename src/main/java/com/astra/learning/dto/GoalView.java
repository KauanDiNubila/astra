package com.astra.learning.dto;

import com.astra.learning.entity.GoalType;
public record GoalView(GoalType type, int targetHours) {
}
