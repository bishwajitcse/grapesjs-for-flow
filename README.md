# GrapesJS for Flow

A [Vaadin Flow](https://vaadin.com/flow) component embedding the [GrapesJS](https://grapesjs.com) visual web/page editor — build the editor UI in pure Java, let end users drag-and-drop their own pages.

`GrapesJsEditor` is a `CustomField<String>` whose value is the editor's HTML, so it plugs directly into `Binder` like any other Vaadin field. CSS and the full GrapesJS project (components, styles, pages, symbols) are exposed separately, since they serve different purposes than the field value.

## Contents

- [Requirements](#requirements)
- [Adding this addon to your project](#adding-this-addon-to-your-project)
- [Quick start](#quick-start)
- [Features](#features)
- [Content: HTML, CSS, and project data](#content-html-css-and-project-data)
- [Blocks](#blocks)
- [Devices](#devices)
- [Assets](#assets)
- [Commands](#commands)
- [Toolbar buttons](#toolbar-buttons)
- [Events](#events)
- [Value change behavior](#value-change-behavior)
- [Enabled vs. read-only](#enabled-vs-read-only)
- [Theming](#theming)
- [Raw configuration escape hatches](#raw-configuration-escape-hatches)
- [Security](#security)
- [Running the demo](#running-the-demo)
- [License](#license)

## Requirements

- Java 17+ (the project's `maven.compiler.source`/`target`)
- Vaadin Flow 24.x
- The default Lumo theme active in your application (see [Theming](#theming) — the editor's own styling depends on Lumo's design tokens)

## Adding this addon to your project

This addon isn't published to a public Maven repository yet. To use it, build and install it into your local Maven repository, then depend on it like any other artifact:

```bash
git clone <this-repository>
cd grapesjs-for-flow
./mvnw install -DskipTests
```

Then, in your own Vaadin project's `pom.xml`:

```xml
<dependency>
    <groupId>com.lausntech</groupId>
    <artifactId>grapesjs-for-flow</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

The addon depends on `vaadin-core` with `provided` scope, so it uses whatever Vaadin version your own project already supplies (must be 24.x). No other runtime dependencies are pulled in — GrapesJS itself is vendored inside the addon's jar, not fetched separately.

If you later publish this to Maven Central, GitHub Packages, or another repository, the same coordinates apply — just add the appropriate `<repository>` entry.

## Quick start

```java
import com.lausntech.grapesjs.GrapesJsEditor;

GrapesJsEditor editor = new GrapesJsEditor();
editor.setSizeFull();
editor.setValue("<h1>Hello, world!</h1>");
add(editor);
```

## Features

- **HTML field** — `GrapesJsEditor` is a `CustomField<String>`; works with `Binder`, `addValueChangeListener`, `setReadOnly`, etc.
- **Separate CSS & project data access** — read/write CSS independently of HTML, and save/restore the full GrapesJS project (component tree, styles, pages, symbols) as JSON for later editing.
- **CSS-inlined HTML export** — `getFullHtml()` returns HTML with all applicable CSS inlined as `style` attributes, for reuse anywhere that only keeps a plain HTML fragment (pasting into another rich text editor such as TinyMCE, email, a CMS field that strips `<style>` tags).
- **Blocks** — register custom HTML blocks individually, or in bulk from a built-in preset library of 40+ modern-website building blocks (navbar, hero, pricing, testimonials, team, stats, gallery, forms, and more) organized by category.
- **Devices** — the standard desktop/tablet/mobile breakpoints out of the box, plus custom devices.
- **Assets** — register images/media for GrapesJS's asset picker.
- **Commands** — undo/redo, fullscreen, and any GrapesJS command by id (`core:preview`, `core:open-code`, ...).
- **Toolbar buttons** — register custom buttons in GrapesJS's own toolbar that fire a server-side event when clicked, e.g. to trigger a "Publish" action.
- **Events** — ready, value-change (with configurable trigger mode and debounce), component selection, focus/blur, custom toolbar button clicks.
- **Enabled vs. read-only** — gray out the whole component, or keep it interactive while only locking canvas editing.
- **Theme variant** — `NO_BORDER` to drop the default border/box-shadow when your layout already provides one.
- **Raw configuration escape hatches** — `configure(key, value)` for simple GrapesJS init options, `setConfig(json)` for anything more complex.
- **Lumo-native chrome** — the editor's own panel layout (toolbar, blocks, layers, style manager, traits) is rebuilt with Vaadin's Lumo design tokens instead of GrapesJS's default skin, so it looks native inside a Vaadin application rather than like an embedded third-party widget.

## Content: HTML, CSS, and project data

```java
// HTML is the field value.
editor.setValue("<h1>Hello</h1>");
editor.addValueChangeListener(e -> save(e.getValue()));

// CSS is separate.
editor.setCss(".hero { color: blue; }");
editor.getCss().thenAccept(css -> ...);

// Full round-trip project data (preferred for persisting/restoring work-in-progress).
editor.getProjectData().thenAccept(json -> database.save(json));
editor.loadProjectData(previouslySavedJson);

// Portable, CSS-inlined export for use outside the editor (e.g. TinyMCE).
editor.getFullHtml().thenAccept(html -> tinyMceField.setValue(html));
```

`getHtml()`, `getCss()`, `getFullHtml()`, `getProjectData()` and `getDevice()` are all asynchronous (`CompletableFuture<String>`): the authoritative state lives in the browser, so reading it requires a round trip. If you only need the last value pushed to the server via a value-change event, `getValue()` returns immediately instead.

Use `getProjectData()`/`loadProjectData()` — not hand-assembled HTML/CSS — to persist and restore in-progress work, since the project JSON captures GrapesJS-internal state that plain HTML/CSS export does not.

## Blocks

```java
editor.addBlock("cta", "Call to action", "<a href=\"#\" class=\"cta\">Click me</a>");

editor.addBlocks(Map.of(
        "Sections", List.of("navbar", "hero", "feature-section", "pricing", "footer"),
        "Layout", List.of("two-columns", "three-columns", "container"),
        "Basic", List.of("text", "heading", "image", "button", "link")));
```

Built-in presets (see `GrapesJsBlockPresets.knownTypes()`):

- **Sections** — `navbar`, `hero`, `about`, `services`, `section`, `feature-section`, `pricing`, `testimonials`, `team`, `stats`, `gallery`, `blog-grid`, `cta`, `newsletter`, `contact`, `logos`, `video-section`, `timeline`, `faq`, `footer`
- **Layout** — `container`, `two-columns`, `three-columns`, `four-columns`
- **Components** — `card`, `pricing-card`, `testimonial-card`, `icon-box`, `image-text`, `button`, `link`, `badge`, `avatar`, `progress-bar`, `rating`, `social-icons`, `alert`, `quote`, `tabs`, `accordion`, `divider`, `spacer`
- **Basic** — `text`, `heading`, `image`, `list`, `table`
- **Forms** — `input`, `textarea`, `select`, `checkbox`

## Devices

```java
editor.addDevice(new GrapesJsDevice("Wide", "1440px"));
editor.setDevice(GrapesJsDevice.MOBILE_PORTRAIT);
```

Every editor instance registers `GrapesJsDevice.DESKTOP`, `TABLET`, `MOBILE_LANDSCAPE` and `MOBILE_PORTRAIT` by default.

## Assets

```java
editor.addAsset("https://example.com/logo.png");
```

The addon doesn't couple itself to any upload mechanism — upload files through your own means (Vaadin `Upload` to a REST endpoint or object storage, etc.) and register the resulting URL.

## Commands

```java
editor.undo();
editor.redo();
editor.setFullscreen(true);
editor.runCommand("core:open-code");
```

Any [GrapesJS command id](https://grapesjs.com/docs/api/commands.html) can be run/stopped via `runCommand`/`stopCommand`.

```java
editor.setToolbarVisible(false);
```

`setToolbarVisible(false)` hides GrapesJS's own internal toolbar entirely (native Undo/Redo/Preview/Fullscreen/View code buttons, the device selector, and any buttons registered via `addToolbarButton`) — useful if your application drives all of these actions from its own UI instead. The canvas and side panels reflow to fill the freed space.

## Toolbar buttons

```java
editor.addToolbarButton("publish", "Publish");
editor.addToolbarButtonClickListener(e -> {
    if ("publish".equals(e.getButtonId())) {
        publishPage(editor);
    }
});
```

`addToolbarButton(id, label)` registers a button in GrapesJS's own toolbar, next to its built-in Undo/Redo/Preview/Fullscreen buttons — `label` can be plain text or a small HTML/SVG snippet, the same as GrapesJS's own built-in buttons use. A single `addToolbarButtonClickListener` handles clicks from every registered button; the event's `getButtonId()` tells them apart. `removeToolbarButton(id)` unregisters one.

## Events

```java
editor.addReadyListener(e -> log("editor ready"));
editor.addSelectListener(e -> log("selected: " + e.getTagName()));
editor.addValueChangeListener(e -> log("changed: " + e.getValue()));
editor.addFocusListener(e -> log("focused"));
editor.addBlurListener(e -> log("blurred"));
editor.addToolbarButtonClickListener(e -> log("clicked: " + e.getButtonId()));
```

## Value change behavior

```java
editor.setValueChangeMode(ValueChangeMode.TIMEOUT);
editor.setDebounceTimeout(500);
```

`ValueChangeMode.CHANGE` (default) fires on every project update; `TIMEOUT` does the same but debounced; `BLUR` fires only when the canvas loses focus.

## Enabled vs. read-only

```java
editor.setEnabled(false);   // grays out the whole component
editor.setReadOnly(true);   // keeps it interactive, but the canvas can't be edited
```

## Theming

The editor's chrome is styled with Vaadin's Lumo design tokens (`--lumo-*`), which are available by default in any Vaadin application — no extra theme setup is required.

Use `GrapesJsEditorVariant.NO_BORDER` to drop the default border/box-shadow when your surrounding layout already provides one:

```java
editor.addThemeVariants(GrapesJsEditorVariant.NO_BORDER);
```

## Raw configuration escape hatches

```java
editor.configure("height", "100%");
editor.setConfig("{ storageManager: false }");
```

`configure(key, value)` merges simple entries into the object passed to `grapesjs.init(...)`. For anything that needs a nested object or isn't otherwise representable in Java, `setConfig(String)` accepts a raw JSON/JavaScript object literal — see [Security](#security) for why this must never contain end-user input. Both must be called before the component is attached to a UI; calling them afterward throws `AlreadyInitializedException`.

## Security

- **GrapesJS's output is not sanitized.** The editor lets end users freely shape HTML/CSS, including arbitrary attributes and (depending on configuration) embedded content. If the resulting HTML/CSS will be rendered to *other* users (e.g. published as a page, sent in an email), your application is responsible for sanitizing or validating it server-side before doing so — this addon does not do that for you.
- **`setConfig(String)` is not sanitized either.** The string is spliced directly into a `<script>` block evaluated in the browser. Only pass configuration your own application controls — never end-user input — and prefer plain JSON over executable JavaScript whenever possible.
- Content loaded via `setHtml`/`setValue`/`setCss` is rendered as-is inside the editor's canvas (an iframe). Treat it the same as any other HTML you'd inject into a page: don't feed it untrusted input without validating it first.

## Running the demo

A small demo/verification app lives under `src/test` (exercising the Java ↔ JavaScript bridge, not part of the published artifact):

```bash
./mvnw org.codehaus.mojo:exec-maven-plugin:java \
    -Dexec.mainClass=com.lausntech.grapesjs.demo.Application \
    -Dexec.classpathScope=test
```

Then open http://localhost:8081. (Or run `com.lausntech.grapesjs.demo.Application#main` directly from your IDE.)

## License

[Apache License 2.0](LICENSE.md).
