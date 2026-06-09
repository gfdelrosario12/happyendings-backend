package com.sparktech.happyendings.security;

import com.sparktech.happyendings.model.InvitationUser;
import com.sparktech.happyendings.model.Guest;
import com.sparktech.happyendings.model.User;
import com.sparktech.happyendings.model.enums.InvitationRole;
import com.sparktech.happyendings.repository.InvitationUserRepository;
import com.sparktech.happyendings.repository.UserRepository;
import com.sparktech.happyendings.repository.GuestRepository;
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

    @Autowired
    private GuestRepository guestRepository;

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

        String targetEmail = username;
        if (username.startsWith("guest:")) {
            targetEmail = username.substring(6);
        }

        // 1. Check if the user is a registered member of the invitation (COUPLE or COORDINATOR)
        Optional<User> optionalUser = userRepository.findByEmail(targetEmail);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<InvitationUser> membership = invitationUserRepository.findByInvitationIdAndUserId(invitationId, user.getId());
            if (membership.isPresent()) {
                InvitationRole userRole = membership.get().getRole();
                boolean hasRequiredRole = Arrays.stream(requiresRole.value())
                        .anyMatch(role -> role == userRole);
                if (hasRequiredRole) {
                    return joinPoint.proceed();
                }
            }
        }

        // 2. Check if the email belongs to an invited guest for this invitation
        Optional<Guest> guestOpt = guestRepository.findByInvitationIdAndEmail(invitationId, targetEmail);
        if (guestOpt.isPresent()) {
            boolean hasGuestRole = Arrays.stream(requiresRole.value())
                    .anyMatch(role -> role == InvitationRole.GUEST);
            if (hasGuestRole) {
                return joinPoint.proceed();
            }
        }

        throw new AccessDeniedException("Unauthorized: Required role not possessed inside invitation scope.");
    }
}
