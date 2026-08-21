package com.lausntech.grapesjs;

import java.io.Serializable;
import java.util.Objects;

/**
 * An asset (typically an image) made available in the GrapesJS Asset
 * Manager. The addon intentionally does not couple itself to any specific
 * upload mechanism: applications are expected to upload files through their
 * own means (e.g. Vaadin {@code Upload} to a REST endpoint or object
 * storage) and register the resulting URL with
 * {@link GrapesJsEditor#addAsset(GrapesJsAsset)}.
 */
public class GrapesJsAsset implements Serializable {

    private final String src;
    private String name;
    private String type = "image";

    /**
     * Creates a new asset for the given URL.
     *
     * @param src the asset URL
     */
    public GrapesJsAsset(String src) {
        this.src = Objects.requireNonNull(src, "src");
    }

    /**
     * Returns the asset URL.
     *
     * @return the asset URL
     */
    public String getSrc() {
        return src;
    }

    /**
     * Returns the display name shown in the Asset Manager, if set.
     *
     * @return the display name, or {@code null} if unset
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name shown in the Asset Manager.
     *
     * @param name the display name
     * @return this instance, for chaining
     */
    public GrapesJsAsset setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the asset type (e.g. {@code "image"}, the default).
     *
     * @return the asset type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the asset type.
     *
     * @param type the asset type
     * @return this instance, for chaining
     */
    public GrapesJsAsset setType(String type) {
        this.type = type;
        return this;
    }
}
