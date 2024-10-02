package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Exception.ServerErrorException;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final MinioClient minioClient;

    @Value("${storage.bucketName}")
    private String bucketName;

    @Value("${storage.url}")
    private String storageUrl;

    @PostConstruct
    private void init() {
        // Change storageUrl if it's set to http://minio:9000 - to make it work while testing in docker.
        if (storageUrl.toLowerCase().contains("minio")) {
            storageUrl = "http://localhost:9000";
        }
    }
    public String upload(MultipartFile file, Long companyId, String imageName) {
        try {
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);

            UploadObjectArgs uArgs = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(companyId + "/" + imageName)
                    .filename(tempFile.getAbsolutePath())
                    .contentType(file.getContentType())
                    .build();

            minioClient.uploadObject(uArgs);

            tempFile.delete();

            return storageUrl + "/" + bucketName + "/" + companyId + "/" + imageName;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ServerErrorException("Error occurred while uploading file: " + e.getMessage());
        }
    }

    public String returnUrlIfPictureExists(Long companyId, String imageName) {
        String objectName = companyId + "/" + imageName;

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            return storageUrl + "/" + bucketName + "/" + objectName;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new NotFoundException(capitalize(imageName) + " not found for company ID: " + companyId);
            } else {
                throw new ServerErrorException("Error occurred while checking " + imageName + " existence: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new ServerErrorException(e.getMessage());
        }
    }

    private String capitalize(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return imageName;
        }
        return Character.toUpperCase(imageName.charAt(0)) + imageName.substring(1);
    }
}