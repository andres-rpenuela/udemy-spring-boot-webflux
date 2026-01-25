package com.codearp.springboot.reactor.facade;

import com.codearp.springboot.reactor.services.files.FileStorageService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileStorageFacade {

    Mono<FileStorageService.ResourceUUID> saveFile(FilePart filePart );

    Mono<FileStorageService.ResourceUUID> updateFile(String resourceIdOld, FilePart filePart);

    Flux<DataBuffer> getContentFieByResourceId(String resourceId);
    Mono<FileStorageService.ResourceDetail> getMetadataFileByResourceId(String resourceId);

    Mono<Void> deleteFileByResourceId(String resourceId);
}
