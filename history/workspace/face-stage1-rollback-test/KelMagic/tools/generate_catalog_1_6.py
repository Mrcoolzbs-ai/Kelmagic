from pathlib import Path
import json,re,hashlib
from PIL import Image,ImageOps,ImageDraw
r=Path(__file__).resolve().parents[1];d=r/'src/main/resources/data/kelmagic/magic';old=json.loads((d/'words.json').read_text())['words'][:6];rows=[]
def group(source,category,profile,txt):
 for entry in txt.split(';'):
  if not entry.strip():continue
  w,t=entry.strip().split('=',1);p=profile
  if '@' in t:t,p=t.rsplit('@',1)
  rows.append(dict(word=w,root=w,translation=t,category=category,source=source,profile=p))
s='ez_word1.png'
group(s,'people','GLOW','per=человек;mil=друг@HEAL;nak=враг@WEAKNESS;mam=мать@REGENERATION;pap=отец@RESISTANCE;kin=ребёнок@JUMP;gav=правительство, власть@STRENGTH')
group(s,'body','RESISTANCE','ged=голова@NIGHT_VISION;gok=глаз@NIGHT_VISION;nos=нос@SCAN;mur=рот@HUNGER;ped=нога@SPEED;kor=сердце@HEAL')
group(s,'nature','RESISTANCE','sol=солнце@NIGHT_VISION;lun=луна@INVISIBILITY;ter=земля;vak=вода@WATER_BREATHING;tur=дерево@REGENERATION;gor=гора@JUMP;mar=море@DOLPHINS_GRACE;riv=река@DOLPHINS_GRACE;neb=небо@SLOW_FALLING;dum=дым@BLINDNESS;vit=ветер@PUSH')
group(s,'animals','SCAN','zun=животное;kuk=кошка@NIGHT_VISION;wuk=собака@STRENGTH;pis=птица@SLOW_FALLING;sal=рыба@WATER_BREATHING')
group(s,'verbs','GLOW','vid=видеть@NIGHT_VISION;tik=слышать@SCAN;kel=говорить;man=есть@FEED;pin=пить@EXTINGUISH;pak=идти@SPEED;dak=давать@HEAL;tak=брать@PULL;sit=находиться@RESISTANCE;liv=жить@HEAL;mor=умирать@DAMAGE;sav=знать@SCAN;pen=думать@NIGHT_VISION;vol=хотеть@LUCK;kan=мочь@STRENGTH;lab=работать@HASTE;ler=учить@SCAN;lud=играть@JUMP;lov=любить@REGENERATION;pul=крутить@SPIN;rez=резать@DAMAGE;lom=ломать@HASTE;zek=жечь@VANILLA_FIRE;set=стирать@CLEANSE;pad=падать@SLOW_FALLING;ved=вести@PULL')
group(s,'objects','RESISTANCE','tom=дом;puk=книга@SCAN;log=слово@GLOW;tem=время@SLOWNESS;lok=место@GLOW;rak=путь@SPEED;mon=деньги@LUCK;tab=стол@SATURATION;kul=стул@SLOWNESS;bed=кровать@REGENERATION;dor=дверь@OPEN;sen=окно@NIGHT_VISION;val=стена;rum=комната@SCAN;kas=город@SCAN;vil=деревня@HERO;lum=улица@SPEED;pal=хлеб@FEED;sin=выход@SPEED;tul=вентиль@CLOSE;kur=круг@SPIN')
group(s,'qualities','RESISTANCE','var=тёплый@EXTINGUISH;kol=холодный@SLOWNESS;nov=новый@REGENERATION;vel=старый@WEAKNESS;bon=хороший@HEAL;mal=плохой@POISON;muk=большой, много@SCALE_UP;nis=маленький@SCALE_DOWN;rap=быстрый@SPEED;len=медленный@SLOWNESS;por=сильный@STRENGTH;nem=слабый@WEAKNESS;sek=сухой@EXTINGUISH')
group(s,'abstract','RESISTANCE','kap=счастье@LUCK;gus=печаль@SLOWNESS;pob=страх@BLINDNESS;rag=гнев@STRENGTH;kal=покой;tuk=истина@GLOW;bal=ложь@INVISIBILITY')
s='ez_word2.png'
# All below are manually read CVC roots; ambiguous pol/dul and vowel-initial exceptions excluded.
group(s,'actions','SPEED','rin=открывать@OPEN;tin=закрывать@CLOSE;vin=толкать@PUSH;jin=тянуть@PULL;don=бросать@PUSH;gon=ловить@SLOWNESS;kon=стоять@RESISTANCE;lon=лежать@SLOWNESS;non=прыгать@JUMP;pon=бежать;ron=плыть@DOLPHINS_GRACE;son=летать@SLOW_FALLING;ton=ползти@SLOWNESS;din=спать@REGENERATION;gin=просыпаться@NIGHT_VISION;lin=мыть@CLEANSE;nin=готовить@SATURATION')
group(s,'nature','RESISTANCE','nok=камень;pok=песок@BLINDNESS;rok=почва@REGENERATION;sok=грязь@SLOWNESS;tok=стекло@INVISIBILITY;vok=пар@INVISIBILITY;buk=туман@BLINDNESS;duk=роса@EXTINGUISH;guk=иней@SLOWNESS;luk=волна@PUSH;nuk=остров@WATER_BREATHING;suk=поле@SPEED;dun=молния@DAMAGE;gun=радуга@GLOW;lup=лужа@EXTINGUISH;nup=снежинка@SLOWNESS;bak=пещера@NIGHT_VISION;gak=долина@SPEED;lak=обрыв@SLOW_FALLING;mak=пляж@DOLPHINS_GRACE;sak=болото@POISON;sap=звезда@GLOW;tup=гром@PUSH')
group(s,'plants','REGENERATION','del=корень;gel=стебель;lel=ветвь@JUMP;mel=цветок@LUCK;tel=трава@SPEED;bel=ягода@FEED;nel=плод@FEED;pel=семя@REGENERATION;rel=кора@RESISTANCE;sel=мох@SLOWNESS;jel=гриб@POISON')
group(s,'tools','STRENGTH','bin=копьё@DAMAGE;den=крепость@RESISTANCE;gen=колесо@SPIN;men=лодка@DOLPHINS_GRACE;nen=телега@SPEED;ren=меч@DAMAGE;ten=щит@ABSORPTION;ven=лук@STRENGTH;jen=стрела@PUSH;bun=верёвка@PULL;kut=мешок@SATURATION;mut=молот@STRENGTH;nut=топор@HASTE;but=игла@DAMAGE;gut=нить@PULL;lut=ткань@RESISTANCE;rut=одежда@RESISTANCE;sut=обувь@SPEED;tut=шляпа@NIGHT_VISION;vut=кольцо@LUCK;bup=барабан@PUSH;kup=купол@ABSORPTION')
group(s,'body','RESISTANCE','bek=кость;dek=кожа;gek=мышца@STRENGTH;lek=мозг@NIGHT_VISION;mek=живот@FEED;nek=спина;pek=плечо@STRENGTH;rek=колено@JUMP;tek=локоть@HASTE;vek=грудь@ABSORPTION;jek=горло@WATER_BREATHING;bik=шея@RESISTANCE;dik=бедро@SPEED;gik=пятка@JUMP;lik=ладонь@HASTE;mik=лоб@NIGHT_VISION;nik=щека@REGENERATION;pik=подбородок@RESISTANCE;rik=бровь@NIGHT_VISION;sik=ресница@NIGHT_VISION;vik=борода@LUCK;bok=губа@SATURATION;gul=ноготь@DAMAGE;jup=кулак@STRENGTH')
group(s,'animals','SCAN','ban=олень;gan=медведь@STRENGTH;lan=лиса@INVISIBILITY;nan=заяц@JUMP;pim=мышь@SPEED;ran=крыса@POISON;san=белка@JUMP;tan=бобр@HASTE;van=выдра@DOLPHINS_GRACE;pun=змея@POISON;run=ящерица@RESISTANCE;sun=черепаха@WATER_BREATHING;vun=орёл@SLOW_FALLING;jap=сова@NIGHT_VISION;kun=крот@HASTE;mun=летучая мышь@NIGHT_VISION;nun=лягушка@JUMP')
# Explicit scope of 180 words; every chosen word is a transcribed source entry, not a generated root.
seen=set(w['word'] for w in old);chosen=[]
for row in rows:
 if row['word'] not in seen and re.fullmatch('[ptkbdgmnslvrjaeiou]+',row['word']):
  chosen.append(row);seen.add(row['word'])
