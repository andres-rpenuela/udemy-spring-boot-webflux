package com.codearp.springboot.reactor.services.files.impl;

import com.codearp.springboot.reactor.services.files.FileStorageService;
import com.codearp.springboot.reactor.services.files.FileStorageWriterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileStorageWriterServiceImplTest {

    @TempDir
    Path tempDir;

    @Mock
    FilePart filePart;

    private FileStorageWriterService fileStorageWriterService;

    @BeforeEach
    void setUp() {
        fileStorageWriterService =
                new FileStorageWriterServiceImpl(new ObjectMapper());
        ((FileStorageWriterServiceImpl) fileStorageWriterService)
                .setUploadsPath(tempDir.toString());
    }

    @Test
    void givenFilePart_whenSaveResource_thenCreatedSuccelly() {
        // given
        byte[] content = "hola mundo".getBytes();

//      Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(
//              new ByteArrayResource(content),
//              new DefaultDataBufferFactory(),
//              content.length
//      );
//      Mockito.when(filePart.content()).thenReturn(dataBufferFlux); // Not needed for this test
        Mockito.when(filePart.filename()).thenReturn("test.txt");

        Mockito.when(filePart.transferTo(Mockito.any(Path.class)))
                .thenReturn(Mono.empty()); // Simulate successful file transfer

        // when
        Mono<FileStorageService.ResourceUUID> result =
                fileStorageWriterService.saveFile(filePart);

        // then
        StepVerifier.create(result)
                .assertNext(uuid -> {
                    assertNotNull(uuid);
                    assertNotNull(uuid.uuid());
                })
                .verifyComplete();
    }

    @Test
    void givenFilePart_whenUpdateResource_thenCreatedSuccelly() throws IOException {
        // given
        byte[] content = "hola mundo".getBytes();
        UUID resourceIdOld = UUID.randomUUID();

        //Mockito.when(filePart.transferTo(Mockito.any(Path.class))).thenReturn(Mono.empty());

        // Save old file to update, si quermos aislar updateFile
        FileStorageService.ResourceUUID mockNewUuid = new FileStorageService.ResourceUUID( UUID.randomUUID() );
        FileStorageWriterService  spyService = Mockito.spy( fileStorageWriterService );

        Mockito.doReturn( Mono.just( mockNewUuid ) )
                .when( spyService ).saveFile( filePart );

        // Preparar e path del archivo viejo
        Path uploadsDirPath = tempDir.toAbsolutePath().normalize();
        Path oldFilePath = FileStorageService.amountFileToPath(uploadsDirPath, resourceIdOld.toString() );
        Files.createFile(oldFilePath); // archivo antiguo fake
        Files.createFile(uploadsDirPath.resolve(resourceIdOld + ".json")); // json fake

        // when
        Mono<FileStorageService.ResourceUUID> result =
                spyService.updateFile( resourceIdOld.toString(), filePart );

        // then
        StepVerifier.create(result)
                .assertNext(uuid -> {
                    assertNotNull(uuid);
                    assertEquals(mockNewUuid.uuid(), uuid.uuid());
                    assertFalse(Files.exists(oldFilePath)); // el archivo antiguo fue borrado
                    assertFalse(Files.exists(uploadsDirPath.resolve(resourceIdOld + ".json")));
                })
                .verifyComplete();
    }
}
