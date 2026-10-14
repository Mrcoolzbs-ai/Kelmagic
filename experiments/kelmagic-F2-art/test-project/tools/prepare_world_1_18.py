from pathlib import Path
import uuid
import nbt19
P=Path(__file__).resolve().parents[1];w=P/'run/saves/KelMagicResearch118';root=nbt19.read((w/'level.dat').read_bytes());d=root['Data'][1];p=d['Player'][1]
assert p['playerGameType'][1]==0 and p['Health'][1]==20
assert p['Pos'][1][1]==[42.,-60.,-12.];assert not p['abilities'][1]['flying'][1]
a=p['neoforge:attachments'][1]['kelmagic:research'][1];assert not a.get('learned',(9,(8,[])))[1][1];assert not a.get('started',(9,(8,[])))[1][1]
items=p['Inventory'][1][1];book=next(i for i in items if i['id'][1]=='kelmagic:archivists_codex');assert len(book['components'][1]['kelmagic:codex_words'][1][1])==15
ids={i['id'][1] for i in items};assert {'kelmagic:mana_core','kelmagic:red_mage_spawn_egg','kelmagic:scripter','minecraft:paper','minecraft:gold_ingot','minecraft:iron_block','minecraft:copper_block','minecraft:oak_log','minecraft:ink_sac','minecraft:smooth_stone','minecraft:amethyst_block'}<=ids
uid=uuid.UUID(bytes=b''.join((i&0xffffffff).to_bytes(4,'big') for i in p['UUID'][1]));wanted=f'{uid}.dat'
d['LevelName']=(8,'KelMagic 1.18.0 — Резонанс');d['GameType']=(3,0);p['foodLevel']=(3,20);p['foodSaturationLevel']=(5,5.0)
(w/'level.dat').write_bytes(nbt19.write(root));(w/'playerdata'/wanted).write_bytes(nbt19.write(p))
for f in (w/'playerdata').glob('*'):
 if f.name!=wanted:f.unlink()
for name in ['stats','advancements']:
 for f in (w/name).glob('*'):f.unlink()
(w/'README-RU.md').write_text((P/'START_HERE.md').read_text())
print('PASS: survival health20/food20, no flight, clean research, 15 test words, spawn42/-60/-12, cores/NPC egg/station supplies, one matching player file, no test stats/advancements')
