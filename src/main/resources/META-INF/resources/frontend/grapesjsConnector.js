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
            decls.removeProperty('background');
            probe.style.cssText = 'background:' + background;
            decls.setProperty('background-color', probe.style.backgroundColor || background);
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

window.Vaadin.Flow.grapesjsConnector = {

    /**
     * @param customConfig raw JS/JSON object spliced in from GrapesJsEditor#setConfig
     * @param c the GrapesJsEditor's own element (holds $connector)
     * @param ta the inner container element GrapesJS renders into
     * @param options the Java-side `config` map (generic configure() entries)
     * @param initialHtml initial HTML content
     * @param initialCss initial CSS content
     * @param enabled whether the canvas should start out editable
     */
    initLazy: function (customConfig, c, ta, options, initialHtml, initialCss, enabled) {

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
        const readyQueue = [];

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
                runOrQueue(() => this.editor.setComponents(expandStyleShorthand(html || '')));
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

            getFullHtml: function () {
                if (!this.editor) {
                    return '';
                }
                return inlineHtmlCss(this.editor.getHtml(), this.editor.getCss() || '');
            },

            getProjectData: function () {
                return this.editor ? JSON.stringify(this.editor.getProjectData()) : '{}';
            },

            loadProjectData: function (json) {
                runOrQueue(() => {
                    try {
                        this.editor.loadProjectData(JSON.parse(json));
                    } catch (e) {
                        console.error('GrapesJS: failed to parse/load project data', e);
                        throw e;
                    }
                });
            },

            clear: function () {
                runOrQueue(() => {
                    this.editor.setComponents('');
                    this.editor.setStyle('');
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
                editor.setComponents(expandStyleShorthand(initialHtml));
            }
            if (initialCss) {
                editor.setStyle(initialCss);
            }
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
