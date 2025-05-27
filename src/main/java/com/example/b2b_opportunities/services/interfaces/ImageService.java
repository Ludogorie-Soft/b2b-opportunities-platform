package com.example.b2b_opportunities.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String upload(MultipartFile file, Long companyId, String imageName);

    String returnUrlIfPictureExists(Long companyId, String imageName);

    void deleteBanner(Long companyId);

    void deleteImage(Long companyId);

    void delete(Long companyId, String imageOrBanner);

    boolean doesImageExist(Long companyId, String imageName);
}
