"""Recovery only: replay already-recorded F2 raster instructions; no new artwork/integration."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json,hashlib,shutil
B=Path(__file__).resolve().parent;R=B/'kelmagic-F2-art/art-F2';P=B/'E/test-project'
for s in ['emotions','visemes','preview','report','tools']:(R/s).mkdir(parents=True,exist_ok=True)
C={'ink':(89,32,46,255),'lip':(155,72,88,255),'inside':(137,58,78,255),'tongue':(205,113,135,255),'tooth':(246,222,217,255)}
def start():
 im=Image.new('RGBA',(32,16),(0,0,0,0));return im,ImageDraw.Draw(im)
def line(d,pts,c='ink'):d.line(pts,fill=C[c])
def rect(d,box,c):d.rectangle(box,fill=C[c])
def mirror(im):
 im.paste(im.crop((0,0,16,16)).transpose(Image.Transpose.FLIP_LEFT_RIGHT),(16,0));return im
meta=[]
def save(im,group,name,label,symmetric=True):
 if symmetric:im=mirror(im)
 p=R/group/(name+'.png');im.save(p);meta.append({'group':group,'id':name,'label':label,'canvas':[32,16],'bbox':im.getbbox(),'axisX':15.5,'symmetric':symmetric,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
im=Image.open(P/'art/red-mage/mouth_e/neutral.png');save(im,'emotions','neutral','Нейтральный · закрыт',False)
im,d=start();line(d,[(6,7),(7,9),(9,9),(10,10),(15,10)]);line(d,[(11,11),(15,11)],'lip');save(im,'emotions','smile','Лёгкая улыбка')
im,d=start();rect(d,(7,6,15,10),'inside');rect(d,(8,7,15,8),'tooth');line(d,[(6,6),(6,8),(7,10),(9,11),(12,12),(15,12)]);line(d,[(7,6),(15,6)]);line(d,[(11,11),(15,11)],'lip');save(im,'emotions','wide_smile','Широкая улыбка · зубы')
im,d=start();line(d,[(6,9),(10,10),(17,10),(21,9),(24,7),(25,6)]);line(d,[(12,11),(19,11)],'lip');save(im,'emotions','smirk','Хитрая усмешка',False)
im,d=start();line(d,[(8,10),(8,9),(15,9)]);line(d,[(11,10),(15,10)],'lip');save(im,'emotions','displeased','Недовольство')
im,d=start();rect(d,(13,4,15,11),'inside');line(d,[(14,3),(15,3)]);line(d,[(13,4),(12,5),(12,10),(13,11),(14,12),(15,12)]);line(d,[(14,11),(15,11)],'lip');save(im,'emotions','surprise','Удивление')
im,d=start();rect(d,(8,7,15,11),'inside');rect(d,(9,7,15,8),'tooth');line(d,[(7,8),(8,6),(15,6)]);line(d,[(7,8),(7,10),(9,12),(15,12)]);line(d,[(10,11),(15,11)],'lip');save(im,'emotions','fear','Испуг')
im,d=start();rect(d,(8,5,15,10),'inside');rect(d,(9,5,15,6),'tooth');rect(d,(11,11,15,12),'tongue');line(d,[(7,4),(15,4)]);line(d,[(7,4),(7,8),(8,10),(10,12),(12,13),(15,13)]);save(im,'emotions','laugh','Смех')
im,d=start();line(d,[(7,10),(9,9),(11,8),(13,7),(15,7)]);line(d,[(12,8),(15,8)],'lip');save(im,'emotions','sad','Грусть')
im,d=start();line(d,[(9,9),(15,9)]);line(d,[(10,10),(15,10)],'lip');save(im,'visemes','closed_mpb','М / П / Б · смыкание')
im,d=start();rect(d,(10,5,15,11),'inside');line(d,[(12,4),(15,4)]);line(d,[(11,5),(10,6),(9,7),(9,10),(10,12),(12,13),(15,13)]);line(d,[(12,12),(15,12)],'tongue');save(im,'visemes','open_a','А · открытая гласная')
im,d=start();rect(d,(13,5,15,11),'inside');line(d,[(14,4),(15,4)]);line(d,[(13,5),(12,6),(12,10),(13,12),(14,13),(15,13)]);line(d,[(14,12),(15,12)],'lip');save(im,'visemes','round_o','О · округлённая')
im,d=start();rect(d,(14,7,15,10),'inside');line(d,[(14,6),(15,6)],'lip');line(d,[(13,7),(13,10),(14,11),(15,11)]);save(im,'visemes','round_u','У · узкая округлённая')
im,d=start();rect(d,(8,8,15,10),'inside');line(d,[(8,7),(15,7)]);line(d,[(9,8),(15,8)],'tooth');line(d,[(7,8),(7,9),(9,11),(15,11)]);save(im,'visemes','spread_ie','И / Э · растянутая')
im,d=start();line(d,[(10,7),(15,7)]);rect(d,(10,8,15,8),'tooth');line(d,[(9,8),(10,10),(15,10)]);line(d,[(12,9),(15,9)],'lip');save(im,'visemes','teeth_fv','Ф / В · губа и зубы')
im,d=start();rect(d,(9,8,15,10),'inside');line(d,[(9,7),(15,7)]);line(d,[(10,8),(15,8)],'tooth');line(d,[(12,10),(15,10)],'tongue');line(d,[(8,8),(8,10),(10,11),(15,11)]);save(im,'visemes','tongue_tdnl','Т / Д / Н / Л')
im,d=start();rect(d,(11,7,15,10),'inside');line(d,[(12,6),(15,6)]);line(d,[(12,7),(15,7)],'tooth');line(d,[(10,7),(10,10),(12,12),(15,12)]);line(d,[(12,11),(15,11)],'lip');save(im,'visemes','hush_sh','Ш / Ж / Ч / Щ')
im,d=start();line(d,[(9,9),(11,10),(15,10)]);save(im,'visemes','rest','Переход · покой')
shutil.copy2(P/'art/red-mage/mouth_e/smile.png',R/'preview/smile-before.png')
(R/'report/library-manifest.json').write_text(json.dumps(meta,ensure_ascii=False,indent=2))
font='/usr/share/fonts/msttcore/arial.ttf';F=lambda n:ImageFont.truetype(font,n)
def sprite(path):
 im=Image.open(path).convert('RGBA');assert im.size==(32,16);big=im.resize((256,128),Image.Resampling.NEAREST);bg=Image.new('RGBA',(256,128));d=ImageDraw.Draw(bg)
 for y in range(0,128,16):
  for x in range(0,256,16):d.rectangle((x,y,x+15,y+15),fill=('#24343e' if (x//16+y//16)%2==0 else '#547181'))
 bg.alpha_composite(big);return bg.convert('RGB')
E=[('neutral','Нейтральный рот'),('smile','Лёгкая улыбка'),('wide_smile','Широкая улыбка · зубы'),('smirk','Хитрая усмешка'),('displeased','Недовольство'),('surprise','Удивление'),('fear','Испуг'),('laugh','Смех'),('sad','Грусть')]
V=[('closed_mpb','М / П / Б · смыкание губ'),('open_a','А · открытая гласная'),('round_o','О · округлённая'),('round_u','У · узкая округлённая'),('spread_ie','И / Э · растянутая'),('teeth_fv','Ф / В · губа и зубы'),('tongue_tdnl','Т / Д / Н / Л'),('hush_sh','Ш / Ж / Ч / Щ'),('rest','Переходное состояние')]
def board(group,items,title,height):
 im=Image.new('RGB',(1080,height),'#14232a');d=ImageDraw.Draw(im);d.text((24,16),title,font=F(29),fill='#f3f5f6');d.text((24,57),'32×16 → 256×128 · ровно 8× · nearest-neighbor · PNG без изменений',font=F(21),fill='#c3d4de')
 for i,(name,label) in enumerate(items):
  x=24+(i%3)*352;y=100+(i//3)*212;d.rounded_rectangle((x-4,y-5,x+332,y+196),radius=5,fill='#1e323c');d.text((x+7,y+5),label,font=F(20),fill='#f3f5f6');d.text((x+7,y+31),name+'.png',font=F(15),fill='#acc2cd');im.paste(sprite(R/group/(name+'.png')),(x+36,y+59))
 return im
im=board('emotions',E,'F2 · эмоциональные состояния',1000);d=ImageDraw.Draw(im);d.text((24,758),'Сравнение улыбки · те же 8×, тот же фон',font=F(25),fill='#f3f5f6')
for x,label,p in [(70,'Исходная улыбка',R/'preview/smile-before.png'),(610,'Исправленная улыбка',R/'emotions/smile.png')]:d.text((x,802),label,font=F(21),fill='#f3f5f6');im.paste(sprite(p),(x,837))
d.text((24,975),'Шахматный фон обозначает прозрачность и не входит в PNG.',font=F(17),fill='#c3d4de');im.save(R/'preview/F2-emotions-contact-8x.png')
im=board('visemes',V,'F2 · виземы: художественные кандидаты',790);d=ImageDraw.Draw(im);d.text((24,756),'Шахматный фон — только превью. Это группы форм, не посимвольная артикуляция.',font=F(19),fill='#c3d4de');im.save(R/'preview/F2-visemes-contact-8x.png')
allpng=[*list((R/'emotions').glob('*.png')),*list((R/'visemes').glob('*.png')),R/'preview/smile-before.png'];assert len(allpng)==19
for p in allpng:
 im=Image.open(p);assert im.size==(32,16) and im.mode=='RGBA'
report={'recoveredFrom':'Recorded generator and contact-sheet commands in transcript, events 13139 and 13232','originalF2DirectoryAvailable':False,'originalF2BinaryHashComparisonPossible':False,'artChangesOrIntegration':False,'PNGcount':19,'recoverySHA256':{str(p.relative_to(R)):hashlib.sha256(p.read_bytes()).hexdigest() for p in allpng},'contactScale':8,'method':'NEAREST','note':'No claim that re-encoded PNG bytes/contact-sheet font raster match inaccessible original F2 artifacts.'};(R/'report/RECOVERY-PROVENANCE.json').write_text(json.dumps(report,ensure_ascii=False,indent=2))
(R/'README-RECOVERY.md').write_text('''# F2 — recovered art-review snapshot\n9 эмоций + 9 визем + прежняя улыбка (19 исходных PNG). Не утверждено, не интегрировано.\nФайлы прежнего окружения недоступны. Рисунки восстановлены по сохранённым командам генерации; контактные листы — по сохранённой команде 8× nearest-neighbor. Побайтовую идентичность с утраченными F2 проверить невозможно. См. report/RECOVERY-PROVENANCE.json.\nЭто сохранение существующего рисунка, не новый художественный вариант. Java, UV, геометрия и ресурсы E не менялись.\nПолная контрольная E лежит в соседнем experiments/E; в полном переносе test-project этой F2-копии также сохранён отдельно без интеграции.\n''')
shutil.copy2(__file__,R/'tools/restore_F2_from_transcript.py')
print('F2_RECOVERED 19 sprites and 2 contact sheets; prior binary hashes unavailable')
