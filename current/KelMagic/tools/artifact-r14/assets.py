"""Original KelMagic R14 pixel-art, block meshes, UI glyphs and synthesized audio.
Reproducible; writes only new R14 resources. No recipes and no third-party images/audio.
"""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json,math,random,wave,struct,subprocess
R=Path(__file__).resolve().parents[2]; A=R/'src/main/resources/assets/kelmagic'; D=R/'src/main/resources/data'
for p in ['textures/item','textures/block','textures/slot','textures/models/armor','models/item','models/block','blockstates','sounds/artifact']: (A/p).mkdir(parents=True,exist_ok=True)
O='#0c1023';N='#1c1a3e';P='#493369';V='#8855bc';L='#c29be8';TD='#184956';T='#2b8b97';C='#4dd3cf';H='#acf6e0';GD='#68421b';G='#c3882f';GH='#f6cf69';S='#9193b5';SH='#e0dbef'
CAT=[
(1,'cautious_word_ring','Перстень осторожного слова','Cautious Word Ring','Curios: запрещает слово, если маны не хватает и понадобится здоровье.','ring'),
(4,'far_address_pendant','Подвеска дальнего обращения','Far Address Pendant','Curios: дальность направленной магии ×1,5; масштаб заклинаний по области ×0,8.','necklace'),
(11,'deep_well_ring','Кольцо глубокого колодца','Deep Well Ring','Curios: +10 маны; естественное восстановление ×0,65.','ring'),
(12,'spare_heart_medallion','Медальон запасного сердца','Spare Heart Medallion','Curios: G — забрать 1 ману; Shift+G — вложить 1. Вместимость 10 маны.','necklace'),
(13,'calm_pulse_bracelet','Браслет спокойного пульса','Calm Pulse Bracelet','Curios: восстановление маны ×1,6 при неподвижности и отсутствии боя 8 секунд.','bracelet'),
(15,'even_flow_amulet','Амулет ровного течения','Even Flow Amulet','Curios: восстанавливает ману малыми порциями каждый тик, не увеличивая общий объём.','necklace'),
(21,'witness_earring','Серьга свидетеля','Witness Earring','Curios: хранит последнюю увиденную чужую формулу; не открывает слова и исследования.','kelmagic_earring'),
(23,'obedient_sample_gloves','Перчатки послушного образца','Obedient Sample Gloves','Curios: Y — поднять предмет перед собой или отпустить. Дальность 5 блоков.','hands'),
(26,'reverse_echo_bracelet','Браслет обратного эха','Reverse Echo Bracelet','ПКМ: направление и расстояние до недавней магической фразы. Перезарядка 10 секунд.','tool'),
(28,'silent_step_clasp','Застёжка немого шага','Silent Step Clasp','Curios: заглушает звуки и вибрации владельца; запрещает сообщения в чат и магическую речь.','charm'),
(31,'root_hearing_brooch','Брошь корневого слуха','Root Hearing Brooch','Curios: чувствует движение существ на связанном участке магической травы.','charm'),
(32,'unbroken_forest_belt','Пояс ненарушенного леса','Unbroken Forest Belt','Curios: обычная магия не разрушает растительность; великие обряды не ограничиваются.','belt'),
(33,'dusk_dew_ring','Перстень сумеречной росы','Dusk Dew Ring','Curios: ночью заряжается в роще. H — снять все вредные эффекты, магический огонь и опутывание.','ring'),
(39,'unnoticed_arrival_cloak','Плащ незамеченного прибытия','Unnoticed Arrival Cloak','Curios: после перехода через порог мобы не выбирают владельца целью 5 секунд; атака снимает покров.','back'),
(46,'mana_cup','Малая чаша маны','Mana Cup','Хранит 32 маны; потери при внесении 5%. ПКМ — вложить до 5; Shift+ПКМ — забрать до 5.','block'),
(46,'mana_bowl','Чаша маны','Mana Bowl','Хранит 128 маны; потери при внесении 5%. ПКМ — вложить до 5; Shift+ПКМ — забрать до 5.','block'),
(46,'mana_basin','Глубокая чаша маны','Mana Basin','Хранит 512 маны; потери при внесении 5%. ПКМ — вложить до 5; Shift+ПКМ — забрать до 5.','block'),
(49,'admission_obelisk','Обелиск допуска','Admission Obelisk','Ограничивает доступ к магическим устройствам в радиусе 8. Именная бирка с именем игрока меняет разрешение.','block'),
(50,'forbidden_threshold_base','Основание запретного шага','Forbidden Threshold Base','Установите вместо опоры под любым порогом: блокирует переходы в обоих направлениях пары.','block'),
(52,'delayed_sound_crystal','Кристалл задержанного слова','Delayed Word Crystal','Поставьте на чашу, сверху — молоточек. ПКМ кристаллом: запись 1–3 слов на 20 секунд.','block'),
(52,'crystal_striker','Молоточек кристалла','Crystal Striker','ПКМ или фронт редстоун-сигнала: исполнить формулу кристалла, взяв ману из чаши. Интервал 1 секунда.','block'),
(56,'night_shadow_catcher','Ловец ночных теней','Night Shadow Catcher','Отгоняет фантомов от спящих в радиусе 12. Запас 64 маны; пополнение как у чаши.','block'),
(57,'grove_mana_bowl','Корневая чаша','Grove Mana Bowl','На магической траве добавляет 0,1 маны в секунду игрокам в радиусе 5. Несколько чаш не складываются.','block'),
(60,'mute_sky_obelisk','Обелиск немого неба','Mute Sky Obelisk','Подавляет магию в радиусе 12 для всех. Расход 1 маны в секунду; запас 128.','block'),
(62,'precise_name_rapier','Рапира точного имени','Precise Name Rapier','Попадание отмечает цель для наведения следующей формулы и усиливает её на 15%.','weapon'),
(63,'mute_verdict_hammer','Молот немого приговора','Mute Verdict Hammer','Удерживайте ПКМ 1 секунду: заряд за 2 маны. Следующий удар подавляет магию цели на 2 секунды.','weapon'),
(64,'short_threshold_spear','Копьё короткого порога','Short Threshold Spear','ПКМ: выпад до 7,5 блоков за 1 ману, только при прямой видимости.','weapon'),
(66,'living_fiber_whip','Кнут живого волокна','Living Fiber Whip','Удар связывает цель живой нитью на 3 секунды; магический иммунитет препятствует опутыванию.','weapon'),
(69,'alien_phrase_shield','Щит чужой фразы','Alien Phrase Shield','Поднимите за 0,25 секунды до попадания магического снаряда: поглощение и возврат половины его цены. Перезарядка 5 секунд.','weapon'),
(71,'unspoken_dagger','Кинжал невысказанного','Unspoken Dagger','ПКМ: превратить уже оплаченную незавершённую обычную формулу в заряд следующего удара.','weapon'),
(72,'heavy_thought_mace','Палица тяжёлой мысли','Heavy Thought Mace','Попадание замедляет цель и подавляет её прыжки на 3 секунды.','weapon'),
(75,'shared_oath_sword','Меч общей клятвы','Shared Oath Sword','ПКМ по участнику своей команды: принять 30% его урона в пределах 12 блоков, пока меч в руке.','weapon'),
(76,'reflected_path_fan','Веер отражённого пути','Reflected Path Fan','ПКМ: отклоняет видимые снаряды перед собой в выбранную сторону за 1 ману.','weapon'),
(79,'still_volley_orb','Сфера недвижного залпа','Still Volley Orb','Удерживайте ПКМ: захват до 4 чужих снарядов; после отпускания или 3 секунд они обезвреживаются.','weapon'),
(84,'wayback_chalk','Мел обратной дороги','Wayback Chalk','ПКМ по блоку — личная путевая метка. Держите мел для просмотра. Shift+ПКМ в воздух — очистить.','tool'),
(88,'stopped_season_secateurs','Секатор остановленного сезона','Stopped Season Secateurs','ПКМ по растению: остановить или возобновить рост. Метка сохраняется с миром.','tool'),
(89,'living_water_can','Лейка живой влаги','Living Water Can','ПКМ: воздействие на рост растения за 1 ману; задержка 2 секунды.','tool'),
(95,'quiet_flame_bottle','Бутыль тихого пламени','Quiet Flame Bottle','ПКМ по магическому огню — забрать один очаг; ПКМ по опоре — выпустить.','tool'),
(100,'hanging_path_staff','Посох висячей тропы','Hanging Path Staff','ПКМ по опоре: временный мост до 8 блоков за 0,5 маны на блок. Исчезает через 10 секунд.','tool')]
(R/'tools/artifact-r14/catalog.json').write_text(json.dumps(CAT,ensure_ascii=False,indent=2))
def write(p,j):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(j,ensure_ascii=False,indent=2)+'\n')
def canvas(n=32):
 im=Image.new('RGBA',(n,n));return im,ImageDraw.Draw(im)
