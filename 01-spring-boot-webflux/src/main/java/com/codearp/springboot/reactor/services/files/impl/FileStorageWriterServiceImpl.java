package com.codearp.springboot.reactor.services.files.impl;

import com.codearp.springboot.reactor.services.files.FileStorageService;
import com.codearp.springboot.reactor.services.files.FileStorageWriterService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageWriterServiceImpl implements FileStorageWriterService {

    @Getter @Setter
    @Value("${config.uploads.path:uploads-app}")
    private String uploadsPath;

    // Instancia local de ObjectMapper para parseo JSON, serialización y deserialización
    // Si no hay bean
    private final ObjectMapper objectMapper;


    /**
     * Guarda un archivo subido en el directorio de uploads de manera reactiva.
     * <p>
     * Este método realiza varias validaciones:
     * <ul>
     *     <li>Verifica que el directorio de uploads exista.</li>
     *     <li>Verifica que el nombre de archivo generado sea válido.</li>
     *     <li>Valida que el objeto {@link FilePart} no sea nulo y tenga un nombre válido.</li>
     *     <li>Previene ataques de <em>path traversal</em> asegurando que el archivo se guarde dentro del directorio permitido.</li>
     * </ul>
     * <p>
     * El tipo de contenido (content type) del archivo se determina de forma no bloqueante utilizando
     * {@link java.nio.file.Files#probeContentType(Path)} ejecutado en un hilo de I/O.
     * Si no se puede determinar, se asigna {@link org.springframework.http.MediaType#APPLICATION_OCTET_STREAM}.
     * <p>
     * El flujo reactivo hace lo siguiente:
     * <ol>
     *     <li>Obtiene el content type de manera no bloqueante.</li>
     *     <li>Guarda el archivo en disco mediante {@link FilePart#transferTo(File)}.</li>
     *     <li>Lee el contenido del archivo de forma reactiva.</li>
     *     <li>Devuelve un objeto {@link FileStorageService.ResourceContent} con nombre, content type, media type y contenido opcional.</li>
     * </ol>
     *
     * @param filePart el archivo a guardar, representado como {@link FilePart}.
     * @return un {@link Mono} que emite un {@link FileStorageService.ResourceContent} con los datos del archivo guardado.
     * @throws IllegalArgumentException si el directorio de uploads no existe, el nombre de archivo es inválido,
     *                                  el filePart es nulo o inválido, o si el archivo apunta fuera del directorio permitido.
     * @throws RuntimeException envuelto en un {@link Mono#error} si ocurre un error durante la transferencia o lectura del archivo.
     */
    @Override
    public Mono<FileStorageService.ResourceUUID> saveFile( FilePart filePart ) {
        final UUID uuid = UUID.randomUUID();
        Path uploadsDirPath = Path.of(uploadsPath).toAbsolutePath().normalize();

        // obtener nombre único normalizado => uuid + "-" + filename normalizado
        String nameResourceContent = uuid.toString();


        if (!FileStorageService.existPathDir(uploadsDirPath)) {
            return Mono.error(new IllegalArgumentException("Uploads directory does not exist"));
        }


        if (filePart == null || filePart.filename().trim().isBlank()) {
            return Mono.error(new IllegalArgumentException("Invalid file part"));
        }

        // Construir path completo del archivo a guardar path + nombre único normalizado
        Path filePath = FileStorageService.amountFileToPath(uploadsDirPath, nameResourceContent );

        // Evitar path transversal
        if (!filePath.startsWith(uploadsDirPath)) {
            return Mono.error(new IllegalArgumentException("Invalid file path"));
        }

        // Nota: las operaciones de I/O bloqueantes (probeContentType, escritura de ficheros)
        // se ejecutan en hilos de I/O mediante `subscribeOn(Schedulers.boundedElastic())`
        // en los lugares correspondientes; un buffer-pool especializado NO está
        // implementado por simplicidad (puede añadirse en una mejora futura).
        return FileStorageService.getContentType(filePath)
                // probeContentType puede ser bloqueante; asegurar ejecución en boundedElastic
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(pair -> {
                    String finalContentType = pair.getFirst();
                    MediaType finalMediaType = pair.getSecond();

                    // Construir ResourceDetail ahora (immutable por referencia)
                    FileStorageService.ResourceDetail rd = new FileStorageService.ResourceDetail(
                            uuid,
                            filePart.filename(),
                            finalContentType,
                            finalMediaType != null ? finalMediaType.toString() : null
                    );

                    // Guardar archivo de forma segura: transferir -> persistir metadatos (saveResourceDetail)
                    // Usar transferTo(Path) evita crear File y posibles operaciones bloqueantes innecesarias
                    return Mono.defer(() -> filePart.transferTo(filePath))
                            // saveResourceDetail devuelve Mono<Void> y ya se ejecuta en boundedElastic internamente
                            .then(saveResourceDetail(rd));
                })
                .thenReturn(new FileStorageService.ResourceUUID(uuid))
                .onErrorResume(e -> {
                    log.error("Error saving file '{}' (uuid={}): {}", filePath, uuid, e.toString(), e);
                    // Ejecutar la limpieza en boundedElastic para no bloquear hilos reactor
                    return Mono.fromRunnable(() -> {
                                try {
                                    Files.deleteIfExists(filePath);
                                    Files.deleteIfExists(uploadsDirPath.resolve(uuid + ".json"));
                                } catch (Exception ex) {
                                    log.warn("Cleanup failed for file '{}' or json: {}", filePath, ex.toString(), ex);
                                }
                            }).subscribeOn(Schedulers.boundedElastic())
                            .then(Mono.error(new RuntimeException("Error saving file", e)));
                });
    }

    /**
     * Actualiza un archivo existente eliminando el recurso antiguo y guardando el nuevo.
     *
     * @param resourceIdOld el ID del recurso antiguo a eliminar
     * @param filePart la parte del nuevo archivo a guardar
     * @return un {@link Mono} que emite un {@code ResourceUUID} con el ID del nuevo recurso guardado
     *        o un error si la operación falla
     */
    @Override
    public Mono<FileStorageService.ResourceUUID> updateFile(String resourceIdOld, FilePart filePart) {
        if( resourceIdOld == null || resourceIdOld.trim().isEmpty() || !FileStorageService.isValidUUID(resourceIdOld) ) {
            log.info("first image upload, no previous resource to delete.");
            return this.saveFile( filePart );
        }

        return saveFile( filePart ).flatMap(newResourceUUID -> {
            // Eliminar el recurso antiguo
            UUID oldUuid = UUID.fromString(resourceIdOld);
            Path uploadsDirPath = Path.of(uploadsPath).toAbsolutePath().normalize();
            Path oldFilePath = FileStorageService.amountFileToPath(uploadsDirPath, oldUuid.toString() );

            return Mono.fromRunnable(() -> {
                        try {
                            Files.deleteIfExists(oldFilePath);
                            Files.deleteIfExists(uploadsDirPath.resolve(oldUuid + ".json"));
                        } catch (Exception e) {
                            log.warn("Failed to delete old resource file or json for resourceIdOld={}", resourceIdOld, e);
                            log.warn("Deleted manually the old resource may be required {}", resourceIdOld);
                        }
                    }).subscribeOn(Schedulers.boundedElastic())
                    .thenReturn(newResourceUUID);
        })
        // terminar con logging en caso de error, pero propagar el error hacia el suscriptor, si quisieramos detener el error,
        // usar .onErrorResume y devolver un valor por defecto, aunque sea otro Mono.error
        .doOnError(e -> log.error("Error updating file for resourceIdOld='{}': {}", resourceIdOld, e.toString(), e));

    }

    /**
     * Guarda los metadatos del recurso en un archivo JSON.
     *
     * @param resourceDetail los detalles del recurso a guardar
     * @return un {@link Mono} que completa cuando se guarda el JSON, o emite error si falla
     * @throws IllegalStateException si ocurre un error durante la escritura del archivo JSON
     */
    protected  Mono<Void> saveResourceDetail(FileStorageService.ResourceDetail resourceDetail){
        String nameResourceDetail = String.format("%s.json", resourceDetail.id() );

        Path uploadsDidPath = Path.of(uploadsPath).toAbsolutePath().normalize();
        Path jsonFilePath = uploadsDidPath.resolve(nameResourceDetail);

        return Mono.fromCallable(() -> {
            try {
                // Construir un ObjectNode para controlar cómo se serializa mediaType (como texto)
                ObjectNode root = objectMapper.createObjectNode();
                if (resourceDetail.id() != null) root.put("id", resourceDetail.id().toString());
                if (resourceDetail.name() != null) root.put("name", resourceDetail.name());
                if (resourceDetail.contentType() != null) root.put("contentType", resourceDetail.contentType());
                if (resourceDetail.mediaType() != null) root.put("mediaType", resourceDetail.mediaType().toString());

                String json = objectMapper.writeValueAsString(root);
                Files.writeString(jsonFilePath, json);
                return  Void.TYPE ;
             } catch (Exception e) {
                 // En caso de fallo, eliminar JSON si existen y registrar el error
                 try { Files.deleteIfExists(jsonFilePath); } catch (Exception ex) {
                     log.warn("Failed to delete jsonFilePath on rollback: {}", jsonFilePath, ex);
                 }
                 throw new IllegalStateException("Error saving JSON, rolled back", e);
             }
         }).subscribeOn(Schedulers.boundedElastic()).then();
     }
 }
