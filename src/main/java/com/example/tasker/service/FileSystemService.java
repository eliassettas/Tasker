package com.example.tasker.service;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.tasker.exception.CustomException;

@Service
public class FileSystemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemService.class);

    private static final int SCALED_WIDTH = 400;
    private static final int SCALED_HEIGHT = 400;

    @Value("${constants.file-system.path}")
    private String fileSystemPath;

    public String storeUserImage(Integer userId, MultipartFile image) throws CustomException {
        String userImagesPath = constructUserImagesPath(userId);
        createDirectories(userImagesPath);

        BufferedImage scaledImage = createScaledImage(image);
        String imagePath = userImagesPath + image.getOriginalFilename();

        try {
            LOGGER.info("Storing image on path: {}", imagePath);
            ImageIO.write(scaledImage, "jpg", new File(imagePath));
        } catch (IOException exception) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store the image in the file system");
        }

        return imagePath;
    }

    private String constructUserImagesPath(Integer userId) {
        return fileSystemPath + "/user_" + userId + "/images/";
    }

    private void createDirectories(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException exception) {
            LOGGER.error("Failed to create initial directories for {}", fileSystemPath);
        }
    }

    private BufferedImage createScaledImage(MultipartFile multipartFile) throws CustomException {
        try (InputStream inputStream = multipartFile.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            AffineTransformOp scaleOp = getScaleOp(image);
            Rectangle2D bounds = scaleOp.getBounds2D(image);
            BufferedImage scaledImage = new BufferedImage((int) bounds.getWidth(), (int) bounds.getHeight(), image.getType());
            return scaleOp.filter(image, scaledImage);
        } catch (IOException exception) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to resize image before storing");
        }
    }

    private AffineTransformOp getScaleOp(BufferedImage image) {
        double sx = image.getWidth() > SCALED_WIDTH
                ? SCALED_WIDTH / (double) image.getWidth()
                : 1;
        double sy = image.getHeight() > SCALED_HEIGHT
                ? SCALED_HEIGHT / (double) image.getHeight()
                : 1;
        double scale = Math.min(sx, sy);

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.scale(scale, scale);
        return new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
    }

    public byte[] loadUserImage(String filePath) throws CustomException {
        try {
            Path path = new File(filePath).toPath();
            return Files.readAllBytes(path);
        } catch (IOException exception) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load the image from the file system");
        }
    }

    public byte[] loadUserImageBase64(String filePath) throws CustomException {
        return Base64.getEncoder().encode(loadUserImage(filePath));
    }
}
