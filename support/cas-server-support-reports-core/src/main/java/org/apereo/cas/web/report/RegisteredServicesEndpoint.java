package org.apereo.cas.web.report;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.io.TemporaryFileSystemResource;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link RegisteredServicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Endpoint(id = "registeredServices", enableByDefault = false)
@Slf4j
public class RegisteredServicesEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<ServicesManager> servicesManager;

    private final ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory;

    private final ObjectProvider<List<? extends StringSerializer<RegisteredService>>> registeredServiceSerializers;

    public RegisteredServicesEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<ServicesManager> servicesManager,
        final ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory,
        final ObjectProvider<List<? extends StringSerializer<RegisteredService>>> registeredServiceSerializers,
        final ObjectProvider<ConfigurableApplicationContext> applicationContext) {
        super(casProperties, applicationContext.getObject());
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.registeredServiceSerializers = registeredServiceSerializers;
    }

    /**
     * Handle and produce a list of services from registry.
     *
     * @return collection of services
     * @throws Exception the exception
     */
    @Operation(summary = "Handle and produce a list of services from registry")
    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MEDIA_TYPE_CAS_YAML
    })
    public ResponseEntity<String> handle() throws Exception {
        return ResponseEntity.ok(MAPPER.writeValueAsString(
            servicesManager.getObject().load()
                .stream()
                .filter(RegisteredServiceProperty.RegisteredServiceProperties.INTERNAL_SERVICE_DEFINITION::isNotAssignedTo)
                .collect(Collectors.toList())
        ));
    }

    /**
     * Fetch service either by numeric id or service id pattern.
     *
     * @param id the id
     * @return the registered service
     * @throws Exception the exception
     */
    @Operation(summary = "Fetch service either by numeric id or service id pattern")
    @GetMapping(path = "{id}", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MEDIA_TYPE_CAS_YAML
    })
    public ResponseEntity<String> fetchService(@PathVariable final String id) throws Exception {
        val service = NumberUtils.isDigits(id)
            ? servicesManager.getObject().findServiceBy(Long.parseLong(id))
            : servicesManager.getObject().findServiceBy(webApplicationServiceFactory.getObject().createService(id));
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(MAPPER.writeValueAsString(service));
    }

    /**
     * Fetch services by type response entity.
     *
     * @param type the simple name of the CAS service type (i.e {@link org.apereo.cas.services.CasRegisteredService}
     * @return the response entity
     * @throws Exception the exception
     */
    @Operation(summary = "Fetch services by their type")
    @GetMapping(path = "type/{type}", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MEDIA_TYPE_CAS_YAML
    })
    public ResponseEntity<String> fetchServicesByType(@PathVariable final String type) throws Exception {
        val services = servicesManager.getObject().findServiceBy(registeredService ->
            registeredService.getClass().getSimpleName().equalsIgnoreCase(type));
        return ResponseEntity.ok(MAPPER.writeValueAsString(services));
    }

    /**
     * Delete registered service.
     *
     * @param id the id
     * @return the registered service
     * @throws Exception the exception
     */
    @Operation(summary = "Delete registered service by id")
    @DeleteMapping(path = "{id}",
        consumes = {
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    public ResponseEntity<String> deleteService(@PathVariable final String id) throws Exception {
        if (NumberUtils.isDigits(id)) {
            val svc = servicesManager.getObject().findServiceBy(Long.parseLong(id));
            if (svc != null) {
                return ResponseEntity.ok(MAPPER.writeValueAsString(servicesManager.getObject().delete(svc)));
            }
        } else {
            val svc = servicesManager.getObject().findServiceBy(
                webApplicationServiceFactory.getObject().createService(id));
            if (svc != null) {
                return ResponseEntity.ok(MAPPER.writeValueAsString(
                    servicesManager.getObject().delete(svc)));
            }
        }
        LOGGER.warn("Could not locate service definition by id [{}]", id);
        return ResponseEntity.notFound().build();
    }

    /**
     * Import services.
     *
     * @param request the request
     * @return the http status
     * @throws Exception the exception
     */
    @PostMapping(path = "/import", consumes = {
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    })
    @Operation(summary = "Import registered services as a JSON document or a zip file")
    public ResponseEntity<RegisteredService> importService(final HttpServletRequest request) throws Exception {
        val contentType = request.getContentType();
        if (StringUtils.equalsAnyIgnoreCase(MediaType.APPLICATION_OCTET_STREAM_VALUE, contentType)) {
            return importServicesAsStream(request);
        }
        return importSingleService(request);
    }

    /**
     * Export.
     *
     * @return the response entity
     */
    @GetMapping(path = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Export registered services as a zip file")
    public ResponseEntity<Resource> export() {
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val resource = CompressionUtils.toZipFile(servicesManager.getObject().stream(),
            Unchecked.function(entry -> {
                val service = (RegisteredService) entry;
                val fileName = String.format("%s-%s", service.getName(), service.getId());
                val sourceFile = File.createTempFile(fileName, ".json");
                serializer.to(sourceFile, service);
                return sourceFile;
            }), "services");
        val headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        headers.put("Filename", List.of("services.zip"));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * Export response.
     *
     * @param id the id
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(path = "/export/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Export registered services as a single JSON file",
        parameters = @Parameter(name = "id", required = true, description = "The id of the registered service to export", in = ParameterIn.PATH))
    public ResponseEntity<Resource> export(@PathVariable("id") final long id) throws Exception {
        val registeredServiceSerializer = new RegisteredServiceJsonSerializer(applicationContext);
        val registeredService = servicesManager.getObject().findServiceBy(id);
        val fileName = String.format("%s-%s", registeredService.getName(), registeredService.getId());
        val serviceFile = File.createTempFile(fileName, ".json");
        registeredServiceSerializer.to(serviceFile, registeredService);
        val headers = new HttpHeaders();
        val resource = new TemporaryFileSystemResource(serviceFile);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        headers.put("Filename", List.of(serviceFile.getName()));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * Save service response entity.
     *
     * @param registeredServiceBody the registered service body
     * @return the response entity
     */
    @PostMapping(consumes = {
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    })
    @ResponseBody
    @Operation(summary = "Save registered service supplied in the request body",
         parameters = @Parameter(name = "body", required = true, description = "The request body to contain service definition"))
    public ResponseEntity saveService(@RequestBody final String registeredServiceBody) {
        val registeredServiceSerializer = new RegisteredServiceJsonSerializer(applicationContext);
        val registeredService = registeredServiceSerializer.from(registeredServiceBody);
        registeredService.setId(RandomUtils.nextInt());
        val result = servicesManager.getObject().save(registeredService);
        return ResponseEntity.ok(registeredServiceSerializer.toString(result));
    }


    /**
     * Update service response entity.
     *
     * @param registeredServiceBody the registered service body
     * @return the response entity
     */
    @PutMapping(consumes = {
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    })
    @ResponseBody
    @Operation(summary = "Update registered service supplied in the request body",
        parameters = @Parameter(name = "body", required = true, description = "The request body to contain service definition"))
    public ResponseEntity updateService(@RequestBody final String registeredServiceBody) {
        val registeredServiceSerializer = new RegisteredServiceJsonSerializer(applicationContext);
        val registeredService = registeredServiceSerializer.from(registeredServiceBody);
        val result = servicesManager.getObject().save(registeredService);
        return ResponseEntity.ok(registeredServiceSerializer.toString(result));
    }

    private ResponseEntity<RegisteredService> importSingleService(final HttpServletRequest request) throws IOException {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Submitted registered service:\n[{}]", requestBody);

        if (StringUtils.isBlank(requestBody)) {
            LOGGER.warn("Could not extract registered services from request body");
            return ResponseEntity.badRequest().build();
        }

        return registeredServiceSerializers
            .getObject()
            .stream()
            .map(serializer -> serializer.from(requestBody))
            .filter(Objects::nonNull)
            .findFirst()
            .map(service -> {
                LOGGER.trace("Storing registered service:\n[{}]", service);
                return servicesManager.getObject().save(service);
            })
            .map(service -> {
                val headers = new HttpHeaders();
                headers.put("id", CollectionUtils.wrapList(String.valueOf(service.getId())));
                return new ResponseEntity<>(service, headers, HttpStatus.CREATED);
            })
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    private ResponseEntity<RegisteredService> importServicesAsStream(final HttpServletRequest request) throws IOException {
        var servicesToImport = Stream.<RegisteredService>empty();
        try (val bais = new ByteArrayInputStream(IOUtils.toByteArray(request.getInputStream()));
             val zipIn = new ZipArchiveInputStream(bais)) {
            var entry = zipIn.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory()) {
                    val requestBody = IOUtils.toString(zipIn, StandardCharsets.UTF_8);
                    servicesToImport = Stream.concat(servicesToImport, registeredServiceSerializers.getObject()
                        .stream()
                        .map(serializer -> serializer.from(requestBody))
                        .filter(Objects::nonNull));
                }
                entry = zipIn.getNextEntry();
            }
        }
        servicesManager.getObject().save(servicesToImport);
        return ResponseEntity.ok().build();
    }
}
