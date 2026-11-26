#!/usr/bin/env python3
"""잔해 리소스팩. 무기는 복셀, 소모품과 재료는 32×32 그림, 갑옷은 입은 텍스처."""

import json
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "resourcepack"
ASSETS = PACK / "assets" / "janhae"

PALETTE = [
    (60, 42, 28),
    (148, 96, 48),
    (196, 112, 52),
    (96, 102, 110),
    (206, 210, 214),
    (28, 28, 32),
    (176, 42, 36),
    (212, 176, 64),
    (236, 232, 220),
    (92, 140, 78),
    (224, 214, 190),
    (170, 214, 222),
    (92, 78, 64),
    (46, 72, 48),
    (150, 96, 62),
    (36, 40, 46),
]

DISPLAY_SWORD = {
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "gui": {"rotation": [30, 225, 0], "scale": [0.625, 0.625, 0.625]},
    "ground": {"translation": [0, 3, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 180, 0], "scale": [0.7, 0.7, 0.7]},
}

DISPLAY_FLAT = {
    "thirdperson_righthand": {"rotation": [0, 90, 0], "translation": [0, 3, 1], "scale": [0.55, 0.55, 0.55]},
    "thirdperson_lefthand": {"rotation": [0, 90, 0], "translation": [0, 3, 1], "scale": [0.55, 0.55, 0.55]},
    "firstperson_righthand": {"rotation": [0, 90, -10], "translation": [1.5, 3, 0], "scale": [0.6, 0.6, 0.6]},
    "firstperson_lefthand": {"rotation": [0, -90, 10], "translation": [1.5, 3, 0], "scale": [0.6, 0.6, 0.6]},
    "gui": {"rotation": [30, 225, 0], "scale": [0.6, 0.6, 0.6]},
    "ground": {"translation": [0, 3, 0], "scale": [0.4, 0.4, 0.4]},
    "fixed": {"rotation": [0, 180, 0], "scale": [0.8, 0.8, 0.8]},
}


def cube(x1, y1, z1, x2, y2, z2, color):
    uv = [color, 0, color + 1, 1]
    face = {"uv": uv, "texture": "#p"}
    return {
        "from": [x1, y1, z1],
        "to": [x2, y2, z2],
        "faces": {side: face for side in ("north", "south", "east", "west", "up", "down")},
    }


def sword(handle, guard, blade, length=15):
    return [
        cube(7, 0, 7, 9, 4, 9, handle),
        cube(4, 4, 7, 12, 5.2, 9, guard),
        cube(7.2, 5, 7.2, 8.8, length, 8.8, blade),
    ]


def axe(handle, head):
    return [
        cube(7, 0, 7, 9, 12, 9, handle),
        cube(9, 9, 6, 14, 14, 10, head),
        cube(8, 10, 6.5, 10, 13, 9.5, head),
    ]


def club(color, dark):
    return [
        cube(6.5, 0, 6.5, 9.5, 6, 9.5, dark),
        cube(5.5, 6, 5.5, 10.5, 15, 10.5, color),
        cube(10, 8, 7, 12, 10, 9, dark),
    ]


def baton():
    return [
        cube(7.2, 0, 7.2, 8.8, 14, 8.8, 5),
        cube(6.6, 0, 6.6, 9.4, 2, 9.4, 15),
        cube(6.8, 12, 6.8, 9.2, 13.2, 9.2, 7),
    ]


def pan():
    return [
        cube(7, 0, 7, 9, 6, 9, 5),
        cube(3, 6, 4, 12, 8, 12, 3),
        cube(4, 8, 5, 11, 9, 11, 2),
    ]


def crossbow(stock, limb):
    return [
        cube(7, 0, 7, 9, 8, 9, stock),
        cube(2, 8, 7, 7, 10, 9, limb),
        cube(9, 8, 7, 14, 10, 9, limb),
        cube(6, 7, 6.5, 10, 8.2, 9.5, 4),
        cube(7.4, 8, 7.4, 8.6, 13, 8.6, 7),
    ]


