#!/usr/bin/env python3
"""Extrae fondos decorativos de un atlas propio sin introducir texto en los recortes.

Las imágenes resultantes son concept art de resolución moderada para tarjetas,
no fotografías originales ni pantallas FullHD. Pillow es la única dependencia.
"""
import json
import sys
from pathlib import Path
from PIL import Image, ImageFilter, ImageEnhance

src = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("art/source/background_atlas.png")
out = Path(sys.argv[2]) if len(sys.argv) > 2 else Path("app/src/main/res/drawable-nodpi")
out.mkdir(parents=True,exist_ok=True)
image = Image.open(src).convert("RGB")
if image.size == (200,133):
    # La URL pública de Canva ofrece solo una miniatura; estas imágenes son
    # placeholders de baja resolución, reemplazables con el atlas 1536x1024.
    print("AVISO: atlas reducido 200x133; fondos provisionales no FullHD.")
    image = image.resize((1536,1024),Image.Resampling.LANCZOS)
elif image.size != (1536,1024):
    raise SystemExit(f"Atlas con dimensiones inesperadas: {image.size}")

def box(x1,x2,y1,y2):
    return (x1,y1,x2,y2)

cols6=[(208,425),(438,641),(652,857),(868,1074),(1087,1301),(1310,1523)]
cols7=[(208+187*i,385+187*i) for i in range(7)]
cols7[-1]=(1329,1518)
def series(labels,cols,lo,hi,kind):
    return {"mission_bg_"+kind+"_"+label:(kind,box(*col,lo,hi)) for label,col in zip(labels,cols)}

all_backdrops={}
all_backdrops.update(series(["city_sunset","gym_hall","association","training_room","night_city","hunters_base"],cols6,182,263,"general"))
all_backdrops.update(series(["forest","ruins","crystals","mineshaft","canyon","shadow_plain"],cols6,314,388,"exploration"))
all_backdrops.update(series(["igris","beru","tank","kaisel","tusk","iron","kiba"],cols7,703,776,"shadow"))
all_backdrops.update(series(["blue_gate","purple_gate","crimson_gate","unstable","shadow_world","gate_hall"],cols6,564,641,"portal"))
all_backdrops.update(series(["destruction","beasts","frost","plague","shadows"],cols7[:5],842,899,"monarch"))
all_backdrops.update({
    "mission_bg_monarch_white_flames":("monarch",box(*cols6[2],444,511)),
    "mission_bg_monarch_iron_body":("monarch",box(*cols6[5],444,511)),
    "mission_bg_monarch_beginning":("monarch",box(*cols6[1],315,389)),
    "mission_bg_monarch_transfiguration":("monarch",box(*cols6[1],567,643))
})
catalog=[]
for name,(kind,rectangle) in all_backdrops.items():
    crop=image.crop(rectangle).resize((720,320),Image.Resampling.LANCZOS)
    crop=ImageEnhance.Color(crop).enhance(1.05)
    crop=crop.filter(ImageFilter.GaussianBlur(1.15)).convert("RGBA")
    mask=Image.new("RGBA",crop.size)
    pixels=mask.load()
    for y in range(320):
        for x in range(720):
            alpha=int(min(255,65+75*y/320+30*abs((x-360)/360)))
            pixels[x,y]=(7,8,25,alpha)
    crop=Image.alpha_composite(crop,mask).convert("RGB")
    dest=out/(name+".webp")
    crop.save(dest,"WEBP",quality=80,method=6)
    catalog.append({"name":name,"type":kind,"file":str(dest),"bytes":dest.stat().st_size,"atlas_rect":rectangle})
print(f"Fondos generados: {len(catalog)}; bytes totales: {sum(x['bytes'] for x in catalog)}")
if len(catalog)!=34 or sum(x["bytes"] for x in catalog)>2_000_000:
    raise SystemExit("La biblioteca de fondos no cumple el presupuesto esperado")
