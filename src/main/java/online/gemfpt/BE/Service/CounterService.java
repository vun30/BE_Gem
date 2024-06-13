package online.gemfpt.BE.service;

import online.gemfpt.BE.entity.Counter;
import online.gemfpt.BE.Repository.CounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CounterService {
    @Autowired
    private CounterRepository counterRepository;

    public Counter saveCounter(Counter counter) {
        return counterRepository.save(counter);
    }

    public Counter getCounterById(long id) {
        return counterRepository.findById(id).orElse(null);
    }

    public List<Counter> getAllCounters() {
        return counterRepository.findAll();
    }

    public void deleteCounter(long id) {
        counterRepository.deleteById(id);
    }
}
