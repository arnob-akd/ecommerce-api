package com.example.ecommerce.controller;


import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")

public class ProductController
{
    private final ProductRepo productRepository;

    public ProductController(ProductRepo productRepository)
    {
        this.productRepository = productRepository;
    }

    @PostMapping
    public Product addProduct(@RequestBody Product product)
    {
        return productRepository.save(product);
    }

    @GetMapping
    public List<Product> getAllProducts()
    {
        return productRepository.findAll();
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name)
    {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProdcutById(@PathVariable Long id)
    {
        Optional<Product> product = productRepository.findById(id);

        if(product.isPresent())
        {
            return ResponseEntity.ok(product.get());
        }
        else
        {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id)
    {
        if(!productRepository.existsById(id))
        {
            return ResponseEntity.notFound().build();
        }

        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")

    public ResponseEntity<Product> updateProduct(@PathVariable Long id , @RequestBody Product newData)

    {
        Optional<Product> existing= productRepository.findById(id);
        if(existing.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        Product product = existing.get();
        product.setName(newData.getName());
        product.setDescription(newData.getDescription());
        product.setPrice(newData.getPrice());
        product.setStockQuantity(newData.getStockQuantity());

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

}
