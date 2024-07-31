package com.EticPlus_POC.config;

import com.EticPlus_POC.models.StoreCategory;
import com.EticPlus_POC.repository.StoreCategoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(StoreCategoryRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new StoreCategory(null, "General Store"));
                repository.save(new StoreCategory(null, "Brand Store"));
                repository.save(new StoreCategory(null, "Boutique Store"));
                repository.save(new StoreCategory(null, "Crafts and Hobby Store"));
                repository.save(new StoreCategory(null, "Food and Beverage Store"));
                repository.save(new StoreCategory(null, "Cosmetics and Personal Care Store"));
                repository.save(new StoreCategory(null, "Electronics Store"));
                repository.save(new StoreCategory(null, "Home and Garden Store"));
                repository.save(new StoreCategory(null, "Sports and Outdoor Store"));
                repository.save(new StoreCategory(null, "Books and Music Store"));
                repository.save(new StoreCategory(null, "Kids and Baby Store"));
                repository.save(new StoreCategory(null, "Category-Specific Store"));
            }
        };
    }
}
