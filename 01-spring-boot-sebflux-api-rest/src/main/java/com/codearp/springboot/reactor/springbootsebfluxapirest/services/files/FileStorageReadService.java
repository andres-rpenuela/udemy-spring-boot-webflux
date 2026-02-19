package com.codearp.springboot.reactor.springbootsebfluxapirest.services.files;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;

public interface FileStorageReadService {

    /** Obtener contendio **/
    Flux<DataBuffer> readContentFileAsDataBuffer(String resourceId);

    Flux<ByteArrayOutputStream> readContentFileAsByteArrayOutputStream(String resourceId);

    /** Obtener metadatos **/
    Mono<FileStorageService.ResourceDetail> getMetadataFileByResourceId(String resourceId);

    /** Obtener resource content **/
    Mono<FileStorageService.ResourceContent> getResourceContentByResourceId(String resourceId);

    Mono<Boolean> existsFileByResourceId(String resourceId);
}
