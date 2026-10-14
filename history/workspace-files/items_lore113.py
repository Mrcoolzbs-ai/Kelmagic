from pathlib import Path
import json
P=Path('/data/KelMagic');A=P/'src/main/resources/assets/kelmagic/lang'
ru={
'mana_core':'Лазурь дрожит внутри красного узора. Крепостные переписчики называли этот свет памятью дыхания.',
'magic_ingot':'В металле слышен тихий отзвук. Такое клеймо находят и на пороге мастерской, и под слоем пепла.',
'magic_cloth':'Нити проводят свет по старым швам. На изнанке узор похож на карту улиц, которых больше нет.',
'dragon_heart':'Под скорлупой не стихло первое дыхание. Кто-то назвал эту вещь сердцем, хотя на ней нет следа охоты.',
'scripter':'Небольшой переплёт помнит голос без хозяина. В последних крепостных списках записи считали вместо людей.',
'arcane_helmet':'Пластина над лбом хранит стёртое клеймо. На полях чертежа — очертание огромного крыла.',
'arcane_chestplate':'Под металлом оставлено место для живого дыхания. Из мастерской унесли доспехи; её хозяева не вернулись.',
'arcane_leggings':'Внутри пластин вырезаны короткие строки. Похожие знаки встречаются на кораблях за последними вратами.',
'arcane_boots':'Подошвы рассчитаны на камень и палубу. На старом чертеже дорога домой обведена, но не подписана.',
'mage_helmet':'Капюшон глушит ветер, не голос. Переписчики опускали его, когда в библиотеке становилось слишком тихо.',
'mage_chestplate':'Золотые швы сходятся возле груди. Пепел легко стряхнуть с ткани; забытые имена — труднее.',
'mage_leggings':'Длинные складки берегут свет в дороге. Последний обоз шёл без знамён и узнавал своих по этим швам.',
'mage_boots':'Мягкий шаг не тревожит пыль архивов. На выкройке отмечено, где спрятать маленький лист на случай бегства.',
'archivists_codex':'Уцелел переплёт, а не весь рассказ. Между найденными строками ещё помещается голос нового читателя.',
'creative_archivists_codex':'Все строки на месте. Только имена тех, кто оставил их, нельзя вернуть даже этой книгой.',
'ancient_manuscript':'Пыль не скрыла последнюю строку. Почерк меняется посреди листа, словно писавший перестал узнавать руку.',
'cassette':'На тонкой ленте хватает места для голоса. Его прежний владелец больше не понадобится механизму.',
'recorded_cassette':'Слова ждут следующего оборота. У оставленного дома нет хозяина, но внутри всё ещё кто-то говорит.',
'cassette_deck':'Два маленьких гнезда и терпеливый ход. Пока города пустели, эти механизмы учили тишину отвечать.',
'cassette_player':'Медный корпус бережёт чужие голоса. Последние путники брали с собой записи тех, кто не мог идти.',
'golden_thread':'Тонкое золото вплетено в дорожную нить. Нижние торговцы узнают этот блеск раньше, чем лицо пришедшего.',
'golden_wool':'Огонь проходит мимо золотых петель. На старых тюках поверх человеческой печати стоит чужая зарубка.',
'archivist_table':'На краю осталось место для второй книги. Переписчик будто вышел на минуту, хотя за дверью сменились леса.',
'magic_torch':'Малый свет отзывается без искры. Такие огни ставили у убежищ, когда солнце перестало обещать возвращение.',
'magic_lantern':'Внутри камня тихо движется свет. В затопленных залах он не переставал ждать читателя.',
'magic_wall_torch':'Свет держится у стены, словно чья-то оставленная метка. Старые пути узнают по нему в темноте.',
'temporary_end_gate':'Тёмная гладь узнаёт дорогу лишь ненадолго. По ту сторону до сих пор стоят города, забывшие свой голос.'}
en={
'mana_core':'Lapis trembles inside a red pattern. The keep scribes called this light a memory of breath.',
'magic_ingot':'A faint echo lives in the metal. Its mark survives beneath workshop dust and ash alike.',
'magic_cloth':'Light follows the old seams. The reverse resembles a map of streets that no longer exist.',
'dragon_heart':'The first breath has not faded beneath the shell. Someone named this a heart, though it bears no mark of a hunt.',
'scripter':'A small binding remembers a voice without its owner. The last keep registers counted recordings instead of people.',
'arcane_helmet':'A worn makers mark rests above the brow. A vast wing is sketched in the margin.',
'arcane_chestplate':'Room remains beneath the metal for living breath. The armour left the workshop; its makers did not return.',
'arcane_leggings':'Short lines are carved inside the plates. Similar signs survive aboard ships beyond the last gate.',
'arcane_boots':'Soles made for stone and deck. The way home is circled in the drawing, but has no name.',
'mage_helmet':'The hood softens wind, not speech. Scribes lowered it when the library grew too quiet.',
'mage_chestplate':'Golden seams meet at the chest. Ash falls easily from cloth; forgotten names do not.',
'mage_leggings':'Long folds shelter light on the road. The last convoy carried no banners and knew its own by these seams.',
'mage_boots':'Soft steps spare the archive dust. The pattern marks a hiding place for one small page in flight.',
'archivists_codex':'The binding survived, not the whole account. A new readers voice still fits between the recovered lines.',
'creative_archivists_codex':'Every line is here. Even this book cannot return the names of those who left them.',
'ancient_manuscript':'Dust spared the final line. The hand changes halfway down, as though its writer no longer knew their fingers.',
'cassette':'A thin ribbon has room for a voice. The mechanism will no longer need its former owner.',
'recorded_cassette':'The words await another turn. The abandoned house has no owner, yet someone still speaks within.',
'cassette_deck':'Two small sockets and a patient movement. While cities emptied, these machines taught silence to answer.',
'cassette_player':'Copper shelters borrowed voices. The last travellers carried recordings of those who could not walk.',
'golden_thread':'Fine gold woven into a travelling thread. The lower traders recognise its gleam before the newcomers face.',
'golden_wool':'Fire passes the golden loops. Another peoples tally crosses a human seal on the old bales.',
'archivist_table':'There is room at the edge for another book. The scribe seems gone for a moment; outside, forests have changed.',
'magic_torch':'A small light answers without a spark. Such lights marked shelters when sunrise ceased to promise a return.',
'magic_lantern':'Light moves quietly inside stone. In drowned halls it never stopped waiting for a reader.',
'magic_wall_torch':'Light clings to the wall like an abandoned sign. It still marks old roads in darkness.',
'temporary_end_gate':'The dark surface remembers the road only briefly. Beyond it stand cities that have forgotten their voices.'}
assert ru.keys()==en.keys()
for locale,items in [('ru_ru',ru),('en_us',en)]:
 p=A/(locale+'.json');d=json.loads(p.read_text());d.pop('advancements.kelmagic.archive_route.title',None);d.pop('advancements.kelmagic.archive_route.description',None)
 for key,text in items.items():d['lore.kelmagic.'+key]=text
 d['kelmagic.lore.mechanics']='Удерживайте Shift: применение' if locale=='ru_ru' else 'Hold Shift: use and effects'
 d['kelmagic.codex.mechanics']='Применение' if locale=='ru_ru' else 'Practical notes'
 if locale=='ru_ru':d['kelmagic.manuscript_revealed']='Рукопись раскрыта: %s. Добавьте её в кодекс на столе архивариуса.'
 p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
print('27 item fragments RU/EN saved')
