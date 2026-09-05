package com.lausntech.grapesjs.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lausntech.grapesjs.GrapesJsEditor;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;

/**
 * Demo/verification view exercising the GrapesJsEditor Java &lt;-&gt;
 * JavaScript bridge: content load/export, project data save/restore, commands,
 * blocks, and events.
 */
@Route("grapes")
public class GrapesJsEditorView extends VerticalLayout {

	private final GrapesJsEditor editor = new GrapesJsEditor();
	

	public GrapesJsEditorView() {
		setSizeFull();
		setPadding(false);
		setSpacing(false);

		editor.setSizeFull();

		Map<String, List<String>> features = new HashMap<>();
		features.put("Sections", List.of("hero", "about", "services", "feature-section", "pricing",
				"testimonials", "team", "stats", "blog-grid", "cta", "logos",
				 "embed", "timeline", "faq", "section"));
		
		features.put("Layout", List.of("two-columns", "three-columns", "four-columns", "container"));
		
		features.put("Components", List.of("card", "pricing-card", "testimonial-card", "icon-box", "button", "link",
				 "badge",  "progress-bar", "rating", "social-icons", "alert", "quote", "tabs",
				"accordion", "divider", "spacer"));
		
		features.put("Basic", List.of("text", "heading", "div", "image", "svg-icon", "list", "table"));
		//features.put("Forms", List.of("input", "textarea", "select", "checkbox"));
		editor.addBlocks(features);

		editor.setValue(
				"""
						           <section style="padding:88px 24px;
background: radial-gradient(circle at 8% 12%,rgba(25,198,194,.18),transparent 27%),radial-gradient(circle at 94% 4%,rgba(55,108,251,.14),transparent 30%),linear-gradient(145deg,#f9fefe 0%,#edf8fa 53%,#e1f1f5 100%);text-align:center;">
						  <div style="max-width:820px;margin:0 auto;">
						    <div style="display:inline-block;padding:7px 14px;border-radius:999px;background:#e0f2fe;color:#0369a1;font:600 13px Arial,sans-serif;margin-bottom:18px;">SMARTER WAY TO WORK</div>
						    <h1 style="box-sizing:border-box;background:linear-gradient(105deg,#054a61 5%,#087f98 58%,#315ee9);-webkit-background-clip:text;background-clip:text;color:transparent">Build better experiences with a modern platform</h1>
						    <p style="max-width:680px;margin:0 auto 30px;font:400 18px/1.7 Arial,sans-serif;color:#64748b;">Bring your teams, workflows and customer experiences together in one simple, powerful workspace.</p>
						    <a href="#" style="display:inline-block;padding:14px 24px;border-radius:10px;background:#2563eb;color:#fff;text-decoration:none;font:600 15px Arial,sans-serif;">Get Started</a>
						    <a href="#" style="display:inline-block;margin-left:10px;padding:13px 24px;border:1px solid #cbd5e1;border-radius:10px;color:#334155;text-decoration:none;font:600 15px Arial,sans-serif;background:#fff;">Learn More</a>
						  </div>
						</section>
						<section style="padding:64px 24px;">
						  <div style="max-width:1120px;margin:0 auto;padding:48px 40px;border-radius:20px;background:#2563eb;text-align:center;">
						    <h2 style="margin:0 0 14px;font:700 34px Arial;color:#fff;">Ready to take the next step?</h2>
						    <p style="max-width:650px;margin:0 auto 25px;font:400 16px/1.7 Arial;color:#dbeafe;">Start building better workflows today and see how much more your team can accomplish.</p>
						    <a href="#" style="display:inline-block;padding:14px 24px;border-radius:9px;background:#fff;color:#1d4ed8;text-decoration:none;font:700 14px Arial;">Get Started Today</a>
						  </div>
						</section>
						<section style="padding:64px 24px;text-align:center;">
						<svg viewBox="0 0 620 470" aria-label="Jibby connected feature constellation" style="box-sizing: border-box; display: block; max-width: 100%;"><defs style="box-sizing:border-box"><linearGradient id="fg" x1="0" x2="1" style="box-sizing:border-box"><stop stop-color="#376cfb" style="box-sizing:border-box"></stop><stop offset="1" stop-color="#7c5cff" style="box-sizing:border-box"></stop></linearGradient></defs><path d="M310 230 120 95m190 135 190-135M310 230 95 345m215-115 215 115M310 230v195" stroke="#d3dfea" stroke-width="3" stroke-dasharray="7 9" style="box-sizing:border-box"></path><circle cx="310" cy="230" r="85" fill="url(#fg)" style="box-sizing:border-box"></circle><text x="310" y="223" text-anchor="middle" fill="#fff" font-size="28" font-weight="800" style="box-sizing:border-box">Jibby</text><text x="310" y="252" text-anchor="middle" fill="#dfe7ff" font-size="14" style="box-sizing:border-box">One workspace</text><g fill="#fff" stroke="#dce5ed" stroke-width="2" style="box-sizing:border-box"><rect x="35" y="45" width="170" height="95" rx="20" style="box-sizing:border-box"></rect><rect x="415" y="45" width="170" height="95" rx="20" style="box-sizing:border-box"></rect><rect x="20" y="305" width="170" height="95" rx="20" style="box-sizing:border-box"></rect><rect x="430" y="305" width="170" height="95" rx="20" style="box-sizing:border-box"></rect><rect x="225" y="385" width="170" height="70" rx="20" style="box-sizing:border-box"></rect></g><g fill="#071a2f" font-size="15" font-weight="700" text-anchor="middle" style="box-sizing:border-box"><text x="120" y="102" style="box-sizing:border-box">Plan &amp; organize</text><text x="500" y="102" style="box-sizing:border-box">Control quality</text><text x="105" y="362" style="box-sizing:border-box">Manage work</text><text x="515" y="362" style="box-sizing:border-box">Collaborate</text><text x="310" y="428" style="box-sizing:border-box">Track operations</text></g></svg>
						</section>

						            """);

		editor.addReadyListener(e -> log("Editor ready"));
		editor.addValueChangeListener(e -> log("Content changed (" + e.getValue().length() + " chars)"));
		editor.addSelectListener(e -> log("Selected: " + (e.getTagName().isEmpty() ? "(none)" : e.getTagName())));

		editor.addToolbarButton("publish", "Publish");
		editor.addToolbarButtonClickListener(e -> {
			if ("publish".equals(e.getButtonId())) {
				log("Publish clicked (server-side listener fired)");
				Notification.show("Published!");
			}
		});


		HorizontalLayout toolbar = buildToolbar();

		FlexLayout main = new FlexLayout(editor);
		main.setSizeFull();
		main.getStyle().set("flex-grow", "1").set("min-height", "0");

		add(toolbar, main);
		setFlexGrow(1, main);
		editor.setToolbarVisible(!editor.isToolbarVisible());

	}

