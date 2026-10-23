# Generates the BetterHunter mockup screens that complete the app, in dark and light, from one template.
# Usage: python3 screens.py <out_dir>
import sys
from pathlib import Path

OUT = Path(sys.argv[1])

DARK = dict(
    name="dark", bg="#0E1611", card="#17221A", border="#24352A", outline="#2C3B30", track="#24352A",
    text="#EEF1EA", muted="#A9B5AB", faint="#8E9A90", orange="#F27B2B", on_orange="#1A0E05", label="#F7A06A",
    sel="#2A1C11", warm_border="#4A2F1B", green="#8FD19E", bar="#8FD19E", green_tint="#22382A",
    orange_tint="#3A2516", red="#F2867A", red_tint="#3A1A16", on_red="#1A0E05", on_green="#0E1611",
    current="#F7F2EA", box="#3A4C3F", link="#8FD19E", link_hover="#B5E4BF", shadow="none", dim="rgba(5,9,7,0.72)",
    dialog="#1C2920", suffix="",
)
LIGHT = dict(
    name="light", bg="#F5F2EA", card="#FFFFFF", border="#E6E1D5", outline="#DDD7C8", track="#E2DCCD",
    text="#17201A", muted="#5B6659", faint="#5B6659", orange="#E8691E", on_orange="#1A0E05", label="#B4500F",
    sel="#FDEBDD", warm_border="#F3C9A6", green="#2F6B3E", bar="#3F8A52", green_tint="#E3EFE4",
    orange_tint="#FCE6D4", red="#B3261E", red_tint="#F9DEDA", on_red="#FFFFFF", on_green="#FFFFFF",
    current="#17201A", box="#B9B3A4", link="#2F6B3E", link_hover="#1F4A2A", shadow="0 1px 2px rgba(23,32,26,0.06)",
    dim="rgba(23,32,26,0.45)", dialog="#FFFFFF", suffix="Light",
)

MOOSE = "/_blob/ed3939a5023b68655218c7345bb481dd"
MULE = "/_blob/38c860d8b4e20555914ef4c9812cbd55"

FACE = ("M21 30H43L38.5 49C36.5 56.5 27.5 56.5 25.5 49Z M25.6 38.5a1.9 1.9 0 1 0 3.8 0a1.9 1.9 0 1 0 -3.8 0Z "
        "M34.6 38.5a1.9 1.9 0 1 0 3.8 0a1.9 1.9 0 1 0 -3.8 0Z M29.2 51.5a2.8 2.2 0 1 0 5.6 0a2.8 2.2 0 1 0 -5.6 0Z")


def buck(size, face, antlers):
    return (f'<svg width="{size}" height="{size}" viewBox="0 0 64 64" aria-hidden="true">'
            f'<g fill="none" stroke="{antlers}" stroke-width="3.6" stroke-linecap="round" stroke-linejoin="round">'
            '<path d="M27 29C23 23 19.5 17 19.5 8"/><path d="M21.2 18.5L13.5 14"/><path d="M20 12.5L25.5 7.5"/>'
            '<path d="M37 29C41 23 44.5 17 44.5 8"/><path d="M42.8 18.5L50.5 14"/><path d="M44 12.5L38.5 7.5"/></g>'
            f'<path fill="{face}" d="M22.5 31.5L11 27.5L20 36.5Z M41.5 31.5L53 27.5L44 36.5Z"/>'
            f'<path fill="{face}" fill-rule="evenodd" d="{FACE}"/></svg>')


ICONS = {
    "back": '<path d="M15 5l-7 7 7 7"/>',
    "close": '<path d="M6 6l12 12"/><path d="M18 6L6 18"/>',
    "chevron": '<path d="M9 5l7 7-7 7"/>',
    "flag": '<path d="M5 21V4"/><path d="M5 4h11l-2 4 2 4H5"/>',
    "play": '<path d="M7 4.5v15l12-7.5z"/>',
    "timer": '<circle cx="12" cy="13" r="8"/><path d="M12 9v4l2.5 2"/><path d="M10 2h4"/>',
    "replay": '<path d="M4 12a8 8 0 1 0 2.3-5.7"/><path d="M4 4v4.5h4.5"/>',
    "shield": '<path d="M12 3l7 3v5c0 4.5-3 8.5-7 10-4-1.5-7-5.5-7-10V6z"/>',
    "tree": '<path d="M12 3l5 7h-3l4 6h-5v5h-2v-5H6l4-6H7z"/>',
    "mountain": '<path d="M3 19l6-9 4 6 3-4 5 7z"/>',
    "external": '<path d="M14 4h6v6"/><path d="M20 4l-9 9"/><path d="M18 14v5a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h5"/>',
    "book": '<path d="M4 5.5C4 4.7 4.7 4 5.5 4H11v15H5.5c-.8 0-1.5.7-1.5 1.5z"/><path d="M20 5.5c0-.8-.7-1.5-1.5-1.5H13v15h5.5c.8 0 1.5.7 1.5 1.5z"/>',
    "mail": '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 7l9 6 9-6"/>',
    "lock": '<rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V8a4 4 0 0 1 8 0v3"/>',
    "info": '<circle cx="12" cy="12" r="9.5"/><path d="M12 11v6"/><path d="M12 7.5v.01"/>',
    "gear": '<circle cx="12" cy="12" r="3.2"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/>',
    "check": '<path d="M5 12.5l4.5 4.5L19 7.5"/>',
    "x": '<path d="M7 7l10 10"/><path d="M17 7L7 17"/>',
    "target": '<circle cx="12" cy="12" r="8"/><circle cx="12" cy="12" r="3"/><path d="M12 2v3"/><path d="M12 19v3"/><path d="M2 12h3"/><path d="M19 12h3"/>',
    "paw": '<circle cx="7" cy="9" r="2"/><circle cx="12" cy="6.5" r="2"/><circle cx="17" cy="9" r="2"/><path d="M8 17c0-3 2-5 4-5s4 2 4 5c0 2-2 2.5-4 2.5S8 19 8 17z"/>',
    "sound": '<path d="M4 9v6h4l5 4V5L8 9z"/><path d="M16.5 8.5a5 5 0 0 1 0 7"/><path d="M19 6a8.5 8.5 0 0 1 0 12"/>',
    "download": '<path d="M12 4v11"/><path d="M7 10l5 5 5-5"/><path d="M5 20h14"/>',
    "goal": '<circle cx="12" cy="12" r="8.5"/><path d="M12 7v5l3 2"/>',
    "user": '<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 3.6-7 8-7s8 3 8 7"/>',
    "globe": '<circle cx="12" cy="12" r="9"/><path d="M3 12h18"/><path d="M12 3c2.5 2.6 3.8 5.6 3.8 9s-1.3 6.4-3.8 9c-2.5-2.6-3.8-5.6-3.8-9S9.5 5.6 12 3z"/>',
    "trophy": '<path d="M8 4h8v5a4 4 0 0 1-8 0z"/><path d="M8 6H5a3 3 0 0 0 3 4"/><path d="M16 6h3a3 3 0 0 1-3 4"/><path d="M12 13v4"/><path d="M8 20h8"/>',
    "eye": '<path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/>',
    "school": '<path d="M2 9l10-5 10 5-10 5z"/><path d="M6 11v5c2 1.5 4 2 6 2s4-.5 6-2v-5"/><path d="M22 9v6"/>',
    "flame": '<path d="M12 3c1 4 5 5.5 5 10a5 5 0 0 1-10 0c0-2 1-3.5 2-4.5.5 2 1.5 2.5 2.5 2.5C11 8 11 5.5 12 3z"/>',
    "bow": '<path d="M7 3c6 3.5 6 14.5 0 18"/><path d="M7 3v18"/><path d="M3 12h17"/><path d="M17 9l3 3-3 3"/>',
    "crossbow": '<path d="M3 8c5-4 13-4 18 0"/><path d="M3 8l9 5 9-5"/><path d="M12 3v18"/><path d="M10 18h4"/>',
    "pin": '<path d="M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21z"/><circle cx="12" cy="9.5" r="2.5"/>',
    "maple": '<path d="M12 3l1.6 3.6 2.4-1-.6 4 3-1-1 3 2.6 1.4-4.2 3.2.6 2.3-3.4-.8V21h-2v-3.3l-3.4.8.6-2.3L4 13l2.6-1.4-1-3 3 1-.6-4 2.4 1z"/>',
    "sprout": '<path d="M12 21v-9"/><path d="M12 12c0-4 2.5-7 7-7 0 4.5-3 7-7 7z"/><path d="M12 14c0-3-2-5.5-6-5.5 0 3.5 2.5 5.5 6 5.5z"/>',
    "first_aid": '<rect x="3" y="6" width="18" height="14" rx="2"/><path d="M9 6V4h6v2"/><path d="M12 10v6"/><path d="M9 13h6"/>',
    "expand": '<path d="M4 9V4h5"/><path d="M20 9V4h-5"/><path d="M4 15v5h5"/><path d="M20 15v5h-5"/>',
}


