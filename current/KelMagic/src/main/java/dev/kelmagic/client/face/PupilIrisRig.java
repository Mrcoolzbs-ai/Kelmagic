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
     var polygon=new ArrayList<GeoVertex>();
     for(var a:q.vertices()){
      int ix=a.texU()>(u0+u1)/2?1:0,iy=a.texV()>(v0+v1)/2?1:0;float u=(col+ix)/20f,v=(row+iy)/20f;
      float dv=map(v,.2f,.8f,pupilV-.20f*size,pupilV+.20f*size);
      float edge=Math.abs((v-.5f)/.3f);float round=.45f;
      // Smooth polygonal corner cuts with exact shared row boundaries.
      if(edge<1)round=Math.max(.45f,(float)Math.sqrt(Math.max(0,1-edge*edge)));
      float du=map(u,.4f,.6f,.5f-.20f*size*round,.5f+.20f*size*round);
      var top=new Vector3f(corners[0][0]).lerp(corners[1][0],du);var bottom=new Vector3f(corners[0][1]).lerp(corners[1][1],du);
      var pos=toHead.transformPosition(top.lerp(bottom,dv));pos.add(dx/16,dy/16,0);
      // Hidden dark cells sample the same-row iris pigment, not transparency.
      float sourceU=hideDark?(5+ix)/20f:u;
      polygon.add(new GeoVertex(pos,u0+(u1-u0)*sourceU,v0+(v1-v0)*v));
     }
     // Pigment underlay below every dark cell: lid clipping must never leave
     // a transparent hole in the iris. Original same-row atlas colour, no new PNG.
     if(dark&&!hideDark){
      var pigment=new ArrayList<GeoVertex>();int n=0;
      for(var v:polygon){float su=v.texU();float localU=(su-u0)/(u1-u0);float ix=Math.round(localU*20-col);pigment.add(new GeoVertex(new Vector3f(v.position()),u0+(u1-u0)*(5+ix)/20f,v.texV()));}
      socket(out,pigment,inverse,q,left,mask,lower,false,visibleLow,visibleHigh);
      for(int j=0;j<polygon.size();j++){var v=polygon.get(j);polygon.set(j,new GeoVertex(new Vector3f(v.position()).add(0,0,-.00002f),v.texU(),v.texV()));}
     }
     socket(out,polygon,inverse,q,left,mask,lower,dark&&!hideDark,visibleLow,visibleHigh);


    }
   }
   fresh.add(new GeoCube(out.toArray(GeoQuad[]::new),c.pivot(),c.rotation(),c.size(),c.inflate(),c.mirror()));
  }
  b.getCubes().clear();b.getCubes().addAll(fresh);
 }
 private static void socket(List<GeoQuad> out,List<GeoVertex> polygon,Matrix4f inverse,GeoQuad q,boolean left,float mask,float lower,boolean dark,float visibleLow,float visibleHigh){
     float[] xs={.55f,.67f,.83f,2.57f,2.73f,2.85f};float[] low={27.63f,27.55f,27.35f,27.55f,27.63f},high={28.77f,28.85f,29.05f,28.85f,28.77f};
     for(int band=0;band<5;band++){
      float xlo=(left?-xs[band+1]:xs[band])/16,xhi=(left?-xs[band]:xs[band+1])/16;
      float ylo=Math.max(low[band],27.35f+.22f*Math.clamp(lower,0,1))/16;
      if(dark)ylo=Math.max(ylo,(visibleLow+.015f)/16);
      float yhi=Math.min(high[band],29.3f-1.95f*Math.clamp(mask,0,1))/16;
      if(dark)yhi=Math.min(yhi,(visibleHigh-.015f)/16);if(yhi<=ylo)continue;
      List<GeoVertex> clipped=clip(polygon,0,xlo,true);clipped=clip(clipped,0,xhi,false);clipped=clip(clipped,1,ylo,true);clipped=clip(clipped,1,yhi,false);
      for(int j=1;j+1<clipped.size();j++){var verts=new GeoVertex[4];int[] ids={0,j,j+1,j+1};for(int k=0;k<4;k++){var v=clipped.get(ids[k]);verts[k]=new GeoVertex(inverse.transformPosition(new Vector3f(v.position())),v.texU(),v.texV());}out.add(new GeoQuad(verts,new Vector3f(q.normal()),q.direction()));}
     }
 }
 private static float map(float a,float lo,float hi,float toLo,float toHi){return a<lo?a/lo*toLo:a>hi?toHi+(a-hi)/(1-hi)*(1-toHi):toLo+(a-lo)/(hi-lo)*(toHi-toLo);}
 private static List<GeoVertex> clip(List<GeoVertex> in,int axis,float bound,boolean greater){var out=new ArrayList<GeoVertex>();if(in.isEmpty())return out;GeoVertex prev=in.getLast();float pa=prev.position().get(axis)-bound;boolean pin=greater?pa>=0:pa<=0;for(var v:in){float a=v.position().get(axis)-bound;boolean inside=greater?a>=0:a<=0;if(inside!=pin){float t=pa/(pa-a);out.add(new GeoVertex(new Vector3f(prev.position()).lerp(v.position(),t),prev.texU()+(v.texU()-prev.texU())*t,prev.texV()+(v.texV()-prev.texV())*t));}if(inside)out.add(v);prev=v;pa=a;pin=inside;}return out;}
 private PupilIrisRig(){}
}
