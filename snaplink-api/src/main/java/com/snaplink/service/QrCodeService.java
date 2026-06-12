package com.snaplink.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

/**
 * Generates QR codes using ZXing and stores them in S3.
 *
 * <p>Each QR code is a 512×512 PNG encoding the short URL.
 * <p>S3 key pattern: {@code qr/{shortCode}.png}
 */
@Service
public class QrCodeService {

    private static final Logger log = LoggerFactory.getLogger(QrCodeService.class);
    private static final int QR_WIDTH = 512;
    private static final int QR_HEIGHT = 512;
    private static final Duration PRESIGN_DURATION = Duration.ofDays(7);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket:snaplink-qrcodes}")
    private String bucketName;

    @Value("${snaplink.base-url:http://localhost:8080}")
    private String baseUrl;

    public QrCodeService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @PostConstruct
    public void initBucket() {
        try {
            s3Client.headBucket(b -> b.bucket(bucketName));
            log.info("S3 bucket '{}' already exists.", bucketName);
        } catch (NoSuchBucketException e) {
            log.info("S3 bucket '{}' does not exist. Creating it...", bucketName);
            s3Client.createBucket(b -> b.bucket(bucketName));
            log.info("S3 bucket '{}' created successfully.", bucketName);
        } catch (Exception e) {
            // Some mock S3 endpoints return a generic error or different code for missing bucket
            if (e.getMessage() != null && (e.getMessage().contains("404") || e.getMessage().contains("NoSuchBucket"))) {
                try {
                    log.info("S3 bucket '{}' not found (caught general 404). Creating it...", bucketName);
                    s3Client.createBucket(b -> b.bucket(bucketName));
                    log.info("S3 bucket '{}' created successfully.", bucketName);
                } catch (Exception ex) {
                    log.warn("Could not create S3 bucket '{}': {}", bucketName, ex.getMessage());
                }
            } else {
                log.warn("Could not verify or create S3 bucket '{}': {}", bucketName, e.getMessage());
            }
        }
    }

    /**
     * Generates a QR code for the given short code and uploads it to S3.
     *
     * @param shortCode the short code
     * @return the S3 object key (e.g. "qr/abc123.png")
     */
    public String generateAndUpload(String shortCode) {
        String shortUrl = baseUrl + "/" + shortCode;
        String s3Key = "qr/" + shortCode + ".png";

        try {
            byte[] qrBytes = generateQrCode(shortUrl);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType("image/png")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(qrBytes));
            log.info("QR code uploaded: s3://{}/{}", bucketName, s3Key);

            return s3Key;
        } catch (Exception e) {
            log.error("Failed to generate/upload QR code for {}: {}", shortCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generates a pre-signed GET URL for downloading a QR code image.
     *
     * @param s3Key the S3 object key
     * @return pre-signed URL with 7-day expiry
     */
    public String getPresignedUrl(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return null;
        }

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(PRESIGN_DURATION)
                    .getObjectRequest(getRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for {}: {}", s3Key, e.getMessage());
            return null;
        }
    }

    /**
     * Generates a QR code PNG image in memory.
     */
    private byte[] generateQrCode(String text) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter()
                .encode(text, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}
