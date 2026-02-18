package com.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;
    
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, 
                              String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        setExpiresAt(expiresAt);
        this.autoRenew = autoRenew;
    }
    
    // Конструктор для загрузки из файла
    public TemporaryAssignment(String assignmentId, User user, Role role, 
                              AssignmentMetadata metadata, String expiresAt, 
                              boolean autoRenew) {
        super(assignmentId, user, role, metadata);
        setExpiresAt(expiresAt);
        this.autoRenew = autoRenew;
    }
    
    private void setExpiresAt(String expiresAt) {
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("ExpiresAt cannot be null or empty");
        }
        // Простая проверка формата даты
        try {
            LocalDateTime.parse(expiresAt, FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use: yyyy-MM-dd HH:mm");
        }
        this.expiresAt = expiresAt;
    }
    
    @Override
    public boolean isActive() {
        if (isExpired()) {
            if (autoRenew) {
                // Автоматически продлеваем на 30 дней
                extend(LocalDateTime.now().plusDays(30).format(FORMATTER));
                return true;
            }
            return false;
        }
        return true;
    }
    
    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }
    
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = LocalDateTime.parse(expiresAt, FORMATTER);
        return now.isAfter(expiry);
    }
    
    public void extend(String newExpirationDate) {
        setExpiresAt(newExpirationDate);
    }
    
    public String getTimeRemaining() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = LocalDateTime.parse(expiresAt, FORMATTER);
        
        if (now.isAfter(expiry)) {
            return "Expired";
        }
        
        long days = ChronoUnit.DAYS.between(now, expiry);
        long hours = ChronoUnit.HOURS.between(now, expiry) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, expiry) % 60;
        
        return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
    }
    
    public String getExpiresAt() {
        return expiresAt;
    }
    
    public boolean isAutoRenew() {
        return autoRenew;
    }
    
    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }
    
    @Override
    public String summary() {
        String base = super.summary();
        String expiryInfo = String.format("\nExpires: %s (Remaining: %s)", 
            expiresAt, getTimeRemaining());
        return base + expiryInfo;
    }
}
