package com.qrcode.walkertech.backend;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

@Controller
public class QRCodeController {

    @GetMapping("/")
    public ResponseEntity<byte[]> getQRCodePage() {
        try {
            Resource resource = new ClassPathResource("resources/static/backend/walkertechqrcodegenerator.html");
            byte[] htmlBytes = resource.getInputStream().readAllBytes();
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao ler o arquivo HTML.".getBytes());
        }
    }

    @GetMapping(value = "/Image/{imageName}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) {
        try {
            BufferedImage image = loadImage(imageName);
            if (image != null) {
                return imageToResponseEntity(image);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private BufferedImage loadImage(String imageName) throws IOException {
        Resource resource = new ClassPathResource("static/Image/" + imageName);
        return ImageIO.read(resource.getInputStream());
    }

    @GetMapping(value = "/generateQRCode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQRCode(@RequestParam("content") String content) {
        if (content == null || content.isEmpty()) {
            return ResponseEntity.badRequest().body("{'error': 'Content is empty'}".getBytes());
        }

        try {
            BufferedImage qrCodeImage = generateQRCodeImage(content);
            return imageToResponseEntity(qrCodeImage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private BufferedImage generateQRCodeImage(String content) throws Exception {
        int width = 200;
        int height = 200;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
        BufferedImage qrCodeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                qrCodeImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return qrCodeImage;
    }

    private ResponseEntity<byte[]> imageToResponseEntity(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return ResponseEntity.ok(imageBytes);
    }
}
