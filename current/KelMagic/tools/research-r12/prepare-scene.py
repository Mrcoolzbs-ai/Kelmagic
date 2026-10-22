from pathlib import Path
import sys,shutil,re
sys.path.insert(0,'tools');import nbt19
source=Path(sys.argv[1])
n=nbt19.read(source.read_bytes());d=n['Data'][1];d.pop('Player',None);d['LevelName']=(8,'KelMagicR12QA');d['GameType']=(3,1);d['allowCommands']=(1,1)
w=d['WorldGenSettings'][1];w['generate_features']=(1,0);g=w['dimensions'][1]['minecraft:overworld'][1]['generator'][1]
assert g['type'][1]=='minecraft:flat'
s=g['settings'][1];s['structure_overrides']=(9,(8,[]));s['lakes']=(1,0);s['features']=(1,0)
for k,v in [('SpawnX',200),('SpawnY',-60),('SpawnZ',0)]:d[k]=(3,v)
f=Path('run/saves/KelMagicR12QA');shutil.rmtree(f,ignore_errors=True);f.mkdir(parents=True);(f/'level.dat').write_bytes(nbt19.write(n))
p=Path('run/options.txt')
if p.exists():
 s=p.read_text();s=re.sub(r'^lang:.*','lang:ru_ru',s,flags=re.M);s=re.sub(r'^onboardAccessibility:.*','onboardAccessibility:false',s,flags=re.M);p.write_text(s)
