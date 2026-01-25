package com.codearp.springboot.reactor.facade;

import com.codearp.springboot.reactor.services.files.FileStorageDeleteService;
import com.codearp.springboot.reactor.services.files.FileStorageReadService;
import com.codearp.springboot.reactor.services.files.FileStorageService;
import com.codearp.springboot.reactor.services.files.FileStorageWriterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageFacadeImpl implements FileStorageFacade {

    private final FileStorageWriterService fileStorageWriterService;
    private final FileStorageReadService fileStorageReadService;
    private final FileStorageDeleteService fileStorageDeleteService;


    @Override
    public Mono<FileStorageService.ResourceUUID> saveFile(FilePart filePart) {
        return fileStorageWriterService.saveFile(filePart);
    }

    @Override
    public Mono<FileStorageService.ResourceUUID> updateFile(String resourceIdOld, FilePart filePart) {
        return fileStorageWriterService.updateFile(resourceIdOld, filePart);
    }

    @Override
    public Flux<DataBuffer> getContentFieByResourceId(String resourceId) {
        return fileStorageReadService.readContentFileAsDataBuffer(resourceId);
    }

    @Override
    public Mono<FileStorageService.ResourceDetail> getMetadataFileByResourceId(String resourceId) {
        return fileStorageReadService.getMetadataFileByResourceId(resourceId);
    }

    @Override
    public Mono<Void> deleteFileByResourceId(String resourceId) {
        return fileStorageDeleteService.deleteFileByResourceId(resourceId);
    }
}
