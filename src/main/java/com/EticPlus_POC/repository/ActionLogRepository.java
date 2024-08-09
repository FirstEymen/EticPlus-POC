package com.EticPlus_POC.repository;

import com.EticPlus_POC.models.ActionLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ActionLogRepository extends MongoRepository<ActionLog, String> {
    List<ActionLog> findByStoreName(String storeName);
}