def icon(name, size=20, color="currentColor", width=2):
    return (f'<svg width="{size}" height="{size}" viewBox="0 0 24 24" fill="none" stroke="{color}" stroke-width="{width}" '
            f'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">{ICONS[name]}</svg>')


def href(t, name):
    return f"{name}{t['suffix']}.dc.html"


def page(t, title, body, gap=16, padding="12px 20px 24px", extra_style=""):
    return f'''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>{title}{", light" if t["name"] == "light" else ""}</title>
<script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Barlow+Condensed:wght@600;700&amp;family=Barlow:wght@400;500;600&amp;display=swap" rel="stylesheet">
<style>
body{{margin:0}}
a{{color:{t["link"]}}}a:hover{{color:{t["link_hover"]}}}
</style>
</helmet>
<div style="width: 390px; height: 844px; box-sizing: border-box; background: {t["bg"]}; color: {t["text"]}; font-family: Barlow, system-ui, sans-serif; padding: {padding}; display: flex; flex-direction: column; gap: {gap}px; overflow: hidden; position: relative{extra_style}">
{body}
</div>
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{{"$preview":{{"width":390,"height":844}}}}'>
class Component extends DCLogic {{
renderVals() {{
return {{}};
}}
}}
</script>
</body>
</html>
'''


def round_link(t, target, label, name, color=None):
    return (f'<a href="{target}" aria-label="{label}" style="width: 44px; height: 44px; border-radius: 999px; display: flex; '
            f'align-items: center; justify-content: center; color: {color or t["text"]}; flex-shrink: 0">{icon(name, 22, width=2.2)}</a>')


def header(t, title, back=None, right="", overline=None, close=False):
    left = round_link(t, back, "Close" if close else "Back", "close" if close else "back") if back else '<span style="width: 44px"></span>'
    over = (f'<span style="font-size: 11px; font-weight: 600; letter-spacing: 1.2px; text-transform: uppercase; color: {t["muted"]}">{overline}</span>'
            if overline else "")
    return f'''  <header style="display: flex; align-items: center; justify-content: space-between; height: 48px; flex-shrink: 0">
    {left}
    <div style="display: flex; flex-direction: column; align-items: center; min-width: 0">{over}<span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 20px; white-space: nowrap">{title}</span></div>
    {right or '<span style="width: 44px"></span>'}
  </header>'''


def label(t, text, color=None, extra=""):
    return (f'<span style="font-size: 12px; font-weight: 600; letter-spacing: 1.2px; text-transform: uppercase; '
            f'color: {color or t["label"]}{extra}">{text}</span>')


def card(t, inner, pad="16px", gap=12, radius=22, extra=""):
    return (f'<section style="background: {t["card"]}; border: 1px solid {t["border"]}; border-radius: {radius}px; padding: {pad}; '
            f'display: flex; flex-direction: column; gap: {gap}px; box-shadow: {t["shadow"]}{extra}">{inner}</section>')


def primary(t, text, target, extra="", icon_name=None):
    ic = icon(icon_name, 16) if icon_name else ""
    return (f'<a href="{target}" style="height: 54px; border-radius: 14px; background: {t["orange"]}; color: {t["on_orange"]}; '
            f'font: 600 17px Barlow, sans-serif; text-decoration: none; display: flex; align-items: center; justify-content: center; gap: 8px{extra}">{ic}{text}</a>')


def secondary(t, text, target, extra="", icon_name=None):
    ic = icon(icon_name, 16) if icon_name else ""
    return (f'<a href="{target}" style="height: 54px; border-radius: 14px; border: 1.5px solid {t["outline"]}; color: {t["text"]}; '
            f'font: 600 15px Barlow, sans-serif; text-decoration: none; display: flex; align-items: center; justify-content: center; gap: 8px; box-sizing: border-box{extra}">{ic}{text}</a>')


def tile(t, name, tone="green", size=44, radius=12):
    bg, fg = (t["green_tint"], t["green"]) if tone == "green" else (t["orange_tint"], t["label"])
    return (f'<span style="width: {size}px; height: {size}px; flex-shrink: 0; border-radius: {radius}px; background: {bg}; color: {fg}; '
            f'display: flex; align-items: center; justify-content: center">{icon(name, 22 if size >= 40 else 18)}</span>')


def ring(t, pct, size, stroke, color, inner):
    r = (size - stroke) / 2
    c = 2 * 3.14159 * r
    off = c * (1 - pct / 100)
    return f'''<div style="position: relative; width: {size}px; height: {size}px; flex-shrink: 0">
      <svg width="{size}" height="{size}" viewBox="0 0 {size} {size}" aria-hidden="true"><circle cx="{size/2}" cy="{size/2}" r="{r}" fill="none" stroke="{t["track"]}" stroke-width="{stroke}"/><circle cx="{size/2}" cy="{size/2}" r="{r}" fill="none" stroke="{color}" stroke-width="{stroke}" stroke-linecap="round" stroke-dasharray="{c:.1f}" stroke-dashoffset="{off:.1f}" transform="rotate(-90 {size/2} {size/2})"/></svg>
      <div style="position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center">{inner}</div>
    </div>'''


def meter(t, label_text, value, pct):
    return f'''<span style="display: flex; flex-direction: column; gap: 6px">
        <span style="display: flex; justify-content: space-between; font-size: 12px; color: {t["muted"]}"><span>{label_text}</span><span style="color: {t["green"]}; font-weight: 600">{value}</span></span>
        <span style="height: 6px; border-radius: 3px; background: {t["track"]}; display: block"><span style="display: block; width: {pct}%; height: 6px; border-radius: 3px; background: {t["bar"]}"></span></span>
      </span>'''


def row(t, lead, title, sub=None, trail="chevron", target=None, divider=True):
    trail_html = {"chevron": icon("chevron", 18, t["muted"]), "external": icon("external", 16, t["muted"]), None: ""}.get(trail, trail)
    sub_html = f'<span style="font-size: 13px; color: {t["muted"]}">{sub}</span>' if sub else ""
    tag = "a" if target else "div"
    attr = f' href="{target}"' if target else ""
    border = f"border-top: 1px solid {t['border']};" if divider else ""
    return (f'<{tag}{attr} style="display: flex; align-items: center; gap: 12px; min-height: 56px; padding: 6px 0; {border} color: {t["text"]}; text-decoration: none">'
            f'{lead}<span style="display: flex; flex-direction: column; gap: 1px; flex: 1; min-width: 0"><span style="font-weight: 600; font-size: 15px">{title}</span>{sub_html}</span>{trail_html}</{tag}>')


# ---------------------------------------------------------------------------------------------------------- screens

