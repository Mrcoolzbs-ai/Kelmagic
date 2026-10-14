from pathlib import Path
from collections import deque
exec(compile(Path('tools/verify_revision_1_14.py').read_text(),'tools/verify_revision_1_14.py','exec'))
for path in (R/'data/kelmagic/structure/archive').glob('*.nbt'):
 n=read(path.read_bytes());pal=n['palette'][1][1];states={tuple(b['pos'][1][1]):pal[b['state'][1]] for b in n['blocks'][1][1]};width,_,depth=n['size'][1][1];w=width-2;d=depth-2
 for (x,y,z),state in states.items():
  if state['Name'][1].endswith('_trapdoor'):
   face=state['Properties'][1]['facing'][1];ck(face==('west' if x==0 else 'east') and x in [0,width-1],'wall-adjacent shutter '+path.name)
 def free(x,z):
  return 2<=x<=w-1 and 2<=z<=d-1 and all(states.get((x,y,z),{}).get('Name',(0,'minecraft:air'))[1] in ['minecraft:air','minecraft:oak_door','minecraft:spruce_door','minecraft:acacia_door'] for y in [1,2])
 start=(w//2+1,2);seen={start};q=deque([start])
 while q:
  x,z=q.popleft()
  for at in [(x-1,z),(x+1,z),(x,z-1),(x,z+1)]:
   if at not in seen and free(*at):seen.add(at);q.append(at)
 for (x,y,z),state in states.items():
  if state['Name'][1] in ['kelmagic:archivist_table','minecraft:chest']:
   ck(y==1 and x in [3,w-1],'workstation at side alcove '+path.name)
   ck(any(v in seen for v in [(x-1,z),(x+1,z),(x,z-1),(x,z+1)]),'walkable workstation access '+path.name)
   ck(states[(x,2,z)]['Name'][1]=='minecraft:air','clear space above workstation '+path.name)
 ck((w//2+1,d-2) in seen or (w//2,d-2) in seen,'continuous main aisle '+path.name)
print(f'FINAL1141 RESOURCES PASSED:{checks} checks; all15 shutters/niches/clear lids/walk routes')
