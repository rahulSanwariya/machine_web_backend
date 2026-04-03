package com.project.machiness_backend.shop.service.serviceimpl;

import com.project.machiness_backend.adminpanel.entity.Machine;
import com.project.machiness_backend.adminpanel.repo.MachineRepository;
import com.project.machiness_backend.auth.entity.User;
import com.project.machiness_backend.auth.exception.AppException;
import com.project.machiness_backend.auth.exception.ErrorCode;
import com.project.machiness_backend.auth.repo.UserRepository;
import com.project.machiness_backend.shop.dto.OrderResponse;
import com.project.machiness_backend.shop.dto.PlaceOrderRequest;
import com.project.machiness_backend.shop.entity.CartItem;
import com.project.machiness_backend.shop.entity.Order;
import com.project.machiness_backend.shop.entity.Order.OrderStatus;
import com.project.machiness_backend.shop.repo.CartItemRepository;
import com.project.machiness_backend.shop.repo.OrderRepository;
import com.project.machiness_backend.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;

    // ── Buy Now — seedha order ────────────────────────────────────────────
    @Override
    @Transactional
    public OrderResponse placeOrder(String userEmail, PlaceOrderRequest request) {
        User user = getUser(userEmail);
        Machine machine = getMachine(request.getMachineUuid());

        validateStock(machine, request.getQuantity());

        Order order = Order.builder()
                .uuid(UUID.randomUUID())
                .user(user)
                .machine(machine)
                .quantity(request.getQuantity())
                .totalPrice(machine.getPrice() * request.getQuantity())
                .status(OrderStatus.PENDING)
                .build();

        // Stock kam karo
        machine.setStock(machine.getStock() - request.getQuantity());
        if (machine.getStock() == 0) machine.setAvailable(false);
        machineRepository.save(machine);

        orderRepository.save(order);
        return buildOrderResponse(order);
    }

    // ── Place Order from Cart ─────────────────────────────────────────────
    @Override
    @Transactional
    public List<OrderResponse> placeOrderFromCart(String userEmail) {
        User user = getUser(userEmail);
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty())
            throw new AppException(ErrorCode.CART_EMPTY);

        // Pehle saare items ka stock check karo
        cartItems.forEach(item -> validateStock(item.getMachine(), item.getQuantity()));

        List<Order> orders = cartItems.stream().map(item -> {
            Machine machine = item.getMachine();

            // Stock kam karo
            machine.setStock(machine.getStock() - item.getQuantity());
            if (machine.getStock() == 0) machine.setAvailable(false);
            machineRepository.save(machine);

            return Order.builder()
                    .uuid(UUID.randomUUID())
                    .user(user)
                    .machine(machine)
                    .quantity(item.getQuantity())
                    .totalPrice(machine.getPrice() * item.getQuantity())
                    .status(OrderStatus.PENDING)
                    .build();
        }).collect(Collectors.toList());

        orderRepository.saveAll(orders);

        // Cart clear karo
        cartItemRepository.deleteByUser(user);

        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    // ── My Orders ─────────────────────────────────────────────────────────
    @Override
    public List<OrderResponse> getMyOrders(String userEmail) {
        User user = getUser(userEmail);
        return orderRepository.findByUserOrderByPlacedAtDesc(user)
                .stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    // ── Get Single Order ──────────────────────────────────────────────────
    @Override
    public OrderResponse getOrder(String userEmail, UUID orderUuid) {
        User user = getUser(userEmail);
        Order order = orderRepository.findByUuid(orderUuid)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId()))
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);

        return buildOrderResponse(order);
    }

    // ── Update Order Status (Admin) ───────────────────────────────────────
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderUuid, OrderStatus status) {
        Order order = orderRepository.findByUuid(orderUuid)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Agar order cancel ho raha hai toh stock wapas karo
        if (status == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            Machine machine = order.getMachine();
            machine.setStock(machine.getStock() + order.getQuantity());
            machine.setAvailable(true);
            machineRepository.save(machine);
        }

        order.setStatus(status);
        orderRepository.save(order);
        return buildOrderResponse(order);
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

    private void validateStock(Machine machine, int requestedQty) {
        if (!machine.isAvailable() || machine.getStock() <= 0)
            throw new AppException(ErrorCode.OUT_OF_STOCK,
                    "Machine '" + machine.getName() + "' is out of stock");
        if (requestedQty > machine.getStock())
            throw new AppException(ErrorCode.OUT_OF_STOCK,
                    "Only " + machine.getStock() + " units available for '" + machine.getName() + "'");
    }

    private OrderResponse buildOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderUuid(order.getUuid())
                .machineUuid(order.getMachine().getUuid())
                .machineName(order.getMachine().getName())
                .machineImageUrl(order.getMachine().getImagePath())
                .pricePerUnit(order.getMachine().getPrice())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .placedAt(order.getPlacedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}