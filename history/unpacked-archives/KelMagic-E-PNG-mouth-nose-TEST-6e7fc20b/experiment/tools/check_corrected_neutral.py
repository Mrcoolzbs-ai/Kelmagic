from pathlib import Path
import sys,json,numpy as np
from PIL import Image
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'tools'));from renderer import render
tex=Image.open(R/'reference/red_mage_geo.png');g=json.loads((R/'reference-fixed.geo.json').read_text());result={}
for v,yaw,cut in [('front',0,False),('angle45',45,False),('diagnostic-front',0,True),('diagnostic45',45,True)]:
 im=render(g,tex,yaw,cut);im.save(R/f'evidence/raw/D-UV-corrected-{v}.png');a=np.array(im);b=np.array(Image.open(R/f'evidence/raw/E0-neutral-{v}.png'));delta=np.abs(a.astype(int)-b.astype(int));result[v]={'changedPixels':int(np.any(delta,axis=2).sum()),'maxDifference':int(delta.max())};print(v,result[v],flush=True)
(R/'report/corrected-neutral-equality.json').write_text(json.dumps(result,indent=2))
