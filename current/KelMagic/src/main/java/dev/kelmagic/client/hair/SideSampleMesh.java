package dev.kelmagic.client.hair;
import software.bernie.geckolib.cache.object.*;
import org.joml.Vector3f;
public final class SideSampleMesh {
 public static String texture(String n){return n.startsWith("sample_base")?"base":n.substring(7);}
 public static void apply(GeoBone b){float[][][] faces=switch(b.getName()) {
 case "sample_base" -> new float[][][]{{{3.99f,31.45f,-3.95f},{3.99f,28.4f,1.48f},{3.99f,31.45f,1.15f},{3.99f,31.45f,1.15f}},{{4.38f,31.45f,-3.95f},{4.38f,31.45f,1.15f},{4.38f,28.4f,1.48f},{4.38f,28.4f,1.48f}},{{3.99f,31.45f,-3.95f},{3.99f,25.5f,1.02f},{3.99f,28.4f,1.48f},{3.99f,28.4f,1.48f}},{{4.38f,31.45f,-3.95f},{4.38f,28.4f,1.48f},{4.38f,25.5f,1.02f},{4.38f,25.5f,1.02f}},{{3.99f,31.45f,-3.95f},{3.99f,24.7f,-0.45f},{3.99f,25.5f,1.02f},{3.99f,25.5f,1.02f}},{{4.38f,31.45f,-3.95f},{4.38f,25.5f,1.02f},{4.38f,24.7f,-0.45f},{4.38f,24.7f,-0.45f}},{{3.99f,31.45f,-3.95f},{3.99f,24.95f,-2.25f},{3.99f,24.7f,-0.45f},{3.99f,24.7f,-0.45f}},{{4.38f,31.45f,-3.95f},{4.38f,24.7f,-0.45f},{4.38f,24.95f,-2.25f},{4.38f,24.95f,-2.25f}},{{3.99f,31.45f,-3.95f},{3.99f,27.0f,-3.95f},{3.99f,24.95f,-2.25f},{3.99f,24.95f,-2.25f}},{{4.38f,31.45f,-3.95f},{4.38f,24.95f,-2.25f},{4.38f,27.0f,-3.95f},{4.38f,27.0f,-3.95f}},{{3.99f,31.45f,-3.95f},{3.99f,31.45f,1.15f},{4.38f,31.45f,1.15f},{4.38f,31.45f,-3.95f}},{{3.99f,31.45f,1.15f},{3.99f,28.4f,1.48f},{4.38f,28.4f,1.48f},{4.38f,31.45f,1.15f}},{{3.99f,28.4f,1.48f},{3.99f,25.5f,1.02f},{4.38f,25.5f,1.02f},{4.38f,28.4f,1.48f}},{{3.99f,25.5f,1.02f},{3.99f,24.7f,-0.45f},{4.38f,24.7f,-0.45f},{4.38f,25.5f,1.02f}},{{3.99f,24.7f,-0.45f},{3.99f,24.95f,-2.25f},{4.38f,24.95f,-2.25f},{4.38f,24.7f,-0.45f}},{{3.99f,24.95f,-2.25f},{3.99f,27.0f,-3.95f},{4.38f,27.0f,-3.95f},{4.38f,24.95f,-2.25f}},{{3.99f,27.0f,-3.95f},{3.99f,31.45f,-3.95f},{4.38f,31.45f,-3.95f},{4.38f,27.0f,-3.95f}}};
 case "sample_base_shoulder" -> new float[][][]{{{4.05f,26.1f,-3.9f},{4.5f,26.1f,-3.9f},{5.05f,24.25f,-3.75f},{4.55f,24.05f,-3.5f}},{{4.55f,24.05f,-3.19f},{5.05f,24.25f,-3.44f},{4.5f,26.1f,-3.59f},{4.05f,26.1f,-3.59f}},{{4.05f,26.1f,-3.9f},{4.05f,26.1f,-3.59f},{4.5f,26.1f,-3.59f},{4.5f,26.1f,-3.9f}},{{4.5f,26.1f,-3.9f},{4.5f,26.1f,-3.59f},{5.05f,24.25f,-3.44f},{5.05f,24.25f,-3.75f}},{{5.05f,24.25f,-3.75f},{5.05f,24.25f,-3.44f},{4.55f,24.05f,-3.19f},{4.55f,24.05f,-3.5f}},{{4.55f,24.05f,-3.5f},{4.55f,24.05f,-3.19f},{4.05f,26.1f,-3.59f},{4.05f,26.1f,-3.9f}}};
 case "sample_side_under" -> new float[][][]{{{4.44f,31.58f,-4.18f},{4.44f,31.58f,1.52f},{4.44f,24.45f,1.52f},{4.44f,24.45f,-4.18f}}};
 case "sample_turn_under" -> new float[][][]{{{3.02f,31.58f,-4.18f},{4.44f,31.58f,-4.18f},{4.44f,25.0f,-4.18f},{3.26f,25.0f,-4.18f}}};
 case "sample_shoulder_under" -> new float[][][]{{{3.46f,27.1f,-4.22f},{4.66f,27.1f,-4.22f},{5.25f,23.65f,-3.88f},{4.02f,23.45f,-3.88f}}};
 case "sample_side_over" -> new float[][][]{{{4.57f,31.58f,-4.31f},{4.57f,31.58f,1.52f},{4.57f,24.45f,1.52f},{4.57f,24.45f,-4.31f}}};
 case "sample_turn_over" -> new float[][][]{{{3.02f,31.58f,-4.31f},{4.57f,31.58f,-4.31f},{4.57f,25.0f,-4.31f},{3.26f,25.0f,-4.31f}}};
 case "sample_shoulder_over" -> new float[][][]{{{3.46f,27.1f,-4.35f},{4.66f,27.1f,-4.35f},{5.25f,23.65f,-4.01f},{4.02f,23.45f,-4.01f}}};
 default -> null;};if(faces==null)return;
 int j=0;for(var cube:b.getCubes()){var p=faces[j++];for(int qi=0;qi<cube.quads().length;qi++){var old=cube.quads()[qi];if(old==null)continue;var v=new GeoVertex[4];for(int k=0;k<4;k++)v[k]=new GeoVertex(new Vector3f(-p[k][0]/16,p[k][1]/16,p[k][2]/16),k==0||k==3?0:1,k<2?0:1);
 var normal=new Vector3f(v[1].position()).sub(v[0].position()).cross(new Vector3f(v[2].position()).sub(v[0].position())).normalize();if(b.getName().startsWith("sample_side")&&normal.x>0)normal.negate();
 if((b.getName().startsWith("sample_turn")||b.getName().startsWith("sample_shoulder"))&&normal.z>0)normal.negate();
 if(b.getName().startsWith("sample_base")){boolean shoulder=b.getName().endsWith("shoulder");var center=new Vector3f(shoulder?-4.55f/16:-4.18f/16,shoulder?24.9f/16:28f/16,shoulder?-3.55f/16:-1f/16);var mid=new Vector3f();for(var a:v)mid.add(a.position());mid.div(4).sub(center);if(normal.dot(mid)<0)normal.negate();}
 cube.quads()[qi]=new GeoQuad(v,normal,old.direction());}}
 }
}
