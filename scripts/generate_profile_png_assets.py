#!/usr/bin/env python3
"""Arte ORIGINAL reproducible en PNG: fondos de perfil FHD y overlays RGBA HD.

Todo se dibuja a resolución nativa mediante Pillow (no amplía miniaturas).
Los fondos Monarca pertenecen a otro sistema y nunca se importan aquí.
"""
from __future__ import annotations

import hashlib
import json
import math
import random
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT = Path("app/src/main/res/drawable-nodpi")
OUTPUT.mkdir(parents=True, exist_ok=True)
RNG = random.Random(230811)
FILES: list[dict] = []


def write(name: str, image: Image.Image, kind: str, required_size: tuple[int, int]):
    assert image.size == required_size, (name, image.size)
    assert image.mode == ("RGB" if kind == "background" else "RGBA")
    if kind != "background":
        assert image.getpixel((0, 0))[3] == 0, name
        if name.startswith("frame_profile_"):
            assert image.getpixel((image.width // 2, image.height // 2))[3] == 0, name
    dest = OUTPUT / (name + ".png")
    image.save(dest, "PNG", optimize=True, compress_level=9)
    FILES.append({
        "name": name, "file": str(dest), "role": kind,
        "width": image.width, "height": image.height,
        "mode": image.mode,
        "sha256": hashlib.sha256(dest.read_bytes()).hexdigest()
    })
    print(name, image.size, image.mode, dest.stat().st_size)


def gradient(w: int, h: int, top, middle, bottom) -> Image.Image:
    im = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(im)
    for y in range(h):
        ratio = y / h
        if ratio < 0.55:
            t = ratio / 0.55
            a, b = top, middle
        else:
            t = (ratio - 0.55) / 0.45
            a, b = middle, bottom
        color = tuple(round(a[k] * (1 - t) + b[k] * t) for k in range(3))
        d.line((0, y, w, y), fill=color)
    return im


def soft_glow(base: Image.Image, center, radius, rgb, strength):
    w, h = base.size
    glow = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(glow)
    for j in range(12, 0, -1):
        r = int(radius * j / 12)
        a = int(strength * (1 - j / 13) ** 2)
        d.ellipse((center[0]-r, center[1]-r,
                   center[0]+r, center[1]+r), fill=(*rgb, a))
    glow = glow.filter(ImageFilter.GaussianBlur(40))
    base.paste(Image.alpha_composite(base.convert("RGBA"), glow).convert("RGB"))


def gothic_background(name: str, light: bool = False, variant: int = 0):
    w, h = 1080, 1920
    if light:
        img = gradient(w, h, (205, 204, 235), (224, 222, 246), (171, 171, 220))
        fog = (91, 67, 156)
        line = (101, 73, 157)
        tower = (91, 76, 139)
    else:
        img = gradient(w, h, (8, 8, 27), (27, 17, 63), (6, 9, 28))
        fog = (143, 82, 255)
        line = (174, 121, 244)
        tower = (20, 15, 48)
    soft_glow(img, (825 if variant == 0 else 240, 310), 410,
              fog, 145 if not light else 73)
    d = ImageDraw.Draw(img, "RGBA")

    # Glifo lunar, anillos y una arquitectura fantástica original.
    cx = 835 if variant == 0 else 230
    cy = 322
    for radius, alpha in [(174, 22), (123, 36), (76, 56)]:
        d.ellipse((cx-radius, cy-radius, cx+radius, cy+radius),
                  outline=(*line, alpha), width=4)
    d.ellipse((cx-62, cy-62, cx+62, cy+62), fill=(*line, 39))
    d.arc((cx-52, cy-52, cx+52, cy+52), 45, 255, fill=(*line, 112), width=5)

    # Rayos geométricos esquivando la zona de formulario.
    for k in range(9):
        phi = 2 * math.pi * k / 9
        x1, y1 = cx + math.cos(phi) * 135, cy + math.sin(phi) * 135
        x2, y2 = cx + math.cos(phi) * 174, cy + math.sin(phi) * 174
        d.line((x1, y1, x2, y2), fill=(*line, 78), width=3)

    # Planos de ruinas en las esquinas inferiores.
    for x, tw, th in [(-35, 132, 330), (86, 70, 190),
                       (900, 110, 280), (1010, 110, 360)]:
        y = h - 85 - th
        d.rectangle((x, y, x+tw, h), fill=(*tower, 200 if not light else 77))
        d.polygon([(x-20,y),(x+tw//2,y-52),(x+tw+20,y)],
                  fill=(*tower, 206 if not light else 95))
        d.rectangle((x+tw*0.44,y-85,x+tw*0.57,y),fill=(*tower, 170 if not light else 75))
        for yy in range(y+45, h-25, 90):
            d.rounded_rectangle((x+tw*0.34, yy, x+tw*0.53, yy+33),radius=9,
                                fill=(139, 110, 217, 28 if not light else 16))

    # Marco editorial con filigrana lineal, siempre detrás de los controles.
    d.rounded_rectangle((24, 24, w-25, h-25), radius=42,
                        outline=(*line, 76 if not light else 112), width=4)
    d.rounded_rectangle((42, 42, w-43, h-44),radius=30,
                        outline=(*line, 32 if not light else 68), width=2)
    for y, side in [(78, 1), (h-82, 1)]:
        for x in (72, w-72):
            d.line((x-32,y,x,y-32,x+32,y),fill=(*line,96),width=3)
    for i in range(175):
        # Partículas semitransparentes: patrones dibujados a resolución original.
        x = RNG.randrange(50, w-50)
        y = RNG.randrange(50, h-50)
        r = RNG.choice([1,1,2,3])
        d.ellipse((x-r,y-r,x+r,y+r),fill=(*line, RNG.randrange(22,69)))
    write(name, img, "background", (w, h))


def frame_png(name: str, rgb):
    im = Image.new("RGBA", (512, 512), (0,0,0,0))
    d = ImageDraw.Draw(im)
    center = 256
    for radius, thick, alpha in [(244, 11, 245), (229, 5, 180), (214, 5, 120)]:
        d.ellipse((center-radius,center-radius,center+radius,center+radius),
                  outline=(*rgb, alpha),width=thick)
    for i in range(12):
        theta = 2 * math.pi * i / 12
        x,y = center + 236*math.cos(theta),center+236*math.sin(theta)
        rr = 9 if i%3 else 14
        d.polygon([(x,y-rr),(x+rr*0.7,y),(x,y+rr),(x-rr*0.7,y)],
                  fill=(*rgb, 244))
    for angle in [15,105,195,285]:
        d.arc((18,18,494,494),angle,angle+40,fill=(238,221,255,172),width=4)
    write(name, im, "transparent", (512,512))


def badge_png(letter: str, rgb):
    im = Image.new("RGBA", (512,512),(0,0,0,0))
    d=ImageDraw.Draw(im)
    pts=[(256+206*math.cos(math.pi*k/4-math.pi/2),
          256+206*math.sin(math.pi*k/4-math.pi/2)) for k in range(8)]
    d.polygon(pts, fill=(22,16,49,232))
    d.line(pts+[pts[0]],fill=(*rgb,255),width=11,joint="curve")
    inner=[(256+180*math.cos(math.pi*k/4-math.pi/2),
            256+180*math.sin(math.pi*k/4-math.pi/2)) for k in range(8)]
    d.line(inner+[inner[0]],fill=(*rgb,90),width=4)
    font_path="/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
    font=ImageFont.truetype(font_path, 215)
    bb=d.textbbox((0,0),letter,font=font,stroke_width=2)
    tw,th=bb[2]-bb[0],bb[3]-bb[1]
    d.text(((512-tw)//2-bb[0],(512-th)//2-bb[1]-12),letter,
           font=font,fill=(255,246,255,255),stroke_width=2,stroke_fill=(*rgb,255))
    write("badge_rank_"+letter.lower(),im,"transparent",(512,512))


def icon_png(name: str, kind: str, rgb):
    im=Image.new("RGBA",(512,512),(0,0,0,0))
    d=ImageDraw.Draw(im)
    col=(*rgb,255)
    d.rounded_rectangle((56,56,456,456),radius=94,outline=(*rgb,135),width=14)
    # La zona central no está rellena, solo trazos/contornos.
    if kind=="camera":
        d.rounded_rectangle((108,175,404,350),radius=38,outline=col,width=19)
        d.ellipse((204,209,308,313),outline=col,width=17)
        d.rounded_rectangle((146,142,231,187),radius=9,fill=col)
    elif kind=="calendar":
        d.rounded_rectangle((118,139,394,380),radius=27,outline=col,width=17)
        d.line((121,204,391,204),fill=col,width=17)
        for x in [168,260,346]:
            for y in [251,316]:
                d.ellipse((x-11,y-11,x+11,y+11),fill=col)
    elif kind=="target":
        for radius in (117,77,32):
            d.ellipse((256-radius,256-radius,256+radius,256+radius),outline=col,width=13)
        d.line((256,256,385,133),fill=col,width=14)
    elif kind=="shield":
        d.polygon([(256,121),(381,168),(358,325),(256,398),(154,325),(131,168)],
                  outline=col)
        d.line([(256,121),(381,168),(358,325),(256,398),(154,325),(131,168),(256,121)],
               fill=col,width=18,joint="curve")
    elif kind=="theme":
        d.arc((145,145,361,361),-75,215,fill=col,width=22)
        d.ellipse((235,181,337,283),fill=col)
        d.ellipse((268,157,354,263),fill=(0,0,0,0))
    elif kind=="edit":
        d.line((167,336,324,170),fill=col,width=29)
        d.polygon([(140,366),(174,310),(193,342)],fill=col)
        d.line((202,381,354,381),fill=col,width=17)
    elif kind=="difficulty":
        d.polygon([(126,348),(193,274),(254,303),(355,164),(397,196),(264,376),(203,348)],
                  fill=col)
    write(name,im,"transparent",(512,512))


def main():
    gothic_background("bg_profile_setup_dark_fhd",False,0)
    gothic_background("bg_profile_setup_light_fhd",True,0)
    gothic_background("bg_profile_summary_dark_fhd",False,1)
    gothic_background("bg_profile_summary_light_fhd",True,1)
    for name,rgb in [
        ("frame_profile_violet",(174,107,255)),
        ("frame_profile_cyan",(70,217,231)),
        ("frame_profile_gold",(236,190,99))
    ]:
        frame_png(name,rgb)
    for i,letter in enumerate("EDCBAS"):
        badge_png(letter,((142,154,181),(120,185,255),
            (112,204,201),(172,145,253),
            (232,173,106),(255,133,211))[i])
    for kind in ("camera","calendar","target","shield","theme","edit","difficulty"):
        icon_png("icon_profile_"+kind,kind,(191,144,248))
    manifest={"schema":"profile-assets-1.0","generator":"scripts/generate_profile_png_assets.py",
              "rights":"Arte geométrico original generado en el repositorio",
              "no_monarch_backgrounds":True,"not_upscaled_previews":True,
              "assets":FILES}
    Path("art").mkdir(exist_ok=True)
    Path("art/profile_asset_manifest.json").write_text(
        json.dumps(manifest,ensure_ascii=False,indent=2)+"\n",encoding="utf8")
    assert len(FILES)==20,len(FILES)
    assert len([x for x in FILES if x["role"]=="background"])==4
    assert all(x["mode"]=="RGBA" for x in FILES if x["role"]=="transparent")
    print("20 PNG originales verificados")


if __name__=="__main__":
    main()
