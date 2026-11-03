#!/usr/bin/env python3
"""Arte vectorial rasterizado nativamente a PNG para Perfil de Gym Breaker.

No usa capturas, reescalado de miniaturas ni recursos externos.
Genera fondos 1080x1920 y overlays/iconos RGBA transparentes.
La geometría se rasteriza directamente a la resolución final.
"""
from __future__ import annotations
from pathlib import Path
import hashlib
import math
import random
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "app/src/main/res/drawable-nodpi"
MASTER = ROOT / "assets_master/profile"
DEST.mkdir(parents=True, exist_ok=True)
MASTER.mkdir(parents=True, exist_ok=True)
RNG = random.Random(843261)
VIOLET = (170, 104, 255)
CYAN = (76, 197, 249)
GOLD = (246, 197, 101)


def output(name: str, img: Image.Image) -> None:
    target = DEST / (name + ".png")
    img.save(target, format="PNG", optimize=True)
    img.save(MASTER / (name + ".png"), format="PNG", optimize=True)
    print(f"{target.relative_to(ROOT)}  {img.width}x{img.height}  {img.mode}  {target.stat().st_size} B")


def background(name: str, seed: int, accent: tuple[int, int, int]) -> None:
    w, h = 1080, 1920
    rand = random.Random(seed)
    im = Image.new("RGB", (w, h))
    px = im.load()
    # Nightfall gradient computed at native resolution, not scaled.
    for y in range(h):
        t = y / h
        mist = math.exp(-((t - .21) / .21) ** 2)
        for x in range(w):
            side = abs((x - w / 2) / (w / 2))
            aura = max(0, 1 - ((x - .77 * w) / (w * .65)) ** 2) * mist
            px[x, y] = (
                min(255, int(5 + 16 * mist + 12 * (1 - side) + accent[0] * .105 * aura)),
                min(255, int(7 + 6 * mist + accent[1] * .08 * aura)),
                min(255, int(24 + 35 * mist + 17 * (1 - side) + accent[2] * .18 * aura)),
            )
    # Ghostly stars, scattered mostly toward top of scene.
    stars = Image.new("RGBA", (w, h))
    sd = ImageDraw.Draw(stars)
    for _ in range(430):
        x, y = rand.randrange(24, w-24), rand.randrange(30, 1300)
        radius = rand.choice([1, 1, 1, 2, 2, 3])
        sd.ellipse((x-radius, y-radius, x+radius, y+radius),
                   fill=(*accent, rand.randint(30, 110)))
    im = Image.alpha_composite(im.convert("RGBA"), stars)
    # Layered arcane light: blurred streaks and ring halos in upper region.
    energy = Image.new("RGBA", (w, h))
    ed = ImageDraw.Draw(energy)
    for i in range(12):
        y = int((i + 1) * 58 + rand.randrange(-19, 24))
        left = rand.randrange(-200, 380)
        ed.arc((left, y, left+rand.randrange(600, 1150), y+rand.randrange(110, 260)),
               start=210, end=343, fill=(*accent, rand.randrange(26, 105)),
               width=rand.randint(3, 9))
    bloom = energy.filter(ImageFilter.GaussianBlur(17))
    im = Image.alpha_composite(im, bloom)
    im = Image.alpha_composite(im, energy)
    # Moon and halo, offset to avoid covering profile fields.
    halo = Image.new("RGBA", (w, h))
    hd = ImageDraw.Draw(halo)
    hd.ellipse((726, 84, 1060, 418), fill=(*accent, 45))
    im = Image.alpha_composite(im, halo.filter(ImageFilter.GaussianBlur(72)))
    d = ImageDraw.Draw(im)
    d.ellipse((848, 157, 1001, 310), fill=(134, 118, 185, 83),
              outline=(217, 194, 255, 80), width=2)
    d.ellipse((882, 140, 1014, 274), fill=(21, 15, 57, 210))
    # Jagged ruin silhouette along bottom. Each tower built as vector polygons.
    for depth in range(4):
        baseline = 1300 + depth * 154
        dark = [(10, 9, 35, 100), (9, 9, 29, 145), (6, 7, 23, 200), (5, 6, 19, 244)][depth]
        vertices = [(0, h)]
        vertices += [(0, baseline)]
        for x in range(0, w + 60, 45):
            vertices.append((x, baseline - rand.randint(8, 112 + depth * 10)))
        vertices.extend([(w, h)])
        d.polygon(vertices, fill=dark)
        for _ in range(15):
            x = rand.randint(-50, w + 40)
            roof = baseline - rand.randint(10, 175)
            width = rand.randint(13, 56)
            height = rand.randint(65, 210)
            d.rectangle((x, roof, x+width, roof+height), fill=dark)
            d.polygon(((x-7,roof),(x+width/2,roof-rand.randint(14,45)),
                       (x+width+7,roof)), fill=dark)
            if rand.random() < .16:
                d.rectangle((x+width*.45,roof-28,x+width*.56,roof+10),fill=dark)
    # Luminous perimeter engraved at full output size.
    line = (*accent, 145)
    d.rounded_rectangle((18, 18, 1062, 1901), radius=30, outline=line, width=3)
    d.rounded_rectangle((30, 30, 1050, 1889), radius=27, outline=(*accent, 62), width=2)
    for cx, cy, sign_x, sign_y in ((18,18,1,1),(1062,18,-1,1),
                                  (18,1901,1,-1),(1062,1901,-1,-1)):
        d.line((cx, cy+30*sign_y, cx+42*sign_x, cy+42*sign_y,
                cx+63*sign_x, cy+9*sign_y), fill=(*accent, 190), width=4)
        d.ellipse((cx+33*sign_x-5,cy+28*sign_y-5,
                   cx+33*sign_x+5,cy+28*sign_y+5), fill=(*accent, 215))
    # Slight contrast veil ensures the UI text stays readable on each step.
    tint = Image.new("RGBA", (w, h), (4, 5, 20, 50))
    im = Image.alpha_composite(im, tint)
    output(name, im.convert("RGB"))


