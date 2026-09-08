package com.lausntech.grapesjs;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Theme variants for {@link GrapesJsEditor}, applied via
 * {@link com.vaadin.flow.component.shared.HasThemeVariant#addThemeVariants(ThemeVariant...)}.
 */
public enum GrapesJsEditorVariant implements ThemeVariant {

    /**
     * Removes the default border/box-shadow that the addon draws around the
     * editor, in case the surrounding layout already provides one.
     */
    NO_BORDER("no-border"),

    /**
     * Gives the editor's chrome a dark appearance &mdash; similar to
     * <a href="https://grapesjs.com/demo.html">GrapesJS's own demo</a>
     * &mdash; regardless of the surrounding application's theme.
     */
    DARK("dark");

    private final String variantName;

    GrapesJsEditorVariant(String variantName) {
        this.variantName = variantName;
    }

    @Override
    public String getVariantName() {
        return variantName;
    }
}
