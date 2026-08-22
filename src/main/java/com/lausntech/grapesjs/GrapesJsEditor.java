package com.lausntech.grapesjs;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.BlurNotifier.BlurEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.FocusNotifier.FocusEvent;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Style.Overflow;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;

/**
 * A Vaadin Flow component embedding the <a href="https://grapesjs.com">GrapesJS</a>
 * visual web/page editor.
 * <p>
 * The component extends {@link CustomField} of {@link String}; the field
 * value represents the editor's <strong>HTML</strong> content, which makes
 * the component directly usable with {@link com.vaadin.flow.data.binder.Binder}.
 * CSS and the full GrapesJS project (components + styles + pages, suitable
 * for re-loading into the editor later) are exposed through the separate,
 * asynchronous {@link #getCss()}, {@link #getProjectData()} and
 * {@link #loadProjectData(String)} APIs &mdash; they are deliberately kept
 * apart from the field value, since HTML, CSS and project JSON serve
 * different purposes and none of them should be reconstructed from another.
 * <p>
 * <b>Theming:</b> the component's own chrome ({@code grapesjs.css}) is
 * styled with Vaadin's Lumo theme design tokens ({@code --lumo-*}), which
 * are available by default in any Vaadin application.
 * <p>
 * <b>Security:</b> GrapesJS produces HTML/CSS that end users can freely
 * shape, including arbitrary attributes and (depending on configuration)
 * embedded content. This addon does not sanitize editor output in any way.
 * If the resulting HTML will be rendered to other users (e.g. published as a
 * page), the application is responsible for sanitizing or validating it
 * server-side before doing so.
 */
@JavaScript("context://frontend/grapesjsConnector.js")
@StyleSheet("context://frontend/grapesjs.css")
public class GrapesJsEditor extends CustomField<String> implements HasSize, HasThemeVariant<GrapesJsEditorVariant> {

    /**
     * The height the editor is given by default, unless overridden via
     * {@link #setHeight(String)} or {@link #setSizeFull()}.
     */
    public static final String DEFAULT_HEIGHT = "700px";

    private final Element editorContainer = new Element("div");
    private final DomListenerRegistration changeListenerRegistration;

    private String id;
    private boolean initialContentSent;
    private boolean connectorInitialized;

    private String currentValue = "";
    private String currentCss = "";
    private String rawConfig;
    private final LinkedHashMap<String, Object> config = new LinkedHashMap<>();

    private int debounceTimeout = 0;
    private ValueChangeMode valueChangeMode = ValueChangeMode.CHANGE;
    private boolean enabled = true;
    private boolean readOnly = false;
    private boolean toolbarVisible = true;

    /**
     * Creates a new, empty editor. Use {@link #configure(String, boolean)}
     * and the other {@code configure*}/{@code add*} methods to set up the
     * editor before attaching it to a UI.
     */
    public GrapesJsEditor() {
        super("");
        addClassName("grapesjs-editor");
        setHeight(DEFAULT_HEIGHT);
        getStyle().setOverflow(Overflow.HIDDEN);

        editorContainer.getStyle().set("height", "100%");
        editorContainer.getStyle().set("width", "100%");
        getElement().appendChild(editorContainer);

        changeListenerRegistration = getElement().addEventListener("gjs-change", event -> {
            if (!event.getEventData().hasKey("event.htmlString")) {
                return;
            }
            String htmlString = event.getEventData().get("event.htmlString").asString();
            currentValue = htmlString;
            setModelValue(htmlString, true);
        });
        changeListenerRegistration.addEventData("event.htmlString");
        changeListenerRegistration.debounce(debounceTimeout);
    }

    /**
     * Creates a new editor with the given initial HTML content.
     *
     * @param initialHtml the initial HTML value
     */
    public GrapesJsEditor(String initialHtml) {
        this();
        setValue(initialHtml);
    }

    /**
     * Creates a new editor with the given value-change listener attached.
     *
     * @param valueChangeListener value change listener
     */
    public GrapesJsEditor(
            ValueChangeListener<? super ComponentValueChangeEvent<CustomField<String>, String>> valueChangeListener) {
        this();
        addValueChangeListener(valueChangeListener);
    }

