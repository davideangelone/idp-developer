package com.idp.developer.repository;

import com.idp.developer.entity.User;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"roles", "groups"})
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "groups"})
    @Override
    @NullMarked List<User> findAll();
}
