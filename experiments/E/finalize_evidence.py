from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import re,json,hashlib,shutil
R=Path(__file__).resolve().parent;S=R/'evidence/native/screenshots';O=R/'evidence'; font='/usr/share/fonts/msttcore/arial.ttf';f=lambda n:ImageFont.truetype(font,n)
files=sorted(S.glob('*.png')); rows={}
for l in (R/'report/native-final.log').read_text().splitlines():
 if l.startswith('MOUTH_SAMPLE '):
  p=dict(re.findall(r'(\w+)=([^ ]+)',l));rows.setdefault(p['stage'],[]).append(p)
summary={}
for st,rs in rows.items():
 summary[st]={'samples':len(rs),'selected':sorted({x['selected'] for x in rs}),'oldMouthHidden':all(x['oldMouthHidden']=='true' for x in rs),'range':{k:[min(float(x[k]) for x in rs),max(float(x[k]) for x in rs)] for k in ['noseY','chestY','chestZ','legacyOpening','eyes','cheeks','head']}}
(R/'report/game-samples.json').write_text(json.dumps(summary,indent=2))
# Four state front +45: exact screenshots, nearest-neighbour crops.
im=Image.new('RGB',(1600,880),'#1c2d31');d=ImageDraw.Draw(im)
d.text((16,12),'Minecraft · E · PNG-рот вместо геометрической полости',font=f(29),fill='#edf2f3');d.text((16,53),'Настоящие игровые кадры · сверху фронт, снизу 45° · без сторонних шейдеров',font=f(22),fill='#bacbce')
for i,(st,label) in enumerate(zip([0,2,4,6],['Нейтральный · закрыт','Улыбка','Открытый · тест речи','Удивление'])):
 d.text((i*400+10,92),label,font=f(22),fill='#edf2f3')
 for j,stage in enumerate([st,st+1]):
  img=Image.open(next(S.glob(f'{stage:02d}-*-040.png')));box=(346,310,614,553) if j==0 else (520,310,788,553)
  img=img.crop(box).resize((390,350),Image.Resampling.NEAREST);im.paste(img,(i*400+5,130+j*365))
d.text((16,852),'Исходный атлас сохранён. Старый рот скрыт только в тестовой копии E.',font=f(21),fill='#bacbce');im.save(O/'Minecraft-PNG-mouth.png')
# Representative independent eyes and head turn + breath toggle exact full-frame thumbnails.
im=Image.new('RGB',(1440,855),'#1c2d31');d=ImageDraw.Draw(im);d.text((15,8),'Minecraft · независимость рта, дыхание и поворот головы',font=f(27),fill='#edf2f3')
ind=rows['eyes-cheeks-independent'];openframe=int(max(ind,key=lambda r:float(r['eyes']))['frame']);closedframe=int(min(ind,key=lambda r:float(r['eyes']))['frame'])
sel=[(8,openframe,'Улыбка + прищур и щёки'),(8,closedframe,'Та же улыбка + моргание'),(9,40,'Поворот головы · рот закрыт'),(0,40,'Дыхание включено'),(10,40,'Дыхание отключено · возврат'),(11,40,'Дыхание восстановлено')]
for i,(st,frame,label) in enumerate(sel):
 x=(i%3)*480;y=50+(i//3)*392;d.text((x+9,y),label,font=f(20),fill='#edf2f3');img=Image.open(next(S.glob(f'{st:02d}-*-{frame:03d}.png'))).resize((480,360),Image.Resampling.NEAREST);im.paste(img,(x,y+28))
d.text((15,838),'Нос: ±0,012 единицы модели. Статические кадры не показывают столь малую амплитуду надёжно.',font=f(17),fill='#bacbce');im.save(O/'Minecraft-independence-breathing.png')
# PNG frame sequence retains original screenshots and pace (one every two game ticks = 10/s).
V=O/'video-frames';V.mkdir(exist_ok=True);n=0
labels=['Нейтральный рот · дыхание','Нейтральный · 45°','Улыбка','Улыбка · 45°','Открытый рот · тест речи','Открытый · 45°','Удивление','Удивление · 45°','Улыбка + веки / щёки / серия морганий','Поворот головы · нейтральный рот','Дыхание отключено','Дыхание восстановлено']
for path in files:
 st=int(path.name.split('-')[0]);img=Image.new('RGB',(960,765),'#1c2d31');d=ImageDraw.Draw(img);d.text((12,9),'Minecraft · E · '+labels[st],font=f(25),fill='#edf2f3');img.paste(Image.open(path),(0,45));img.save(V/f'{n:05d}.png');n+=1
before=json.loads((R/'report/E-before.json').read_text()); P=R/'test-project';changes=[]
for s,h in before.items():
 p=P/s
 if not p.exists(): changes.append({'path':s,'change':'removed'})
 elif hashlib.sha256(p.read_bytes()).hexdigest()!=h: changes.append({'path':s,'change':'modified'})
for prefix in ['src','art','tools']:
 for p in (P/prefix).rglob('*'):
  if p.is_file() and '__pycache__' not in str(p) and str(p.relative_to(P)) not in before:changes.append({'path':str(p.relative_to(P)),'change':'added'})
(R/'report/changed-files.json').write_text(json.dumps(changes,indent=2));print('GAME',len(files),'screens',len(rows),'stages');print('RANGES',json.dumps({k:v['range'] for k,v in summary.items() if k in ['neutral-breath','breathing-off','eyes-cheeks-independent']}));print('CHANGES',len(changes))
