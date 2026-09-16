package com.civicAI.backend.security;

public record CurrentUser(Long id, String email, String role) {
}