from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).parent;O=R/'comparisons';O.mkdir(exist_ok=True);f='/usr/share/fonts/msttcore/arial.ttf'
font=lambda n:ImageFont.truetype(f,n)
labels=['Исходная 1.23.0','A · Выразительные глаза','B · Объём лица','C · Геометрия + текстура']
keys=['Original','A','B','C'];bg=(26,42,46);fg=(240,244,243);muted=(183,201,203)
def sheet(name,title,subtitle,kind,detail=False):
 w=2000;h=560 if detail else 656
 im=Image.new('RGB',(w,h),bg);d=ImageDraw.Draw(im);d.text((26,18),title,font=font(30),fill=fg);d.text((26,60),subtitle,font=font(20),fill=muted)
 for i,(s,label) in enumerate(zip(keys,labels)):
  x=20+i*495;d.text((x+5,103),label,font=font(22),fill=fg)
  src=Image.open(R/'renders'/f'{s}-{kind}.png')
  if detail:
   src=src.crop((214,304,586,564));src.save(R/'renders'/f'{s}-details.png');src=src.resize((472,330),Image.Resampling.NEAREST)
  else:src=src.resize((475,475),Image.Resampling.LANCZOS)
  im.paste(src,(x,143))
 d.text((26,h-28),'Рендер настоящих .geo.json и UV · не скриншот из игры · без новой анимации',font=font(17),fill=muted);im.save(O/name)
sheet('01-front.png','Лицо спереди','Одинаковая нейтральная поза, ортографическая камера, фиксированный свет и расчёт теней','front')
sheet('02-angle45.png','Ракурс 45°','Волосы, шляпа и их исходная нейтральная поза сохранены во всех вариантах','angle45')
sheet('03-profile-face.png','Профиль · диагностический вид','Волосы и шляпа скрыты только в рендере: сравнение силуэта самого лица','profile-face')
sheet('04-details.png','Глаза и рот · увеличенный фрагмент','Одинаковый кроп реальных фронтальных рендеров; увеличение без дорисовки','front',True)
sheet('05-volume45.png','Объём лица под 45° · диагностический вид','Скрыты только волосы и шляпа: глубина глазниц, выступ щёк и форма подбородка','angle45-face')
sheet('06-full-profile.png','Обычный профиль · волосы на месте','Лицо закрыто существующими боковыми волосами; различий здесь почти не видно','profile')
