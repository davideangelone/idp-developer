package com.idp.enterpriseidp.service;

import java.util.List;

import com.idp.enterpriseidp.mapper.UserDtoMapper;
import com.idp.enterpriseidp.model.UserDto;
import com.idp.enterpriseidp.repository.UserRepository;
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
