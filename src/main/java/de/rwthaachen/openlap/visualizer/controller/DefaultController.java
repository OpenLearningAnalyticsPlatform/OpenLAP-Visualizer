package de.rwthaachen.openlap.visualizer.controller;

import de.rwthaachen.openlap.visualizer.exceptions.UnmappedURIException;
import de.rwthaachen.openlap.visualizer.dtos.error.BaseErrorDTO;
import de.rwthaachen.openlap.visualizer.dtos.request.RequestDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller which contains the code to send as a response for any unmapped requests
 */
@RestController
public class DefaultController {
    @RequestMapping("/**")
    public void unmappedRequest(@RequestBody RequestDTO<?> requestDTOBody, HttpServletRequest request) {
        String uri = request.getRequestURI();
        throw new UnmappedURIException("The endpoint " + uri + " is not supported");
    }

    @ExceptionHandler(UnmappedURIException.class)
    public ResponseEntity<Object> handleUnmappedURIException(UnmappedURIException exception, HttpServletRequest request) {
        //log the error
        //create a client response
        BaseErrorDTO error = BaseErrorDTO.createBaseErrorDTO(exception.getLocalStatusCode(),exception.getLocalizedMessage(),"","");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(error,headers,HttpStatus.NOT_IMPLEMENTED);
    }
}