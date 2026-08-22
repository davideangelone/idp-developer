package com.idp.developer.repository;

import java.util.Set;

import com.idp.developer.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findByName(String name);
    Set<Group> findByNameIn(Set<String> names);
}
