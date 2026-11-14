package com.example.ecommerce.controller;


import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.CartItemRepo;
import com.example.ecommerce.repository.ProductRepo;
import com.example.ecommerce.repository.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.Authenticator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cart")
public class CartController
{
    private final CartItemRepo cartItemRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    public CartController(CartItemRepo cartItemRepo , UserRepo userRepo , ProductRepo productRepo)
    {
        this.cartItemRepo = cartItemRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    @PostMapping("/add")

    public ResponseEntity<String> addtoCart(@RequestBody AddToCartRequest request , Authentication authentication)
    {
        String email = authentication.getName();
        Optional<User> userOptional = userRepo.findByEmail(email);

        if(userOptional.isEmpty())
        {
            return ResponseEntity.status(401).body("User not found");
        }

        if(request.getQuantity() <= 0)
        {
            return ResponseEntity.badRequest().body("Quantity must be at least 1");
        }

        Optional<Product> productOptional = productRepo.findById(request.getProductId());

        if(productOptional.isEmpty())
        {
            return ResponseEntity.status(404).body("Product not found");
        }

        User user = userOptional.get();
        Product product = productOptional.get();

        Optional<CartItem> existingItem = cartItemRepo.findByUserAndProduct(user , product);

        if(existingItem.isPresent())
        {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepo.save(item);
        }
        else
        {
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cartItemRepo.save(newItem);
        }
        return ResponseEntity.ok("Product added to cart");
    }

    @GetMapping
    public ResponseEntity <List<CartItem>>getCart(Authentication authentication)
    {
        String email = authentication.getName();
        Optional<User>userOptional = userRepo.findByEmail(email);

        if(userOptional.isEmpty())
        {
            return ResponseEntity.status(401).build();
        }

        List<CartItem> items = cartItemRepo.findByUser(userOptional.get());
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/remove/{productId}")

    public ResponseEntity<String> removeFromCart(@PathVariable Long productId , Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        Optional<Product> productOptional = productRepo.findById(productId);

        if (productOptional.isEmpty())
        {
            return ResponseEntity.status(404).body("Product not found");
        }

        Optional<CartItem>itemOptional = cartItemRepo.findByUserAndProduct(userOptional.get(),productOptional.get());
        if(itemOptional.isEmpty())
        {
            return ResponseEntity.status(404).body("Product is not in your cart");
        }

        cartItemRepo.delete(itemOptional.get());
        return ResponseEntity.ok("Product removed from cart");
    }
}