def pickaxe(handle, head):
    return [
        cube(7, 0, 7, 9, 12, 9, handle),
        cube(3, 11, 7, 13, 13.5, 9, head),
        cube(3, 10, 7.3, 5, 12, 8.7, head),
        cube(11, 10, 7.3, 13, 12, 8.7, head),
    ]


def shard():
    return [
        cube(7, 0, 7, 9, 3, 9, 12),
        cube(6.5, 3, 6.5, 9.5, 8, 9.5, 11),
        cube(7, 8, 7, 9, 15, 8.2, 11),
    ]


def bone_club():
    return [
        cube(6, 0, 6, 10, 12, 10, 10),
        cube(5, 11, 5, 11, 15, 11, 10),
        cube(8, 8, 5, 11, 11, 8, 9),
    ]


def rib():
    return [
        cube(7, 0, 7, 9, 5, 9, 14),
        cube(6, 5, 7, 10, 7, 9, 10),
        cube(7, 7, 7.2, 9, 15, 8.4, 10),
        cube(5, 9, 7, 7, 10, 9, 10),
        cube(9, 12, 7, 11, 13, 9, 10),
    ]


def needle():
    return [
        cube(7, 2, 7, 9, 8, 9, 11),
        cube(7.4, 0, 7.4, 8.6, 3, 8.6, 4),
        cube(6.5, 8, 6.5, 9.5, 14, 9.5, 1),
        cube(7.6, 13, 7.6, 8.4, 16, 8.4, 6),
    ]


def saw(handle, tooth):
    parts = [cube(7, 0, 7, 9, 7, 9, handle)]
    parts.append(cube(4, 8, 7, 12, 14, 9, tooth))
    parts.append(cube(6, 6.5, 7, 10, 15.5, 9, tooth))
    parts.append(cube(3, 10, 6.6, 5, 12, 8.4, 4))
    parts.append(cube(11, 10, 6.6, 13, 12, 8.4, 4))
    return parts


def breaker():
    return axe(7, 6) + [cube(12, 8, 6, 15, 15, 10, 7)]


def vial_sword():
    return sword(11, 4, 8, 14) + [cube(6.2, 8, 6.2, 8, 12, 8, 9)]


def scythe():
    return [
        cube(7, 0, 7, 9, 14, 9, 8),
        cube(9, 12, 7, 15, 14, 9, 4),
        cube(13, 8, 7, 15, 13, 9, 4),
        cube(8, 13, 6.5, 11, 15, 9.5, 6),
    ]


def syringe():
    return [
        cube(6.5, 3, 6.5, 9.5, 12, 9.5, 11),
        cube(7.2, 0, 7.2, 8.8, 4, 8.8, 4),
        cube(6.8, 12, 6.8, 9.2, 14, 9.2, 8),
        cube(7.5, 11, 5.5, 8.5, 13, 7, 9),
    ]


def rifle():
    return [
        cube(4, 6, 7, 15, 8.5, 9, 15),
        cube(2, 7, 7.2, 6, 9, 8.8, 5),
        cube(10, 8.5, 7.3, 12, 11, 8.7, 5),
        cube(6, 5, 7.4, 9, 6.5, 8.6, 14),
        cube(13, 6.4, 7.4, 16, 8, 8.6, 6),
    ]


def shield():
    return [
        cube(3, 1, 7, 13, 15, 9, 15),
        cube(6, 3, 6.2, 10, 13, 7.2, 7),
        cube(7.2, 4, 6, 8.8, 12, 6.4, 3),
    ]


MODELS = {
    "plank_club": (club(1, 0), DISPLAY_SWORD),
    "riot_baton": (baton(), DISPLAY_SWORD),
    "frying_pan": (pan(), DISPLAY_SWORD),
    "fire_axe": (axe(6, 3), DISPLAY_SWORD),
    "lab_cutter": (sword(7, 4, 4, 14), DISPLAY_SWORD),
    "rust_sword": (sword(12, 3, 2), DISPLAY_SWORD),
    "patrol_crossbow": (crossbow(5, 1), DISPLAY_FLAT),
    "rust_pickaxe": (pickaxe(12, 2), DISPLAY_SWORD),
    "culture_shard": (shard(), DISPLAY_SWORD),
    "femur_mace": (bone_club(), DISPLAY_SWORD),
    "rib_knife": (rib(), DISPLAY_SWORD),
    "needle_lance": (needle(), DISPLAY_SWORD),
    "spine_saw": (saw(10, 10), DISPLAY_SWORD),
    "hazmat_breaker": (breaker(), DISPLAY_SWORD),
    "specimen_saw": (saw(3, 4), DISPLAY_SWORD),
    "last_vial": (vial_sword(), DISPLAY_SWORD),
    "state_rifle": (rifle(), DISPLAY_FLAT),
    "white_coat_reaper": (scythe(), DISPLAY_SWORD),
    "seal_syringe": (syringe(), DISPLAY_SWORD),
    "riot_shield": (shield(), DISPLAY_FLAT),
}

