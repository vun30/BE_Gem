package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.*;
import online.gemfpt.BE.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BillService {
    @Autowired
    ProductsRepository productsRepository;

    @Autowired
    BillRepository billRepository;

    @Autowired
    BillItemRepository billItemRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    DiscountProductRepository discountProductRepository;
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

        Bill bill = new Bill();
        bill.setCustomerName(name);
        bill.setCustomerPhone(phone);
        bill.setCreateTime(LocalDateTime.now());
        bill.setStatus(true);
        // Khởi tạo danh sách items nếu chưa được khởi tạo
        if (bill.getItems() == null) {
            bill.setItems(new ArrayList<>());
        }

        double totalAmount = 0;

        for (String barcode : barcodes) {
            Optional<Product> optionalProduct = productsRepository.findByBarcode(barcode);
            Product product;
            if (optionalProduct.isPresent()) {
                product = optionalProduct.get();
            } else {
                throw new IllegalArgumentException("Invalid product barcode: " + barcode);
            }

        List<DiscountProduct> discountProduct = discountProductRepository.findByProductAndIsActive(product, true);
            double discount = 0;
            if(!discountProduct.isEmpty()){
                discount = discountProduct.get(0).getDiscountValue();
            }

            double discountedPrice = product.getPrice() - (product.getPrice() * discount / 100);
            product.setNewPrice(discountedPrice);

            int newQuantity = product.getStock() - 1;
            if (newQuantity < 0) {
                throw new IllegalArgumentException("Not enough stock for product: " + barcode);
            }
            BillItem billItem = new BillItem();
            billItem.setBill(bill);
            billItem.setProduct_barcode(product.getBarcode());
            billItem.setQuantity(1); // Giả định số lượng là 1 để đơn giản hóa
            billItem.setPrice(product.getPrice());
            billItem.setDiscount(discount);
            billItem.setNewPrice(product.getNewPrice());
            product.setStock(newQuantity);
            totalAmount += product.getPrice();
            bill.getItems().add(billItem);

            productsRepository.save(product);
        }

        double customer_point = totalAmount / 1000;
        customer.setPoints(customer.getPoints() + customer_point);

        bill.setTotalAmount(totalAmount);
        billRepository.save(bill);
    }

    public Bill getBillDetails(long id) {
        Optional<Bill> optionalBill = billRepository.findById(id);
        if (optionalBill.isPresent()) {
            return optionalBill.get();
        } else {
            throw new IllegalArgumentException("Invalid bill ID: " + id);
        }
    }

    public List<Bill> getAllBillOfCustumer(int customerPhone){
        return billRepository.findByCustomerPhone(customerPhone);
    }

    public void deleteBill(long billId){
        Optional<Bill> bill = billRepository.findById(billId);
        if(bill.isPresent()){
            billRepository.deleteById(billId);
        } else {
            throw new IllegalArgumentException("Invalid bill ID :" + billId);
        }
    }
}
