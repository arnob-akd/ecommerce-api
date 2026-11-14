package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.ecommerce.model.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartItemRepo extends JpaRepository<CartItem, Long>
{
    List<CartItem>findByUser(User user);

    Optional<CartItem>findByUserAndProduct(User user , Product product);
}