from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parent;O=R/'comparisons';O.mkdir(exist_ok=True)
F='/usr/share/fonts/msttcore/arial.ttf';font=lambda n:ImageFont.truetype(F,n)
keys=['Original','B','C','D'];labels=['Исходная 1.23.0','B · Объёмная основа','C · Текстурные акценты','D · Гибрид B+C']
def sheet(name,title,sub,view,eyes=False):
 im=Image.new('RGB',(2000,452 if eyes else 656),(26,42,46));d=ImageDraw.Draw(im);d.text((26,18),title,font=font(30),fill=(240,244,243));d.text((26,60),sub,font=font(20),fill=(183,201,203))
 for i,(s,label) in enumerate(zip(keys,labels)):
  x=20+i*495;d.text((x+5,103),label,font=font(22),fill=(240,244,243));src=Image.open(R/'renders'/f'{s}-{view}.png')
  if eyes:
   src=src.crop((215,304,585,480));src.save(R/'renders'/f'{s}-eyes.png');src=src.resize((475,226),Image.Resampling.NEAREST)
  else:src=src.resize((475,475),Image.Resampling.LANCZOS)
  im.paste(src,(x,143))
 d.text((26,im.height-28),'Технический рендер фактических .geo.json + UV · не кадр из игры · одинаковая существующая нейтральная поза',font=font(17),fill=(183,201,203));im.save(O/name)
sheet('01-front.png','Спереди · нейтральный статический взгляд','Одинаковый масштаб, ортографическая камера, фиксированный свет и тени','front')
sheet('02-angle45.png','Ракурс 45°','Волосы и шляпа сохранены; сравнение геометрии и текстур при одинаковом освещении','angle45')
sheet('03-eyes.png','Глаза крупным планом','Одинаковый кроп фронтальных рендеров; без дорисовки или изменения положения глаз','front',True)
sheet('04-profile-diagnostic.png','Профиль · диагностический вид','Волосы и шляпа скрыты ТОЛЬКО в рендере; профиль головы, носа и перехода к подбородку','profile-face')
sheet('05-profile-normal.png','Профиль · обычные волосы на месте','Существующие волосы закрывают лицо; их цвет, геометрия и нейтральная поза не менялись','profile')
sheet('06-volume45-diagnostic.png','Объём под 45° · диагностический вид','Без волос и шляпы только в превью; у D горизонтальная полка заменена наклонным переходом','angle45-face')
# D review card for profile: actual normal vs diagnostic, not an edited game screenshot.
im=Image.new('RGB',(1320,810),(26,42,46));d=ImageDraw.Draw(im);d.text((26,18),'D · профиль: обычный и диагностический',font=font(30),fill='white')
for x,v,label in [(20,'profile','Обычный профиль · волосы на месте'),(680,'profile-face','Диагностика · волосы и шляпа скрыты')]:
 d.text((x,78),label,font=font(23),fill=(224,235,235));im.paste(Image.open(R/'renders'/f'D-{v}.png').resize((620,620),Image.Resampling.LANCZOS),(x,120))
d.text((26,776),'Технические рендеры одной модели · не скриншоты Minecraft',font=font(21),fill=(183,201,203));im.save(O/'D-profiles.png')