def poly(d,pts,c,w=2):d.polygon(pts,fill=c,outline=O,width=w)
def gem(d,x,y,r=4,c=C):
 poly(d,[(x,y-r),(x+r,y),(x,y+r),(x-r,y)],c,1);d.line([(x-r+1,y),(x,y-r+1),(x,y)],fill=H,width=1)
def handle(d,a=(7,28),b=(23,5),c=TD):
 d.line([a,b],fill=O,width=5);d.line([a,b],fill=c,width=3)
 for k in [.2,.45,.7]:
  x=round(a[0]+(b[0]-a[0])*k);y=round(a[1]+(b[1]-a[1])*k);d.line([(x-1,y),(x+1,y+1)],fill=G,width=1)
def ring(d,variant):
 d.ellipse((5,8,27,29),fill=O);d.ellipse((7,10,25,27),fill=G);d.ellipse((11,14,21,24),fill=O);d.ellipse((12,15,20,23),fill=(0,0,0,0));d.arc((7,10,25,27),185,290,fill=GH,width=2)
 if variant=='deep_well_ring':
  poly(d,[(10,4),(22,4),(25,9),(22,15),(10,15),(7,9)],TD);d.ellipse((11,6,21,13),fill=O,outline=C);d.point((16,8),fill=V)
 elif variant=='dusk_dew_ring':
  poly(d,[(16,1),(22,8),(22,12),(17,16),(11,13),(10,9)],T);d.line([(15,6),(13,10),(15,12)],fill=H,width=2)
 else:
  poly(d,[(11,3),(21,3),(25,7),(23,13),(16,16),(9,13),(7,7)],P);gem(d,16,8,4);d.line([(12,9),(20,9)],fill=GH,width=1)
def pendant(d,id):
 d.line([(5,3),(9,9),(16,12),(23,9),(27,3)],fill=O,width=4);d.line([(5,3),(9,9),(16,12),(23,9),(27,3)],fill=G,width=2)
 if id=='far_address_pendant':poly(d,[(16,8),(23,17),(17,29),(10,22),(8,17)],P);gem(d,16,18,5);d.line([(16,22),(16,26)],fill=GH,width=1)
 elif id=='spare_heart_medallion':
  poly(d,[(9,11),(16,13),(23,11),(26,17),(23,25),(16,30),(9,25),(6,17)],P);poly(d,[(16,25),(10,19),(11,15),(15,15),(16,17),(18,15),(22,16),(22,20)],C,1);d.line([(12,17),(14,17)],fill=H,width=1)
 else:
  poly(d,[(10,11),(22,11),(24,26),(16,30),(8,26)],TD);d.line([(11,16),(15,16),(15,20),(20,20),(20,24),(12,24)],fill=C,width=2);d.point((20,16),fill=GH)
