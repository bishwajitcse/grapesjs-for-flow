package com.lausntech.grapesjs;

import java.io.Serializable;
import java.util.Objects;

/**
 * Definition of a responsive-design device/breakpoint for the GrapesJS
 * Device Manager. See {@link GrapesJsEditor#addDevice(GrapesJsDevice)} and
 * {@link GrapesJsEditor#setDevice(String)}.
 */
public class GrapesJsDevice implements Serializable {

    /** Default device present in every editor instance. */
    public static final String DESKTOP = "Desktop";
    /** Default device present in every editor instance. */
    public static final String TABLET = "Tablet";
    /** Default device present in every editor instance. */
    public static final String MOBILE_LANDSCAPE = "Mobile landscape";
    /** Default device present in every editor instance. */
    public static final String MOBILE_PORTRAIT = "Mobile portrait";

    private final String name;
    private final String width;
    private String widthMedia;

    /**
     * Creates a new device.
     *
     * @param name unique device name
     * @param width the canvas width for this device (e.g. {@code "768px"}), or an empty string for 100%
     */
    public GrapesJsDevice(String name, String width) {
        this.name = Objects.requireNonNull(name, "name");
        this.width = Objects.requireNonNull(width, "width");
    }

    /**
     * Returns the device name.
     *
     * @return the device name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the canvas width for this device.
     *
     * @return the canvas width
     */
    public String getWidth() {
        return width;
    }

    /**
     * Returns the width used in the generated CSS media query for this
     * device, if different from {@link #getWidth()}.
     *
     * @return the media query width, or {@code null} if unset
     */
    public String getWidthMedia() {
        return widthMedia;
    }

    /**
     * Sets the width used in the generated CSS media query for this device.
     *
     * @param widthMedia the media query width
     * @return this instance, for chaining
     */
    public GrapesJsDevice setWidthMedia(String widthMedia) {
        this.widthMedia = widthMedia;
        return this;
    }
}