chosen=chosen[:180-len(old)]
assert len(chosen)+len(old)==180
profiles={
 'GLOW':('BUFF','glowing','Подсвечивает цель.',False), 'HEAL':('HEAL','','Восстанавливает здоровье.',False),
 'REGENERATION':('BUFF','regeneration','Постепенно лечит цель.',False), 'RESISTANCE':('BUFF','resistance','Укрепляет защиту цели.',False),
 'STRENGTH':('BUFF','strength','Усиливает ближний бой.',False),'WEAKNESS':('BUFF','weakness','Ослабляет ближний бой.',True),
 'JUMP':('BUFF','jump_boost','Увеличивает высоту прыжка.',False),'SPEED':('BUFF','speed','Ускоряет движение.',False),
 'SLOWNESS':('BUFF','slowness','Замедляет движение.',True),'NIGHT_VISION':('BUFF','night_vision','Позволяет видеть в темноте.',False),
 'INVISIBILITY':('BUFF','invisibility','Скрывает цель от глаз.',False),'BLINDNESS':('BUFF','blindness','Затуманивает зрение.',True),
 'WATER_BREATHING':('BUFF','water_breathing','Позволяет дышать под водой.',False),'DOLPHINS_GRACE':('BUFF','dolphins_grace','Ускоряет плавание.',False),
 'SLOW_FALLING':('BUFF','slow_falling','Смягчает падение.',False),'HUNGER':('BUFF','hunger','Усиливает голод.',True),
 'HASTE':('BUFF','haste','Ускоряет добычу блоков.',False),'LUCK':('BUFF','luck','Повышает удачу.',False),
 'HERO':('BUFF','hero_of_the_village','Даёт скидку у жителей.',False),'ABSORPTION':('BUFF','absorption','Даёт временные сердца.',False),
 'POISON':('BUFF','poison','Отравляет цель, не добивая её.',True),
 'DAMAGE':('DAMAGE','','Наносит магический удар.',True),'FEED':('FEED','','Восстанавливает сытость игрока.',False),
 'SATURATION':('FEED','','Восстанавливает сытость игрока.',False),'EXTINGUISH':('EXTINGUISH','','Гасит только обычное горение.',False),
 'CLEANSE':('CLEANSE','','Снимает яд, голод и слабость.',False),'PUSH':('PUSH','','Отталкивает цель от заклинателя.',True),
 'PULL':('PULL','','Притягивает цель к заклинателю.',True),'SPIN':('SPIN','','Закручивает цель вокруг заклинателя.',True),
 'SCAN':('SCAN','','Подсвечивает ближайших существ.',False),'OPEN':('OPEN','','Открывает дверь или люк в прицеле.',False),
 'CLOSE':('CLOSE','','Закрывает дверь или люк в прицеле.',False),'VANILLA_FIRE':('VANILLA_FIRE','','Поджигает обычным огнём.',True)}
