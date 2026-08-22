package com.idp.developer.model;

public interface UserDtoInterface {

    String firstName();

    String lastName();

    default String getFullName() {
        return firstName() + " " + lastName();
    }
}