def settings(t):
    pill = (f'<span style="display: flex; border: 1px solid {t["outline"]}; border-radius: 999px; padding: 3px; background: {t["card"]}">'
            f'<span style="color: {t["muted"]}; font: 600 13px Barlow, sans-serif; height: 34px; min-width: 42px; border-radius: 999px; display: flex; align-items: center; justify-content: center">FR</span>'
            f'<span style="background: {t["green_tint"]}; color: {t["text"]}; font: 600 13px Barlow, sans-serif; height: 34px; min-width: 42px; border-radius: 999px; display: flex; align-items: center; justify-content: center">EN</span></span>')
    goal = (f'<span style="display: flex; border: 1px solid {t["outline"]}; border-radius: 999px; padding: 3px; background: {t["card"]}">'
            + "".join(f'<span style="{"background: " + t["sel"] + "; border: 1.5px solid " + t["orange"] + "; color: " + t["text"] if n == 20 else "color: " + t["muted"] + "; border: 1.5px solid transparent"}; font: 600 13px Barlow, sans-serif; height: 34px; min-width: 42px; box-sizing: border-box; border-radius: 999px; display: flex; align-items: center; justify-content: center">{n}</span>' for n in (10, 20, 30))
            + '</span>')
    body = f'''{header(t, "Settings", back=href(t, "Main"))}
  {label(t, "Your profile", t["muted"])}
  {card(t, row(t, tile(t, "user"), "Hunter type", "Working on it", target=href(t, "HunterProfile"), divider=False)
        + row(t, tile(t, "target"), "Hunting with", "Firearm", target=href(t, "HuntWith"))
        + row(t, tile(t, "pin"), "Where you hunt", "Québec", target=href(t, "HuntWhere"))
        + row(t, tile(t, "maple", "orange"), "My provinces", "Québec · add other provinces\' quizzes", target=href(t, "Provinces")), pad="2px 16px", gap=0)}
  {label(t, "App", t["muted"])}
  {card(t, row(t, tile(t, "globe"), "Language", None, trail=pill, divider=False)
        + row(t, tile(t, "goal"), "Daily goal", "Questions a day", trail=goal), pad="2px 16px", gap=0)}
  {label(t, "About", t["muted"])}
  {card(t, row(t, tile(t, "info"), "About BetterHunter", "How it works, sources, disclaimer", target=href(t, "About"), divider=False)
        + row(t, tile(t, "mail"), "Contact", "contact@kallistesoft.ca", trail="external", target="#")
        + row(t, tile(t, "lock"), "Privacy policy", None, trail="external", target="#"), pad="2px 16px", gap=0)}
  <p style="margin: auto 0 0; text-align: center; font-size: 12px; color: {t["faint"]}">BetterHunter · Version 1.0.0</p>'''
    return page(t, "BetterHunter settings", body, gap=10)


def switch(t, on):
    track, knob, pos = (t["orange"], t["on_orange"] if t["name"] == "dark" else "#FFFFFF", "right: 3px") if on else (t["track"], t["muted"], "left: 3px")
    return (f'<span role="switch" aria-checked="{"true" if on else "false"}" style="position: relative; width: 48px; height: 28px; flex-shrink: 0; border-radius: 999px; background: {track}">'
            f'<span style="position: absolute; top: 3px; {pos}; width: 22px; height: 22px; border-radius: 999px; background: {knob}"></span></span>')


def provinces(t):
    badge = (f'<span style="font-size: 11px; font-weight: 600; padding: 3px 8px; border-radius: 999px; background: {t["green_tint"]}; '
             f'color: {t["green"]}; align-self: flex-start">Where you hunt</span>')

    def province(name, title, quizzes, on, where=False):
        return (f'<div style="display: flex; align-items: center; gap: 12px; padding: 14px 0; {"" if where else "border-top: 1px solid " + t["border"] + ";"}">'
                f'{tile(t, name)}<span style="display: flex; flex-direction: column; gap: 3px; flex: 1; min-width: 0">'
                f'<span style="font-family: \'Barlow Condensed\', sans-serif; font-weight: 600; font-size: 20px; line-height: 1.1">{title}</span>'
                f'<span style="font-size: 13px; line-height: 1.35; color: {t["muted"]}">{quizzes}</span>{badge if where else ""}</span>{switch(t, on)}</div>')
    body = f'''{header(t, "My provinces", back=href(t, "Settings"))}
  <p style="margin: 0; font-size: 14px; line-height: 1.45; color: {t["muted"]}">Your quizzes follow your provinces. Add a province to practise its hunter course, or remove one to hide its quizzes.</p>
  {card(t, province("tree", "Québec", "Hunter course with firearms (ICAF) · bow and crossbow coming soon", True, where=True)
        + province("mountain", "Alberta", "Conservation and Hunter Education · PAL (firearms licence)", False)
        + province("maple", "Elsewhere in Canada", "PAL (firearms licence)", False), pad="0 16px", gap=0)}
  <p style="margin: 0; font-size: 13px; color: {t["muted"]}; display: flex; align-items: center; gap: 8px">{icon("goal", 16)} More provinces coming.</p>'''
    return page(t, "BetterHunter my provinces", body, gap=12)


def hunt_where(t, new_hunter=False):
    fl = "NewHunter" if new_hunter else ""
    def option(name, title, sub, selected=False):
        bg, border = (t["sel"], t["orange"]) if selected else (t["card"], t["outline"])
        if selected:
            mark = (f'<span style="width: 22px; height: 22px; flex-shrink: 0; border-radius: 999px; background: {t["orange"]}; color: {t["on_orange"]}; '
                    f'display: flex; align-items: center; justify-content: center">{icon("check", 14, width=3)}</span>')
        else:
            mark = f'<span style="width: 22px; height: 22px; flex-shrink: 0; box-sizing: border-box; border-radius: 999px; border: 2px solid {t["box"]}"></span>'
        return (f'<label style="display: flex; align-items: center; gap: 14px; min-height: 84px; box-sizing: border-box; padding: 14px 16px; border-radius: 18px; '
                f'background: {bg}; border: 1.5px solid {border}">'
                f'<input type="radio" name="province"{" checked" if selected else ""} style="position: absolute; opacity: 0; width: 1px; height: 1px">'
                f'{tile(t, name, "orange" if selected else "green")}<span style="display: flex; flex-direction: column; gap: 2px; flex: 1; min-width: 0">'
                f'<span style="font-family: \'Barlow Condensed\', sans-serif; font-weight: 600; font-size: 21px; line-height: 1.1">{title}</span>'
                f'<span style="font-size: 13px; line-height: 1.35; color: {t["muted"]}">{sub}</span></span>{mark}</label>')
    body = f'''<header style="display: flex; align-items: center; height: 48px; flex-shrink: 0">{round_link(t, href(t, "HuntWith" + fl), "Back", "back")}</header>
  <div style="display: flex; flex-direction: column; gap: 8px">
    {label(t, "Last question")}
    <h1 style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 34px; line-height: 1.05">Where do you hunt?</h1>
    <p style="margin: 0; font-size: 15px; line-height: 1.4; color: {t["muted"]}">{"Your rules and field skills follow your province." if new_hunter else "Your quizzes and rules follow your province."}</p>
  </div>
  <fieldset style="border: 0; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 12px">
    <legend style="position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0)">Where do you hunt?</legend>
    {option("tree", "Québec", "Québec hunting rules and game" if new_hunter else "Québec hunter course (ICAF; bow and crossbow coming soon)", selected=True)}
    {option("mountain", "Alberta", "Alberta hunting rules and game" if new_hunter else "Alberta hunter course and the PAL (firearms licence)")}
    {option("maple", "Elsewhere in Canada", "Canada-wide skills for now; more provinces coming" if new_hunter else "The PAL (firearms licence) for now; more provinces coming")}
  </fieldset>
  <p style="margin: 0; font-size: 13px; line-height: 1.4; color: {t["muted"]}; display: flex; gap: 8px">{icon("info", 16)}<span>{"You can add other provinces later in Settings." if new_hunter else "You can add other provinces' quizzes later in Settings."}</span></p>
  <div style="margin-top: auto">{primary(t, "Continue", href(t, "HomeNewHunter" if new_hunter else "Main"))}</div>'''
    return page(t, "BetterHunter onboarding: where do you hunt", body, gap=16)


