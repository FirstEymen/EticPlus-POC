package com.EticPlus_POC.repository;

import com.EticPlus_POC.models.StoreCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StoreCategoryRepository extends MongoRepository<StoreCategory, String> {
    StoreCategory findByName(String name);
}
