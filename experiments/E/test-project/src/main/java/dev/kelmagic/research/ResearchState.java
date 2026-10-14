package dev.kelmagic.research;
import java.util.*;import com.mojang.serialization.Codec;import com.mojang.serialization.codecs.RecordCodecBuilder;
public record ResearchState(List<String> learned,List<String> tabs,List<String> started,List<String> evidence){
 public ResearchState {learned=List.copyOf(new LinkedHashSet<>(learned));tabs=List.copyOf(new LinkedHashSet<>(tabs));started=List.copyOf(new LinkedHashSet<>(started));evidence=List.copyOf(new LinkedHashSet<>(evidence));}
 public static final ResearchState EMPTY=new ResearchState(List.of(),List.of("origins"),List.of(),List.of());
 private static final Codec<List<String>> L=Codec.STRING.listOf(0,512);
 public static final Codec<ResearchState> CODEC=RecordCodecBuilder.create(i->i.group(L.optionalFieldOf("learned",List.of()).forGetter(ResearchState::learned),L.optionalFieldOf("tabs",List.of("origins")).forGetter(ResearchState::tabs),L.optionalFieldOf("started",List.of()).forGetter(ResearchState::started),L.optionalFieldOf("evidence",List.of()).forGetter(ResearchState::evidence)).apply(i,ResearchState::new));
 public ResearchState add(String list,String value){var l=new ArrayList<>(learned);var t=new ArrayList<>(tabs);var a=new ArrayList<>(started);var e=new ArrayList<>(evidence);switch(list){case "learned"->l.add(value);case "tabs"->t.add(value);case "started"->a.add(value);case "evidence"->e.add(value);default->throw new IllegalArgumentException(list);}return new ResearchState(l,t,a,e);}
}
