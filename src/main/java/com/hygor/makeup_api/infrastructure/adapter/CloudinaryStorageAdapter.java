package com.hygor.makeup_api.infrastructure.adapter;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.gateway.FileStorageGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
@Profile("prod")
@Slf4j
public class CloudinaryStorageAdapter implements FileStorageGateway {

    private final Cloudinary cloudinary;

    public CloudinaryStorageAdapter(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {
        
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        try {
            log.info("Enviando arquivo para Cloudinary...");
            
            // Define ID único e pasta
            String filename = UUID.randomUUID().toString();
            
            Map params = ObjectUtils.asMap(
                    "public_id", filename,
                    "folder", folderName, // ex: "produtos", "avatares"
                    "overwrite", true,
                    "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            
            // Retorna a URL segura (https)
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            log.error("Erro upload Cloudinary", e);
            throw new BusinessException("Falha ao salvar imagem.");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extrai o public_id da URL (lógica simples, pode melhorar com regex)
            // URL Ex: https://res.cloudinary.com/demo/image/upload/v123/folder/imagem.jpg
            String publicId = extractPublicId(fileUrl);
            
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Arquivo deletado: {}", publicId);
            
        } catch (Exception e) {
            log.warn("Erro ao deletar imagem no Cloudinary: {}", e.getMessage());
            // Não lança erro para não travar o processo principal (ex: deletar produto)
        }
    }
    
    // Método auxiliar para pegar o ID da URL
    private String extractPublicId(String url) {
        // Implementação simplificada: Pega o que está depois da última barra e antes do ponto
        // Para produção, use regex robusto ou guarde o public_id no banco
        String temp = url.substring(url.lastIndexOf("/") + 1);
        return temp.substring(0, temp.lastIndexOf("."));
    }
}