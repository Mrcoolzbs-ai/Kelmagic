package dev.kelmagic.core;
public final class ManaFormat {public static String number(double x){return x==Math.rint(x)?Long.toString((long)x):String.format(java.util.Locale.ROOT,"%.1f",x);}private ManaFormat(){}}