def about(t):
    src = "".join(row(t, tile(t, "book"), title, None, trail="external", target="#", divider=i > 0) for i, title in enumerate(
        ["CFSC student manual (RCMP, 2014)", "ICAF manual (FédéCP, 2020)", "2026 Alberta Guide to Hunting Regulations"]))
    body = f'''{header(t, "About", back=href(t, "Settings"))}
  <section style="background: {t["green_tint"]}; border: 1px solid {t["border"]}; border-radius: 16px; padding: 14px 16px; display: flex; gap: 12px">
    <span style="color: {t["green"]}; flex-shrink: 0; padding-top: 2px">{icon("info", 20)}</span>
    <p style="margin: 0; font-size: 13px; line-height: 1.45">Independent study aid, not affiliated with or endorsed by the RCMP, the Canadian Firearms Program, the Government of Québec or FédéCP. Questions are original practice questions based on the official manuals; they are not the official exam questions. Errors are possible and laws change: always check the official manual and current regulations.</p>
  </section>
  {label(t, "How it works", t["muted"])}
  {card(t, f'<p style="margin: 0; font-size: 14px; line-height: 1.45"><b style="font-weight: 600">Practice:</b> answer and explanation after every question</p><p style="margin: 0; font-size: 14px; line-height: 1.45; border-top: 1px solid {t["border"]}; padding-top: 10px"><b style="font-weight: 600">Mock exam:</b> timed, results at the end, like the real one</p>', pad="14px 16px", gap=10, radius=18)}
  {label(t, "Sources", t["muted"])}
  {card(t, src, pad="4px 16px", gap=0, radius=18)}
  <p style="margin: 0; font-size: 12px; line-height: 1.45; color: {t["muted"]}">Photos: Wikimedia Commons, under free licences. Author and licence are shown under each photo.</p>'''
    return page(t, "BetterHunter about", body, gap=12)


def exam_training(t):
    ring_html = ring(t, 48, 56, 6, t["green"], f'<span style="font-size: 13px; font-weight: 600; color: {t["green"]}">48%</span>')
    icaf = f'''<div style="display: flex; gap: 12px; align-items: flex-start">
      {tile(t, "tree")}
      <span style="display: flex; flex-direction: column; gap: 2px; flex: 1; min-width: 0">
        <span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 20px; line-height: 1.1">Hunter education with firearms (ICAF)</span>
        <span style="font-size: 13px; color: {t["muted"]}">228 questions</span>
        <span style="font-size: 13px; color: {t["muted"]}">Québec · 60 min · 80% to pass</span>
      </span>
      <span style="display: flex; flex-direction: column; align-items: center; gap: 4px">{ring_html}<span style="font-size: 11px; color: {t["muted"]}">Readiness</span></span>
    </div>
    <div style="display: flex; gap: 10px">
      {primary(t, "Practice", href(t, "PracticeQuestion"), "; flex: 1; height: 52px", "play")}
      {secondary(t, "Mock exam", href(t, "ExamQuestion"), "; flex: 1; height: 52px", "timer")}
    </div>
    <a href="{href(t, "PracticeQuestion")}" style="display: flex; align-items: center; gap: 8px; color: {t["link"]}; font-weight: 600; font-size: 14px; text-decoration: none; min-height: 32px">{icon("replay", 16)} Review my mistakes (5)</a>
    <a href="{href(t, "PassedExam")}" style="display: flex; align-items: center; gap: 8px; color: {t["link"]}; font-weight: 600; font-size: 14px; text-decoration: none; min-height: 32px; border-top: 1px solid {t["border"]}; padding-top: 10px">{icon("trophy", 16)} I passed the real exam</a>'''
    add = row(t, tile(t, "pin"), "Other provinces", "Alberta, elsewhere in Canada (PAL)", target=href(t, "Provinces"), divider=False)
    body = f'''{header(t, "Exam training", back=href(t, "Main"))}
  <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 4px">{label(t, "My exams", t["muted"])}<a href="{href(t, "Provinces")}" style="color: {t["link"]}; font-weight: 600; font-size: 14px; text-decoration: none; min-height: 32px; display: flex; align-items: center">Change</a></div>
  {card(t, icaf, pad="18px", gap=14)}
  {label(t, "Add more", t["muted"], "; margin-top: 6px")}
  {card(t, add, pad="4px 16px", gap=0)}'''
    return page(t, "BetterHunter exam training", body, gap=12)


Q = "Compared with cervids, where is the aiming point in a bear's vital zone?"
CHOICES = ["Much farther forward, at the neck", "Slightly farther back", "At the head only", "Exactly in the same spot"]


def choice(t, letter, text, state):
    if state == "wrong":
        bg, border, tbg, tfg, mark = t["red_tint"], t["red"], t["red"], t["on_red"], icon("x", 20, t["red"], 2.6)
    elif state == "right":
        bg, border, tbg, tfg, mark = t["green_tint"], t["green"], t["green"], t["on_green"], icon("check", 20, t["green"], 2.6)
    else:
        bg, border, tbg, tfg, mark = t["card"], t["outline"], t["track"], t["muted"], ""
    return (f'<div style="display: flex; align-items: center; gap: 14px; min-height: 60px; box-sizing: border-box; padding: 10px 14px; border-radius: 16px; background: {bg}; border: 1.5px solid {border}">'
            f'<span style="width: 32px; height: 32px; flex-shrink: 0; border-radius: 10px; background: {tbg}; color: {tfg}; display: flex; align-items: center; justify-content: center; font-weight: 600">{letter}</span>'
            f'<span style="font-size: 15px; line-height: 1.3; flex: 1">{text}</span>{mark}</div>')


def segments(t, states):
    colors = {"r": t["green"], "w": t["red"], "c": t["current"], "u": t["track"]}
    return (f'<div style="display: grid; grid-template-columns: repeat({len(states)}, minmax(0, 1fr)); gap: 3px">'
            + "".join(f'<span style="height: 5px; border-radius: 3px; background: {colors[s]}"></span>' for s in states) + "</div>")


def practice_question(t):
    right = round_link(t, "#", "Report a problem", "flag", t["muted"])
    body = f'''<div style="display: flex; flex-direction: column; gap: 10px">
{header(t, "Question 4 / 20", back=href(t, "QuitDialog"), right=right)}
  {segments(t, "rrrw" + "u" * 16)}
  </div>
  <h1 style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 26px; line-height: 1.15">{Q}</h1>
  <div style="display: flex; flex-direction: column; gap: 8px">
    {choice(t, "A", CHOICES[0], "wrong")}
    {choice(t, "B", CHOICES[1], "right")}
    {choice(t, "C", CHOICES[2], "idle")}
    {choice(t, "D", CHOICES[3], "idle")}
  </div>
  {card(t, f"""<span style="display: flex; align-items: center; gap: 8px; color: {t["red"]}; font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 20px">{icon("x", 20, t["red"], 2.6)} Incorrect</span>
    <span style="display: flex; flex-direction: column; gap: 4px">{label(t, "Why?", t["green"])}<span style="font-size: 14px; line-height: 1.4">The centre of the vital zone is just behind the front legs on cervids and slightly farther back on bear.</span></span>
    <span style="font-size: 13px; color: {t["muted"]}">Source: <a href="#" style="font-weight: 600; text-decoration: none">ICAF manual (FédéCP, 2020), p. 61 ↗</a></span>""", pad="14px 16px", gap=10, radius=16)}
  <div style="margin-top: auto">{primary(t, "Next", href(t, "PracticeResults"))}</div>'''
    return page(t, "BetterHunter practice question, answered", body, gap=14)


MISSED = [
    ("You are still hunting for deer. Which way of moving is appropriate?", "Walk with the wind at your back so your scent stays behind you",
     "Walk slowly and quietly with frequent pauses"),
    ("For which migratory bird may an electronic call be used?", "Canada goose", "Snow goose"),
    ("How many white-tailed deer may a hunter take per year, barring exceptions?", "One", "Two"),
]


def missed_card(t, q, yours, correct):
    return card(t, f'''<span style="font-weight: 600; font-size: 15px; line-height: 1.3">{q}</span>
      <span style="display: flex; gap: 8px; font-size: 13px; line-height: 1.35; color: {t["red"]}">{icon("x", 16, t["red"], 2.4)}<span>Your answer: {yours}</span></span>
      <span style="display: flex; gap: 8px; font-size: 13px; line-height: 1.35; color: {t["green"]}; font-weight: 600">{icon("check", 16, t["green"], 2.4)}<span>Correct answer: {correct}</span></span>''', pad="14px 16px", gap=6, radius=16)


