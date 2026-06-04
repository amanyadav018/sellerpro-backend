package com.sellerpro.security;

import com.sellerpro.annotation.RequiresPlan;
import com.sellerpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
@RequiredArgsConstructor
public class PlanGuard {

    private final UserRepository userRepository;

    @Around("@annotation(requiresPlan)")
    public Object checkPlan(ProceedingJoinPoint joinPoint, RequiresPlan requiresPlan) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String email = auth.getName();
        String userPlan = userRepository.findPlanByEmail(email);

        if (userPlan == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No active subscription found");
        }

        String requiredPlan = requiresPlan.value();

        // Plan hierarchy: ADMIN > PRO > BASIC
        boolean hasAccess = switch (userPlan.toUpperCase()) {
            case "ADMIN" -> true;
            case "PRO" -> requiredPlan.equals("PRO") || requiredPlan.equals("BASIC");
            case "BASIC" -> requiredPlan.equals("BASIC");
            default -> false;
        };

        if (!hasAccess) {
            throw new ResponseStatusException(
                HttpStatus.PAYMENT_REQUIRED,
                String.format("This feature requires %s plan. Upgrade at sellerpro.in/pricing", requiredPlan)
            );
        }

        return joinPoint.proceed();
    }
}
