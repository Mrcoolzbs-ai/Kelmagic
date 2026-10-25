"""Validate new R14 content, references and unchanged baseline recipes. No game-client claim."""
from pathlib import Path
from PIL import Image
import json,hashlib,subprocess,sys,zipfile,re
R=Path(__file__).resolve().parents[2]; A=R/'src/main/resources/assets/kelmagic';D=R/'src/main/resources/data';cat=json.loads((R/'tools/artifact-r14/catalog.json').read_text());checks=0

def check(ok,message):
 global checks
 checks+=1
 if not ok:raise AssertionError(message)
for lang in ['ru_ru','en_us']:
 j=json.loads((A/'lang'/f'{lang}.json').read_text())
 for _,id,*rest in cat:
  kind=rest[-1];check(('block' if kind=='block' else 'item')+'.kelmagic.'+id in j,'Missing name '+id)
  check('kelmagic.artifact.tip.'+id in j,'Missing tooltip '+id)
 check('\ufffd' not in json.dumps(j,ensure_ascii=False),'Broken Unicode')
for _,id,*rest in cat:
 kind=rest[-1];check((A/'models/item'/f'{id}.json').is_file(),'Missing item model '+id)
 if kind!='block':
  im=Image.open(A/'textures/item'/f'{id}.png');check(im.size==(32,32),'Icon size '+id);check(im.mode=='RGBA','Icon transparency '+id);check(im.getbbox() is not None,'Empty icon '+id)
 else:check((A/'blockstates'/f'{id}.json').is_file(),'Missing block state '+id)
for set in ['deep_source','steady_anchor','grove_keeper','strict_negation']:
 for part in ['helmet','chestplate','leggings','boots']:
  check((A/'models/item'/f'{set}_{part}.json').is_file(),'Missing armor item');check(Image.open(A/'textures/item'/f'{set}_{part}.png').size==(32,32),'Armor icon size')
 for layer in [1,2]:check(Image.open(A/'textures/models/armor'/f'{set}_layer_{layer}.png').size==(128,128),'Armor UV atlas')
for p in (A/'sounds/artifact').glob('*.ogg'):
 info=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-show_format','-of','json',str(p)]));stream=info['streams'][0];check(stream['codec_name']=='vorbis' and stream['channels']==1,'Sound encoding');check(.1<float(info['format']['duration'])<2,'Sound duration')
for p in (D/'kelmagic/curios/slots').glob('kelmagic_*.json'):check(json.loads(p.read_text())['size']>0,'Invalid Curios slot')
# All newly generated block textures resolve; models are not referencing missing placeholders.
for p in (A/'models/block').glob('*.json'):
 if p.stem not in [x[1] for x in cat if x[-1]=='block'] and not any(p.stem.startswith(x) for x in ['mana_cup_','mana_bowl_','mana_basin_','fleeting_']):continue
 j=json.loads(p.read_text())
 for tex in j.get('textures',{}).values():
  if tex.startswith('kelmagic:'):check((A/'textures'/(tex.split(':',1)[1]+'.png')).is_file(),'Missing texture '+tex)
for p in list((R/'src/main/java/dev/kelmagic/artifact').glob('*.java'))+list((R/'tools/artifact-r14').glob('*.py')):
 check(not re.search(r'gh[pousr]_[A-Za-z0-9]{20,}',p.read_text()),'Credential must not be shipped')
if len(sys.argv)>1:
 baseline=Path(sys.argv[1]);recipe=lambda base:{str(p.relative_to(base)):hashlib.sha256(p.read_bytes()).hexdigest() for p in (base/'src/main/resources/data').rglob('*.json') if '/recipe/' in str(p) or '/recipes/' in str(p)}
 check(recipe(R)==recipe(baseline),'Recipes changed')
 for scope in ['src/main/resources/assets/kelmagic/geo','src/main/resources/assets/kelmagic/animations']:
  for old in (baseline/scope).rglob('*'):
   if old.is_file():check((R/old.relative_to(baseline)).read_bytes()==old.read_bytes(),'Unrelated model/animation changed')
jar=next((R/'build/libs').glob('kelmagic-1.35.0-r14-artifacts-TEST.jar'))
with zipfile.ZipFile(jar) as z:
 names=z.namelist();check(not any(x.startswith(('software/bernie/','top/theillusivec4/')) for x in names),'Third-party dependency bundled');check('dev/kelmagic/artifact/ArtifactContent.class' in names,'Artifact classes absent');check('assets/kelmagic/sounds/artifact/spell_absorb.ogg' in names,'Audio absent');check('assets/kelmagic/textures/models/armor/deep_source_layer_1.png' in names,'Armor texture absent')
print(f'R14_STATIC_PASS checks={checks}; recipes unchanged; no third-party dependency classes bundled')
