# Generates the "Logo concepts" artboard for the BetterHunter design canvas.
import sys
OUT = sys.argv[1]

ORANGE = "#F27B2B"; ORANGE_L = "#E8691E"; CREAM = "#F5F2EA"; INK = "#17201A"
GREEN_ICON = "#1F4229"; DARK = "#0E1611"; LIGHT = "#F5F2EA"

def buck(fg, accent, cut):
    """A: geometric front-facing buck head, blaze-orange antlers."""
    return f'''<g fill="none" stroke="{accent}" stroke-width="3.6" stroke-linecap="round" stroke-linejoin="round">
<path d="M27 29C23 23 19.5 17 19.5 8"/><path d="M21.2 18.5L13.5 14"/><path d="M20 12.5L25.5 7.5"/>
<path d="M37 29C41 23 44.5 17 44.5 8"/><path d="M42.8 18.5L50.5 14"/><path d="M44 12.5L38.5 7.5"/></g>
<path fill="{fg}" d="M22.5 31.5L11 27.5L20 36.5Z"/><path fill="{fg}" d="M41.5 31.5L53 27.5L44 36.5Z"/>
<path fill="{fg}" d="M21 30H43L38.5 49C36.5 56.5 27.5 56.5 25.5 49Z"/>
<circle cx="27.5" cy="38.5" r="1.9" fill="{cut}"/><circle cx="36.5" cy="38.5" r="1.9" fill="{cut}"/>
<ellipse cx="32" cy="51.5" rx="2.8" ry="2.2" fill="{cut}"/>'''

def track(fg, accent, cut):
    """B: deer track (two hoof toes) with orange dew claws."""
    return f'''<path fill="{fg}" d="M29.5 11C23 15 19.5 27 20.5 38C21.3 46 27.5 49 29.5 44.5C30.5 42 30.5 30 29.5 11Z"/>
<path fill="{fg}" d="M34.5 11C41 15 44.5 27 43.5 38C42.7 46 36.5 49 34.5 44.5C33.5 42 33.5 30 34.5 11Z"/>
<circle cx="23" cy="55" r="3.2" fill="{accent}"/><circle cx="41" cy="55" r="3.2" fill="{accent}"/>'''

def monogram(fg, accent, cut):
    """C: condensed B with one antler rising from it."""
    return f'''<text x="32" y="56" text-anchor="middle" font-family="Barlow Condensed, sans-serif" font-weight="700" font-size="50" fill="{fg}">B</text>
<g fill="none" stroke="{accent}" stroke-width="3.6" stroke-linecap="round" stroke-linejoin="round">
<path d="M40 17C42 12 45.5 8.5 50 6.5"/><path d="M44 11.5L42.5 5"/><path d="M47.5 8.5L53 11.5"/></g>'''

def crest(fg, accent, cut):
    """D: the current antler mark, refined, in a field patch with a ridge line."""
    return f'''<circle cx="32" cy="32" r="27" fill="none" stroke="{fg}" stroke-width="3"/>
<path d="M9 41L20 33L26 37L35 29L55 41" fill="none" stroke="{fg}" stroke-width="2.6" stroke-linejoin="round" stroke-linecap="round" opacity="0.55"/>
<g transform="translate(14 9) scale(1.12)" fill="none" stroke="{accent}" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round">
<path d="M13 17c-3-1.5-6-3.5-7.5-7.5"/><path d="M9.5 13.5c-.5-2.5 0-4.5 1.5-6.5"/><path d="M19 17c3-1.5 6-3.5 7.5-7.5"/><path d="M22.5 13.5c.5-2.5 0-4.5-1.5-6.5"/>
<path d="M12.5 17.5c0-1.5 2-2 3.5-2s3.5.5 3.5 2c0 4-1.5 8-3 10.5-.3.5-.7.5-1 0-1.5-2.5-3-6.5-3-10.5z"/></g>'''

CONCEPTS = [
    ("A", "Blaze buck", "A front-facing buck, simplified to flat shapes, with blaze-orange antlers. Reads as a deer at any size; keeps the current icon's subject.", buck),
    ("B", "Track", "A deer's hoof print with orange dew claws. Ties to identification and tracking; very distinctive and works at 16 px.", track),
    ("C", "Tine monogram", "A condensed B with one antler rising from it. A typographic mark that pairs with the wordmark; needs the name nearby to read as hunting.", monogram),
    ("D", "Field patch", "Today's antler mark, refined, inside a round patch with a ridge line, like a club crest. Familiar, but busier when small.", crest),
]