def icon(id):
 im,d=canvas()
 if id in ['cautious_word_ring','deep_well_ring','dusk_dew_ring']:ring(d,id)
 elif id in ['far_address_pendant','spare_heart_medallion','even_flow_amulet']:pendant(d,id)
 elif id=='calm_pulse_bracelet':
  d.ellipse((3,7,29,26),fill=O);d.ellipse((6,10,26,23),fill=G);d.ellipse((9,12,23,20),fill=(0,0,0,0));poly(d,[(9,5),(23,5),(26,10),(23,15),(9,15),(6,10)],P);d.line([(9,10),(12,10),(14,7),(17,13),(20,9),(23,9)],fill=C,width=2)
 elif id=='witness_earring':
  d.arc((9,1,23,15),160,410,fill=O,width=5);d.arc((9,1,23,15),160,410,fill=GH,width=2);d.line([(17,12),(17,17)],fill=G,width=3);poly(d,[(17,15),(25,22),(17,30),(9,22)],P);d.ellipse((11,19,23,25),fill=TD,outline=G);gem(d,17,22,3)
 elif id=='obedient_sample_gloves':
  for x in [3,18]:poly(d,[(x+1,15),(x+5,10),(x+10,12),(x+11,23),(x+8,28),(x+3,27),(x,22)],P);d.line([(x+2,23),(x+9,23)],fill=GH,width=2)
  gem(d,16,6,4);d.arc((9,5,23,15),10,170,fill=T,width=1);d.point((7,8),fill=H)
 elif id=='reverse_echo_bracelet':
  d.ellipse((5,7,27,28),fill=O);d.ellipse((8,10,24,25),fill=P);d.ellipse((12,13,20,21),fill=(0,0,0,0));d.arc((3,2,28,22),220,330,fill=GH,width=2);d.arc((8,6,25,18),225,335,fill=C,width=2);gem(d,9,9,4)
 elif id=='silent_step_clasp':
  poly(d,[(9,5),(23,5),(27,13),(24,24),(16,29),(8,24),(5,13)],P);d.arc((10,10,22,23),180,360,fill=GH,width=2);d.line([(10,16),(10,21),(22,21),(22,16)],fill=G,width=2);d.line([(7,26),(25,7)],fill=O,width=5);d.line([(8,25),(24,8)],fill=C,width=2)
 elif id=='root_hearing_brooch':
  poly(d,[(16,2),(25,7),(28,17),(21,28),(10,28),(4,18),(7,8)],TD);d.line([(16,7),(16,21),(12,26)],fill=GH,width=2);d.line([(16,17),(9,12),(8,8)],fill=C,width=2);d.line([(16,13),(23,9)],fill=C,width=2);d.line([(16,21),(23,24)],fill=G,width=2);gem(d,16,8,3)
 elif id=='unbroken_forest_belt':
  poly(d,[(2,9),(9,7),(25,8),(30,12),(28,23),(7,25),(2,21)],P);d.line([(5,11),(27,13)],fill=G,width=2);d.line([(6,22),(27,20)],fill=GH,width=1);poly(d,[(12,10),(21,10),(24,15),(21,23),(12,23),(9,16)],GD);gem(d,17,16,4);d.line([(17,20),(14,23)],fill=C,width=1)
 elif id=='unnoticed_arrival_cloak':
  poly(d,[(11,3),(21,3),(27,10),(25,25),(20,23),(16,30),(12,25),(5,27),(7,11)],P);poly(d,[(12,6),(20,6),(23,11),(21,21),(18,20),(16,25),(12,20),(9,23),(10,12)],V,1);d.line([(10,9),(16,12),(22,9)],fill=GH,width=2);gem(d,16,12,2);d.point((27,27),fill=C);d.point((3,23),fill=L)
 elif id in ['precise_name_rapier','short_threshold_spear','unspoken_dagger','shared_oath_sword','hanging_path_staff']:
  handle(d)
  if id=='precise_name_rapier':poly(d,[(12,22),(26,2),(28,2),(16,24)],SH,1);d.arc((8,19,20,30),10,270,fill=G,width=2);gem(d,14,23,2)
  elif id=='short_threshold_spear':poly(d,[(18,11),(20,4),(29,1),(27,10),(22,13)],T);d.line([(22,9),(27,3)],fill=H,width=2);d.arc((15,1,30,16),120,230,fill=L,width=1)
  elif id=='unspoken_dagger':poly(d,[(13,22),(18,9),(26,5),(25,15),(18,24)],S);d.line([(19,18),(23,9)],fill=C,width=2);d.line([(10,21),(19,26)],fill=GH,width=2)
  elif id=='shared_oath_sword':poly(d,[(12,22),(22,4),(29,1),(28,9),(17,25)],S);d.line([(17,21),(26,6)],fill=GH,width=1);d.line([(10,21),(20,27)],fill=G,width=3);gem(d,15,22,2);gem(d,11,28,2,L)
  else:
   poly(d,[(16,8),(17,3),(25,1),(29,6),(25,13),(19,14)],P);gem(d,23,7,4);d.line([(2,20),(7,20),(7,23)],fill=C,width=2);d.line([(3,15),(9,15),(9,18)],fill=GH,width=2)
 elif id in ['mute_verdict_hammer','heavy_thought_mace']:
  handle(d,(8,28),(21,9))
  if id=='mute_verdict_hammer':poly(d,[(9,5),(14,1),(29,9),(30,15),(25,19),(10,11)],S);poly(d,[(13,5),(16,4),(26,10),(26,14),(23,15),(13,9)],P,1);d.line([(16,6),(24,11)],fill=C,width=2)
  else:poly(d,[(15,3),(24,1),(30,9),(26,18),(17,20),(10,12)],S);poly(d,[(18,5),(24,4),(27,10),(24,15),(18,16),(14,11)],P,1);gem(d,21,10,3)
 elif id=='living_fiber_whip':
  handle(d,(4,28),(11,21));d.line([(10,22),(7,16),(10,9),(18,5),(25,8),(27,14),(22,18),(17,15),(19,11)],fill=O,width=5);d.line([(10,22),(7,16),(10,9),(18,5),(25,8),(27,14),(22,18),(17,15),(19,11)],fill=T,width=3);d.line([(10,10),(17,7),(24,9)],fill=H,width=1)
 elif id=='alien_phrase_shield':
  poly(d,[(16,1),(28,7),(27,20),(22,27),(16,31),(9,27),(4,19),(4,7)],G);poly(d,[(16,4),(25,9),(24,19),(20,25),(16,27),(11,24),(7,18),(7,9)],P);d.arc((9,7,23,22),205,515,fill=C,width=2);gem(d,16,15,4);d.point((16,5),fill=GH)
 elif id=='reflected_path_fan':
  for i,pts in enumerate([[(15,28),(1,13),(4,6),(10,11)],[(15,28),(6,7),(12,2),(16,10)],[(15,28),(14,3),(21,2),(23,10)],[(15,28),(22,6),(29,9),(30,17)]]):poly(d,pts,[TD,P,V,T][i]);d.line([pts[0],pts[2]],fill=G,width=1)
  gem(d,15,26,3);d.line([(15,28),(13,31)],fill=GH,width=2)
 elif id=='still_volley_orb':
  d.ellipse((7,5,25,25),fill=O);d.ellipse((9,7,23,23),fill=V);d.ellipse((12,9,21,21),fill=T);d.arc((2,4,30,27),15,180,fill=G,width=2);d.arc((3,8,29,29),195,345,fill=GH,width=2);gem(d,15,14,4);d.point((5,7),fill=C);d.rectangle((25,22,27,24),fill=L)
 elif id=='wayback_chalk':
  poly(d,[(5,27),(5,21),(21,4),(27,8),(12,27)],TD);poly(d,[(7,22),(22,6),(25,9),(11,25)],H,1);d.line([(9,20),(15,23)],fill=GH,width=3);d.point((7,28),fill=C);d.point((3,29),fill=SH)
 elif id=='stopped_season_secateurs':
  d.ellipse((2,18,13,30),fill=O);d.ellipse((4,20,11,28),fill=G);d.ellipse((6,22,9,26),fill=(0,0,0,0));d.ellipse((18,19,29,30),fill=O);d.ellipse((20,21,27,28),fill=G);d.ellipse((22,23,25,26),fill=(0,0,0,0));poly(d,[(7,21),(18,12),(27,3),(26,11),(14,23)],SH,1);poly(d,[(22,23),(13,15),(7,3),(5,8),(8,18),(17,25)],S,1);gem(d,15,18,2)
 elif id=='living_water_can':
  poly(d,[(7,12),(24,12),(26,27),(6,27)],P);d.arc((17,4,30,22),190,500,fill=G,width=3);poly(d,[(7,17),(2,13),(1,8),(4,8),(10,18)],T);d.line([(8,14),(21,14)],fill=GH,width=2);gem(d,15,21,4);d.line([(1,5),(1,3)],fill=C,width=1)
 elif id=='quiet_flame_bottle':
  poly(d,[(12,2),(20,2),(20,8),(25,13),(26,25),(21,29),(11,29),(6,25),(7,13),(12,8)],TD);d.rectangle((12,3,20,6),fill=G,outline=O);poly(d,[(11,12),(21,12),(23,16),(23,24),(20,27),(12,27),(9,24),(9,16)],T,1);poly(d,[(16,24),(12,20),(14,15),(16,11),(18,16),(20,19),(19,23)],V,1);d.line([(16,17),(16,22)],fill=L,width=2);d.line([(10,16),(10,21)],fill=H,width=1)
 else:raise ValueError(id)
 return im
