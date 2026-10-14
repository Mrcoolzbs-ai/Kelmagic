from pathlib import Path
import sys,json
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R/'experiment/tools'));from renderer import render
A=R/'test-project/src/main/resources/assets/kelmagic';g=json.loads((A/'geo/red_mage.geo.json').read_text());tex=Image.open(A/'textures/entity/red_mage_geo.png');O=R/'evidence/technical';O.mkdir(exist_ok=True,parents=True)
for name in ['neutral','smile','speech','surprised']:
 png=Image.open(A/f'textures/entity/mouth_e/{name}.png')
 for view,yaw in [('front',0),('angle45',45),('diagnostic-front',0)]:render(g,tex,yaw,view.startswith('diagnostic'),mouth_png=png).save(O/f'{name}-{view}.png')
 print('TECHNICAL_RENDER',name,flush=True)
font='/usr/share/fonts/msttcore/arial.ttf';f=lambda n:ImageFont.truetype(font,n);im=Image.new('RGB',(1600,630),'#1c2d31');d=ImageDraw.Draw(im);d.text((16,12),'E · четыре оригинальных PNG-состояния рта',font=f(30),fill='#edf2f3');d.text((16,55),'Технические рендеры · не Minecraft · прозрачный слой, без геометрической полости',font=f(23),fill='#bacbce')
for i,(name,label) in enumerate(zip(['neutral','smile','speech','surprised'],['Нейтральный · закрыт','Улыбка','Открытый · тест речи','Удивление'])):
 d.text((i*400+12,104),label,font=f(23),fill='#edf2f3');img=Image.open(O/f'{name}-diagnostic-front.png').crop((155,300,645,680)).resize((390,303),Image.Resampling.NEAREST);im.paste(img,(i*400+5,145))
 png=Image.open(A/f'textures/entity/mouth_e/{name}.png').resize((256,128),Image.Resampling.NEAREST);check=Image.new('RGB',(256,128),'#39474a');check.paste(png,(0,0),png);im.paste(check,(i*400+72,470))
d.text((16,605),'Нижняя строка — PNG на служебном фоне. Этот фон не входит в текстуры.',font=f(21),fill='#bacbce');im.save(R/'evidence/PNG-mouth-technical.png')
