from PIL import Image,ImageChops,ImageDraw,ImageFont
import numpy as np,json,hashlib
from pathlib import Path
p=Path(__file__).resolve().parents[1];src=p/'docs/kel-language/source/writ.png';im=Image.open(src).convert('RGB')
cons='ptkbdgmnslvrj';vowels='aeiou'
boxes=[(410,150,458,216),(507,150,555,216),(597,150,642,216),(692,150,737,216),(783,150,828,216),(410,282,456,351),(500,282,553,351),(594,282,645,351),(689,282,738,351),(782,282,831,351),(410,423,456,488),(500,423,552,488),(594,423,644,488)]
vboxes=[(1215,180,1250,216),(1210,238,1252,258),(1211,282,1251,321),(1210,331,1252,378),(1210,391,1260,423)]
folder=p/'src/main/resources/assets/kelmagic/textures/font';folder.mkdir(parents=True,exist_ok=True)
basefolder=p/'src/main/resources/assets/kelmagic/textures/glyph';basefolder.mkdir(parents=True,exist_ok=True)
def extract(box,color=False):
 a=np.array(im.crop(box));lo=a.min(axis=2);hi=a.max(axis=2)
 mask=(hi.astype(int)-lo.astype(int)>65)&(lo<165) if color else hi<120
 alpha=np.where(mask,255,0).astype('uint8')
 out=Image.new('RGBA',(a.shape[1],a.shape[0]),(255,255,255,0));out.putalpha(Image.fromarray(alpha));bbox=out.getbbox()
 if not bbox:raise ValueError(box)
 return out.crop(bbox)
def fit(image,w,h):
 image=image.copy();image.thumbnail((w,h),Image.Resampling.LANCZOS);return image
bases=[extract(b) for b in boxes];marks=[extract(b,True) for b in vboxes]
for c,g in zip(cons,bases):g.save(basefolder/(c+'.png'))
for c,g in zip(vowels,marks):g.save(basefolder/(c+'.png'))
positions={'a':(37,14,8,12),'e':(36,34,11,5),'i':(36,51,9,12),'o':(12,0,13,13),'u':(10,61,22,10)}
CW,CH=48,72;atlas=Image.new('RGBA',(CW*8,CH*10));mapping={};tiles={}
for ci,c in enumerate(cons):
 for vi,v in enumerate(['']+list(vowels)):
  index=ci*6+vi;tile=Image.new('RGBA',(CW,CH));g=fit(bases[ci],30,44);tile.alpha_composite(g,(4+(30-g.width)//2,15+(44-g.height)//2))
  if v:
   x,y,w,h=positions[v];m=fit(marks[vowels.index(v)],w,h);tile.alpha_composite(m,(x+(w-m.width)//2,y+(h-m.height)//2))
  atlas.alpha_composite(tile,((index%8)*CW,(index//8)*CH));mapping[c+v]={'codepoint':hex(0xE000+index),'cell':[index%8,index//8]};tiles[c+v]=tile
atlas.save(folder/'kel.png')
chars=[''.join(chr(0xE000+i) if i<78 else '\x00' for i in range(row*8,row*8+8)) for row in range(10)]
f=p/'src/main/resources/assets/kelmagic/font/kel.json';f.parent.mkdir(exist_ok=True);f.write_text(json.dumps({'providers':[{'type':'space','advances':{' ':4.0}},{'type':'bitmap','file':'kelmagic:font/kel.png','ascent':14,'height':18,'chars':chars}]},indent=2)+'\n')
f=p/'src/main/resources/assets/kelmagic/glyph-map.json';f.write_text(json.dumps({'version':1,'source':'docs/kel-language/source/writ.png','source_sha256':hashlib.sha256(src.read_bytes()).hexdigest(),'consonants':{c:{'source_crop':b,'texture':'kelmagic:textures/glyph/'+c+'.png'} for c,b in zip(cons,boxes)},'vowels':{c:{'source_crop':b,'texture':'kelmagic:textures/glyph/'+c+'.png','placement':positions[c]} for c,b in zip(vowels,vboxes)},'units':{'cell':[48,72],'consonant_box':[4,15,30,44]},'composition':'attach next vowel to preceding consonant; bare final consonant; no substitution from erroneous min example','forms':mapping},ensure_ascii=False,indent=2)+'\n')
# QA proof: 18 canonical isolated shapes, plus all six magical words.
proof=Image.new('RGB',(940,650),'#101c2c');d=ImageDraw.Draw(proof);font=ImageFont.truetype('/usr/share/fonts/dejavu/DejaVuSans.ttf',18) if Path('/usr/share/fonts/dejavu/DejaVuSans.ttf').exists() else ImageFont.load_default()
for i,(c,g) in enumerate(zip(cons+vowels,bases+marks)):
 x=25+(i%9)*100;y=25+(i//9)*150;d.text((x,y),c,fill='white',font=font);g=fit(g,50,85);proof.paste(g,(x+15,y+35),g)
for row,w in enumerate(['mina','tuna','sila','pir','no','lu']):
 y=345+(row//3)*135;x=30+(row%3)*300;d.text((x,y),w,fill='#69dfef',font=font);units=[];i=0
 while i<len(w):
  c=w[i];i+=1
  if i<len(w) and w[i] in vowels:c+=w[i];i+=1
  tile=tiles[c].resize((72,108),Image.Resampling.NEAREST);proof.paste(tile,(x+len(units)*77,y+25),tile);units.append(c)
proof.save(p/'docs/architecture/screenshots/glyph-proof.png')
print('18 source glyphs; 78 composed forms; six-word visual proof.')
