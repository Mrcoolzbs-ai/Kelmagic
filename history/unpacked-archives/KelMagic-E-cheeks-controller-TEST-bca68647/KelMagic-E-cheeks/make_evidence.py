from pathlib import Path
import re,json
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parent;O=R/'evidence';S=O/'native/screenshots';bg='#1c2d31';fg='#edf2f3';mut='#bacbce';font='/usr/share/fonts/msttcore/arial.ttf'
def f(n):return ImageFont.truetype(font,n)
rows=[]
for l in (R/'report/native-final.log').read_text().splitlines():
 if l.startswith('CHEEK_SAMPLE '):
  v=dict(re.findall(r'(\w+)=([^ ]+)',l));rows.append(v)
by={}
for v in rows:by.setdefault(v['stage'],[]).append(v)
names=['neutral-front','neutral-45','normal-blink','squint-front','smile-squint-front','smile-squint-45','asymmetric-front','asymmetric-45','burst-squint-smile','return-squint-smile','head-turn','angry-speech']
def file(stage,frame):return S/f'{names.index(stage):02d}-{stage}-{int(frame):03d}.png'
def select(stage,closed=False):
 a=by[stage]
 if closed:return min(a,key=lambda d:float(d['upperMask']) if False else -float(d['upperMask']))
 return min(a,key=lambda d:(abs(float(d['blink'])),abs(int(d['frame'])-60)))
entries=[('Нейтральное лицо','neutral-front',False),('Обычное моргание · закрыто','normal-blink',True),('Прищур + небольшой подъём','squint-front',False),('Подъём под улыбку + прищур','smile-squint-front',False),('Асимметричный подъём','asymmetric-front',False),('Серия морганий · закрыто','burst-squint-smile',True),('Возврат после серии','return-squint-smile',False),('Моргание + поворот головы','head-turn',True),('Существующая речь / сердитость','angry-speech',False)]
im=Image.new('RGB',(1500,1440),bg);d=ImageDraw.Draw(im)
d.text((18,13),'Minecraft · эксперимент E · щёки и нижние веки',font=f(30),fill=fg)
d.text((18,55),'Настоящие игровые кадры · штатный рендер · исходный PNG не менялся · световые сенсоры отключены',font=f(21),fill=mut)
selected={}
for i,(label,stage,closed) in enumerate(entries):
 row=select(stage,closed);path=file(stage,row['frame']);selected[stage]={'file':path.name,'sample':row}
 x=(i%3)*500;y=105+(i//3)*430;d.text((x+12,y),label,font=f(23),fill=fg)
 img=Image.open(path).crop((375,325,590,515)).resize((480,424),Image.Resampling.NEAREST)
 # fit cell without stretching: uniform scaling and lower crop, identical for all frontal views
 img=img.resize((440,389),Image.Resampling.NEAREST);im.paste(img,(x+30,y+36))
d.text((18,1410),'«Под улыбку» — тест подъёма щёк, без нового движения рта. Увеличенные кропы игровых кадров.',font=f(21),fill=mut)
im.save(O/'Minecraft-E-states.png')
# Full real frames for direct front/45 comparison.
im=Image.new('RGB',(1920,840),bg);d=ImageDraw.Draw(im);d.text((16,12),'Minecraft · исправленный E · нейтральное лицо спереди и под 45°',font=f(30),fill=fg);d.text((16,55),'Кадры из тестового мира · волосы и шляпа на месте · без сторонних шейдеров',font=f(23),fill=mut)
for i,stage in enumerate(['neutral-front','neutral-45']):
 row=select(stage);im.paste(Image.open(file(stage,row['frame'])),(i*960,100))
im.save(O/'Minecraft-E-neutral.png')
# Static UV cause: previous real resource reference vs E0, no draw-over.
p=R/'experiment/evidence/raw';im=Image.new('RGB',(1600,620),bg);d=ImageDraw.Draw(im)
d.text((18,12),'Румянец · локальная коррекция направления U',font=f(30),fill=fg);d.text((18,55),'Технические рендеры · одинаковые камера и свет · PNG и геометрия нейтрального лица не изменены',font=f(22),fill=mut)
for i,(n,label) in enumerate([('D-approved','Прежнее отображение · пятно рядом с носом'),('E0-neutral','Исправлено только в E · пятно на щеке')]):
 d.text((i*800+15,98),label,font=f(24),fill=fg);img=Image.open(p/f'{n}-diagnostic-front.png').crop((150,330,650,610)).resize((760,426),Image.Resampling.NEAREST);im.paste(img,(i*800+20,140))
d.text((18,582),'Границы участка атласа сохранены: U 958,8→921,2 заменено на 921,2→958,8. Исходный D нетронут.',font=f(23),fill=mut)
im.save(O/'UV-blush-correction.png')
# Telemetry summary for FINAL run only.
summary={}
for n,a in by.items():
 summary[n]={'samples':len(a),'minimumOpening':min(float(v['open']) for v in a),'maximumActualUpperMask':max(float(v['upperMask']) for v in a),'cheekLeftRange':[min(float(v['cheekL']) for v in a),max(float(v['cheekL']) for v in a)],'cheekRightRange':[min(float(v['cheekR']) for v in a),max(float(v['cheekR']) for v in a)],'burstSamples':sum(v['burst']=='true' for v in a)}
(R/'report/native-telemetry-summary.json').write_text(json.dumps(summary,indent=2));(R/'report/selected-frames.json').write_text(json.dumps(selected,indent=2))
# 10fps evidence sequence: one actual screenshot per 2 game ticks, not a new render.
V=R/'video-frames';V.mkdir(exist_ok=True);counter=0
for i,n in enumerate(names):
 a=by[n]
 # neutral abbreviated; retain whole blinking/burst transitions, clipped only the initial scene-settle.
 if i<2:a=a[:20]
 for v in a:
  img=Image.open(file(n,v['frame'])).convert('RGB');can=Image.new('RGB',(960,780),bg);can.paste(img,(0,60));dd=ImageDraw.Draw(can)
  labels=['Нейтральное · фронт','Нейтральное · 45°','Обычное моргание','Прищур + небольшой подъём','Подъём под улыбку + прищур','Подъём под улыбку · 45°','Асимметрия · фронт','Асимметрия · 45°','Быстрая серия поверх подъёма','Возврат к устойчивому состоянию','Моргание и поворот головы','Существующая речь и сердитость']
  dd.text((12,5),'Minecraft · E · '+labels[i],font=f(24),fill=fg);dd.text((12,34),'Реальные игровые кадры · выборка 10 кадров/игровую секунду · не технический рендер',font=f(18),fill=mut)
  can.save(V/f'{counter:05d}.png');counter+=1
print('Final QA frames',len(rows),'video frames',counter)
