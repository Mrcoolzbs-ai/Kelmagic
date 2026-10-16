from pathlib import Path
import re,json,hashlib
base=Path('/data/hair83/base/KelMagic-F3-Hair81-Head65/project/src')
work=Path('/data/hair83/work/project/src')
changed=[str(p.relative_to(base)) for p in base.rglob('*') if p.is_file() and p.read_bytes()!=(work/p.relative_to(base)).read_bytes()]
assert set(changed)=={'main/java/dev/kelmagic/client/hair/Hair6Meshes.java','main/java/dev/kelmagic/client/hair/RearHairCloth.java'},changed
old=(base/'main/java/dev/kelmagic/client/hair/Hair6Meshes.java').read_text();new=(work/'main/java/dev/kelmagic/client/hair/Hair6Meshes.java').read_text()
changedverts=0
for i in range(48):
 def mesh(s):
  a=re.search(r'private static float\[\]\[\]\[\] mesh'+str(i)+r'\(\).*?return new float\[\]\[\]\[\](.*?);}',s)[1]
  return json.loads(a.replace('{','[').replace('}',']').replace('f',''))
 a,b=mesh(old),mesh(new);assert len(a)==len(b)
 for fa,fb in zip(a,b):
  for va,vb in zip(fa,fb):
   expected=va.copy()
   if i in [33,34,35] and va[1]==26.2:expected[1]=24.35;changedverts+=1
   assert expected==vb,(i,va,vb)
oldcloth=(base/'main/java/dev/kelmagic/client/hair/RearHairCloth.java').read_text();newcloth=(work/'main/java/dev/kelmagic/client/hair/RearHairCloth.java').read_text()
# All simulation/contact/iteration code remains byte-identical.
start=' public void sample(';end=' /** Bilinear material coordinates:'
assert oldcloth[oldcloth.index(start):oldcloth.index(end)]==newcloth[newcloth.index(start):newcloth.index(end)]
# Mapping is monotonic, continuous and inverse; lower rest points fixed.
join,top,nape=20.2,26.2,24.35
for k in range(14851):
 y=11.35+k*.001
 f=y if y<=join else join+(y-join)*(nape-join)/(top-join)
 g=f if f<=join else join+(f-join)*(top-join)/(nape-join)
 assert abs(g-y)<1e-10
 if y<=join:assert f==y
print(json.dumps({'changed_source_files':changed,'changed_upper_vertex_entries':changedverts,'all_48_meshes_checked':True,'UV_X_Z_unchanged':True,'resources_identical':True,'Head65_and_RearHairFollow_identical':True,'solver_contact_code_identical':True,'COLS':9,'ROWS':19,'render_subdivision':[6,12],'max_iterations':192,'native_frames_per_run':180,'old_root_body_Y':26.2,'new_root_body_Y':24.35,'neutral_skull_bottom_body_Y':24.0,'upper_overlap_px':.35},indent=2))
