package com.idp.developer.service;

import java.util.List;

import com.idp.developer.mapper.UserDtoMapper;
import com.idp.developer.model.UserDto;
import com.idp.developer.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;

    public UserService(UserRepository userRepository, UserDtoMapper userDtoMapper) {
        this.userRepository = userRepository;
        this.userDtoMapper = userDtoMapper;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userDtoMapper::toDto)
                .toList();
    }
}
