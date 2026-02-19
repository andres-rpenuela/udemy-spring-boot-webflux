package com.codearp.springboot.reactor.springbootsebfluxapirest.facades.files;

import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageReadService;
import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageRemoveService;
import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageService;
import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageWriterService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FileStorageFacadeImpl implements FileStorageFacade {

    private final FileStorageWriterService fileStorageWriterService;
    private final FileStorageReadService fileStorageReadService;
    private final FileStorageRemoveService fileStorageRemoveService;

    @Override
    public Mono<FileStorageService.ResourceUUID> saveFile(FilePart filePart) {
        return fileStorageWriterService.saveFile(filePart);
    }

    @Override
    public Mono<FileStorageService.ResourceUUID> updateFile(String resourceIdOld, FilePart filePart) {
        return fileStorageWriterService.updateFile(resourceIdOld, filePart);
    }

    @Override
    public Flux<DataBuffer> downloadFile(String resourceId) {
        return fileStorageReadService.readContentFileAsDataBuffer(resourceId);
    }

    @Override
    public Mono<Pair<String, Flux<DataBuffer>>> downloadFileFull(String resourceId) {

        return fileStorageReadService.getResourceContentByResourceId(resourceId)
                .map(resource -> {

                    Flux<DataBuffer> data = DataBufferUtils.read(
                            resource.filePath(),
                            new DefaultDataBufferFactory(),
                            4096
                    );

                    return Pair.of(resource.name(), data);
                });
    }

    @Override
    public Mono<Void> deleteFile(String resourceId) {
        return fileStorageRemoveService.deleteFile(resourceId);
    }
}
