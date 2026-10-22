package dev.kelmagic.content;
import java.util.*;
/** Stable IDs survive translation and selection order changes. No arbitrary user input packets. */
public final class InkPatterns {
 public record Pattern(String id,String glyph,String category,int tile){}
 public static final List<Pattern> ALL;public static final Map<String,Pattern> BY_ID;
 static {var a=new ArrayList<Pattern>();letters(a,"АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя","Русский");letters(a,"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz","English");letters(a,"0123456789","Числа");letters(a,"←↑→↓↖↗↘↙↔↕","Стрелки");for(String id:List.of("creeper","zombie","skeleton","enderman","pig","sheep","cat","wolf","bee","slime"))a.add(new Pattern(id,id,"Мобы",a.size()));letters(a,"★✦♥◆●☀☾⚡!?+=−×÷∞⚒⚓","Знаки");a.add(new Pattern("erase","⌫","Знаки",a.size()));ALL=List.copyOf(a);var m=new LinkedHashMap<String,Pattern>();a.forEach(p->m.put(p.id,p));BY_ID=Collections.unmodifiableMap(m);}
 static void letters(List<Pattern> a,String chars,String category){chars.codePoints().forEach(c->a.add(new Pattern("u"+Integer.toHexString(c),new String(Character.toChars(c)),category,a.size())));}
 public static Pattern get(String id){return BY_ID.getOrDefault(id,BY_ID.get("u2192"));}
 public static String label(Pattern p){return switch(p.id){case "creeper"->"Крипер";case "zombie"->"Зомби";case "skeleton"->"Скелет";case "enderman"->"Эндермен";case "pig"->"Свинья";case "sheep"->"Овца";case "cat"->"Кот";case "wolf"->"Волк";case "bee"->"Пчела";case "slime"->"Слизень";case "erase"->"Ластик";default->p.glyph;};}
 private InkPatterns(){}
}
