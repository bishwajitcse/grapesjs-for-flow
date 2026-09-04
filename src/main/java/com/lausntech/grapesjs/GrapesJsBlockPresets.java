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
 *         "Sections", List.of("navbar", "hero", "feature-section", "pricing", "footer"),
 *         "Layout", List.of("two-columns", "three-columns", "container"),
 *         "Basic", List.of("text", "heading", "image", "button", "link")));
 * }</pre>
 * <p>
 * This is a starting set covering common page-building patterns, including a
 * library of best-practice marketing/landing-page sections (navbar, hero,
 * about, services, pricing, testimonials, team, stats, gallery, blog grid,
 * call to action, newsletter, contact, logo cloud, video, timeline, FAQ and
 * footer); register anything else with
 * {@link GrapesJsEditor#addBlock(GrapesJsBlock)} directly.
 */
public final class GrapesJsBlockPresets {

    private static final Map<String, GrapesJsBlock> PRESETS = new LinkedHashMap<>();

    static {
        register("text", "Text", "<div data-gjs-type=\"text\">Insert your text here</div>");

        register("heading", "Heading", "<h2>Heading text</h2>");

        register("div", "Div", "<div style=\"min-height:60px;padding:10px\"></div>");

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

        register("faq", "FAQ", """
                <section style="padding:48px 24px">
                    <h2 style="text-align:center;margin-bottom:24px;font:700 32px Arial,sans-serif;color:#0f172a">Frequently Asked Questions</h2>
                    <div style="max-width:640px;margin:0 auto">
                        <details open style="padding:22px 0;border-bottom:1px solid #e2e8f0">
                            <summary style="display:flex;justify-content:space-between;align-items:center;cursor:pointer;font:700 16px Arial,sans-serif;color:#0f172a">Question one goes here?<span style="color:#2563eb" aria-hidden="true">+</span></summary>
                            <p style="margin:12px 0 0;font:400 15px/1.7 Arial,sans-serif;color:#64748b">Answer to the first question goes here.</p>
                        </details>
                        <details style="padding:22px 0;border-bottom:1px solid #e2e8f0">
                            <summary style="display:flex;justify-content:space-between;align-items:center;cursor:pointer;font:700 16px Arial,sans-serif;color:#0f172a">Question two goes here?<span style="color:#2563eb" aria-hidden="true">+</span></summary>
                            <p style="margin:12px 0 0;font:400 15px/1.7 Arial,sans-serif;color:#64748b">Answer to the second question goes here.</p>
                        </details>
                        <details style="padding:22px 0;border-bottom:1px solid #e2e8f0">
                            <summary style="display:flex;justify-content:space-between;align-items:center;cursor:pointer;font:700 16px Arial,sans-serif;color:#0f172a">Question three goes here?<span style="color:#2563eb" aria-hidden="true">+</span></summary>
                            <p style="margin:12px 0 0;font:400 15px/1.7 Arial,sans-serif;color:#64748b">Answer to the third question goes here.</p>
                        </details>
                    </div>
                </section>
                """);

        // -- Sections (additional common landing/marketing page sections) --

        register("navbar", "Navbar", """
                <header style="display:flex;align-items:center;justify-content:space-between;padding:16px 32px;background:#fff;border-bottom:1px solid #e2e8f0">
                    <div style="font:700 20px Arial,sans-serif;color:#0f172a">Brand</div>
                    <nav style="display:flex;gap:28px">
                        <a href="#" style="text-decoration:none;font:500 15px Arial,sans-serif;color:#334155">Home</a>
                        <a href="#" style="text-decoration:none;font:500 15px Arial,sans-serif;color:#334155">Features</a>
                        <a href="#" style="text-decoration:none;font:500 15px Arial,sans-serif;color:#334155">Pricing</a>
                        <a href="#" style="text-decoration:none;font:500 15px Arial,sans-serif;color:#334155">Contact</a>
                    </nav>
                    <a href="#" style="display:inline-block;padding:10px 20px;border-radius:8px;background:#2563eb;color:#fff;text-decoration:none;font:600 14px Arial,sans-serif">Sign Up</a>
                </header>
                """);

        register("footer", "Footer", """
                <footer style="padding:56px 24px 24px;background:#0f172a;color:#94a3b8">
                    <div style="max-width:1120px;margin:0 auto;display:flex;gap:48px;flex-wrap:wrap">
                        <div style="flex:2;min-width:220px">
                            <div style="font:700 20px Arial,sans-serif;color:#fff;margin-bottom:12px">Brand</div>
                            <p style="font:400 14px/1.7 Arial,sans-serif;max-width:280px">Building better digital experiences, one workspace at a time.</p>
                        </div>
                        <div style="flex:1;min-width:140px">
                            <div style="font:700 14px Arial,sans-serif;color:#fff;margin-bottom:14px">Product</div>
                            <a href="#" style="display:block;margin-bottom:10px;text-decoration:none;color:#94a3b8;font:400 14px Arial,sans-serif">Features</a>
                            <a href="#" style="display:block;margin-bottom:10px;text-decoration:none;color:#94a3b8;font:400 14px Arial,sans-serif">Pricing</a>
                            <a href="#" style="display:block;text-decoration:none;color:#94a3b8;font:400 14px Arial,sans-serif">Changelog</a>
                        </div>
                        <div style="flex:1;min-width:140px">
                            <div style="font:700 14px Arial,sans-serif;color:#fff;margin-bottom:14px">Company</div>
                            <a href="#" style="display:block;margin-bottom:10px;text-decoration:none;color:#94a3b8;font:400 14px Arial,sans-serif">About</a>
                            <a href="#" style="display:block;margin-bottom:10px;text-decoration:none;color:#94a3b8;font:400 14px Arial,sans-serif">Blog</a>
                            <a href="#" style="display:block;text-decoration:none;color:#94a3b8;font:400 14px Arial,sans-serif">Careers</a>
                        </div>
                        <div style="flex:1;min-width:140px">
                            <div style="font:700 14px Arial,sans-serif;color:#fff;margin-bottom:14px">Legal</div>
                            <a href="#" style="display:block;margin-bottom:10px;text-decoration:none;color:#94a3b8;font:400 14px Arial,sans-serif">Privacy</a>
                            <a href="#" style="display:block;text-decoration:none;color:#94a3b8;font:400 14px Arial,sans-serif">Terms</a>
                        </div>
                    </div>
                    <div style="max-width:1120px;margin:40px auto 0;padding-top:24px;border-top:1px solid #1e293b;font:400 13px Arial,sans-serif">© 2026 Brand. All rights reserved.</div>
                </footer>
                """);

        register("about", "About", """
                <section style="padding:64px 24px">
                    <div style="max-width:1120px;margin:0 auto;display:flex;gap:48px;align-items:center;flex-wrap:wrap">
                        <img src="https://placehold.co/560x420" style="flex:1;min-width:280px;max-width:100%;border-radius:12px" alt="" />
                        <div style="flex:1;min-width:280px">
                            <div style="display:inline-block;padding:6px 14px;border-radius:999px;background:#eef2ff;color:#4f46e5;font:600 12px Arial,sans-serif;margin-bottom:16px">ABOUT US</div>
                            <h2 style="margin:0 0 16px;font:700 34px/1.2 Arial,sans-serif;color:#0f172a">Our story, in a nutshell</h2>
                            <p style="margin:0;font:400 16px/1.7 Arial,sans-serif;color:#64748b">We started with a simple idea: make great tools accessible to everyone. Today our team ships products used by thousands of people every day.</p>
                        </div>
                    </div>
                </section>
                """);

        register("services", "Services", """
                <section style="padding:64px 24px;background:#f8fafc">
                    <div style="max-width:1120px;margin:0 auto">
                        <h2 style="text-align:center;margin:0 0 8px;font:700 32px Arial,sans-serif;color:#0f172a">Our Services</h2>
                        <p style="text-align:center;margin:0 0 40px;font:400 16px Arial,sans-serif;color:#64748b">Everything you need, all in one place.</p>
                        <div style="display:flex;gap:24px;flex-wrap:wrap">
                            <div style="flex:1;min-width:240px;padding:28px;background:#fff;border-radius:12px;border:1px solid #e2e8f0">
                                <div style="width:44px;height:44px;border-radius:10px;background:#eef2ff;color:#4f46e5;display:flex;align-items:center;justify-content:center;font:700 18px Arial,sans-serif;margin-bottom:16px">1</div>
                                <h3 style="margin:0 0 8px;font:700 18px Arial,sans-serif;color:#0f172a">Strategy</h3>
                                <p style="margin:0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">Plan a roadmap that gets you where you want to go.</p>
                            </div>
                            <div style="flex:1;min-width:240px;padding:28px;background:#fff;border-radius:12px;border:1px solid #e2e8f0">
                                <div style="width:44px;height:44px;border-radius:10px;background:#eef2ff;color:#4f46e5;display:flex;align-items:center;justify-content:center;font:700 18px Arial,sans-serif;margin-bottom:16px">2</div>
                                <h3 style="margin:0 0 8px;font:700 18px Arial,sans-serif;color:#0f172a">Design</h3>
                                <p style="margin:0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">Craft interfaces people enjoy using every day.</p>
                            </div>
                            <div style="flex:1;min-width:240px;padding:28px;background:#fff;border-radius:12px;border:1px solid #e2e8f0">
                                <div style="width:44px;height:44px;border-radius:10px;background:#eef2ff;color:#4f46e5;display:flex;align-items:center;justify-content:center;font:700 18px Arial,sans-serif;margin-bottom:16px">3</div>
                                <h3 style="margin:0 0 8px;font:700 18px Arial,sans-serif;color:#0f172a">Delivery</h3>
                                <p style="margin:0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">Ship reliably with support every step of the way.</p>
                            </div>
                        </div>
                    </div>
                </section>
                """);

        register("pricing", "Pricing", """
                <section style="padding:64px 24px">
                    <div style="max-width:1120px;margin:0 auto">
                        <h2 style="text-align:center;margin:0 0 8px;font:700 32px Arial,sans-serif;color:#0f172a">Simple, transparent pricing</h2>
                        <p style="text-align:center;margin:0 0 40px;font:400 16px Arial,sans-serif;color:#64748b">Choose the plan that fits your team.</p>
                        <div style="display:flex;gap:24px;flex-wrap:wrap;align-items:stretch">
                            <div style="flex:1;min-width:260px;padding:32px;border:1px solid #e2e8f0;border-radius:14px">
                                <h3 style="margin:0 0 4px;font:700 18px Arial,sans-serif;color:#0f172a">Starter</h3>
                                <p style="margin:0 0 20px;font:400 14px Arial,sans-serif;color:#64748b">For individuals getting started</p>
                                <div style="margin:0 0 24px;font:700 40px Arial,sans-serif;color:#0f172a">$9<span style="font:400 15px Arial,sans-serif;color:#64748b">/mo</span></div>
                                <a href="#" style="display:block;text-align:center;padding:12px;border-radius:8px;border:1px solid #cbd5e1;color:#334155;text-decoration:none;font:600 14px Arial,sans-serif">Get Started</a>
                            </div>
                            <div style="flex:1;min-width:260px;padding:32px;border-radius:14px;background:#0f172a;color:#fff;transform:scale(1.03)">
                                <h3 style="margin:0 0 4px;font:700 18px Arial,sans-serif;color:#fff">Pro</h3>
                                <p style="margin:0 0 20px;font:400 14px Arial,sans-serif;color:#94a3b8">For growing teams</p>
                                <div style="margin:0 0 24px;font:700 40px Arial,sans-serif;color:#fff">$29<span style="font:400 15px Arial,sans-serif;color:#94a3b8">/mo</span></div>
                                <a href="#" style="display:block;text-align:center;padding:12px;border-radius:8px;background:#2563eb;color:#fff;text-decoration:none;font:600 14px Arial,sans-serif">Get Started</a>
                            </div>
                            <div style="flex:1;min-width:260px;padding:32px;border:1px solid #e2e8f0;border-radius:14px">
                                <h3 style="margin:0 0 4px;font:700 18px Arial,sans-serif;color:#0f172a">Enterprise</h3>
                                <p style="margin:0 0 20px;font:400 14px Arial,sans-serif;color:#64748b">For large organizations</p>
                                <div style="margin:0 0 24px;font:700 40px Arial,sans-serif;color:#0f172a">Custom</div>
                                <a href="#" style="display:block;text-align:center;padding:12px;border-radius:8px;border:1px solid #cbd5e1;color:#334155;text-decoration:none;font:600 14px Arial,sans-serif">Contact Sales</a>
                            </div>
                        </div>
                    </div>
                </section>
                """);

        register("testimonials", "Testimonials", """
                <section style="padding:64px 24px;background:#f8fafc">
                    <div style="max-width:1120px;margin:0 auto">
                        <h2 style="text-align:center;margin:0 0 40px;font:700 32px Arial,sans-serif;color:#0f172a">What our customers say</h2>
                        <div style="display:flex;gap:24px;flex-wrap:wrap">
                            <div style="flex:1;min-width:260px;padding:28px;background:#fff;border-radius:12px;border:1px solid #e2e8f0">
                                <p style="margin:0 0 20px;font:400 15px/1.7 Arial,sans-serif;color:#334155">"This product completely changed how our team works. Setup took minutes, not weeks."</p>
                                <div style="display:flex;align-items:center;gap:12px">
                                    <img src="https://placehold.co/40x40" style="border-radius:50%" alt="" />
                                    <div>
                                        <div style="font:700 14px Arial,sans-serif;color:#0f172a">Jamie Lee</div>
                                        <div style="font:400 13px Arial,sans-serif;color:#64748b">Product Manager</div>
                                    </div>
                                </div>
                            </div>
                            <div style="flex:1;min-width:260px;padding:28px;background:#fff;border-radius:12px;border:1px solid #e2e8f0">
                                <p style="margin:0 0 20px;font:400 15px/1.7 Arial,sans-serif;color:#334155">"Support is fast and the platform just works. Exactly what we needed."</p>
                                <div style="display:flex;align-items:center;gap:12px">
                                    <img src="https://placehold.co/40x40" style="border-radius:50%" alt="" />
                                    <div>
                                        <div style="font:700 14px Arial,sans-serif;color:#0f172a">Sam Rivera</div>
                                        <div style="font:400 13px Arial,sans-serif;color:#64748b">CTO, Acme Inc.</div>
                                    </div>
                                </div>
                            </div>
                            <div style="flex:1;min-width:260px;padding:28px;background:#fff;border-radius:12px;border:1px solid #e2e8f0">
                                <p style="margin:0 0 20px;font:400 15px/1.7 Arial,sans-serif;color:#334155">"We evaluated five tools and this was the easiest to roll out company-wide."</p>
                                <div style="display:flex;align-items:center;gap:12px">
                                    <img src="https://placehold.co/40x40" style="border-radius:50%" alt="" />
                                    <div>
                                        <div style="font:700 14px Arial,sans-serif;color:#0f172a">Alex Chen</div>
                                        <div style="font:400 13px Arial,sans-serif;color:#64748b">Head of Design</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>
                """);

        register("team", "Team", """
                <section style="padding:64px 24px">
                    <div style="max-width:1120px;margin:0 auto">
                        <h2 style="text-align:center;margin:0 0 8px;font:700 32px Arial,sans-serif;color:#0f172a">Meet the team</h2>
                        <p style="text-align:center;margin:0 0 40px;font:400 16px Arial,sans-serif;color:#64748b">The people building the product.</p>
                        <div style="display:flex;gap:24px;flex-wrap:wrap;justify-content:center">
                            <div style="width:220px;text-align:center">
                                <img src="https://placehold.co/160x160" style="width:160px;height:160px;border-radius:50%;margin-bottom:16px" alt="" />
                                <div style="font:700 16px Arial,sans-serif;color:#0f172a">Taylor Morgan</div>
                                <div style="font:400 14px Arial,sans-serif;color:#64748b">Co-founder & CEO</div>
                            </div>
                            <div style="width:220px;text-align:center">
                                <img src="https://placehold.co/160x160" style="width:160px;height:160px;border-radius:50%;margin-bottom:16px" alt="" />
                                <div style="font:700 16px Arial,sans-serif;color:#0f172a">Jordan Kim</div>
                                <div style="font:400 14px Arial,sans-serif;color:#64748b">Co-founder & CTO</div>
                            </div>
                            <div style="width:220px;text-align:center">
                                <img src="https://placehold.co/160x160" style="width:160px;height:160px;border-radius:50%;margin-bottom:16px" alt="" />
                                <div style="font:700 16px Arial,sans-serif;color:#0f172a">Casey Nguyen</div>
                                <div style="font:400 14px Arial,sans-serif;color:#64748b">Head of Design</div>
                            </div>
                        </div>
                    </div>
                </section>
                """);

        register("stats", "Stats", """
                <section style="padding:56px 24px;background:#0f172a">
                    <div style="max-width:1120px;margin:0 auto;display:flex;gap:24px;flex-wrap:wrap;text-align:center">
                        <div style="flex:1;min-width:160px">
                            <div style="font:700 40px Arial,sans-serif;color:#fff">10K+</div>
                            <div style="font:400 14px Arial,sans-serif;color:#94a3b8;margin-top:8px">Active users</div>
                        </div>
                        <div style="flex:1;min-width:160px">
                            <div style="font:700 40px Arial,sans-serif;color:#fff">99.9%</div>
                            <div style="font:400 14px Arial,sans-serif;color:#94a3b8;margin-top:8px">Uptime</div>
                        </div>
                        <div style="flex:1;min-width:160px">
                            <div style="font:700 40px Arial,sans-serif;color:#fff">150+</div>
                            <div style="font:400 14px Arial,sans-serif;color:#94a3b8;margin-top:8px">Countries</div>
                        </div>
                        <div style="flex:1;min-width:160px">
                            <div style="font:700 40px Arial,sans-serif;color:#fff">24/7</div>
                            <div style="font:400 14px Arial,sans-serif;color:#94a3b8;margin-top:8px">Support</div>
                        </div>
                    </div>
                </section>
                """);

        register("gallery", "Gallery", """
                <section style="padding:64px 24px">
                    <div style="max-width:1120px;margin:0 auto">
                        <h2 style="text-align:center;margin:0 0 32px;font:700 32px Arial,sans-serif;color:#0f172a">Gallery</h2>
                        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px">
                            <img src="https://placehold.co/360x260" style="width:100%;border-radius:10px" alt="" />
                            <img src="https://placehold.co/360x260" style="width:100%;border-radius:10px" alt="" />
                            <img src="https://placehold.co/360x260" style="width:100%;border-radius:10px" alt="" />
                            <img src="https://placehold.co/360x260" style="width:100%;border-radius:10px" alt="" />
                            <img src="https://placehold.co/360x260" style="width:100%;border-radius:10px" alt="" />
                            <img src="https://placehold.co/360x260" style="width:100%;border-radius:10px" alt="" />
                        </div>
                    </div>
                </section>
                """);

        register("blog-grid", "Blog Grid", """
                <section style="padding:64px 24px;background:#f8fafc">
                    <div style="max-width:1120px;margin:0 auto">
                        <h2 style="text-align:center;margin:0 0 40px;font:700 32px Arial,sans-serif;color:#0f172a">From the blog</h2>
                        <div style="display:flex;gap:24px;flex-wrap:wrap">
                            <div style="flex:1;min-width:260px;background:#fff;border-radius:12px;overflow:hidden;border:1px solid #e2e8f0">
                                <img src="https://placehold.co/400x220" style="width:100%;display:block" alt="" />
                                <div style="padding:20px">
                                    <div style="font:600 12px Arial,sans-serif;color:#2563eb;margin-bottom:8px">DESIGN</div>
                                    <h3 style="margin:0 0 8px;font:700 17px Arial,sans-serif;color:#0f172a">Five tips for cleaner UI</h3>
                                    <p style="margin:0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">A short guide to tightening up your interfaces.</p>
                                </div>
                            </div>
                            <div style="flex:1;min-width:260px;background:#fff;border-radius:12px;overflow:hidden;border:1px solid #e2e8f0">
                                <img src="https://placehold.co/400x220" style="width:100%;display:block" alt="" />
                                <div style="padding:20px">
                                    <div style="font:600 12px Arial,sans-serif;color:#2563eb;margin-bottom:8px">ENGINEERING</div>
                                    <h3 style="margin:0 0 8px;font:700 17px Arial,sans-serif;color:#0f172a">Scaling our backend</h3>
                                    <p style="margin:0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">What we learned migrating to a new architecture.</p>
                                </div>
                            </div>
                            <div style="flex:1;min-width:260px;background:#fff;border-radius:12px;overflow:hidden;border:1px solid #e2e8f0">
                                <img src="https://placehold.co/400x220" style="width:100%;display:block" alt="" />
                                <div style="padding:20px">
                                    <div style="font:600 12px Arial,sans-serif;color:#2563eb;margin-bottom:8px">COMPANY</div>
                                    <h3 style="margin:0 0 8px;font:700 17px Arial,sans-serif;color:#0f172a">Announcing our Series A</h3>
                                    <p style="margin:0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">Here's what's next for the team.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>
                """);

        register("cta", "Call To Action", """
                <section style="padding:64px 24px">
                    <div style="max-width:1120px;margin:0 auto;padding:48px 40px;border-radius:20px;background:#2563eb;text-align:center">
                        <h2 style="margin:0 0 14px;font:700 34px Arial,sans-serif;color:#fff">Ready to take the next step?</h2>
                        <p style="max-width:650px;margin:0 auto 25px;font:400 16px/1.7 Arial,sans-serif;color:#dbeafe">Start building better workflows today and see how much more your team can accomplish.</p>
                        <a href="#" style="display:inline-block;padding:14px 24px;border-radius:9px;background:#fff;color:#1d4ed8;text-decoration:none;font:700 14px Arial,sans-serif">Get Started Today</a>
                    </div>
                </section>
                """);

        register("newsletter", "Newsletter", """
                <section style="padding:56px 24px;background:#eef2ff;text-align:center">
                    <div style="max-width:560px;margin:0 auto">
                        <h2 style="margin:0 0 10px;font:700 26px Arial,sans-serif;color:#0f172a">Stay in the loop</h2>
                        <p style="margin:0 0 24px;font:400 15px/1.6 Arial,sans-serif;color:#64748b">Get product updates and news delivered to your inbox.</p>
                        <div style="display:flex;gap:10px;max-width:420px;margin:0 auto">
                            <input type="email" placeholder="you@example.com" style="flex:1;padding:12px 14px;border:1px solid #cbd5e1;border-radius:8px;font:400 14px Arial,sans-serif" />
                            <a href="#" style="display:inline-block;padding:12px 22px;border-radius:8px;background:#4f46e5;color:#fff;text-decoration:none;font:600 14px Arial,sans-serif;white-space:nowrap">Subscribe</a>
                        </div>
                    </div>
                </section>
                """);

        register("contact", "Contact", """
                <section style="padding:64px 24px">
                    <div style="max-width:640px;margin:0 auto">
                        <h2 style="text-align:center;margin:0 0 8px;font:700 32px Arial,sans-serif;color:#0f172a">Get in touch</h2>
                        <p style="text-align:center;margin:0 0 32px;font:400 16px Arial,sans-serif;color:#64748b">We'd love to hear from you.</p>
                        <div style="display:flex;gap:16px;margin-bottom:16px">
                            <input type="text" placeholder="Your name" style="flex:1;padding:12px 14px;border:1px solid #cbd5e1;border-radius:8px;font:400 14px Arial,sans-serif" />
                            <input type="email" placeholder="Your email" style="flex:1;padding:12px 14px;border:1px solid #cbd5e1;border-radius:8px;font:400 14px Arial,sans-serif" />
                        </div>
                        <textarea placeholder="Your message" rows="5" style="width:100%;padding:12px 14px;border:1px solid #cbd5e1;border-radius:8px;font:400 14px Arial,sans-serif;margin-bottom:16px;box-sizing:border-box"></textarea>
                        <a href="#" style="display:inline-block;padding:12px 26px;border-radius:8px;background:#2563eb;color:#fff;text-decoration:none;font:600 14px Arial,sans-serif">Send Message</a>
                    </div>
                </section>
                """);

        register("logos", "Logo Cloud", """
                <section style="padding:48px 24px">
                    <div style="max-width:1120px;margin:0 auto">
                        <p style="text-align:center;margin:0 0 28px;font:600 13px Arial,sans-serif;color:#94a3b8;letter-spacing:1px">TRUSTED BY TEAMS AT</p>
                        <div style="display:flex;gap:40px;flex-wrap:wrap;justify-content:center;align-items:center;opacity:0.7">
                            <img src="https://placehold.co/120x40?text=Logo" alt="" />
                            <img src="https://placehold.co/120x40?text=Logo" alt="" />
                            <img src="https://placehold.co/120x40?text=Logo" alt="" />
                            <img src="https://placehold.co/120x40?text=Logo" alt="" />
                            <img src="https://placehold.co/120x40?text=Logo" alt="" />
                        </div>
                    </div>
                </section>
                """);

        register("embed", "Embed", """
                <div data-gjs-type="embed">
                    <div data-gjs-type="embed-placeholder" style="padding:24px;text-align:center;border:1px dashed #94a3b8;border-radius:8px;font:400 13px Arial,sans-serif;color:#64748b;background:#f8fafc">
                        Double-click to paste embed code (e.g. a YouTube embed)
                    </div>
                </div>
                """);

        register("video-section", "Video Section", """
                <section style="padding:64px 24px;text-align:center">
                    <h2 style="margin:0 0 8px;font:700 32px Arial,sans-serif;color:#0f172a">See it in action</h2>
                    <p style="margin:0 0 32px;font:400 16px Arial,sans-serif;color:#64748b">A two minute tour of the product.</p>
                    <div style="max-width:840px;margin:0 auto;aspect-ratio:16/9;border-radius:14px;overflow:hidden;background:#0f172a">
                        <iframe width="100%" height="100%" src="about:blank" title="Video" style="border:0;display:block" allowfullscreen></iframe>
                    </div>
                </section>
                """);

        register("timeline", "Timeline", """
                <section style="padding:64px 24px;background:#f8fafc">
                    <div style="max-width:820px;margin:0 auto">
                        <h2 style="text-align:center;margin:0 0 40px;font:700 32px Arial,sans-serif;color:#0f172a">How it works</h2>
                        <div style="border-left:2px solid #e2e8f0;padding-left:28px">
                            <div style="position:relative;padding-bottom:32px">
                                <div style="position:absolute;left:-35px;top:2px;width:14px;height:14px;border-radius:50%;background:#2563eb"></div>
                                <h3 style="margin:0 0 6px;font:700 17px Arial,sans-serif;color:#0f172a">1. Sign up</h3>
                                <p style="margin:0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">Create your account in under a minute.</p>
                            </div>
                            <div style="position:relative;padding-bottom:32px">
                                <div style="position:absolute;left:-35px;top:2px;width:14px;height:14px;border-radius:50%;background:#2563eb"></div>
                                <h3 style="margin:0 0 6px;font:700 17px Arial,sans-serif;color:#0f172a">2. Set up your workspace</h3>
                                <p style="margin:0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">Invite your team and configure your settings.</p>
                            </div>
                            <div style="position:relative">
                                <div style="position:absolute;left:-35px;top:2px;width:14px;height:14px;border-radius:50%;background:#2563eb"></div>
                                <h3 style="margin:0 0 6px;font:700 17px Arial,sans-serif;color:#0f172a">3. Start building</h3>
                                <p style="margin:0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">You're ready to go — no extra setup required.</p>
                            </div>
                        </div>
                    </div>
                </section>
                """);

        // -- Layout --

        register("four-columns", "Four Columns", """
                <div style="display:flex;gap:16px;padding:16px">
                    <div style="flex:1;padding:16px;background:#f8fafc">Column A</div>
                    <div style="flex:1;padding:16px;background:#f8fafc">Column B</div>
                    <div style="flex:1;padding:16px;background:#f8fafc">Column C</div>
                    <div style="flex:1;padding:16px;background:#f8fafc">Column D</div>
                </div>
                """);

        // -- Components --

        register("testimonial-card", "Testimonial Card", """
                <div style="max-width:360px;padding:24px;border:1px solid #e2e8f0;border-radius:12px">
                    <p style="margin:0 0 16px;font:400 15px/1.7 Arial,sans-serif;color:#334155">"A fantastic product that just works."</p>
                    <div style="display:flex;align-items:center;gap:12px">
                        <img src="https://placehold.co/40x40" style="border-radius:50%" alt="" />
                        <div>
                            <div style="font:700 14px Arial,sans-serif;color:#0f172a">Jamie Lee</div>
                            <div style="font:400 13px Arial,sans-serif;color:#64748b">Product Manager</div>
                        </div>
                    </div>
                </div>
                """);

        register("pricing-card", "Pricing Card", """
                <div style="max-width:280px;padding:28px;border:1px solid #e2e8f0;border-radius:14px">
                    <h3 style="margin:0 0 4px;font:700 18px Arial,sans-serif;color:#0f172a">Pro Plan</h3>
                    <p style="margin:0 0 20px;font:400 14px Arial,sans-serif;color:#64748b">For growing teams</p>
                    <div style="margin:0 0 20px;font:700 36px Arial,sans-serif;color:#0f172a">$29<span style="font:400 14px Arial,sans-serif;color:#64748b">/mo</span></div>
                    <a href="#" style="display:block;text-align:center;padding:12px;border-radius:8px;background:#2563eb;color:#fff;text-decoration:none;font:600 14px Arial,sans-serif">Get Started</a>
                </div>
                """);

        register("icon-box", "Icon Box", """
                <div style="padding:8px">
                    <div style="width:44px;height:44px;border-radius:10px;background:#eef2ff;color:#4f46e5;display:flex;align-items:center;justify-content:center;font:700 18px Arial,sans-serif;margin-bottom:14px">★</div>
                    <h3 style="margin:0 0 8px;font:700 17px Arial,sans-serif;color:#0f172a">Feature title</h3>
                    <p style="margin:0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">A short description of this feature.</p>
                </div>
                """);

        register("badge", "Badge", """
                <span style="display:inline-block;padding:6px 14px;border-radius:999px;background:#eef2ff;color:#4f46e5;font:600 12px Arial,sans-serif">New</span>
                """);

        register("avatar", "Avatar", """
                <img src="https://placehold.co/64x64" style="width:64px;height:64px;border-radius:50%" alt="" />
                """);

        register("progress-bar", "Progress Bar", """
                <div style="width:100%;height:10px;border-radius:999px;background:#e2e8f0;overflow:hidden">
                    <div style="width:65%;height:100%;background:#2563eb"></div>
                </div>
                """);

        register("rating", "Rating", """
                <div style="font:400 20px Arial,sans-serif;color:#f59e0b;letter-spacing:2px">★★★★☆</div>
                """);

        register("divider", "Divider", """
                <hr style="border:none;border-top:1px solid #e2e8f0;margin:24px 0" />
                """);

        register("spacer", "Spacer", """
                <div style="height:48px"></div>
                """);

        register("social-icons", "Social Icons", """
                <div style="display:flex;gap:14px">
                    <a href="#" style="width:38px;height:38px;border-radius:50%;background:#f1f5f9;color:#334155;display:flex;align-items:center;justify-content:center;text-decoration:none;font:700 13px Arial,sans-serif">Fb</a>
                    <a href="#" style="width:38px;height:38px;border-radius:50%;background:#f1f5f9;color:#334155;display:flex;align-items:center;justify-content:center;text-decoration:none;font:700 13px Arial,sans-serif">Ig</a>
                    <a href="#" style="width:38px;height:38px;border-radius:50%;background:#f1f5f9;color:#334155;display:flex;align-items:center;justify-content:center;text-decoration:none;font:700 13px Arial,sans-serif">X</a>
                    <a href="#" style="width:38px;height:38px;border-radius:50%;background:#f1f5f9;color:#334155;display:flex;align-items:center;justify-content:center;text-decoration:none;font:700 13px Arial,sans-serif">In</a>
                </div>
                """);

        register("alert", "Alert", """
                <div style="display:flex;align-items:center;gap:10px;padding:14px 18px;border-radius:10px;background:#fffbeb;border:1px solid #fde68a;color:#92400e;font:400 14px/1.5 Arial,sans-serif">
                    <strong style="font:700 14px Arial,sans-serif">Heads up:</strong> this is an announcement banner.
                </div>
                """);

        register("quote", "Quote", """
                <blockquote style="margin:0;padding:0 0 0 20px;border-left:4px solid #2563eb;font:italic 400 18px/1.7 Georgia,serif;color:#334155">
                    "The best way to predict the future is to build it."
                </blockquote>
                """);

        register("tabs", "Tabs", """
                <div>
                    <div style="display:flex;gap:4px;border-bottom:1px solid #e2e8f0">
                        <div style="padding:12px 20px;font:600 14px Arial,sans-serif;color:#2563eb;border-bottom:2px solid #2563eb">Overview</div>
                        <div style="padding:12px 20px;font:600 14px Arial,sans-serif;color:#64748b">Features</div>
                        <div style="padding:12px 20px;font:600 14px Arial,sans-serif;color:#64748b">Pricing</div>
                    </div>
                    <div style="padding:20px 4px;font:400 14px/1.6 Arial,sans-serif;color:#334155">Tab content goes here.</div>
                </div>
                """);

        register("accordion", "Accordion Item", """
                <details style="padding:18px 0;border-bottom:1px solid #e2e8f0">
                    <summary style="display:flex;justify-content:space-between;align-items:center;cursor:pointer;font:700 15px Arial,sans-serif;color:#0f172a">Accordion item title<span style="color:#2563eb" aria-hidden="true">+</span></summary>
                    <p style="margin:12px 0 0;font:400 14px/1.6 Arial,sans-serif;color:#64748b">Content revealed when this item is expanded.</p>
                </details>
                """);

        // -- Basic --

        register("list", "List", """
                <ul style="margin:0;padding-left:20px;font:400 15px/1.9 Arial,sans-serif;color:#334155">
                    <li>First list item</li>
                    <li>Second list item</li>
                    <li>Third list item</li>
                </ul>
                """);

        register("table", "Table", """
                <table style="width:100%;border-collapse:collapse;font:400 14px Arial,sans-serif;color:#334155">
                    <thead>
                        <tr>
                            <th style="text-align:left;padding:10px 12px;border-bottom:2px solid #e2e8f0;font:700 13px Arial,sans-serif;color:#0f172a">Name</th>
                            <th style="text-align:left;padding:10px 12px;border-bottom:2px solid #e2e8f0;font:700 13px Arial,sans-serif;color:#0f172a">Role</th>
                            <th style="text-align:left;padding:10px 12px;border-bottom:2px solid #e2e8f0;font:700 13px Arial,sans-serif;color:#0f172a">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="padding:10px 12px;border-bottom:1px solid #e2e8f0">Jamie Lee</td>
                            <td style="padding:10px 12px;border-bottom:1px solid #e2e8f0">Designer</td>
                            <td style="padding:10px 12px;border-bottom:1px solid #e2e8f0">Active</td>
                        </tr>
                        <tr>
                            <td style="padding:10px 12px">Sam Rivera</td>
                            <td style="padding:10px 12px">Engineer</td>
                            <td style="padding:10px 12px">Active</td>
                        </tr>
                    </tbody>
                </table>
                """);

        register("input", "Input", """
                <input type="text" placeholder="Enter text" style="width:100%;padding:12px 14px;border:1px solid #cbd5e1;border-radius:8px;font:400 14px Arial,sans-serif;box-sizing:border-box" />
                """);

        register("textarea", "Textarea", """
                <textarea placeholder="Enter your message" rows="4" style="width:100%;padding:12px 14px;border:1px solid #cbd5e1;border-radius:8px;font:400 14px Arial,sans-serif;box-sizing:border-box"></textarea>
                """);

        register("select", "Select", """
                <select style="width:100%;padding:12px 14px;border:1px solid #cbd5e1;border-radius:8px;font:400 14px Arial,sans-serif;background:#fff">
                    <option>Option one</option>
                    <option>Option two</option>
                    <option>Option three</option>
                </select>
                """);

        register("checkbox", "Checkbox", """
                <label style="display:flex;align-items:center;gap:8px;font:400 14px Arial,sans-serif;color:#334155">
                    <input type="checkbox" /> I agree to the terms
                </label>
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
