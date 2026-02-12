package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import com.skillbridge.lms.exception.BadRequestException;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
    }

    @Test
    @DisplayName("storeFile - JPEG保存成功")
    void storeFile_jpeg_success() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1024]));

        String result = fileStorageService.storeFile(file);

        assertThat(result).startsWith("/api/files/");
        assertThat(result).endsWith(".jpg");
    }

    @Test
    @DisplayName("storeFile - 空ファイルでエラー")
    void storeFile_emptyFile_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> fileStorageService.storeFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("空");
    }

    @Test
    @DisplayName("storeFile - サイズ超過でエラー")
    void storeFile_tooLarge_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10 * 1024 * 1024L);

        assertThatThrownBy(() -> fileStorageService.storeFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("5MB");
    }

    @Test
    @DisplayName("storeFile - 許可されない形式でエラー")
    void storeFile_invalidType_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");

        assertThatThrownBy(() -> fileStorageService.storeFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("許可されていない");
    }

    @Test
    @DisplayName("storeFile - PNG保存成功")
    void storeFile_png_success() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(2048L);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[2048]));

        String result = fileStorageService.storeFile(file);

        assertThat(result).startsWith("/api/files/");
        assertThat(result).endsWith(".png");
    }

    @Test
    @DisplayName("getFilePath - パス取得")
    void getFilePath_returnsCorrectPath() {
        Path path = fileStorageService.getFilePath("test.jpg");

        assertThat(path.getFileName().toString()).isEqualTo("test.jpg");
    }
}
