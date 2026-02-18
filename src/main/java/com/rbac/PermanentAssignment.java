package com.rbac;

public class PermanentAssignment extends AbstractRoleAssignment {
    private boolean revoked;
    
    public PermanentAssignment(User user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
        this.revoked = false;
    }
    
    // Конструктор для загрузки из файла
    public PermanentAssignment(String assignmentId, User user, Role role, 
                              AssignmentMetadata metadata, boolean revoked) {
        super(assignmentId, user, role, metadata);
        this.revoked = revoked;
    }
    
    @Override
    public boolean isActive() {
        return !revoked;
    }
    
    @Override
    public String assignmentType() {
        return "PERMANENT";
    }
    
    public void revoke() {
        this.revoked = true;
    }
    
    public boolean isRevoked() {
        return revoked;
    }
    
    @Override
    public String summary() {
        String base = super.summary();
        if (revoked) {
            return base.replace("Status: INACTIVE", "Status: REVOKED");
        }
        return base;
    }
}