ICONS = {
    "bandage": ((230, 226, 214), "roll"),
    "lab_ration": ((150, 156, 160), "can"),
    "boiled_water": ((180, 210, 220), "bottle"),
    "painkiller": ((210, 200, 170), "bottle"),
    "antidote": ((70, 130, 78), "bottle"),
    "chem_light": ((230, 190, 70), "stick"),
    "field_arrow": ((120, 90, 50), "arrow"),
    "infected_tissue": ((96, 130, 70), "blob"),
    "mutated_bone": ((220, 210, 186), "bone"),
    "spore_sac": ((190, 176, 60), "blob"),
    "tainted_cloth": ((110, 70, 78), "cloth"),
    "sealed_sample": ((150, 190, 170), "bottle"),
    "unstable_core": ((120, 80, 170), "gem"),
    "sample_kit": ((70, 78, 64), "book"),
    "tissue_grill": ((120, 70, 48), "food"),
    "bone_broth": ((90, 70, 50), "cup"),
    "spore_fry": ((210, 170, 60), "food"),
}

ARMOR = {
    "work": (226, 224, 210),
    "riot": (42, 46, 52),
    "sealed": (214, 176, 48),
}


def write_model(item_id, elements, display):
    model = {
        "textures": {"p": "janhae:item/palette"},
        "elements": elements,
        "display": display,
    }
    path = ASSETS / "models" / "item" / f"{item_id}.json"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(model, indent=2), encoding="utf-8")
    definition = {"model": {"type": "minecraft:model", "model": f"janhae:item/{item_id}"}}
    def_path = ASSETS / "items" / f"{item_id}.json"
    def_path.parent.mkdir(parents=True, exist_ok=True)
    def_path.write_text(json.dumps(definition), encoding="utf-8")


def write_icon(item_id, rgb, shape):
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    shade = tuple(max(0, c - 40) for c in rgb)
    if shape == "roll":
        draw.ellipse((6, 8, 26, 24), fill=rgb)
        draw.rectangle((10, 12, 22, 20), fill=shade)
    elif shape == "can":
        draw.rounded_rectangle((8, 6, 24, 26), radius=3, fill=rgb)
        draw.rectangle((8, 8, 24, 12), fill=shade)
    elif shape == "bottle":
        draw.rectangle((14, 3, 18, 8), fill=shade)
        draw.polygon([(10, 10), (22, 10), (24, 28), (8, 28)], fill=rgb)
    elif shape == "stick":
        draw.rectangle((14, 4, 18, 28), fill=rgb)
        draw.ellipse((11, 2, 21, 10), fill=(255, 236, 140, 255))
    elif shape == "arrow":
        draw.line((6, 26, 24, 6), fill=rgb, width=3)
        draw.polygon([(22, 4), (28, 8), (22, 12)], fill=shade)
    elif shape == "blob":
        draw.ellipse((6, 8, 26, 26), fill=rgb)
        draw.ellipse((12, 12, 18, 18), fill=shade)
    elif shape == "bone":
        draw.rectangle((8, 14, 24, 18), fill=rgb)
        draw.ellipse((4, 10, 12, 22), fill=rgb)
        draw.ellipse((20, 10, 28, 22), fill=rgb)
    elif shape == "cloth":
        draw.polygon([(6, 8), (24, 6), (27, 24), (8, 26)], fill=rgb)
    elif shape == "gem":
        draw.polygon([(16, 3), (27, 14), (16, 29), (5, 14)], fill=rgb)
        draw.polygon([(16, 8), (22, 14), (16, 24), (10, 14)], fill=shade)
    elif shape == "book":
        draw.rectangle((7, 5, 25, 27), fill=rgb)
        draw.line((16, 5, 16, 27), fill=shade, width=2)
    elif shape == "food":
        draw.ellipse((6, 10, 26, 24), fill=rgb)
        draw.rectangle((15, 4, 18, 12), fill=shade)
    elif shape == "cup":
        draw.polygon([(8, 8), (24, 8), (22, 24), (10, 24)], fill=rgb)
        draw.arc((20, 10, 28, 20), 270, 90, fill=shade, width=2)
    image.save(ASSETS / "textures" / "item" / f"{item_id}.png")
    model = {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"janhae:item/{item_id}"},
    }
    (ASSETS / "models" / "item" / f"{item_id}.json").write_text(json.dumps(model), encoding="utf-8")
    definition = {"model": {"type": "minecraft:model", "model": f"janhae:item/{item_id}"}}
    (ASSETS / "items" / f"{item_id}.json").write_text(json.dumps(definition), encoding="utf-8")


