package com.cms.consultant_management_system.exception;

public class ConsultantNotFoundException extends RuntimeException {
    public ConsultantNotFoundException(Long id) {
        super("Consultant not found with id: " + id);
    }
}