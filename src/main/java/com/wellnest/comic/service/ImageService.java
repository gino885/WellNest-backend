package com.wellnest.comic.service;

import com.luciad.imageio.webp.WebPReadParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImageService {

    private static final int MAX_IMAGES_PER_GROUP = 4;
    private static final int WIDTH = 512;
    private static final int HEIGHT = 512;
    private static final int THREAD_POOL_SIZE = 4;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    List<List<Integer>> groupIndices = Arrays.asList(
            Arrays.asList(2, 1, 0, 3),
            Arrays.asList(4, 5, 6,7),
            Arrays.asList(8, 9)
    );
    public List<String> mergeImages(List<String> imageUrls, String[] captions) throws IOException, InterruptedException {
        List<String> filePaths = new ArrayList<>();
        String[][] caption = {
                {captions[0], captions[1], captions[2], captions[3]},
                {captions[4], captions[5], captions[6], captions[7]},
                {captions[8], captions[9]},
        };

        for (int i = 0; i < groupIndices.size(); i++) {
            List<Integer> groupIndex = groupIndices.get(i);
            List<String> group = new ArrayList<>();
            for (int index : groupIndex) {
                group.add(imageUrls.get(index));
            }

            BufferedImage combinedImage = mergeImageGroup(group);
            if (combinedImage != null) {
                if (captions != null && i < caption.length) {
                    combinedImage = addFrameWithText(combinedImage, caption[i]);
                }

                String filePath = saveImageToFile(combinedImage, i);
                filePaths.add(filePath);
            } else {
                log.error("Failed to merge image group at index {}", i);
            }
        }

        return filePaths;
    }


    private BufferedImage mergeImageGroup(List<String> imageUrls) throws IOException {
        int cols = Math.min(2, imageUrls.size());
        int rows = (int) Math.ceil((double) imageUrls.size() / cols);

        BufferedImage combinedImage = new BufferedImage(WIDTH * cols, HEIGHT * rows + 50 * rows, BufferedImage.TYPE_INT_ARGB); // 增加高度以容纳文本框
        Graphics2D g = combinedImage.createGraphics();

        List<CompletableFuture<BufferedImage>> futures = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            futures.add(loadImageWithRetry(imageUrl));
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error waiting for image loading completion", e);
            Thread.currentThread().interrupt();
        }

        int i = 0;
        for (CompletableFuture<BufferedImage> future : futures) {
            try {
                BufferedImage image = future.get();
                if (image != null) {
                    image = convertToRGB(image); // 转换为 RGB 颜色空间
                    int x = (i % cols) * WIDTH;
                    int y = (i / cols) * (HEIGHT + 50); // 调整 y 坐标以容纳文本框
                    g.drawImage(image, x, y, WIDTH, HEIGHT, null);
                } else {
                    log.warn("Failed to load image from URL");
                }
            } catch (ExecutionException | InterruptedException e) {
                log.error("Error retrieving loaded image", e);
                Thread.currentThread().interrupt();
            }
            i++;
        }

        g.dispose();
        return combinedImage;
    }

    private CompletableFuture<BufferedImage> loadImageWithRetry(String imageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    if (isUrlAccessible(imageUrl)) {
                        return loadImage(imageUrl);
                    } else {
                        log.warn("URL not accessible: {}", imageUrl);
                    }
                } catch (IOException e) {
                    log.error("Error loading image from URL: {} on attempt {}", imageUrl, attempt, e);
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    log.error("Retry interrupted", e);
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            return null;
        }, executorService);
    }

    private boolean isUrlAccessible(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            int responseCode = connection.getResponseCode();
            return (responseCode == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            log.error("Error checking URL accessibility: {}", imageUrl, e);
            return false;
        }
    }

    private BufferedImage loadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        return readWebP(url);
    }

    private BufferedImage readWebP(URL url) throws IOException {
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(url.openStream())) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType("image/webp");
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(inputStream, true, true);
                WebPReadParam param = new WebPReadParam();
                return reader.read(0, param);
            } else {
                throw new IOException("No WebP reader found");
            }
        }
    }

    private BufferedImage convertToRGB(BufferedImage image) {
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgbImage;
    }

    private BufferedImage addFrameWithText(BufferedImage image, String[] texts) {
        int frameThickness = 10;
        int cornerRadius = 20;
        int textBoxHeight = 50;
        int width = image.getWidth();
        int height = image.getHeight() + textBoxHeight; // 增加高度以容纳文本框
        BufferedImage framedImage = new BufferedImage(width + frameThickness * 2, height + frameThickness * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = framedImage.createGraphics();

        // Draw the frame with rounded corners
        g.setColor(Color.BLACK);
        g.fill(new RoundRectangle2D.Double(0, 0, framedImage.getWidth(), framedImage.getHeight(), cornerRadius, cornerRadius));

        // Draw the original image on top of the frame with rounded corners
        g.setClip(new RoundRectangle2D.Double(frameThickness, frameThickness, image.getWidth(), image.getHeight(), cornerRadius, cornerRadius));
        g.drawImage(image, frameThickness, frameThickness, image.getWidth(), image.getHeight(), null);

        // Draw text boxes
        g.setClip(null); // Reset clip to draw text boxes
        g.setFont(new Font("宋体", Font.BOLD, 24));
        g.setColor(Color.WHITE);

        int cols = Math.min(2, texts.length);
        int rows = (int) Math.ceil((double) texts.length / cols);

        for (int i = 0; i < texts.length; i++) {
            int x = (i % cols) * WIDTH + frameThickness;
            int y = (i / cols) * (HEIGHT + textBoxHeight) + HEIGHT + frameThickness; // Adjust y to place the text box below the image
            g.fillRoundRect(x, y, WIDTH - frameThickness * 2, textBoxHeight - frameThickness, cornerRadius, cornerRadius);
            g.setColor(Color.BLACK);
            drawCenteredString(g, texts[i], new Rectangle(x, y, WIDTH - frameThickness * 2, textBoxHeight - frameThickness));
            g.setColor(Color.WHITE);
        }

        g.dispose();
        return framedImage;
    }

    private void drawCenteredString(Graphics2D g, String text, Rectangle rect) {
        FontRenderContext frc = g.getFontRenderContext();
        TextLayout layout = new TextLayout(text, g.getFont(), frc);

        float drawPosX = rect.x + (rect.width - layout.getAdvance()) / 2;
        float drawPosY = rect.y + ((rect.height - layout.getAscent() - layout.getDescent()) / 2 + layout.getAscent());

        layout.draw(g, drawPosX, drawPosY);
    }

    private String saveImageToFile(BufferedImage image, int index) throws IOException {
        String directoryPath = "src/picture";
        File directory = new File(directoryPath);

        // 如果資料夾不存在，則創建它
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                log.error("Failed to create directory: {}", directoryPath);
                throw new IOException("Failed to create directory: " + directoryPath);
            }
        }

        String filePath = directoryPath + "/comic_" + index + ".jpg";
        File outputfile = new File(filePath);

        BufferedImage rgbImage = convertToRGB(image); // Ensure the image is in RGB colorspace

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.8f); // Adjust the compression quality as needed

        try (FileImageOutputStream imageOutputStream = new FileImageOutputStream(outputfile)) {
            writer.setOutput(imageOutputStream);
            writer.write(null, new javax.imageio.IIOImage(rgbImage, null, null), param);
            writer.dispose();
        }

        return filePath;
    }
}
