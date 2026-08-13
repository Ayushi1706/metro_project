package org.spring.metro.repository;

import org.spring.metro.models.entity.User;
import org.spring.metro.models.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Page<User> findByRole(Role userRole, Pageable pageable);

    Page<User> findByRoleIn(List<Role> roles, Pageable pageable);
}