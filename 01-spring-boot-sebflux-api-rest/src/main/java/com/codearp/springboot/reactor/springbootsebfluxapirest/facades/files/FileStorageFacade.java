package com.codearp.springboot.reactor.springbootsebfluxapirest.facades.files;

import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileStorageFacade {

    Mono<FileStorageService.ResourceUUID> saveFile(FilePart filePart);
    Mono<FileStorageService.ResourceUUID> updateFile(String resourceIdOld, FilePart filePart);

    Flux<DataBuffer> downloadFile(String resourceId);
    Mono<Pair<String, Flux<DataBuffer>>> downloadFileFull(String resourceId);

    Mono<Void> deleteFile(String resourceId);
}
