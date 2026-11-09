package com.astra.learning.adapter.in.web;

import jakarta.validation.constraints.NotNull;

record UpdateLessonRequest(@NotNull Boolean completed) {
}
