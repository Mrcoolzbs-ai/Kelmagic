package dev.kelmagic.client.face;
import software.bernie.geckolib.cache.object.*;import software.bernie.geckolib.util.RenderUtil;
import net.minecraft.core.Direction;import org.joml.Vector3f;import org.joml.Matrix4f;import java.util.*;import com.mojang.blaze3d.vertex.PoseStack;
/** Eye-only render tessellation: rounded central dark patch using the ORIGINAL atlas UVs.
 * Expanded iris motion is clipped in head space to the existing stepped socket and live lids.
 * No PNG editing, eye scaling, socket/head changes, or accumulating deformation. */
public final class PupilIrisRig {
 private static final Map<GeoBone,List<GeoCube>> REST=new WeakHashMap<>();
 public static void apply(GeoBone b,float size){apply(b,size,0,0,0,0);}
 public static void apply(GeoBone b,float size,float dx,float dy,float mask,float lower){
  Work work=new Work();
  var rest=REST.computeIfAbsent(b,x->List.copyOf(b.getCubes()));var fresh=new ArrayList<GeoCube>();boolean left=b.getName().endsWith("left");
  for(var c:rest){var out=new ArrayList<GeoQuad>();var stack=new PoseStack();RenderUtil.translateToPivotPoint(stack,c);RenderUtil.rotateMatrixAroundCube(stack,c);RenderUtil.translateAwayFromPivotPoint(stack,c);
   Matrix4f toHead=new Matrix4f(stack.last().pose()),inverse=new Matrix4f(toHead).invert();
   for(var q:c.quads())if(q!=null){
    float u0=Float.POSITIVE_INFINITY,u1=Float.NEGATIVE_INFINITY,v0=u0,v1=u1;
    for(var a:q.vertices()){u0=Math.min(u0,a.texU());u1=Math.max(u1,a.texU());v0=Math.min(v0,a.texV());v1=Math.max(v1,a.texV());}
    if(u1-u0<1e-8||v1-v0<1e-8)continue;
    var corners=new Vector3f[2][2];for(var a:q.vertices())corners[a.texU()>(u0+u1)/2?1:0][a.texV()>(v0+v1)/2?1:0]=a.position();
    if(q.direction()!=Direction.NORTH)continue; // iris depth is unchanged; only the front optical surface is visible
    // The iris keeps its full expressive travel. Only its dark centre is seated inside
    // the currently visible iris/lid intersection when enough coloured area remains.
    var topH=toHead.transformPosition(new Vector3f(corners[0][0]).lerp(corners[1][0],.5f));
    var bottomH=toHead.transformPosition(new Vector3f(corners[0][1]).lerp(corners[1][1],.5f));
    float y0=topH.y*16+dy,y1=bottomH.y*16+dy;
    float visibleLow=Math.max(Math.min(y0,y1),27.35f+.22f*Math.clamp(lower,0,1));
    float visibleHigh=Math.min(Math.max(y0,y1),29.3f-1.95f*Math.clamp(mask,0,1));
    visibleHigh=Math.min(visibleHigh,29.05f);
    float half=Math.abs(y1-y0)*.20f*size;
    var seat=PupilVisibility.seat((y0+y1)*.5f,half,visibleLow,visibleHigh);
    float pupilV=Math.clamp(.5f+(seat.centre()-(y0+y1)*.5f)/(y1-y0),.20f*size,1-.20f*size);
    for(int row=0;row<20;row++)for(int col=0;col<20;col++){
     boolean dark=col>=8&&col<12&&row>=4&&row<16;
     boolean hideDark=dark&&!seat.visible();
     Buffer polygon=work.polygon;polygon.count=0;
     for(var a:q.vertices()){
      int ix=a.texU()>(u0+u1)/2?1:0,iy=a.texV()>(v0+v1)/2?1:0;float u=(col+ix)/20f,v=(row+iy)/20f;
      float dv=map(v,.2f,.8f,pupilV-.20f*size,pupilV+.20f*size);
      float edge=Math.abs((v-.5f)/.3f);float round=.45f;
      // Smooth polygonal corner cuts with exact shared row boundaries.
      if(edge<1)round=Math.max(.45f,(float)Math.sqrt(Math.max(0,1-edge*edge)));
      float du=map(u,.4f,.6f,.5f-.20f*size*round,.5f+.20f*size*round);
      var top=work.top.set(corners[0][0]).lerp(corners[1][0],du);var bottom=work.bottom.set(corners[0][1]).lerp(corners[1][1],du);
      var pos=toHead.transformPosition(top.lerp(bottom,dv));pos.add(dx/16,dy/16,0);
      // Hidden dark cells sample the same-row iris pigment, not transparency.
      float sourceU=hideDark?(5+ix)/20f:u;
      polygon.add(pos.x,pos.y,pos.z,u0+(u1-u0)*sourceU,v0+(v1-v0)*v);
     }
     // Pigment underlay below every dark cell: lid clipping must never leave
     // a transparent hole in the iris. Original same-row atlas colour, no new PNG.
     if(dark&&!hideDark){
      Buffer pigment=work.pigment;pigment.count=0;
      for(int j=0;j<polygon.count;j++){int at=j*5;float su=polygon.data[at+3];float localU=(su-u0)/(u1-u0);float ix=Math.round(localU*20-col);pigment.add(polygon.data[at],polygon.data[at+1],polygon.data[at+2],u0+(u1-u0)*(5+ix)/20f,polygon.data[at+4]);}
      socket(out,pigment,inverse,q,left,mask,lower,false,visibleLow,visibleHigh,work);
      for(int j=0;j<polygon.count;j++){int at=j*5;polygon.data[at]+=0f;polygon.data[at+1]+=0f;polygon.data[at+2]+=-.00002f;}
     }
     socket(out,polygon,inverse,q,left,mask,lower,dark&&!hideDark,visibleLow,visibleHigh,work);


    }
   }
   fresh.add(new GeoCube(out.toArray(GeoQuad[]::new),c.pivot(),c.rotation(),c.size(),c.inflate(),c.mirror()));
  }
  b.getCubes().clear();b.getCubes().addAll(fresh);
 }
 private static void socket(List<GeoQuad> out,Buffer polygon,Matrix4f inverse,GeoQuad q,boolean left,float mask,float lower,boolean dark,float visibleLow,float visibleHigh,Work work){

     for(int band=0;band<5;band++){
      float xlo=(left?-XS[band+1]:XS[band])/16,xhi=(left?-XS[band]:XS[band+1])/16;
      float ylo=Math.max(LOW[band],27.35f+.22f*Math.clamp(lower,0,1))/16;
      if(dark)ylo=Math.max(ylo,(visibleLow+.015f)/16);
      float yhi=Math.min(HIGH[band],29.3f-1.95f*Math.clamp(mask,0,1))/16;
      if(dark)yhi=Math.min(yhi,(visibleHigh-.015f)/16);if(yhi<=ylo)continue;
      clip(polygon,work.a,0,xlo,true,work);clip(work.a,work.b,0,xhi,false,work);clip(work.b,work.a,1,ylo,true,work);clip(work.a,work.b,1,yhi,false,work);
      Buffer clipped=work.b;
      for(int j=1;j+1<clipped.count;j++){var verts=new GeoVertex[4];for(int k=0;k<4;k++){int at=(k==0?0:k==1?j:j+1)*5;verts[k]=new GeoVertex(inverse.transformPosition(new Vector3f(clipped.data[at],clipped.data[at+1],clipped.data[at+2])),clipped.data[at+3],clipped.data[at+4]);}out.add(new GeoQuad(verts,new Vector3f(q.normal()),q.direction()));}
     }
 }
 private static float map(float a,float lo,float hi,float toLo,float toHi){return a<lo?a/lo*toLo:a>hi?toHi+(a-hi)/(1-hi)*(1-toHi):toLo+(a-lo)/(hi-lo)*(toHi-toLo);}
 // Private read-only socket bands; no per-cell copies.
 private static final float[] XS={.55f,.67f,.83f,2.57f,2.73f,2.85f};
 private static final float[] LOW={27.63f,27.55f,27.35f,27.55f,27.63f},HIGH={28.77f,28.85f,29.05f,28.85f,28.77f};
 /** Scratch lives for ONE apply invocation. Never retained in REST/output or shared by entities. */
 private static final class Work {
  final Buffer polygon=new Buffer(),pigment=new Buffer(),a=new Buffer(),b=new Buffer();
  final Vector3f top=new Vector3f(),bottom=new Vector3f(),prev=new Vector3f(),next=new Vector3f();
 }
 private static final class Buffer {
  float[] data=new float[16*5];int count;
  void add(float x,float y,float z,float u,float v){int at=count*5;if(at+5>data.length)data=Arrays.copyOf(data,Math.max(at+5,data.length*2));data[at]=x;data[at+1]=y;data[at+2]=z;data[at+3]=u;data[at+4]=v;count++;}
  void copy(Buffer from,int at){add(from.data[at],from.data[at+1],from.data[at+2],from.data[at+3],from.data[at+4]);}
 }
 private static void clip(Buffer in,Buffer out,int axis,float bound,boolean greater,Work w){
  out.count=0;if(in.count==0)return;int prev=(in.count-1)*5;float pa=in.data[prev+axis]-bound;boolean pin=greater?pa>=0:pa<=0;
  for(int i=0;i<in.count;i++){int at=i*5;float a=in.data[at+axis]-bound;boolean inside=greater?a>=0:a<=0;
   if(inside!=pin){float t=pa/(pa-a);w.prev.set(in.data[prev],in.data[prev+1],in.data[prev+2]);w.next.set(in.data[at],in.data[at+1],in.data[at+2]);w.prev.lerp(w.next,t);out.add(w.prev.x,w.prev.y,w.prev.z,in.data[prev+3]+(in.data[at+3]-in.data[prev+3])*t,in.data[prev+4]+(in.data[at+4]-in.data[prev+4])*t);}
   if(inside)out.copy(in,at);prev=at;pa=a;pin=inside;
  }
 }
 private PupilIrisRig(){}
}
