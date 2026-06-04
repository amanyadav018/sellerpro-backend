package com.sellerpro.exception;

import lombok.Getter;

@Getter
public class PlanUpgradeRequiredException extends RuntimeException {
    private final String requiredPlan;

    public PlanUpgradeRequiredException(String message, String requiredPlan) {
        super(message);
        this.requiredPlan = requiredPlan;
    }
}
