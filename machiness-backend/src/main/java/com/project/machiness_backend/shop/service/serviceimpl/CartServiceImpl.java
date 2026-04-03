package com.project.machiness_backend.shop.service.serviceimpl;

import com.project.machiness_backend.adminpanel.entity.Machine;
import com.project.machiness_backend.adminpanel.repo.MachineRepository;
import com.project.machiness_backend.auth.entity.User;
import com.project.machiness_backend.auth.exception.AppException;
import com.project.machiness_backend.auth.exception.ErrorCode;
import com.project.machiness_backend.auth.repo.UserRepository;
import com.project.machiness_backend.shop.dto.AddToCartRequest;
import com.project.machiness_backend.shop.dto.CartItemResponse;
import com.project.machiness_backend.shop.dto.CartResponse;
import com.project.machiness_backend.shop.entity.CartItem;
import com.project.machiness_backend.shop.repo.CartItemRepository;
import com.project.machiness_backend.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;

    // ── Add to Cart ───────────────────────────────────────────────────────
    @Override
    @Transactional
    public CartItemResponse addToCart(String userEmail, AddToCartRequest request) {
        User user = getUser(userEmail);
        Machine machine = getMachine(request.getMachineUuid());

        // Agar machine available nahi hai
        if (!machine.isAvailable() || machine.getStock() <= 0)
            throw new AppException(ErrorCode.MACHINE_NOT_FOUND, "Machine is not available");

        // Agar already cart mein hai toh quantity update karo
        CartItem cartItem = cartItemRepository.findByUserAndMachine(user, machine)
                .map(existing -> {
                    int newQty = existing.getQuantity() + request.getQuantity();
                    if (newQty > machine.getStock())
                        throw new AppException(ErrorCode.VALIDATION_FAILED,
                                "Only " + machine.getStock() + " units available in stock");
                    existing.setQuantity(newQty);
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> {
                    if (request.getQuantity() > machine.getStock())
                        throw new AppException(ErrorCode.VALIDATION_FAILED,
                                "Only " + machine.getStock() + " units available in stock");
                    CartItem newItem = CartItem.builder()
                            .uuid(UUID.randomUUID())
                            .user(user)
                            .machine(machine)
                            .quantity(request.getQuantity())
                            .build();
                    return cartItemRepository.save(newItem);
                });

        return buildCartItemResponse(cartItem);
    }

    // ── Get Cart ──────────────────────────────────────────────────────────
    @Override
    public CartResponse getCart(String userEmail) {
        User user = getUser(userEmail);
        List<CartItem> items = cartItemRepository.findByUser(user);

        List<CartItemResponse> responseItems = items.stream()
                .map(this::buildCartItemResponse)
                .collect(Collectors.toList());

        double cartTotal = responseItems.stream()
                .mapToDouble(CartItemResponse::getTotalPrice)
                .sum();

        return CartResponse.builder()
                .items(responseItems)
                .totalItems(responseItems.size())
                .cartTotal(cartTotal)
                .build();
    }

    // ── Update Quantity ───────────────────────────────────────────────────
    @Override
    @Transactional
    public CartItemResponse updateQuantity(String userEmail, UUID cartItemUuid, int quantity) {
        User user = getUser(userEmail);

        CartItem cartItem = cartItemRepository.findByUuid(cartItemUuid)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Security: apna hi item update kar sakta hai
        if (!cartItem.getUser().getId().equals(user.getId()))
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);

        if (quantity > cartItem.getMachine().getStock())
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Only " + cartItem.getMachine().getStock() + " units available in stock");

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return buildCartItemResponse(cartItem);
    }

    // ── Remove from Cart ──────────────────────────────────────────────────
    @Override
    @Transactional
    public void removeFromCart(String userEmail, UUID cartItemUuid) {
        User user = getUser(userEmail);

        CartItem cartItem = cartItemRepository.findByUuid(cartItemUuid)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!cartItem.getUser().getId().equals(user.getId()))
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);

        cartItemRepository.delete(cartItem);
    }

    // ── Clear Cart ────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void clearCart(String userEmail) {
        User user = getUser(userEmail);
        cartItemRepository.deleteByUser(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Machine getMachine(UUID uuid) {
        return machineRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_NOT_FOUND));
    }

    private CartItemResponse buildCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .cartItemUuid(item.getUuid())
                .machineUuid(item.getMachine().getUuid())
                .machineName(item.getMachine().getName())
                .machineImageUrl(item.getMachine().getImagePath())
                .pricePerUnit(item.getMachine().getPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getMachine().getPrice() * item.getQuantity())
                .addedAt(item.getAddedAt())
                .build();
    }
}
