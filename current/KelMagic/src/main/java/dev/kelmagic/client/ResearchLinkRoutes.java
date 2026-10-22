package dev.kelmagic.client;
import dev.kelmagic.research.ResearchCatalog;
import java.util.*;
/** Cached obstacle-avoiding routes; unrelated frames and progress badges are obstacles. */
public final class ResearchLinkRoutes {
 public record Point(int x,int y){}
 private static final Map<String,List<Point>> CACHE=new HashMap<>();
 private record Cell(int x,int y){}
 private record Visit(Cell c,int score){}
 public static List<Point> route(List<ResearchCatalog.Node> nodes,ResearchCatalog.Node a,ResearchCatalog.Node b){String key=nodes.stream().map(n->n.id()+":"+n.x()+","+n.y()).reduce("",(s,t)->s+";"+t)+"/"+a.id()+"/"+b.id();return CACHE.computeIfAbsent(key,k->find(nodes,a,b));}
 private static List<Point> find(List<ResearchCatalog.Node> ns,ResearchCatalog.Node a,ResearchCatalog.Node b){Cell start=new Cell(Math.round(a.x()/4f),Math.round(a.y()/4f)),goal=new Cell(Math.round(b.x()/4f),Math.round(b.y()/4f));var open=new PriorityQueue<Visit>(Comparator.comparingInt(Visit::score));var cost=new HashMap<Cell,Integer>();var prev=new HashMap<Cell,Cell>();open.add(new Visit(start,0));cost.put(start,0);int guard=0;while(!open.isEmpty()&&guard++<60000){var c=open.remove().c;if(c.equals(goal)){var out=new ArrayList<Point>();while(c!=null){out.add(new Point(c.x*4,c.y*4));c=prev.get(c);}Collections.reverse(out);return smooth(out,ns,a,b);}for(int[] d:new int[][]{{1,0},{0,1},{-1,0},{0,-1}}){var next=new Cell(c.x+d[0],c.y+d[1]);if(Math.abs(next.x)>160||Math.abs(next.y)>160)continue;boolean blocked=false;for(var n:ns)if(n!=a&&n!=b&&Math.abs(next.x*4-n.x())<=23&&next.y*4>=n.y()-23&&next.y*4<=n.y()+38){blocked=true;break;}if(blocked)continue;int g=cost.get(c)+1;if(g>=cost.getOrDefault(next,Integer.MAX_VALUE))continue;cost.put(next,g);prev.put(next,c);open.add(new Visit(next,g+Math.abs(next.x-goal.x)+Math.abs(next.y-goal.y)));}}return List.of();}
 private static boolean clear(Point a,Point b,List<ResearchCatalog.Node> ns,ResearchCatalog.Node source,ResearchCatalog.Node target){int steps=Math.max(1,(int)Math.ceil(Math.hypot(b.x-a.x,b.y-a.y)));for(int i=0;i<=steps;i++){double f=i/(double)steps;int x=(int)Math.round(a.x+(b.x-a.x)*f),y=(int)Math.round(a.y+(b.y-a.y)*f);for(var n:ns)if(n!=source&&n!=target&&Math.abs(x-n.x())<=23&&y>=n.y()-23&&y<=n.y()+38)return false;}return true;}
 private static List<Point> smooth(List<Point> path,List<ResearchCatalog.Node> ns,ResearchCatalog.Node a,ResearchCatalog.Node b){var out=new ArrayList<Point>();int at=0;out.add(path.getFirst());while(at<path.size()-1){int next=path.size()-1;while(next>at+1&&!clear(path.get(at),path.get(next),ns,a,b))next--;var p=path.get(at);var q=path.get(next);int steps=Math.max(1,(int)Math.ceil(Math.hypot(q.x-p.x,q.y-p.y)));for(int i=1;i<=steps;i++){double f=i/(double)steps;out.add(new Point((int)Math.round(p.x+(q.x-p.x)*f),(int)Math.round(p.y+(q.y-p.y)*f)));}at=next;}return List.copyOf(out);}
 private ResearchLinkRoutes(){}
}
