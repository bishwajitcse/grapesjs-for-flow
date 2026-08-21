package com.lausntech.grapesjs;

import java.io.Serializable;
import java.util.Objects;

/**
 * Definition of a GrapesJS block, i.e. a piece of HTML that can be dragged
 * from the Block Manager panel into the canvas. Mirrors the subset of
 * GrapesJS's {@code BlockProperties} that is useful from Java; see
 * {@link GrapesJsEditor#addBlock(GrapesJsBlock)}.
 */
public class GrapesJsBlock implements Serializable {

    private final String id;
    private String label;
    private String category;
    private String content;
    private String media;

    /**
     * Creates a new block.
     *
     * @param id unique block id
     * @param label the label shown in the Block Manager panel
     * @param content the HTML content inserted into the canvas when the block is dropped
     */
    public GrapesJsBlock(String id, String label, String content) {
        this.id = Objects.requireNonNull(id, "id");
        this.label = Objects.requireNonNull(label, "label");
        this.content = Objects.requireNonNull(content, "content");
    }

    /**
     * Returns the block id.
     *
     * @return the block id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the label shown in the Block Manager panel.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label shown in the Block Manager panel.
     *
     * @param label the label
     * @return this instance, for chaining
     */
    public GrapesJsBlock setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Returns the Block Manager category this block is grouped under, if set.
     *
     * @return the category, or {@code null} if unset
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the Block Manager category this block is grouped under.
     *
     * @param category the category
     * @return this instance, for chaining
     */
    public GrapesJsBlock setCategory(String category) {
        this.category = category;
        return this;
    }

    /**
     * Returns the HTML content inserted into the canvas when the block is dropped.
     *
     * @return the HTML content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the HTML content inserted into the canvas when the block is dropped.
     *
     * @param content the HTML content
     * @return this instance, for chaining
     */
    public GrapesJsBlock setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Returns the HTML snippet (typically an inline SVG or an {@code <img>}
     * tag) shown as the block's icon/preview in the Block Manager panel, if
     * set.
     *
     * @return the media HTML, or {@code null} if unset
     */
    public String getMedia() {
        return media;
    }

    /**
     * Sets the HTML snippet shown as the block's icon/preview.
     *
     * @param media the media HTML
     * @return this instance, for chaining
     */
    public GrapesJsBlock setMedia(String media) {
        this.media = media;
        return this;
    }
}
