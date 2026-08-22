package com.lausntech.grapesjs.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lausntech.grapesjs.GrapesJsEditor;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
				 "timeline", "faq", "section"));
		
		features.put("Layout", List.of("two-columns", "three-columns", "four-columns", "container"));
		
		features.put("Components", List.of("card", "pricing-card", "testimonial-card", "icon-box", "button", "link",
				 "badge",  "progress-bar", "rating", "social-icons", "alert", "quote", "tabs",
				"accordion", "divider", "spacer"));
		
		features.put("Basic", List.of("text", "heading", "image", "list", "table"));
		//features.put("Forms", List.of("input", "textarea", "select", "checkbox"));
		editor.addBlocks(features);

		editor.setValue(
				"""
						           <section style="padding:88px 24px;background:#f8fafc;text-align:center;">
						  <div style="max-width:820px;margin:0 auto;">
						    <div style="display:inline-block;padding:7px 14px;border-radius:999px;background:#e0f2fe;color:#0369a1;font:600 13px Arial,sans-serif;margin-bottom:18px;">SMARTER WAY TO WORK</div>
						    <h1 style="margin:0 0 20px;font:700 48px/1.12 Arial,sans-serif;letter-spacing:-1.5px;color:#0f172a;">Build better experiences with a modern platform</h1>
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

		
		Div spacer = new Div();
		spacer.setWidthFull();
		spacer.getStyle().setFlexGrow("1");

		Button close = new Button();
		close.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
		close.setIcon(LumoIcon.CROSS.create());
		HorizontalLayout toolbar = new HorizontalLayout(undo, redo, preview, spacer, save, close);
		toolbar.setWidthFull();
		toolbar.setPadding(true);
		return toolbar;
	}

	private void log(String message) {
		
	}
}
