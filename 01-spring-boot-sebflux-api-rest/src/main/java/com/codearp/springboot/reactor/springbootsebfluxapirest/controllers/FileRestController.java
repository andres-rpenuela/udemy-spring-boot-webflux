package com.codearp.springboot.reactor.springbootsebfluxapirest.controllers;

import com.codearp.springboot.reactor.springbootsebfluxapirest.facades.files.FileStorageFacade;
import com.codearp.springboot.reactor.springbootsebfluxapirest.services.files.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileRestController {

    private final FileStorageFacade fileStorageFacade;

    @PostMapping("/add")
    public Mono<ResponseEntity<FileStorageService.ResourceUUID>> addFile(@RequestPart("file") FilePart filePart) {
        return fileStorageFacade.saveFile(filePart)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    @PutMapping("/update/{resourceId}")
    public Mono<ResponseEntity<FileStorageService.ResourceUUID>> updateFile(@RequestPart("file") FilePart filePart, @PathVariable("resourceId") String resourceId) {
        return fileStorageFacade.updateFile(resourceId, filePart)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/download/{id}")
    public Mono<ResponseEntity<Flux<DataBuffer>>> download(@PathVariable String id) {
        return Mono.just(
                ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + id + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(fileStorageFacade.downloadFile(id))
        );
    }

    @GetMapping("/download-full/{id}")
    public Mono<ResponseEntity<Flux<DataBuffer>>> downloadWithName(
            @PathVariable String id) {

        return fileStorageFacade.downloadFileFull(id)
                .map(file -> ResponseEntity.ok()
                        .header("Content-Disposition",
                                "attachment; filename=\"" + file.getFirst() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(file.getSecond())
                )
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<Void>> deleteFile(@PathVariable String id) {
        return fileStorageFacade.deleteFile(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

}
