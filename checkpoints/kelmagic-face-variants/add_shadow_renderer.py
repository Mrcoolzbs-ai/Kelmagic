from pathlib import Path
p=Path(__file__).parent/'render_variants.py';s=p.read_text()
s=s.replace(' def tri(q,uv,shade):',''' faces=[]
 lr=np.cross(up,light);lr/=np.linalg.norm(lr);lu=np.cross(light,lr)
 shadow=np.full((1024,1024),-np.inf)
 def shadow_tri(v):
  q=np.c_[512+(v-target)@lr*46,512-(v-target)@lu*46,(v-target)@light]
  x0=max(0,int(q[:,0].min()));x1=min(1023,int(q[:,0].max()+1));y0=max(0,int(q[:,1].min()));y1=min(1023,int(q[:,1].max()+1))
  if x0>x1 or y0>y1:return
  den=(q[1,1]-q[2,1])*(q[0,0]-q[2,0])+(q[2,0]-q[1,0])*(q[0,1]-q[2,1])
  if abs(den)<1e-8:return
  yy,xx=np.mgrid[y0:y1+1,x0:x1+1];xx=xx+.5;yy=yy+.5
  w0=((q[1,1]-q[2,1])*(xx-q[2,0])+(q[2,0]-q[1,0])*(yy-q[2,1]))/den;w1=((q[2,1]-q[0,1])*(xx-q[2,0])+(q[0,0]-q[2,0])*(yy-q[2,1]))/den;w2=1-w0-w1
  zz=w0*q[0,2]+w1*q[1,2]+w2*q[2,2];sl=np.s_[y0:y1+1,x0:x1+1];ok=(w0>=0)&(w1>=0)&(w2>=0)&(zz>shadow[sl]);shadow[sl][ok]=zz[ok]
 def tri(q,uv,normal,v):''')
s=s.replace("depth[sl][ok]=zz[ok];img[sl][ok]=np.clip(colour[:,:,:3][ok]*shade,0,255).astype(np.uint8)",'''world=w0[:,:,None]*v[0]+w1[:,:,None]*v[1]+w2[:,:,None]*v[2]
  sx=np.clip((512+(world-target)@lr*46).astype(int),0,1023);sy=np.clip((512-(world-target)@lu*46).astype(int),0,1023)
  lit=((world-target)@light >= shadow[sy,sx]-.045)
  shade=.68+.30*max(0,normal@light)*lit
  depth[sl][ok]=zz[ok];img[sl][ok]=np.clip(colour[:,:,:3][ok]*shade[ok,None],0,255).astype(np.uint8)''')
s=s.replace('    if normal@toward<=.00001:continue','')
a=s.index('    q=np.c_[size/2+');b=s.index(' return Image.fromarray',a)
s=s[:a]+'''    u,w=t['uv'],t['uv_size'];uv=np.array([[u[0],u[1]],[u[0]+w[0]-1e-5,u[1]],[u[0]+w[0]-1e-5,u[1]+w[1]-1e-5],[u[0],u[1]+w[1]-1e-5]])
    faces.append((v,uv,normal))
 for v,uv,normal in faces:
  if normal@light>0:
   for ids in [(0,1,2),(0,2,3)]:shadow_tri(v[list(ids)])
 for v,uv,normal in faces:
  if normal@toward<=.00001:continue
  q=np.c_[size/2+(v-target)@right*scale,size/2-(v-target)@up*scale,(v-target)@toward]
  for ids in [(0,1,2),(0,2,3)]:tri(q[list(ids)],uv[list(ids)],normal,v[list(ids)])
'''+s[b:]
p.write_text(s)
