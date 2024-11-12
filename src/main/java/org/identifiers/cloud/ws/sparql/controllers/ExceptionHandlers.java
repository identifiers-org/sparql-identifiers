package org.identifiers.cloud.ws.sparql.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.identifiers.cloud.ws.sparql.data.resolution_models.EndpointResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;


@Slf4j
@ControllerAdvice
public class ExceptionHandlers {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @ExceptionHandler(value = {MissingRequestValueException.class, HttpMediaTypeNotSupportedException.class, MalformedQueryException.class})
    public void handleExceptionAsEndpointResponse(Throwable e, HttpServletResponse response) throws IOException {
        var endpointResponse = new EndpointResponse("1.0", e.getMessage(), null);

        //Done like this to ignore accept header and always answer json on error
        // May fix in the future if we find a good way to display error messages in other formats
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getOutputStream(), endpointResponse);
    }
}
