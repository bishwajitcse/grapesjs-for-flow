package com.lausntech.grapesjs;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Fired when the selected component in the canvas changes, i.e. on
 * GrapesJS's {@code component:selected} event. Carries a lightweight
 * summary of the selection rather than the full component model, to avoid
 * shipping large payloads to the server on every click. Register with
 * {@link GrapesJsEditor#addSelectListener(com.vaadin.flow.component.ComponentEventListener)}.
 */
@DomEvent("gjs-select")
public class GrapesJsSelectEvent extends ComponentEvent<GrapesJsEditor> {

    private final String componentId;
    private final String tagName;

    /**
     * Creates a new event.
     *
     * @param source the editor that fired the event
     * @param fromClient {@code true} if the event originated from the client
     * @param componentId the GrapesJS-internal id of the selected component
     * @param tagName the HTML tag name of the selected component
     */
    public GrapesJsSelectEvent(GrapesJsEditor source, boolean fromClient,
            @EventData("event.componentId") String componentId, @EventData("event.tagName") String tagName) {
        super(source, fromClient);
        this.componentId = componentId;
        this.tagName = tagName;
    }

    /**
     * Returns the GrapesJS-internal id of the selected component, or an
     * empty string if the selection was cleared.
     *
     * @return the selected component's id
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Returns the HTML tag name of the selected component, or an empty
     * string if the selection was cleared.
     *
     * @return the selected component's tag name
     */
    public String getTagName() {
        return tagName;
    }
}