	private HorizontalLayout buildToolbar() {
		Button save = new Button("", e -> editor.getProjectData().thenAccept(data -> {
			UI ui = UI.getCurrent();
			if (ui != null) {
				ui.access(() -> {
					
					Notification.show("Saved");
				});
			}
		}));
		save.setIcon(LumoIcon.DOWNLOAD.create());
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

		Button undo = new Button("", e -> editor.undo());
		undo.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
		undo.setIcon(LumoIcon.UNDO.create());
		Button redo = new Button("", e -> editor.redo());
		redo.setIcon(LumoIcon.REDO.create());
		redo.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
		Button preview = new Button("", e -> editor.runCommand("core:preview"));
		preview.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
		preview.setIcon(LumoIcon.EYE.create());
		
		Button insertCode = new Button("", e -> openInsertCodeDialog());
		insertCode.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
		insertCode.setIcon(LumoIcon.UPLOAD.create());
		insertCode.getElement().setAttribute("title", "Insert code");


		Div spacer = new Div();
		spacer.setWidthFull();
		spacer.getStyle().setFlexGrow("1");

		Button close = new Button();
		close.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
		close.setIcon(LumoIcon.CROSS.create());
		HorizontalLayout toolbar = new HorizontalLayout(undo, redo, preview, insertCode, spacer, save, close);
		toolbar.setWidthFull();
		toolbar.setPadding(true);
		return toolbar;
	}

	private void openInsertCodeDialog() {
		Dialog dialog = new Dialog();
		dialog.setHeaderTitle("Insert code");
		dialog.setWidth("600px");

		TextArea codeArea = new TextArea();
		codeArea.setPlaceholder("Paste HTML code here");
		codeArea.setWidthFull();
		codeArea.setHeight("300px");
		dialog.add(codeArea);

		Button insert = new Button("Insert", e -> {
			String html = codeArea.getValue();
			if (html != null && !html.isBlank()) {
				editor.insertHtml(html);
			}
			dialog.close();
		});
		insert.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		Button cancel = new Button("Cancel", e -> dialog.close());

		dialog.getFooter().add(cancel, insert);
		dialog.open();
	}

	private void log(String message) {
		
	}
}