for _,id,ru,en,tip,kind in CAT:
 if kind!='block':
  icon(id).save(A/'textures/item'/f'{id}.png');write(A/'models/item'/f'{id}.json',{'parent':'minecraft:item/handheld' if kind in ['weapon','tool'] and id not in ['still_volley_orb','quiet_flame_bottle','living_water_can','reverse_echo_bracelet'] else 'minecraft:item/generated','textures':{'layer0':'kelmagic:item/'+id}})
# Correct shield pose and orb views remain item-driven, with a custom block animation.
# Armor atlas and inventory silhouettes. Each set has its own palette and embroidery motif.
SETS={'deep_source':('Облачение глубокого источника','Deep Source Vestments',P,T,GH),'steady_anchor':('Латы неподвижной опоры','Steadfast Anchor Armor','#343d57',S,G),'grove_keeper':('Покров хранителя сумеречной рощи','Dusk Grove Keeper Garb','#253f4c','#55a997',GH),'strict_negation':('Доспехи строгого отрицания','Strict Negation Armor','#262237','#77678d',SH)}
for set,(ru,en,base,accent,metal) in SETS.items():
 im,d=canvas(128);d.rectangle((0,0,127,127),fill=base)
 # Deliberate pixel weave; bounded tonal variation, no noisy dithering.
 for y in range(128):
  for x in range(128):
   if x%8==0 or y%16==15:d.point((x,y),fill=O)
   elif (x+y)%16==1:d.point((x,y),fill=accent)
 for y in [5,13,19,27,35,43,51,67,83,91,99,115]:d.line([(1,y),(126,y)],fill=metal,width=1)
 for x in [4,12,20,28,36,44,52,60,68,76,84,92,100,108,116,124]:
  d.line([(x,1),(x,126)],fill=accent,width=1)
  for y in range(6,125,16):
   if set=='deep_source':d.line([(x-2,y),(x,y-2),(x+2,y),(x,y+2)],fill=C,width=1)
   elif set=='grove_keeper':d.line([(x,y-3),(x,y+3),(x-2,y+1),(x,y-1),(x+2,y-2)],fill=H,width=1)
   elif set=='strict_negation':d.line([(x-2,y-2),(x+2,y+2)],fill=SH,width=1);d.line([(x-2,y+2),(x+2,y-2)],fill=SH,width=1)
   else:d.rectangle((x-2,y-2,x+2,y+2),outline=G,width=1)
 # Jewel regions for dedicated model UVs.
 d.rectangle((64,48,76,60),fill=TD,outline=metal,width=1);gem(d,69,53,3)
 # Face visor reads as a slit, not a human face on cloth.
 if set in ['strict_negation','steady_anchor']:d.rectangle((8,9,15,12),fill=O);d.line([(8,10),(15,10)],fill=C,width=1)
 for layer in [1,2]:im.save(A/'textures/models/armor'/f'{set}_layer_{layer}.png')
 for part in ['helmet','chestplate','leggings','boots']:
  ii,dd=canvas()
  if part=='helmet':
   poly(dd,[(8,4),(16,1),(24,4),(28,13),(26,26),(21,26),(20,12),(12,12),(11,26),(6,26),(4,13)],base);dd.line([(9,8),(16,5),(23,8)],fill=metal,width=2);gem(dd,16,7,2)
   if set=='grove_keeper':dd.line([(7,8),(4,3)],fill=accent,width=2);dd.line([(24,8),(27,2)],fill=accent,width=2)
   if set=='strict_negation':dd.rectangle((10,13,22,18),fill=accent,outline=O);dd.line([(12,15),(20,15)],fill=C,width=1)
  elif part=='chestplate':
   poly(dd,[(10,5),(13,8),(19,8),(22,5),(30,9),(28,20),(24,19),(24,29),(8,29),(8,19),(4,20),(2,9)],base);dd.line([(9,10),(16,13),(23,10)],fill=metal,width=2);dd.line([(9,24),(23,24)],fill=metal,width=2);gem(dd,16,15,3)
   if set in ['deep_source','grove_keeper']:dd.line([(11,18),(10,28)],fill=accent,width=2);dd.line([(21,18),(22,28)],fill=accent,width=2)
   else:dd.line([(4,10),(9,8)],fill=accent,width=3);dd.line([(23,8),(28,10)],fill=accent,width=3)
  elif part=='leggings':poly(dd,[(6,4),(26,4),(25,29),(18,29),(16,15),(14,29),(7,29)],base);dd.line([(8,7),(24,7)],fill=metal,width=2);dd.line([(9,13),(10,26)],fill=accent,width=2);dd.line([(23,13),(22,26)],fill=accent,width=2)
  else:
   for x in [3,18]:poly(dd,[(x+3,4),(x+10,4),(x+10,20),(x+13,23),(x+13,28),(x,28),(x,23),(x+3,20)],base);dd.line([(x+3,9),(x+10,9)],fill=metal,width=2);dd.line([(x+2,25),(x+11,25)],fill=accent,width=2)
  ii.save(A/'textures/item'/f'{set}_{part}.png');write(A/'models/item'/f'{set}_{part}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'kelmagic:item/{set}_{part}'}})
# Block texture families: dark indigo materials, gold inlay, cyan facets.
def tile(id,kind):
 im,d=canvas(16);d.rectangle((0,0,15,15),fill=N);d.rectangle((0,0,15,15),outline=O);d.rectangle((1,1,14,14),outline=GD)
 if kind=='stone':
  d.rectangle((2,2,13,13),fill=P);d.line([(3,12),(3,4),(8,4),(8,10),(12,10)],fill=G,width=1);d.point((8,7),fill=C)
 elif kind=='rim':
  d.rectangle((2,2,13,13),fill=G);d.rectangle((3,3,12,12),fill=GD);d.line([(2,2),(13,2)],fill=GH);d.line([(3,4),(12,4)],fill=GH)
 elif kind=='crystal':
  d.polygon([(1,1),(10,0),(15,5),(12,15),(3,15),(0,8)],fill=T);d.line([(1,1),(6,5),(4,14)],fill=C,width=2);d.line([(7,4),(10,1),(14,5)],fill=H,width=1);d.line([(8,5),(11,14)],fill=TD,width=2)
 elif kind=='seal':
  d.ellipse((2,2,13,13),outline=G,width=1);d.line([(4,4),(11,11)],fill=C,width=2);d.line([(4,11),(11,4)],fill=GH,width=1)
 elif kind=='root':
  d.line([(8,1),(8,9),(3,14)],fill=G,width=2);d.line([(8,6),(3,3)],fill=T,width=2);d.line([(8,9),(13,13)],fill=C,width=1)
 elif kind=='void':
  d.rectangle((3,2,12,13),fill=O,outline=V);d.line([(4,4),(11,11)],fill=L,width=1);d.line([(4,11),(11,4)],fill=G,width=1)
 im.save(A/'textures/block'/f'{id}.png')
for n,k in [('artifact_stone','stone'),('artifact_rim','rim'),('artifact_crystal','crystal'),('artifact_seal','seal'),('artifact_root','root'),('artifact_void','void')]:tile(n,k)
# Animated fluid, four slow frames; deliberately low-contrast interior and bright perimeter.
im=Image.new('RGBA',(16,64));dr=ImageDraw.Draw(im)
for frame in range(4):
 for y in range(16):
  for x in range(16):
   v=(math.sin((x+y-frame*3)*math.pi/8)+1)*.5;dr.point((x,y+frame*16),fill=(35+int(v*20),118+int(v*65),141+int(v*55),235))
 dr.rectangle((1,1+frame*16,14,14+frame*16),outline=H)
im.save(A/'textures/block/artifact_mana.png');write(A/'textures/block/artifact_mana.png.mcmeta',{'animation':{'frametime':12,'interpolate':False}})
def element(a,b,texture,rotation=None):
 e={'from':a,'to':b,'faces':{face:{'texture':'#'+texture,'uv':[0,0,16,16]} for face in ['up','down','north','south','west','east']}}
 if rotation:e['rotation']=rotation
 return e
def model(id,elements):write(A/'models/block'/f'{id}.json',{'parent':'minecraft:block/block','ambientocclusion':True,'textures':{'particle':'kelmagic:block/artifact_stone','stone':'kelmagic:block/artifact_stone','rim':'kelmagic:block/artifact_rim','crystal':'kelmagic:block/artifact_crystal','seal':'kelmagic:block/artifact_seal','root':'kelmagic:block/artifact_root','void':'kelmagic:block/artifact_void','mana':'kelmagic:block/artifact_mana'},'elements':elements})
for _,id,_,_,_,kind in CAT:
 if kind!='block':continue
 models=[]
 if id in ['mana_cup','mana_bowl','mana_basin']:
  width={'mana_cup':10,'mana_bowl':13,'mana_basin':15}[id];lo=(16-width)/2;hi=16-lo
  for fill in range(5):
   es=[element([lo,0,lo],[hi,2,hi],'stone'),element([lo,2,lo],[lo+1.5,9,hi],'rim'),element([hi-1.5,2,lo],[hi,9,hi],'rim'),element([lo+1.5,2,lo],[hi-1.5,9,lo+1.5],'rim'),element([lo+1.5,2,hi-1.5],[hi-1.5,9,hi],'rim'),element([6.5,2,6.5],[9.5,16,9.5],'crystal')]
   if fill:es.append(element([lo+1.5,2,lo+1.5],[hi-1.5,2+fill*1.4,hi-1.5],'mana'))
   name=f'{id}_{fill}';model(name,es);models.append((fill,name))
 elif id=='delayed_sound_crystal':
  model(id,[element([4,0,4],[12,2,12],'rim'),element([5,2,5],[11,14,11],'crystal',{'origin':[8,8,8],'axis':'y','angle':22.5}),element([6,14,6],[10,16,10],'rim')]);models=[(None,id)]
 elif id=='crystal_striker':
  for on in [False,True]:
   name=id+('_on' if on else '');model(name,[element([3,0,3],[13,2,13],'stone'),element([7,2,7],[9,11,9],'rim'),element([3,10,5],[13,14,11],'rim',{'origin':[8,10,8],'axis':'z','angle':-22.5 if on else 22.5})]);models.append((on,name))
 elif id=='forbidden_threshold_base':model(id,[element([0,0,0],[16,14,16],'stone'),element([0,14,0],[16,16,16],'seal')]);models=[(None,id)]
 else:
  tex='root' if id=='grove_mana_bowl' else 'void' if id=='mute_sky_obelisk' else 'seal'
  es=[element([2,0,2],[14,3,14],'rim'),element([4,3,4],[12,13,12],tex),element([3,13,3],[13,15,13],'rim')]
  if id=='grove_mana_bowl':es=[element([2,0,2],[14,3,14],'root'),element([4,3,4],[12,7,12],'stone'),element([3,7,3],[13,9,13],'rim'),element([4,9,4],[12,9.5,12],'mana')]
  if id=='night_shadow_catcher':es=[element([3,0,3],[13,2,13],'rim'),element([5,3,5],[11,13,11],'crystal'),element([3,14,3],[13,16,13],'rim')]+[element([x,2,z],[x+1,14,z+1],'rim') for x,z in [(3,3),(12,3),(3,12),(12,12)]]
  model(id,es);models=[(None,id)]
 if id.startswith('mana_'):
  write(A/'blockstates'/f'{id}.json',{'variants':{f'fill={n}':{'model':'kelmagic:block/'+name} for n,name in models}});base=id+'_0'
 elif id=='crystal_striker':write(A/'blockstates'/f'{id}.json',{'variants':{f'powered={str(n).lower()}':{'model':'kelmagic:block/'+name} for n,name in models}});base=id
 else:write(A/'blockstates'/f'{id}.json',{'variants':{'':{'model':'kelmagic:block/'+id}}});base=id
 write(A/'models/item'/f'{id}.json',{'parent':'kelmagic:block/'+base})
 write(D/'kelmagic/loot_table/blocks'/f'{id}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'kelmagic:'+id}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
model('fleeting_path',[element([0,12,0],[16,13,16],'crystal'),element([0,13,0],[16,15,1],'rim'),element([0,13,15],[16,15,16],'rim'),element([1,13,1],[15,13.2,15],'mana')]);write(A/'blockstates/fleeting_path.json',{'variants':{'':{'model':'kelmagic:block/fleeting_path'}}});write(D/'kelmagic/loot_table/blocks/fleeting_path.json',{'type':'minecraft:block','pools':[]})
# Curios slots are KelMagic-specific: no overriding another mod's standard ring/necklace counts.
slots={}
for _,id,ru,en,tip,kind in CAT:
 if kind in ['ring','necklace','bracelet','kelmagic_earring','hands','charm','belt','back']:
  slot=kind if kind.startswith('kelmagic_') else 'kelmagic_'+kind;slots.setdefault(slot,[]).append('kelmagic:'+id)
for n,(slot,items) in enumerate(slots.items()):
 count=2 if slot=='kelmagic_ring' else 1
 write(D/'kelmagic/curios/slots'/f'{slot}.json',{'size':count,'order':50+n,'icon':f'kelmagic:slot/empty_{slot}','validators':['curios:tag'],'add_cosmetic':False,'render_toggle':False})
 write(D/'curios/tags/item'/f'{slot}.json',{'replace':False,'values':items})
 source=Image.open(A/'textures/item'/f'{items[0].split(":")[1]}.png').convert('RGBA').resize((16,16),Image.Resampling.NEAREST);pix=source.load()
 for y in range(16):
  for x in range(16):
   if pix[x,y][3]:pix[x,y]=(170,173,182,pix[x,y][3])
 source.save(A/'textures/slot'/f'empty_{slot}.png')
write(D/'kelmagic/curios/entities/artifact_player.json',{'entities':['minecraft:player'],'slots':list(slots)})
write(D/'kelmagic/tags/entity_type/magical_projectiles.json',{'replace':False,'values':['kelmagic:word_bolt','minecraft:small_fireball','minecraft:fireball','minecraft:dragon_fireball','minecraft:wither_skull','minecraft:shulker_bullet']})
write(D/'kelmagic/damage_type/oath_debt.json',{'message_id':'kelmagic.oath_debt','scaling':'never','exhaustion':0})
for tag in ['bypasses_armor','bypasses_effects','bypasses_resistance','bypasses_enchantments']:
 path=D/'minecraft/tags/damage_type'/f'{tag}.json';old=json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]};old['values']=list(dict.fromkeys(old['values']+['kelmagic:oath_debt']));write(path,old)