def results(t, exam):
    if exam:
        color, pct, count = t["red"], 74, "37 / 50"
        title, over = "Mock exam results", "ICAF · Québec"
        verdict = (f'<span style="display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: 999px; border: 1.5px solid {t["red"]}; color: {t["red"]}; font-weight: 600; font-size: 14px">{icon("x", 16, t["red"], 2.4)} Not passed · Pass mark 80%</span>'
                   f'<span style="font-size: 13px; color: {t["muted"]}">Time: 42:18 of 60:00 · 13 questions to review</span>')
        missed = "".join(missed_card(t, *m) for m in MISSED[:2]) + f'<span style="text-align: center; font-size: 13px; color: {t["muted"]}">+ 11 more below</span>'
        again = href(t, "ExamQuestion")
    else:
        color, pct, count = t["green"], 85, "17 / 20"
        title, over = "Practice results", "ICAF · Québec"
        verdict = (f'<span style="display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: 999px; background: {t["green_tint"]}; color: {t["green"]}; font-weight: 600; font-size: 14px">{icon("check", 16, t["green"], 2.4)} Daily goal reached</span>'
                   f'<span style="font-size: 13px; color: {t["muted"]}">3 questions go to "Review my mistakes"</span>')
        missed = "".join(missed_card(t, *m) for m in MISSED)
        again = href(t, "PracticeQuestion")
    inner = (f'<span style="font-family: \'Barlow Condensed\', sans-serif; font-weight: 700; font-size: 44px; line-height: 1; color: {color}">{pct}%</span>'
             f'<span style="font-size: 15px; color: {t["muted"]}">{count}</span>')
    body = f'''{header(t, title, overline=over)}
  <div style="display: flex; flex-direction: column; align-items: center; gap: 10px">
    {ring(t, pct, 150, 12, color, inner)}
    {verdict}
  </div>
  {label(t, "Questions to review", t["muted"], "; margin-top: 4px")}
  <div style="display: flex; flex-direction: column; gap: 8px; flex: 1; min-height: 0; overflow: hidden">{missed}</div>
  <div style="display: flex; gap: 10px">{secondary(t, "Home", href(t, "Main"), "; flex: 1")}{primary(t, "Try again", again, "; flex: 1")}</div>'''
    return page(t, f"BetterHunter {title.lower()}", body, gap=12)


def quit_dialog(t):
    behind = f'''{header(t, "Question 18 / 50", back="#", close=True, overline="Mock exam · ICAF")}
  <div style="height: 5px; border-radius: 3px; background: {t["track"]}"><div style="width: 36%; height: 5px; border-radius: 3px; background: {t["current"]}"></div></div>
  <h1 style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 28px; line-height: 1.15">Which level of government is responsible for managing migratory birds?</h1>
  {choice(t, "A", "The provincial government", "idle")}
  {choice(t, "B", "The municipalities", "idle")}'''
    dialog = f'''<div style="position: absolute; inset: 0; background: {t["dim"]}; display: flex; align-items: center; justify-content: center; padding: 24px">
    <div role="dialog" aria-labelledby="quit-title" style="width: 100%; background: {t["dialog"]}; border-radius: 28px; padding: 24px; display: flex; flex-direction: column; gap: 12px; box-shadow: 0 12px 40px rgba(0,0,0,0.35)">
      <h2 id="quit-title" style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 26px">Quit this test?</h2>
      <p style="margin: 0; font-size: 15px; line-height: 1.4; color: {t["muted"]}">Your progress in this test will be lost.</p>
      <div style="display: flex; justify-content: flex-end; align-items: center; gap: 8px; margin-top: 8px">
        <a href="{href(t, "ExamQuestion")}" style="height: 48px; padding: 0 16px; display: flex; align-items: center; color: {t["link"]}; font-weight: 600; font-size: 15px; text-decoration: none">Cancel</a>
        {primary(t, "Quit", href(t, "ExamTraining"), "; height: 48px; padding: 0 24px")}
      </div>
    </div>
  </div>'''
    return page(t, "BetterHunter quit dialog", behind + "\n  " + dialog, gap=14)


def photo_zoom(t):
    body = f'''<img src="{MULE}" alt="Mystery animal, full screen" style="position: absolute; inset: 0; width: 100%; height: 100%; object-fit: contain; background: #000">
  <a href="{href(t, "PhotoQuiz")}" aria-label="Close" style="position: absolute; left: 16px; top: 16px; width: 44px; height: 44px; border-radius: 999px; background: rgba(0,0,0,0.55); color: #FFFFFF; display: flex; align-items: center; justify-content: center">{icon("close", 22, width=2.2)}</a>
  <div style="position: absolute; left: 0; right: 0; bottom: 0; padding: 40px 20px 24px; background: linear-gradient(180deg, rgba(0,0,0,0) 0%, rgba(0,0,0,0.8) 70%); display: flex; flex-direction: column; gap: 4px; color: #FFFFFF">
    <span style="font-size: 14px; font-weight: 600">Pinch to zoom, drag to look around</span>
    <span style="font-size: 12px; color: #D6DBD5">Photo: Oregon Dept. of Fish &amp; Wildlife, CC BY-SA 2.0</span>
  </div>'''
    return page(t, "BetterHunter photo full screen", body, gap=0, padding="0", extra_style="; background: #000000")


def nothing_to_review(t):
    body = f'''{header(t, "Review my mistakes", back=href(t, "ExamTraining"), overline="ICAF · Québec")}
  <div style="flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 14px; text-align: center; padding: 0 12px">
    <span style="width: 84px; height: 84px; border-radius: 24px; background: {t["green_tint"]}; color: {t["green"]}; display: flex; align-items: center; justify-content: center">{icon("check", 44, width=2.4)}</span>
    <h1 style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 30px">Nothing to review yet</h1>
    <p style="margin: 0; font-size: 15px; line-height: 1.45; color: {t["muted"]}">Questions you miss come here until you answer them right twice in a row.</p>
  </div>
  {primary(t, "Practise the ICAF", href(t, "PracticeQuestion"), icon_name="play")}'''
    return page(t, "BetterHunter nothing to review", body, gap=14)


def recognition(t):
    game = f'''<div style="position: relative; height: 170px; border-radius: 16px; overflow: hidden; background: {t["track"]}">
      <img src="{MOOSE}" alt="Moose" style="width: 100%; height: 100%; object-fit: cover">
      <div style="position: absolute; left: 0; right: 0; bottom: 0; padding: 30px 14px 12px; background: linear-gradient(180deg, rgba(14,22,17,0) 0%, rgba(14,22,17,0.9) 60%); display: flex; flex-direction: column; gap: 2px; color: #FFFFFF">
        <span style="font-size: 11px; font-weight: 600; letter-spacing: 1.2px; text-transform: uppercase; color: #F7A06A">Photo quiz</span>
        <span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 24px">What is it?</span>
      </div>
    </div>
    <span style="font-size: 13px; color: {t["muted"]}">66 animals · 20 per round · look-alikes and protected species</span>
    {meter(t, "Readiness", "40%", 40)}
    <div style="display: flex; gap: 10px">
      {primary(t, "Practice", href(t, "PhotoQuiz"), "; flex: 1; height: 52px", "play")}
      {secondary(t, "Timed round", href(t, "PhotoQuiz"), "; flex: 1; height: 52px", "timer")}
    </div>
    <a href="{href(t, "NothingToReview")}" style="display: flex; align-items: center; gap: 8px; color: {t["link"]}; font-weight: 600; font-size: 14px; text-decoration: none; min-height: 32px">{icon("replay", 16)} Review my mistakes (3)</a>'''

    def later(name, title, sub):
        return (f'<div style="display: flex; align-items: center; gap: 12px; padding: 12px 14px; border-radius: 16px; border: 1.5px dashed {t["outline"]}">'
                f'{tile(t, name)}<span style="display: flex; flex-direction: column; gap: 1px; flex: 1"><span style="font-weight: 600; font-size: 15px">{title}</span>'
                f'<span style="font-size: 13px; color: {t["muted"]}">{sub}</span></span><span style="font-size: 12px; font-weight: 600; color: {t["muted"]}">Coming later</span></div>')
    body = f'''{header(t, "Can you recognise them?", back=href(t, "Main"))}
  <p style="margin: 0; font-size: 15px; color: {t["muted"]}">Identify Canada's game animals</p>
  {card(t, game, pad="12px 12px 16px", gap=12)}
  {later("paw", "Tracks", "Who left these prints?")}
  {later("sound", "Calls", "Which animal makes this sound?")}'''
    return page(t, "BetterHunter recognition games", body, gap=12)


