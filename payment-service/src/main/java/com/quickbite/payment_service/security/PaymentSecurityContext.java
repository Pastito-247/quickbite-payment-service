package com.quickbite.payment_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentSecurityContext {

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof String) {
            return (String) authentication.getDetails();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    @SuppressWarnings("unchecked")
    public List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .toList();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    public boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }

    public boolean isCurrentUser(String userId) {
        try {
            return getCurrentUserId().equals(userId);
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
