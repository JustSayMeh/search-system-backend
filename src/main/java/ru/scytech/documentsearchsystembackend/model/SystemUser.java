package ru.scytech.documentsearchsystembackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class SystemUser {
    private String username;
    private String password;
    private Collection<String> authorities;
}
