from pathlib import Path
import json,zipfile,io,random,copy
from PIL import Image,ImageDraw
r=Path('src/main/resources');a=r/'assets/kelmagic';d=r/'data/kelmagic'
def write(p,j):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(j,ensure_ascii=False,indent=2)+'\n')
removed=['grove_leaves','threshold_travel','living_fiber','rite_threshold_link']
for n in removed:(d/f'advancement/{n}.json').unlink(missing_ok=True)
for name,parent in [('threshold_pair','combination'),('rite_grove_awakening','combination'),('magic_turf','rite_grove_awakening'),('archive_storage','midnight_wood')]:
 p=d/f'advancement/{name}.json';j=json.loads(p.read_text());j['parent']='kelmagic:'+parent
 if name in ['threshold_pair','rite_grove_awakening']:j['display'].update(frame='challenge',hidden=True)
 write(p,j)
j=json.loads((d/'advancement/magic_turf.json').read_text());j['parent']='kelmagic:archive_table';j['display']['icon']['id']='kelmagic:archivist_table';j['display']['title']['translate']='advancements.kelmagic.word_born_power.title';j['display']['description']['translate']='advancements.kelmagic.word_born_power.description';write(d/'advancement/word_born_power.json',j)
z=zipfile.ZipFile('/data/.agent-service/files/25d82891-88e1-4fdc-b8c5-976788611130/KelMagic_R10_Dreams_TRANSFER.zip')
for name in ['midnight_leaves','golden_midnight_leaves']:
 suffix=f'/assets/kelmagic/textures/block/{name}.png';im=Image.open(io.BytesIO(z.read(next(n for n in z.namelist() if n.endswith(suffix))))).convert('RGBA');coords=[(x,y) for y in range(im.height) for x in range(im.width) if im.getpixel((x,y))[3]>0];random.Random(1234).shuffle(coords)
 for x,y in coords[:round(len(coords)*.125)]:c=im.getpixel((x,y));im.putpixel((x,y),c[:3]+(0,))
 im.save(a/f'textures/block/{name}.png')
 print(name,'original opaque',len(coords),'removed',round(len(coords)*.125))
# Non-overlapping legs, rails, endboards, mattress. No shared exterior planes.
def box(lo,hi,t):
 dx,dy,dz=[hi[i]-lo[i] for i in range(3)];return {'from':lo,'to':hi,'faces':{f:{'texture':'#'+t,'uv':[0,0,min(16,w),min(16,h)]} for f,w,h in [('up',dx,dz),('down',dx,dz),('north',dx,dy),('south',dx,dy),('east',dz,dy),('west',dz,dy)]}}
for half in ['head','foot']:
 head=half=='head';z1,z2=(2,16) if head else (0,14);zz=0 if head else 14
 els=[box([x,0,zz],[x+2,4,zz+2],'wood') for x in [0,14]]+[box([x,3,z1],[x+2,5,z2],'wood') for x in [0,14]]+[box([0,4,zz],[16,10 if head else 8,zz+2],'wood')]
 mattress=box([2.02,5.02,2.02 if head else 0],[13.98,8.8,16 if head else 13.98],'blanket_edge');mattress['faces']['up']={'texture':'#blanket','uv':[0,0 if head else 8,16,8 if head else 16]};els.append(mattress)
 if head:els.append(box([3,8.82,3],[13,9.8,7],'pillow'))
 else:els.append({'from':[6,5,16.02],'to':[10,7,16.04],'faces':{'south':{'uv':[0,0,16,16],'texture':'#mana'}}})
 j={'textures':{'particle':'kelmagic:block/dream_frame','wood':'kelmagic:block/dream_frame','blanket':'kelmagic:block/dream_blanket','blanket_edge':'kelmagic:block/dream_blanket_edge','pillow':'kelmagic:block/dream_pillow','mana':'kelmagic:block/dream_mana'},'elements':els};write(a/f'models/block/otherworld_dream_bed_{half}.json',j)
im=Image.new('RGBA',(32,64));draw=ImageDraw.Draw(im)
for y in range(64):
 for x in range(32):
  grain=((x+y)%2)*2;im.putpixel((x,y),(174+grain,139+grain,63+grain,255))
