/*
 * Frontend connector bridging GrapesJsEditor.java to a browser-side GrapesJS
 * editor instance.
 */

/**
 * Inlines `css` onto matching elements in `html` as `style="..."`
 * attributes. Rules that can't be expressed as an inline style
 * (pseudo-classes/elements, @media, @keyframes, @font-face, ...) are kept
 * verbatim in a small <style> block prepended to the result instead.
 */
function inlineHtmlCss(html, css) {
    const root = document.createElement('div');
    root.innerHTML = html || '';

    let sheet;
    try {
        sheet = new CSSStyleSheet();
        sheet.replaceSync(css || '');
    } catch (e) {
        const styleEl = document.createElement('style');
        styleEl.textContent = css || '';
        document.head.appendChild(styleEl);
        sheet = styleEl.sheet;
        document.head.removeChild(styleEl);
    }

    const nonInlinable = /::?(hover|focus(-within|-visible)?|active|visited|before|after|placeholder|first-child|last-child|nth-child|checked|disabled|not\()/i;
    const residualRules = [];

    (Array.from(sheet.cssRules || [])).forEach((rule) => {
        if (!(rule.selectorText && rule.style)) {
            residualRules.push(rule.cssText);
            return;
        }
        if (nonInlinable.test(rule.selectorText)) {
            residualRules.push(rule.cssText);
            return;
        }
        rule.selectorText.split(',').forEach((rawSelector) => {
            const selector = rawSelector.trim();
            if (!selector) {
                return;
            }
            let matches;
            try {
                matches = root.querySelectorAll(selector);
            } catch (e) {
                residualRules.push(selector + ' { ' + rule.style.cssText + ' }');
                return;
            }
            matches.forEach((el) => {
                // Merge as raw cssText, not by copying rule.style's
                // individual longhand properties one by one: the CSSOM
                // expands shorthands (border, background, font, ...) into
                // their longhands as soon as rule.style is indexed/read
                // per-property, and any longhand whose value depends on a
                // custom property (e.g. `border: 1.5px solid var(--ink)`)
                // comes back empty from that expansion - silently dropping
                // the whole declaration. cssText round-trips such values
                // (and shorthands generally) untouched.
                const existing = el.style.cssText;
                el.style.cssText = (existing ? existing.replace(/;?\s*$/, '; ') : '') + rule.style.cssText;
            });
        });
    });

    const inlinedHtml = root.innerHTML;
    return residualRules.length ? '<style>' + residualRules.join('\n') + '</style>' + inlinedHtml : inlinedHtml;
}

/**
 * Expands the `background` and `font` shorthand properties (when present)
 * in each element's inline `style="..."` attribute into the specific
 * longhand properties our Style Manager sectors are configured with
 * (background-color; font-family, font-size, font-weight, line-height).
 */
function expandStyleShorthand(html) {
    const root = document.createElement('div');
    root.innerHTML = html || '';
    const probe = document.createElement('div');

    root.querySelectorAll('[style]').forEach((el) => {
        const decls = el.style;

        const background = decls.getPropertyValue('background');
        if (background) {
            probe.style.cssText = 'background:' + background;
            // A layer without an explicit color leaves the background-color
            // sub-property unset, which some engines serialize back as the
            // literal keyword "initial" rather than an empty string.
            const bgColor = probe.style.backgroundColor;
            const hasColor = !!bgColor && bgColor !== 'initial';
            const bgImage = probe.style.backgroundImage;
            const hasImage = !!bgImage && bgImage !== 'none' && bgImage !== 'initial';
            decls.removeProperty('background');
            if (hasColor) {
                decls.setProperty('background-color', bgColor);
            }
            if (hasImage) {
                // Layered gradients/images have no single color component
                // (hasColor above is false for them): keep them as
                // background-image rather than collapsing into
                // background-color, whose value syntax can't hold a
                // gradient and would silently drop the declaration.
                decls.setProperty('background-image', bgImage);
                if (probe.style.backgroundRepeat) {
                    decls.setProperty('background-repeat', probe.style.backgroundRepeat);
                }
                if (probe.style.backgroundPosition) {
                    decls.setProperty('background-position', probe.style.backgroundPosition);
                }
                if (probe.style.backgroundSize) {
                    decls.setProperty('background-size', probe.style.backgroundSize);
                }
                // background-clip is itself a component of the `background`
                // shorthand (e.g. the gradient-text trick relies on
                // `background-clip: text`/`-webkit-background-clip: text`,
                // which browsers fold into the shorthand's serialized
                // value). Preserve it explicitly, or removeProperty('background')
                // above silently drops it.
                if (probe.style.backgroundClip && probe.style.backgroundClip !== 'initial') {
                    decls.setProperty('background-clip', probe.style.backgroundClip);
                    decls.setProperty('-webkit-background-clip', probe.style.backgroundClip);
                }
            } else if (!hasColor) {
                // Couldn't decompose (e.g. a var()-based shorthand): keep
                // the original shorthand rather than losing it.
                decls.setProperty('background', background);
            }
        }

        const font = decls.getPropertyValue('font');
        if (font) {
            decls.removeProperty('font');
            probe.style.cssText = 'font:' + font;
            if (probe.style.fontFamily) {
                decls.setProperty('font-family', probe.style.fontFamily);
            }
            if (probe.style.fontSize) {
                decls.setProperty('font-size', probe.style.fontSize);
            }
            if (probe.style.fontWeight) {
                decls.setProperty('font-weight', probe.style.fontWeight);
            }
            if (probe.style.lineHeight && probe.style.lineHeight !== 'normal') {
                decls.setProperty('line-height', probe.style.lineHeight);
            }
        }
    });

    return root.innerHTML;
}

/**
 * Pulls any top-level `<style>` tag(s) out of `html`, e.g. when loading an
 * existing/legacy page that carries its custom CSS embedded in its markup
 * rather than as a separate stylesheet. Without this, GrapesJS's own HTML
 * importer silently strips `<style>` tags out of the component tree and
 * merges their rules straight into its own managed stylesheet
 * (`editor.getCss()`) - leaving no trace in the page-level Custom CSS panel,
 * even though that's exactly the kind of hand-written CSS (media queries,
 * ad-hoc classes) that panel is for.
 *
 * @returns {{html: string, css: string}} `html` with the `<style>` tag(s)
 *   removed, and `css` the concatenation of their contents (empty string if
 *   none were found).
 */
function extractStyleTags(html) {
    const root = document.createElement('div');
    root.innerHTML = html || '';
    const styleEls = Array.from(root.querySelectorAll('style'));
    if (!styleEls.length) {
        return { html: html || '', css: '' };
    }
    const css = styleEls.map((el) => el.textContent).join('\n');
    styleEls.forEach((el) => el.remove());
    return { html: root.innerHTML, css: css };
}

/**
 * Converts a component style object (as returned by Component#getStyle) into
 * the newline-separated "prop: value;" text shown in the inline styles
 * textarea.
 */
function styleObjectToText(styleObj) {
    return Object.keys(styleObj || {})
        .filter((prop) => styleObj[prop] !== undefined && styleObj[prop] !== '')
        .map((prop) => `${prop}: ${styleObj[prop]};`)
        .join('\n');
}

/**
 * Parses the inline styles textarea's "prop: value;" text back into a style
 * object suitable for Component#setStyle.
 */
function parseStyleText(text) {
    const result = {};
    (text || '').split(';').forEach((decl) => {
        const idx = decl.indexOf(':');
        if (idx === -1) {
            return;
        }
        const prop = decl.slice(0, idx).trim();
        const value = decl.slice(idx + 1).trim();
        if (prop && value) {
            result[prop] = value;
        }
    });
    return result;
}

const EMBED_PLACEHOLDER_HTML = '<div data-gjs-type="embed-placeholder" '
    + 'style="padding:24px;text-align:center;border:1px dashed #94a3b8;border-radius:8px;'
    + 'font:400 13px Arial,sans-serif;color:#64748b;background:#f8fafc">'
    + 'Double-click to paste embed code (e.g. a YouTube embed)</div>';

/**
 * Registers the `embed` component type used for pasting third-party embed
 * markup (YouTube/Vimeo/Google Maps iframes, Twitter/Instagram
 * blockquote+script snippets, ...) into the canvas. The block for it
 * (registered as the "embed" preset, see GrapesJsBlockPresets) drops a
 * placeholder; double-clicking the component opens a paste-code prompt
 * whose contents are parsed as real child components - the same path
 * `editor.setComponents()` already uses - so markup like a plain <iframe>
 * renders (and exports via getHtml()) exactly as pasted, rather than being
 * mangled by the canvas's rich-text editing.
 */
function registerEmbedComponent(editor) {
    editor.Components.addType('embed-placeholder', {
        isComponent: (el) => !!(el.getAttribute && el.getAttribute('data-gjs-type') === 'embed-placeholder'),
        model: {
            defaults: {
                draggable: false,
                droppable: false,
                selectable: false,
                hoverable: false,
                editable: false,
                removable: false,
                copyable: false,
                attributes: { 'data-gjs-type': 'embed-placeholder' },
            },
        },
    });

    editor.Components.addType('embed', {
        isComponent: (el) => !!(el.getAttribute && el.getAttribute('data-gjs-type') === 'embed'),
        model: {
            defaults: {
                draggable: true,
                droppable: false,
                editable: false,
                attributes: { 'data-gjs-type': 'embed' },
            },
        },
        view: {
            events: { dblclick: 'onDblClick' },
            onDblClick() {
                openEmbedPrompt(editor, this.model);
            },
        },
    });
}

function isEmbedPlaceholder(model) {
    const children = model.components();
    return children.length === 1 && children.at(0).get('type') === 'embed-placeholder';
}

function openEmbedPrompt(editor, model) {
    const currentCode = isEmbedPlaceholder(model)
        ? ''
        : model.components().map((child) => editor.getHtml({ component: child })).join('\n');

    const wrapper = document.createElement('div');
    wrapper.className = 'gjs-vaadin-embed-modal';
    wrapper.innerHTML = `
        <textarea style="width:100%;height:400px" class="gjs-vaadin-embed-textarea" placeholder="Paste embed code here, e.g. a YouTube iframe embed" spellcheck="false"></textarea>
        <div class="gjs-vaadin-embed-modal-actions">
            <button type="button" class="gjs-vaadin-embed-apply">Apply</button>
        </div>
    `;
    const textarea = wrapper.querySelector('.gjs-vaadin-embed-textarea');
    textarea.value = currentCode;

    wrapper.querySelector('.gjs-vaadin-embed-apply').addEventListener('click', () => {
        const code = textarea.value.trim();
        model.components(code || EMBED_PLACEHOLDER_HTML);
        editor.Modal.close();
    });

    editor.Modal.open({ title: 'Embed code', content: wrapper });
    setTimeout(() => textarea.focus(), 0);
}

const CUSTOM_HTML_PLACEHOLDER_HTML = '<div data-gjs-type="custom-html-placeholder" '
    + 'style="padding:24px;text-align:center;border:1px dashed #94a3b8;border-radius:8px;'
    + 'font:400 13px Arial,sans-serif;color:#64748b;background:#f8fafc">'
    + 'Double-click to edit custom HTML</div>';

/**
 * Registers the `custom-html` component type: a free-form HTML block whose
 * markup is authored directly by the user (as opposed to `embed`, which is
 * meant for pasting third-party embed snippets). Structurally identical to
 * the `embed` component above - the block drops a placeholder, double-click
 * opens a code prompt, and the entered markup is parsed into real child
 * components via `model.components()` so it renders/exports exactly as
 * written.
 */
function registerCustomHtmlComponent(editor) {
    editor.Components.addType('custom-html-placeholder', {
        isComponent: (el) => !!(el.getAttribute && el.getAttribute('data-gjs-type') === 'custom-html-placeholder'),
        model: {
            defaults: {
                draggable: false,
                droppable: false,
                selectable: false,
                hoverable: false,
                editable: false,
                removable: false,
                copyable: false,
                attributes: { 'data-gjs-type': 'custom-html-placeholder' },
            },
        },
    });

    editor.Components.addType('custom-html', {
        isComponent: (el) => !!(el.getAttribute && el.getAttribute('data-gjs-type') === 'custom-html'),
        model: {
            defaults: {
                draggable: true,
                droppable: false,
                editable: false,
                attributes: { 'data-gjs-type': 'custom-html' },
            },
        },
        view: {
            events: { dblclick: 'onDblClick' },
            onDblClick() {
                openCustomHtmlPrompt(editor, this.model);
            },
        },
    });
}

function isCustomHtmlPlaceholder(model) {
    const children = model.components();
    return children.length === 1 && children.at(0).get('type') === 'custom-html-placeholder';
}

function openCustomHtmlPrompt(editor, model) {
    const currentCode = isCustomHtmlPlaceholder(model)
        ? ''
        : model.components().map((child) => editor.getHtml({ component: child })).join('\n');

    const wrapper = document.createElement('div');
    wrapper.className = 'gjs-vaadin-embed-modal';
    wrapper.innerHTML = `
        <textarea style="width:100%;height:400px" class="gjs-vaadin-embed-textarea" placeholder="Write custom HTML here" spellcheck="false"></textarea>
        <div class="gjs-vaadin-embed-modal-actions">
            <button type="button" class="gjs-vaadin-embed-apply">Apply</button>
        </div>
    `;
    const textarea = wrapper.querySelector('.gjs-vaadin-embed-textarea');
    textarea.value = currentCode;

    wrapper.querySelector('.gjs-vaadin-embed-apply').addEventListener('click', () => {
        const code = textarea.value.trim();
        model.components(code || CUSTOM_HTML_PLACEHOLDER_HTML);
        editor.Modal.close();
    });

    editor.Modal.open({ title: 'Custom HTML', content: wrapper });
    setTimeout(() => textarea.focus(), 0);
}

const SVG_NS = 'http://www.w3.org/2000/svg';

// Tags with a dedicated component type below - excluded from the
// `svg-element` catch-all's isComponent match so that match order (which
// GrapesJS does not guarantee to prefer the most-recently-added type for
// every parse path - e.g. an appended root element resolves differently
// than its parsed children) can't cause the generic fallback to shadow a
// more specific type.
const SVG_SPECIFIC_TAGS = ['svg', 'path', 'circle', 'ellipse', 'rect', 'line', 'polygon', 'polyline', 'text'];

function isSvgElement(el) {
    return !!(el && el.namespaceURI === SVG_NS);
}

function svgTagName(el) {
    return el && el.tagName ? el.tagName.toLowerCase() : '';
}

/**
 * Registers component types for inline SVG markup (an `<svg>` dropped into
 * the canvas, e.g. via the "SVG Icon" block) so its shape elements expose
 * their SVG-specific attributes (path data, fill/stroke, coordinates, ...)
 * as traits in the Settings panel - GrapesJS's default component type
 * would let a user select/move these elements, but wouldn't surface
 * anything beyond the generic id/title HTML traits.
 *
 * Every type below extends GrapesJS's built-in `svg`/`svg-in` types rather
 * than defining a fresh one: those built-ins are the ones whose view
 * creates DOM nodes via `createElementNS` (SVG namespace). A plain custom
 * type falls back to the default component view's unnamespaced
 * `document.createElement`, which for an SVG child produces an inert,
 * non-rendering element (and mangles camelCase tags like `linearGradient`
 * to all-lowercase) - the element sits in the model/layers tree but draws
 * nothing on the canvas.
 */
function registerSvgComponents(editor) {
    const commonTraits = [
        'id',
        { type: 'color', name: 'fill' },
        { type: 'color', name: 'stroke' },
        { type: 'text', name: 'stroke-width', label: 'Stroke width' },
        { type: 'text', name: 'opacity' },
        { type: 'text', name: 'transform' },
    ];

    // `svg-in` (the built-in type these all extend for its namespaced view -
    // see the block comment above) defaults to selectable: false / hoverable:
    // false, since it's meant for static icon-style SVGs where only the
    // whole `<svg>` is picked as one unit. Editing individual shapes needs
    // both back on.
    const selectableDefaults = { selectable: true, hoverable: true };

    editor.Components.addType('svg-element', {
        extend: 'svg-in',
        isComponent: (el) => isSvgElement(el) && !SVG_SPECIFIC_TAGS.includes(svgTagName(el)),
        model: { defaults: { ...selectableDefaults, traits: commonTraits } },
    });

    editor.Components.addType('svg', {
        extend: 'svg',
        isComponent: (el) => isSvgElement(el) && svgTagName(el) === 'svg',
        model: {
            defaults: {
                traits: [
                    'id',
                    { type: 'text', name: 'viewBox' },
                    { type: 'text', name: 'width' },
                    { type: 'text', name: 'height' },
                    { type: 'text', name: 'preserveAspectRatio' },
                ],
            },
        },
    });

    editor.Components.addType('svg-path', {
        extend: 'svg-in',
        isComponent: (el) => isSvgElement(el) && svgTagName(el) === 'path',
        model: { defaults: { ...selectableDefaults, traits: [{ type: 'text', name: 'd', label: 'Path data' }, ...commonTraits] } },
    });

    editor.Components.addType('svg-circle', {
        extend: 'svg-in',
        isComponent: (el) => isSvgElement(el) && svgTagName(el) === 'circle',
        model: {
            defaults: {
                ...selectableDefaults,
                traits: [
                    { type: 'text', name: 'cx' },
                    { type: 'text', name: 'cy' },
                    { type: 'text', name: 'r' },
                    ...commonTraits,
                ],
            },
        },
    });

    editor.Components.addType('svg-ellipse', {
        extend: 'svg-in',
        isComponent: (el) => isSvgElement(el) && svgTagName(el) === 'ellipse',
        model: {
            defaults: {
                ...selectableDefaults,
                traits: [
                    { type: 'text', name: 'cx' },
                    { type: 'text', name: 'cy' },
                    { type: 'text', name: 'rx' },
                    { type: 'text', name: 'ry' },
                    ...commonTraits,
                ],
            },
        },
    });

    editor.Components.addType('svg-rect', {
        extend: 'svg-in',
        isComponent: (el) => isSvgElement(el) && svgTagName(el) === 'rect',
        model: {
            defaults: {
                ...selectableDefaults,
                traits: [
                    { type: 'text', name: 'x' },
                    { type: 'text', name: 'y' },
                    { type: 'text', name: 'width' },
                    { type: 'text', name: 'height' },
                    { type: 'text', name: 'rx' },
                    { type: 'text', name: 'ry' },
                    ...commonTraits,
                ],
            },
        },
    });

    editor.Components.addType('svg-line', {
        extend: 'svg-in',
        isComponent: (el) => isSvgElement(el) && svgTagName(el) === 'line',
        model: {
            defaults: {
                ...selectableDefaults,
                traits: [
                    { type: 'text', name: 'x1' },
                    { type: 'text', name: 'y1' },
                    { type: 'text', name: 'x2' },
                    { type: 'text', name: 'y2' },
                    ...commonTraits,
                ],
            },
        },
    });

    editor.Components.addType('svg-poly', {
        extend: 'svg-in',
        isComponent: (el) => isSvgElement(el) && ['polygon', 'polyline'].includes(svgTagName(el)),
        model: { defaults: { ...selectableDefaults, traits: [{ type: 'text', name: 'points' }, ...commonTraits] } },
    });

    editor.Components.addType('svg-text', {
        extend: 'svg-in',
        isComponent: (el) => isSvgElement(el) && svgTagName(el) === 'text',
        model: {
            defaults: {
                ...selectableDefaults,
                editable: true,
                traits: [
                    { type: 'text', name: 'x' },
                    { type: 'text', name: 'y' },
                    { type: 'text', name: 'font-size', label: 'Font size' },
                    { type: 'text', name: 'font-family', label: 'Font family' },
                    ...commonTraits,
                ],
            },
        },
        // `svg-in` (see the block comment above) only overrides element
        // creation; it doesn't carry the built-in `text` type's RTE wiring
        // (that lives on a separate view class, and a component can only
        // extend one), so double-click-to-edit has to be added by hand here
        // - otherwise every <text> keeps its parsed content forever.
        //
        // A native `contenteditable` on the element (the first approach
        // tried here) does NOT work: `contenteditable` is an HTML editing
        // feature only - browsers accept the attribute on an SVG element and
        // even focus it, but never wire up their text-insertion machinery
        // for it, so typed keystrokes silently go nowhere. A small prompt is
        // the reliable cross-browser option, and matches the pattern already
        // used by the `embed` component above for editing its content.
        view: {
            events: { dblclick: 'onEditStart' },
            onEditStart(ev) {
                ev.stopPropagation();
                openTextPrompt(editor, this.model);
            },
        },
    });
}

function openTextPrompt(editor, model) {
    const currentText = model.components().at(0)?.get('content') || '';

    const wrapper = document.createElement('div');
    wrapper.className = 'gjs-vaadin-embed-modal';
    wrapper.innerHTML = `
        <input type="text" class="gjs-vaadin-text-prompt-input" spellcheck="false" />
        <div class="gjs-vaadin-embed-modal-actions">
            <button type="button" class="gjs-vaadin-embed-apply">Apply</button>
        </div>
    `;
    const input = wrapper.querySelector('.gjs-vaadin-text-prompt-input');
    input.value = currentText;

    const apply = () => {
        model.components(input.value);
        editor.Modal.close();
    };
    wrapper.querySelector('.gjs-vaadin-embed-apply').addEventListener('click', apply);
    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            apply();
        }
    });

    editor.Modal.open({ title: 'Edit text', content: wrapper });
    setTimeout(() => {
        input.focus();
        input.select();
    }, 0);
}

