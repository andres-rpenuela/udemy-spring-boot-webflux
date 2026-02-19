package com.codearp.springboot.reactor.springbootsebfluxapirest.services.files;

import reactor.core.publisher.Mono;

public interface FileStorageDeleteService {

    Mono<Void> deleteFileByResourceId(String resourceId);
}
