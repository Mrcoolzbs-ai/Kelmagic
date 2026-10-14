import struct,gzip,io
# Small typed NBT codec for reproducible vanilla structure assets. Numeric types preserved.
def read(data):
 f=io.BytesIO(gzip.decompress(data) if data[:2]==b'\x1f\x8b' else data)
 def num(fmt):return struct.unpack('>'+fmt,f.read(struct.calcsize(fmt)))[0]
 def string():return f.read(num('H')).decode()
 def tag(t):
  if t in [1,2,3,4,5,6]:return num({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[t])
  if t==8:return string()
  if t==9:q=num('B');return (q,[tag(q) for _ in range(num('i'))])
  if t==10:
   d={}
   while (q:=num('B')):n=string();d[n]=(q,tag(q))
   return d
  if t in [7,11,12]:return [num({7:'b',11:'i',12:'q'}[t]) for _ in range(num('i'))]
  raise ValueError(t)
 t=num('B');name=string();return tag(t)
def write(root):
 def string(s):b=s.encode();return struct.pack('>H',len(b))+b
 def tag(t,v):
  if t in [1,2,3,4,5,6]:return struct.pack('>'+{1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[t],v)
  if t in [7,11,12]:return struct.pack('>i',len(v))+b''.join(struct.pack('>'+{7:'b',11:'i',12:'q'}[t],x) for x in v)
  if t==8:return string(v)
  if t==9:q,ls=v;return bytes([q])+struct.pack('>i',len(ls))+b''.join(tag(q,x) for x in ls)
  if t==10:return b''.join(bytes([q])+string(n)+tag(q,x) for n,(q,x) in v.items())+b'\0'
  raise ValueError(t)
 return gzip.compress(b'\x0a\0\0'+tag(10,root),mtime=0)
