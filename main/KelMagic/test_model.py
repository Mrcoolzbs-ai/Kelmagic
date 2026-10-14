import json

data = {
    "bases": ["minecraft:item"],
    "parent": "minecraft:item/generated",
    "display": {
        "thirdperson": {"body": "minecraft:item", "rotation": [90, 0, 0]},
        "firstperson": {"body": "minecraft:item", "rotation": [0, 0, 0]},
        "ground": {"body": "minecraft:item", "rotation": [0, -90, 0]},
        "item": {
            "textures": [
                {"overlay": "kelmagic:magic_crystal", "index": 0, "visible": True}
            ]
        }
    }
}

f = open("c:\\Projects\\KelMagic\\src\\main\\resources\\assets\\kelmagic\\models\\item\\magic_crystal.json", "w")
f.write(json.dumps(data, indent=2))
f.close()