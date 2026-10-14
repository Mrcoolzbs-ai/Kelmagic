from pathlib import Path
from PIL import Image,ImageOps
import numpy as np
P=Path(__file__).resolve().parents[1];src=P/'art/research-ink-source.jpg';source=Image.open(src).convert('L').resize((128,128),Image.Resampling.LANCZOS);a=np.array(source).astype(float);lo,hi=np.percentile(a,[3,98]);a=np.clip((a-lo)/(hi-lo),0,1)
palettes={'origins':((31,23,27),(109,78,55)),'materials':((12,28,29),(46,95,78)),'sound':((13,23,39),(45,86,115)),'equipment':((25,17,39),(94,61,120)),'writing':((35,20,22),(113,65,44)),'great':((13,9,30),(88,53,118))}
out=P/'src/main/resources/assets/kelmagic/textures/gui/research';out.mkdir(parents=True,exist_ok=True)
for key,(dark,light) in palettes.items():
 rgb=np.dstack([np.rint(dark[i]+a*(light[i]-dark[i])).astype('uint8') for i in range(3)]);im=Image.fromarray(rgb);tile=Image.new('RGB',(256,256));tile.paste(im,(0,0));tile.paste(ImageOps.mirror(im),(128,0));tile.paste(ImageOps.flip(im),(0,128));tile.paste(ImageOps.mirror(ImageOps.flip(im)),(128,128));tile.save(out/(key+'.png'))
 ar=np.array(tile);assert np.array_equal(ar[0],ar[-1]) and np.array_equal(ar[:,0],ar[:,-1]),key
print('PASS: six original palette variants, 256px, seamless opposing edges')