# Language and controls.
extra={
'silenced':'Магическая речь здесь невозможна.','word_blocked':'Недостаточно маны: перстень не позволяет платить здоровьем.','denied':'Обелиск не разрешает доступ.','player_missing':'Игрок с таким именем сейчас не в мире.','access_updated':'Разрешение изменено: %s','access_info':'Доверенных игроков: %s. Используйте именную бирку с именем игрока.','portal_locked':'Оба направления этой пары порогов заперты.','grove_info':'Корневая чаша питает ману только на магической траве.','record_ready':'Запись кристалла: произнесите 1–3 слова за 20 секунд, затем ударьте молоточком.','record_limit':'Кристалл удерживает не более трёх слов Кэл.','recorded':'Кристалл запомнил: %s','stored':'Мана: %s / %s','dew':'Сумеречная роса: %s%%','witnessed':'В серьге сохранился отголосок чужой формулы.','witness_note':'Отголосок %s: %s','echo_distance':'Отголосок: примерно %s блоков.','no_echo':'Поблизости нет свежего магического отголоска.','bottle_full':'Внутри удерживается магическое пламя.','dagger_charge':'Сила невысказанного: %s'}
settips={'deep_source':'Каждая часть: +4 маны. Восстановление замедляется до ×0,6 при полном комплекте. Без отдельного резерва.','steady_anchor':'Части: защита от взрывов и отбрасывания. Полный комплект: B — закрепиться на месте / выйти из стойки.','grove_keeper':'Части: защита и восстановление в роще. Полный комплект: атакующего опутывают корни, перезарядка 10 секунд.','strict_negation':'Части: сокращают полезные и вредные эффекты. Полный комплект: мгновенное снятие и иммунитет к статусам и магическому воздействию. Цена магии здоровьем сохраняется.'}
for lang in ['ru_ru','en_us']:
 path=A/'lang'/f'{lang}.json';j=json.loads(path.read_text())
 for _,id,ru,en,tip,kind in CAT:j[('block' if kind=='block' else 'item')+'.kelmagic.'+id]=ru if lang=='ru_ru' else en;j['kelmagic.artifact.tip.'+id]=tip if lang=='ru_ru' else en+' — see R14 controls in README-R14-RU.md.'
 for key,value in extra.items():j['kelmagic.artifact.'+key]=value
 for set,(ru,en,*_) in SETS.items():
  j['kelmagic.artifact.set.'+set]=settips[set]
  for part,label in [('helmet','Головной убор'),('chestplate','Нагрудник'),('leggings','Поножи'),('boots','Обувь')]:j[f'item.kelmagic.{set}_{part}']=f'{label}: {ru}' if lang=='ru_ru' else en+' '+part
 for slot in slots:j['curios.identifier.'+slot]={'kelmagic_ring':'Кольцо Кэл','kelmagic_necklace':'Амулет Кэл','kelmagic_bracelet':'Браслет Кэл','kelmagic_earring':'Серьга Кэл','kelmagic_hands':'Перчатки Кэл','kelmagic_charm':'Оберег Кэл','kelmagic_belt':'Пояс Кэл','kelmagic_back':'Плащ Кэл'}[slot]
 j.update({'key.categories.kelmagic_artifacts':'Артефакты KelMagic','key.kelmagic.reserve':'Мана медальона (Shift — вложить)','key.kelmagic.dew':'Очиститься сумеречной росой','key.kelmagic.sample':'Поднять / отпустить образец','key.kelmagic.anchor':'Стойка неподвижной опоры','death.attack.kelmagic.oath_debt':'%1$s принял на себя чужой удар'})
 write(path,j)
