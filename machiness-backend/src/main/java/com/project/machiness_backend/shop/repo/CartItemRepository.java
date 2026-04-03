package com.project.machiness_backend.shop.repo;

import com.project.machiness_backend.shop.entity.CartItem;
import com.project.machiness_backend.auth.entity.User;
import com.project.machiness_backend.adminpanel.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUuid(UUID uuid);

    Optional<CartItem> findByUserAndMachine(User user, Machine machine);

    void deleteByUser(User user);
}
