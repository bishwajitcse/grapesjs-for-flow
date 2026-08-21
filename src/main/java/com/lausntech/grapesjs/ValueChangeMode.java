package com.lausntech.grapesjs;

/**
 * Defines when {@link GrapesJsEditor} pushes its HTML value to the server as a
 * value-change event.
 */
public enum ValueChangeMode {

    /**
     * A value change is sent whenever GrapesJS fires its aggregate
     * {@code update} event (any component/style change in the project).
     */
    CHANGE,

    /**
     * Like {@link #CHANGE}, but eagerly debounced with
     * {@link GrapesJsEditor#setDebounceTimeout(int)}.
     */
    TIMEOUT,

    /**
     * A value change is sent only when the canvas loses focus.
     */
    BLUR
}