# Original sound cues, mono Vorbis, clean amplitude and short tails.
SOUNDS={'artifact_chime':(.65,[880,1320,1760]),'mana_transfer':(.55,[330,495,660]),'crystal_strike':(.8,[660,990,1485]),'spell_absorb':(.75,[220,440,880]),'root_bind':(.5,[146.8,220,293.6]),'silence_fall':(.65,[196,294,392]),'threshold_lock':(.7,[174.6,349.2,523.3]),'dream_guard':(1.1,[261.6,392,523.2]),'path_weave':(.7,[440,660,990])}
j=json.loads((A/'sounds.json').read_text());tmp=R/'build/artifact-audio';tmp.mkdir(parents=True,exist_ok=True)
for name,(dur,notes) in SOUNDS.items():
 rate=44100;rng=random.Random(name);samples=[]
 for i in range(int(rate*dur)):
  t=i/rate;attack=min(1,t/.008);env=attack*math.exp(-t*(5 if name!='dream_guard' else 3));v=0
  for n,f in enumerate(notes):v+=math.sin(2*math.pi*f*t+math.sin(t*17)*.05)*math.exp(-t*(n+1)*1.5)/(n+1)
  v+=rng.uniform(-1,1)*math.exp(-t*45)*.16
  if name=='spell_absorb':v+=.35*math.sin(2*math.pi*(160*t+300*t*t))*math.sin(min(1,t/.5)*math.pi)
  samples.append(int(max(-1,min(1,v*env*.32))*32767))
 wav=tmp/(name+'.wav')
 with wave.open(str(wav),'w') as w:w.setparams((1,2,rate,0,'NONE','not compressed'));w.writeframes(struct.pack('<'+'h'*len(samples),*samples))
 subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','5',str(A/'sounds/artifact'/f'{name}.ogg')],check=True)
 j[name]={'sounds':[{'name':'kelmagic:artifact/'+name,'stream':False}],'subtitle':'subtitles.kelmagic.'+name}
 for lang in ['ru_ru','en_us']:
  p=A/'lang'/f'{lang}.json';words=json.loads(p.read_text());words['subtitles.kelmagic.'+name]={'artifact_chime':'Отзвук артефакта','mana_transfer':'Перетекание маны','crystal_strike':'Удар по кристаллу','spell_absorb':'Поглощение заклинания','root_bind':'Сплетение корней','silence_fall':'Наступает тишина','threshold_lock':'Порог заперт','dream_guard':'Шёпот спокойного сна','path_weave':'Плетение тропы'}[name];write(p,words)
