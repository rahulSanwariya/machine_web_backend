package com.project.machiness_backend.shop.repo;

import com.project.machiness_backend.shop.entity.Order;
import com.project.machiness_backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByPlacedAtDesc(User user);

    Optional<Order> findByUuid(UUID uuid);
}
