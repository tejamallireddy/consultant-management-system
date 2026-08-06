package com.cms.consultant_management_system.entity;

public enum Status {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String label;

    Status(String label) { this.label = label; }

    public String getLabel() { return label; }
}