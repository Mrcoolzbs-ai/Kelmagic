from pathlib import Path
import json,re,sys,hashlib
from PIL import Image
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/kelmagic';d=r/'src/main/resources/data';j=r/'src/main/java/dev/kelmagic';checks=0
def ck(c,m):
 global checks
 assert c,m;checks+=1
words=json.loads((d/'kelmagic/magic/words.json').read_text())['words'];effects=json.loads((d/'kelmagic/magic/effects.json').read_text())['effects'];sy=json.loads((d/'kelmagic/magic/synergies.json').read_text())['synergies']
ck(len(words)==180,'180 words');ck(len(effects)==172,'172 profiles');ck(len({(p['action'],p['status']) for p in effects.values()})==172,'no numeric-only duplicate signatures');ck(len(sy)==16,'16 named combinations')
ck([w['word'] for w in words[:6]]==['mina','tuna','sila','pir','no','lu'],'stable saved note indices');ck(any(w['word']=='nis' for w in words) and not any(w['word']=='zin' for w in words),'authorized nis');ck(next(w for w in words if w['word']=='sin')['translation']=='выход','sin preserved')
source='\n'.join((j/'server'/f).read_text() for f in ['ExpandedSpellEffects.java','EntitySpells.java','WorldSpells.java'])
implemented={'W_SOUND','W_PARTICLES','OPEN','CLOSE'}
for group in re.findall(r'case ((?:"[^"]+"(?:,\s*)?)+)\s*(?::|->)',source):implemented.update(re.findall(r'"([^"]+)"',group))
for w,p in effects.items():ck(p['action'] in implemented,'runtime handler '+w+' '+p['action']);ck(p['description'] and p['basePower']>0,'documented positive profile '+w)
for s in sy:
 ck(len(set(s['words']))==len(s['words']),'no repeated synergy words');ck(all(w in effects or w=='pir' for w in s['words']),'canonical synergy words');ck(f'"{s["id"]}"' in (j/'server/WorldSpells.java').read_text(),'synergy implemented')
for w in words:
 name=w['word'];ck((a/f'models/item/manuscript_{name}.json').exists(),'page model '+name);ck(Image.open(a/f'textures/item/manuscript_{name}.png').size==(32,32),'page texture '+name)
for n in ['cassette','cassette_player','recorded_cassette']:
 ck(Image.open(a/f'textures/item/{n}.png').size==(32,32),'icon '+n);ck((a/f'models/item/{n}.json').exists(),'icon model '+n)
for n in ['tape_deck','tape_player']:ck(Image.open(a/f'textures/gui/{n}.png').size==(256,256),'GUI atlas '+n)
for n in ['cassette','cassette_deck','cassette_player']:
 rec=json.loads((d/f'kelmagic/recipe/{n}.json').read_text());ck(rec['type']=='minecraft:crafting_shaped','shaped recipe '+n);ck(all(len(row)==3 for row in rec['pattern']),'3x3 '+n);ck(rec['result']=={'id':'kelmagic:'+n,'count':1},'result '+n)
rec=json.loads((d/'kelmagic/recipe/cassette.json').read_text());ck(rec['pattern']==['IPI','SRS','STS'],'exact requested tape pattern');ck({k:v['item'] for k,v in rec['key'].items()}=={'I':'minecraft:iron_nugget','P':'minecraft:paper','S':'minecraft:slime_ball','R':'minecraft:redstone','T':'minecraft:string'},'exact tape ingredients')
rec=json.loads((d/'kelmagic/recipe/cassette_player.json').read_text());ck({v['item'] for v in rec['key'].values()}=={'kelmagic:mana_core','minecraft:redstone_block','minecraft:quartz','minecraft:stone_button','minecraft:iron_ingot','minecraft:copper_ingot','minecraft:slime_ball'},'all seven requested player ingredients')
ck(len(json.loads((a/'blockstates/cassette_deck.json').read_text())['variants'])==4,'four deck facings');ck((d/'kelmagic/loot_table/blocks/cassette_deck.json').exists(),'deck loot');ck('addCooldown' not in (j/'audio/TapePlayerItem.java').read_text(),'no player cooldown');ck('.durability(' not in (j/'MagicAudio.java').read_text(),'no player durability')
for lang in ['ru_ru','en_us']:
 v=json.loads((a/f'lang/{lang}.json').read_text());ck(v['kelmagic.no_target'] in ['Нет подходящей цели','No suitable target'],'neutral target hint');
 for key in ['item.kelmagic.cassette','item.kelmagic.recorded_cassette','item.kelmagic.cassette_player','block.kelmagic.cassette_deck','kelmagic.deck.done','death.attack.kelmagic.scripter_mana_debt.1','death.attack.kelmagic.scripter_spell']:ck(bool(v.get(key)),'localization '+lang+' '+key)
for p in (r/'src/main/resources').rglob('*.json'):json.loads(p.read_text());checks+=1
if len(sys.argv)>1:
 old=Path(sys.argv[1]);oldassets=old/'main/resources/assets/kelmagic'
 for sub in ['textures/glyph','textures/font','font','textures/models/armor','textures/block']:
  for p in (oldassets/sub).rglob('*'):
   if p.is_file():ck(p.read_bytes()==(a/p.relative_to(oldassets)).read_bytes(),'old visual intact '+str(p.relative_to(oldassets)))
 ck((oldassets/'textures/item/mana_core.png').read_bytes()==(a/'textures/item/mana_core.png').read_bytes(),'mana core intact')
print('REVISION 1.7 ASSET CHECKS PASSED:',checks)
