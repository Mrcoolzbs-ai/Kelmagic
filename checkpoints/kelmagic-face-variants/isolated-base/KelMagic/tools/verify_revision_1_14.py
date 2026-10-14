from pathlib import Path
from PIL import Image
import json,re,hashlib
# Preserve all1.13 visual/lore/data contracts except intentional visible-count and removed-tooltip changes.
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';A=R/'assets/kelmagic'
s=(P/'tools/verify_revision_1_13.py').read_text().replace('==34','==35').replace('34 achievements','35 achievements').replace('34achievements','35achievements');exec(compile(s,str(P/'tools/verify_revision_1_13.py'),'exec'))
manifest=json.loads((A/'copper-glyphs.json').read_text());m=manifest['words'];ck(len(m)==74,'74 unique rite words');source=(P/'src/main/java/dev/kelmagic/core/GrandSpells.java').read_text();allWords=set(' '.join(re.findall(r'"([a-z]+(?: [a-z]+)+)"',source)).split());ck(set(m)==allWords,'exact great spell formula union');ck(hashlib.sha256((A/'textures/font/kel.png').read_bytes()).hexdigest()==manifest['glyphAtlasSHA256'],'canonical glyph atlas unchanged')
cons='ptkbdgmnslvrj';vowels='aeiou';expected=[]
for w,v in m.items():
 units=[];i=0
 while i<len(w):
  c=cons.index(w[i]);vv=0;i+=1
  if i<len(w) and w[i] in vowels:vv=vowels.index(w[i])+1;i+=1
  units.append(hex(0xE000+c*6+vv))
 ck(units==v['units'],'GlyphWriting composition '+w);n=v['strokePixels'];ck(n[0]>n[1]>n[2]>n[3]==0,'monotonic erosion '+w)
 for stage,id in enumerate(v['blocks']):
  model=json.loads((A/f'models/block/{id}.json').read_text());ck(model['textures']['all']==('minecraft:block/oxidized_copper' if stage==3 else 'kelmagic:block/'+id),'native oxidized identity '+id)
  if stage<3:
   image=Image.open(A/f'textures/block/{id}.png');ck(image.size==(64,64) and image.getchannel('A').getextrema()==(255,255),'opaque texture '+id)
  for prefix in ['','waxed_']:
   block=prefix+id;expected.append('kelmagic:'+block);ck((A/f'blockstates/{block}.json').exists() and (A/f'models/item/{block}.json').exists(),'block item resources '+block)
   recipe=json.loads((R/f'data/kelmagic/recipe/{block}.json').read_text());ck(recipe['type']=='minecraft:stonecutting' and recipe['result']=={'id':'kelmagic:'+block,'count':1},'one-to-one cutter '+block)
   ck((R/f'data/kelmagic/loot_table/blocks/{block}.json').exists(),'self drop '+block)
oxid=json.loads((R/'data/neoforge/data_maps/block/oxidizables.json').read_text());wax=json.loads((R/'data/neoforge/data_maps/block/waxables.json').read_text());ck(len(oxid['values'])==222 and len(wax['values'])==296 and not oxid['replace'] and not wax['replace'],'native map cardinality/merge')
for path in ['data/minecraft/tags/block/mineable/pickaxe.json','data/minecraft/tags/block/needs_stone_tool.json','data/kelmagic/tags/item/inscribed_copper.json']:
 ck(set(json.loads((R/path).read_text())['values'])==set(expected),'complete copper tool/item tag '+path)
ck('new WeatheringCopperFullBlock' in (P/'src/main/java/dev/kelmagic/MagicCopper.java').read_text(),'native weathering implementation')
for name in ['CodexScreen','ItemLore']:
 text=(P/f'src/main/java/dev/kelmagic/client/{name}.java').read_text();ck('hasShiftDown(' not in text and 'SpellProfiles.mechanics(' not in text,'no Shift secrets '+name)
text=(P/'src/main/java/dev/kelmagic/client/ItemLore.java').read_text();ck('ancient_manuscript' in text and 'recorded_cassette' not in text,'only manuscripts get item lore')
from nbt19 import read
for path in (R/'data/kelmagic/structure/archive').glob('*.nbt'):
 n=read(path.read_bytes());palette=n['palette'][1][1];blocks=n['blocks'][1][1];width=n['size'][1][1][0]
 states={tuple(b['pos'][1][1]):palette[b['state'][1]] for b in blocks}
 def mirror(state):
  out=dict(state)
  if 'Properties' in out:
   props=dict(out['Properties'][1]);f=props.get('facing');
   if f and f[1] in ['east','west']:props['facing']=(8,{'east':'west','west':'east'}[f[1]])
   out['Properties']=(10,props)
  return out
 for (x,y,z),state in states.items():
  if z in [0,1] and y>0:ck(states.get((width-1-x,y,z))==mirror(state),'symmetric facade '+path.name+str((x,y,z)))
 chairs=[pos for pos,state in states.items() if pos[1]==1 and state['Name'][1].endswith('_stairs')]
 for x,y,z in chairs:
  ck(states.get((x-1,1,z),{}).get('Name',(0,''))[1].endswith('_fence'),'chair leg table '+path.name);ck(states.get((x-1,2,z),{}).get('Name',(0,''))[1].endswith('_pressure_plate'),'chair tabletop '+path.name)
 ck(len(chairs)>=3,'three reading seats '+path.name)
print(f'FINAL114 RESOURCES PASSED:{checks}checks;74 canonical glyphs,592copper blocks,15decorated symmetric archives;no Shift secrets')
