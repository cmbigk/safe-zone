package com.ecommerce.mediaservice.service;

import com.ecommerce.mediaservice.dto.MediaResponse;
import com.ecommerce.mediaservice.exception.ResourceNotFoundException;
import com.ecommerce.mediaservice.model.Media;
import com.ecommerce.mediaservice.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    
    private final MediaRepository mediaRepository;
    private final Tika tika = new Tika();
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${media.upload.dir}")
    private String uploadDir;
    
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    public MediaResponse uploadMedia(MultipartFile file, String uploadedBy, String productId) throws IOException {
        // Validate file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Validate file size (2MB max)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 2MB limit. Your file size: " + 
                    (file.getSize() / 1024 / 1024) + "MB");
        }
        
        // Detect actual content type using Apache Tika (not just trusting the client)
        String detectedContentType = tika.detect(file.getInputStream());
        log.info("Detected content type: {}", detectedContentType);
        
        // Validate file type - MUST be an image
        if (!ALLOWED_IMAGE_TYPES.contains(detectedContentType)) {
            throw new IllegalArgumentException("Only image files are allowed (JPEG, PNG, GIF, WebP). " +
                    "Detected type: " + detectedContentType);
        }
        
        // Create upload directory if it doesn't exist
        Files.createDirectories(Paths.get(uploadDir));
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String filename = UUID.randomUUID().toString() + extension;
        Path filepath = Paths.get(uploadDir, filename);
        
        // Save file
        Files.write(filepath, file.getBytes());
        log.info("File saved: {}", filepath);
        
        // Save metadata to MongoDB
        Media media = new Media();
        media.setFilename(filename);
        media.setOriginalFilename(originalFilename);
        media.setContentType(detectedContentType);
        media.setFileSize(file.getSize());
        media.setFilePath(filepath.toString());
        media.setUploadedBy(uploadedBy);
        media.setProductId(productId);
        media.setUploadedAt(LocalDateTime.now());
        
        Media savedMedia = mediaRepository.save(media);
        log.info("Media metadata saved: {} for product: {}", savedMedia.getId(), productId);
        
        // Publish event to Kafka
        if (kafkaTemplate != null) {
            String eventData = "IMAGE_UPLOADED:" + savedMedia.getId() + ":" + uploadedBy + ":" + (productId != null ? productId : "no-product");
            kafkaTemplate.send("media-events", eventData);
        }
        
        return mapToMediaResponse(savedMedia);
    }
    
    public MediaResponse getMediaById(String id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
        
        return mapToMediaResponse(media);
    }
    
    public List<MediaResponse> getMediaByProductId(String productId) {
        return mediaRepository.findByProductId(productId).stream()
                .map(this::mapToMediaResponse)
                .collect(Collectors.toList());
    }
    
    public List<MediaResponse> getMediaByUser(String userEmail) {
        return mediaRepository.findByUploadedBy(userEmail).stream()
                .map(this::mapToMediaResponse)
                .collect(Collectors.toList());
    }
    
    public void deleteMedia(String id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
        
        // Delete file from disk
        try {
            Path filepath = Paths.get(media.getFilePath());
            Files.deleteIfExists(filepath);
            log.info("File deleted: {}", filepath);
        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage());
        }
        
        // Delete metadata from database
        mediaRepository.deleteById(id);
        log.info("Media metadata deleted: {}", id);
    }
    
    private MediaResponse mapToMediaResponse(Media media) {
        String url = "/api/media/files/" + media.getId();
        return new MediaResponse(
                media.getId(),
                media.getFilename(),
                media.getOriginalFilename(),
                media.getContentType(),
                media.getFileSize(),
                url,
                media.getUploadedBy(),
                media.getProductId(),
                media.getUploadedAt()
        );
    }
}
