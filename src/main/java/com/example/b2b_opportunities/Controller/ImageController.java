package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping(value = "/upload-image", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadImage(@RequestParam("file") MultipartFile file,
                              @RequestParam("company_id") Long companyId) {
        return imageService.upload(file, companyId, "image");
    }

    @PostMapping(value = "/upload-banner", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadBanner(@RequestParam("file") MultipartFile file,
                               @RequestParam("company_id") Long companyId) {
        return imageService.upload(file, companyId, "banner");
    }

    @GetMapping("/get-image/{company_id}")
    public String getImage(@PathVariable("company_id") Long id) {
        return imageService.returnUrlIfPictureExists(id, "image");
    }

    @GetMapping("/get-banner/{company_id}")
    public String getBanner(@PathVariable("company_id") Long id) {
        return imageService.returnUrlIfPictureExists(id, "banner");
    }
}