package com.lausntech.grapesjs;

/**
 * Thrown when a configuration method is called after the GrapesJS editor
 * instance has already been initialized in the browser. Such configuration
 * (blocks/devices excluded, see {@link GrapesJsEditor}) must be applied before
 * the component is attached to a UI.
 */
public class AlreadyInitializedException extends RuntimeException {

    /**
     * Creates a new exception with a default message.
     */
    public AlreadyInitializedException() {
        super("Cannot apply configuration to the editor, it has already been initialized. "
                + "Apply configuration before attaching the component, or detach it first.");
    }
}
