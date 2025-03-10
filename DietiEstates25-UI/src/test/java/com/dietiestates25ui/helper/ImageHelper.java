package com.dietiestates25ui.helper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageHelper {
    public static File createImageWithSpecificFileSize(String fileName, long fileSizeInBytes) {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        File directory = new File("src/test/resources/images/");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File imageFile = new File(directory, fileName);
        try {
            ImageIO.write(image, "png", imageFile);
            long currentSize = imageFile.length();
            if (currentSize < fileSizeInBytes) {
                long paddingSize = fileSizeInBytes - currentSize;
                try (FileOutputStream fos = new FileOutputStream(imageFile, true)) {
                    byte[] padding = new byte[1024];
                    while (paddingSize > 0) {
                        int bytesToWrite = (int) Math.min(padding.length, paddingSize);
                        fos.write(padding, 0, bytesToWrite);
                        paddingSize -= bytesToWrite;
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return imageFile;
    }

    public static File createImageWithSpecificSize(String fileName, int width, int height) {
        File directory = new File("src/test/resources/images/");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File imageFile = new File(directory, fileName);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {
            ImageIO.write(image, "png", imageFile);
        } catch (IOException e) {
            return null;
        }

        return imageFile;
    }

}
