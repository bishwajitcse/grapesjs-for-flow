package com.lausntech.grapesjs;

/**
 * Thrown when a browser-side GrapesJS operation (invoked asynchronously via
 * the frontend connector) fails, e.g. malformed project JSON passed to
 * {@link GrapesJsEditor#loadProjectData(String)}, or an uncaught JavaScript
 * error while executing the request.
 */
public class GrapesJsException extends RuntimeException {

    /**
     * Creates a new exception with the given message.
     *
     * @param message the error message
     */
    public GrapesJsException(String message) {
        super(message);
    }
}
