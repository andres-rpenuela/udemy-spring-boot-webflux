package com.codearp.springboot.reactor.controllers;

import com.codearp.springboot.reactor.facade.FileStorageFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class FilesController {

    private final FileStorageFacade fileStorageFacade;

    // Sirve archivos con seguridad básica y en modo reactivo (IO en boundedElastic)
    @GetMapping("/files/{resourceId:.+}")
    public Mono<ResponseEntity<Flux<DataBuffer>>> getFile(
            @PathVariable String resourceId) {

        return fileStorageFacade.getMetadataFileByResourceId(resourceId)
                .map(resourceDetail ->
                        Optional.ofNullable(resourceDetail.contentType())
                                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                )
                .map(MediaType::parseMediaType)
                .map(mediaType ->
                        ResponseEntity.ok()
                                .contentType(mediaType)
                                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resourceId + "\"")
                                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, must-revalidate")
                                .body(fileStorageFacade.getContentFieByResourceId(resourceId))
                );
    }




    // download
    @GetMapping("/files/download/{resourceId:.+}")
    public Mono<ResponseEntity<Flux<DataBuffer>>> downloadFile(@PathVariable("resourceId") String resourceId) {

        Flux<DataBuffer> dataBufferFlux =
                fileStorageFacade.getContentFieByResourceId(resourceId);

        return fileStorageFacade.getMetadataFileByResourceId(resourceId)
                .switchIfEmpty(Mono.error(new RuntimeException("File not found")))
                .flatMap(metadata ->
                        Mono.just(
                                ResponseEntity.ok()
                                        .contentType(MediaType.parseMediaType(
                                                Optional.ofNullable(metadata.contentType())
                                                        .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                        ))
                                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.name() + "\"")
                                        .body(dataBufferFlux)
                        )
                )
                .switchIfEmpty(Mono.error(new RuntimeException("File not found")))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

}
