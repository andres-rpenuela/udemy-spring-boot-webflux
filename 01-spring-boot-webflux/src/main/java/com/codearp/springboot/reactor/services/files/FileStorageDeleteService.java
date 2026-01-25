package com.codearp.springboot.reactor.services.files;

import reactor.core.publisher.Mono;

public interface FileStorageDeleteService {

    Mono<Void> deleteFileByResourceId(String resourceId);
}
