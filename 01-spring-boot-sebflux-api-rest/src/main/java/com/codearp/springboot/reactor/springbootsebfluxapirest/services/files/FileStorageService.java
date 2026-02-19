package com.codearp.springboot.reactor.springbootsebfluxapirest.services.files;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio reactivo para la gestión y lectura de archivos almacenados.
 * <p>
 * Esta interfaz define distintas formas de acceso al contenido de archivos,
 * adaptadas a diferentes necesidades de consumo:
 * <ul>
 *   <li>Lectura completa en memoria.</li>
 *   <li>Lectura en streaming mediante {@link DataBuffer}.</li>
 *   <li>Acceso a metadatos junto con el contenido.</li>
 * </ul>
 *
 * <p>
 * Todas las operaciones están diseñadas para ser utilizadas en entornos reactivos
 * (por ejemplo, Spring WebFlux) y deben evitar operaciones bloqueantes en el hilo
 * reactivo principal.
 * </p>
 *
 * <p><strong>Consideraciones generales:</strong></p>
 * <ul>
 *   <li>El nombre del archivo debe validarse previamente para evitar ataques de tipo
 *       <em>path traversal</em>.</li>
 *   <li>Los métodos que cargan el contenido completo en memoria deben utilizarse
 *       únicamente con archivos de tamaño controlado.</li>
 * </ul>
 */
public interface FileStorageService {

    int BUFFER_SIZE_FILE_READ = 4096;

    /***************************************
     *  RECORDS
     ***************************************/
    record ResourceUUID(UUID uuid){ }
    record ResourceDetail(UUID id, String name, String contentType, long size ) { }
    record ResourceContent(UUID id, String name, String contentType, long size, Path filePath ) { }




    Mono<ResourceContent> getResourceDetail(String id);

    Mono<Pair<String,MediaType>> getResourceMediaType(Path filePath);
    /**
     * Obtiene un archivo como un recurso enriquecido que incluye metadatos
     * y, opcionalmente, su contenido.
     * <p>
     * Este método es útil cuando se requiere acceder tanto a la información
     * descriptiva del archivo (nombre, tipo de contenido, media type, etc.)
     * como a su contenido binario.
     *
     * <p>
     * El contenido puede no estar presente dependiendo de la implementación.
     * En ese caso, el {@link ResourceContent#filePath()} devolverá
     * un {@link Optional#empty()}.
     * </p>
     *
     * @param filename nombre del archivo a recuperar
     * @return un {@link Mono} que emite un {@code ResourceCustom} con los metadatos
     *         y el contenido opcional del archivo; finaliza con error si el archivo
     *         no existe o no es accesible
     */
    Mono<ResourceContent> getResourceContent(String filename);
    /**
     * Lee el contenido de un archivo ubicado en el sistema de archivos y lo devuelve
     * completamente en memoria como un {@link ByteArrayOutputStream}.
     * <p>
     * La lectura se realiza de forma reactiva utilizando buffers de tamaño configurable.
     * Internamente, las operaciones potencialmente bloqueantes deben ejecutarse en un
     * {@link reactor.core.scheduler.Scheduler} adecuado (por ejemplo, {@code boundedElastic}).
     *
     * <p><strong>Advertencia:</strong></p>
     * <ul>
     *   <li>Este método carga el archivo completo en memoria.</li>
     *   <li>Debe utilizarse únicamente para archivos pequeños o de tamaño controlado.</li>
     * </ul>
     *
     * @param filePath ruta absoluta del archivo a leer
     * @param bufferSize tamaño opcional del buffer de lectura (en bytes);
     *                   si no se especifica, se utilizará un valor por defecto
     * @return un {@link Mono} que emite un {@link ByteArrayOutputStream} con el contenido
     *         completo del archivo; finaliza con error si el archivo no existe,
     *         está vacío o no puede leerse
     */
    Mono<ByteArrayOutputStream> readFileContent(Path filePath, Integer... bufferSize);

    /**
     * Lee el contenido de un archivo y lo expone como un flujo reactivo de
     * {@link DataBuffer}, permitiendo su consumo en streaming.
     * <p>
     * Este método es la opción recomendada para:
     * <ul>
     *   <li>Descarga de archivos mediante HTTP.</li>
     *   <li>Procesamiento de archivos grandes.</li>
     *   <li>Escenarios donde se requiere un bajo consumo de memoria.</li>
     * </ul>
     *
     * <p>
     * El archivo se lee por fragmentos, respetando el mecanismo de backpressure
     * del modelo reactivo.
     * </p>
     *
     * <p><strong>Gestión de memoria:</strong></p>
     * <ul>
     *   <li>Los {@link DataBuffer} emitidos deben ser liberados correctamente.</li>
     *   <li>Cuando se escriben directamente en un {@code ServerHttpResponse},
     *       la liberación se gestiona automáticamente por el framework.</li>
     * </ul>
     *
     * @param filename nombre del archivo a leer
     * @return un {@link Flux} de {@link DataBuffer} que emite el contenido del archivo
     *         de forma incremental; finaliza con error si el archivo no existe
     *         o no es accesible
     */
    Flux<DataBuffer> readFileAsDataBuffer(String filename);

