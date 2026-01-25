package com.codearp.springboot.reactor.services.files.impl;

import com.codearp.springboot.reactor.services.files.FileStorageReadService;
import com.codearp.springboot.reactor.services.files.FileStorageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageReadServiceImpl implements FileStorageReadService {

    @Getter @Setter
    @Value("${config.uploads.path:uploads-app}")
    private String uploadDirProperty;

    // Instancia local de ObjectMapper para parseo JSON
    private final ObjectMapper objectMapper;


    /**
     * Lee el contenido del recurso indicado por resourceId como un Flux de DataBuffer.
     * Método reactivo y no bloqueante; usa DataBufferUtils.read(...) para emitir DataBuffer
     * sin copiar a estructuras en memoria. Recomiendo usar BodyInserters.fromDataBuffers(flux)
     * en controladores para servir el contenido directamente.
     *
     * @param resourceId identificador del recurso (UUID o nombre único)
     * @return Flux<DataBuffer> flujo reactivo con los buffers de datos leídos del fichero
     */
    @Override
    public Flux<DataBuffer> readContentFileAsDataBuffer(String resourceId) {

        return FileStorageService.getPathFileByResourceId( uploadDirProperty, resourceId  )
                .flatMapMany( pathFile -> DataBufferUtils.read(
                        pathFile,
                        new DefaultDataBufferFactory(),
                        FileStorageService.BUFFER_SIZE_FILE_READ
                ))
                // Sólo registra el error; lo propaga hacia el consumidor para que decida.
                .doOnError( error -> log.error("Error reading file with resourceId {}: {}", resourceId, error.getMessage()) );
    }

    /**
     * Lee el contenido del recurso indicado por resourceId y lo expone como un Flux
     * de ByteArrayOutputStream.
     *
     * Este método es reactivo y no bloqueante:
     * - No realiza operaciones de E/S en el hilo del event-loop principal.
     * - Las operaciones de lectura de fichero se hacen usando DataBufferUtils.read(...) que
     *   devuelve un Flux<DataBuffer> y está diseñado para funcionamiento reactivo.
     * - Se aplica subscribeOn(Schedulers.boundedElastic()) para mover trabajo potencialmente
     *   bloqueante (copias a memoria) a un scheduler apropiado.
     *
     * Flujo (línea a línea):
     * 1. getPathFileByResourceId(...) -> Mono<Path>
     *    - Valida el resourceId y resuelve la ruta del fichero.
     * 2. flatMapMany(pathFile -> DataBufferUtils.read(...))
     *    - Abre una lectura reactiva del fichero devolviendo Flux<DataBuffer>.
     * 3. map(dataBuffer -> { ... })
     *    - Para cada DataBuffer: crea un ByteArrayOutputStream, copia los bytes
     *      del DataBuffer al stream y libera el DataBuffer con DataBufferUtils.release(...).
     *      NOTA: esta copia convierte bloques de datos en instancias de ByteArrayOutputStream
     *      — puede aumentar uso de memoria si el fichero es grande; considerar otra estrategia
     *      (por ejemplo devolver Flux<DataBuffer> y dejar que el framework escriba los buffers
     *      directamente en la respuesta).
     * 4. subscribeOn(Schedulers.boundedElastic())
     *    - Indica que la suscripción y el procesamiento se hagan en un scheduler apto para
     *      operaciones potencialmente bloqueantes.
     * 5. doOnError(...)
     *    - Loggea errores sin consumirlos.
     *
     * Consideraciones:
     * - Esta implementación evita bloqueos del event-loop, pero la conversión a
     *   ByteArrayOutputStream implica copiar datos a memoria; para ficheros grandes
     *   preferir devolver Flux<DataBuffer> y dejar que el motor reactive escriba los buffers.
     *
     * @param resourceId identificador del recurso (UUID o nombre único)
     * @return Flux<ByteArrayOutputStream> flujo con trozos del fichero como ByteArrayOutputStream
     */
    public Flux<ByteArrayOutputStream> readContentFileAsByteArrayOutputStream(String resourceId) {

        return FileStorageService.getPathFileByResourceId( uploadDirProperty, resourceId  )
                .flatMapMany( pathFile -> DataBufferUtils.read(
                        pathFile,
                        new DefaultDataBufferFactory(),
                        FileStorageService.BUFFER_SIZE_FILE_READ
                ))
                .map( dataBuffer -> {
                    // Convertir DataBuffer a ByteArrayOutputStream
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    outputStream.writeBytes(bytes);
                    // Liberar el DataBuffer para evitar fugas de memoria
                    DataBufferUtils.release(dataBuffer);
                    return outputStream;
                })
                // El procesamiento de conversión puede implicar trabajo de copia en memoria,
                // por eso lo hacemos en boundedElastic para evitar bloquear el event-loop.
                .subscribeOn(Schedulers.boundedElastic() )
                .doOnError( error -> log.error("Error reading file with resourceId {}: {}", resourceId, error.getMessage()) );
    }

    /**
     * Obtiene los metadatos del archivo asociado al resourceId.
     * Busca un archivo JSON con el mismo nombre que el resourceId en la
     * misma carpeta del archivo real.
     *
     * @param resourceId identificador del recurso (UUID o nombre único)
     * @return Mono<FileStorageService.ResourceDetail> con los metadatos si existen, o Mono.empty() si no
     */
    @Override
    public Mono<FileStorageService.ResourceDetail> getMetadataFileByResourceId(String resourceId) {
        return FileStorageService.getPathFileByResourceId(uploadDirProperty, resourceId)
                .flatMap(pathFile -> Mono.fromCallable(() -> {
                    // Asumimos que los metadatos se guardan en <uuid>.json al mismo nivel del archivo
                    Path jsonPath = pathFile.getParent().resolve(resourceId + ".json");
                    if (!jsonPath.toFile().exists()) {
                        return null;
                    }
                    // Leer el contenido del JSON como String
                    return java.nio.file.Files.readString(jsonPath);
                }).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(content -> {
                    if (content == null) return Mono.empty();
                    try {
                        // Parsear en árbol para evitar el error de deserializar MediaType
                        JsonNode node = objectMapper.readTree(content);

                        UUID id = null;
                        if (node.has("id") && !node.get("id").isNull()) {
                            try { id = UUID.fromString(node.get("id").asText()); } catch (Exception ex) { id = null; }
                        }

                        String name = node.has("name") && !node.get("name").isNull() ? node.get("name").asText() : null;
                        String contentType = node.has("contentType") && !node.get("contentType").isNull() ? node.get("contentType").asText() : null;

                        MediaType mediaType = null;
                        if (node.has("mediaType") && !node.get("mediaType").isNull()) {
                            JsonNode mtNode = node.get("mediaType");
                            if (mtNode.isTextual()) {
                                try { mediaType = MediaType.parseMediaType(mtNode.asText()); } catch (Exception ex) { mediaType = null; }
                            } else if (mtNode.has("type") && mtNode.has("subtype")) {
                                try {
                                    String type = mtNode.get("type").asText();
                                    String subtype = mtNode.get("subtype").asText();
                                    mediaType = MediaType.parseMediaType(type + "/" + subtype);
                                } catch (Exception ex) { mediaType = null; }
                            }
                        }

                        if (mediaType == null && contentType != null) {
                            try { mediaType = MediaType.parseMediaType(contentType); } catch (Exception ex) { mediaType = MediaType.APPLICATION_OCTET_STREAM; }
                        }

                        UUID finalId = id != null ? id : (resourceId != null ? UUID.fromString(resourceId) : null);
                        FileStorageService.ResourceDetail rd = new FileStorageService.ResourceDetail(finalId, name, contentType, mediaType != null ? mediaType.toString() : null);
                        return Mono.justOrEmpty(rd);
                    } catch (JsonProcessingException jpe) {
                        // Si el JSON tiene formato inválido, no queremos romper el flujo global: devolvemos Mono.empty() y registramos un aviso
                        String snippet = content.length() > 512 ? content.substring(0, 512) + "..." : content;
                        log.warn("Failed to parse metadata JSON for resourceId {}: {}. Content snippet: {}", resourceId, jpe.toString(), snippet);
                        return Mono.empty();
                    } catch (Exception e) {
                        log.error("Failed to parse metadata JSON for resourceId {}: {}", resourceId, e.toString());
                        return Mono.error(new RuntimeException("Failed to parse metadata JSON", e));
                    }
                });
    }

    /**
     * Lee el contenido (metadato/JSON) asociado a resourceId y lo devuelve como Mono<ResourceContent>.
     * Reactivo: resolvemos el path de forma reactiva y movemos la lectura/parseo (bloqueante)
     * a Schedulers.boundedElastic() mediante Mono.fromCallable(...).
     *
     * Devuelve Mono.empty() si no existe el fichero o el contenido es null. Errores de lectura/parseo se propagan.
     *
     * @param resourceId identificador del recurso
     * @return Mono<FileStorageService.ResourceContent> con el contenido o Mono.empty()
     */
    @Override
    public Mono<FileStorageService.ResourceContent> getResourceContentByResourceId(String resourceId) {
        return FileStorageService.getPathFileByResourceId(uploadDirProperty, resourceId)
                // Comprobación de existencia + lectura/parseo en boundedElastic para evitar bloquear el event-loop
                .flatMap(pathFile -> Mono.fromCallable(() -> {
                    // comprobar existencia (bloqueante) y leer/parsear (bloqueante)
                    if (!Files.exists(pathFile)) return null;
                    try {
                        return objectMapper.readValue(pathFile.toFile(), FileStorageService.ResourceContent.class);
                    } catch (JsonProcessingException jpe) {
                        // Si el JSON está corrupto o no coincide con la clase, intentar parsear manualmente
                        try {
                            String content = java.nio.file.Files.readString(pathFile);
                            JsonNode node = objectMapper.readTree(content);

                            UUID id = null;
                            if (node.has("id") && !node.get("id").isNull()) {
                                try { id = UUID.fromString(node.get("id").asText()); } catch (Exception ex) { id = null; }
                            }
                            String name = node.has("name") && !node.get("name").isNull() ? node.get("name").asText() : null;
                            String contentType = node.has("contentType") && !node.get("contentType").isNull() ? node.get("contentType").asText() : null;

                            MediaType mediaType = null;
                            if (node.has("mediaType") && !node.get("mediaType").isNull()) {
                                JsonNode mtNode = node.get("mediaType");
                                if (mtNode.isTextual()) {
                                    try { mediaType = MediaType.parseMediaType(mtNode.asText()); } catch (Exception ex) { mediaType = null; }
                                } else if (mtNode.has("type") && mtNode.has("subtype")) {
                                    try {
                                        String type = mtNode.get("type").asText();
                                        String subtype = mtNode.get("subtype").asText();
                                        mediaType = MediaType.parseMediaType(type + "/" + subtype);
                                    } catch (Exception ex) { mediaType = null; }
                                }
                            }

                            if (mediaType == null && contentType != null) {
                                try { mediaType = MediaType.parseMediaType(contentType); } catch (Exception ex) { mediaType = MediaType.APPLICATION_OCTET_STREAM; }
                            }

                            byte[] contentBytes = null;
                            if (node.has("content") && !node.get("content").isNull()) {
                                try {
                                    // si content está como base64
                                    String b64 = node.get("content").asText();
                                    if (b64 != null && !b64.isBlank()) contentBytes = java.util.Base64.getDecoder().decode(b64);
                                } catch (Exception ex) { contentBytes = null; }
                            }

                            UUID finalId = id != null ? id : (resourceId != null ? UUID.fromString(resourceId) : null);
                            String mediaTypeStr = mediaType != null ? mediaType.toString() : null;
                            FileStorageService.ResourceContent rc = new FileStorageService.ResourceContent(finalId, name, contentType, mediaTypeStr, java.util.Optional.ofNullable(contentBytes));
                             return rc;
                        } catch (Exception ex) {
                            log.warn("Failed to parse resource content JSON for resourceId {}: {}", resourceId, ex.toString());
                            return null;
                        }
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
                // Normalizar la salida: si el resultado es null devolvemos Mono.empty()
                .flatMap(Mono::justOrEmpty)
                .doOnError(error -> log.error("Error getting resource content for resourceId {}: {}", resourceId, error.getMessage()));
    }

    @Override
    public Mono<Boolean> existsFileByResourceId(String resourceId) {
        return FileStorageService.getPathFileByResourceId(uploadDirProperty, resourceId)
                .flatMap(pathFile -> Mono.fromCallable(() -> Files.exists(pathFile))
                        .subscribeOn(Schedulers.boundedElastic())
                )
                .defaultIfEmpty(false)
                .onErrorResume(error -> {
                    log.error("Error checking existence of file with resourceId {}: {}", resourceId, error.getMessage());
                    return Mono.just(false);
                });
    }


}