def update_required(t):
    body = f'''<div style="flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; text-align: center; padding: 0 16px">
    <span style="width: 96px; height: 96px; border-radius: 28px; background: {t["orange_tint"]}; color: {t["label"]}; display: flex; align-items: center; justify-content: center">{icon("download", 48, width=2.2)}</span>
    <h1 style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 32px">Update required</h1>
    <p style="margin: 0; font-size: 15px; line-height: 1.45; color: {t["muted"]}">This version of BetterHunter is no longer supported. Update the app to keep practising with up-to-date questions and rules.</p>
  </div>
  {primary(t, "Update", "#")}
  <div style="display: flex; align-items: center; justify-content: center; gap: 8px; color: {t["muted"]}; font-size: 12px">{buck(20, t["text"], t["orange"])} BetterHunter</div>'''
    return page(t, "BetterHunter update required", body, gap=14, padding="24px 20px 24px")


def passed_exam(t):
    changes = "".join(
        f'<div style="display: flex; gap: 12px; align-items: flex-start">{tile(t, ic, tone, 36, 10)}<span style="font-size: 14px; line-height: 1.4; padding-top: 2px">{text}</span></div>'
        for ic, tone, text in [
            ("eye", "orange", "Home focuses on identifying game: <b style=\"font-weight: 600\">What is it?</b> becomes your daily practice."),
            ("school", "green", "Your exam practice stays in <b style=\"font-weight: 600\">Exam training</b>, to refresh the rules any time."),
            ("user", "green", "You become a <b style=\"font-weight: 600\">New hunter</b>; you can change it back in Settings."),
        ])
    body = f'''{header(t, "", back=href(t, "ExamTraining"))}
  <div style="display: flex; flex-direction: column; align-items: center; gap: 12px; text-align: center; padding: 8px 8px 0">
    <span style="width: 96px; height: 96px; border-radius: 28px; background: {t["orange_tint"]}; color: {t["label"]}; display: flex; align-items: center; justify-content: center">{icon("trophy", 50, width=2)}</span>
    {label(t, "Real exam")}
    <h1 style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 32px; line-height: 1.05">Did you pass your hunter course (ICAF)?</h1>
    <p style="margin: 0; font-size: 15px; line-height: 1.45; color: {t["muted"]}">Congratulations if so! Here's what changes in BetterHunter:</p>
  </div>
  {card(t, changes, pad="16px", gap=14, radius=18)}
  <div style="margin-top: auto; display: flex; flex-direction: column; gap: 6px">
    {primary(t, "Yes, I passed", href(t, "MainNewHunter"), icon_name="check")}
    <a href="{href(t, "ExamTraining")}" style="height: 48px; display: flex; align-items: center; justify-content: center; color: {t["link"]}; font-weight: 600; font-size: 15px; text-decoration: none">Not yet</a>
  </div>'''
    return page(t, "BetterHunter passed the real exam", body, gap=16)


def main_new_hunter(t, passed=True):
    pill = (f'<span style="display: flex; border: 1px solid {t["outline"]}; border-radius: 999px; padding: 3px; background: {t["card"] if t["name"] == "light" else "transparent"}">'
            f'<span style="color: {t["muted"]}; font: 600 13px Barlow, sans-serif; height: 38px; min-width: 44px; border-radius: 999px; display: flex; align-items: center; justify-content: center">FR</span>'
            f'<span style="background: {t["green_tint"] if t["name"] == "light" else t["track"]}; color: {t["text"]}; font: 600 13px Barlow, sans-serif; height: 38px; min-width: 44px; border-radius: 999px; display: flex; align-items: center; justify-content: center">EN</span></span>')
    banner = f'''<section style="display: flex; align-items: center; gap: 12px; padding: 12px 14px; border-radius: 16px; background: {t["sel"]}; border: 1px solid {t["warm_border"]}">
    {tile(t, "trophy", "orange", 40, 10)}
    <span style="display: flex; flex-direction: column; gap: 1px; flex: 1"><span style="font-weight: 600; font-size: 15px">Hunter course passed. Welcome, new hunter!</span><span style="font-size: 13px; color: {t["muted"]}">Now sharpen your eye in the field.</span></span>
    <a href="#" aria-label="Dismiss" style="width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; color: {t["muted"]}; margin-right: -10px">{icon("close", 18)}</a>
  </section>'''
    exam_tile = f'''<a href="{href(t, "ExamTraining")}" style="background: {t["card"]}; border: 1px solid {t["border"]}; border-radius: 20px; padding: 16px; display: flex; flex-direction: column; gap: 10px; color: {t["text"]}; text-decoration: none; box-shadow: {t["shadow"]}">
      {tile(t, "school")}
      <span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 21px; line-height: 1.1">Exam training</span>
      <span style="font-size: 13px; color: {t["muted"]}; line-height: 1.35">Refresh the rules any time</span>
      <span style="display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; color: {t["green"]}; margin-top: auto">{icon("check", 14, t["green"], 2.6)} ICAF passed</span>
    </a>'''
    skills_tile = f'''<a href="{href(t, "FieldSkills")}" style="background: {t["card"]}; border: 1px solid {t["border"]}; border-radius: 20px; padding: 16px; display: flex; flex-direction: column; gap: 10px; color: {t["text"]}; text-decoration: none; box-shadow: {t["shadow"]}">
      {tile(t, "target")}
      <span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 21px; line-height: 1.1">Field skills</span>
      <span style="font-size: 13px; color: {t["muted"]}; line-height: 1.35">Shot placement, tracking, safety</span>
      {meter(t, "Readiness", "64%", 64)}
    </a>'''
    ring_html = ring(t, 25, 84, 8, t["orange"], f'<span style="font-family: \'Barlow Condensed\', sans-serif; font-weight: 700; font-size: 28px; line-height: 1">5</span><span style="font-size: 11px; color: {t["muted"]}">of 20</span>')
    body = f'''<header style="display: flex; align-items: center; justify-content: space-between; height: 48px; flex-shrink: 0">
    <div style="display: flex; align-items: center; gap: 10px">{buck(34, t["text"], t["orange"])}<span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 26px; letter-spacing: 0.2px">BetterHunter</span></div>
    <div style="display: flex; align-items: center; gap: 8px">{pill}{round_link(t, href(t, "Settings"), "Settings", "gear", t["muted"])}</div>
  </header>
  {banner if passed else ""}
  {card(t, f'''<div style="display: flex; align-items: center; gap: 16px">{ring_html}
      <div style="display: flex; flex-direction: column; gap: 4px; min-width: 0">
        {label(t, "Today")}
        <span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 22px; line-height: 1.15">15 animals to your daily goal</span>
        <span style="display: flex; align-items: center; gap: 6px; font-size: 13px; color: {t["muted"]}">{icon("flame", 14, t["orange"], 2.2)} 6-day streak</span>
      </div></div>
    {primary(t, "Play What is it?", href(t, "PhotoQuiz"), "; height: 52px", "play")}''', pad="18px", gap=16)}
  <div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px">
    <a href="{href(t, "Recognition")}" style="background: {t["card"]}; border: 1px solid {t["border"]}; border-radius: 20px; padding: 16px; display: flex; flex-direction: column; gap: 10px; color: {t["text"]}; text-decoration: none; box-shadow: {t["shadow"]}">
      {tile(t, "eye", "orange")}
      <span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 21px; line-height: 1.1">What is it?</span>
      <span style="font-size: 13px; color: {t["muted"]}; line-height: 1.35">Identify Canada's game animals</span>
      {meter(t, "Readiness", "40%", 40)}
    </a>
    {exam_tile if passed else skills_tile}
  </div>
  <section style="position: relative; flex: 1; min-height: 0; border-radius: 22px; overflow: hidden; background: {t["track"]}">
    <img src="/_blob/876ea656307e18bff50cd003ae123e81" alt="Photo of the day, to identify" style="position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover">
    <div style="position: absolute; left: 0; right: 0; bottom: 0; padding: 36px 16px 14px; background: linear-gradient(180deg, rgba(14,22,17,0) 0%, rgba(14,22,17,0.9) 60%); display: flex; flex-direction: column; gap: 2px; color: #FFFFFF">
      <span style="font-size: 11px; font-weight: 600; letter-spacing: 1.2px; text-transform: uppercase; color: #F7A06A">Photo of the day</span>
      <span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 24px">What is it?</span>
    </div>
  </section>'''
    return page(t, "BetterHunter home, new hunter" + ("" if passed else " (from onboarding)"), body, gap=14, padding="20px 20px 24px")


