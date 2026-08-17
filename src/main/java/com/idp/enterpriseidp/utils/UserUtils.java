package com.idp.enterpriseidp.utils;

import com.idp.enterpriseidp.model.UserDto;
import com.idp.enterpriseidp.properties.UserProperties;

public class UserUtils {

    private UserUtils() {
    }

    private static String getFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }

    public static String getFullName(UserDto user) {
        return getFullName(user.firstName(), user.lastName());
    }

    public static String getFullName(UserProperties user) {
        return getFullName(user.getFirstName(), user.getLastName());
    }
}