def svg(fn, size, fg, accent, cut, label=None):
    aria = f'role="img" aria-label="{label}"' if label else 'aria-hidden="true"'
    return f'<svg width="{size}" height="{size}" viewBox="0 0 64 64" {aria}>{fn(fg, accent, cut)}</svg>'

def lockup(fn, bg, fg, accent, cut, text):
    return f'''<div style="height: 132px; border-radius: 18px; background: {bg}; border: 1px solid {'#24352A' if bg == DARK else '#E6E1D5'}; display: flex; align-items: center; justify-content: center; gap: 10px; color: {text}">
{svg(fn, 46, fg, accent, cut)}<span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 30px; letter-spacing: 0.2px">BetterHunter</span></div>'''

rows = []
for letter, name, why, fn in CONCEPTS:
    rows.append(f'''  <section style="display: grid; grid-template-columns: 250px 132px 1fr 1fr 150px; gap: 20px; align-items: center; padding: 20px 0; border-top: 1px solid #24352A">
    <div style="display: flex; flex-direction: column; gap: 6px">
      <span style="font-size: 12px; font-weight: 600; letter-spacing: 1.2px; text-transform: uppercase; color: #F7A06A">Concept {letter}</span>
      <span style="font-family: 'Barlow Condensed', sans-serif; font-weight: 600; font-size: 26px; line-height: 1.05">{name}</span>
      <span style="font-size: 13px; line-height: 1.4; color: #A9B5AB">{why}</span>
    </div>
    <div style="width: 132px; height: 132px; border-radius: 30px; background: {GREEN_ICON}; display: flex; align-items: center; justify-content: center">{svg(fn, 96, CREAM, ORANGE, GREEN_ICON, f"Concept {letter} app icon")}</div>
    {lockup(fn, DARK, CREAM, ORANGE, DARK, "#EEF1EA")}
    {lockup(fn, LIGHT, INK, ORANGE_L, LIGHT, INK)}
    <div style="display: flex; align-items: flex-end; gap: 14px; height: 132px; box-sizing: border-box; padding-bottom: 20px">
      <span style="width: 48px; height: 48px; border-radius: 12px; background: {GREEN_ICON}; display: flex; align-items: center; justify-content: center">{svg(fn, 36, CREAM, ORANGE, GREEN_ICON)}</span>
      <span style="width: 32px; height: 32px; border-radius: 8px; background: {GREEN_ICON}; display: flex; align-items: center; justify-content: center">{svg(fn, 24, CREAM, ORANGE, GREEN_ICON)}</span>
      {svg(fn, 20, CREAM, ORANGE, DARK)}
    </div>
  </section>''')

html = f'''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>BetterHunter logo concepts</title>
<script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Barlow+Condensed:wght@600;700&amp;family=Barlow:wght@400;500;600&amp;display=swap" rel="stylesheet">
<style>
body{{margin:0}}
a{{color:#8FD19E}}a:hover{{color:#B5E4BF}}
</style>
</helmet>
<div style="width: 1280px; height: 1000px; box-sizing: border-box; background: #0E1611; color: #EEF1EA; font-family: Barlow, system-ui, sans-serif; padding: 40px 48px; display: flex; flex-direction: column; overflow: hidden">
  <header style="display: grid; grid-template-columns: 250px 132px 1fr 1fr 150px; gap: 20px; align-items: end; padding-bottom: 14px">
    <h1 style="margin: 0; font-family: 'Barlow Condensed', sans-serif; font-weight: 700; font-size: 34px; line-height: 1">Logo concepts</h1>
    <span style="font-size: 12px; font-weight: 600; letter-spacing: 1.2px; text-transform: uppercase; color: #A9B5AB">App icon</span>
    <span style="font-size: 12px; font-weight: 600; letter-spacing: 1.2px; text-transform: uppercase; color: #A9B5AB">On dark</span>
    <span style="font-size: 12px; font-weight: 600; letter-spacing: 1.2px; text-transform: uppercase; color: #A9B5AB">On light</span>
    <span style="font-size: 12px; font-weight: 600; letter-spacing: 1.2px; text-transform: uppercase; color: #A9B5AB">48 · 32 · 20 px</span>
  </header>
{chr(10).join(rows)}
</div>
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{{"$preview":{{"width":1280,"height":1000}}}}'>
class Component extends DCLogic {{
renderVals() {{
return {{}};
}}
}}
</script>
</body>
</html>
'''
open(OUT, "w").write(html)
