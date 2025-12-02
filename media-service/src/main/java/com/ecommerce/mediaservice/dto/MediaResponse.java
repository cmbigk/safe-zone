package com.ecommerce.mediaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {
    private String id;
    private String filename;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String url;
    private String uploadedBy;
    private String productId;
    private LocalDateTime uploadedAt;
}
