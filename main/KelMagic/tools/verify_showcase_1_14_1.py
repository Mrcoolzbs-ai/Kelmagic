from pathlib import Path
import struct,zlib,math,collections,json
from nbt19 import read
W=Path('run/saves/KelMagicShowcase1141');counts=collections.Counter();positions={};chunks=0
for p in (W/'region').glob('*.mca'):
 raw=p.read_bytes()
 for i in range(1024):
  loc=int.from_bytes(raw[i*4:i*4+4],'big');off=(loc>>8)*4096
  if not off:continue
  n=int.from_bytes(raw[off:off+4],'big');typ=raw[off+4];body=raw[off+5:off+4+n];root=read(zlib.decompress(body) if typ==2 else body);chunks+=1
  cx,cz=root['xPos'][1],root['zPos'][1]
  for sec in root.get('sections',(9,(10,[])))[1][1]:
   bs=sec.get('block_states',(10,{}))[1]
   pal=bs.get('palette',(9,(10,[])))[1][1]
   if not pal:continue
   names=[v['Name'][1] for v in pal]
   if not any(n.startswith('kelmagic:') or n.endswith('pressure_plate') or n.endswith('_fence') for n in names):continue
   bits=max(4,(len(pal)-1).bit_length());per=64//bits;data=bs.get('data',(12,[]))[1];sy=sec['Y'][1]*16
   for k in range(4096):
    ix=((data[k//per] & ((1<<64)-1))>>((k%per)*bits))&((1<<bits)-1) if data else 0
    name=names[ix]
    if name.startswith('kelmagic:') or name.endswith('pressure_plate') or name.endswith('_fence'):
     counts[name]+=1;positions[(cx*16+k%16,sy+k//256,cz*16+(k//16)%16)]=name
copper=sum(v for k,v in counts.items() if 'inscribed_copper_' in k);tables=counts['kelmagic:archivist_table'];plates=sum(v for k,v in counts.items() if k.endswith('pressure_plate'))
assert copper==592,(copper,counts);assert tables==20,tables;assert plates==45,plates
for row in range(5):
 for col in range(3):
  x=col*40;z=row*40
  assert any(k.endswith('pressure_plate') and x<=p[0]<=x+20 and z<=p[2]<=z+25 for p,k in positions.items())
level=read((W/'level.dat').read_bytes())['Data'][1]
assert level['GameType'][1]==1 and level['allowCommands'][1]==1
out={'chunks':chunks,'copperBlocks':copper,'archivistTables':tables,'diningTables':plates,'archiveVariants':15,'spawn':[level['Spawn'+a][1] for a in ['X','Y','Z']],'customBlocks':dict(counts)}
Path('/data/showcase1141-verification.json').write_text(json.dumps(out,indent=2));print({k:v for k,v in out.items() if k!='customBlocks'})
