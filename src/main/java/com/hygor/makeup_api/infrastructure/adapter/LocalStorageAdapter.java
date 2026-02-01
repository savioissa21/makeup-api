package com.hygor.makeup_api.infrastructure.adapter;

import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.gateway.FileStorageGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Component
@Profile("dev") // <--- Só ativa este quando o perfil for 'dev'
@Slf4j
public class LocalStorageAdapter implements FileStorageGateway {

    private final Path root = Paths.get("uploads");

    public LocalStorageAdapter() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectory(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar a pasta de uploads!");
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        try {
            log.info("Salvando arquivo LOCALMENTE (Modo Dev)...");
            
            // Cria subpasta se necessário (ex: uploads/products)
            Path folderPath = this.root.resolve(folderName);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path filePath = folderPath.resolve(fileName);
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Retorna URL completa para conseguir acessar do navegador
            // Ex: http://localhost:8080/uploads/products/nome-arquivo.jpg
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(folderName + "/")
                    .path(fileName)
                    .toUriString();

        } catch (Exception e) {
            throw new BusinessException("Erro ao salvar arquivo local: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Lógica simples para extrair o caminho do arquivo da URL
            // Isso depende de como a URL é gerada acima.
            log.info("Simulando deleção de arquivo local: {}", fileUrl);
            // Em dev, deletar é opcional, mas podes implementar Files.delete(path)
        } catch (Exception e) {
            log.warn("Erro ao deletar arquivo local", e);
        }
    }
}