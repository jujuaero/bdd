package com.project.artconnect.security;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Holds the current user session (role and optional member identity for ARTIST role).
 */
public final class AuthSession {

    private static final AuthSession INSTANCE = new AuthSession();

    private final ObjectProperty<UserRole> role = new SimpleObjectProperty<>(UserRole.VISITOR);
    private final StringProperty memberEmail = new SimpleStringProperty("");

    private AuthSession() {}

    public static AuthSession get() {
        return INSTANCE;
    }

    public ObjectProperty<UserRole> roleProperty() {
        return role;
    }

    public UserRole getRole() {
        return role.get();
    }

    public void setRole(UserRole role) {
        this.role.set(role == null ? UserRole.VISITOR : role);
    }

    public StringProperty memberEmailProperty() {
        return memberEmail;
    }

    public String getMemberEmail() {
        return memberEmail.get();
    }

    public void setMemberEmail(String email) {
        memberEmail.set(email == null ? "" : email.trim());
    }
}
