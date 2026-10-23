# Buck logo variants on the 64-unit grid, shared by the preview render and the canvas board.
ANTLER_L = ["M27 29C23 23 19.5 17 19.5 8", "M21.2 18.5L13.5 14", "M20 12.5L25.5 7.5"]
ANTLER_R = ["M37 29C41 23 44.5 17 44.5 8", "M42.8 18.5L50.5 14", "M44 12.5L38.5 7.5"]
ANTLER_R_UNEVEN = ["M37 29C41 23 44.5 17 45.5 5", "M42.8 18.5L50.5 14", "M44 12.5L38.5 7.5", "M44.9 10.5L50.5 7.5"]
EARS = "M22.5 31.5L11 27.5L20 36.5Z M41.5 31.5L53 27.5L44 36.5Z"
HEAD = "M21 30H43L38.5 49C36.5 56.5 27.5 56.5 25.5 49Z"
EYES = " M25.6 38.5a1.9 1.9 0 1 0 3.8 0a1.9 1.9 0 1 0 -3.8 0Z M34.6 38.5a1.9 1.9 0 1 0 3.8 0a1.9 1.9 0 1 0 -3.8 0Z"
NOSE = " M29.2 51.5a2.8 2.2 0 1 0 5.6 0a2.8 2.2 0 1 0 -5.6 0Z"
BLAZE = "M27 30H37L32 36.8Z"
SUN = "M27.5 16a4.5 4.5 0 1 0 9 0a4.5 4.5 0 1 0 -9 0Z"

def mark(variant, face, accent):
    right = ANTLER_R_UNEVEN if variant in ("uneven", "signature") else ANTLER_R
    cut = EYES + NOSE
    antlers = "".join(f'<path d="{d}"/>' for d in ANTLER_L + right)
    out = (f'<g fill="none" stroke="{accent}" stroke-width="3.6" stroke-linecap="round" stroke-linejoin="round">{antlers}</g>'
           f'<path fill="{face}" d="{EARS}"/><path fill="{face}" fill-rule="evenodd" d="{HEAD}{cut}"/>')
    if variant in ("blaze", "signature"):
        out += f'<path fill="{accent}" d="{BLAZE}"/>'
    if variant == "dawn":
        out = f'<path fill="{accent}" d="{SUN}"/>' + out
    return out

VARIANTS = [
    ("current", "Current", "Concept A as it is in the app today, for comparison."),
    ("blaze", "Blaze patch", "A blaze-orange triangle on the forehead, like the hunter-orange patch worn in the field. Ties the mark to the app's one accent colour and to hunting safety."),
    ("uneven", "Uneven rack", "Three points on one side, four on the other, as real bucks often carry. A small, natural signature that makes the silhouette ours."),
    ("signature", "Blaze + uneven rack", "Variants 1 and 2 together: the blaze-orange forehead patch and the 3-and-4-point rack. Two small signature details that together make the buck clearly ours."),
    ("dawn", "First light", "A blaze-orange sun rising between the antlers: first light, when the hunt starts. Adds a simple round shape that stays readable at small sizes."),
]
