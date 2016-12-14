package com.cargowhale.docker.test.integration;

import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.bodies.Response;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.methods.Method;
import org.raml.v2.api.model.v10.resources.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

final class RamlApiBuilder {

    private RamlApiBuilder() {
    }

    static Map<String, Map<HttpMethod, Map<HttpStatus, Map<MediaType, String>>>> flattenRaml(final Api api) {
        Map<String, Map<HttpMethod, Map<HttpStatus, Map<MediaType, String>>>> resourceMap = new HashMap<>();

        for (final Resource resource : flattenResources(api)) {
            Map<HttpMethod, Map<HttpStatus, Map<MediaType, String>>> methodMap;

            methodMap = buildMethodMap(resource);
            resourceMap.put(resource.resourcePath(), methodMap);
        }

        return resourceMap;
    }

    private static Queue<Resource> flattenResources(final Api api) {
        final Queue<Resource> queue = new LinkedList<>();

        for (final Resource resource : api.resources()) {
            addResources(queue, resource);
        }
        return queue;
    }

    private static void addResources(final Queue<Resource> queue, final Resource parent) {
        queue.add(parent);

        parent.resources().forEach((Resource child) -> addResources(queue, child));
    }

    private static Map<HttpMethod, Map<HttpStatus, Map<MediaType, String>>> buildMethodMap(final Resource resource) {
        final Map<HttpMethod, Map<HttpStatus, Map<MediaType, String>>> methodMap = new HashMap<>();

        for (final Method method : resource.methods()) {
            methodMap.put(HttpMethod.resolve(method.method().toUpperCase()), buildStatusMap(method));
        }

        return methodMap;
    }

    private static Map<HttpStatus, Map<MediaType, String>> buildStatusMap(final Method method) {
        final Map<HttpStatus, Map<MediaType, String>> responseMap = new HashMap<>();

        for (final Response response : method.responses()) {
            responseMap.put(HttpStatus.valueOf(Integer.valueOf(response.code().value())), buildMediaTypeMap(response));
        }

        return responseMap;
    }

    private static Map<MediaType, String> buildMediaTypeMap(final Response response) {
        final Map<MediaType, String> exampleMap = new HashMap<>();

        for (final TypeDeclaration mediaType : response.body()) {
            exampleMap.put(MediaType.valueOf(mediaType.name()), mediaType.example().value());
        }

        return exampleMap;
    }
}
