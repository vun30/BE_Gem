package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.BillRepository;
import online.gemfpt.BE.Repository.BillItemRepository;
import online.gemfpt.BE.Repository.CustomerRepository;
import online.gemfpt.BE.Repository.ProductsRepository;
import online.gemfpt.BE.entity.Bill;
import online.gemfpt.BE.entity.BillItem;
import online.gemfpt.BE.entity.Customer;
import online.gemfpt.BE.entity.Product;
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

            BillItem billItem = new BillItem();
            billItem.setBill(bill);
            billItem.setProduct_barcode(product.getBarcode());
            billItem.setQuantity(1); // Giả định số lượng là 1 để đơn giản hóa
            billItem.setPrice(product.getPrice());
            totalAmount += product.getPrice();
            bill.getItems().add(billItem);
        }

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