def armor_icon(set_id, part, rgb):
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    shade = tuple(max(0, c - 35) for c in rgb)
    if part == "helmet":
        draw.ellipse((6, 4, 26, 26), fill=rgb)
        draw.rectangle((8, 16, 24, 22), fill=shade)
    elif part == "chestplate":
        draw.rectangle((8, 6, 24, 26), fill=rgb)
        draw.rectangle((4, 8, 8, 20), fill=shade)
        draw.rectangle((24, 8, 28, 20), fill=shade)
    elif part == "leggings":
        draw.rectangle((8, 4, 15, 28), fill=rgb)
        draw.rectangle((17, 4, 24, 28), fill=rgb)
    else:
        draw.rectangle((6, 14, 14, 26), fill=rgb)
        draw.rectangle((18, 14, 26, 26), fill=rgb)
    item_id = f"{set_id}_{part}"
    image.save(ASSETS / "textures" / "item" / f"{item_id}.png")
    model = {"parent": "minecraft:item/generated", "textures": {"layer0": f"janhae:item/{item_id}"}}
    (ASSETS / "models" / "item" / f"{item_id}.json").write_text(json.dumps(model), encoding="utf-8")
    definition = {"model": {"type": "minecraft:model", "model": f"janhae:item/{item_id}"}}
    (ASSETS / "items" / f"{item_id}.json").write_text(json.dumps(definition), encoding="utf-8")


def tint_armor(rgb):
    """바닐라 갑옷 칸 배치를 직접 칠한다. 64×32."""
    human = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    legs = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    paint = ImageDraw.Draw(human)
    shade = tuple(max(0, c - 40) for c in rgb)
    # head + hat
    for ox in (0, 32):
        paint.rectangle((ox + 8, 0, ox + 16, 8), fill=rgb)
        paint.rectangle((ox + 16, 0, ox + 24, 8), fill=shade)
        paint.rectangle((ox + 0, 8, ox + 8, 16), fill=shade)
        paint.rectangle((ox + 8, 8, ox + 16, 16), fill=rgb)
        paint.rectangle((ox + 16, 8, ox + 24, 16), fill=rgb)
        paint.rectangle((ox + 24, 8, ox + 32, 16), fill=shade)
    # body
    paint.rectangle((20, 16, 28, 20), fill=rgb)
    paint.rectangle((28, 16, 36, 20), fill=shade)
    paint.rectangle((16, 20, 20, 32), fill=shade)
    paint.rectangle((20, 20, 28, 32), fill=rgb)
    paint.rectangle((28, 20, 32, 32), fill=shade)
    paint.rectangle((32, 20, 40, 32), fill=rgb)
    # right arm, and the left-arm mirror region used by the 64px layout
    paint.rectangle((44, 16, 48, 20), fill=rgb)
    paint.rectangle((48, 16, 52, 20), fill=shade)
    paint.rectangle((40, 20, 44, 32), fill=shade)
    paint.rectangle((44, 20, 48, 32), fill=rgb)
    paint.rectangle((48, 20, 52, 32), fill=shade)
    paint.rectangle((52, 20, 56, 32), fill=rgb)
    # boots occupy the lower leg on the humanoid layer
    paint.rectangle((4, 16, 8, 20), fill=shade)
    paint.rectangle((8, 16, 12, 20), fill=rgb)
    paint.rectangle((0, 20, 4, 32), fill=shade)
    paint.rectangle((4, 28, 8, 32), fill=rgb)
    paint.rectangle((8, 28, 12, 32), fill=rgb)
    paint.rectangle((12, 28, 16, 32), fill=shade)
    leg_paint = ImageDraw.Draw(legs)
    # right leg
    leg_paint.rectangle((4, 0, 8, 4), fill=rgb)
    leg_paint.rectangle((8, 0, 12, 4), fill=shade)
    leg_paint.rectangle((0, 4, 4, 16), fill=shade)
    leg_paint.rectangle((4, 4, 8, 16), fill=rgb)
    leg_paint.rectangle((8, 4, 12, 16), fill=rgb)
    leg_paint.rectangle((12, 4, 16, 16), fill=shade)
    # left leg
    leg_paint.rectangle((20, 0, 24, 4), fill=rgb)
    leg_paint.rectangle((24, 0, 28, 4), fill=shade)
    leg_paint.rectangle((16, 4, 20, 16), fill=shade)
    leg_paint.rectangle((20, 4, 24, 16), fill=rgb)
    leg_paint.rectangle((24, 4, 28, 16), fill=rgb)
    leg_paint.rectangle((28, 4, 32, 16), fill=shade)
    return human, legs


