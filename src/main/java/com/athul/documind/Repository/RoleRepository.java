package com.athul.documind.Repository;

import com.athul.documind.Entity.Role;
import com.athul.documind.Enum.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
    boolean existsByName(RoleType name);


}
