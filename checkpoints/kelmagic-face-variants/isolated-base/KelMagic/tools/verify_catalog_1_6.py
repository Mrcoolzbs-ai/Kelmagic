"""Check registry inputs, canonical forms, resources and preservation for KelMagic 1.6.0."""
from pathlib import Path
from PIL import Image
import json,re,sys,hashlib
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/kelmagic';words=json.loads((r/'src/main/resources/data/kelmagic/magic/words.json').read_text())['words'];profiles=json.loads((r/'src/main/resources/data/kelmagic/magic/effects.json').read_text())['effects'];checks=0
def check(c,m):
 global checks
 assert c,m
 checks+=1
check(len(words)==180,'180 words');check(len({w['word'] for w in words})==180,'unique words');check(len(profiles)==172,'172 additional word profiles')
check([w['word'] for w in words[:6]]==['mina','tuna','sila','pir','no','lu'],'stable saved note indices')
check(next(w for w in words if w['word']=='sin')['translation']=='выход','sin unchanged')
check(next(w for w in words if w['word']=='nis')['source'].startswith('User-confirmed'),'nis user-authorized')
check(not any(w['word'] in ['zin','pol','dul','mus','wuk','zek'] for w in words),'unresolved roots excluded')
font=Image.open(a/'textures/font/kel.png').convert('RGBA');meta=json.loads((a/'glyph-map.json').read_text());cw,ch=meta['units']['cell'];forms=meta['forms'];cons='ptkbdgmnslvrj';vowels='aeiou'
for c in cons:
 for v in ['']+list(vowels):
  unit=c+v;check(unit in forms,'form '+unit);u,y=forms[unit]['cell'];check(font.crop((u*cw,y*ch,(u+1)*cw,(y+1)*ch)).getbbox() is not None,'nonempty glyph '+unit)
for row in words:
 w=row['word'];check(bool(re.fullmatch('[ptkbdgmnslvrjaeiou]{2,8}',w)),'canonical alphabet '+w)
 units=[];i=0
 while i<len(w):
  c=w[i];check(c in cons,'consonant start '+w);i+=1
  if i<len(w) and w[i] in vowels:c+=w[i];i+=1
  units.append(c)
 check(all(u in forms for u in units),'complete writing '+w)
 model=json.loads((a/f'models/item/manuscript_{w}.json').read_text());check(model['textures']['layer0']==f'kelmagic:item/manuscript_{w}','manuscript model '+w)
 im=Image.open(a/f'textures/item/manuscript_{w}.png');check(im.size==(32,32),'manuscript size '+w);check(im.getbbox() is not None,'visible manuscript '+w)
 check(row['source'] in ['gram1.png','gram2.png','ez_word1.png','ez_word2.png'] or row['source'].startswith('User-confirmed'),'declared source '+w)
 check(w in profiles or w in ['mina','tuna','sila','pir','no','lu','muk','nis'],'effect or operator '+w)
 for lang in ['ru_ru','en_us']:check('item.kelmagic.manuscript_'+w in json.loads((a/f'lang/{lang}.json').read_text()),'localized '+w)
for w,p in profiles.items():
 check(p['action'] in {'BUFF','HEAL','DAMAGE','FEED','EXTINGUISH','CLEANSE','PUSH','PULL','SPIN','SCAN','OPEN','CLOSE','VANILLA_FIRE'},'implemented action '+w)
 check(p['baseDuration']==240 and p['basePower']>0,'bounded profile '+w)
for f in (r/'src/main/resources').rglob('*.json'):json.loads(f.read_text());checks+=1
check(Image.open(a/'textures/mob_effect/flame_life.png').size==(18,18),'flame life status icon')
check(json.loads((r/'src/main/resources/data/kelmagic/recipe/copy_codex.json').read_text())['type']=='kelmagic:copy_codex','copy recipe')
check('kelmagic:arcane_fire' in json.loads((r/'src/main/resources/data/minecraft/tags/damage_type/bypasses_resistance.json').read_text())['values'],'only flame-life counters arcane damage')
if len(sys.argv)>1:
 old=Path(sys.argv[1]);oa=old/'main/resources/assets/kelmagic'
 for sub in ['textures/glyph','textures/font','font']:
  for f in (oa/sub).rglob('*'):
   if f.is_file():check(f.read_bytes()==(a/f.relative_to(oa)).read_bytes(),'canonical asset unchanged '+str(f))
 for sub in ['textures/models/armor','textures/block']:
  for f in (oa/sub).rglob('*'):
   if f.is_file():check(f.read_bytes()==(a/f.relative_to(oa)).read_bytes(),'existing visual unchanged '+str(f))
 check((oa/'textures/item/mana_core.png').read_bytes()==(a/'textures/item/mana_core.png').read_bytes(),'mana core unchanged')
print('CATALOG ASSET CHECKS PASSED:',checks)
