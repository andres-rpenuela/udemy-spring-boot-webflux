package com.codearp.springboot.reactor.services.files.impl;

import com.codearp.springboot.reactor.services.files.FileStorageReadService;
import com.codearp.springboot.reactor.services.files.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FileStorageReadServiceImplTest {

    @TempDir
    Path tempDir;


    private FileStorageReadService fileStorageReadService;

    @BeforeEach
    void setUp() {
        fileStorageReadService = new FileStorageReadServiceImpl( new ObjectMapper() );
        ((FileStorageReadServiceImpl) fileStorageReadService)
                .setUploadDirProperty( tempDir.toString() );
    }


    @Test
    void readContentFileAsDataBuffer() throws Exception {
        UUID resourceId = UUID.randomUUID();

        // Create a temp file that mimics a real uploaded file
        Path file = tempDir.resolve(resourceId.toString());
        String content = "hola mundo";
        java.nio.file.Files.write(file, content.getBytes(StandardCharsets.UTF_8));

        // Now the service should return a path
        Path path = FileStorageService.getPathFileByResourceId(tempDir.toString(), resourceId.toString())
                .block();
        assertNotNull(path);
        assertTrue(path.startsWith(tempDir));
        
        // Call your service
        Flux<DataBuffer> result = fileStorageReadService.readContentFileAsDataBuffer(resourceId.toString());

        // Verify
        StepVerifier.create(result)
                .assertNext(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    String readContent = new String(bytes, StandardCharsets.UTF_8);
                    assertEquals("hola mundo", readContent);
                    DataBufferUtils.release(dataBuffer);
                })
                .verifyComplete();
    }




    @Test
    void readContentFileAsByteArrayOutputStream() throws IOException {
        UUID  resourceId = UUID.randomUUID();

        // Create a temp file that mimics a real uploaded file
        Path file = tempDir.resolve(resourceId.toString());
        String content = "hola mundo";
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));

        Flux<ByteArrayOutputStream> resul =  fileStorageReadService.readContentFileAsByteArrayOutputStream( resourceId.toString() );

        StepVerifier.create( resul )
                .assertNext( byteArrayOutputStream -> {
                    String readContent = byteArrayOutputStream.toString( StandardCharsets.UTF_8 );
                    assertEquals("hola mundo", readContent);
                })
                .verifyComplete();
    }

    @Test
    void getMetadataFileByResourceId() {
        // Todo implement test
    }

    @Test
    void getResourceContentByResourceId() {
        // Todo implement test
    }

    @Test
    void existsFileByResourceId() {
        // Todo implement test
    }
}