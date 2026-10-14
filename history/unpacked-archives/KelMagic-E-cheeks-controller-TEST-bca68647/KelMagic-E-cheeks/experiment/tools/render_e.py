from pathlib import Path
import sys,json
from PIL import Image
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'tools'));from renderer import render
tex=Image.open(R/'reference/red_mage_geo.png');O=R/'evidence/raw';O.mkdir(exist_ok=True,parents=True)
for name,path in [('D-approved',R/'reference')]+[(p.name,p) for p in sorted((R/'states').iterdir()) if p.is_dir()]:
 geo=json.loads((path/'red_mage.geo.json').read_text())
 for v,yaw,cut in [('front',0,False),('angle45',45,False),('diagnostic-front',0,True),('diagnostic45',45,True)]:
  render(geo,tex,yaw,cut, json.loads((path/'pose.json').read_text())['normalizedValues'] if (path/'pose.json').exists() else {}).save(O/f'{name}-{v}.png')
 print('RENDERED',name,flush=True)
