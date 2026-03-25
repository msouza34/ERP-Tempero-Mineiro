package com.temperomineiro.erp.repository;

import com.temperomineiro.erp.model.DomainEnums.RoleName;
import com.temperomineiro.erp.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}

