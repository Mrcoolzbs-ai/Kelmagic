package dev.kelmagic.server;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
public final class Playback {
 private static final Map<UUID,String> SOURCE=new HashMap<>();
 public static String source(ServerPlayer p){return SOURCE.getOrDefault(p.getUUID(),"");}
 public static void run(ServerPlayer p,String source,Runnable f){var old=SOURCE.put(p.getUUID(),source);try{f.run();}finally{if(old==null)SOURCE.remove(p.getUUID());else SOURCE.put(p.getUUID(),old);}}
 private Playback(){}
}
