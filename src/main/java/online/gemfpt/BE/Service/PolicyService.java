package online.gemfpt.BE.Service;

import jakarta.transaction.Transactional;
import online.gemfpt.BE.Repository.PolicyRepository;
import online.gemfpt.BE.entity.Policy;
import online.gemfpt.BE.model.PolicyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;

@Service
public class PolicyService {

    @Autowired
    private PolicyRepository  policyRepository;

    @Transactional
    public Policy addPolicy(PolicyRequest  policyRequest) {
        // Set all existing policies to false
        List<Policy> allPolicies = policyRepository.findAll();
        for (Policy policy : allPolicies) {
            policy.setStatus(false);
        }
        policyRepository.saveAll(allPolicies);

        // Create new policy with status true
        Policy newPolicy = new Policy();
        newPolicy.setCreateDate(new Date());
        newPolicy.setStatus(true);
        newPolicy.setDescription(policyRequest.getDescription());

        return policyRepository.save(newPolicy);
    }

    public List<Policy> viewAllPolicies() {
        return policyRepository.findAll();
    }

    public Policy viewActivePolicy() {
        List<Policy> activePolicies = policyRepository.findByStatus(true);
        if (activePolicies.isEmpty()) {
            return null;
        }
        return activePolicies.get(0);
    }
}
