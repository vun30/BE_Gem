package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.CartRepository;
import online.gemfpt.BE.Repository.CustomerRepository;
import online.gemfpt.BE.Repository.ProductsRepository;
import online.gemfpt.BE.entity.Cart;
import online.gemfpt.BE.entity.Customer;
import online.gemfpt.BE.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    ProductsRepository productsRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Transactional
    public void addToCart(String name, int phone, List<String> barcodes) {
        Optional<Customer> optionalCustomer = customerRepository.findByPhone(phone);
        Customer customer;
        if (optionalCustomer.isPresent()) {
            customer = optionalCustomer.get();
        } else {
            customer = new Customer();
            customer.setName(name);
            customer.setPhone(phone);
            customer.setCreateTime(LocalDateTime.now());
            customer = customerRepository.save(customer);
        }

        for (String barcode : barcodes) {
            Optional<Product> optionalProduct = productsRepository.findByBarcode(barcode);
            Product product;
            if (optionalProduct.isPresent()) {
                product = optionalProduct.get();
            } else {
                throw new IllegalArgumentException("Invalid product barcode: " + barcode);
            }



            Cart cart = new Cart();
//            cart.setCustomer(customer);
//            cart.setProduct(product);
            cart.setCusPhone(customer.getPhone());
            cart.setProBarcode(product.getBarcode());
            cart.setCreateTime(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }
}