    /**
     * Creates a new editor with the given initial HTML content and
     * value-change listener attached.
     *
     * @param initialHtml the initial HTML value
     * @param valueChangeListener value change listener
     */
    public GrapesJsEditor(String initialHtml,
            ValueChangeListener<? super ComponentValueChangeEvent<CustomField<String>, String>> valueChangeListener) {
        this();
        setValue(initialHtml);
        addValueChangeListener(valueChangeListener);
    }

    @Override
    protected String generateModelValue() {
        return currentValue;
    }

    @Override
    protected void setPresentationValue(String html) {
        this.currentValue = html == null ? "" : html;
        if (initialContentSent) {
            runBeforeClientResponse(
                    ui -> getElement().callJsFunction("$connector.setEditorContent", currentValue));
        }
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (id == null) {
            id = UUID.randomUUID().toString();
            editorContainer.setAttribute("id", id);
        }
        if (!attachEvent.isInitialAttach()) {
            initialContentSent = true;
        }
        super.onAttach(attachEvent);
        if (attachEvent.isInitialAttach()) {
            injectEditorScript();
        }
        initConnector();
        saveOnClose();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (isVisible()) {
            detachEvent.getUI().getPage().executeJs(
                    "window.Vaadin.Flow.grapesjsConnector.destroy($0);", getElement());
        }
        super.onDetach(detachEvent);
        initialContentSent = false;
        connectorInitialized = false;
    }

    private void initConnector() {
        runBeforeClientResponse(ui -> {
            String rawConfigJs = rawConfig != null ? rawConfig : "{}";
            String optionsJs = toJson(config);
            ui.getPage().executeJs(
                    "const rawconfig = " + rawConfigJs + ";\n"
                            + "const options = " + optionsJs + ";\n"
                            + "window.Vaadin.Flow.grapesjsConnector.initLazy(rawconfig, $0, $1, options, $2, $3, $4)",
                    getElement(), editorContainer, currentValue, currentCss, (enabled && !readOnly))
                    .then(ignore -> initialContentSent = true);
            connectorInitialized = true;
        });
    }

