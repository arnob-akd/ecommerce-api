package com.example.ecommerce.controller;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.payment.PaymentService;
import com.example.ecommerce.repository.CartItemRepo;
import com.example.ecommerce.repository.OrderRepo;
import com.example.ecommerce.repository.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.ecommerce.repository.ProductRepo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepo orderRepo;
    private final CartItemRepo cartItemRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final PaymentService paymentService;

    public OrderController(OrderRepo orderRepo,
                           CartItemRepo cartItemRepo,
                           UserRepo userRepo,
                           ProductRepo productRepo,
                           PaymentService paymentService) {
        this.orderRepo = orderRepo;
        this.cartItemRepo = cartItemRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(Authentication authentication) {

        String email = authentication.getName();
        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User user = userOptional.get();
        List<CartItem> cartItems = cartItemRepo.findByUser(user);

        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                return ResponseEntity.badRequest()
                        .body("Not enough stock for " + product.getName());
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        double total = 0;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            order.getItems().add(orderItem);
            total = total + product.getPrice() * cartItem.getQuantity();
        }

        order.setTotalAmount(total);

        String paymentId = paymentService.createPayment(total);
        order.setPaymentId(paymentId);

        Order savedOrder = orderRepo.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    @PostMapping("/{orderId}/confirm")
    @Transactional
    public ResponseEntity<?> confirmOrder(@PathVariable Long orderId,
                                          Authentication authentication) {

        String email = authentication.getName();
        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User user = userOptional.get();

        Optional<Order> orderOptional = orderRepo.findById(orderId);

        if (orderOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Order not found");
        }

        Order order = orderOptional.get();

        if (!order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(404).body("Order not found");
        }

        if ("PAID".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Order is already paid");
        }

        if (!paymentService.isPaid(order.getPaymentId())) {
            return ResponseEntity.badRequest().body("Payment not completed yet");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();

            if (product.getStockQuantity() < item.getQuantity()) {
                return ResponseEntity.badRequest()
                        .body("Not enough stock for " + product.getName());
            }
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepo.save(product);
        }

        order.setStatus("PAID");
        orderRepo.save(order);

        List<CartItem> cartItems = cartItemRepo.findByUser(user);
        cartItemRepo.deleteAll(cartItems);

        return ResponseEntity.ok(order);
    }
}