package com.karthi.expense_tracker.dto;

public record AuthResponse(String token, String tokenType, String name, String email) {}