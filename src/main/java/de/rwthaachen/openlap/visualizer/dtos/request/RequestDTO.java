package de.rwthaachen.openlap.visualizer.dtos.request;

/**
 * The super class of all the requests sent by the Modular Visualization Framework.
 * Extend this class and add details of the requests
 */
public class RequestDTO<T> {
    private T requestBodyContent;

    public T getRequestBodyContent() {
        return requestBodyContent;
    }

    public void setRequestBodyContent(T requestBodyContent) {
        this.requestBodyContent = requestBodyContent;
    }
}