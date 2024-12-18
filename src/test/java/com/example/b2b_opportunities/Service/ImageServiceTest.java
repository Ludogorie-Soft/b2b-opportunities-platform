package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.PositionApplication;
import com.example.b2b_opportunities.Exception.ServerErrorException;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.ErrorResponse;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageServiceTest {
    @InjectMocks
    ImageService imageService;

    @Mock
    MinioClient minioClient;

    @Mock
    MultipartFile multipartFile;

    @Mock
    private Logger log;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(imageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(imageService, "storageUrl", "http://localhost:9000");
    }

    @Test
    void testUpload() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        InputStream inputStream = mock(InputStream.class);
        when(file.getInputStream()).thenReturn(inputStream);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");

        Position position = new Position();
        position.setId(1L);

        PositionApplication application = new PositionApplication();
        application.setId(1L);
        application.setPosition(position);

        String url = imageService.upload(file, 1L, "image");

        assertNotNull(url);
        assertEquals(url, "http://localhost:9000/test-bucket/1/image");
    }

    @Test
    void whenUploadingImageFailsShouldThrowServerErrorException() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenThrow(new IOException("IO error"));

        assertThrows(ServerErrorException.class, () -> {
            imageService.upload(mockFile, 123L, "image");
        });
    }

    @Test
    void shouldReturnFalseWhenObjectDoesNotExist() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        ErrorResponse errorResponse = new ErrorResponse("NoSuchKey", "Object not found", "test", "text/plain", null, null, null);
        doThrow(new ErrorResponseException(errorResponse, null, null))
                .when(minioClient).statObject(any(StatObjectArgs.class));

        boolean result = imageService.doesImageExist(1L, "image");

        assertFalse(result);
    }

    @Test
    void shouldThrowServerErrorExceptionWhenMinioThrowsOtherErrorResponse() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse("UnexpectedError", "testMessage", null, null, null, null, null);
        doThrow(new ErrorResponseException(errorResponse, null, null))
                .when(minioClient).statObject(any(StatObjectArgs.class));

        ServerErrorException exception = Assertions.assertThrows(ServerErrorException.class, () -> {
            imageService.doesImageExist(1L, "image");
        });

        assertEquals("Error occurred while checking image existence: testMessage", exception.getMessage());
    }

    @Test
    void shouldThrowServerErrorExceptionWhenMinioThrowsOtherExceptions() throws Exception {
        doThrow(new IOException("I/O error occurred"))
                .when(minioClient).statObject(any(StatObjectArgs.class));

        ServerErrorException exception = Assertions.assertThrows(ServerErrorException.class, () -> {
            imageService.doesImageExist(1L, "image");
        });

        assertTrue(exception.getMessage().contains("Error occurred while checking object existence:"));
    }

    @Test
    void shouldReturnUrlWhenPictureExists() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(null);

        boolean exists = imageService.doesImageExist(1L, "image");

        assertTrue(exists);
    }

    @Test
    void testReturnUrlIfCVExists() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(null);

        String url = imageService.returnUrlIfPictureExists(1L, "image");

        assertNotNull(url);
        assertEquals(url, "http://localhost:9000/test-bucket/1/image");
    }

    @Test
    void shouldDeleteImageSuccessfully() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Long companyId = 123L;

        imageService.deleteImage(companyId);

        ArgumentCaptor<RemoveObjectArgs> removeObjectArgsCaptor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(removeObjectArgsCaptor.capture());

        RemoveObjectArgs capturedArgs = removeObjectArgsCaptor.getValue();
        assertEquals("test-bucket", capturedArgs.bucket());
        assertEquals("123/image", capturedArgs.object());
    }

    @Test
    void shouldDeleteBannerSuccessfully() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Long companyId = 123L;

        imageService.deleteBanner(companyId);

        ArgumentCaptor<RemoveObjectArgs> removeObjectArgsCaptor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(removeObjectArgsCaptor.capture());

        RemoveObjectArgs capturedArgs = removeObjectArgsCaptor.getValue();
        assertEquals("test-bucket", capturedArgs.bucket());
        assertEquals("123/banner", capturedArgs.object());
    }

    @Test
    void shouldThrowServerErrorExceptionOnIOException() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Long companyId = 123L;
        String imageOrBanner = "image.jpg";

        doThrow(IOException.class).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThrows(ServerErrorException.class, () -> imageService.delete(companyId, imageOrBanner));

        verify(log, never()).info(anyString(), Optional.ofNullable(any()));
    }

}
