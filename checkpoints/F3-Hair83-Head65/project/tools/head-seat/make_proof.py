from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import zipfile,io
M=Path(__file__).resolve().parents[2];R=Path('/data/reports/kelmagic-f3-hair7-head-seat');D=R/'deliverables';D.mkdir(exist_ok=True);S=R/'native/screenshots'
F='/usr/share/fonts/msttcore/arial.ttf';B='/usr/share/fonts/msttcore/arialbd.ttf'
def text(d,xy,s,size=21,b=False,c='#2c2c2b'):d.text(xy,s,font=ImageFont.truetype(B if b else F,size),fill=c)
def shot(n):return Image.open(max(S.glob(n+'-*.png'),key=lambda f:int(f.stem.rsplit('-',1)[1]))).convert('RGB')
im=Image.new('RGB',(1140,1050),'#f9f8f7');d=ImageDraw.Draw(im);text(d,(25,22),'Hair7 · голова на теле, боковые слои подогнаны',29,True);text(d,(25,65),'Голова опущена на 1,208 пикселя. Боковой рисунок подогнан по высоте через UV.',21,c='#716b62')
for i,(n,label) in enumerate([('front','Спереди'),('quarter','Камера 45°'),('side','Сбоку'),('rear','Сзади'),('head45','Голова 45°'),('head90','Голова 90°')]):
 x=25+i%3*375;y=110+i//3*425;d.rounded_rectangle((x,y,x+350,y+405),radius=8,fill='white',outline='#dedad4');text(d,(x+12,y+12),label,23,True);pic=shot(n).crop((485,230,795,620)).resize((279,351),Image.Resampling.NEAREST);im.paste(pic,(x+35,y+47))
text(d,(25,980),'Нижняя граница головы совмещена с верхом тела. Верх грудных прядей опущен к той же линии.',20)
text(d,(25,1014),'Реальные кадры Minecraft / GeckoLib. 90° — QA-стресс-тест; кропы без ретуши.',20,c='#716b62');im.save(D/'01-standard-views.png')
# Reuse actual prior native screenshots from the recovery archive; no rerun of the old build.
def before(n):return Image.open(M/'reports/F3-hair7-head-seat/before-native'/ (n+'.png')).convert('RGB')
im=Image.new('RGB',(1040,890),'#f9f8f7');d=ImageDraw.Draw(im);text(d,(25,22),'Посадка головы · было / стало',29,True);text(d,(25,66),'Голова, лицо, шляпа и головные волосы перемещены вместе. Подбородок не увеличивался.',21,c='#716b62')
for row,n in enumerate(['front','side']):
 for col,(label,pic) in enumerate([('Было',before(n)),('Стало',shot(n))]):
  x=25+col*510;y=110+row*350;d.rounded_rectangle((x,y,x+490,y+330),radius=8,fill='white',outline='#dedad4');text(d,(x+12,y+10),label+' · '+('фронт' if n=='front' else 'бок'),22,True)
  pic=pic.crop((510,275,770,440)).resize((442,280),Image.Resampling.NEAREST);im.paste(pic,(x+24,y+44))
text(d,(25,835),'Голова опущена целиком; лицо не масштабировалось. PNG прежние, боковые UV подогнаны.',20,c='#716b62');im.save(D/'02-before-after.png')
im=Image.new('RGB',(800,710),'#f9f8f7');d=ImageDraw.Draw(im);text(d,(25,22),'Переход вокруг шеи · на 1 пиксель выше',28,True);text(d,(25,65),'Голова и задние волосы скрыты только в QA.',21,c='#716b62');pic=shot('neckOnly').crop((420,250,860,620)).resize((660,555),Image.Resampling.NEAREST);im.paste(pic,(70,100));text(d,(25,669),'Без костей и анимации; грудные пряди не привязаны к голове.',20);im.save(D/'03-neck-lift.png')
print('Saved standard views, before/after and neck reveal')
