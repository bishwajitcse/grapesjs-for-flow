package com.lausntech.grapesjs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A small built-in library of named block templates, keyed by a short type
 * id (e.g. {@code "hero"}, {@code "two-columns"}). Used by
 * {@link GrapesJsEditor#addBlocks(Map)} to let applications register a set
 * of blocks declaratively, without having to author the HTML for common
 * layout patterns themselves:
 *
 * <pre>{@code
 * editor.addBlocks(Map.of(
 *         "Sections", List.of("hero", "feature-section"),
 *         "Layout", List.of("two-columns", "three-columns", "container"),
 *         "Basic", List.of("text", "heading", "image", "button", "link")));
 * }</pre>
 * <p>
 * This is a starting set covering common page-building patterns; register
 * anything else with {@link GrapesJsEditor#addBlock(GrapesJsBlock)} directly.
 */
public final class GrapesJsBlockPresets {

    private static final Map<String, GrapesJsBlock> PRESETS = new LinkedHashMap<>();

    static {
        register("text", "Text", "<div data-gjs-type=\"text\">Insert your text here</div>");

        register("heading", "Heading", "<h2>Heading text</h2>");

        register("image", "Image",
                "<img src=\"https://placehold.co/600x400\" style=\"max-width:100%\" alt=\"\" />");

        register("button", "Button", """
                <a href="#" style="display:inline-block;padding:10px 20px;background:#4f46e5;
                    color:#fff;border-radius:6px;text-decoration:none">Click me</a>
                """);

        register("link", "Link", "<a href=\"#\">Link text</a>");

        register("container", "Container", "<div style=\"padding:16px\">Container</div>");

        register("section", "Section", """
                <section style="padding:32px">
                    <h2>Section title</h2>
                    <p>Section content goes here.</p>
                </section>
                """);

        register("two-columns", "Two Columns", """
                <div style="display:flex;gap:16px;padding:16px">
                    <div style="flex:1;padding:16px;background:#f8fafc">Column A</div>
                    <div style="flex:1;padding:16px;background:#f8fafc">Column B</div>
                </div>
                """);

        register("three-columns", "Three Columns", """
                <div style="display:flex;gap:16px;padding:16px">
                    <div style="flex:1;padding:16px;background:#f8fafc">Column A</div>
                    <div style="flex:1;padding:16px;background:#f8fafc">Column B</div>
                    <div style="flex:1;padding:16px;background:#f8fafc">Column C</div>
                </div>
                """);

        register("hero", "Hero", """
                <section style="padding:48px 24px;text-align:center;background:#eef2ff">
                    <h1>Hero title</h1>
                    <p>Hero subtitle text goes here.</p>
                </section>
                """);

        register("card", "Card", """
                <div style="max-width:320px;padding:16px;border:1px solid #e2e8f0;border-radius:8px">
                    <h3>Card title</h3>
                    <p>Card description text.</p>
                </div>
                """);

        register("image-text", "Image + Text", """
                <div style="display:flex;gap:24px;align-items:center;padding:16px">
                    <img src="https://placehold.co/300x200" style="flex:1;max-width:100%" alt="" />
                    <div style="flex:1">
                        <h3>Title</h3>
                        <p>Supporting text next to the image.</p>
                    </div>
                </div>
                """);

        register("feature-section", "Feature Section", """
                <section style="padding:48px 24px">
                    <h2 style="text-align:center">Features</h2>
                    <div style="display:flex;gap:16px;margin-top:24px">
                        <div style="flex:1;padding:16px;text-align:center">
                            <h3>Feature A</h3>
                            <p>Description of feature A.</p>
                        </div>
                        <div style="flex:1;padding:16px;text-align:center">
                            <h3>Feature B</h3>
                            <p>Description of feature B.</p>
                        </div>
                        <div style="flex:1;padding:16px;text-align:center">
                            <h3>Feature C</h3>
                            <p>Description of feature C.</p>
                        </div>
                    </div>
                </section>
                """);
    }

    private GrapesJsBlockPresets() {
    }

    private static void register(String type, String label, String content) {
        PRESETS.put(type, new GrapesJsBlock(type, label, content));
    }

    /**
     * Returns the known preset block type ids, e.g. for validation or
     * listing them in a UI.
     *
     * @return the known preset type ids
     */
    public static Set<String> knownTypes() {
        return Collections.unmodifiableSet(PRESETS.keySet());
    }

    /**
     * Returns a fresh copy of the preset registered under {@code type}
     * (without a category set), or throws if the type is unknown.
     *
     * @param type a preset id, e.g. {@code "hero"}
     * @throws IllegalArgumentException if no preset is registered under that id
     */
    static GrapesJsBlock get(String type) {
        GrapesJsBlock preset = PRESETS.get(type);
        if (preset == null) {
            throw new IllegalArgumentException(
                    "Unknown block type '" + type + "'. Known types: " + PRESETS.keySet());
        }
        return new GrapesJsBlock(preset.getId(), preset.getLabel(), preset.getContent()).setMedia(preset.getMedia());
    }
}
