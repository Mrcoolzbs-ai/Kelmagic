import numpy as np,collections
import audit_mascot_pose_1_23 as q
errors=[]
for name,a in q.an.items():
 if name.endswith('kitten'):continue
 a['name']=name
 for t in np.arange(0,a['animation_length']+.01,.1):
  m=q.matrices(a,t)
  for side in ['left','right']:
   n=side+'_forearm';arm=q.obb(q.bs[n]['cubes'][0],m[n])
   for hair in ['hair_left','hair_right','hair_front_left','hair_front_right','hair_end_left','hair_end_right']:
    for c in q.bs[hair]['cubes']:
     if q.overlaps(arm,q.obb(c,m[hair])):errors.append((name,round(float(t),2),side,hair))
print('Forearm/front hair intersections',len(errors));print(collections.Counter(x[0]for x in errors));print(errors[:20])
