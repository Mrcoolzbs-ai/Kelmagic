"""Static resource regressions for 1.5.1. Optional original src folder verifies preservation."""
from pathlib import Path
import json, re, sys
from PIL import Image, ImageOps
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/kelmagic';checks=0
def check(v,msg):
 global checks
 assert v,msg
 checks+=1
for f in (r/'src/main/resources').rglob('*.json'):
 json.loads(f.read_text());checks+=1
for prefix in ['arcane','mage']:
 for slot in ['helmet','chestplate','leggings','boots']:
  for suffix in ['', '_overlay'] if prefix=='mage' else ['']:
   f=a/f'textures/item/{prefix}_{slot}{suffix}.png';im=Image.open(f).convert('RGBA')
   check(im.size==(32,32),str(f));check(im.tobytes()==ImageOps.mirror(im).tobytes(),'icon symmetric: '+str(f))
s=(r/'src/main/java/dev/kelmagic/client/CodexScreen.java').read_text()
check('codex_recipes' not in s,'recipes tab removed')
check('x+198+col*22' in s,'recipe on right page')
check('y+73+row*22' in s,'recipe below heading')
check('x+291,y+99' in s,'recipe output on right page')
# Compare every displayed pattern and ingredient to the actual recipes.
patterns={'mana_core':['LRL','RDR','LRL'],'magic_ingot':['III','ICI','III'],'magic_cloth':['LSL','SCS','LSL'],'scripter':['IRI','CJC','IRI'],'dragon_heart':['NCA','HES','ACN']}
keys={'mana_core':{'L':'minecraft:lapis_lazuli','R':'minecraft:redstone','D':'minecraft:diamond'},'magic_ingot':{'I':'minecraft:iron_ingot','C':'kelmagic:mana_core'},'magic_cloth':{'L':'minecraft:leather','S':'minecraft:string','C':'kelmagic:mana_core'},'scripter':{'I':'minecraft:iron_ingot','R':'minecraft:redstone_block','C':'kelmagic:mana_core','J':'minecraft:jukebox'},'dragon_heart':{'N':'minecraft:netherite_ingot','C':'minecraft:end_crystal','A':'minecraft:amethyst_block','H':'minecraft:heart_of_the_sea','E':'minecraft:dragon_egg','S':'minecraft:nether_star'}}
for prefix in ['arcane','mage']:
 for slot,pat in {'helmet':['MMM','M M'],'chestplate':['M M','MMM','MMM'],'leggings':['MMM','M M','M M'],'boots':['M M','M M']}.items():
  n=f'{prefix}_{slot}';patterns[n]=pat;keys[n]={'M':'kelmagic:magic_cloth' if prefix=='mage' else 'kelmagic:magic_ingot'}
for n,pat in patterns.items():
 rec=json.loads((r/f'src/main/resources/data/kelmagic/recipe/{n}.json').read_text())
 check(rec['pattern']==pat,'pattern '+n);check({k:v['item'] for k,v in rec['key'].items()}==keys[n],'ingredients '+n)
 check(rec['result']['id']=='kelmagic:'+n,'result '+n)
 check(rec['result']['count']==(8 if n in ['magic_ingot','magic_cloth'] else 1),'count '+n)
rec=json.loads((r/'src/main/resources/data/kelmagic/recipe/archivists_codex.json').read_text())
check(rec['type']=='minecraft:crafting_shapeless','codex shapeless')
check(rec['ingredients'][:3]==[{'item':'kelmagic:mana_core'},{'item':'minecraft:book'},{'item':'minecraft:feather'}],'codex base ingredients')
check(rec['ingredients'][3]==[{'item':'minecraft:ink_sac'},{'item':'minecraft:glow_ink_sac'}],'codex alternative inks')
for lang in ['en_us','ru_ru']:
 obj=json.loads((a/f'lang/{lang}.json').read_text());check('kelmagic.codex_recipes' not in obj,'removed tab localization')
 check(not any(re.search('магия кэл|магии кэл|магией кэл|kel magic',v,re.I) for k,v in obj.items()),'old magic wording removed')
check('kelmagic:magic_fire' in json.loads((r/'src/main/resources/data/minecraft/tags/block/fire.json').read_text())['values'],'portal recognizes magic fire')
check('mirror(name.equals("lapel1") || name.equals("front1"))' in (r/'src/main/java/dev/kelmagic/client/MagicArmorModels.java').read_text(),'mirrored positive-side panels')
check('mirror(name.equals("lapel1") || name.equals("front1"))' in (r/'tools/generate_stage3_assets.py').read_text(),'generator preserves mirror fix')
check('BLOCK_BURN_TICKS=100' in (r/'src/main/java/dev/kelmagic/block/MagicFireBlock.java').read_text(),'fixed five seconds')
if len(sys.argv)>1:
 orig=Path(sys.argv[1]);old=orig/'main/resources/assets/kelmagic'
 for k in [0,1]:
  im=Image.open(a/f'textures/block/magic_fire_{k}.png').convert('RGBA');before=Image.open(old/f'textures/block/magic_fire_{k}.png').convert('RGBA');w=im.width;n=im.height//w
  for i in range(n):check(im.crop((0,i*w,w,(i+1)*w)).tobytes()==before.crop((0,(n-1-i)*w,w,(n-i)*w)).tobytes(),'reverse animation frame')
 check((a/'textures/item/mana_core.png').read_bytes()==(old/'textures/item/mana_core.png').read_bytes(),'mana core unchanged')
 for sub in ['textures/glyph','font']:
  for f in (old/sub).rglob('*'):
   if f.is_file():check(f.read_bytes()==(a/f.relative_to(old)).read_bytes(),'canonical glyph preservation')
 oldfire=(orig/'main/java/dev/kelmagic/server/ArcaneFire.java').read_text();newfire=(r/'src/main/java/dev/kelmagic/server/ArcaneFire.java').read_text()
 check('Math.max(old,Config.FIRE_SECONDS.get()*60)' in oldfire and 'Math.max(old,Config.FIRE_SECONDS.get()*60)' in newfire,'entity duration unchanged')
print(f'ASSET CHECKS PASSED: {checks}')