def hunt_with(t, new_hunter=False):
    fl = "NewHunter" if new_hunter else ""
    def option(name, title, sub, selected=False):
        bg, border = (t["sel"], t["orange"]) if selected else (t["card"], t["outline"])
        if selected:
            mark = (f'<span style="width: 22px; height: 22px; flex-shrink: 0; border-radius: 6px; background: {t["orange"]}; color: {t["on_orange"]}; '
                    f'display: flex; align-items: center; justify-content: center">{icon("check", 14, width=3)}</span>')
        else:
            mark = f'<span style="width: 22px; height: 22px; flex-shrink: 0; box-sizing: border-box; border-radius: 6px; border: 2px solid {t["box"]}"></span>'
        return (f'<label style="display: flex; align-items: center; gap: 14px; min-height: 84px; box-sizing: border-box; padding: 14px 16px; border-radius: 18px; '
                f'background: {bg}; border: 1.5px solid {border}">'
                f'<input type="checkbox"{" checked" if selected else ""} style="position: absolute; opacity: 0; width: 1px; height: 1px">'
                f'{tile(t, name, "orange" if selected else "green")}<span style="display: flex; flex-direction: column; gap: 2px; flex: 1; min-width: 0">'
                f'<span style="font-family: \'Barlow Condensed\', sans-serif; font-weight: 600; font-size: 21px; line-height: 1.1">{title}</span>'
                f'<span style="font-size: 13px; line-height: 1.35; color: {t["muted"]}">{sub}</span></span>{mark}</label>')
    body = f'''<header style="display: flex; align-items: center; height: 48px; flex-shrink: 0">{round_link(t, href(t, "HunterProfile" + fl), "Back", "back")}</header>
  <div style="display: flex; flex-direction: column; gap: 8px">
    {label(t, "Next question")}
    <h1 style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 34px; line-height: 1.05">What do you hunt with?</h1>
    <p style="margin: 0; font-size: 15px; line-height: 1.4; color: {t["muted"]}">Pick all that apply. It decides which hunter course and rules you'll see.</p>
  </div>
  <fieldset style="border: 0; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 12px">
    <legend style="position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0)">What do you hunt with?</legend>
    {option("target", "Firearm", "Rifle, shotgun or muzzleloader", selected=True)}
    {option("bow", "Bow", "Compound, recurve or longbow")}
    {option("crossbow", "Crossbow", "Hunting with a crossbow")}
  </fieldset>
  <div style="margin-top: auto">{primary(t, "Continue", href(t, "HuntWhere" + fl))}</div>'''
    return page(t, "BetterHunter onboarding: what do you hunt with", body, gap=16, padding="12px 20px 24px")


# ------------------------------------------------------------------------------------- flow: new hunter, first seasons

RACK = ('<svg width="24" height="24" viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" '
        'stroke-linejoin="round" aria-hidden="true"><path d="M13 17c-3-1.5-6-3.5-7.5-7.5"/><path d="M9.5 13.5c-.5-2.5 0-4.5 1.5-6.5"/>'
        '<path d="M19 17c3-1.5 6-3.5 7.5-7.5"/><path d="M22.5 13.5c.5-2.5 0-4.5-1.5-6.5"/>'
        '<path d="M12.5 17.5c0-1.5 2-2 3.5-2s3.5.5 3.5 2c0 4-1.5 8-3 10.5-.3.5-.7.5-1 0-1.5-2.5-3-6.5-3-10.5z"/></svg>')


def hunter_profile_new(t):
    # The onboarding's first question (hand-made in HunterProfile.dc.html) with "New hunter" picked.
    def option(glyph, title, sub, selected=False):
        bg, border = (t["sel"], t["orange"]) if selected else (t["card"], t["outline"])
        tbg, tfg = (t["orange_tint"], t["label"]) if selected else (t["green_tint"], t["green"])
        if selected:
            mark = (f'<span style="width: 22px; height: 22px; flex-shrink: 0; border-radius: 999px; background: {t["orange"]}; color: {t["on_orange"]}; '
                    f'display: flex; align-items: center; justify-content: center">{icon("check", 14, width=3)}</span>')
        else:
            mark = f'<span style="width: 22px; height: 22px; flex-shrink: 0; box-sizing: border-box; border-radius: 999px; border: 2px solid {t["box"]}"></span>'
        return (f'<label style="display: flex; align-items: center; gap: 14px; min-height: 84px; box-sizing: border-box; padding: 14px 16px; border-radius: 18px; '
                f'background: {bg}; border: 1.5px solid {border}">'
                f'<input type="radio" name="profile"{" checked" if selected else ""} style="position: absolute; opacity: 0; width: 1px; height: 1px">'
                f'<span style="width: 44px; height: 44px; flex-shrink: 0; border-radius: 12px; background: {tbg}; color: {tfg}; display: flex; align-items: center; justify-content: center">{glyph}</span>'
                f'<span style="display: flex; flex-direction: column; gap: 2px; flex: 1; min-width: 0">'
                f'<span style="font-family: \'Barlow Condensed\', sans-serif; font-weight: 600; font-size: 21px; line-height: 1.1">{title}</span>'
                f'<span style="font-size: 13px; line-height: 1.35; color: {t["muted"]}">{sub}</span></span>{mark}</label>')
    pill = (f'<span style="display: flex; border: 1px solid {t["outline"]}; border-radius: 999px; padding: 3px; background: {t["card"] if t["name"] == "light" else "transparent"}">'
            f'<span style="color: {t["muted"]}; font: 600 13px Barlow, sans-serif; height: 38px; min-width: 44px; border-radius: 999px; display: flex; align-items: center; justify-content: center">FR</span>'
            f'<span style="background: {t["green_tint"] if t["name"] == "light" else t["track"]}; color: {t["text"]}; font: 600 13px Barlow, sans-serif; height: 38px; min-width: 44px; border-radius: 999px; display: flex; align-items: center; justify-content: center">EN</span></span>')
    body = f'''<header style="display: flex; align-items: center; justify-content: space-between; height: 48px; flex-shrink: 0">
    <div style="display: flex; align-items: center; gap: 10px">{buck(34, t["text"], t["orange"])}<span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 26px; letter-spacing: 0.2px">BetterHunter</span></div>
    {pill}
  </header>
  <div style="display: flex; flex-direction: column; gap: 8px; margin-top: 6px">
    {label(t, "Welcome")}
    <h1 style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 34px; line-height: 1.05">What kind of hunter are you?</h1>
    <p style="margin: 0; font-size: 15px; line-height: 1.4; color: {t["muted"]}">We'll suggest the right exams and practice for you. You can change this later.</p>
  </div>
  <fieldset style="border: 0; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 12px">
    <legend style="position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0)">What kind of hunter are you?</legend>
    {option(icon("book", 24), "Working on it", "Preparing for my licence or hunter course")}
    {option(icon("sprout", 24), "New hunter", "Licensed, a few seasons so far", selected=True)}
    {option(RACK, "Experienced hunter", "Many seasons in the field")}
  </fieldset>
  <div style="margin-top: auto; display: flex; flex-direction: column; gap: 10px">
    {primary(t, "Continue", href(t, "HuntWithNewHunter"))}
    <p style="margin: 0; font-size: 12px; line-height: 1.4; color: {t["faint"]}; text-align: center">Saved on this device only. No account needed.</p>
  </div>'''
    return page(t, "BetterHunter onboarding: what kind of hunter, new hunter", body, gap=18, padding="20px 20px 24px")


