package com.cms.consultant_management_system.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("A consultant with email '" + email + "' already exists");
    }
}