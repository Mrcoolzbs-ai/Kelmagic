from pathlib import Path
import re
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parent;S=R/'evidence/native/screenshots';font='/usr/share/fonts/msttcore/arial.ttf'
f=lambda n:ImageFont.truetype(font,n)
im=Image.new('RGB',(1500,930),'#1c2d31');d=ImageDraw.Draw(im);d.text((16,12),'Minecraft · E · нейтральное лицо, подъём щёк и асимметрия',font=f(30),fill='#edf2f3');d.text((16,54),'Сверху — фронт · снизу — 45° · реальные кадры, без дорисовки · одинаковый масштаб кропов',font=f(22),fill='#bacbce')
rows=[['00-neutral-front-060.png','04-smile-squint-front-060.png','06-asymmetric-front-060.png'],['01-neutral-45-060.png','05-smile-squint-45-060.png','07-asymmetric-45-060.png']]
samples=[]
for line in (R/'report/native-final.log').read_text().splitlines():
 if line.startswith('CHEEK_SAMPLE '):samples.append(dict(re.findall(r'(\w+)=([^ ]+)',line)))
for j,row in enumerate(rows):
 for i,n in enumerate(row):
  stage=n[3:-8];choices=[v for v in samples if v['stage']==stage]
  if choices:
   v=min(choices,key=lambda d:(float(d['blink']),abs(int(d['frame'])-60)));n=n[:3]+stage+'-'+str(int(v['frame'])).zfill(3)+'.png'
  x=i*500;y=100+j*400;d.text((x+15,y),['Нейтральное','Подъём под улыбку + прищур','Асимметрия'][i],font=f(23),fill='#edf2f3');img=Image.open(S/n).crop((365,320,690,550)).resize((480,340),Image.Resampling.NEAREST);im.paste(img,(x+10,y+35))
d.text((16,907),'PNG не изменялся. Нос, рот и центральная область не участвуют в деформации щёк.',font=f(21),fill='#bacbce');im.save(R/'evidence/Minecraft-E-angles.png')
