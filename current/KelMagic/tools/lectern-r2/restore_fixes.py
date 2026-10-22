from pathlib import Path
p=Path(__file__).resolve().parents[2]
def patch(name,old,new):
 f=p/'src/main/java/dev/kelmagic'/name;s=f.read_text();assert old in s,(name,old);f.write_text(s.replace(old,new))
patch('archive/ArchiveTableBlock.java','c.getHorizontalDirection().getOpposite()','c.getHorizontalDirection()')
patch('MagicStudy.java','.strength(3).noOcclusion()', '.strength(3).lightLevel(s->12).noOcclusion()')
patch('archive/TableOrientation.java','.875+', '.78125+')
patch('client/TableBookView.java','PIXEL=.0025','PIXEL=.0021')
f=p/'src/main/java/dev/kelmagic/client/TableBookRenderer.java';s=f.read_text();s=s.replace('-.225*(1-open)','-.19*(1-open)')
for a,b in [('.455f','.385f'),('.44f','.37f'),('.275f','.231f'),('.263f','.22f'),('-.45f','-.38f'),('-.27f','-.226f'),(',.27f',',.226f')]:s=s.replace(a,b)
s=s.replace('-.52f,.001f,-.28f,.52f,.28f','-(float)(TableBookView.WIDTH*TableBookView.PIXEL/2),.001f,-(float)(TableBookView.HEIGHT*TableBookView.PIXEL/2),(float)(TableBookView.WIDTH*TableBookView.PIXEL/2),(float)(TableBookView.HEIGHT*TableBookView.PIXEL/2)');f.write_text(s)
f=p/'src/main/java/dev/kelmagic/client/CodexScreen.java';s=f.read_text().replace('by=y+18+slot*34','by=y+26+slot*28').replace('x+10,y+18,x+350,y+200','x+20,y+40,x+340,y+188');s=s.replace('mx>x+10&&mx<x+350&&my>y+18&&my<y+200','mx>x+20&&mx<x+340&&my>y+40&&my<y+188').replace('mx<x+10||mx>x+350||my<y+18||my>y+200','mx<x+20||mx>x+340||my<y+40||my>y+188');f.write_text(s)
patch('client/motion/MotionState.java','desiredPitch,-25,35','desiredPitch,-65,45')
patch('client/motion/MotionState.java','=(float)Math.toRadians(headPitch)+','=-(float)Math.toRadians(headPitch)+')
f=p/'src/main/java/dev/kelmagic/entity/RedMage.java';s=f.read_text();s=s.replace('private static final EntityDataAccessor<Integer> SCENE=', 'private static final EntityDataAccessor<Integer> LOOK_TARGET=SynchedEntityData.defineId(RedMage.class,EntityDataSerializers.INT);\n private static final EntityDataAccessor<Integer> SCENE=');s=s.replace('super.defineSynchedData(b);','super.defineSynchedData(b);b.define(LOOK_TARGET,-1);');idx=s.index(' @Override public boolean hurt(');s=s[:idx]+''' /** Presentation-only mirror of the server's existing combat target. Does not choose targets. */
 public LivingEntity visualCombatTarget(){var t=level().getEntity(entityData.get(LOOK_TARGET));return t instanceof LivingEntity l&&l.isAlive()?l:null;}
 @Override public void setTarget(LivingEntity target){super.setTarget(target);if(!level().isClientSide)entityData.set(LOOK_TARGET,target==null?-1:target.getId());}
'''+s[idx:];f.write_text(s)
f=p/'src/main/java/dev/kelmagic/client/motion/MageMotion.java';s=f.read_text().replace('if(gaze.target()!=null','var combat=e.visualCombatTarget();\n  if(combat!=null){var v=combat.getEyePosition(p);double dx=v.x-e.getX(),dz=v.z-e.getZ(),dy=v.y-e.getEyeY();if(dx*dx+dz*dz>1e-8)head=(float)Math.toDegrees(Math.atan2(-dx,dz));pitch=(float)-Math.toDegrees(Math.atan2(dy,Math.hypot(dx,dz)));target=combat.getId();}\n  else if(gaze.target()!=null');s=s.replace('var s=prepare(e,partial);if(s.weight<.0001)return;','var s=prepare(e,partial);boolean combat=e.visualCombatTarget()!=null&&!e.kitten()&&e.throne().isEmpty();if(s.weight<.0001&&!combat)return;');s=s.replace('GeoBone b=model.getBone','if(s.weight<.0001&&i!=MotionState.HEAD)continue;\n   GeoBone b=model.getBone');s=s.replace('i==MotionState.HEAD||','(i==MotionState.HEAD&&!combat)||');s=s.replace('float w=s.weight;','float w=combat&&i==MotionState.HEAD?1:s.weight;');f.write_text(s)
f=p/'src/main/java/dev/kelmagic/client/face/MageGazeStates.java';s=f.read_text().replace('if(book){choose(v,null,Kind.BOOK,t);','if(e.visualCombatTarget()!=null){var a=e.visualCombatTarget();choose(v,a,kind(a),t);wanted=a.getEyePosition();}\n  else if(book){choose(v,null,Kind.BOOK,t);');f.write_text(s)
print('Source fixes applied')
