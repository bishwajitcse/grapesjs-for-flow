package com.lausntech.grapesjs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Reference to a GrapesJS plugin to enable on the editor.
 * <p>
 * GrapesJS plugins are plain JavaScript functions of the shape
 * {@code (editor, options) => { ... }}; an arbitrary JavaScript function
 * cannot be represented as a Java object, so this class only carries the
 * plugin's registered name (matching GrapesJS's own {@code plugins}/
 * {@code pluginsOpts} init options) plus its options. The plugin function
 * itself must be made available in the browser beforehand, either by:
 * <ul>
 *     <li>registering it globally with {@code grapesjs.plugins.add(name, fn)}
 *     from a separate {@code @JavaScript}-loaded script, or</li>
 *     <li>vendoring a UMD build of the plugin the same way GrapesJS itself is
 *     vendored (see {@code grapesjs_addon/} for the pattern).</li>
 * </ul>
 */
public class GrapesJsPlugin implements Serializable {

    private final String name;
    private final Map<String, Object> options = new HashMap<>();

    /**
     * Creates a new plugin reference.
     *
     * @param name the plugin's registered name
     */
    public GrapesJsPlugin(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /**
     * Returns the plugin's registered name.
     *
     * @return the plugin name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a plugin option.
     *
     * @param key the option key
     * @param value the option value
     * @return this instance, for chaining
     */
    public GrapesJsPlugin option(String key, Object value) {
        options.put(key, value);
        return this;
    }

    /**
     * Returns the plugin's options.
     *
     * @return the plugin options
     */
    public Map<String, Object> getOptions() {
        return options;
    }
}
