package com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.impls;

import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageService;
import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageWriterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageWriterServiceImpl implements FileStorageWriterService {

    @Getter @Setter
    @Value("${file.storage.path:../uploads}")
    private String uploadDir;

    private final ObjectMapper objectMapper;

    @Override
    public Mono<FileStorageService.ResourceUUID> saveFile(FilePart filePart) {

        return populationSaveAndUpdate(filePart)
                .doOnError(e -> log.error("Error saving file {}: {}", filePart.filename(), e.getMessage()));
    }

    @Override
    public Mono<FileStorageService.ResourceUUID> updateFile(String resourceIdOld, FilePart filePart) {

        if (!FileStorageService.isValidUUID(resourceIdOld)) {
            return Mono.error(new IllegalArgumentException("Resource ID old must not be null or blank"));
        }

        return populationSaveAndUpdate(filePart)
                .flatMap(newResourceId -> {

                    Path oldFilePath = FileStorageService.amountFileToPath(
                            Path.of(uploadDir),
                            FileStorageService.buildNameFileUniqueNormalice(UUID.fromString(resourceIdOld))
                    );

                    Path oldJsonPath = Path.of(uploadDir, resourceIdOld + ".json");

                    return Mono.fromRunnable(() -> {
                                try {
                                    Files.deleteIfExists(oldFilePath);
                                    Files.deleteIfExists(oldJsonPath);
                                    log.info("Old file and JSON for resource ID {} deleted successfully if exists", resourceIdOld);
                                } catch (IOException e) {
                                    log.error("Error deleting old file or JSON for resource ID {}: {}", resourceIdOld, e.getMessage());
                                    log.warn("New resource ID {} will be saved, but old resources may still exist", newResourceId.uuid());
                                }
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .thenReturn(newResourceId);
                })
                .doOnError(e ->
                        log.error("Error updating file with old resource ID {}: {}", resourceIdOld, e.getMessage())
                );
    }



    private Mono<FileStorageService.ResourceUUID> populationSaveAndUpdate(FilePart filePart) {

        UUID resourceId = UUID.randomUUID();
        String nameFile = FileStorageService.buildNameFileUniqueNormalice(resourceId);
        Path pathFile;
        try {
            pathFile = FileStorageService.amountFileToPath(Path.of(uploadDir), nameFile);
        }catch (IllegalArgumentException e) {
            log.error("Error creating file path for {}: {}", nameFile, e.getMessage());
            return Mono.error(e);
        }

        String contentType = filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        FileStorageService.ResourceDetail resourceDetail =
                new FileStorageService.ResourceDetail(
                        resourceId,
                        filePart.filename(),
                        contentType,
                        filePart.headers().getContentLength()
                );

        Path jsonFile = Path.of(uploadDir, resourceId + ".json");

        return filePart.transferTo(pathFile) // guarda el archivo
                .then(Mono.fromCallable(() -> {
                    // escribe el JSON de manera bloqueante
                    objectMapper.writeValue(jsonFile.toFile(), resourceDetail);
                    return new FileStorageService.ResourceUUID(resourceId);
                }).subscribeOn(Schedulers.boundedElastic()))
                .doOnSuccess(r -> log.info("File {} saved successfully with resource ID: {}", filePart.filename(), resourceId))
                .doOnError(e -> log.error("Error saving file {}: {}", filePart.filename(), e.getMessage()));
    }

}
