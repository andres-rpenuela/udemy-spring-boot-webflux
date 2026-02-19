package com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.impls;

import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageReadService;
import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageReadServiceImpl implements FileStorageReadService {

    @Value("${file.storage.path:../uploads}")
    private String uploadDir;

    private final DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
    private final ObjectMapper objectMapper;

    @Override
    public Flux<DataBuffer> readContentFileAsDataBuffer(String resourceId) {

        if (!FileStorageService.isValidUUID(resourceId)) {
            return Flux.error(new IllegalArgumentException("Invalid resource ID"));
        }

        Path filePath = FileStorageService.amountFileToPath(
                Path.of(uploadDir),
                FileStorageService.buildNameFileUniqueNormalice(UUID.fromString(resourceId))
        );

        return DataBufferUtils.read(
                        filePath,
                        dataBufferFactory,
                        4096
                )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e ->
                        log.error("Error reading file with resource ID {}: {}", resourceId, e.getMessage())
                );
    }

    @Override
    public Flux<ByteArrayOutputStream> readContentFileAsByteArrayOutputStream(String resourceId) {
        return null;
    }

    @Override
    public Mono<FileStorageService.ResourceDetail> getMetadataFileByResourceId(String resourceId) {
        return null;
    }

    @Override
    public Mono<FileStorageService.ResourceContent> getResourceContentByResourceId(String resourceId) {

        if (!FileStorageService.isValidUUID(resourceId)) {
            return Mono.error(new IllegalArgumentException("Invalid UUID"));
        }

        return Mono.fromCallable(() -> {

            UUID uuid = UUID.fromString(resourceId);

            Path filePath = FileStorageService.amountFileToPath(
                    Path.of(uploadDir),
                    FileStorageService.buildNameFileUniqueNormalice(uuid)
            );

            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("File not found");
            }

            FileStorageService.ResourceDetail detail =
                    objectMapper.readValue(
                            Path.of(uploadDir, resourceId + ".json").toFile(),
                            FileStorageService.ResourceDetail.class
                    );

            return new FileStorageService.ResourceContent(
                    uuid,
                    detail.name(),
                    detail.contentType(),
                    detail.size(),
                    filePath
            );

        }).subscribeOn(Schedulers.boundedElastic());
    }


    @Override
    public Mono<Boolean> existsFileByResourceId(String resourceId) {

        return Mono.justOrEmpty(resourceId)
                .filter(FileStorageService::isValidUUID)
                .thenReturn(  resourceId )
                .flatMap(id ->
                        Mono.fromCallable(() -> {
                            Path filePath = FileStorageService.amountFileToPath(
                                    Path.of(uploadDir),
                                    FileStorageService.buildNameFileUniqueNormalice(UUID.fromString( id ) )
                            );
                            return Files.exists(filePath);
                        }).subscribeOn(Schedulers.boundedElastic())
                )
                .defaultIfEmpty(false);
    }

}
