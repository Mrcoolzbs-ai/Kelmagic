from pathlib import Path
from PIL import Image
import json,zipfile
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';A=R/'assets/kelmagic';checks=0
def ck(v,label):
 global checks
 assert v,label
 checks+=1
for p in R.rglob('*.json'):json.loads(p.read_text());checks+=1
words=json.loads((R/'data/kelmagic/magic/rarities.json').read_text())['words'];ck(len(words)==180 and words['pir']=='epic','word rarity')
for p in (R/'data/kelmagic/advancement').glob('*.json'):
 d=json.loads(p.read_text());ck(d['display']['show_toast'],'toast');ck(d['display']['announce_to_chat']==(p.stem!='root'),'chat '+p.stem);ck(d.get('requirements')==[['done']],'requirements')
ck(len(list((R/'data/kelmagic/advancement').glob('*.json')))==35,'35 achievements')
ck(Image.open(A/'textures/block/magic_torch.png').size==(16,16),'native torch UV')
ck(Image.open(A/'textures/block/magic_lantern.png').size==(16,80),'native 5frame lantern');ck((A/'textures/block/magic_lantern.png.mcmeta').exists(),'animation')
ck(json.loads((A/'models/block/magic_torch.json').read_text())['parent']=='minecraft:block/template_torch','torch geometry')
ck(json.loads((A/'models/block/magic_wall_torch.json').read_text())['parent']=='minecraft:block/template_torch_wall','wall geometry')
ck(json.loads((A/'blockstates/temporary_end_gate.json').read_text())['variants']['']['model']=='minecraft:block/end_portal','native portal')
for b in ['magic_torch','magic_lantern','magic_wall_torch']:
 ck(not (R/f'data/kelmagic/loot_table/blocks/{b}.json').exists(),'no drop '+b);ck(not (R/f'data/kelmagic/recipe/{b}.json').exists(),'no recipe '+b)
files=list((A/'textures/trims').rglob('*.png'));ck(len(files)==1440,'18x10x8trim textures')
for p in files:
 im=Image.open(p);ck(im.size==(256,256),'UV '+str(p));ck(im.getbbox() is not None,'visible pattern '+str(p))
for prefix in ['mage','arcane']:
 for slot in ['helmet','chestplate','leggings','boots']:
  d=json.loads((A/f'models/item/{prefix}_{slot}.json').read_text());ck(len(d['overrides'])==10,'material-only item mapping')
  for i,v in enumerate(d['overrides']):
   ck(v['predicate']['kelmagic:trimmed']==i+1,'predicate');model=json.loads((A/('models/'+v['model'].split(':')[1]+'.json')).read_text());ck('layer2' in model['textures'],'trim layer')
for biome in ['plains','desert','taiga','snow','savanna','jungle','swamp']:
 p=A/f'textures/entity/villager/profession/archivist_{biome}.png';im=Image.open(p);ck(im.size==(64,64),'villager UV');ck(im.crop((24,0,30,8)).getbbox() is None,'nose transparent');ck(im.crop((30,48,64,64)).getbbox() is None,'hat rim transparent');ck(im.crop((20,44,28,64)).getbbox() is not None,'jacket back');ck(json.loads(Path(str(p)+'.mcmeta').read_text())['villager']['hat']=='full','profession hat prevents biome cap')
ck(json.loads((A/'models/item/archivist_table.json').read_text())['display']['firstperson_righthand']['scale']==[.4]*3,'standard held block scale')
print(f'BASE RESOURCES PASSED: {checks} checks; 1440 equipped pattern/material/slot maps,35achievements, native UV/animation/geometry')
# Mirror contracts: positive-X paired pieces use identical UV nets; model mirrors them.
import re,math
code=(P/'src/main/java/dev/kelmagic/client/MagicArmorModels.java').read_text()
def faces(u,v,w,h,d):return {'top':(u+d,v,u+d+w,v+d),'bottom':(u+d+w,v,u+d+2*w,v+d),'right':(u,v+d,u+d,v+d+h),'front':(u+d,v+d,u+d+w,v+d+h),'left':(u+d+w,v+d,u+2*d+w,v+d+h),'back':(u+2*d+w,v+d,u+2*d+2*w,v+d+h)}
pairs={'right_cheek':'left_cheek','front1':'front-1','rear1':'rear-1','side1':'side-1','rib1':'rib-1','hip_plate1':'hip_plate-1'}
parts={}
for robe,prefix in [('true','mage'),('false','arcane')]:
 section=code.split('if(robe=='+robe+')')[1].split('  if(robe==')[0]
 for slot in ['HEAD','CHEST','LEGS','FEET']:
  region=section.split('if(slot==EquipmentSlot.'+slot+'){')[1].split('   }')[0];entries={}
  for line in region.splitlines():
   m=re.search(r'(piece|leftPiece)\((\w+),"([^"]+)",(\d+),(\d+),(.+)\);',line)
   if not m:continue
   call,bone,name,u,v,values=m.groups();vals=[float(x.strip().replace('f','')) for x in values.split(',')];w,h,d=[math.ceil(x) for x in vals[3:6]];entries[bone,name]=(int(u),int(v),w,h,d)
   if bone in ['left_arm','left_leg']:ck(call=='leftPiece','left limb geometry mirrored')
  parts[prefix,slot.lower()]=entries