    /**
     * Lee el contenido completo de un archivo y lo devuelve como un arreglo de bytes.
     * <p>
     * Este método proporciona una forma sencilla de acceder al contenido del archivo
     * cuando se requiere tener todos los datos en memoria para su procesamiento.
     *
     * <p><strong>Advertencia:</strong></p>
     * <ul>
     *   <li>El archivo se carga completamente en memoria.</li>
     *   <li>No debe utilizarse para archivos grandes o de tamaño desconocido.</li>
     * </ul>
     *
     * @param filename nombre del archivo a leer
     * @return un {@link Mono} que emite un {@code byte[]} con el contenido completo
     *         del archivo; {@link Mono#empty()} si el archivo no contiene datos,
     *         o finaliza con error si no puede leerse
     */
    Mono<byte[]> getFileContent(String filename);

    /**
     * Elimina un archivo del sistema de archivos.
     * <p>
     * Esta operación es definitiva y no puede deshacerse.
     * Se debe asegurar que el archivo existe y que se tienen los permisos
     * necesarios antes de invocar este método.
     *
     * @param filename nombre del archivo a eliminar
     * @return un {@link Mono} que completa cuando el archivo ha sido eliminado;
     *         finaliza con error si el archivo no existe o no puede eliminarse
     */
    Mono<Void> deleteFile(String filename);
    /***************************************
     *  DEFAULT METHODS
     *************************************/

    /**
     * Formato utilizado para generar nombres de archivo únicos.
     * <p>
     * El formato espera dos parámetros:
     * <ol>
     *   <li>Un identificador único (UUID)</li>
     *   <li>El nombre del archivo normalizado</li>
     * </ol>
     *
     * Ejemplo de resultado:
     * <pre>
     * {@code 550e8400-e29b-41d4-a716-446655440000-documento.pdf}
     * </pre>
     */
    String UNIQUE_NAME_FORMAT = "%s-%s";

    String UUID_NAME = "%s";
    /**
     * Normaliza un nombre de archivo para que sea seguro y válido para su uso
     * en sistemas de archivos.
     * <p>
     * La normalización puede incluir (según implementación):
     * <ul>
     *   <li>Eliminación de rutas relativas o absolutas (ej. "../", "/", "\\").</li>
     *   <li>Sustitución de espacios por guiones o guiones bajos.</li>
     *   <li>Eliminación de caracteres especiales o no permitidos.</li>
     *   <li>Conversión a minúsculas.</li>
     * </ul>
     *
     * El método no realiza operaciones de E/S ni valida la existencia del archivo.
     *
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * Entrada : "../Mi Archivo (final).PDF"
     * Salida  : "mi_archivo_final.pdf"
     * </pre>
     *
     * @param fileName nombre original del archivo
     * @return un {@link Optional} con el nombre normalizado si es válido;
     *         {@link Optional#empty()} si el nombre es nulo, vacío o no puede normalizarse
     */
    static Optional<String> normaliceName(String fileName) {
        return Optional.ofNullable(fileName)
                .map(String::trim)
                .map(String::toLowerCase)
                .map(f -> f.replace(" ", ""))
                .map(f -> f.replace("..", ""))
                .map(f -> f.replace("/", ""))
                .map(f -> f.replace("\\", ""));
    }

    static String buildNameFileUniqueNormalice(UUID uuid) {
        return normaliceName(uuid.toString())
                .map(nameNormalize -> UUID_NAME.formatted(uuid))
                .orElseThrow(() -> new IllegalArgumentException("Filename is invalid after normalization"));
    }
    /**
     * Genera un nombre de archivo único y seguro a partir del nombre original proporcionado.
     *
     * Este método combina el UUID proporcionado con la versión normalizada del nombre original para garantizar:
     * <ul>
     *   <li>Unicidad: evitando colisiones de nombres al almacenar múltiples archivos.</li>
     *   <li>Seguridad: evitando caracteres inválidos o path traversal en el sistema de archivos.</li>
     * </ul>
     *
     * <p><strong>Formato final:</strong></p>
     * <pre>{@code UUID-nombreNormalizado}</pre>
     *
     * <p><strong>Ejemplo:</strong></p>
     * <pre>{@code
     * UUID generado: f47ac10b-58cc-4372-a567-0e02b2c3d479
     * Entrada : "mi archivo.pdf"
     * Salida  : "f47ac10b-58cc-4372-a567-0e02b2c3d479-mi_archivo.pdf"
     * }</pre>
     *
     * @param uuid UUID único que se antepone al nombre normalizado
     * @param fileName nombre original del archivo proporcionado por el cliente
     * @return un {@link String} con el nombre de archivo único y normalizado
     * @throws IllegalArgumentException si el nombre no puede ser normalizado
     *
     * @see #normaliceName(String)
     */
    static String buildNameFileUniqueNormalice(UUID uuid, String fileName) {
        return normaliceName(fileName)
                .map(nameNormalize -> UNIQUE_NAME_FORMAT.formatted(uuid, nameNormalize))
                .orElseThrow(() -> new IllegalArgumentException("Filename is invalid after normalization"));
    }


