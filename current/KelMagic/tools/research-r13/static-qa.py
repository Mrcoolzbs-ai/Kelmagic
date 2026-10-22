#!/usr/bin/env python3
from pathlib import Path
import json,zipfile,re,hashlib
R=Path(__file__).resolve().parents[2]; checks=0
def ok(v,msg):
 global checks
 if not v: raise AssertionError(msg)
 checks+=1
# all JSON resources parse
for p in R.joinpath('src/main/resources').rglob('*.json'):
 json.loads(p.read_text());checks+=1
cat=json.loads((R/'src/main/resources/data/kelmagic/research/catalog.json').read_text());nodes=cat['nodes'];ids={n['id'] for n in nodes}
ok('copper_inscriptions' not in ids,'copper-writing node removed')
ok(all(n['tab']!='legacy_writing' for n in nodes),'copper-writing tab absent')
for n in nodes:
 for parent in n.get('parents',[]):ok(parent in ids,f'parent {parent}')
by={n['id']:n for n in nodes}
ok(by['midnight_wood']['parents']==['magic_sapling'],'wood appears after sapling')
ok(by['magic_sapling']['tab']=='infusion','sapling under infusion')
ok(by['resonance_glass']['tab']=='infusion','glass under infusion')
# explicit recipes
required=['ember_pickaxe','vein_pickaxe','large_field_hoe','surveyor_shovel','gardener_sickle','return_stone','seeker_lantern','gardener_boots','builder_gloves','collector_ring','last_breath_pendant','fading_heat_cloak','guardian_brooch','echo_receiver','echo_isolator','musical_resonator','archivist_helmet','archivist_chestplate','archivist_leggings','archivist_boots']
for n in required:
 ok((R/f'src/main/resources/data/kelmagic/recipe/{n}.json').is_file(),f'{n} recipe')
 ok((R/f'src/main/resources/assets/kelmagic/models/item/{n}.json').is_file(),f'{n} model')
for n in required[:13]:ok((R/f'src/main/resources/assets/kelmagic/textures/item/{n}.png').is_file(),f'{n} texture')
sap=json.loads((R/'src/main/resources/data/kelmagic/recipe/magic_sapling.json').read_text());ok(sap['pattern']==['SSS','SCS','SSS'] and sap['result']['count']==1,'8 saplings + core -> 1')
glass=json.loads((R/'src/main/resources/data/kelmagic/recipe/resonance_glass.json').read_text());ok(glass['pattern']==['GGG','GCG','GGG'],'8 glass + core')
# source invariants
src=(R/'src/main/java/dev/kelmagic/server/GroveAwakening.java').read_text();
for marker in ['CLEANUP_PAD=8','containsCleanup','fallbackRoots','prepareTreeSite','MANGROVE_ROOTS','NATURAL_TREE.get().place']:ok(marker in src,'grove '+marker)
grass=(R/'src/main/java/dev/kelmagic/content/MagicGrassBlock.java').read_text();dream=(R/'src/main/java/dev/kelmagic/content/MagicDreams.java').read_text();renderer=(R/'src/main/java/dev/kelmagic/client/MagicGrassRenderer.java').read_text();waves=(R/'src/main/java/dev/kelmagic/core/GrassWaves.java').read_text()
ok('implements EntityBlock' not in grass and 'GRASS_ENTITY' not in dream,'no turf block entities')
ok('AFTER_CUTOUT_BLOCKS' in renderer and 'List<BlockPos> grass' in renderer,'batched turf renderer')
ok('x+z' not in waves and 'time/2000' in waves,'server-wide synchronized base tide')
ok('interference(x,y,z,time,sources)' in waves,'mana waves retained')
ok('18+146*wave' in waves and '66+158*wave' in waves,'violet endpoint darkened')
content=(R/'src/main/java/dev/kelmagic/MagicApprovedContent.java').read_text()
for marker in ['COOLDOWN=300','inflate(10)','seen.size()<32','FarmlandTrampleEvent','BLOCK_INTERACTION_RANGE','FIRE_RESISTANCE','AMETHYST_BLOCK_RESONATE']:ok(marker in (content+(R/'src/main/java/dev/kelmagic/item/SeekerLanternItem.java').read_text()),marker)
codex=(R/'src/main/java/dev/kelmagic/client/CodexScreen.java').read_text();ok('"Слова Кэл","Основы"' in codex,'tab renamed to Basics')
ok('index!=5' in codex and 'new int[]{0,1,2,3,4,6,8}' in codex,'copper bookmark omitted')
ok('return creativeResearch||' in codex,'creative codex opens all hypotheses')
# exact protected grammar and mascot Java compared with R12 transfer
archive=Path('/data/.agent-service/files/399e44fe-4938-4d25-b8ec-ed8fa8d0cd21/KelMagic_R12_Roots_TRANSFER.zip')
with zipfile.ZipFile(archive) as z:
 for rel in ['src/main/java/dev/kelmagic/core/KelLexicon.java','src/main/java/dev/kelmagic/core/SpellParser.java','src/main/java/dev/kelmagic/client/MageRendering.java','src/main/java/dev/kelmagic/client/MageModel.java']:
  cur=R/rel
  if cur.exists():ok(cur.read_bytes()==z.read('KelMagic_R12_Roots_TRANSFER/project/'+rel),f'protected unchanged {rel}')
print(f'R13 STATIC QA PASSED: {checks} checks')