SKILLS = [
    ("target", "Shot placement", "Vital zones, angles, distance", 62),
    ("paw", "After the shot", "Tracking, recovery, tagging", 48),
    ("shield", "Safe firearm handling", "Carrying, crossing, zones of fire", 90),
    ("book", "Rules in the field", "Hunting hours, tags, transport", 82),
    ("first_aid", "Survival and first aid", "Lost, cold, hurt in the woods", 71),
]


def field_skills(t):
    mix = f'''<div style="display: flex; gap: 12px; align-items: center">
      {tile(t, "flame", "orange")}
      <span style="display: flex; flex-direction: column; gap: 2px; flex: 1; min-width: 0">
        <span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 21px; line-height: 1.1">Today's mix</span>
        <span style="font-size: 13px; line-height: 1.35; color: {t["muted"]}">10 questions, mostly from your weakest skills</span>
      </span>
    </div>
    {primary(t, "Start the mix", href(t, "SkillQuestion"), "; height: 52px", "play")}'''

    def skill(i, ic, title, sub, pct):
        return (f'<a href="{href(t, "SkillQuestion")}" style="display: flex; align-items: center; gap: 12px; min-height: 64px; padding: 8px 0; '
                f'{"border-top: 1px solid " + t["border"] + ";" if i else ""} color: {t["text"]}; text-decoration: none">{tile(t, ic, size=40, radius=11)}'
                f'<span style="display: flex; flex-direction: column; gap: 5px; flex: 1; min-width: 0">'
                f'<span style="display: flex; justify-content: space-between; gap: 8px"><span style="font-weight: 600; font-size: 15px">{title}</span>'
                f'<span style="font-size: 13px; font-weight: 600; color: {t["green"]}">{pct}%</span></span>'
                f'<span style="font-size: 12px; color: {t["muted"]}">{sub}</span>'
                f'<span style="height: 5px; border-radius: 3px; background: {t["track"]}; display: block"><span style="display: block; width: {pct}%; height: 5px; border-radius: 3px; background: {t["bar"]}"></span></span>'
                f'</span>{icon("chevron", 18, t["muted"])}</a>')
    skills = "".join(skill(i, *sk) for i, sk in enumerate(SKILLS))
    body = f'''{header(t, "Field skills", back=href(t, "HomeNewHunter"))}
  <p style="margin: 0; font-size: 15px; line-height: 1.4; color: {t["muted"]}">Sharpen what matters in the field, one skill at a time.</p>
  {card(t, mix, pad="18px", gap=14)}
  <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 2px">{label(t, "Skills", t["muted"])}<a href="{href(t, "NothingToReview")}" style="display: flex; align-items: center; gap: 6px; color: {t["link"]}; font-weight: 600; font-size: 14px; text-decoration: none; min-height: 32px">{icon("replay", 15)} My mistakes (2)</a></div>
  {card(t, skills, pad="4px 16px", gap=0)}'''
    return page(t, "BetterHunter field skills", body, gap=12)


def skill_question(t):
    right = round_link(t, "#", "Report a problem", "flag", t["muted"])
    body = f'''<div style="display: flex; flex-direction: column; gap: 10px">
{header(t, "Question 4 / 10", back=href(t, "FieldSkills"), right=right, overline="Field skills · Shot placement", close=True)}
  {segments(t, "rrwr" + "u" * 6)}
  </div>
  <h1 style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 26px; line-height: 1.15">{Q}</h1>
  <div style="display: flex; flex-direction: column; gap: 8px">
    {choice(t, "A", CHOICES[0], "idle")}
    {choice(t, "B", CHOICES[1], "right")}
    {choice(t, "C", CHOICES[2], "idle")}
    {choice(t, "D", CHOICES[3], "idle")}
  </div>
  {card(t, f"""<span style="display: flex; align-items: center; gap: 8px; color: {t["green"]}; font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 20px">{icon("check", 20, t["green"], 2.6)} Correct</span>
    <span style="display: flex; flex-direction: column; gap: 4px">{label(t, "In the field", t["green"])}<span style="font-size: 14px; line-height: 1.4">Wait for a broadside bear to step its near front leg forward, then aim slightly behind the shoulder.</span></span>
    <span style="font-size: 13px; color: {t["muted"]}">Source: <a href="#" style="font-weight: 600; text-decoration: none">ICAF manual (FédéCP, 2020), p. 61 ↗</a></span>""", pad="14px 16px", gap=10, radius=16)}
  <div style="margin-top: auto">{primary(t, "Next", href(t, "SkillResults"))}</div>'''
    return page(t, "BetterHunter field skills question, answered", body, gap=14)


def skill_results(t):
    inner = (f'<span style="font-family: \'Barlow Condensed\', sans-serif; font-weight: 700; font-size: 44px; line-height: 1; color: {t["green"]}">80%</span>'
             f'<span style="font-size: 15px; color: {t["muted"]}">8 / 10</span>')
    gain = card(t, f'''<div style="display: flex; align-items: center; gap: 12px">{tile(t, "target", size=40, radius=11)}
      <span style="display: flex; flex-direction: column; gap: 6px; flex: 1; min-width: 0">
        <span style="display: flex; justify-content: space-between"><span style="font-weight: 600; font-size: 15px">Shot placement</span><span style="font-size: 13px; font-weight: 600; color: {t["green"]}">62% → 70%</span></span>
        <span style="position: relative; height: 6px; border-radius: 3px; background: {t["track"]}; display: block"><span style="position: absolute; left: 0; top: 0; width: 70%; height: 6px; border-radius: 3px; background: {t["green_tint"]}; border: 1px solid {t["bar"]}; box-sizing: border-box"></span><span style="position: absolute; left: 0; top: 0; width: 62%; height: 6px; border-radius: 3px; background: {t["bar"]}"></span></span>
      </span></div>''', pad="14px 16px", gap=0, radius=18)
    body = f'''{header(t, "Shot placement", overline="Field skills · results")}
  <div style="display: flex; flex-direction: column; align-items: center; gap: 10px">
    {ring(t, 80, 150, 12, t["green"], inner)}
    <span style="display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: 999px; background: {t["green_tint"]}; color: {t["green"]}; font-weight: 600; font-size: 14px">{icon("check", 16, t["green"], 2.4)} Skill getting stronger</span>
  </div>
  {gain}
  {label(t, "Questions to review", t["muted"], "; margin-top: 4px")}
  <div style="display: flex; flex-direction: column; gap: 8px">{missed_card(t, "A wounded deer runs off. What should you do first?", "Follow it right away before the trail goes cold", "Mark where it stood and wait before tracking")}</div>
  <span style="font-size: 13px; color: {t["muted"]}; text-align: center">+ 1 more below · both go to "My mistakes"</span>
  <div style="margin-top: auto; display: flex; gap: 10px">{secondary(t, "Skills", href(t, "FieldSkills"), "; flex: 1")}{primary(t, "Next: After the shot", href(t, "SkillQuestion"), "; flex: 1.5")}</div>'''
    return page(t, "BetterHunter field skills results", body, gap=12)


SCREENS = {
    "Settings": settings, "About": about, "ExamTraining": exam_training, "PracticeQuestion": practice_question,
    "PracticeResults": lambda t: results(t, exam=False), "ExamResults": lambda t: results(t, exam=True),
    "QuitDialog": quit_dialog, "PhotoZoom": photo_zoom, "NothingToReview": nothing_to_review,
    "Recognition": recognition, "UpdateRequired": update_required, "PassedExam": passed_exam,
    "MainNewHunter": main_new_hunter, "HuntWith": hunt_with, "HuntWhere": hunt_where, "Provinces": provinces,
    # Flow: new hunter, first seasons
    "HunterProfileNewHunter": hunter_profile_new, "HuntWithNewHunter": lambda t: hunt_with(t, new_hunter=True),
    "HuntWhereNewHunter": lambda t: hunt_where(t, new_hunter=True), "HomeNewHunter": lambda t: main_new_hunter(t, passed=False),
    "FieldSkills": field_skills, "SkillQuestion": skill_question, "SkillResults": skill_results,
}

if __name__ == "__main__":
    for name, fn in SCREENS.items():
        for t in (DARK, LIGHT):
            (OUT / f"{name}{t['suffix']}.dc.html").write_text(fn(t), encoding="utf-8")
    print(f"{len(SCREENS) * 2} files")
