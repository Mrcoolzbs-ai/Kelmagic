package dev.kelmagic.core;
/** Original analytical motifs, no copied meshes, assets or ability code. Units are blocks/ticks. */
public final class PhraseGeometry {
 public interface Sink {
  void point(double x,double y,double z,int style,double size,double alpha);
  default void line(double x,double y,double z,double bx,double by,double bz,int style,double width,double alpha){
   point(x,y,z,style,width,alpha);point((x+bx)/2,(y+by)/2,(z+bz)/2,style,width,alpha);
  }
 }
 public static final int FIRE=0,STORM=1,GUARDIAN=2;
 public static int lifetime(int kind){return kind==FIRE?44:kind==STORM?34:kind==GUARDIAN?44:0;}
 public static void draw(int kind,int age,double scale,int seed,Sink out){
  if(age<0||age>=lifetime(kind)||!Double.isFinite(scale))return;
  double s=Math.max(.1,Math.min(2,scale)),fade=Math.min(1,(lifetime(kind)-age)/10.0);
  Sink sink=new Sink(){
   public void point(double x,double y,double z,int style,double size,double a){out.point(x*s,y*s,z*s,style,size,fade*a);}
   public void line(double x,double y,double z,double bx,double by,double bz,int style,double width,double a){out.line(x*s,y*s,z*s,bx*s,by*s,bz*s,style,width,fade*a);}
  };
  if(kind==FIRE)fire(age,seed,sink);else if(kind==STORM)storm(age,seed,sink);else if(kind==GUARDIAN)guardian(age,sink);
 }
 private static void ring(Sink s,double r,double y,int n,double phase,int style,double size,double alpha){
  for(int i=0;i<n;i++){double a=phase+Math.PI*2*i/n;double b=phase+Math.PI*2*(i+1)/n;s.line(Math.cos(a)*r,y,Math.sin(a)*r,Math.cos(b)*r,y,Math.sin(b)*r,style,size*.35,alpha);}
 }
 private static void fire(int age,int seed,Sink s){
  double reveal=1-Math.pow(1-Math.min(1,age/10.0),3),r=3.15*reveal;
  if(age%2==0){ring(s,r,.10,32,age*.018,0,.055,.9);ring(s,r*.84,.12,24,-age*.02,1,.05,.85);}
  for(int i=0;i<8;i++){double a=i*Math.PI/4+age*.012;
   s.point(Math.cos(a)*r,.16,Math.sin(a)*r,4,.11,.9);
  }
  // Eight woven ember plumes, rather than a solid wall of flame hiding the world.
  for(int i=0;i<24;i++){double t=(i%3)/3.0,y=((age+i*3)%28)/28.0*3.0,a=(i/3)*Math.PI/4+y*.7+seed*.00001;
   double rad=Math.max(.2,r*(1-y*.20));s.point(Math.cos(a)*rad,.12+y,Math.sin(a)*rad,i%3==0?1:0,.035+t*.018,.8*(1-y/3.6));}
  if(age>=8){
   double h=Math.min(1,(age-6)/12.0)*2.5;
   // Raised crown stays legible above the pre-existing solid arcane-fire sprites.
   for(int i=0;i<8;i++){
    double base=i*Math.PI/4+age*.012;
    for(int j=0;j<4;j++){double t=j/4.0,u=(j+1)/4.0,a=base+t*.5,b=base+u*.5,ra=r-.5*t,rb=r-.5*u;
     s.line(Math.cos(a)*ra,.15+h*t,Math.sin(a)*ra,Math.cos(b)*rb,.15+h*u,Math.sin(b)*rb,0,.025,.65);
    }
    s.point(Math.cos(base+.5)*(r-.5),.15+h,Math.sin(base+.5)*(r-.5),4,.10,.9);
   }
   if(age%2==0)ring(s,r-.5,.15+h,16,age*.012+.5,1,.055,.8);
  }
 }
 private static void storm(int age,int seed,Sink s){
  double phase=(seed&1023)*.006;
  // Crown descends into a handful of asymmetric branching filaments. No screen flash.
  if(age<10)ring(s,1.15,2.5-age*.07,32,phase+age*.06,2,.045,.7);
  if(age>=4&&age<24){
   for(int arm=0;arm<4;arm++){
    double a=phase+arm*Math.PI/2,prevX=Math.cos(a)*.5,prevY=2.8,prevZ=Math.sin(a)*.5;
    for(int k=1;k<=7;k++){
     double t=k/7.0,j=Math.sin(seed*.0003+arm*13+k*7+(age/3)*2)*.24;
     double x=Math.cos(a)*(t*1.7)+Math.cos(a+1.4)*j,z=Math.sin(a)*(t*1.7)+Math.sin(a+1.4)*j,y=2.8*(1-t);
     s.line(prevX,prevY,prevZ,x,y,z,2,.035,.95);s.point(x,y,z,3,.045,.9);
     if(k==4||k==6)s.point(x+Math.cos(a+1)*.22,y+.16,z+Math.sin(a+1)*.22,3,.07,.7);
     prevX=x;prevY=y;prevZ=z;
    }
   }
  }
  if(age>=7&&age%2==1){double t=(age-7)/26.0;ring(s,.5+3.1*t,.15,32,phase,2,.05,.85*(1-t));}
  if(age>=8&&age<22)for(int i=0;i<8;i++){double a=i*Math.PI/4+phase;s.point(Math.cos(a)*1.8,.1+(age-8)*.045,Math.sin(a)*1.8,5,.09,.7);}
 }
 private static void guardian(int age,Sink s){
  double reveal=Math.min(1,age/8.0),r=1.05*reveal;
  for(int i=0;i<32;i++){double t=(i%16)/15.0,a=t*Math.PI*2+age*.12+(i>=16?Math.PI:0);s.point(Math.cos(a)*r,.16+t*1.9,Math.sin(a)*r,i>=16?2:1,.035,.75);}
  if(age%2==0)for(int panel=0;panel<6;panel++){
   double a=panel*Math.PI/3+age*.008;
   for(int k=0;k<6;k++){
    double b=k*Math.PI/3,b2=(k+1)*Math.PI/3;
    double u=Math.cos(b)*.32,v=Math.sin(b)*.53,u2=Math.cos(b2)*.32,v2=Math.sin(b2)*.53;
    s.line(Math.cos(a)*r-Math.sin(a)*u,1.12+v*reveal,Math.sin(a)*r+Math.cos(a)*u,Math.cos(a)*r-Math.sin(a)*u2,1.12+v2*reveal,Math.sin(a)*r+Math.cos(a)*u2,1,.022,.9);
   }
   s.point(Math.cos(a)*r,1.12,Math.sin(a)*r,5,.10,.9);
  }
  if(age%3==0){ring(s,r,.13,18,0,2,.035,.6);ring(s,r,2.15,18,0,1,.035,.6);}
 }
 private PhraseGeometry(){}
}
