package dev.kelmagic.client;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import com.google.gson.*;
import org.joml.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
/** Exports the actual baked in-game meshes for a headless, explicitly technical UV proof. */
public final class ArtifactArmorExport {
    private static Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    private static void walk(ModelPart part,Matrix4f parent,JsonArray output)throws Exception{var matrix=new Matrix4f(parent).translate(part.x,part.y,part.z).rotateZYX(part.zRot,part.yRot,part.xRot).scale(part.xScale,part.yScale,part.zScale);for(Object cube:(List<?>)field(part,"cubes"))for(Object polygon:(Object[])field(cube,"polygons")){var points=new JsonArray();for(Object vertex:(Object[])field(polygon,"vertices")){var pos=new Vector3f((Vector3f)field(vertex,"pos"));matrix.transformPosition(pos);var p=new JsonArray();p.add(pos.x);p.add(pos.y);p.add(pos.z);p.add(((Number)field(vertex,"u")).floatValue());p.add(((Number)field(vertex,"v")).floatValue());points.add(p);}output.add(points);}for(Object child:((Map<?,?>)field(part,"children")).values())walk((ModelPart)child,matrix,output);}
    public static void main(String[] args)throws Exception{var all=new JsonObject();for(String set:List.of("deep_source","steady_anchor","grove_keeper","strict_negation")){var polygons=new JsonArray();for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)){var m=new ArtifactArmorModel(set,slot);for(var part:List.of(m.head,m.hat,m.body,m.rightArm,m.leftArm,m.rightLeg,m.leftLeg))walk(part,new Matrix4f(),polygons);}all.add(set,polygons);}var target=Path.of(args[0]);Files.createDirectories(target.getParent());Files.writeString(target,new GsonBuilder().create().toJson(all));System.out.println("Actual baked armor exported: 4 sets; "+target);}
}