for x in [1,4,27,30]:draw.line((x,0,x,63),fill=(225,196,113,255),width=1)
# A single uninterrupted central motif, not one cropped full design per mattress half.
for y in [16,48]:draw.polygon([(16,y-5),(21,y),(16,y+5),(11,y)],outline=(240,218,145,255));draw.point((16,y),fill=(249,230,169,255))
im.save(a/'textures/block/dream_blanket.png');Image.new('RGBA',(16,16),(167,131,57,255)).save(a/'textures/block/dream_blanket_edge.png')
head=json.loads((a/'models/block/otherworld_dream_bed_head.json').read_text());foot=json.loads((a/'models/block/otherworld_dream_bed_foot.json').read_text());whole=copy.deepcopy(head)
for el in foot['elements']:
 el=copy.deepcopy(el)
 for k in ['from','to']:el[k][2]+=16
 whole['elements'].append(el)
for el in whole['elements']:
 for k in ['from','to']:el[k][2]-=8
whole['display']=json.loads((a/'models/item/otherworld_dream_bed.json').read_text())['display'];write(a/'models/item/otherworld_dream_bed.json',whole)
# Book entries are discoverable records, not survival research unlocks.
p=d/'research/catalog.json';j=json.loads(p.read_text());old=[n for n in j['nodes'] if n['tab']=='great' and n['id'] not in ['rite:threshold_link','rite:grove_awakening']];print('great positions',[(n['id'],n['x'],n['y']) for n in old])
for n in j['nodes']:
 if n['id']=='rite:threshold_link':n.update(x=45,y=55,icon='manuscript_dor',lore='research.kelmagic.threshold_link_lore',thought='Порог слышит не чужой зов, а продолжение моего собственного слова.',example='Первый порог ждёт второго. Следующие два образуют другую пару. Путь простирается и между мирами; любой удар разрывает связь. Порог появляется над поверхностью, на которую я смотрю.')
 if n['id']=='rite:grove_awakening':n.update(x=130,y=55,icon='magic_sapling',lore='research.kelmagic.grove_awakening_lore',thought='Место помнит лес не во мне, а в живых образцах былых лет.',example='Нужно хотя бы десять магических саженцев рядом. Роща пробуждается вокруг найденных саженцев и разрастается по равнинам и лесам. Её листва снова даёт потомство.')
write(p,j)
for lang in ['ru_ru','en_us']:
 p=a/f'lang/{lang}.json';j=json.loads(p.read_text());ru=lang=='ru_ru'
 for name in removed:
  for part in ['title','description']:j.pop(f'advancements.kelmagic.{name}.{part}',None)
 for name in ['threshold_pair','rite_grove_awakening']:j[f'advancements.kelmagic.{name}.description']=j['advancements.kelmagic.rite_wings.description']
 j.update({'advancements.kelmagic.word_born_power.title':'Сила, рождённая словом' if ru else 'Power Born of a Word','advancements.kelmagic.word_born_power.description':'Преобразите светильник стола архивариуса словом nov: не запас маны, а чистая магия слова.' if ru else 'Transform the archivist table lamp with nov: not stored mana, but the pure magic of a word.',
 'research.kelmagic.threshold_link_lore':'Связь двух порогов соединяет две точки, отмеченные одним голосом. Цвет принадлежит паре; каждый новый незавершённый порог ждёт только моего продолжения. Это ещё закрытое знание: запись не открывает обряд в выживании.' if ru else 'Two thresholds answer one voice. Each pair shares a colour and spans worlds. This record does not unlock the rite in survival.',
 'research.kelmagic.grove_awakening_lore':'Десять живых саженцев возвращают память земли. Роща начинается у них и охватывает подходящие равнины и леса. Старые кроны уступают сумеречным, чья листва вновь плодоносна. Эта запись пока не открывает обряд в выживании.' if ru else 'Ten living saplings return the memory of the soil. The grove begins at their roots and awakens suitable plains and forests. This record does not unlock the rite in survival.'})
 for sound,desc in [('threshold_open','Порог раскрывается' if ru else 'Threshold unfolds'),('threshold_close','Связь порогов обрывается' if ru else 'Threshold bond breaks'),('threshold_travel','Шаг между порогами' if ru else 'Step between thresholds'),('threshold_hum','Порог отзывается' if ru else 'Threshold resonates')]:j['subtitles.kelmagic.'+sound]=desc
 write(p,j)