for path in files:
 prefix,slot=path.parent.name.split('_');entries=parts[prefix,slot];im=Image.open(path).convert('RGBA')
 for (bone,name),net in entries.items():
  canonical=( {'left_arm':'right_arm','left_leg':'right_leg'}.get(bone,bone),pairs.get(name,name))
  if canonical==(bone,name) or canonical not in entries:continue
  a,b=faces(*net),faces(*entries[canonical]);
  for face,rect in a.items():
   tile=im.crop(rect);other=im.crop(b[face]).resize(tile.size,Image.Resampling.NEAREST);ck(tile.tobytes()==other.tobytes(),str(path)+': paired '+name+'/'+face)
for biome in ['plains','desert','taiga','snow','savanna','jungle','swamp']:
 im=Image.open(A/f'textures/entity/villager/profession/archivist_{biome}.png').convert('RGBA');cloth=im.getpixel((6,60));ck(all(im.getpixel((x,y))==cloth for x in range(7,10) for y in range(57,60)), 'no stray pocket '+biome)
ck((R/'data/kelmagic/worldgen/structure/archive.json').exists(),'registered structure target');ck(json.loads((R/'data/kelmagic/tags/worldgen/structure/archives.json').read_text())['values']==['kelmagic:archive'],'native locate tag')
ck('ClientSpellState.VISUALS.isEmpty()' in (P/'src/main/java/dev/kelmagic/client/GlyphRenderer.java').read_text(),'empty glyph render avoids global buffer flush')
ck('BufferUploader.drawWithShader' in (P/'src/main/java/dev/kelmagic/client/ManaHud.java').read_text(),'HUD submitted as one textured batch')
print(f'BASE112 RESOURCES PASSED: {checks} checks; exact mirror nets,35 achievements,80 inventory trim models,1440 equipped textures')
from nbt19 import read
structs=list((R/'data/kelmagic/structure/archive').glob('*.nbt'));ck(len(structs)==15,'15 archive variants')
for p in structs:
 n=read(p.read_bytes());palette=[{k:v for k,(t,v) in e.items()} for e in n['palette'][1][1]];blocks=n['blocks'][1][1];states=[palette[b['state'][1]] for b in blocks]
 gates=[b for b in blocks if palette[b['state'][1]]['Name']=='minecraft:jigsaw'];ck(len(gates)==1,'one entrance '+p.name)
 final=gates[0]['nbt'][1]['final_state'][1];ck(not final.endswith('stairs') and '[' not in final,'solid threshold '+p.name)
 ck(any(s['Name']=='kelmagic:archivist_table' for s in states),'table '+p.name);ck(any(s['Name']=='minecraft:chest' for s in states),'chest '+p.name);ck(sum(s['Name']=='minecraft:bookshelf' for s in states)>=20,'shelves '+p.name)
 if not p.name.startswith('desert'):
  ck(sum(s['Name'].endswith('_stairs') and b['pos'][1][1][1]>=5 for b,s in zip(blocks,states))>40,'stair roof '+p.name)
for biome in ['plains','desert','taiga','snow','savanna','jungle','swamp']:
 im=Image.open(A/f'textures/entity/villager/profession/archivist_{biome}.png');ck(im.crop((48,22,52,26)).getbbox()==(0,0,4,4),'opaque shoulder tops '+biome)
source=(P/'src/main/java/dev/kelmagic/worldgen/ArchiveSearch.java').read_text();ck('.join(' not in source and '.get(' not in source.replace('CACHE.get(','CACHE.lookup('),'no blocking future waits');ck('getNow' in source and 'isDone' in source,'poll completed futures');ck('2_000_000' in source and 'JOBS.size()>=2' in source,'budget and global limit')
for biome in ['plains','desert','taiga','snowy','savanna']:
 pool=json.loads((R/f'data/minecraft/worldgen/template_pool/village/{biome}/houses.json').read_text());ck(sum(e['element'].get('location','').startswith('kelmagic:archive/') for e in pool['elements'])==3,'3 pool variants '+biome)
print(f'RESOURCES PASSED: {checks} checks; 15 archives,35 achievements,opaque shoulder tops,nonblocking search')