def frame(name: str, color: tuple[int, int, int]) -> None:
    s = 1024
    im = Image.new("RGBA", (s,s), (0,0,0,0))
    halo = Image.new("RGBA",(s,s))
    hd = ImageDraw.Draw(halo)
    hd.ellipse((92,92,932,932),outline=(*color,170),width=26)
    im = Image.alpha_composite(im,halo.filter(ImageFilter.GaussianBlur(40)))
    d = ImageDraw.Draw(im)
    for inset,alpha,width in ((110,205,8),(131,255,15),(150,120,4)):
        d.ellipse((inset,inset,s-inset,s-inset),outline=(*color,alpha),width=width)
    # Eight repeating crystal ornaments around the circular portrait.
    for i in range(8):
        a = math.pi * i / 4
        cx, cy = 512 + 373*math.cos(a), 512 + 373*math.sin(a)
        along = (math.cos(a),math.sin(a))
        across = (-math.sin(a),math.cos(a))
        def point(rad, side=0):
            return (int(cx+rad*along[0]+side*across[0]),
                    int(cy+rad*along[1]+side*across[1]))
        d.polygon([point(-34),point(0,18),point(37),point(0,-18)],
                  fill=(*color,160),outline=(239,226,255,190))
        d.line((point(-16),point(16)),fill=(250,247,255,190),width=4)
    # Subtle alpha interior remains exactly zero: photo is not obscured.
    output(name,im)


def badge() -> None:
    s=512
    im=Image.new("RGBA",(s,s))
    d=ImageDraw.Draw(im)
    outer=[(256,22),(450,137),(450,372),(256,491),(62,372),(62,137)]
    inner=[(256,52),(423,152),(423,357),(256,456),(89,357),(89,152)]
    d.polygon(outer,fill=(28,18,62,232),outline=(198,158,255,255),width=9)
    d.polygon(inner,outline=(147,106,230,220),width=5)
    for x,y in ((256,22),(450,137),(450,372),(256,491),(62,372),(62,137)):
        d.ellipse((x-11,y-11,x+11,y+11),fill=(236,218,255,255))
    # "E" drawn as geometry rather than relying on a licensed font.
    d.rounded_rectangle((180,153,207,354),radius=8,fill=(239,231,255,255))
    for y in (153,242,329):
        d.rounded_rectangle((181,y,331,y+27),radius=6,fill=(239,231,255,255))
    output("badge_rank_e",im)