    static boolean existPathDir(Path pathDir) {
        return pathDir != null && Files.isDirectory(pathDir);
    }


    static Path amountFileToPath(Path dirPath, String filename) {
        if (dirPath == null || !Files.isDirectory(dirPath) || !Files.isReadable(dirPath) || !Files.isWritable(dirPath)) {
            throw new IllegalArgumentException("Directory path is invalid or not accessible %s".formatted(dirPath));
        }

        var maybeName = normaliceName(filename);
        if (maybeName.isEmpty()) {
            throw new IllegalArgumentException("Filename is invalid after normalization");
        }

        return dirPath.toAbsolutePath()
                .normalize()
                .resolve(maybeName.get())
                .normalize();
    }

    /**
     * Obtiene de forma reactiva el Content-Type de un archivo dado.
     * <p>
     * Usa {@link Files#probeContentType} de forma no bloqueante, ejecutándolo en un hilo boundedElastic.
     * Si no se puede determinar el tipo, retorna "application/octet-stream" por defecto.
     *
     * @param filePath Ruta del archivo a inspeccionar. Debe existir y ser legible.
     * @return Mono con un {@link Pair} donde el primer valor es el Content-Type como {@link String}
     *         y el segundo valor como {@link MediaType}.
     * @throws IllegalArgumentException si filePath es nulo, no existe o no es legible.
     */
    static Mono<Pair<String, MediaType>> getContentType(Path filePath) {
        if (filePath == null ) {
            return Mono.error(
                    new IllegalArgumentException("filePath cannot be null")
            );
        }

        return Mono.fromCallable(() -> Files.probeContentType(filePath))
                .subscribeOn(Schedulers.boundedElastic())
                .defaultIfEmpty(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .map(ct -> Pair.of(ct, MediaType.parseMediaType(ct)));
    }

    /**
     * Valida si una cadena es un UUID válido.
     *
     * @param uuidStr la cadena a validar
     * @return {@code true} si la cadena es un UUID válido; {@code false} en caso contrario
     */
    static boolean isValidUUID(String uuidStr) {
        if (uuidStr == null || uuidStr.trim().isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(uuidStr);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Valida si un resourceId es nulo, vacío o no es un UUID válido.
     *
     * @param resourceId el identificador del recurso a validar
     * @return {@code true} si el resourceId es nulo, vacío o inválido; {@code false} en caso contrario
     */
    static boolean isResourceIdInvalid(String resourceId) {
        return resourceId == null || resourceId.trim().isEmpty() || !isValidUUID(resourceId);
    }

    static Mono<Path> getPathFileByResourceId(String basePath, String resourceId) {
        if (resourceId == null || resourceId.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Resource ID cannot be null or empty"));
        }

        Path pathUploadDir = Path.of( basePath ).toAbsolutePath().normalize();

        if( !FileStorageService.existPathDir( pathUploadDir ) ) {
            return Mono.error(new IllegalArgumentException("Upload directory does not exist"));
        }

        // Ejecutar la búsqueda en boundedElastic porque usa APIs bloqueantes de java.nio.file
        return Mono.fromCallable(() -> {
            // 1) Intentar ruta directa: el caller puede pasar ya el nombre único (UUID-nombre)
            Path direct = pathUploadDir.resolve(resourceId).normalize();
            if (Files.exists(direct) && Files.isRegularFile(direct)) {
                return direct;
            }

            // 2) Si resourceId es un UUID válido, buscar archivos que empiecen por "{uuid}-"
            if (isValidUUID(resourceId)) {
                try (var stream = Files.list(pathUploadDir)) {
                    return stream.filter(p -> p.getFileName().toString().startsWith(resourceId + "-"))
                            .findFirst().orElse(null);
                }
            }

            // 3) Tratar resourceId como 'nombre final' y buscar archivos que terminen en "-nombreNormalizado"
            var maybeNormalized = normaliceName(resourceId);
            if (maybeNormalized.isPresent()) {
                String suffix = "-" + maybeNormalized.get();
                try (var stream = Files.list(pathUploadDir)) {
                    return stream.filter(p -> p.getFileName().toString().endsWith(suffix))
                            .findFirst().orElse(null);
                }
            }

            // Nada encontrado
            return null;
        }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty);
    }
}
