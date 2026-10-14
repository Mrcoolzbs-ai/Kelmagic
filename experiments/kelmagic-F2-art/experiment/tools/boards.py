from PIL import Image,ImageDraw,ImageFont
from pathlib import Path
p=Path(__file__).resolve().parents[1]; out=p/'evidence'; raw=out/'raw'
font='/usr/share/fonts/msttcore/arial.ttf'
def f(n):return ImageFont.truetype(font,n)
bg='#1c2d31';fg='#edf2f3';mut='#bacbce'
states=['E0-neutral','E1-subtle','E2-smile-lift','E3-asymmetric']
labels=['E0 · нейтральное','E1 · небольшой подъём','E2 · усиленный подъём','E3 · асимметрия']
for view,title in [('front','Спереди'),('angle45','Ракурс 45°')]:
 im=Image.new('RGB',(2000,590),bg);d=ImageDraw.Draw(im)
 d.text((20,14),'Эксперимент E · '+title,font=f(30),fill=fg)
 d.text((20,57),'Статические технические рендеры экспорта · НЕ кадры Minecraft · одинаковые камера, свет и масштаб',font=f(22),fill=mut)
 for i,(name,label) in enumerate(zip(['D-approved']+states,['Утверждённый D']+labels)):
  d.text((i*400+12,103),label,font=f(23),fill=fg)
  img=Image.open(raw/f'{name}-{view}.png').resize((400,400),Image.Resampling.NEAREST);im.paste(img,(i*400,145))
 d.text((20,556),'В E исправлено локальное отражение румянца. E2 — подъём под улыбку; рот не изменён.',font=f(22),fill=mut)
 im.save(out/f'E-{view}.png')
im=Image.new('RGB',(1920,990),bg);d=ImageDraw.Draw(im)
d.text((20,12),'Эксперимент E · поверхность без волос и шляпы',font=f(30),fill=fg)
d.text((20,54),'Только диагностический рендер · верхние ресницы, радужки, нос и рот сохранены',font=f(23),fill=mut)
for j,view in enumerate(['diagnostic-front','diagnostic45']):
 for i,(name,label) in enumerate(zip(states,labels)):
  x=i*480;y=100+j*420;d.text((x+12,y),label+(' · 45°' if j else ' · фронт'),font=f(22),fill=fg)
  box=(150,290,650,670) if not j else (345,290,745,670)
  img=Image.open(raw/f'{name}-{view}.png').crop(box).resize((470,357),Image.Resampling.NEAREST)
  im.paste(img,(x+5,y+36))
d.text((20,952),'Подъём щёк: Y до 0,16 и выдвижение до 0,04 единицы модели. Румянец движется целиком.',font=f(23),fill=mut)
im.save(out/'E-diagnostic.png')
