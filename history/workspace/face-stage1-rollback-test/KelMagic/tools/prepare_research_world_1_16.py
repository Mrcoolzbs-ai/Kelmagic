from pathlib import Path
import uuid,json
import nbt19
P=Path(__file__).resolve().parents[1];w=P/'run/saves/KelMagicResearch116';root=nbt19.read((w/'level.dat').read_bytes());d=root['Data'][1];d['GameType']=(3,0);d['LevelName']=(8,'KelMagic 1.16.0 — Листы, которым есть что сказать')
for k,v in [('SpawnX',42),('SpawnY',-60),('SpawnZ',-12)]:d[k]=(3,v)
p=d['Player'][1];assert p['playerGameType'][1]==0;assert p['Pos'][1][1]==[42.,-60.,-12.];assert p['Health'][1]==20
assert not p['abilities'][1]['flying'][1]
a=p['neoforge:attachments'][1]['kelmagic:research'][1];assert not a.get('learned',(9,(8,[])))[1][1];assert not a.get('started',(9,(8,[])))[1][1]
items=p['Inventory'][1][1];book=next(i for i in items if i['id'][1]=='kelmagic:archivists_codex');words=book['components'][1]['kelmagic:codex_words'][1][1];assert len(set(words))==23;assert {'mina','pir','no'}<=set(words)
ids={i['id'][1] for i in items};assert {'minecraft:string','minecraft:glass_bottle','minecraft:paper','kelmagic:ancient_manuscript','kelmagic:scripter'}<=ids
canonical={s['word'] for s in json.loads((P/'src/main/resources/data/kelmagic/magic/words.json').read_text())['words']};assert set(words)<=canonical
(w/'level.dat').write_bytes(nbt19.write(root));(w/'README-RU.md').write_text((P/'START_HERE.md').read_text())
uid=uuid.UUID(bytes=b''.join((i&0xffffffff).to_bytes(4,'big') for i in p['UUID'][1]));wanted=f'{uid}.dat'
for f in (w/'playerdata').glob('*'):
 if f.name!=wanted:f.unlink()
assert (w/'playerdata'/wanted).exists()
for dirname in ['advancements','stats']:
 for f in (w/dirname).glob('*'):f.unlink()
print('PASS: clean survival world/player, health20, flight off, spawn42/-60/-12, empty research, 23 valid codex words, starter paper/materials/manuscript/found scripter, matching player file; prior player logs removed.')
