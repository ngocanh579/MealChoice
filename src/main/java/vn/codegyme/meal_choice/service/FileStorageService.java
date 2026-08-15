package vn.codegyme.meal_choice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadRoot = Paths.get("uploads", "foods");

    // Lưu ảnh món ăn
    public String saveFoodImage(UUID foodId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Ảnh món ăn không được để trống");
        }

        String originalFileName = image.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new RuntimeException("Tên file ảnh không hợp lệ");
        }

        String extension = getFileExtension(originalFileName);

        if (!isImageExtension(extension)) {
            throw new RuntimeException("File tải lên phải là ảnh");
        }

        String fileName = UUID.randomUUID() + extension;
        Path foodDirectory = uploadRoot.resolve(foodId.toString());
        Path targetFile = foodDirectory.resolve(fileName);

        try {
            Files.createDirectories(foodDirectory);

            Files.copy(
                    image.getInputStream(),
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/foods/" + foodId + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu ảnh món ăn", e);
        }
    }

    // Xóa ảnh
    public void deleteFoodImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String relativePath = imageUrl.startsWith("/")
                    ? imageUrl.substring(1)
                    : imageUrl;

            Path filePath = Paths.get(relativePath);

            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Không thể xóa ảnh món ăn", e);
        }
    }

    // Lấy phần mở rộng của file
    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf(".");

        if (index == -1) {
            return "";
        }

        return fileName.substring(index).toLowerCase();
    }

    // Kiểm tra định dạng ảnh
    private boolean isImageExtension(String extension) {
        return extension.equals(".jpg")
                || extension.equals(".jpeg")
                || extension.equals(".png")
                || extension.equals(".webp");
    }
}