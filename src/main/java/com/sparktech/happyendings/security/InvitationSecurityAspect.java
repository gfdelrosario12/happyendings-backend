package com.sparktech.happyendings.security;

import com.sparktech.happyendings.model.InvitationUser;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.enums.InvitationRole;
import com.sparktech.happyendings.repository.InvitationUserRepository;
import com.sparktech.happyendings.repository.UserRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class InvitationSecurityAspect {

    @Autowired
    private InvitationUserRepository invitationUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Around("@annotation(requiresRole)")
    public Object authorize(ProceedingJoinPoint joinPoint, RequiresInvitationRole requiresRole) throws Throwable {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || "anonymousUser".equals(username)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        // Retrieve invitation ID from the method arguments (looks for first Long arg)
        Long invitationId = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long) {
                invitationId = (Long) arg;
                break;
            }
        }

        if (invitationId == null) {
            throw new IllegalArgumentException("Method annotated with @RequiresInvitationRole must provide an invitationId parameter of type Long");
        }

        // Retrieve user
        Optional<User> optionalUser = userRepository.findByEmail(username);
        if (!optionalUser.isPresent()) {
            throw new AccessDeniedException("User record not found in system database");
        }
        User user = optionalUser.get();

        // Check invitation-scoped role mapping
        Optional<InvitationUser> membership = invitationUserRepository.findByInvitationIdAndUserId(invitationId, user.getId());
        if (!membership.isPresent()) {
            throw new AccessDeniedException("User is not a member of invitation ID: " + invitationId);
        }

        InvitationRole userRole = membership.get().getRole();
        boolean hasRequiredRole = Arrays.stream(requiresRole.value())
                .anyMatch(role -> role == userRole);

        if (!hasRequiredRole) {
            throw new AccessDeniedException("Unauthorized: Required role not possessed inside invitation scope.");
        }

        return joinPoint.proceed();
    }
}
