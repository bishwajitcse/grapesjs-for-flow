package com.lausntech.grapesjs;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Fired when a custom toolbar button registered via
 * {@link GrapesJsEditor#addToolbarButton(String, String)} is clicked.
 * Carries the clicked button's id, so a single listener can distinguish
 * between multiple registered buttons. Register with
 * {@link GrapesJsEditor#addToolbarButtonClickListener(com.vaadin.flow.component.ComponentEventListener)}.
 */
@DomEvent("gjs-toolbar-button-click")
public class GrapesJsToolbarButtonClickEvent extends ComponentEvent<GrapesJsEditor> {

    private final String buttonId;

    /**
     * Creates a new event.
     *
     * @param source the editor that fired the event
     * @param fromClient {@code true} if the event originated from the client
     * @param buttonId the id of the clicked button, as passed to {@link GrapesJsEditor#addToolbarButton(String, String)}
     */
    public GrapesJsToolbarButtonClickEvent(GrapesJsEditor source, boolean fromClient,
            @EventData("event.buttonId") String buttonId) {
        super(source, fromClient);
        this.buttonId = buttonId;
    }

    /**
     * Returns the id of the clicked button.
     *
     * @return the button id
     */
    public String getButtonId() {
        return buttonId;
    }
}
