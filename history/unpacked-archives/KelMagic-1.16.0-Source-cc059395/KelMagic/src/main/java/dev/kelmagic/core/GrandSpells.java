package dev.kelmagic.core;
import java.util.*;
public final class GrandSpells {
 public record Rite(String id,int minimum,List<String> roots){public List<String> formula(){var out=new ArrayList<>(roots);while(out.size()<minimum)out.add("muk");return List.copyOf(out);}}
 private static Rite r(String id,int min,String words){return new Rite(id,min,List.of(words.split(" ")));}
 public static final List<Rite> ALL=List.of(
 r("wings",10,"mina son pis neb vit rap kin ten liv tem"),
 r("island",15,"ter gor nuk neb nok rok sel tur del gel lel mel tel lok kan"),
 r("blast",5,"pir tup dun por"),
 r("duplicate",30,"sila nov liv per ged gok nos mur ped kor mam pap kin dek gek lek mek nek pek rek tek rut sut tut sav pen kan lok tem lu"),
 r("ender",10,"mina kut tom lok rin puk sav kan sin tem"),
 r("mana_tide",25,"tuna kor liv por kan sav pen log kel tem bon mam pap kin ged gek lek mek nek pek rek tek rut vut ten"),
 r("meteor",25,"sap nok gor pir dun tup pad neb mor nak por muk muk muk muk muk muk muk muk muk muk muk muk muk muk"),
 r("transcend",100,String.join(" ",Collections.nCopies(5,"mina neb sap sol lun tem sav pen kan por liv mor sin rin tin log kel tuk bal bon"))),
 r("end_gate",20,"dor rin sin lok neb sap lun tom kan tem sav pen log kel tuk bal per liv mor ten"));
 public static Rite find(Collection<String> words){if(words.size()>100)return null;var stripped=words.stream().filter(w->!w.equals("muk")&&!w.equals("nis")).sorted().toList();for(var r:ALL)if(words.size()>=r.minimum&&(!r.id.equals("transcend")||words.size()==100)&&stripped.equals(r.roots.stream().filter(w->!w.equals("muk")&&!w.equals("nis")).sorted().toList()))return r;return null;}
 public static double destructiveScale(Collection<String> words){return (1+.5*Collections.frequency(new ArrayList<>(words),"muk"))/(1+.5*Collections.frequency(new ArrayList<>(words),"nis"));}
 private GrandSpells(){}
}