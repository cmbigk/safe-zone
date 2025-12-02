package com.ecommerce.mediaservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Media {
    @Id
    private String id;
    private String filename;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String filePath;
    private String uploadedBy;
    private String productId;
    private LocalDateTime uploadedAt;
}
