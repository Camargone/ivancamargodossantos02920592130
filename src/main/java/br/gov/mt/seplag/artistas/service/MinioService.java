package br.gov.mt.seplag.artistas.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {
 
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.presigned-url-expiry}")
    private int presignedUrlExpiry;

    /**
     * Faz upload de um arquivo para o MinIO
     * @param file arquivo a ser enviado
     * @param albumId ID do álbum associado
     * @return objectKey (caminho do objeto no bucket)
     */
    public String uploadFile(MultipartFile file, Long albumId) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";

            String objectKey = String.format("album-%d/%s%s",
                    albumId,
                    UUID.randomUUID().toString(),
                    extension);

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectKey)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            log.info("Arquivo uploaded com sucesso: {}", objectKey);
            return objectKey;

        } catch (Exception e) {
            log.error("Erro ao fazer upload do arquivo: {}", e.getMessage());
            throw new RuntimeException("Erro ao fazer upload do arquivo", e);
        }
    }

    /**
     * Gera uma URL pré-assinada para acesso ao arquivo com expiração de 30 minutos
     * @param objectKey caminho do objeto no bucket
     * @return URL pré-assinada
     */
    public String getPresignedUrl(String objectKey) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(presignedUrlExpiry, TimeUnit.SECONDS)
                            .build()
            );
            log.debug("URL pré-assinada gerada para: {}", objectKey);
            return url;
        } catch (Exception e) {
            log.error("Erro ao gerar URL pré-assinada: {}", e.getMessage());
            throw new RuntimeException("Erro ao gerar URL pré-assinada", e);
        }
    }

    /**
     * Remove um arquivo do MinIO
     * @param objectKey caminho do objeto no bucket
     */
    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            log.info("Arquivo removido: {}", objectKey);
        } catch (Exception e) {
            log.error("Erro ao remover arquivo: {}", e.getMessage());
            throw new RuntimeException("Erro ao remover arquivo", e);
        }
    }
}
