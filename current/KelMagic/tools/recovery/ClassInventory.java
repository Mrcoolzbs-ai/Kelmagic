import org.objectweb.asm.*;
import java.nio.file.*;import java.security.*;import java.util.*;import java.util.zip.*;
public class ClassInventory {
 static byte[] norm(byte[] b){ClassWriter w=new ClassWriter(0);new ClassReader(b).accept(w,ClassReader.SKIP_DEBUG);return w.toByteArray();}
 static String hash(byte[] b)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));}
 public static void main(String[] args)throws Exception{
  Path base=Path.of(args[0]);boolean zip=args[0].endsWith(".jar");TreeMap<String,byte[]> files=new TreeMap<>();
  if(zip){try(ZipFile z=new ZipFile(base.toFile())){for(var e:Collections.list(z.entries()))if(e.getName().endsWith(".class"))files.put(e.getName(),z.getInputStream(e).readAllBytes());}}
  else{try(var s=Files.walk(base)){for(Path p:s.filter(x->x.toString().endsWith(".class")).toList())files.put(base.relativize(p).toString(),Files.readAllBytes(p));}}
  for(var e:files.entrySet())System.out.println(e.getKey()+"\t"+hash(norm(e.getValue())));
 }
}
