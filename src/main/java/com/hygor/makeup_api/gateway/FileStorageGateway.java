package com.hygor.makeup_api.gateway;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageGateway {
    // Retorna a URL pública da imagem
    String uploadFile(MultipartFile file, String folderName);
    
    // Deleta a imagem
    void deleteFile(String fileUrl);
}