effects={}
for row in chosen:
 profile=row.pop('profile')
 if row['word']=='nis':row['source']='User-confirmed replacement for zin (2026-10-10)'
 if profile.startswith('SCALE'):
  row['category']='modifier';row['description']='Масштаб ×1,5 за повтор; nis обращает muk.' if profile=='SCALE_UP' else 'Масштаб ÷1,5 за повтор; muk обращает nis.'
 else:
  action,status,desc,harm=profiles[profile];row['description']=desc
  effects[row['word']]=dict(action=action,status=status,description=desc,harmful=harm,baseDuration=240,basePower=4 if action in ['HEAL','FEED'] else 2 if action=='DAMAGE' else 1)
 # Source meaning and assigned gameplay interpretation are distinct.
for row in old:
 row['description']={'mina':'Цель: заклинатель.','tuna':'Цель: другой игрок.','sila':'Цель: моб.','pir':'Магический огонь.','no':'Обращение эффекта.','lu':'Несколько целей.'}[row['word']]
(d/'words.json').write_text(json.dumps({'version':2,'words':old+chosen},ensure_ascii=False,indent=2)+'\n')
(d/'effects.json').write_text(json.dumps({'version':1,'effects':effects},ensure_ascii=False,indent=2)+'\n')
# Individual manuscript icons reuse the exact current paper and composed canonical atlas, not decorative letter images.
a=r/'src/main/resources/assets/kelmagic';font=Image.open(a/'textures/font/kel.png').convert('RGBA');glyphmap=json.loads((a/'glyph-map.json').read_text());cw,ch=glyphmap['units']['cell'];cons='ptkbdgmnslvrj';vowels='aeiou'
def tiles(word):
 i=0;result=[]
 while i<len(word):
  c=word[i];assert c in cons;idx=cons.index(c)*6;i+=1
  if i<len(word) and word[i] in vowels:idx+=vowels.index(word[i])+1;i+=1
  box=((idx%8)*cw,(idx//8)*ch,(idx%8+1)*cw,(idx//8+1)*ch);g=font.crop(box);bb=g.getbbox();assert bb;g=g.crop(bb);g.thumbnail((6,10),Image.Resampling.LANCZOS);result.append(g)
 return result
base=Image.open(a/'textures/item/manuscript_mina.png').convert('RGBA')
# Use source paper with a clean interior reconstructed from the unlettered ancient manuscript interior.
paper=base.copy();ImageDraw.Draw(paper).rectangle((6,10,24,22),fill='#e5cda0')
for row in old+chosen:
 word=row['word'];im=paper.copy();gs=tiles(word);x=(32-sum(g.width+1 for g in gs)+1)//2
 for g in gs:
  ink=Image.new('RGBA',g.size,'#555a60');ink.putalpha(g.getchannel('A'));im.alpha_composite(ink,(x,11));x+=g.width+1
 im.save(a/f'textures/item/manuscript_{word}.png')
 (a/f'models/item/manuscript_{word}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':f'kelmagic:item/manuscript_{word}'}},indent=2)+'\n')
