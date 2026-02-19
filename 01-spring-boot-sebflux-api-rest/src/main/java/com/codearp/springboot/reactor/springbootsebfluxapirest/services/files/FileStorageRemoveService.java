package com.codearp.springboot.reactor.springbootsebfluxapirest.services.files;

import reactor.core.publisher.Mono;

public interface FileStorageRemoveService {
    Mono<Void> deleteFile(String resourceId);
}
