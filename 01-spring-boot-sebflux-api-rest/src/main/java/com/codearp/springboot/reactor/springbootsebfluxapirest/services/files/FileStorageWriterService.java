package com.codearp.springboot.reactor.springbootsebfluxapirest.services.files;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface FileStorageWriterService {

    Mono<FileStorageService.ResourceUUID> saveFile( FilePart filePart );

    Mono<FileStorageService.ResourceUUID> updateFile(String resourceIdOld, FilePart filePart);
}
