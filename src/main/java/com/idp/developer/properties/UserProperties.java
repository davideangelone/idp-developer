package com.idp.developer.properties;

import java.util.Set;

import lombok.Data;

@Data
public class UserProperties {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phoneNumber;
    private Set<String> roles;
    private Set<String> groups;
}