window.Vaadin.Flow.grapesjsConnector = {

    /**
     * @param customConfig raw JS/JSON object spliced in from GrapesJsEditor#setConfig
     * @param c the GrapesJsEditor's own element (holds $connector)
     * @param ta the inner container element GrapesJS renders into
     * @param options the Java-side `config` map (generic configure() entries)
     * @param initialHtml initial HTML content
     * @param initialCss initial CSS content
     * @param enabled whether the canvas should start out editable
     * @param initialCustomCss initial custom (page-level) CSS content
     */
    initLazy: function (customConfig, c, ta, options, initialHtml, initialCss, enabled, initialCustomCss) {

        const beforeUnloadHandler = () => {
            if (c.$connector && c.$connector.editor) {
                dispatchChange(c.$connector.editor);
            }
        };
        window.removeEventListener('beforeunload', beforeUnloadHandler);

        function dispatchChange(editor) {
            const event = new Event('gjs-change');
            event.htmlString = editor.getHtml();
            c.dispatchEvent(event);
        }

        if (c.$connector) {
            if (c.$connector.editor) {
                try {
                    c.$connector.editor.destroy();
                } catch (e) {
                    console.warn('GrapesJS: error destroying previous instance', e);
                }
                c.$connector.editor = null;
            }
            ta.innerHTML = '';
        }

        let changeMode = 'change';
        let readonlyTimeout;
        let customCss = initialCustomCss || '';
        const readyQueue = [];

        // GrapesJS deselects the current component as a side effect of any
        // click outside the canvas iframe - including a click on our own
        // Vaadin toolbar buttons, which live in the parent document, not
        // inside GrapesJS's own UI tree. That means editor.getSelected() is
        // already null by the time a toolbar button's server round-trip
        // calls back into e.g. getSelectedHtml/insertHtml below. Track the
        // last real selection independently instead, and use that - with a
        // liveness check (still attached to the live canvas DOM) since the
        // component may since have been removed/replaced.
        let lastSelectedComponent = null;
        const isComponentLive = (component) => {
            if (!component || !component.getEl) {
                return false;
            }
            const el = component.getEl();
            return !!(el && el.isConnected);
        };

        const runOrQueue = (fn) => {
            if (c.$connector.editor && c.$connector.ready) {
                fn();
            } else {
                readyQueue.push(fn);
            }
        };

        c.$connector = {
            editor: null,
            ready: false,

            setEditorContent: function (html) {
                runOrQueue(() => {
                    const extracted = extractStyleTags(html || '');
                    if (extracted.css.trim()) {
                        customCss = extracted.css;
                        customCssTextarea.value = customCss;
                    }
                    this.editor.setComponents(expandStyleShorthand(extracted.html));
                    applyCustomCssToCanvas();
                });
            },

            setCss: function (css) {
                runOrQueue(() => this.editor.setStyle(css || ''));
            },

            getHtml: function () {
                return this.editor ? this.editor.getHtml() : '';
            },

            getCss: function () {
                return this.editor ? (this.editor.getCss() || '') : '';
            },

            // Page-level custom CSS - hand-written by the end user (e.g.
            // @media queries or classes not modeled by the Style Manager
            // sectors), kept apart from GrapesJS's own component-generated
            // CSS. See applyCustomCssToCanvas() for how it's live-previewed.
            setCustomCss: function (css) {
                runOrQueue(() => {
                    customCss = css || '';
                    customCssTextarea.value = customCss;
                    applyCustomCssToCanvas();
                });
            },

            getCustomCss: function () {
                return customCss || '';
            },

            getFullHtml: function () {
                if (!this.editor) {
                    return '';
                }
                const inlined = inlineHtmlCss(this.editor.getHtml(), this.editor.getCss() || '');
                const trimmedCustomCss = (customCss || '').trim();
                return trimmedCustomCss ? '<style>' + customCss + '</style>' + inlined : inlined;
            },

            // The custom CSS box is deliberately kept apart from GrapesJS's
            // own internal state (see setCustomCss above), which means it
            // does NOT ride along with editor.getProjectData()/
            // loadProjectData() the way components and Style-Manager CSS
            // do. Stash/restore it here as an extra top-level field on the
            // same JSON, so an app that persists a page via this Save/Load
            // pair (rather than the separate getCustomCss()/setCustomCss()
            // calls) doesn't lose the page's custom CSS on reload.
            getProjectData: function () {
                if (!this.editor) {
                    return '{}';
                }
                const data = this.editor.getProjectData();
                data.customCss = customCss || '';
                return JSON.stringify(data);
            },

            loadProjectData: function (json) {
                runOrQueue(() => {
                    let data;
                    try {
                        data = JSON.parse(json);
                    } catch (e) {
                        console.error('GrapesJS: failed to parse/load project data', e);
                        throw e;
                    }
                    const restoredCustomCss = typeof data.customCss === 'string' ? data.customCss : '';
                    delete data.customCss;
                    this.editor.loadProjectData(data);
                    customCss = restoredCustomCss;
                    customCssTextarea.value = customCss;
                    applyCustomCssToCanvas();
                });
            },

            clear: function () {
                runOrQueue(() => {
                    this.editor.setComponents('');
                    this.editor.setStyle('');
                    customCss = '';
                    customCssTextarea.value = '';
                    applyCustomCssToCanvas();
                });
            },

            // Returns the last-selected component's own HTML (tag,
            // attributes and content) with its GrapesJS-managed styling
            // inlined as a style="..." attribute, or '' if nothing has been
            // selected (or it was since removed) - used to pre-fill a "code
            // editor" style dialog with what's about to be edited.
            // Deliberately reads lastSelectedComponent rather than
            // editor.getSelected(): by the time a toolbar button outside the
            // canvas iframe calls this, GrapesJS has already cleared the
            // live selection as a side effect of that outside click.
            //
            // Inlining matters here, not just for display: GrapesJS keeps
            // style in a separate stylesheet keyed off each component's own
            // generated id/class, so the plain HTML alone doesn't carry it -
            // and insertHtml() below replaces the component outright (a new
            // component, new generated id), which would silently drop any
            // style that wasn't already inline. Baking it into the
            // style="..." attribute here makes the round trip
            // (getSelectedHtml -> edit -> insertHtml) style-preserving.
            getSelectedHtml: function () {
                if (!this.editor || !isComponentLive(lastSelectedComponent)) {
                    return '';
                }
                const html = this.editor.getHtml({ component: lastSelectedComponent });
                return inlineHtmlCss(html, this.editor.getCss() || '');
            },

            // Applies edited/pasted HTML like a source-code editor would:
            // if a component was last selected (and is still live - see
            // getSelectedHtml above), that component is REPLACED entirely
            // (tag, attributes and content) by the given HTML - pairs with
            // getSelectedHtml() above, so editing what it returned and
            // calling this back applies the edit in place. Otherwise the
            // HTML is appended at the end of the page instead, so the same
            // action also works for adding brand new markup.
            insertHtml: function (html) {
                runOrQueue(() => {
                    const expanded = expandStyleShorthand(html || '');
                    const target = isComponentLive(lastSelectedComponent) ? lastSelectedComponent : null;
                    const result = target
                        ? target.replaceWith(expanded)
                        : this.editor.getWrapper().append(expanded);
                    const list = Array.isArray(result) ? result : [result];
                    const last = list[list.length - 1];
                    if (last) {
                        this.editor.select(last);
                    }
                });
            },

            focus: function () {
                runOrQueue(() => {
                    const body = this.editor.Canvas.getBody();
                    if (body) {
                        body.setAttribute('contenteditable', body.getAttribute('contenteditable'));
                        body.focus();
                    }
                });
            },

            setEnabled: function (editable) {
                clearTimeout(readonlyTimeout);
                readonlyTimeout = setTimeout(() => {
                    runOrQueue(() => {
                        const isPreview = this.editor.Commands.isActive('core:preview');
                        if (editable && isPreview) {
                            this.editor.stopCommand('core:preview');
                        } else if (!editable && !isPreview) {
                            this.editor.runCommand('core:preview');
                        }
                        ta.classList.toggle('gjs-vaadin-readonly', !editable);
                    });
                }, 20);
            },

            setToolbarVisible: function (visible) {
                ta.classList.toggle('gjs-vaadin-toolbar-hidden', !visible);
            },

            setMode: function (newChangeMode) {
                changeMode = newChangeMode;
            },

            undo: function () {
                runOrQueue(() => this.editor.UndoManager.undo());
            },

            redo: function () {
                runOrQueue(() => this.editor.UndoManager.redo());
            },

            runCommand: function (id) {
                runOrQueue(() => this.editor.runCommand(id));
            },

            stopCommand: function (id) {
                runOrQueue(() => this.editor.stopCommand(id));
            },

            toggleFullscreen: function () {
                runOrQueue(() => {
                    if (this.editor.Commands.isActive('core:fullscreen')) {
                        this.editor.stopCommand('core:fullscreen');
                    } else {
                        this.editor.runCommand('core:fullscreen');
                    }
                });
            },

            addToolbarButton: function (id, label) {
                runOrQueue(() => {
                    const panels = this.editor.Panels;
                    let panel = panels.getPanel('gjs-vaadin-custom-buttons');
                    if (!panel) {
                        panel = panels.addPanel({ id: 'gjs-vaadin-custom-buttons', buttons: [] });
                        actionsEl.appendChild(panel.view.el);
                    }
                    if (panels.getButton('gjs-vaadin-custom-buttons', id)) {
                        return;
                    }
                    panels.addButton('gjs-vaadin-custom-buttons', {
                        id: id,
                        label: label,
                        command: () => {
                            const event = new Event('gjs-toolbar-button-click');
                            event.buttonId = id;
                            c.dispatchEvent(event);
                        },
                    });
                    // PanelView only renders its buttons once, at panel creation; it doesn't
                    // react to buttons added/removed afterwards, so re-render explicitly.
                    panel.view.render();
                });
            },

            removeToolbarButton: function (id) {
                runOrQueue(() => {
                    this.editor.Panels.removeButton('gjs-vaadin-custom-buttons', id);
                    const panel = this.editor.Panels.getPanel('gjs-vaadin-custom-buttons');
                    if (panel) {
                        panel.view.render();
                    }
                });
            },

            addBlock: function (id, label, category, content, media) {
                runOrQueue(() => this.editor.Blocks.add(id, {
                    label: label,
                    category: category || undefined,
                    content: content,
                    media: media || undefined,
                }));
            },

            removeBlock: function (id) {
                runOrQueue(() => this.editor.Blocks.remove(id));
            },

            addDevice: function (name, width, widthMedia) {
                runOrQueue(() => this.editor.Devices.add({
                    id: name,
                    name: name,
                    width: width,
                    widthMedia: widthMedia || undefined,
                }));
            },

            setDevice: function (name) {
                runOrQueue(() => this.editor.setDevice(name));
            },

            getDevice: function () {
                return this.editor ? this.editor.getDevice() : '';
            },

            addAsset: function (src, type, name) {
                runOrQueue(() => this.editor.Assets.add({ src: src, type: type || 'image', name: name || undefined }));
            },

            removeAsset: function (src) {
                runOrQueue(() => this.editor.Assets.remove(src));
            },

            clearAssets: function () {
                runOrQueue(() => this.editor.Assets.getAll().reset());
            },

            isInDialog: function () {
                let parent = c.parentElement || (c.getRootNode() && c.getRootNode().host);
                while (parent != null) {
                    const tag = (parent.tagName || '').toLowerCase();
                    if (tag.indexOf('vaadin-dialog') === 0) {
                        return true;
                    }
                    parent = parent.parentElement || (parent.getRootNode && parent.getRootNode().host);
                }
                return false;
            },

            saveOnClose: function () {
                window.addEventListener('beforeunload', beforeUnloadHandler);
            },
        };

        const root = document.createElement('div');
        root.className = 'gjs-vaadin-root';
        root.innerHTML = `
            <div class="gjs-vaadin-topbar">
                <div class="gjs-vaadin-topbar-actions"></div>
                <div class="gjs-vaadin-topbar-devices"></div>
            </div>
            <div class="gjs-vaadin-body">
                <div class="gjs-vaadin-blocks"></div>
                <div class="gjs-vaadin-canvas"></div>
                <div class="gjs-vaadin-right">
                    <div class="gjs-vaadin-layers"></div>
                    <div class="gjs-vaadin-selectors"></div>
                    <div class="gjs-vaadin-styles"></div>
                    <div class="gjs-vaadin-inline-style">
                        <div class="gjs-vaadin-inline-style-label">Inline styles</div>
                        <textarea class="gjs-vaadin-inline-style-textarea" placeholder="property: value;" spellcheck="false" disabled></textarea>
                    </div>
                    <div class="gjs-vaadin-custom-css">
                        <div class="gjs-vaadin-custom-css-label">Custom CSS</div>
                        <textarea class="gjs-vaadin-custom-css-textarea" placeholder="@media (max-width: 480px) { ... }" spellcheck="false"></textarea>
                    </div>
                    <div class="gjs-vaadin-traits"></div>
                </div>
            </div>
        `;
        ta.appendChild(root);

        const canvasEl = root.querySelector('.gjs-vaadin-canvas');
        const blocksEl = root.querySelector('.gjs-vaadin-blocks');
        const layersEl = root.querySelector('.gjs-vaadin-layers');
        const selectorsEl = root.querySelector('.gjs-vaadin-selectors');
        const stylesEl = root.querySelector('.gjs-vaadin-styles');
        const inlineStyleTextarea = root.querySelector('.gjs-vaadin-inline-style-textarea');
        const customCssTextarea = root.querySelector('.gjs-vaadin-custom-css-textarea');
        const traitsEl = root.querySelector('.gjs-vaadin-traits');
        const actionsEl = root.querySelector('.gjs-vaadin-topbar-actions');
        const devicesEl = root.querySelector('.gjs-vaadin-topbar-devices');

        const baseConfig = {
            container: canvasEl,
            height: '100%',
            width: '100%',
            fromElement: false,
            storageManager: false,
            undoManager: { trackSelection: false },
            blockManager: { appendTo: blocksEl },
            layerManager: { appendTo: layersEl },
            styleManager: {
                appendTo: stylesEl,
                sectors: [
                    { name: 'Dimension', open: false, buildProps: ['width', 'min-height', 'padding', 'margin'] },
                    { name: 'Typography', open: false, buildProps: ['font-family', 'font-size', 'font-weight', 'letter-spacing', 'color', 'line-height', 'text-align'] },
                    { name: 'Decorations', open: false, buildProps: ['background-color', 'border-radius', 'border', 'box-shadow'] },
                    { name: 'Extra', open: false, buildProps: ['opacity', 'transition', 'display', 'position'] },
                ],
            },
            traitManager: { appendTo: traitsEl },
            deviceManager: {
                devices: [
                    { id: 'Desktop', name: 'Desktop', width: '' },
                    { id: 'Tablet', name: 'Tablet', width: '768px', widthMedia: '992px' },
                    { id: 'Mobile landscape', name: 'Mobile landscape', width: '568px', widthMedia: '768px' },
                    { id: 'Mobile portrait', name: 'Mobile portrait', width: '320px', widthMedia: '480px' },
                ],
            },
        };

        Object.assign(baseConfig, customConfig, options || {});
        baseConfig.container = canvasEl;

        const editor = grapesjs.init(baseConfig);
        c.$connector.editor = editor;

        registerEmbedComponent(editor);
        registerCustomHtmlComponent(editor);
        registerSvgComponents(editor);

        // Page-level custom CSS, hand-written by the end user rather than
        // generated by the Style Manager - e.g. @media queries or rules
        // targeting classes/selectors that aren't tied to a single
        // component. Applied directly into the canvas iframe (so @media
        // breakpoints preview live against the current device width) via a
        // dedicated <style> tag kept apart from GrapesJS's own generated
        // stylesheet, and prepended verbatim to getFullHtml()'s output.
        customCssTextarea.value = customCss;

        function applyCustomCssToCanvas() {
            const doc = editor.Canvas.getDocument();
            if (!doc || !doc.body) {
                return;
            }
            let styleEl = doc.getElementById('gjs-vaadin-custom-css');
            if (!styleEl) {
                styleEl = doc.createElement('style');
                styleEl.id = 'gjs-vaadin-custom-css';
            }
            styleEl.textContent = customCss;
            // GrapesJS keeps its own generated component CSS in a <style>
            // tag it pins as the first child of <body> (not <head>), so a
            // tag placed in <head> would always sit earlier in source order
            // and lose every equal-specificity cascade tie against it,
            // making custom CSS edits look like they don't take effect.
            // appendChild() on an already-attached node moves it, so this
            // both inserts it initially and re-pins it as the LAST node in
            // the document on every update, guaranteeing it cascades after
            // GrapesJS's own stylesheet.
            doc.body.appendChild(styleEl);
        }

        // Applied on every keystroke, not debounced: setting a <style>
        // tag's textContent is cheap (the browser only needs to
        // re-parse/re-match that one stylesheet), so there's no need to lag
        // behind typing the way the pricier component.setStyle() calls
        // below do.
        customCssTextarea.addEventListener('input', () => {
            customCss = customCssTextarea.value;
            applyCustomCssToCanvas();
        });

        // Raw inline-styles textarea: always mirrors the selected
        // component's full style object (all properties, including those
        // already surfaced by the sectors above), and lets the user add,
        // edit or remove properties - including ones the sector widgets
        // above don't expose, or values like `var(...)` they may not
        // round-trip - by editing the CSS text directly.
        let inlineStyleEditing = false;

        function refreshInlineStyleTextarea(component) {
            if (inlineStyleEditing) {
                return;
            }
            const style = component && component.getStyle ? component.getStyle() : null;
            inlineStyleTextarea.value = styleObjectToText(style);
            inlineStyleTextarea.disabled = !component;
        }

        function applyInlineStyleTextarea() {
            const component = editor.getSelected();
            if (!component) {
                return;
            }
            component.setStyle(parseStyleText(inlineStyleTextarea.value));
        }

        let inlineStyleApplyTimeout;
        inlineStyleTextarea.addEventListener('focus', () => {
            inlineStyleEditing = true;
        });
        inlineStyleTextarea.addEventListener('blur', () => {
            inlineStyleEditing = false;
            clearTimeout(inlineStyleApplyTimeout);
            applyInlineStyleTextarea();
        });
        inlineStyleTextarea.addEventListener('input', () => {
            clearTimeout(inlineStyleApplyTimeout);
            inlineStyleApplyTimeout = setTimeout(applyInlineStyleTextarea, 400);
        });

        editor.on('load', () => {
            setTimeout(() => {
                const undoRedoPanel = editor.Panels.addPanel({
                    id: 'gjs-vaadin-undo-redo',
                    buttons: [
                        { id: 'core:undo', command: 'core:undo', label: '↶', attributes: { title: 'Undo' } },
                        { id: 'core:redo', command: 'core:redo', label: '↷', attributes: { title: 'Redo' } },
                    ],
                });
                actionsEl.appendChild(undoRedoPanel.view.el);

                const optionsPanel = editor.Panels.getPanel('options');
                if (optionsPanel) {
                    actionsEl.appendChild(optionsPanel.view.el);
                }
                const devicesPanel = editor.Panels.getPanel('devices-c');
                if (devicesPanel) {
                    devicesEl.appendChild(devicesPanel.view.el);
                }
                ['commands', 'views', 'views-container'].forEach((id) => editor.Panels.removePanel(id));

                selectorsEl.appendChild(editor.SelectorManager.render());
            }, 0);

            const ourStyles = document.head.querySelector("link[href*='grapesjs.css']");
            if (ourStyles) {
                document.head.appendChild(ourStyles);
            }

            if (initialHtml) {
                const extracted = extractStyleTags(initialHtml);
                if (extracted.css.trim()) {
                    customCss = extracted.css;
                    customCssTextarea.value = customCss;
                }
                editor.setComponents(expandStyleShorthand(extracted.html));
            }
            if (initialCss) {
                editor.setStyle(initialCss);
            }
            applyCustomCssToCanvas();
            if (enabled === false) {
                editor.runCommand('core:preview');
                ta.classList.add('gjs-vaadin-readonly');
            }

            c.$connector.ready = true;
            const queued = readyQueue.splice(0);
            queued.forEach(fn => fn());

            const readyEvent = new Event('gjs-ready');
            c.dispatchEvent(readyEvent);
        });

        editor.on('update', () => {
            if (changeMode === 'change' || changeMode === 'timeout') {
                dispatchChange(editor);
            }
        });

        editor.on('component:selected', (component) => {
            lastSelectedComponent = component || null;
            refreshInlineStyleTextarea(component);
            const event = new Event('gjs-select');
            event.componentId = (component && component.getId && component.getId()) || '';
            event.tagName = (component && component.get && component.get('tagName')) || '';
            c.dispatchEvent(event);
        });

        editor.on('component:deselected', () => {
            refreshInlineStyleTextarea(null);
            const event = new Event('gjs-select');
            event.componentId = '';
            event.tagName = '';
            c.dispatchEvent(event);
        });

        editor.on('component:styleUpdate', (component) => {
            if (editor.getSelected() === component) {
                refreshInlineStyleTextarea(component);
            }
        });

        editor.on('load', () => {
            const body = editor.Canvas.getBody();
            if (body) {
                body.addEventListener('focusout', () => {
                    const focusEvent = new Event('gjs-blur');
                    c.dispatchEvent(focusEvent);
                    if (changeMode === 'blur') {
                        dispatchChange(editor);
                    }
                });
                body.addEventListener('focusin', () => {
                    c.dispatchEvent(new Event('gjs-focus'));
                });
            }
        });
    },

    /**
     * Destroys the GrapesJS editor instance attached to the given element,
     * if any.
     */
    destroy: function (c) {
        if (c && c.$connector && c.$connector.editor) {
            try {
                c.$connector.editor.destroy();
            } catch (e) {
                console.warn('GrapesJS: error during destroy', e);
            }
            c.$connector.editor = null;
            c.$connector.ready = false;
        }
    },
};
