package dev.kelmagic.research;
import java.util.*;import com.google.gson.*;
/** Data-driven graph. Icons, prerequisites, experiments and prose live in the catalog. */
public final class ResearchCatalog {
 public record Step(String kind,List<String> signals,String item,String hint,String comment,List<String> samples){}
 public record Node(String id,String tab,List<String> tabs,List<String> parents,int x,int y,String icon,boolean free,boolean sealed,List<Step> steps,String lore,String thought,String example){public boolean in(String t){return tab.equals(t)||tabs.contains(t);}}
 public static final List<Node> NODES;public static final Map<String,Node> BY_ID;
 static {try(var in=ResearchCatalog.class.getResourceAsStream("/data/kelmagic/research/catalog.json")){var root=JsonParser.parseReader(new java.io.InputStreamReader(Objects.requireNonNull(in),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();var out=new ArrayList<Node>();var ids=new HashSet<String>();for(var e:root.getAsJsonArray("nodes")){var n=e.getAsJsonObject();String id=n.get("id").getAsString();if(!ids.add(id))throw new IllegalArgumentException("duplicate research "+id);var steps=new ArrayList<Step>();for(var se:n.getAsJsonArray("steps")){var s=se.getAsJsonObject();steps.add(new Step(s.get("kind").getAsString(),strings(s,"signals"),str(s,"item"),str(s,"hint"),str(s,"comment"),strings(s,"samples")));}out.add(new Node(id,str(n,"tab"),strings(n,"tabs"),strings(n,"parents"),n.get("x").getAsInt(),n.get("y").getAsInt(),str(n,"icon"),n.get("free").getAsBoolean(),n.has("sealed")&&n.get("sealed").getAsBoolean(),List.copyOf(steps),str(n,"lore"),str(n,"thought"),str(n,"example")));}NODES=List.copyOf(out);var map=new LinkedHashMap<String,Node>();out.forEach(n->map.put(n.id,n));BY_ID=Collections.unmodifiableMap(map);for(var n:out)for(var parent:n.parents)if(!map.containsKey(parent))throw new IllegalArgumentException("missing research parent "+parent);}catch(Exception ex){throw new ExceptionInInitializerError(ex);}}
 static String str(JsonObject o,String key){return o.has(key)?o.get(key).getAsString():"";}static List<String> strings(JsonObject o,String key){if(!o.has(key))return List.of();var out=new ArrayList<String>();for(var e:o.getAsJsonArray(key))out.add(e.getAsString());return List.copyOf(out);}
 public static boolean learned(ResearchState s,String id){var n=BY_ID.get(id);return n!=null&&(n.free||s.learned().contains(id));}
 public static boolean visible(ResearchState s,Node n){return s.tabs().contains(n.tab)&&n.parents.stream().allMatch(p->learned(s,p));}
 private ResearchCatalog(){}
}
