package com.EticPlus_POC.repository;

import com.EticPlus_POC.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByStoreName(String storeName);
}
