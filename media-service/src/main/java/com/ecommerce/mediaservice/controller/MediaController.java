package com.ecommerce.mediaservice.controller;

import com.ecommerce.mediaservice.dto.MediaResponse;
import com.ecommerce.mediaservice.service.MediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "*")
public class MediaController {
    
    private final MediaService mediaService;
    
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }
    
    @Value("${media.upload.dir}")
    private String uploadDir;
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "productId", required = false) String productId,
            @RequestHeader("X-User-Email") String uploadedBy) throws IOException {
        MediaResponse response = mediaService.uploadMedia(file, uploadedBy, productId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MediaResponse> getMediaById(@PathVariable String id) {
        MediaResponse response = mediaService.getMediaById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<MediaResponse>> getMediaByProduct(@PathVariable String productId) {
        List<MediaResponse> media = mediaService.getMediaByProductId(productId);
        return ResponseEntity.ok(media);
    }
    
    @GetMapping("/user/{email}")
    public ResponseEntity<List<MediaResponse>> getMediaByUser(@PathVariable String email) {
        List<MediaResponse> media = mediaService.getMediaByUser(email);
        return ResponseEntity.ok(media);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable String id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> serveFile(@PathVariable String id) {
        try {
            // Get media metadata from database using the ID
            MediaResponse mediaResponse = mediaService.getMediaById(id);
            
            // Resolve the actual file path
            Path file = Paths.get(uploadDir).resolve(mediaResponse.getFilename());
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(mediaResponse.getContentType()))
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read file: " + mediaResponse.getFilename());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