(a/'models/item/creative_archivists_codex.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'kelmagic:item/archivists_codex'}},indent=2)+'\n')
for lang in ['ru_ru','en_us']:
 p=a/f'lang/{lang}.json';o=json.loads(p.read_text())
 for row in chosen:o['item.kelmagic.manuscript_'+row['word']]=('Рукопись: ' if lang=='ru_ru' else 'Manuscript: ')+row['word']
 o['item.kelmagic.creative_archivists_codex']='Творческий архивариусный кодекс' if lang=='ru_ru' else 'Creative Archivist’s Codex'
 o['effect.kelmagic.flame_life']='Жизнь в пламени' if lang=='ru_ru' else 'Life in Flame'
 o['kelmagic.codex_copy']='Кодекс + книга + перо + чернила: копия знаний и заметок; оригинал сохраняется.' if lang=='ru_ru' else 'Codex + book + feather + ink: copy knowledge and notes; keep the original.'
 o['kelmagic.page_hint']='Соедините с кодексом на верстаке. Повторное слово не расходуется.' if lang=='ru_ru' else 'Combine with a codex in crafting. Duplicate words are not consumed.'
 p.write_text(json.dumps(o,ensure_ascii=False,indent=2)+'\n')
# Status icon: preserve the existing gold-rimmed effect badge, add a violet flame/heart.
effect=Image.open(a/'textures/mob_effect/mana_restore.png').convert('RGBA')
for y in range(effect.height):
 for x in range(effect.width):
  rr,gg,bb,alpha=effect.getpixel((x,y))
  if alpha and (bb>rr or gg>rr*1.2):effect.putpixel((x,y),(39,27,56,alpha))
ed=ImageDraw.Draw(effect)
ed.polygon([(4,13),(4,9),(6,7),(7,3),(10,7),(12,5),(14,10),(13,13),(11,15),(7,15)],fill='#a45ed2')
ed.polygon([(6,8),(7,7),(8,7),(9,8),(10,7),(11,7),(12,8),(12,10),(9,13),(6,10)],fill='#ee9bbc')
ed.point((7,8),fill='#ffe2dd');effect.save(a/'textures/mob_effect/flame_life.png')
print('WORDS',len(old+chosen),'PROFILES',len(effects),'last',chosen[-1]['word'],'excluded: pol, dul, mus, akt, est, ust, arm, wuk, zek (w/z absent from alphabet); only nis is user-authorized replacement.')
(r/'docs/kel-language/CATALOG-1.6.0.json').write_text(json.dumps({'words':old+chosen,'effects':effects},ensure_ascii=False,indent=2)+'\n')
