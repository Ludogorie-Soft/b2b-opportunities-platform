package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Exception.ServerErrorException;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
public class ImageController {
    private final MinioClient minioClient;

    @Value("${storage.bucketName}")
    private String bucketName;

    @Value("${storage.url}")
    private String storageUrl;

    @PostMapping("/upload-image")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadImage(@RequestParam("fileName") String filepath,
                              @RequestParam("company_id") Long companyId) {
        return upload(filepath, companyId, "image");
    }

    @PostMapping("/upload-banner")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadBanner(@RequestParam("fileName") String filepath,
                               @RequestParam("company_id") Long companyId) {
        return upload(filepath, companyId, "banner");
    }

    @GetMapping("/get-image/{company_id}")
    public String getImage(@PathVariable("company_id") Long id) {
        return storageUrl + "/" + bucketName + "/" + id + "/image";
    }

    @GetMapping("/get-banner/{company_id}")
    public String getBanner(@PathVariable("company_id") Long id) {
        return storageUrl + "/" + bucketName + "/" + id + "/banner";
    }

    private String upload(String filepath, Long companyId, String imageName) {
        filepath = filepath.replace("\"", "");
        try {
            Path path = Paths.get(filepath);

            UploadObjectArgs uArgs = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(companyId + "/" + imageName)  // "image.png"
                    .filename(filepath)  // C:\Users\User\Pictures\picture.png
                    .contentType(getContentType(path))  // image/png
                    .build();
            ObjectWriteResponse response = minioClient.uploadObject(uArgs);
            return storageUrl + "/" + bucketName + "/" + companyId + "/" + imageName;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ServerErrorException("Error occurred while uploading file: " + e.getMessage());
        }
    }

    private String getContentType(Path path) throws IOException {
        String contentType = Files.probeContentType(path);
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream"; // Default to binary if not found
        }
        return contentType;
    }
}