def icon(name: str, kind: str) -> None:
    s=512
    im=Image.new("RGBA",(s,s))
    g=Image.new("RGBA",(s,s))
    gd=ImageDraw.Draw(g)
    gd.ellipse((82,82,430,430),fill=(*VIOLET,60))
    im=Image.alpha_composite(im,g.filter(ImageFilter.GaussianBlur(46)))
    d=ImageDraw.Draw(im)
    color=(226,210,255,255)
    line=29
    if kind=="identity":
        d.ellipse((196,110,318,236),outline=color,width=line)
        d.arc((134,250,378,465),190,349,fill=color,width=line)
    elif kind=="camera":
        d.rounded_rectangle((93,158,420,379),radius=34,outline=color,width=line)
        d.rounded_rectangle((155,124,271,166),radius=6,fill=color)
        d.ellipse((190,202,331,342),outline=color,width=line)
        d.ellipse((363,201,380,218),fill=color)
    elif kind=="theme":
        d.ellipse((120,118,392,390),outline=color,width=line)
        d.pieslice((135,132,379,376),30,225,fill=(*VIOLET,185))
        d.line((256,106,256,400),fill=color,width=8)
    elif kind=="week":
        d.rounded_rectangle((110,148,401,396),radius=20,outline=color,width=line)
        d.line((115,210,393,210),fill=color,width=line)
        for x in (167,254,341):
            d.line((x,126,x,182),fill=color,width=18)
        for x in (169,252,335):
            for y in (269,333):
                d.ellipse((x-12,y-12,x+12,y+12),fill=color)
    elif kind=="goal":
        for r in (137,99,55):
            d.ellipse((256-r,256-r,256+r,256+r),outline=color,width=17)
        d.line((256,256,404,109),fill=(*CYAN,255),width=27)
    elif kind=="difficulty":
        for i,amt in enumerate((135,190,265)):
            x=126+i*96
            d.rounded_rectangle((x,380-amt,x+53,380),radius=10,fill=color)
    elif kind=="restriction":
        d.polygon(((255,80),(393,143),(376,328),(255,427),
                   (137,328),(119,143)),outline=color,width=25)
        d.line((190,258,240,314,323,205),fill=color,width=23,joint="curve")
    elif kind=="edit":
        d.line((141,359,345,154),fill=color,width=36,joint="curve")
        d.polygon(((121,380),(182,367),(132,326)),fill=(*CYAN,255))
        d.polygon(((342,123),(392,172),(359,205),(307,155)),fill=color)
    elif kind=="xp":
        d.polygon(((256,92),(291,200),(400,200),(313,265),(353,380),
                   (256,313),(161,380),(200,265),(112,200),(221,200)),fill=color)
    else:
        raise ValueError(kind)
    output(name,im)


def main() -> None:
    # Cada uno de los cinco paneles de creación dispone de un fondo
    # PNG Full HD independiente; introducción y éxito reutilizan el inicial.
    background("bg_onboarding_identity_fullhd", 1941, VIOLET)
    background("bg_onboarding_appearance_fullhd", 2759, (112,89,229))
    background("bg_onboarding_week_fullhd", 4631, CYAN)
    background("bg_onboarding_goals_fullhd", 8843, (183,91,252))
    background("bg_onboarding_restrictions_fullhd", 3149, (119,155,248))
    for color_name,color in [("violet",VIOLET),("cyan",CYAN),("gold",GOLD)]:
        frame(f"frame_profile_{color_name}",color)
    badge()
    for name,kind in [
        ("icon_profile_identity","identity"),("icon_profile_photo","camera"),
        ("icon_profile_theme","theme"),("icon_profile_week","week"),
        ("icon_profile_goal","goal"),("icon_profile_difficulty","difficulty"),
        ("icon_profile_restrictions","restriction"),("icon_profile_edit","edit"),
        ("icon_profile_xp","xp")
    ]:
        icon(name,kind)
    assert len(list(DEST.glob("bg_onboarding_*_fullhd.png"))) == 5
    print("PNG reproducible: 5 fondos de panel Full HD, 3 marcos, 1 insignia, 9 iconos.")


if __name__ == "__main__":
    main()
