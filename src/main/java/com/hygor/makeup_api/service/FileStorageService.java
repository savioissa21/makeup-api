package com.hygor.makeup_api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads");

    public FileStorageService() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectory(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar a pasta de uploads!");
        }
    }

    public String saveImage(MultipartFile file) {
        try {
            // Gera um nome único para a imagem (evita sobreposição)
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.root.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName; // Retorna o nome/caminho para guardar na base de dados
        } catch (Exception e) {
            throw new RuntimeException("Erro ao guardar a imagem: " + e.getMessage());
        }
    }
}