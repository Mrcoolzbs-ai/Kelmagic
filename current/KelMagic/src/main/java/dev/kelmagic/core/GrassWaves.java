package dev.kelmagic.core;
import java.util.List;
/** Signed source amplitudes sum before colouring: opposite phases cancel, base tide stays. */
public final class GrassWaves {
 public record Source(double x,double y,double z){}
 public static double interference(double x,double y,double z,double time,List<Source> sources){double sum=0;for(var s:sources){double d=Math.sqrt((x-s.x)*(x-s.x)+(z-s.z)*(z-s.z));if(d>96||Math.abs(y-s.y)>8)continue;double falloff=Math.pow(1-d/96,2);sum+=Math.sin(d*Math.PI/18-time*Math.PI/240)*falloff;}return sum;}
 public static int color(double x,double y,double z,double time,List<Source> sources){double base=Math.sin(-time/2000*Math.PI*2);double signal=base+.9*interference(x,y,z,time,sources);double wave=(1+Math.tanh(signal*1.25)/Math.tanh(1.25))*.5;wave=Math.max(0,Math.min(1,wave));wave=wave*wave*(3-2*wave);return 0xff000000|((int)Math.round(18+146*wave)<<16)|((int)Math.round(30+20*wave)<<8)|(int)Math.round(66+158*wave);}
 private GrassWaves(){}
}
