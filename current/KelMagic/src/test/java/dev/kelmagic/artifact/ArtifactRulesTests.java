package dev.kelmagic.artifact;
public final class ArtifactRulesTests {
    private static int checks;
    private static void check(boolean ok){checks++;if(!ok)throw new AssertionError("Artifact rule #"+checks);}
    public static void main(String[] args){
        for(int capacity:new int[]{32,128,512})for(int stored=0;stored<=capacity;stored++)for(int requested:new int[]{0,1,5,1000}){double n=ArtifactRules.deposit(stored,requested,capacity);check(n>=0);check(n<=requested);check(stored+n<=capacity);}
        check(ArtifactRules.finite(Double.NaN,10)==0);check(ArtifactRules.finite(Double.POSITIVE_INFINITY,10)==0);check(ArtifactRules.finite(-3,10)==0);
        for(int time=0;time<100;time++){check(ArtifactRules.parry(time,false,1)==(time<=5));check(!ArtifactRules.parry(time,true,1));check(!ArtifactRules.parry(time,false,-1));}
        for(int cost=0;cost<=1024;cost++)check(ArtifactRules.refund(cost)*2==cost);
        for(int armor=0;armor<=4;armor++){double factor=ArtifactRules.regenFactor(true,armor);check(factor>0&&factor<=.65);}
        check(ArtifactRules.regenFactor(false,0)==1);check(Math.abs(ArtifactRules.regenFactor(false,4)-.6)<1e-8);
        System.out.println("ArtifactRulesTests: "+checks+" checks passed");
    }
}
