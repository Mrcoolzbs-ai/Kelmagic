from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
M=Path(__file__).resolve().parents[2];R=Path('/data/reports/kelmagic-f3-hair7-neck-fix');D=R/'deliverables';D.mkdir(exist_ok=True);S=R/'native/screenshots'
F='/usr/share/fonts/msttcore/arial.ttf';B='/usr/share/fonts/msttcore/arialbd.ttf'
def text(d,xy,s,size=21,b=False,c='#2c2c2b'):d.text(xy,s,font=ImageFont.truetype(B if b else F,size),fill=c)
def shot(folder,n):return Image.open(max(folder.glob(n+'-*.png'),key=lambda f:int(f.stem.rsplit('-',1)[1]))).convert('RGB')
im=Image.new('RGB',(1480,640),'#f9f8f7');d=ImageDraw.Draw(im);text(d,(25,22),'Hair7 · грудные пряди → плечи → вокруг шеи',30,True);text(d,(25,66),'Верх грудных прядей ограничен плечами. Вместо высоких карточек — низкое продолжение по линии шеи.',21,c='#716b62')
for i,(n,title) in enumerate([('front','Спереди'),('side','Сбоку'),('rear','Сзади'),('head45','Голова 45°'),('head90','Голова 90°')]):
 x=25+i*290;d.rounded_rectangle((x,112,x+276,550),radius=8,fill='white',outline='#dedad4');text(d,(x+12,124),title,23,True)
 pic=shot(S,n).crop((500,240,780,640)).resize((252,360),Image.Resampling.NEAREST);im.paste(pic,(x+12,166))
text(d,(25,575),'Голова, бока, спина и прежние PNG не изменены. Новых костей и объёмных прядей нет.',20)
text(d,(25,610),'90° — QA-стресс-тест, игровой ограничитель прежний. Реальные кадры Minecraft / GeckoLib; кропы без ретуши.',20,c='#716b62');im.save(D/'01-hair7-native.png')
# Head turn: fixed chest roots aligned in the same camera, no morph/retouch.
im=Image.new('RGB',(1000,650),'#f9f8f7');d=ImageDraw.Draw(im);text(d,(25,22),'F3-hair7 · грудные волосы при повороте головы',28,True);text(d,(25,64),'Один ракурс камеры; верхние переходы hair6, читавшие head, удалены.',21,c='#716b62')
for i,(n,t) in enumerate([('front','0°'),('head45','45°'),('head90','90°')]):
 x=25+i*325;text(d,(x+12,112),t,24,True);pic=shot(S,n).crop((490,330,790,650));im.paste(pic,(x+8,151))
text(d,(25,501),'Шея: только плоские RGBA-поверхности с постоянными координатами;',22)
text(d,(25,534),'без GeoBone, матриц головы/груди и анимационных каналов.',22)
text(d,(25,590),'Короткая проверка не доказывает отсутствие clipping во всех движениях.',20,c='#716b62');im.save(D/'02-chest-binding.png')
# Actual additional neck alpha assets, never overwrite previous hair materials.
im=Image.new('RGB',(1050,580),'#f9f8f7');d=ImageDraw.Draw(im);text(d,(25,20),'F3-hair7 · новые плоские текстуры шеи',29,True)
for row,zone in enumerate(['side','back']):
 text(d,(25,125+row*210),zone,22,True)
 for col,tier in enumerate(['under','over']):
  x=150+col*430;y=85+row*210;text(d,(x,y),tier+' · RGBA / alpha',20);a=Image.open(M/f'src/main/resources/assets/kelmagic/textures/entity/hair7/{zone}_{tier}.png').convert('RGBA');bg=Image.new('RGBA',a.size,'#e2e6e8');dd=ImageDraw.Draw(bg)
  for yy in range(0,64,8):
   for xx in range(0,64,8):
    if (xx//8+yy//8)%2:dd.rectangle((xx,yy,xx+7,yy+7),fill='#a3b4ba')
  bg.alpha_composite(a);im.paste(bg.convert('RGB').resize((160,160),Image.Resampling.NEAREST),(x,y+32));im.paste(a.getchannel('A').convert('RGB').resize((160,160),Image.Resampling.NEAREST),(x+195,y+32))
text(d,(25,535),'Белый = непрозрачно, чёрный = прозрачно. Палитра взята из существующего атласа.',20,c='#716b62');im.save(D/'03-neck-alpha.png')
print('Saved three proof sheets')

# Additional native reveal: only QA hides head and back hair to expose shoulder-level U.
im=Image.new('RGB',(800,720),'#f9f8f7');d=ImageDraw.Draw(im);text(d,(25,22),'Где находится новый переход',30,True);text(d,(25,67),'Голова и задние волосы скрыты только в QA.',21,c='#716b62')
pic=shot(R/'reveal/screenshots','neckOnly').crop((420,250,860,620)).resize((660,555),Image.Resampling.NEAREST);im.paste(pic,(70,108))
text(d,(25,674),'Низкий переход по плечам, не вертикальные панели у головы.',21);im.save(D/'04-neck-at-shoulders.png')
