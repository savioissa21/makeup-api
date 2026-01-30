package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Permission;
import com.hygor.makeup_api.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pela gestão de permissões granulares do sistema.
 * Permite criar e procurar permissões que definem o acesso a funcionalidades específicas.
 */
@Service
public class PermissionService extends BaseService<Permission, PermissionRepository> {

    public PermissionService(PermissionRepository repository) {
        super(repository);
    }

    /**
     * Procura uma permissão específica pelo seu nome único.
     * @param name Nome da permissão (ex: 'PRODUCT_EDIT').
     * @return A permissão encontrada.
     */
    @Transactional(readOnly = true)
    public Permission findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Permissão não encontrada com o nome: " + name));
    }

    /**
     * Cria uma nova permissão no sistema, validando se o nome já existe.
     * @param permission Objecto da permissão a ser guardado.
     * @return A permissão guardada.
     */
    @Transactional
    public Permission create(Permission permission) {
        if (repository.findByName(permission.getName()).isPresent()) {
            throw new RuntimeException("Já existe uma permissão com este nome.");
        }
        return repository.save(permission);
    }
}