package me.TechsCode.UltraPermissions.base.util;

import java.text.DecimalFormat;
import java.util.TreeMap;

public class Number {
   private static final TreeMap<Integer, String> ROMAN_NUMERAL_MAP = new TreeMap<>();
   private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.##");

   public Number() {
      super();
   }

   public static String toRoman(int var0) {
      Integer var1 = ROMAN_NUMERAL_MAP.floorKey(var0);
      if (var1 == null) {
         return String.valueOf(var0);
      } else {
         return var0 == var1 ? ROMAN_NUMERAL_MAP.get(var0) : ROMAN_NUMERAL_MAP.get(var1) + toRoman(var0 - var1);
      }
   }

   public static String format2Decimals(double var0) {
      return DECIMAL_FORMAT.format(var0);
   }

   public static double sum(double var0, double var2) {
      return sum(var0, var2, 2);
   }

   public static double sum(double var0, double var2, int var4) {
      double var5 = Math.pow(10.0, var4);
      return (Math.round(var0 * var5) + Math.round(var2 * var5)) / var5;
   }

   public static double clamp(double var0, double var2, double var4) {
      return var0 < var2 ? var2 : (var0 > var4 ? var4 : var0);
   }

   public static int clamp(int var0, int var1, int var2) {
      return var0 < var1 ? var1 : (var0 > var2 ? var2 : var0);
   }

   public static long clamp(long var0, long var2, long var4) {
      return var0 < var2 ? var2 : (var0 > var4 ? var4 : var0);
   }

   static {
      ROMAN_NUMERAL_MAP.put(1000, "M");
      ROMAN_NUMERAL_MAP.put(900, "CM");
      ROMAN_NUMERAL_MAP.put(500, "D");
      ROMAN_NUMERAL_MAP.put(400, "CD");
      ROMAN_NUMERAL_MAP.put(100, "C");
      ROMAN_NUMERAL_MAP.put(90, "XC");
      ROMAN_NUMERAL_MAP.put(50, "L");
      ROMAN_NUMERAL_MAP.put(40, "XL");
      ROMAN_NUMERAL_MAP.put(10, "X");
      ROMAN_NUMERAL_MAP.put(9, "IX");
      ROMAN_NUMERAL_MAP.put(5, "V");
      ROMAN_NUMERAL_MAP.put(4, "IV");
      ROMAN_NUMERAL_MAP.put(1, "I");
   }
}
