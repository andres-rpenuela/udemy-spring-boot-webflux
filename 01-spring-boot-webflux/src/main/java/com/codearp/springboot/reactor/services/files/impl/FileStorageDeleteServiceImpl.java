package com.codearp.springboot.reactor.services.files.impl;

import com.codearp.springboot.reactor.services.files.FileStorageReadService;
import com.codearp.springboot.reactor.services.files.FileStorageService;
import com.codearp.springboot.reactor.services.files.FileStorageDeleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageDeleteServiceImpl implements FileStorageDeleteService {

    @Value("${config.uploads.path}")
    private String uploadDirProperty;

    private final FileStorageReadService fileStorageReadService;

    /**
     * Elimina el fichero asociado a `resourceId` de forma reactiva.
     * - Valida entrada.
     * - Resuelve la ruta mediante FileStorageService.getPathFileByResourceId(...).
     * - Ejecuta Files.deleteIfExists(...) en Schedulers.boundedElastic() para no bloquear el event-loop.
     * - Si no se resuelve path o el fichero no existe, completa normalmente.
     *
     * @param resourceId identificador del recurso
     * @return Mono<Void> que completa cuando termina la operación
     */
    @Override
    public Mono<Void> deleteFileByResourceId(String resourceId) {
        if (resourceId == null || resourceId.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("resourceId is required"));
        }

        return fileStorageReadService.existsFileByResourceId( resourceId )
                .filter( exists -> exists )
                .flatMap( exists ->
                        FileStorageService.getPathFileByResourceId( uploadDirProperty, resourceId )
                )
                .flatMap( pathFile ->
                {
                    return Mono.fromRunnable(() -> {
                        try {
                            Files.deleteIfExists(pathFile);
                            Files.deleteIfExists(pathFile.resolve(".json")); // Elimina metadatos si existen
                            log.info("Deleted file with resourceId {} at path {}", resourceId, pathFile);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
                        }
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .then()
                .doOnError(e -> log.error("Error deleting file with resourceId {}: {}", resourceId, e.getMessage(), e));
    }
}
