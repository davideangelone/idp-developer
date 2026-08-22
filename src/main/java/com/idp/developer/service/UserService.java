package com.idp.developer.service;

import java.util.List;
import java.util.Set;

import com.idp.developer.entity.User;
import com.idp.developer.mapper.UserDtoMapper;
import com.idp.developer.model.UserDto;
import com.idp.developer.model.UserUpdateDto;
import com.idp.developer.repository.GroupRepository;
import com.idp.developer.repository.RoleRepository;
import com.idp.developer.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final UserDtoMapper userDtoMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, GroupRepository groupRepository, UserDtoMapper userDtoMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.groupRepository = groupRepository;
        this.userDtoMapper = userDtoMapper;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userDtoMapper::toDto)
                .toList();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public void updateUser(UserUpdateDto dto) {
        User user = userRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + dto.id()));

        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPassword(dto.password());
        user.setEmail(dto.email());
        user.setEmailVerified(dto.emailVerified());
        user.setAddress(dto.address());
        user.setPhoneNumber(dto.phoneNumber());
        user.setPassword(dto.password());
        user.setRoles(roleRepository.findByNameIn(dto.roles()));
        user.setGroups(groupRepository.findByNameIn(dto.groups()));
        user.setEnabled(dto.enabled());
        user.setAccountNonExpired(dto.accountNonExpired());
        user.setAccountNonLocked(dto.accountNonLocked());
        user.setCredentialsNonExpired(dto.credentialsNonExpired());

        userRepository.save(user);
    }

    @Transactional
    public void deleteUsers(Set<Long> ids) {
        userRepository.deleteAllById(ids);
    }

    @Transactional
    public UserDto createUser(String username) {

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Utente già esistente: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setFirstName("");
        user.setLastName("");
        user.setPassword("");
        user.setEmail("");
        user.setEmailVerified(true);
        user.setAddress("");
        user.setPhoneNumber("");
        user.setRoles(Set.of());
        user.setGroups(Set.of());
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        User saved = userRepository.save(user);
        return userDtoMapper.toDto(saved);
    }
}
