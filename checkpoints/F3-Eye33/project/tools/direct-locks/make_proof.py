from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
M=Path(__file__).resolve().parents[2];R=Path('/data/reports/kelmagic-f3-hair7-direct-locks');D=R/'deliverables';D.mkdir(exist_ok=True);S=R/'native/screenshots'
F='/usr/share/fonts/msttcore/arial.ttf';B='/usr/share/fonts/msttcore/arialbd.ttf'
def text(d,xy,s,size=21,b=False,c='#2c2c2b'):d.text(xy,s,font=ImageFont.truetype(B if b else F,size),fill=c)
def shot(n):return Image.open(max(S.glob(n+'-*.png'),key=lambda f:int(f.stem.rsplit('-',1)[1]))).convert('RGB')
im=Image.new('RGB',(1140,1050),'#f9f8f7');d=ImageDraw.Draw(im);text(d,(25,22),'Hair7 · напрямую от угла груди к подбородку',29,True);text(d,(25,65),'Верхний участок — одна диагональ, без промежуточного подъёма и перегиба у плеча.',21,c='#716b62')
for i,(n,label) in enumerate([('front','Спереди'),('quarter','Камера 45°'),('side','Сбоку'),('rear','Сзади'),('head45','Голова 45°'),('head90','Голова 90°')]):
 x=25+i%3*375;y=110+i//3*425;d.rounded_rectangle((x,y,x+350,y+405),radius=8,fill='white',outline='#dedad4');text(d,(x+12,y+12),label,23,True);pic=shot(n).crop((485,230,795,620)).resize((279,351),Image.Resampling.NEAREST);im.paste(pic,(x+35,y+47))
text(d,(25,980),'Посадка головы, короткий подбородок и прочие волосы сохранены. Пряди не привязаны к голове.',20)
text(d,(25,1014),'Реальные кадры Minecraft / GeckoLib. 90° — QA-стресс-тест; кропы без ретуши.',20,c='#716b62');im.save(D/'01-standard-views.png')
im=Image.new('RGB',(1040,890),'#f9f8f7');d=ImageDraw.Draw(im);text(d,(25,22),'Верх грудных прядей · было / стало',29,True);text(d,(25,66),'Удалена промежуточная точка. Теперь линия соединяет угол груди и нижний край подбородка.',20,c='#716b62')
for row,n in enumerate(['quarter','side']):
 before=Image.open(M/'reports/F3-hair7-direct-locks/before-native'/(n+'.png')).convert('RGB')
 for col,(label,pic) in enumerate([('Было',before),('Стало',shot(n))]):
  x=25+col*510;y=110+row*350;d.rounded_rectangle((x,y,x+490,y+330),radius=8,fill='white',outline='#dedad4');text(d,(x+12,y+10),label+' · '+('45°' if n=='quarter' else 'бок'),22,True);pic=pic.crop((510,340,770,505)).resize((442,280),Image.Resampling.NEAREST);im.paste(pic,(x+24,y+44))
text(d,(25,835),'Изменены только шесть mesh-групп двух грудных прядей. Остальные части модели прежние.',20,c='#716b62');im.save(D/'02-before-after.png');print('Proof sheets ready')