write(A/'sounds.json',j)
# Inventory proof sheet, not a Minecraft screenshot.
font=ImageFont.truetype('/usr/share/fonts/dejavu/DejaVuSans.ttf',12) if Path('/usr/share/fonts/dejavu/DejaVuSans.ttf').exists() else ImageFont.load_default()
paths=[A/'textures/item'/f'{id}.png' for _,id,*rest in CAT if rest[-1]!='block']+[A/'textures/item'/f'{s}_{part}.png' for s in SETS for part in ['helmet','chestplate','leggings','boots']]
cols=8;cw,ch=128,116;sheet=Image.new('RGB',(cols*cw,math.ceil(len(paths)/cols)*ch),'#17192c');sd=ImageDraw.Draw(sheet)
for i,p in enumerate(paths):
 x=(i%cols)*cw;y=(i//cols)*ch;im=Image.open(p).resize((80,80),Image.Resampling.NEAREST);sheet.paste(im,(x+24,y+5),im);words=p.stem.replace('_',' ')
 # Small proof labels outside the game texture.
 sd.text((x+3,y+87),words[:20],font=font,fill='#d4c8e6');sd.text((x+3,y+101),words[20:40],font=font,fill='#9aa5bb')
proof=R/'art/artifact-r14';proof.mkdir(parents=True,exist_ok=True);sheet.save(proof/'inventory-proof.png')
print(f'Generated {len(paths)} original item icons, 4 armor atlases, block models, 8 Curios slot definitions and 9 original OGG cues.')
# Temporary defensive root walls have no obtainable item or loot.
write(A/'models/block/fleeting_root_wall.json',json.loads('{\n  "parent": "minecraft:block/block",\n  "textures": {\n    "particle": "kelmagic:block/artifact_root",\n    "root": "kelmagic:block/artifact_root"\n  },\n  "elements": [\n    {\n      "from": [\n        1,\n        0,\n        1\n      ],\n      "to": [\n        5,\n        21,\n        5\n      ],\n      "faces": {\n        "up": {\n          "texture": "#root"\n        },\n        "down": {\n          "texture": "#root"\n        },\n        "north": {\n          "texture": "#root"\n        },\n        "south": {\n          "texture": "#root"\n        },\n        "east": {\n          "texture": "#root"\n        },\n        "west": {\n          "texture": "#root"\n        }\n      }\n    },\n    {\n      "from": [\n        11,\n        0,\n        11\n      ],\n      "to": [\n        15,\n        22,\n        15\n      ],\n      "faces": {\n        "up": {\n          "texture": "#root"\n        },\n        "down": {\n          "texture": "#root"\n        },\n        "north": {\n          "texture": "#root"\n        },\n        "south": {\n          "texture": "#root"\n        },\n        "east": {\n          "texture": "#root"\n        },\n        "west": {\n          "texture": "#root"\n        }\n      }\n    },\n    {\n      "from": [\n        2,\n        7,\n        3\n      ],\n      "to": [\n        14,\n        10,\n        13\n      ],\n      "faces": {\n        "up": {\n          "texture": "#root"\n        },\n        "down": {\n          "texture": "#root"\n        },\n        "north": {\n          "texture": "#root"\n        },\n        "south": {\n          "texture": "#root"\n        },\n        "east": {\n          "texture": "#root"\n        },\n        "west": {\n          "texture": "#root"\n        }\n      }\n    },\n    {\n      "from": [\n        3,\n        15,\n        2\n      ],\n      "to": [\n        13,\n        18,\n        14\n      ],\n      "faces": {\n        "up": {\n          "texture": "#root"\n        },\n        "down": {\n          "texture": "#root"\n        },\n        "north": {\n          "texture": "#root"\n        },\n        "south": {\n          "texture": "#root"\n        },\n        "east": {\n          "texture": "#root"\n        },\n        "west": {\n          "texture": "#root"\n        }\n      }\n    }\n  ]\n}'))
write(A/'blockstates/fleeting_root_wall.json',json.loads('{\n  "variants": {\n    "": {\n      "model": "kelmagic:block/fleeting_root_wall"\n    }\n  }\n}'))
write(D/'kelmagic/loot_table/blocks/fleeting_root_wall.json',{'type':'minecraft:block','pools':[]})
# Final UV-specific paint pass supersedes the coarse construction atlas.
import runpy
runpy.run_path(str(R/'tools/artifact-r14/armor_atlases.py'))
