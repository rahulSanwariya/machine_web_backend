package com.project.machiness_backend.adminpanel.repo;

import com.project.machiness_backend.adminpanel.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {

    Optional<Machine> findByUuid(UUID uuid);

    Optional<Machine> findByImageUuid(UUID imageUuid);
}