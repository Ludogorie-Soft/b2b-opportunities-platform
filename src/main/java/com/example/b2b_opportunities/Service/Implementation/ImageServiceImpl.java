package com.example.b2b_opportunities.Service.Implementation;

import com.example.b2b_opportunities.Exception.ServerErrorException;
import com.example.b2b_opportunities.Service.Interface.ImageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final MinioClient minioClient;

    @Value("${storage.bucketName}")
    private String bucketName;

    @Value("${storage.url}")
    private String storageUrl;

    @PostConstruct
    private void init() {
        // Change storageUrl to localhost if it's set to http://minio:9000 - to make it work in docker(FE)
        if (storageUrl.toLowerCase().contains("minio:9000")) {
            storageUrl = "http://localhost:9000";
        }
    }

    @Override
    public String upload(MultipartFile file, Long companyId, String imageName) {
        log.info("Attempting to upload image for company ID: {}", companyId);
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
            log.info("Uploaded image for company ID: {}", companyId);

            return storageUrl + "/" + bucketName + "/" + companyId + "/" + imageName;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ServerErrorException("Error occurred while uploading file: " + e.getMessage());
        }
    }

    @Override
    public String returnUrlIfPictureExists(Long companyId, String imageName) {
        if (doesImageExist(companyId, imageName)) {
            return storageUrl + "/" + bucketName + "/" + companyId + "/" + imageName;
        }
        return null;
    }

    @Override
    public void deleteBanner(Long companyId) {
        delete(companyId, "banner");
    }

    @Override
    public void deleteImage(Long companyId) {
        delete(companyId, "image");
    }

    @Override
    public void delete(Long companyId, String imageOrBanner) {
        try {
            RemoveObjectArgs rArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(companyId + "/" + imageOrBanner)
                    .build();
            minioClient.removeObject(rArgs);
            log.info("Deleted " + imageOrBanner + " for company ID: {}", companyId);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ServerErrorException("Error occurred while deleting file: " + e.getMessage());
        }
    }

    @Override
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