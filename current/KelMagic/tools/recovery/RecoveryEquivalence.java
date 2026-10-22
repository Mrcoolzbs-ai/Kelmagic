import java.net.*;import java.nio.file.*;import java.lang.reflect.*;import java.util.*;import org.joml.Matrix4f;import org.joml.Vector3f;
public final class RecoveryEquivalence {
 static long checks;
 static void eq(Object a,Object b,String p)throws Exception {
  checks++;
  if(a==null||b==null){if(a!=b)throw new AssertionError(p);return;}
  if(a instanceof Float x){if(Float.floatToRawIntBits(x)!=Float.floatToRawIntBits((Float)b))throw new AssertionError(p+" "+a+" != "+b);return;}
  if(a instanceof Double x){if(Double.doubleToRawLongBits(x)!=Double.doubleToRawLongBits((Double)b))throw new AssertionError(p+" "+a+" != "+b);return;}
  if(a instanceof Number||a instanceof Boolean||a instanceof String){if(!a.equals(b))throw new AssertionError(p);return;}
  if(a instanceof Vector3f v){Vector3f w=(Vector3f)b;eq(v.x,w.x,p+".x");eq(v.y,w.y,p+".y");eq(v.z,w.z,p+".z");return;}
  if(a instanceof Matrix4f m){eq(m.get(new float[16]),((Matrix4f)b).get(new float[16]),p);return;}
  if(a.getClass().isArray()){int n=Array.getLength(a);if(n!=Array.getLength(b))throw new AssertionError(p+" length");for(int i=0;i<n;i++)eq(Array.get(a,i),Array.get(b,i),p+"["+i+"]");return;}
  for(Field f:a.getClass().getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);Field g=b.getClass().getDeclaredField(f.getName());g.setAccessible(true);eq(f.get(a),g.get(b),p+"."+f.getName());}
 }
 static Object collider(ClassLoader c,Matrix4f m,Vector3f lo,Vector3f hi,boolean arm)throws Exception{return c.loadClass("dev.kelmagic.client.hair.RearHairCloth$Collider").getConstructor(Matrix4f.class,Vector3f.class,Vector3f.class,boolean.class).newInstance(m,lo,hi,arm);}
 static void engine(ClassLoader a,ClassLoader b,String name,int frames)throws Exception {
  String full="dev.kelmagic.client.hair."+name;Class<?> ca=a.loadClass(full),cb=b.loadClass(full);Object x=ca.getConstructor().newInstance(),y=cb.getConstructor().newInstance();
  Class<?>[] sig={double.class,Matrix4f.class,List.class,boolean.class,Vector3f.class,Matrix4f.class};Method ma=ca.getMethod("sample",sig),mb=cb.getMethod("sample",sig);Method na=ca.getMethod("needsSample",double.class,boolean.class),nb=cb.getMethod("needsSample",double.class,boolean.class);Method da=ca.getMethod("deform",Vector3f.class),db=cb.getMethod("deform",Vector3f.class);
  Random r=new Random(981723);double t=0;
  for(int i=0;i<frames;i++){
   double dt=switch(i%131){case 0->0;case 1->.00000001;case 2->-.01;case 3->.4;case 4->.2;default->1.0/(30+r.nextInt(110));};t+=dt;
   boolean reset=i%197==0;float yaw=(float)java.lang.Math.sin(i*.083)*1.12f,pitch=(float)java.lang.Math.sin(i*.049)*.42f;
   Matrix4f head=new Matrix4f().rotateY(yaw).rotateX(pitch),body=new Matrix4f().rotateY((float)java.lang.Math.sin(i*.019)*2f).rotateZ((float)java.lang.Math.sin(i*.04)*.05f);
   List<Object> la=new ArrayList<>(),lb=new ArrayList<>();
   Matrix4f cm=new Matrix4f().translate((float)java.lang.Math.sin(i*.04)*.04f,0,0).rotateX(.06f);
   for(var l:List.of(la,lb)){ClassLoader cl=l==la?a:b;l.add(collider(cl,head,new Vector3f(-3.3f,23.9f,-3.3f),new Vector3f(3.3f,31.8f,3.3f),false));l.add(collider(cl,cm,new Vector3f(-4,12,-2),new Vector3f(4,24,2),false));l.add(collider(cl,new Matrix4f().rotateZ((float)java.lang.Math.sin(i*.1)*.5f),new Vector3f(4,12,-2),new Vector3f(7,24,2),true));}
   Vector3f move=new Vector3f((float)java.lang.Math.sin(i*.1)*.025f,(float)java.lang.Math.cos(i*.05)*.01f,(float)java.lang.Math.cos(i*.1)*.02f);
   eq(na.invoke(x,t,reset),nb.invoke(y,t,reset),name+" needsSample");
   ma.invoke(x,t,head,la,reset,move,body);mb.invoke(y,t,head,lb,reset,move,body);eq(x,y,name+" frame "+i);
   for(int v=0;v<16;v++){Vector3f p=new Vector3f(r.nextFloat()*12-6,r.nextFloat()*22+8,r.nextFloat()*12-6),q=new Vector3f(p);da.invoke(x,p);db.invoke(y,q);eq(p,q,name+" deform "+i+"/"+v);}
  }
  System.out.println("PASS "+name+": "+frames+" identical-input frames; all instance fields and deformed probes bitwise equal");
 }
 static void contacts(ClassLoader a,ClassLoader b)throws Exception{
  Class<?> ca=a.loadClass("dev.kelmagic.client.hair.RearHairCloth$Collider"),cb=b.loadClass("dev.kelmagic.client.hair.RearHairCloth$Collider");Method ma=ca.getDeclaredMethod("projectNarrow",Vector3f.class,float.class),mb=cb.getDeclaredMethod("projectNarrow",Vector3f.class,float.class),na=ca.getDeclaredMethod("mightContact",Vector3f.class,float.class),nb=cb.getDeclaredMethod("mightContact",Vector3f.class,float.class);for(Method m:List.of(ma,mb,na,nb))m.setAccessible(true);
  Random r=new Random(615);for(int i=0;i<10000;i++){Matrix4f m=new Matrix4f().translate(r.nextFloat()-.5f,r.nextFloat()-.5f,r.nextFloat()-.5f).rotateXYZ(r.nextFloat(),r.nextFloat(),r.nextFloat());Object x=collider(a,m,new Vector3f(-4,12,-2),new Vector3f(4,24,2),false),y=collider(b,m,new Vector3f(-4,12,-2),new Vector3f(4,24,2),false);Vector3f p=new Vector3f(r.nextFloat()*20-10,r.nextFloat()*32,r.nextFloat()*20-10),q=new Vector3f(p);float radius=r.nextFloat();eq(na.invoke(x,p,radius),nb.invoke(y,q,radius),"broad phase");eq(ma.invoke(x,p,radius),mb.invoke(y,q,radius),"contact");eq(p,q,"contact result");}
  System.out.println("PASS 10000 transformed contact probes");
 }
 public static void main(String[] args)throws Exception{
  try(URLClassLoader a=new URLClassLoader(new URL[]{Path.of(args[0]).toUri().toURL()},RecoveryEquivalence.class.getClassLoader());URLClassLoader b=new URLClassLoader(new URL[]{Path.of(args[1]).toUri().toURL()},RecoveryEquivalence.class.getClassLoader())){
   contacts(a,b);engine(a,b,"RedesignedHairEngine",650);engine(a,b,"ReferenceHairEngine",650);engine(a,b,"RearHairCloth",300);System.out.println("PASS total comparisons="+checks+"; JOML useMathFma="+System.getProperty("joml.useMathFma","false"));
  }
 }
}
