package dev.kelmagic.client.hair;
import java.util.*;import org.joml.Matrix4f;import org.joml.Vector3f;
/** Hair v8.4: bounded 2-D XPBD cloth in body-local model pixels.
 * Pinned scalp row, structural/shear and compliant bend distances, damped Verlet,
 * rounded oriented body/arm contacts and non-local self-separation. No bone scaling. */
public final class RearHairCloth {
 public static final int COLS=9,ROWS=19;private static final float WIDTH=8.4f,TOP=26.2f,LENGTH=14.85f,Z=4.2f;
 private static final float CONTACT=.65f,SELF=.35f;
 private static final float GRAVITY=650,STRETCH=1.035f;
 // Topology-only exclusions are invariant for the fixed grid. Preserve a/b order.
 private static final int[] SELF_PAIRS=selfPairs();
 private static int[] selfPairs(){
  int[] pairs=new int[(COLS*ROWS-COLS)*(COLS*ROWS-COLS-1)];int n=0;
  for(int a=COLS;a<COLS*ROWS;a++)for(int b=a+1;b<COLS*ROWS;b++){
   if(Math.abs(a/COLS-b/COLS)<=2&&Math.abs(a%COLS-b%COLS)<=2)continue;
   pairs[n++]=a;pairs[n++]=b;
  }
  return Arrays.copyOf(pairs,n);
 }
 private final Matrix4f previousBodyFrame=new Matrix4f();
 // Render-thread scratch storage, no state/solver policy change.
 private final Vector3f solverDelta=new Vector3f(),contactBefore=new Vector3f(),contactCorrection=new Vector3f();
 private final Vector3f[] pos=new Vector3f[COLS*ROWS],prev=new Vector3f[pos.length],oldPins=new Vector3f[COLS];
 private final List<Link> links=new ArrayList<>();private double time=Double.NaN;
 public int bodyContacts,armContacts,selfContacts;public float maxStrain;public int steps;
 private static final class Link {int a,b;float length,compliance,lambda;Link(int a,int b,float l,float c){this.a=a;this.b=b;length=l;compliance=c;}}
 public static final class Collider {
  final Matrix4f toBody,local;final Vector3f low,high;final boolean arm;
  private final Vector3f pointScratch=new Vector3f(),nearScratch=new Vector3f(),deltaScratch=new Vector3f();
  public Collider(Matrix4f matrix,Vector3f lo,Vector3f hi,boolean arm){toBody=new Matrix4f(matrix);local=new Matrix4f(matrix).invert();low=lo;high=hi;this.arm=arm;}
  public boolean project(Vector3f p,float radius){
   Vector3f q=local.transformPosition(pointScratch.set(p).div(16)).mul(16);
   Vector3f near=nearScratch.set(Math.clamp(q.x,low.x,high.x),Math.clamp(q.y,low.y,high.y),Math.clamp(q.z,low.z,high.z));
   Vector3f delta=deltaScratch.set(q).sub(near);float length=delta.length();
   if(length>=radius)return false;
   if(length>1e-6f)q.set(near).fma(radius/length,delta);
   else {float best=Float.POSITIVE_INFINITY;int axis=0;boolean positive=false;for(int a=0;a<3;a++){float d=q.get(a)-low.get(a);if(d<best){best=d;axis=a;positive=false;}d=high.get(a)-q.get(a);if(d<best){best=d;axis=a;positive=true;}}q.setComponent(axis,(positive?high.get(axis)+radius:low.get(axis)-radius));}
   p.set(toBody.transformPosition(q.div(16))).mul(16);return true;
  }
 }
 public RearHairCloth(){
  for(int r=0;r<ROWS;r++)for(int c=0;c<COLS;c++){int i=id(c,r);pos[i]=rest(c,r);prev[i]=new Vector3f(pos[i]);if(r==0)oldPins[c]=new Vector3f(pos[i]);}
  for(int r=0;r<ROWS;r++)for(int c=0;c<COLS;c++){
   if(c+1<COLS)add(c,r,c+1,r,.0000003f);if(r+1<ROWS)add(c,r,c,r+1,.0000015f);
   if(c+1<COLS&&r+1<ROWS){add(c,r,c+1,r+1,.000012f);add(c+1,r,c,r+1,.000012f);}
   if(c+2<COLS)add(c,r,c+2,r,.00035f);if(r+2<ROWS)add(c,r,c,r+2,.00035f);
  }
 }
 private static int id(int c,int r){return r*COLS+c;}
 // v8.3 source-rest correction only: lower the scalp row to the nape.
 // Preserve every rest point at/below the mid-sheet join; do not translate the drape.
 private static final float NAPE_TOP=24.35f,ROOT_JOIN=20.2f;
 private static float napeY(float y){return y<=ROOT_JOIN?y:ROOT_JOIN+(y-ROOT_JOIN)*(NAPE_TOP-ROOT_JOIN)/(TOP-ROOT_JOIN);}
 private static float materialY(float y){return y<=ROOT_JOIN?y:ROOT_JOIN+(y-ROOT_JOIN)*(TOP-ROOT_JOIN)/(NAPE_TOP-ROOT_JOIN);}
 private static Vector3f rest(int c,int r){return new Vector3f(-WIDTH/2+WIDTH*c/(COLS-1),napeY(TOP-LENGTH*r/(ROWS-1)),Z);}
 private void add(int c,int r,int c2,int r2,float compliance){int a=id(c,r),b=id(c2,r2);links.add(new Link(a,b,pos[a].distance(pos[b]),compliance));}
 /** No cached pose/contact data: only the original no-advance decision. Reset always wins. */
 public boolean needsSample(double now,boolean reset){double dt=now-time;return reset||!Double.isFinite(time)||dt<0||!(dt<1e-6);}
 public void sample(double now,Matrix4f head,List<Collider> colliders,boolean reset,Vector3f bodyDisplacement,Matrix4f bodyFrame){
  if(!needsSample(now,reset))return;
  Vector3f[] pins=new Vector3f[COLS];for(int c=0;c<COLS;c++)pins[c]=head.transformPosition(rest(c,0).div(16)).mul(16);
  double dt=now-time;
  if(reset||!Double.isFinite(time)||dt<0||dt>.25){for(int r=0;r<ROWS;r++)for(int c=0;c<COLS;c++){pos[id(c,r)].set(rest(c,r)).add(new Vector3f(pins[c]).sub(rest(c,0)));prev[id(c,r)].set(pos[id(c,r)]);}for(int c=0;c<COLS;c++)oldPins[c].set(pins[c]);previousBodyFrame.set(bodyFrame);time=now;return;}
  // Keep free particles in the same WORLD frame when the animated body frame moves.
  // Roots alone inherit the current head. No head rotation is imposed on free lengths.
  var worldToBody=new Matrix4f(bodyFrame).invert();
  var change=new Matrix4f(worldToBody).translate(-bodyDisplacement.x,-bodyDisplacement.y,-bodyDisplacement.z).mul(previousBodyFrame);
  for(int i=0;i<pos.length;i++){change.transformPosition(pos[i].div(16)).mul(16);change.transformPosition(prev[i].div(16)).mul(16);}
  for(var p:oldPins)change.transformPosition(p.div(16)).mul(16);
  var gravity=worldToBody.transformDirection(new Vector3f(0,-GRAVITY,0));
  int sub=Math.max(1,(int)Math.ceil(dt*180));float h=(float)(dt/sub),decay=(float)Math.exp(-11*h);bodyContacts=armContacts=selfContacts=0;maxStrain=0;
  for(int step=0;step<sub;step++){
   float phase=(step+1f)/sub;
   for(int r=1;r<ROWS;r++)for(int c=0;c<COLS;c++){
    int i=id(c,r);var p=pos[i];var v=new Vector3f(p).sub(prev[i]).mul(decay);float limit=70*h;if(v.length()>limit)v.normalize(limit);prev[i].set(p);p.add(v).fma(h*h,gravity);
   }
   for(var link:links)link.lambda=0;
   for(int iteration=0;iteration<64;iteration++){
    pin(pins,phase);
    if((iteration&1)==0){for(Link l:links)solve(l,h);}else{for(int k=links.size()-1;k>=0;k--)solve(links.get(k),h);}
    contacts(colliders);
    if(iteration%8==7)selfSeparate();
    if(iteration>=31&&iteration%8==7&&strain()<.008f)break;
   }
   pin(pins,phase);contacts(colliders);steps++;
  }
  for(Link l:links)if(l.compliance<.00001f)maxStrain=Math.max(maxStrain,Math.abs(pos[l.a].distance(pos[l.b])/l.length-1));
  for(int c=0;c<COLS;c++)oldPins[c].set(pins[c]);previousBodyFrame.set(bodyFrame);time=now;
 }
 private float strain(){float m=0;for(Link l:links)if(l.compliance<.00001f)m=Math.max(m,Math.abs(pos[l.a].distance(pos[l.b])/l.length-1));return m;}
 private void pin(Vector3f[] pins,float t){for(int c=0;c<COLS;c++){pos[c].set(oldPins[c]).lerp(pins[c],t);prev[c].set(pos[c]);}}
 private void solve(Link l,float h){float wa=l.a<COLS?0:1,wb=l.b<COLS?0:1;if(wa+wb==0)return;var d=solverDelta.set(pos[l.b]).sub(pos[l.a]);float length=d.length();if(length<1e-8f)return;
  // True timestep-scaled compliance: bend/shear may yield instead of locking a plate.
  float alpha=l.compliance/(h*h),dl=(-(length-l.length)-alpha*l.lambda)/(wa+wb+alpha);l.lambda+=dl;
  d.mul(dl/length);pos[l.a].fma(-wa,d);pos[l.b].fma(wb,d);
  // Separate safety envelope for short edges, not rigid enforcement of every diagonal.
  if(l.compliance<.00001f){d.set(pos[l.b]).sub(pos[l.a]);length=d.length();float target=Math.clamp(length,l.length*.94f,l.length*STRETCH);
   if(length>1e-8f&&target!=length){d.mul((length-target)/(length*(wa+wb)));pos[l.a].fma(wa,d);pos[l.b].fma(-wb,d);}}
 }
 private void contacts(List<Collider> colliders){for(int i=COLS;i<pos.length;i++)for(var collider:colliders){var before=contactBefore.set(pos[i]);
  // The pinned row sits only .35 px above the body top. Grow thickness over three
  // rows instead of making its first free edge satisfy an impossible .65 px gap.
  float radius=CONTACT*Math.min(1,(i/COLS)/3f);
  if(collider.project(pos[i],radius)){if(collider.arm)armContacts++;else bodyContacts++;var correction=contactCorrection.set(pos[i]).sub(before);prev[i].fma(.85f,correction);}}}
 private void selfSeparate(){
  for(int k=0;k<SELF_PAIRS.length;k+=2){int a=SELF_PAIRS[k],b=SELF_PAIRS[k+1];
   var d=new Vector3f(pos[b]).sub(pos[a]);float sq=d.lengthSquared();if(sq>=SELF*SELF||sq<1e-10f)continue;float len=(float)Math.sqrt(sq);d.mul((SELF-len)*.5f/len);pos[a].sub(d);pos[b].add(d);selfContacts++;
  }
 }
 /** Bilinear material coordinates: one shared 2-D field, not one rotating strip per bone.
  * Preserve every original point, width taper, layer spacing and UV in the neutral rest. */
 public void deform(Vector3f p){
  float x=p.x*16,y=materialY(p.y*16),z=p.z*16;
  float u=Math.clamp((x+WIDTH/2)/WIDTH*(COLS-1),0,COLS-1),v=Math.clamp((TOP-y)/LENGTH*(ROWS-1),0,ROWS-1);
  int c=Math.min(COLS-2,(int)u),r=Math.min(ROWS-2,(int)v);float a=u-c,b=v-r;
  var p00=pos[id(c,r)];var p10=pos[id(c+1,r)];var p01=pos[id(c,r+1)];var p11=pos[id(c+1,r+1)];
  var centre=new Vector3f(p00).lerp(p10,a).lerp(new Vector3f(p01).lerp(p11,a),b);
  var across=new Vector3f(p10).sub(p00).lerp(new Vector3f(p11).sub(p01),b);
  var down=new Vector3f(p01).sub(p00).lerp(new Vector3f(p11).sub(p10),a);
  var normal=down.cross(across).normalize();if(!Float.isFinite(normal.lengthSquared()))normal.set(0,0,1);
  centre.fma(z-Z,normal);p.set(centre).div(16);
 }
 public float widthMobility(){var a=pos[id(0,ROWS/2)];var b=pos[id(COLS-1,ROWS/2)];return a.y-b.y;}
}
