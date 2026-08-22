package com.idp.developer.repository;

import java.util.Set;

import com.idp.developer.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
    Set<Role> findByNameIn(Set<String> names);
}
