package com.lausntech.grapesjs;

import com.vaadin.flow.component.ComponentEvent;

/**
 * Fired once the underlying GrapesJS editor instance has finished
 * initializing and rendered its initial content, i.e. on GrapesJS's own
 * {@code load} event. Register with
 * {@link GrapesJsEditor#addReadyListener(com.vaadin.flow.component.ComponentEventListener)}.
 */
public class GrapesJsReadyEvent extends ComponentEvent<GrapesJsEditor> {

    /**
     * Creates a new event.
     *
     * @param source the editor that fired the event
     * @param fromClient {@code true} if the event originated from the client
     */
    public GrapesJsReadyEvent(GrapesJsEditor source, boolean fromClient) {
        super(source, fromClient);
    }
}
