package com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.impls;

import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageRemoveService;
import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageRemoveServiceImp implements FileStorageRemoveService {

    @Value("${file.storage.path")
    private String fileStoragePath;

    @Override
    public Mono<Void> deleteFile(String resourceId) {

        if( !FileStorageService.isValidUUID(resourceId) ) {
            log.error("Invalid resourceId: {}", resourceId);
            return Mono.error(new IllegalArgumentException("Invalid resourceId: " + resourceId));

        }

        return Mono.fromRunnable(() -> {
                    try {
                        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(fileStoragePath, resourceId));
                        log.info("File with resourceId {} deleted successfully", resourceId);
                    } catch (Exception e) {
                        log.error("Error deleting file with resourceId {}: {}", resourceId, e.getMessage());
                        throw new RuntimeException("Error deleting file: " + e.getMessage(), e);
                    }
                }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .then();

    }
}
