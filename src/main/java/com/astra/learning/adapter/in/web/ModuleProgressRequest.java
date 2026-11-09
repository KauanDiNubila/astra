package com.astra.learning.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

record ModuleProgressRequest(@NotNull @PositiveOrZero Integer uptoPosition) {
}
