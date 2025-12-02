package com.ecommerce.mediaservice.repository;

import com.ecommerce.mediaservice.model.Media;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends MongoRepository<Media, String> {
    List<Media> findByProductId(String productId);
    List<Media> findByUploadedBy(String uploadedBy);
}
