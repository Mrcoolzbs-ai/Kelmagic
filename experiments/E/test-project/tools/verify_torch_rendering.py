"""Regression checks for floor/wall spell torch transparency and vanilla geometry."""
from pathlib import Path
import json, zipfile
from PIL import Image
P=Path(__file__).resolve().parents[1]
A=P/'src/main/resources/assets/kelmagic'
for name,parent in [('magic_torch','minecraft:block/template_torch'),('magic_wall_torch','minecraft:block/template_torch_wall')]:
    d=json.loads((A/f'models/block/{name}.json').read_text())
    assert d['parent']==parent, (name,'must use native torch geometry, not crossed cards')
    assert d['render_type']=='minecraft:cutout', (name,'must discard transparent texels')
    assert d['textures']['torch']=='kelmagic:block/magic_torch'
    assert 'elements' not in d, (name,'do not override native geometry')
im=Image.open(A/'textures/block/magic_torch.png')
assert im.mode=='RGBA' and im.size==(16,16)
assert set(im.getchannel('A').getdata())=={0,255}
assert json.loads((A/'blockstates/magic_torch.json').read_text())['variants']['']['model']=='kelmagic:block/magic_torch'
variants=json.loads((A/'blockstates/magic_wall_torch.json').read_text())['variants']
for direction,rotation in [('east',0),('south',90),('west',180),('north',270)]:
    v=variants[f'facing={direction}']; assert v['model']=='kelmagic:block/magic_wall_torch' and v['y']==rotation
print('PASS: native floor/wall geometry, explicit alpha cutout, RGBA texture, all four wall orientations')

for block,model in [('transformed_magic_torch','magic_torch'),('transformed_magic_wall_torch','magic_wall_torch')]:
    variants=json.loads((A/f'blockstates/{block}.json').read_text())['variants']
    assert all(v['model']=='kelmagic:block/'+model for v in variants.values())
print('PASS: transformed lights reuse the same fixed torch models')
