package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Exception.ServerErrorException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
            // Use the input stream directly from the MultipartFile
            InputStream inputStream = file.getInputStream();

            // Upload the object by streaming the file directly to Minio
            //PutObjectArgs is better for its efficiency, performance, and simplicity than UploadObjectArgs
            PutObjectArgs pArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(companyId + "/" + imageName)
                    .stream(inputStream, file.getSize(), -1) // Pass the file size and unknown part size (-1)
                    .contentType(file.getContentType())
                    .build();

            minioClient.putObject(pArgs);

            inputStream.close();

            return storageUrl + "/" + bucketName + "/" + companyId + "/" + imageName;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ServerErrorException("Error occurred while uploading file: " + e.getMessage());
        }
    }

    public String returnUrlIfPictureExists(Long companyId, String imageName) {
        if (doesImageExist(companyId, imageName)) {
            return storageUrl + "/" + bucketName + "/" + companyId + "/" + imageName;
        }
        return null;
    }

    public void deleteBanner(Long companyId) {
        try {
            RemoveObjectArgs rArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(companyId + "/banner")
                    .build();
            minioClient.removeObject(rArgs);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ServerErrorException("Error occurred while deleting file: " + e.getMessage());
        }
    }

    public boolean doesImageExist(Long companyId, String imageName) {
        try {
            String objectName = companyId + "/" + imageName;

            // Check if the object exists by fetching its metadata
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            return true;
        } catch (ErrorResponseException e) {
            // If it's a "NoSuchKey" error, it means the object doesn't exist
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            } else {
                throw new ServerErrorException("Error occurred while checking " + imageName + " existence: " + e.getMessage());
            }
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ServerErrorException("Error occurred while checking object existence: " + e.getMessage());
        }
    }
}