    /**
     * Serializes a flat map of JSON-primitive values ({@link String},
     * {@link Boolean}, {@link Number}) to a JSON object literal, for
     * splicing directly into JavaScript evaluated via {@link
     * com.vaadin.flow.component.page.Page#executeJs}. This is used instead
     * of passing the map itself as an {@code executeJs} argument, since
     * Flow's JSON codec does not support encoding arbitrary {@link Map}
     * arguments.
     */
    private static String toJson(Map<String, ?> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append(jsonString(entry.getKey())).append(':').append(jsonValue(entry.getValue()));
        }
        return json.append('}').toString();
    }

    private static String jsonValue(Object value) {
        if (value instanceof String string) {
            return jsonString(string);
        } else if (value instanceof Boolean || value instanceof Number) {
            return String.valueOf(value);
        }
        return "null";
    }

    private static String jsonString(String value) {
        StringBuilder json = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (c < 0x20) {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
        return json.append('"').toString();
    }

    private void saveOnClose() {
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.saveOnClose"));
    }

    /**
     * Injects the vendored GrapesJS library into the page. Override this
     * with an empty implementation to supply GrapesJS via other means (e.g.
     * a self-hosted build at a different path).
     */
    protected void injectEditorScript() {
        UI ui = getUI().orElseThrow();
        ui.getPage().addJavaScript("context://frontend/grapesjs_addon/grapesjs/grapes.min.js");
        ui.getPage().addStyleSheet("context://frontend/grapesjs_addon/grapesjs/grapes.min.css");
    }

    void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(this, context -> command.accept(ui)));
    }

    private void checkAlreadyInitialized() {
        if (connectorInitialized) {
            throw new AlreadyInitializedException();
        }
    }

    // ------------------------------------------------------------------
    // Generic configuration (applied before attach)
    // ------------------------------------------------------------------

    /**
     * Sets a raw GrapesJS init-configuration entry, e.g.
     * {@code configure("height", "100%")}. Values are merged into the
     * object passed to {@code grapesjs.init(...)}; keys that require nested
     * objects (e.g. {@code blockManager}) are better set with the dedicated
     * {@code add*}/{@code configure*} APIs, or via {@link #setConfig(String)}
     * for anything not otherwise representable in Java.
     *
     * @param configurationKey the GrapesJS init option key
     * @param value the value
     * @return this instance, for chaining
     */
    public GrapesJsEditor configure(String configurationKey, String value) {
        checkAlreadyInitialized();
        config.put(configurationKey, value);
        return this;
    }

    /**
     * Boolean-valued overload of {@link #configure(String, String)}.
     *
     * @param configurationKey the GrapesJS init option key
     * @param value the value
     * @return this instance, for chaining
     */
    public GrapesJsEditor configure(String configurationKey, boolean value) {
        checkAlreadyInitialized();
        config.put(configurationKey, value);
        return this;
    }

    /**
     * Numeric-valued overload of {@link #configure(String, String)}.
     *
     * @param configurationKey the GrapesJS init option key
     * @param value the value
     * @return this instance, for chaining
     */
    public GrapesJsEditor configure(String configurationKey, double value) {
        checkAlreadyInitialized();
        config.put(configurationKey, value);
        return this;
    }

    /**
     * Sets the base GrapesJS configuration object as raw JSON/JavaScript, for
     * options that cannot reasonably be represented via {@link #configure}.
     * <p>
     * <b>Caution:</b> this string is spliced directly into a
     * {@code <script>} evaluated in the browser as
     * {@code const rawconfig = <jsConfig>;}. It is not sanitized. Only pass
     * configuration your own application controls &mdash; never end-user
     * input &mdash; and prefer plain JSON over executable JavaScript
     * whenever possible.
     *
     * @param jsConfig a JSON (or JavaScript object literal) string
     */
    public void setConfig(String jsConfig) {
        checkAlreadyInitialized();
        this.rawConfig = jsConfig;
    }

    // ------------------------------------------------------------------
    // HTML / CSS
    // ------------------------------------------------------------------

    /**
     * Sets the editor's HTML content. Equivalent to {@code setValue(html)}.
     *
     * @param html the HTML to load into the canvas
     */
    public void setHtml(String html) {
        setValue(html == null ? "" : html);
    }

    /**
     * Asynchronously reads the HTML currently built in the canvas via
     * GrapesJS's own {@code editor.getHtml()}.
     * <p>
     * This is asynchronous because the authoritative state lives in the
     * browser: the server cannot synchronously read live browser state
     * across the network boundary. If you only need the last value that was
     * pushed to the server via a value-change event, use {@link #getValue()}
     * instead, which returns immediately.
     *
     * @return a future resolving to the current HTML
     */
    public CompletableFuture<String> getHtml() {
        return callAndGetString("$connector.getHtml");
    }

    /**
     * Sets the editor's CSS content.
     *
     * @param css the CSS to load
     */
    public void setCss(String css) {
        this.currentCss = css == null ? "" : css;
        if (initialContentSent) {
            runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.setCss", currentCss));
        }
    }

    /**
     * Asynchronously reads the CSS currently built by the editor via
     * {@code editor.getCss()}. See {@link #getHtml()} for why this is
     * asynchronous.
     *
     * @return a future resolving to the current CSS
     */
    public CompletableFuture<String> getCss() {
        return callAndGetString("$connector.getCss");
    }

    /**
     * Asynchronously reads the editor's HTML with all applicable CSS
     * inlined directly onto elements as {@code style} attributes &mdash;
     * the standard "CSS inlining" technique for portable HTML that has to
     * render correctly without its stylesheet along for the ride. Useful
     * for reusing the editor's output somewhere that only keeps a plain
     * HTML fragment, such as pasting into another rich text editor (e.g.
     * TinyMCE via {@code tinymce.activeEditor.setContent(html)}), sending
     * in an email, or storing in a CMS field that strips {@code <style>}
     * tags.
     * <p>
     * Rules that genuinely can't be expressed as an inline style
     * ({@code :hover}/{@code :focus}/other pseudo-classes, {@code @media},
     * {@code @keyframes}, {@code @font-face}) are kept verbatim in a small
     * {@code <style>} block prepended to the result instead. The base look
     * of the content is unaffected if that block gets stripped by the
     * destination &mdash; only those extra states/behaviors are lost.
     * <p>
     * Unlike a full CSS engine, rules are applied in source order rather
     * than by CSS specificity. GrapesJS's own generated CSS is normally
     * conflict-free in practice (each component is styled via its own
     * auto-generated id or class), so this rarely matters, but hand-written
     * CSS loaded via {@link #setCss(String)} that relies on specificity to
     * override itself may not inline exactly as it renders in the canvas.
     * See {@link #getHtml()} for why this is asynchronous.
     *
     * @return a future resolving to the HTML with its CSS inlined
     */
    public CompletableFuture<String> getFullHtml() {
        return callAndGetString("$connector.getFullHtml");
    }

    // ------------------------------------------------------------------
    // Project data
    // ------------------------------------------------------------------

    /**
     * Loads a GrapesJS project (as previously returned by
     * {@link #getProjectData()}) into the editor, replacing its current
     * content. Use this &mdash; not hand-assembled HTML/CSS &mdash; to
     * restore a previously saved project, since the project JSON captures
     * GrapesJS-internal state (component tree, style rules, pages, symbols)
     * that plain HTML/CSS export does not.
     *
     * @param projectData a JSON string as returned by {@link #getProjectData()}
     */
    public void loadProjectData(String projectData) {
        Objects.requireNonNull(projectData, "projectData");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.loadProjectData", projectData));
    }

    /**
     * Asynchronously retrieves the editor's full project data (via
     * {@code editor.getProjectData()}) as a JSON string, suitable for
     * persisting to a database and later restoring with
     * {@link #loadProjectData(String)}.
     *
     * @return a future resolving to the project data as a JSON string
     */
    public CompletableFuture<String> getProjectData() {
        return callAndGetString("$connector.getProjectData");
    }

    /**
     * Clears all content (components and styles) from the canvas.
     */
    public void clear() {
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.clear"));
        currentValue = "";
        currentCss = "";
    }

    // ------------------------------------------------------------------
    // Value change behavior
    // ------------------------------------------------------------------

    /**
     * Defines when the editor pushes its HTML value to the server as a
     * value-change event: on every project update ({@link ValueChangeMode#CHANGE}),
     * the same but debounced (see {@link #setDebounceTimeout(int)}), or only
     * when the canvas loses focus ({@link ValueChangeMode#BLUR}).
     *
     * @param mode the value change mode
     */
    public void setValueChangeMode(ValueChangeMode mode) {
        this.valueChangeMode = Objects.requireNonNull(mode);
        String clientMode = switch (mode) {
            case BLUR -> "blur";
            case TIMEOUT -> "timeout";
            case CHANGE -> "change";
        };
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.setMode", clientMode));
    }

    /**
     * Returns the currently configured value change mode.
     *
     * @return the value change mode
     */
    public ValueChangeMode getValueChangeMode() {
        return valueChangeMode;
    }

    /**
     * Sets the debounce timeout (in milliseconds) applied to value-change
     * events when {@link ValueChangeMode#TIMEOUT} is used. Has no effect for
     * the other modes.
     *
     * @param debounceTimeout the debounce timeout in milliseconds
     */
    public void setDebounceTimeout(int debounceTimeout) {
        this.debounceTimeout = Math.max(debounceTimeout, 0);
        changeListenerRegistration.debounce(this.debounceTimeout);
    }

    /**
     * Returns the currently configured debounce timeout, in milliseconds.
     *
     * @return the debounce timeout in milliseconds
     */
    public int getDebounceTimeout() {
        return debounceTimeout;
    }

    // ------------------------------------------------------------------
    // Focus / blur
    // ------------------------------------------------------------------

    @Override
    public void focus() {
        runBeforeClientResponse(ui -> getElement().executeJs("""
                const el = this;
                if (el.$connector && el.$connector.isInDialog()) {
                    setTimeout(() => el.$connector.focus(), 150);
                } else if (el.$connector) {
                    el.$connector.focus();
                }
                """));
    }

    @Override
    public void blur() {
        throw new UnsupportedOperationException("GrapesJS does not support programmatic blur of the canvas.");
    }

    @Override
    public Registration addFocusListener(ComponentEventListener<FocusEvent<CustomField<String>>> listener) {
        return getElement().addEventListener("gjs-focus",
                event -> listener.onComponentEvent(new FocusEvent<>(this, true)));
    }

    @Override
    public Registration addBlurListener(ComponentEventListener<BlurEvent<CustomField<String>>> listener) {
        return getElement().addEventListener("gjs-blur",
                event -> listener.onComponentEvent(new BlurEvent<>(this, true)));
    }

    /**
     * Registers a listener invoked once the GrapesJS editor instance has
     * finished initializing and rendered its initial content.
     *
     * @param listener the listener
     * @return a registration for removing the listener
     */
    public Registration addReadyListener(ComponentEventListener<GrapesJsReadyEvent> listener) {
        return addListener(GrapesJsReadyEvent.class, listener);
    }

    /**
     * Registers a listener invoked whenever the selected component in the
     * canvas changes.
     *
     * @param listener the listener
     * @return a registration for removing the listener
     */
    public Registration addSelectListener(ComponentEventListener<GrapesJsSelectEvent> listener) {
        return addListener(GrapesJsSelectEvent.class, listener);
    }

    // ------------------------------------------------------------------
    // Enabled / read-only
    // ------------------------------------------------------------------

    /**
     * Enables or disables the whole component, graying it out when disabled.
     * To keep the component fully interactive but only prevent editing the
     * canvas content, use {@link #setReadOnly(boolean)} instead.
     *
     * @param enabled {@code false} to disable the component
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        adjustEditableState();
    }

    /**
     * Sets whether the canvas can be edited. Unlike {@link #setEnabled(boolean)},
     * this does not gray out the whole component &mdash; it only prevents
     * editing, using GrapesJS's own {@code core:preview} command, which
     * renders the canvas without editing affordances (toolbars, outlines,
     * drag handles).
     *
     * @param readOnly {@code true} to prevent editing
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        getElement().setProperty("readonly", readOnly);
        adjustEditableState();
    }

    private void adjustEditableState() {
        boolean editable = enabled && !readOnly;
        super.setEnabled(editable);
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.setEnabled", editable));
    }

    // ------------------------------------------------------------------
    // Commands (undo/redo/fullscreen/preview/...)
    // ------------------------------------------------------------------

    /**
     * Undoes the last change, via GrapesJS's own undo/redo history.
     */
    public void undo() {
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.undo"));
    }

    /**
     * Redoes the last undone change, via GrapesJS's own undo/redo history.
     */
    public void redo() {
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.redo"));
    }

    /**
     * Runs a GrapesJS command by id, e.g. {@code "core:fullscreen"} or
     * {@code "core:open-code"}. See the
     * <a href="https://grapesjs.com/docs/api/commands.html">GrapesJS Commands API</a>
     * for the built-in command ids.
     *
     * @param commandId the command id
     */
    public void runCommand(String commandId) {
        Objects.requireNonNull(commandId, "commandId");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.runCommand", commandId));
    }

    /**
     * Stops a currently running/active GrapesJS command by id.
     *
     * @param commandId the command id
     */
    public void stopCommand(String commandId) {
        Objects.requireNonNull(commandId, "commandId");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.stopCommand", commandId));
    }

    /**
     * Enters or exits fullscreen mode, via GrapesJS's built-in
     * {@code core:fullscreen} command.
     *
     * @param fullscreen {@code true} to enter fullscreen, {@code false} to exit it
     */
    public void setFullscreen(boolean fullscreen) {
        if (fullscreen) {
            runCommand("core:fullscreen");
        } else {
            stopCommand("core:fullscreen");
        }
    }

    /**
     * Toggles fullscreen mode on or off, depending on its current state.
     */
    public void toggleFullscreen() {
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.toggleFullscreen"));
    }

    // ------------------------------------------------------------------
    // Toolbar visibility
    // ------------------------------------------------------------------

    /**
     * Shows or hides GrapesJS's own internal toolbar entirely &mdash; the
     * row with its native Undo/Redo, View components, Preview, Fullscreen
     * and View code buttons, the device selector, and any buttons
     * registered via {@link #addToolbarButton(String, String)}. Use this
     * if your application drives these actions from its own UI instead
     * (e.g. your own Save/Undo/Redo buttons next to the editor) and
     * doesn't want GrapesJS's built-in chrome duplicating them.
     *
     * @param visible {@code false} to hide the internal toolbar
     */
    public void setToolbarVisible(boolean visible) {
        this.toolbarVisible = visible;
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.setToolbarVisible", visible));
    }

    /**
     * Returns whether GrapesJS's own internal toolbar is currently visible.
     *
     * @return {@code true} if the internal toolbar is visible
     */
    public boolean isToolbarVisible() {
        return toolbarVisible;
    }

    // ------------------------------------------------------------------
    // Toolbar buttons
    // ------------------------------------------------------------------

    /**
     * Registers a custom button in GrapesJS's own toolbar, next to its
     * built-in Undo/Redo/Preview/Fullscreen buttons. Clicking it fires a
     * {@link GrapesJsToolbarButtonClickEvent} on the server; register a
     * listener with {@link #addToolbarButtonClickListener(ComponentEventListener)}
     * to run server-side logic (e.g. publish the page, call an API) when
     * it's clicked.
     *
     * @param id unique button id
     * @param label the label shown on the button; either plain text or a
     *        small HTML/SVG snippet, the same as GrapesJS's own built-in
     *        buttons use
     * @return this instance, for chaining
     */
    public GrapesJsEditor addToolbarButton(String id, String label) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(label, "label");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.addToolbarButton", id, label));
        return this;
    }

    /**
     * Removes a custom toolbar button previously registered with
     * {@link #addToolbarButton(String, String)}.
     *
     * @param id the button id
     */
    public void removeToolbarButton(String id) {
        Objects.requireNonNull(id, "id");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.removeToolbarButton", id));
    }

    /**
     * Registers a listener invoked whenever a custom toolbar button
     * (registered via {@link #addToolbarButton(String, String)}) is
     * clicked. The event carries the clicked button's id
     * ({@link GrapesJsToolbarButtonClickEvent#getButtonId()}), so a single
     * listener can handle multiple buttons.
     *
     * @param listener the listener
     * @return a registration for removing the listener
     */
    public Registration addToolbarButtonClickListener(ComponentEventListener<GrapesJsToolbarButtonClickEvent> listener) {
        return addListener(GrapesJsToolbarButtonClickEvent.class, listener);
    }

    // ------------------------------------------------------------------
    // Blocks
    // ------------------------------------------------------------------

    /**
     * Adds a block to the Block Manager panel. Unlike most configuration
     * methods, this may be called both before and after the editor has been
     * initialized: blocks queued before attach are included in the initial
     * configuration, and later calls use GrapesJS's own runtime
     * {@code BlockManager.add(...)} API.
     *
     * @param block the block definition
     */
    public void addBlock(GrapesJsBlock block) {
        Objects.requireNonNull(block, "block");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.addBlock", block.getId(),
                block.getLabel(), block.getCategory(), block.getContent(), block.getMedia()));
    }

    /**
     * Convenience overload of {@link #addBlock(GrapesJsBlock)}.
     *
     * @param id unique block id
     * @param label the label shown in the Block Manager panel
     * @param content the HTML content inserted into the canvas when the block is dropped
     */
    public void addBlock(String id, String label, String content) {
        addBlock(new GrapesJsBlock(id, label, content));
    }

    /**
     * Removes a block previously registered with {@link #addBlock(GrapesJsBlock)}
     * (or one of its overloads) from the Block Manager panel.
     *
     * @param id the block id
     */
    public void removeBlock(String id) {
        Objects.requireNonNull(id, "id");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.removeBlock", id));
    }

    /**
     * Registers a batch of built-in preset blocks (see {@link GrapesJsBlockPresets})
     * organized by category: each map key is a Block Manager category name,
     * and each value is a list of preset block type ids to register under
     * it, e.g.
     *
     * <pre>{@code
     * editor.addBlocks(Map.of(
     *         "Sections", List.of("hero", "feature-section"),
     *         "Layout", List.of("two-columns", "three-columns", "container"),
     *         "Basic", List.of("text", "heading", "image", "button", "link")));
     * }</pre>
     * <p>
     * Each preset's block id is namespaced with its category (so the same
     * preset type can be registered under multiple categories without an id
     * clash). For custom HTML that isn't one of the presets, use
     * {@link #addBlock(GrapesJsBlock)} directly.
     *
     * @param blocksByCategory category name -&gt; list of preset block type ids
     * @return this instance, for chaining
     * @throws IllegalArgumentException if a block type id isn't a known preset
     *         (see {@link GrapesJsBlockPresets#knownTypes()})
     */
    public GrapesJsEditor addBlocks(Map<String, List<String>> blocksByCategory) {
        Objects.requireNonNull(blocksByCategory, "blocksByCategory");
        blocksByCategory.forEach((category, types) -> {
            if (types == null) {
                return;
            }
            for (String type : types) {
                GrapesJsBlock preset = GrapesJsBlockPresets.get(type);
                addBlock(new GrapesJsBlock(slug(category) + "-" + type, preset.getLabel(), preset.getContent())
                        .setCategory(category).setMedia(preset.getMedia()));
            }
        });
        return this;
    }

    private static String slug(String value) {
        return value == null ? "default" : value.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
    }

    // ------------------------------------------------------------------
    // Devices
    // ------------------------------------------------------------------

    /**
     * Registers a custom device with the Device Manager, in addition to the
     * defaults ({@link GrapesJsDevice#DESKTOP}, {@link GrapesJsDevice#TABLET},
     * {@link GrapesJsDevice#MOBILE_LANDSCAPE}, {@link GrapesJsDevice#MOBILE_PORTRAIT})
     * registered with every editor instance. Switch to it with
     * {@link #setDevice(String)}.
     *
     * @param device the device definition
     */
    public void addDevice(GrapesJsDevice device) {
        Objects.requireNonNull(device, "device");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.addDevice", device.getName(),
                device.getWidth(), device.getWidthMedia()));
    }

    /**
     * Switches the canvas to the given device (by name), e.g.
     * {@link GrapesJsDevice#DESKTOP}, {@link GrapesJsDevice#TABLET} or
     * {@link GrapesJsDevice#MOBILE_PORTRAIT} (the defaults registered with
     * every editor instance), or a name previously passed to
     * {@link #addDevice(GrapesJsDevice)}.
     *
     * @param name the device name
     */
    public void setDevice(String name) {
        Objects.requireNonNull(name, "name");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.setDevice", name));
    }

    /**
     * Asynchronously reads the name of the currently active device.
     *
     * @return a future resolving to the active device name
     */
    public CompletableFuture<String> getDevice() {
        return callAndGetString("$connector.getDevice");
    }

    // ------------------------------------------------------------------
    // Assets
    // ------------------------------------------------------------------

    /**
     * Adds an asset (image, or other media) to the Asset Manager, making it
     * available for selection wherever GrapesJS offers an asset picker (e.g.
     * an image component's "select image" trait).
     *
     * @param asset the asset definition
     */
    public void addAsset(GrapesJsAsset asset) {
        Objects.requireNonNull(asset, "asset");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.addAsset", asset.getSrc(),
                asset.getType(), asset.getName()));
    }

    /**
     * Convenience overload of {@link #addAsset(GrapesJsAsset)} for a plain
     * image URL.
     *
     * @param url the asset URL
     */
    public void addAsset(String url) {
        addAsset(new GrapesJsAsset(url));
    }

    /**
     * Removes an asset previously registered with {@link #addAsset(GrapesJsAsset)}
     * (or one of its overloads) from the Asset Manager.
     *
     * @param url the asset URL, as passed to {@link #addAsset}
     */
    public void removeAsset(String url) {
        Objects.requireNonNull(url, "url");
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.removeAsset", url));
    }

    /**
     * Removes all assets from the Asset Manager.
     */
    public void clearAssets() {
        runBeforeClientResponse(ui -> getElement().callJsFunction("$connector.clearAssets"));
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private CompletableFuture<String> callAndGetString(String jsFunction, Serializable... params) {
        CompletableFuture<String> future = new CompletableFuture<>();
        runBeforeClientResponse(ui -> getElement().callJsFunction(jsFunction, params).then(String.class, result -> {
            future.complete(result);
        }, error -> future.completeExceptionally(new GrapesJsException(error))));
        return future;
    }
}
