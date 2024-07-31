package com.EticPlus_POC.service;

import com.EticPlus_POC.models.StoreCategory;
import com.EticPlus_POC.repository.StoreCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreCategoryService {

    @Autowired
    private StoreCategoryRepository storeCategoryRepository;

    public List<StoreCategory> getAllCategories() {
        return storeCategoryRepository.findAll();
    }

    public StoreCategory findById(String id) {
        return storeCategoryRepository.findById(id).orElse(null);
    }

    public StoreCategory findByName(String name) {
        return storeCategoryRepository.findByName(name);  // Önceden tanımlı özel bir sorgu ile
    }
}