def main():
    if PACK.exists():
        for path in PACK.rglob("*"):
            pass
    (ASSETS / "textures" / "item").mkdir(parents=True, exist_ok=True)
    (ASSETS / "textures" / "entity" / "equipment" / "humanoid").mkdir(parents=True, exist_ok=True)
    (ASSETS / "textures" / "entity" / "equipment" / "humanoid_leggings").mkdir(parents=True, exist_ok=True)
    (ASSETS / "equipment").mkdir(parents=True, exist_ok=True)

    palette = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    px = palette.load()
    for i, color in enumerate(PALETTE):
        for y in range(16):
            px[i, y] = (*color, 255)
    palette.save(ASSETS / "textures" / "item" / "palette.png")

    for item_id, (elements, display) in MODELS.items():
        write_model(item_id, elements, display)

    for item_id, (rgb, shape) in ICONS.items():
        write_icon(item_id, rgb, shape)

    parts = ("helmet", "chestplate", "leggings", "boots")
    for set_id, rgb in ARMOR.items():
        for part in parts:
            armor_icon(set_id, part, rgb)
        human, legs = tint_armor(rgb)
        human.save(ASSETS / "textures" / "entity" / "equipment" / "humanoid" / f"{set_id}.png")
        legs.save(ASSETS / "textures" / "entity" / "equipment" / "humanoid_leggings" / f"{set_id}.png")
        equipment = {
            "layers": {
                "humanoid": [{"texture": f"janhae:{set_id}"}],
                "humanoid_leggings": [{"texture": f"janhae:{set_id}"}],
            }
        }
        (ASSETS / "equipment" / f"{set_id}.json").write_text(json.dumps(equipment), encoding="utf-8")

    (PACK / "pack.mcmeta").write_text(
        json.dumps(
            {
                "pack": {
                    "description": "잔해",
                    "min_format": [97, 1],
                    "max_format": 97,
                }
            },
            ensure_ascii=False,
        ),
        encoding="utf-8",
    )
    icon = Image.new("RGBA", (64, 64), (176, 42, 36, 255))
    ImageDraw.Draw(icon).rectangle((8, 28, 56, 36), fill=(236, 232, 220, 255))
    icon.save(PACK / "pack.png")

    zip_path = ROOT / "src" / "main" / "resources" / "janhae-pack.zip"
    zip_path.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as archive:
        for path in PACK.rglob("*"):
            if path.is_file():
                archive.write(path, path.relative_to(PACK).as_posix())
    print(f"items {len(list((ASSETS / 'items').glob('*.json')))} -> {zip_path}")


if __name__ == "__main__":
    main()
