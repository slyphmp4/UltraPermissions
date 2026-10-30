package me.TechsCode.UltraPermissions.base.item;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public final class Particles {
   public static final double PII = 6.283185307179586;
   public static final double R270 = Math.toRadians(270.0);
   public static final double R90 = 1.5707963267948966;

   private Particles() {
      super();
   }

   public static Optional<XParticle> randomParticle(String... var0) {
      int var1 = randInt(0, var0.length - 1);
      return XParticle.of(var0[var1]);
   }

   public static double random(double var0, double var2) {
      return ThreadLocalRandom.current().nextDouble(var0, var2);
   }

   public static int randInt(int var0, int var1) {
      return ThreadLocalRandom.current().nextInt(var0, var1 + 1);
   }

   public static Color randomColor() {
      ThreadLocalRandom var0 = ThreadLocalRandom.current();
      int var1 = var0.nextInt(0, 256);
      int var2 = var0.nextInt(0, 256);
      int var3 = var0.nextInt(0, 256);
      return Color.fromRGB(var1, var2, var3);
   }

   public static DustOptions randomDust() {
      float var0 = randInt(5, 10) / 10.0F;
      return new DustOptions(randomColor(), var0);
   }

   public static void blackSun(double var0, double var2, double var4, double var6, ParticleDisplay var8) {
      double var9 = 0.0;
      double var11 = 10.0;

      while (var11 > 0.0) {
         var9 += var6;
         circle(var0 + var11, var4 - var9, var8);
         var11 -= var2;
      }
   }

   public static void circle(double var0, double var2, ParticleDisplay var4) {
      circle(var0, var0, 1.0, var2, 0.0, var4);
   }

   public static void circle(double var0, double var2, double var4, double var6, double var8, ParticleDisplay var10) {
      double var11 = 3.141592653589793 / Math.abs(var6);
      if (var8 == 0.0) {
         var8 = 6.283185307179586;
      } else if (var8 == -1.0) {
         var8 = 6.283185307179586 / Math.abs(var4);
      }

      for (double var13 = 0.0; var13 <= var8; var13 += var11) {
         double var15 = var0 * Math.cos(var4 * var13);
         double var17 = var2 * Math.sin(var4 * var13);
         if (var10.isDirectional()) {
            double var19 = Math.atan2(var17, var15);
            double var21 = Math.cos(var4 * var19);
            double var23 = Math.sin(var4 * var19);
            var10.particleDirection(var21, var10.getOffset().getY(), var23);
         }

         var10.spawn(var15, 0.0, var17);
      }
   }

   public static void diamond(double var0, double var2, double var4, ParticleDisplay var6) {
      double var7 = 0.0;
      double var9 = 0.0;

      while (var9 < var4 * 2.0) {
         if (var9 < var4) {
            var7 += var0;
         } else {
            var7 -= var0;
         }

         for (double var11 = -var7; var11 < var7; var11 += var2) {
            var6.spawn(var11, var9, 0.0);
         }

         var9 += var2;
      }
   }

   public static Runnable circularBeam(final double var0, final double var2, final double var4, final double var6, final ParticleDisplay var8) {
      return new Runnable() {
         final double rateDiv = 3.141592653589793 / var2;
         final double radiusDiv = 3.141592653589793 / var4;
         final Vector dir = var8.getLocation().getDirection().normalize().multiply(var6);
         double dynamicRadius = 0.0;

         @Override
         public void run() {
            double var1 = var0 * Math.sin(this.dynamicRadius);

            for (double var3 = 0.0; var3 < 6.283185307179586; var3 += this.rateDiv) {
               double var5 = var1 * Math.sin(var3);
               double var7 = var1 * Math.cos(var3);
               var8.spawn(var5, 0.0, var7);
            }

            this.dynamicRadius = this.dynamicRadius + this.radiusDiv;
            if (this.dynamicRadius > 3.141592653589793) {
               this.dynamicRadius = 0.0;
            }

            var8.getLocation().add(this.dir);
         }
      };
   }

   public static BukkitTask circularBeam(Plugin var0, double var1, double var3, double var5, double var7, ParticleDisplay var9) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, circularBeam(var1, var3, var5, var7, var9), 0L, 1L);
   }

   public static void flower(int var0, double var1, ParticleDisplay var3, Runnable var4) {
      double var5 = 0.0;

      while (var5 < 6.283185307179586) {
         double var7 = var1 * Math.cos(var5);
         double var9 = var1 * Math.sin(var5);
         var3.getLocation().add(var7, 0.0, var9);
         var4.run();
         var3.getLocation().subtract(var7, 0.0, var9);
         var5 += 6.283185307179586 / var0;
      }
   }

   public static void filledCircle(double var0, double var2, double var4, ParticleDisplay var6) {
      double var7 = 0.0;
      double var9 = 0.1;

      while (var9 < var0) {
         if (var9 > var0) {
            var9 = var0;
         }

         var7 += var2 / (var0 / var4);
         circle(var9, var7, var6);
         var9 += var4;
      }
   }

   public static Runnable chaoticDoublePendulum(
      final double var0,
      final double var2,
      final double var4,
      final double var6,
      final double var8,
      final double var10,
      final boolean var12,
      final int var13,
      final ParticleDisplay var14
   ) {
      return new Runnable() {
         double theta = 1.5707963267948966;
         double theta2 = 1.5707963267948966;
         double thetaPrime = 0.0;
         double thetaPrime2 = 0.0;

         @Override
         public void run() {
            int var1 = var13;

            while (var1-- != 0) {
               if (var12) {
                  var14.rotate(0.09519977738150888, 0.07139983303613166, 0.057119866428905326);
               }

               double var2x = var8 + var10;
               double var4x = 2.0 * var2x;
               double var6x = this.theta - this.theta2;
               double var8x = var4x - var10 * Math.cos(2.0 * this.theta - 2.0 * this.theta2);
               double var10x = Math.cos(var6x);
               double var12x = Math.sin(var6x);
               double var14x = this.thetaPrime * this.thetaPrime * var4;
               double var16 = this.thetaPrime2 * this.thetaPrime2 * var6;
               double var18 = -var2 * var4x * Math.sin(this.theta);
               double var20 = -var10 * var2 * Math.sin(this.theta - 2.0 * this.theta2);
               double var22 = -2.0 * var12x * var10;
               double var24 = var16 + var14x * var10x;
               double var26 = var4 * var8x;
               double var28 = (var18 + var20 + var22 * var24) / var26;
               var18 = 2.0 * var12x;
               var20 = var14x * var2x;
               var22 = var2 * var2x * Math.cos(this.theta);
               var24 = var16 * var10 * var10x;
               var26 = var6 * var8x;
               double var30 = var18 * (var20 + var22 + var24) / var26;
               this.thetaPrime += var28;
               this.thetaPrime2 += var30;
               this.theta = this.theta + this.thetaPrime;
               this.theta2 = this.theta2 + this.thetaPrime2;
               double var32 = var0 * Math.sin(this.theta);
               double var34 = var0 * Math.cos(this.theta);
               double var36 = var32 + var0 * Math.sin(this.theta2);
               double var38 = var34 + var0 * Math.cos(this.theta2);
               var14.spawn(var36, var38, 0.0);
            }
         }
      };
   }

   public static BukkitTask chaoticDoublePendulum(
      Plugin var0, double var1, double var3, double var5, double var7, double var9, double var11, boolean var13, int var14, ParticleDisplay var15
   ) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, chaoticDoublePendulum(var1, var3, var5, var7, var9, var11, var13, var14, var15), 0L, 1L);
   }

   public static Runnable magicCircles(final double var0, final double var2, final double var4, final double var6, final ParticleDisplay var8) {
      return new Runnable() {
         final double radiusDiv = 3.141592653589793 / var4;
         final Vector dir = var8.getLocation().getDirection().normalize().multiply(var6);
         double dynamicRadius = var0;

         @Override
         public void run() {
            double var1 = 3.141592653589793 / (var2 * this.dynamicRadius);

            for (double var3 = 0.0; var3 < 6.283185307179586; var3 += var1) {
               double var5 = this.dynamicRadius * Math.sin(var3);
               double var7 = this.dynamicRadius * Math.cos(var3);
               var8.spawn(var5, 0.0, var7);
            }

            this.dynamicRadius = this.dynamicRadius + this.radiusDiv;
            var8.getLocation().add(this.dir);
         }
      };
   }

   public static BukkitTask magicCircles(Plugin var0, double var1, double var3, double var5, double var7, ParticleDisplay var9) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, magicCircles(var1, var3, var5, var7, var9), 0L, 1L);
   }

   public static void infinity(double var0, double var2, ParticleDisplay var4) {
      double var5 = 3.141592653589793 / var2;

      for (double var7 = 0.0; var7 < 6.283185307179586; var7 += var5) {
         double var9 = Math.sin(var7);
         double var11 = Math.pow(var9, 2.0) + 1.0;
         double var13 = var0 * Math.cos(var7);
         double var15 = var13 / var11;
         double var17 = var13 * var9 / var11;
         circle(1.0, var2, var4.cloneWithLocation(var9, var17, var15));
      }
   }

   public static void cone(double var0, double var2, double var4, double var6, ParticleDisplay var8) {
      double var9 = var2 / (var0 / var4);

      for (double var11 = 0.0; var11 < var0; var11 += var4) {
         var2 -= var9;
         if (var2 < 0.0) {
            var2 = 0.0;
         }

         circle(var2, var6 - var11, var8.cloneWithLocation(0.0, var11, 0.0));
      }
   }

   public static void slash(double var0, boolean var2, ParticleDisplay var3) {
      double var4 = var2 ? 1.5707963267948966 : 0.0;
      double var6 = var2 ? R270 : 3.141592653589793;
      ellipse(var4, var6, 0.10471975511965977, var0, var0 + 2.0, var3);
   }

   public static void slash(
      Plugin var0, final double var1, final boolean var3, final Supplier<Double> var4, final Supplier<Double> var5, final ParticleDisplay var6
   ) {
      (new BukkitRunnable() {
         double distanceTraveled = 0.0;

         public void run() {
            Particles.slash((Double)var4.get(), var3, var6);
            double var1x = (Double)var5.get();
            this.distanceTraveled += var1x;
            if (this.distanceTraveled >= var1) {
               this.cancel();
            } else {
               var6.advanceInDirection(var1x);
            }
         }
      }).runTaskTimerAsynchronously(var0, 1L, 1L);
   }

   public static void ellipse(double var0, double var2, double var4, double var6, double var8, ParticleDisplay var10) {
      double var11 = var0;

      while (var11 <= var2) {
         double var13 = var6 * Math.cos(var11);
         double var15 = var8 * Math.sin(var11);
         var10.spawn(var13, 0.0, var15);
         var11 += var4;
      }
   }

   public static BooleanSupplier blackhole(final int var0, final double var1, final double var3, final int var5, final int var6, final ParticleDisplay var7) {
      var7.extra = 0.1;
      return new BooleanSupplier() {
         final double rateDiv = 3.141592653589793 / var3;
         int timer = var6;
         double theta = 0.0;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            for (int var1x = 0; var1x < var0; var1x++) {
               double var2 = 6.283185307179586 * ((double)var1x / var0);
               double var4 = var1 * Math.cos(this.theta + var2);
               double var6x = var1 * Math.sin(this.theta + var2);
               double var8 = Math.atan2(var6x, var4);
               double var10 = -Math.cos(var8);
               double var12 = -Math.sin(var8);
               var7.particleDirection(var10, 0.0, var12);
               var7.spawn(var4, 0.0, var6x);
               if (var5 > 1) {
                  var4 = var1 * Math.cos(-this.theta + var2);
                  var6x = var1 * Math.sin(-this.theta + var2);
                  if (var5 == 2) {
                     var8 = Math.atan2(var6x, var4);
                  } else if (var5 == 3) {
                     var8 = Math.atan2(var4, var6x);
                  } else if (var5 == 4) {
                     Math.atan2(Math.log(var4), Math.log(var6x));
                  }

                  var10 = -Math.cos(var8);
                  var12 = -Math.sin(var8);
                  var7.particleDirection(var10, 0.0, var12);
                  var7.spawn(var4, 0.0, var6x);
               }
            }

            this.theta = this.theta + this.rateDiv;
            if (--this.timer <= 0) {
               this.done = true;
               return false;
            } else {
               return true;
            }
         }
      };
   }

   public static BukkitTask blackhole(Plugin var0, int var1, double var2, double var4, int var6, int var7, ParticleDisplay var8) {
      final BooleanSupplier var9 = blackhole(var1, var2, var4, var6, var7, var8);
      return (new BukkitRunnable() {
         public void run() {
            if (!var9.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(var0, 0L, 1L);
   }

   public static void rainbow(double var0, double var2, double var4, double var6, double var8, ParticleDisplay var10) {
      int[][] var11 = new int[][]{{128, 0, 128}, {75, 0, 130}, {0, 0, 255}, {0, 255, 0}, {255, 255, 0}, {255, 140, 0}, {255, 0, 0}};
      double var12 = var0 * var4;

      for (int var14 = 0; var14 < 7; var14++) {
         int[] var15 = var11[var14];
         var10 = ParticleDisplay.of(XParticle.DUST).withLocation(var10.getLocation()).withColor(new java.awt.Color(var15[0], var15[1], var15[2]), 1.0F);

         for (int var16 = 0; var16 < var6; var16++) {
            double var17 = 3.141592653589793 / (var2 * (var14 + 2));

            for (double var19 = 0.0; var19 <= 3.141592653589793; var19 += var17) {
               double var21 = var0 * Math.cos(var19);
               double var23 = var12 * Math.sin(var19);
               var10.spawn(var21, var23, 0.0);
            }

            var0 += var8;
         }
      }
   }

   public static void crescent(double var0, double var2, ParticleDisplay var4) {
      double var5 = 3.141592653589793 / var2;
      double var7 = Math.toRadians(325.0);

      for (double var9 = Math.toRadians(45.0); var9 <= var7; var9 += var5) {
         double var11 = Math.cos(var9);
         double var13 = Math.sin(var9);
         var4.spawn(var0 * var11, 0.0, var0 * var13);
         double var15 = var0 / 1.3;
         var4.spawn(var15 * var11 + 0.8, 0.0, var15 * var13);
      }
   }

   public static void waveFunction(double var0, double var2, double var4, double var6, ParticleDisplay var8) {
      double var9 = var2 / 2.0;
      boolean var11 = true;
      double var12 = random(var2 / 2.0, var2);
      double var14 = 3.141592653589793 / var6;
      var4 *= 6.283185307179586;

      for (double var16 = 0.0; var16 <= var4; var16 += var14) {
         double var18 = var0 * var16;
         double var20 = Math.sin(var16);
         if (var20 == 1.0) {
            var11 = !var11;
            if (var11) {
               var12 = random(var2 / 2.0, var2);
            } else {
               var12 = random(-var2, -var2 / 2.0);
            }
         }

         var9 += var12;

         for (double var22 = 0.0; var22 <= var4; var22 += var14) {
            double var24 = Math.cos(var22);
            double var26 = var9 * var20 * var24;
            double var28 = var0 * var22;
            var8.spawn(var18, var26, var28);
         }
      }
   }

   public static Runnable vortex(final int var0, double var1, final ParticleDisplay var3) {
      final double var4 = 3.141592653589793 / var1;
      return new Runnable() {
         double theta = 0.0;

         @Override
         public void run() {
            this.theta = this.theta + var4;

            for (int var1x = 0; var1x < var0; var1x++) {
               double var2 = 6.283185307179586 * ((double)var1x / var0);
               double var4x = Math.cos(this.theta + var2);
               double var6 = Math.sin(this.theta + var2);
               double var8 = Math.atan2(var6, var4x);
               double var10 = Math.cos(var8);
               double var12 = Math.sin(var8);
               var3.particleDirection(var10, 0.0, var12);
               var3.spawn(var4x, 0.0, var6);
            }
         }
      };
   }

   public static BukkitTask vortex(Plugin var0, int var1, double var2, ParticleDisplay var4) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, vortex(var1, var2, var4), 0L, 1L);
   }

   public static void cylinder(double var0, double var2, double var4, ParticleDisplay var6) {
      filledCircle(var2, var4, 3.0, var6);
      filledCircle(var2, var4, 3.0, var6.cloneWithLocation(0.0, var0, 0.0));

      for (double var7 = 0.0; var7 < var0; var7 += 0.1) {
         circle(var2, var4, var6.cloneWithLocation(0.0, var7, 0.0));
      }
   }

   public static Runnable moveRotatingAround(
      final double var0, final double var2, final double var4, final double var6, final Runnable var8, final ParticleDisplay... var9
   ) {
      return new Runnable() {
         double rotation = 180.0;

         @Override
         public void run() {
            this.rotation = this.rotation + var0;
            double var1 = Math.toRadians(90.0 + this.rotation);
            double var3 = Math.toRadians(60.0 + this.rotation);
            double var5 = Math.toRadians(30.0 + this.rotation);
            Vector var7 = new Vector(var2 * 3.141592653589793, var4 * 3.141592653589793, var6 * 3.141592653589793);
            if (var2 != 0.0) {
               ParticleDisplay.rotateAround(var7, ParticleDisplay.Axis.X, var1);
            }

            if (var4 != 0.0) {
               ParticleDisplay.rotateAround(var7, ParticleDisplay.Axis.Y, var3);
            }

            if (var6 != 0.0) {
               ParticleDisplay.rotateAround(var7, ParticleDisplay.Axis.Z, var5);
            }

            for (ParticleDisplay var11 : var9) {
               var11.getLocation().add(var7);
            }

            var8.run();

            for (ParticleDisplay var15 : var9) {
               var15.getLocation().subtract(var7);
            }
         }
      };
   }

   public static BukkitTask moveRotatingAround(
      Plugin var0, long var1, double var3, double var5, double var7, double var9, Runnable var11, ParticleDisplay... var12
   ) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, moveRotatingAround(var3, var5, var7, var9, var11, var12), 0L, var1);
   }

   public static Runnable moveAround(
      final double var0, final double var2, final double var4, final double var6, final double var8, final Runnable var10, final ParticleDisplay... var11
   ) {
      return new Runnable() {
         double multiplier = 0.0;
         boolean opposite = false;

         @Override
         public void run() {
            if (this.opposite) {
               this.multiplier = this.multiplier - var0;
            } else {
               this.multiplier = this.multiplier + var0;
            }

            double var1 = this.multiplier * var4;
            double var3 = this.multiplier * var6;
            double var5 = this.multiplier * var8;

            for (ParticleDisplay var10x : var11) {
               var10x.getLocation().add(var1, var3, var5);
            }

            var10.run();

            for (ParticleDisplay var14 : var11) {
               var14.getLocation().subtract(var1, var3, var5);
            }

            if (this.opposite) {
               if (this.multiplier <= 0.0) {
                  this.opposite = false;
               }
            } else if (this.multiplier >= var2) {
               this.opposite = true;
            }
         }
      };
   }

   public static BukkitTask moveAround(
      Plugin var0, long var1, double var3, double var5, double var7, double var9, double var11, Runnable var13, ParticleDisplay... var14
   ) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, moveAround(var3, var5, var7, var9, var11, var13, var14), 0L, var1);
   }

   public static BukkitTask testDisplay(Plugin var0, Runnable var1) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, var1, 0L, 1L);
   }

   public static Runnable rotateAround(
      final double var0, final double var2, final double var4, final double var6, final Runnable var8, final ParticleDisplay... var9
   ) {
      return new Runnable() {
         double rotation = 180.0;

         @Override
         public void run() {
            this.rotation = this.rotation + var0;
            double var1 = Math.toRadians((90.0 + this.rotation) * var2);
            double var3 = Math.toRadians((60.0 + this.rotation) * var4);
            double var5 = Math.toRadians((30.0 + this.rotation) * var6);

            for (ParticleDisplay var10 : var9) {
               var10.rotate(var1, var3, var5);
            }

            var8.run();
         }
      };
   }

   public static BukkitTask rotateAround(Plugin var0, long var1, double var3, double var5, double var7, double var9, Runnable var11, ParticleDisplay... var12) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, rotateAround(var3, var5, var7, var9, var11, var12), 0L, var1);
   }

   public static Runnable guard(final double var0, final double var2, final double var4, final double var6, final Runnable var8, final ParticleDisplay... var9) {
      return new Runnable() {
         double rotation = 180.0;

         @Override
         public void run() {
            this.rotation = this.rotation + var0;
            double var1 = Math.toRadians((90.0 + this.rotation) * var2);
            double var3 = Math.toRadians((60.0 + this.rotation) * var4);
            double var5 = Math.toRadians((30.0 + this.rotation) * var6);
            Vector var7 = new Vector(var2 * 3.141592653589793, var4 * 3.141592653589793, var6 * 3.141592653589793);
            ParticleDisplay.rotateAround(var7, var1, var3, var5);

            for (ParticleDisplay var11 : var9) {
               var11.rotate(var1, var3, var5);
               var11.getLocation().add(var7);
            }

            var8.run();

            for (ParticleDisplay var15 : var9) {
               var15.getLocation().subtract(var7);
            }
         }
      };
   }

   public static BukkitTask guard(Plugin var0, long var1, double var3, double var5, double var7, double var9, Runnable var11, ParticleDisplay... var12) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, guard(var3, var5, var7, var9, var11, var12), 0L, var1);
   }

   public static void sphere(double var0, double var2, ParticleDisplay var4) {
      double var5 = 3.141592653589793 / var2;

      for (double var7 = 0.0; var7 <= 3.141592653589793; var7 += var5) {
         double var9 = var0 * Math.cos(var7);
         double var11 = var0 * Math.sin(var7);

         for (double var13 = 0.0; var13 <= 6.283185307179586; var13 += var5) {
            double var15 = Math.cos(var13) * var11;
            double var17 = Math.sin(var13) * var11;
            if (var4.isDirectional()) {
               double var19 = Math.atan2(var17, var15);
               double var21 = Math.cos(var19);
               double var23 = Math.sin(Math.atan2(var11, var9));
               double var25 = Math.sin(var19);
               var4.particleDirection(var21, var23, var25);
            }

            var4.spawn(var15, var9, var17);
         }
      }
   }

   public static void spikeSphere(double var0, double var2, int var4, double var5, double var7, ParticleDisplay var9) {
      double var10 = 3.141592653589793 / var2;

      for (double var12 = 0.0; var12 <= 3.141592653589793; var12 += var10) {
         double var14 = var0 * Math.cos(var12);
         double var16 = var0 * Math.sin(var12);

         for (double var18 = 0.0; var18 <= 6.283185307179586; var18 += var10) {
            double var20 = Math.cos(var18) * var16;
            double var22 = Math.sin(var18) * var16;
            if (var4 == 0 || randInt(0, var4) == 1) {
               Location var24 = var9.cloneLocation(var20, var14, var22);
               Vector var25 = var24.clone().subtract(var9.getLocation()).toVector().multiply(random(var5, var7));
               Location var26 = var24.clone().add(var25);
               line(var24, var26, 0.1, var9);
            }
         }
      }
   }

   public static void ring(double var0, double var2, double var4, ParticleDisplay var6) {
      double var7 = 3.141592653589793 / var0;
      double var9 = 3.141592653589793 / var4;

      for (double var11 = 0.0; var11 <= 6.283185307179586; var11 += var7) {
         double var13 = Math.cos(var11);
         double var15 = Math.sin(var11);

         for (double var17 = 0.0; var17 <= 6.283185307179586; var17 += var9) {
            double var19 = var2 + var4 * Math.cos(var17);
            double var21 = var19 * var13;
            double var23 = var19 * var15;
            double var25 = var4 * Math.sin(var17);
            var6.spawn(var21, var23, var25);
         }
      }
   }

   public static BooleanSupplier spread(
      final int var0,
      final int var1,
      final Location var2,
      final Location var3,
      final double var4,
      final double var6,
      final double var8,
      final ParticleDisplay var10
   ) {
      return new BooleanSupplier() {
         int count = var0;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            int var1x = var1;

            while (var1x-- != 0) {
               double var2x = Particles.random(-var4, var4);
               double var4x = Particles.random(-var6, var6);
               double var6x = Particles.random(-var8, var8);
               Location var8x = var3.clone().add(var2x, var4x, var6x);
               Particles.line(var2, var8x, 0.1, var10);
            }

            if (this.count-- <= 0) {
               this.done = true;
               return false;
            } else {
               return true;
            }
         }
      };
   }

   public static BukkitTask spread(Plugin var0, int var1, int var2, Location var3, Location var4, double var5, double var7, double var9, ParticleDisplay var11) {
      final BooleanSupplier var12 = spread(var1, var2, var3, var4, var5, var7, var9, var11);
      return (new BukkitRunnable() {
         public void run() {
            if (!var12.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(var0, 0L, 1L);
   }

   public static void heart(double var0, double var2, double var4, double var6, double var8, ParticleDisplay var10) {
      double var11 = 0.0;

      while (var11 <= 6.283185307179586) {
         double var13 = var11 / var0;
         double var15 = Math.cos(var13);
         double var17 = Math.sin(var13);
         double var19 = Math.pow(Math.abs(Math.sin(2.0 * var2 * var13)) + var4 * Math.abs(Math.sin(var2 * var13)), 1.0 / var6);
         double var21 = var19 * (var17 + var15);
         double var23 = var19 * (var15 - var17);
         var10.spawn(0.0, var21, var23);
         var11 += 3.141592653589793 / var8;
      }
   }

   public static Runnable atomic(final int var0, final double var1, final double var3, final ParticleDisplay var5) {
      return new Runnable() {
         final double rateDiv = 3.141592653589793 / var3;
         final double dist = 3.141592653589793 / var0;
         double theta = 0.0;

         @Override
         public void run() {
            int var1x = var0;
            this.theta = this.theta + this.rateDiv;
            double var2 = var1 * Math.cos(this.theta);
            double var4 = var1 * Math.sin(this.theta);

            for (double var6 = 0.0; var1x > 0; var6 += this.dist) {
               var5.rotate(ParticleDisplay.Rotation.of(var6, ParticleDisplay.Axis.Z));
               var5.spawn(var2, 0.0, var4);
               var1x--;
            }
         }
      };
   }

   public static BukkitTask atomic(Plugin var0, int var1, double var2, double var4, ParticleDisplay var6) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, atomic(var1, var2, var4, var6), 0L, 1L);
   }

   public static BooleanSupplier helix(
      final int var0,
      final double var1,
      final double var3,
      final double var5,
      final double var7,
      final double var9,
      final double var11,
      final boolean var13,
      final boolean var14,
      final ParticleDisplay var15
   ) {
      return new BooleanSupplier() {
         final double distanceBetweenEachCirclePoints = 6.283185307179586 / var0;
         final double radiusDiv = var1 / (var7 / var3);
         final double radiusDiv2 = var13 && var14 ? this.radiusDiv * 2.0 : this.radiusDiv;
         double dynamicRadius = var14 ? 0.0 : var1;
         boolean center = !var14;
         final double calculatedRotRate = this.distanceBetweenEachCirclePoints / var11;
         double rotation = 0.0;
         double currentDistance = 0.0;

         @Override
         public boolean getAsBoolean() {
            if (this.currentDistance >= var7) {
               return false;
            }

            if (!this.center) {
               this.dynamicRadius = this.dynamicRadius + this.radiusDiv2;
               if (this.dynamicRadius >= var1) {
                  this.center = true;
               }
            } else if (var13) {
               this.dynamicRadius = this.dynamicRadius - this.radiusDiv2;
            }

            for (double var1x = 0.0; var1x < var0; var1x++) {
               double var3x = var1x * this.distanceBetweenEachCirclePoints * var5 + this.rotation;
               double var5x = this.dynamicRadius * Math.cos(var3x);
               double var7x = this.dynamicRadius * Math.sin(var3x);
               var15.spawn(var5x, 0.0, var7x);
            }

            this.currentDistance = this.currentDistance + var9;
            if (this.currentDistance < var7) {
               var15.advanceInDirection(var9);
            } else {
               var15.advanceInDirection(var9 - (this.currentDistance - var7));
            }

            this.rotation = this.rotation + this.calculatedRotRate;
            return true;
         }
      };
   }

   public static BukkitTask helix(
      Plugin var0,
      int var1,
      double var2,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      boolean var14,
      boolean var15,
      ParticleDisplay var16
   ) {
      final BooleanSupplier var17 = helix(var1, var2, var4, var6, var8, var10, var12, var14, var15, var16);
      return (new BukkitRunnable() {
         public void run() {
            if (!var17.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(var0, 0L, 1L);
   }

   public static void lightning(
      Location var0,
      Vector var1,
      int var2,
      int var3,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      ParticleDisplay var18
   ) {
      ThreadLocalRandom var19 = ThreadLocalRandom.current();
      if (var2 > 0) {
         boolean var20 = true;

         while (var19.nextDouble() < var14 || var20) {
            Vector var21 = new Vector(var19.nextDouble(-var4, var4), var19.nextDouble(-var4, var4), var19.nextDouble(-var4, var4))
               .normalize()
               .multiply(var19.nextDouble(-var4, var4) * var6);
            Vector var22 = var0.clone().toVector().add(var1.clone().multiply(var10)).add(var21);
            Location var23 = var22.toLocation(var0.getWorld());
            if (var23.distance(var0) <= var10) {
               var20 = true;
            } else {
               var20 = false;
               int var24 = (int)(var0.distance(var23) / 0.1);
               Vector var25 = var22.clone().subtract(var0.toVector()).normalize().multiply(0.1);

               for (int var26 = 0; var26 < var24; var26++) {
                  Location var27 = var0.clone().add(var25.clone().multiply(var26));
                  var18.spawn(var27);
               }

               lightning(var23.clone(), var1, var2 - 1, var3 - 1, var4, var6 * var8, var8, var10 * var12, var12, var14 * var16, var16, var18);
               if (var3 <= 0) {
                  break;
               }
            }
         }
      }
   }

   public static void dna(double var0, double var2, double var4, int var6, int var7, ParticleDisplay var8, ParticleDisplay var9) {
      int var10 = 0;
      double var11 = 0.0;

      while (var11 <= var6) {
         var10++;
         double var13 = var0 * Math.cos(var4 * var11);
         double var15 = var0 * Math.sin(var4 * var11);
         Location var17 = var8.getLocation().clone().add(var13, var11, var15);
         var8.spawn(var13, var11, var15);
         Location var18 = var8.getLocation().clone().subtract(var13, -var11, var15);
         var8.spawn(-var13, var11, -var15);
         if (var10 >= var7) {
            var10 = 0;
            line(var17, var18, var2 * 2.0, var9);
         }

         var11 += var2;
      }
   }

   public static BooleanSupplier dnaReplication(
      final double var0, final double var2, final int var4, final double var5, final int var7, final int var8, final ParticleDisplay var9
   ) {
      final ParticleDisplay var10 = ParticleDisplay.of(XParticle.DUST).withColor(java.awt.Color.BLUE, 1.0F);
      final ParticleDisplay var11 = ParticleDisplay.of(XParticle.DUST).withColor(java.awt.Color.YELLOW, 1.0F);
      final ParticleDisplay var12 = ParticleDisplay.of(XParticle.DUST).withColor(java.awt.Color.GREEN, 1.0F);
      final ParticleDisplay var13 = ParticleDisplay.of(XParticle.DUST).withColor(java.awt.Color.RED, 1.0F);
      return new BooleanSupplier() {
         double y = 0.0;
         int nucleotideDist = 0;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            int var1 = var4;

            while (var1-- != 0) {
               this.y = this.y + var2;
               this.nucleotideDist++;
               double var2x = var0 * Math.cos(var5 * this.y);
               double var4x = var0 * Math.sin(var5 * this.y);
               Location var6 = var9.getLocation().clone().add(var2x, this.y, var4x);
               Particles.circle(0.1, 10.0, var9.cloneWithLocation(var2x, this.y, var4x));
               Location var7x = var9.getLocation().clone().subtract(var2x, -this.y, var4x);
               Particles.circle(0.1, 10.0, var9.cloneWithLocation(-var2x, this.y, -var4x));
               Location var8x = var6.toVector().midpoint(var7x.toVector()).toLocation(var6.getWorld());
               if (this.nucleotideDist >= var8) {
                  this.nucleotideDist = 0;
                  if (Particles.randInt(0, 1) == 1) {
                     Particles.line(var6, var8x, var2 - 0.1, var10);
                     Particles.line(var7x, var8x, var2 - 0.1, var11);
                  } else {
                     Particles.line(var6, var8x, var2 - 0.1, var13);
                     Particles.line(var7x, var8x, var2 - 0.1, var12);
                  }
               }

               if (this.y > var7) {
                  this.done = true;
                  return false;
               }
            }

            return true;
         }
      };
   }

   public static BukkitTask dnaReplication(Plugin var0, double var1, double var3, int var5, double var6, int var8, int var9, ParticleDisplay var10) {
      final BooleanSupplier var11 = dnaReplication(var1, var3, var5, var6, var8, var9, var10);
      return (new BukkitRunnable() {
         public void run() {
            if (!var11.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(var0, 0L, 1L);
   }

   public static void drawLine(Player var0, double var1, double var3, ParticleDisplay var5) {
      Location var6 = var0.getEyeLocation();
      line(var6, var6.clone().add(var6.getDirection().multiply(var1)), var3, var5);
   }

   public static Runnable cloud(ParticleDisplay var0, ParticleDisplay var1) {
      return () -> {
         var0.spawn();
         var1.spawn();
      };
   }

   public static BukkitTask cloud(Plugin var0, ParticleDisplay var1, ParticleDisplay var2) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(var0, cloud(var1, var2), 0L, 1L);
   }

   public static void line(Location var0, Location var1, double var2, ParticleDisplay var4) {
      var2 = Math.abs(var2);
      double var5 = var1.getX() - var0.getX();
      double var7 = var1.getY() - var0.getY();
      double var9 = var1.getZ() - var0.getZ();
      double var11 = Math.sqrt(NumberConversions.square(var5) + NumberConversions.square(var7) + NumberConversions.square(var9));
      var5 /= var11;
      var7 /= var11;
      var9 /= var11;
      ParticleDisplay var13 = var4.copy();
      var13.withLocation(var0);

      for (double var14 = 0.0; var14 < var11; var14 += var2) {
         if (var14 > var11) {
            var14 = var11;
         }

         var13.spawn(var5 * var14, var7 * var14, var9 * var14);
      }
   }

   public static void rectangle(Location var0, Location var1, double var2, ParticleDisplay var4) {
      var4.withLocation(var0);
      double var5 = Math.max(var0.getX(), var1.getX());
      double var7 = Math.min(var0.getX(), var1.getX());
      double var9 = Math.max(var0.getY(), var1.getY());
      double var11 = Math.min(var0.getY(), var1.getY());
      double var13 = var7;

      while (var13 <= var5) {
         for (double var15 = var11; var15 <= var9; var15 += var2) {
            var4.spawn(var13 - var7, var15 - var11, 0.0);
         }

         var13 += var2;
      }
   }

   public static void cage(Location var0, Location var1, double var2, double var4, ParticleDisplay var6) {
      double var7 = Math.max(var0.getX(), var1.getX());
      double var9 = Math.min(var0.getX(), var1.getX());
      double var11 = Math.max(var0.getZ(), var1.getZ());
      double var13 = Math.min(var0.getZ(), var1.getZ());
      double var15 = 0.0;
      double var17 = var9;

      while (var17 <= var7) {
         for (double var19 = var13; var19 <= var11; var19 += var2) {
            Location var21 = var6.spawn(var17 - var9, 0.0, var19 - var13);
            Location var22 = var6.spawn(var17 - var9, 3.0, var19 - var13);
            if (var17 == var9 || var17 + var2 > var7 || var19 == var13 || var19 + var2 > var11) {
               if (++var15 >= var4) {
                  var15 = 0.0;
                  line(var21, var22, var2, var6);
               }
            }
         }

         var17 += var2;
      }
   }

   public static void filledCube(Location var0, Location var1, double var2, ParticleDisplay var4) {
      var4.withLocation(var0);
      double var5 = Math.max(var0.getX(), var1.getX());
      double var7 = Math.min(var0.getX(), var1.getX());
      double var9 = Math.max(var0.getY(), var1.getY());
      double var11 = Math.min(var0.getY(), var1.getY());
      double var13 = Math.max(var0.getZ(), var1.getZ());
      double var15 = Math.min(var0.getZ(), var1.getZ());
      double var17 = var7;

      while (var17 <= var5) {
         for (double var19 = var11; var19 <= var9; var19 += var2) {
            for (double var21 = var15; var21 <= var13; var21 += var2) {
               var4.spawn(var17 - var7, var19 - var11, var21 - var15);
            }
         }

         var17 += var2;
      }
   }

   public static void cube(Location var0, Location var1, double var2, ParticleDisplay var4) {
      var4.withLocation(var0);
      double var5 = Math.max(var0.getX(), var1.getX());
      double var7 = Math.min(var0.getX(), var1.getX());
      double var9 = Math.max(var0.getY(), var1.getY());
      double var11 = Math.min(var0.getY(), var1.getY());
      double var13 = Math.max(var0.getZ(), var1.getZ());
      double var15 = Math.min(var0.getZ(), var1.getZ());
      double var17 = var7;

      while (var17 <= var5) {
         for (double var19 = var11; var19 <= var9; var19 += var2) {
            for (double var21 = var15; var21 <= var13; var21 += var2) {
               if (var19 == var11 || var19 + var2 > var9 || var17 == var7 || var17 + var2 > var5 || var21 == var15 || var21 + var2 > var13) {
                  var4.spawn(var17 - var7, var19 - var11, var21 - var15);
               }
            }
         }

         var17 += var2;
      }
   }

   public static void structuredCube(Location var0, Location var1, double var2, ParticleDisplay var4) {
      var4.withLocation(var0);
      double var5 = Math.max(var0.getX(), var1.getX());
      double var7 = Math.min(var0.getX(), var1.getX());
      double var9 = Math.max(var0.getY(), var1.getY());
      double var11 = Math.min(var0.getY(), var1.getY());
      double var13 = Math.max(var0.getZ(), var1.getZ());
      double var15 = Math.min(var0.getZ(), var1.getZ());
      double var17 = var7;

      while (var17 <= var5) {
         for (double var19 = var11; var19 <= var9; var19 += var2) {
            for (double var21 = var15; var21 <= var13; var21 += var2) {
               int var23 = 0;
               if (var17 == var7 || var17 + var2 > var5) {
                  var23++;
               }

               if (var19 == var11 || var19 + var2 > var9) {
                  var23++;
               }

               if (var21 == var15 || var21 + var2 > var13) {
                  var23++;
               }

               if (var23 >= 2) {
                  var4.spawn(var17 - var7, var19 - var11, var21 - var15);
               }
            }
         }

         var17 += var2;
      }
   }

   public static void hypercube(Location var0, Location var1, double var2, double var4, int var6, ParticleDisplay var7) {
      ArrayList var8 = null;

      for (int var9 = 0; var9 < var6 + 1; var9++) {
         ArrayList var10 = new ArrayList(8);
         Location var11 = var0.clone().subtract(var9 * var4, var9 * var4, var9 * var4);
         Location var12 = var1.clone().add(var9 * var4, var9 * var4, var9 * var4);
         var7.withLocation(var11);
         double var13 = Math.max(var11.getX(), var12.getX());
         double var15 = Math.min(var11.getX(), var12.getX());
         double var17 = Math.max(var11.getY(), var12.getY());
         double var19 = Math.min(var11.getY(), var12.getY());
         double var21 = Math.max(var11.getZ(), var12.getZ());
         double var23 = Math.min(var11.getZ(), var12.getZ());
         var10.add(new Location(var11.getWorld(), var13, var17, var21));
         var10.add(new Location(var11.getWorld(), var15, var19, var23));
         var10.add(new Location(var11.getWorld(), var13, var19, var21));
         var10.add(new Location(var11.getWorld(), var15, var17, var23));
         var10.add(new Location(var11.getWorld(), var15, var19, var21));
         var10.add(new Location(var11.getWorld(), var13, var19, var23));
         var10.add(new Location(var11.getWorld(), var13, var17, var23));
         var10.add(new Location(var11.getWorld(), var15, var17, var21));
         if (var8 != null) {
            for (int var25 = 0; var25 < 8; var25++) {
               Location var26 = (Location)var10.get(var25);
               Location var27 = (Location)var8.get(var25);
               line(var27, var26, var2, var7);
            }
         }

         var8 = var10;

         for (double var32 = var15; var32 <= var13; var32 += var2) {
            for (double var33 = var19; var33 <= var17; var33 += var2) {
               for (double var29 = var23; var29 <= var21; var29 += var2) {
                  int var31 = 0;
                  if (var32 == var15 || var32 + var2 > var13) {
                     var31++;
                  }

                  if (var33 == var19 || var33 + var2 > var17) {
                     var31++;
                  }

                  if (var29 == var23 || var29 + var2 > var21) {
                     var31++;
                  }

                  if (var31 >= 2) {
                     var7.spawn(var32 - var15, var33 - var19, var29 - var23);
                  }
               }
            }
         }
      }
   }

   public static BooleanSupplier tesseract(final double var0, final double var2, final double var4, final long var6, final ParticleDisplay var8) {
      final double[][] var9 = new double[][]{
         {-1.0, -1.0, -1.0, 1.0},
         {1.0, -1.0, -1.0, 1.0},
         {1.0, 1.0, -1.0, 1.0},
         {-1.0, 1.0, -1.0, 1.0},
         {-1.0, -1.0, 1.0, 1.0},
         {1.0, -1.0, 1.0, 1.0},
         {1.0, 1.0, 1.0, 1.0},
         {-1.0, 1.0, 1.0, 1.0},
         {-1.0, -1.0, -1.0, -1.0},
         {1.0, -1.0, -1.0, -1.0},
         {1.0, 1.0, -1.0, -1.0},
         {-1.0, 1.0, -1.0, -1.0},
         {-1.0, -1.0, 1.0, -1.0},
         {1.0, -1.0, 1.0, -1.0},
         {1.0, 1.0, 1.0, -1.0},
         {-1.0, 1.0, 1.0, -1.0}
      };
      final ArrayList<int[]> var10 = new ArrayList<>();
      byte var11 = 1;

      for (int var12 = 0; var12 <= var11; var12++) {
         int var13 = 8 * var12;

         for (int var14 = var13; var14 < var13 + 4; var14++) {
            var10.add(new int[]{var14, (var14 + 1) % 4 + var13});
            var10.add(new int[]{var14 + 4, (var14 + 1) % 4 + 4 + var13});
            var10.add(new int[]{var14, var14 + 4});
         }
      }

      for (int var15 = 0; var15 < (var11 + 1) * 4; var15++) {
         var10.add(new int[]{var15, var15 + 8});
      }

      return new BooleanSupplier() {
         double angle = 0.0;
         long repeat = 0L;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            double var1 = Math.cos(this.angle);
            double var3 = Math.sin(this.angle);
            double[][] var5 = new double[][]{{var1, -var3, 0.0, 0.0}, {var3, var1, 0.0, 0.0}, {0.0, 0.0, 1.0, 0.0}, {0.0, 0.0, 0.0, 1.0}};
            double[][] var6x = new double[][]{{1.0, 0.0, 0.0, 0.0}, {0.0, 1.0, 0.0, 0.0}, {0.0, 0.0, var1, -var3}, {0.0, 0.0, var3, var1}};
            double[][] var7 = new double[var9.length][4];

            for (int var8x = 0; var8x < var9.length; var8x++) {
               double[] var9x = var9[var8x];
               double[] var10x = Particles.matrix(var5, var9x);
               var10x = Particles.matrix(var6x, var10x);
               byte var11x = 2;
               double var12 = 1.0 / (var11x - var10x[3]);
               double[][] var14 = new double[][]{{var12, 0.0, 0.0, 0.0}, {0.0, var12, 0.0, 0.0}, {0.0, 0.0, var12, 0.0}};
               double[] var15 = Particles.matrix(var14, var10x);

               for (int var16 = 0; var16 < var15.length; var16++) {
                  var15[var16] *= var0;
               }

               var7[var8x] = var15;
               var8.spawn(var15[0], var15[1], var15[2]);
            }

            for (int[] var18 : var10) {
               double[] var20 = var7[var18[0]];
               double[] var21 = var7[var18[1]];
               Location var22 = var8.cloneLocation(var20[0], var20[1], var20[2]);
               Location var13 = var8.cloneLocation(var21[0], var21[1], var21[2]);
               Particles.line(var22, var13, var2, var8);
            }

            if (++this.repeat > var6) {
               this.done = true;
               return false;
            } else {
               this.angle = this.angle + var4;
               return true;
            }
         }
      };
   }

   public static BukkitTask tesseract(Plugin var0, double var1, double var3, double var5, long var7, ParticleDisplay var9) {
      final BooleanSupplier var10 = tesseract(var1, var3, var5, var7, var9);
      return (new BukkitRunnable() {
         public void run() {
            if (!var10.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(var0, 0L, 1L);
   }

   private static double[] matrix(double[][] var0, double[] var1) {
      double[][] var2 = new double[4][1];
      var2[0][0] = var1[0];
      var2[1][0] = var1[1];
      var2[2][0] = var1[2];
      var2[3][0] = var1[3];
      int var3 = var0[0].length;
      int var4 = var0.length;
      int var5 = var2[0].length;
      int var6 = var2.length;
      double[][] var7 = new double[var4][var6];

      for (int var8 = 0; var8 < var4; var8++) {
         for (int var9 = 0; var9 < var5; var9++) {
            float var10 = 0.0F;

            for (int var11 = 0; var11 < var3; var11++) {
               var10 += (float)(var0[var8][var11] * var2[var11][var9]);
            }

            var7[var8][var9] = var10;
         }
      }

      double[] var12 = new double[]{var7[0][0], var7[1][0], var7[2][0], 0.0};
      if (var7.length > 3) {
         var12[3] = var7[3][0];
      }

      return var12;
   }

   public static void mandelbrot(double var0, double var2, double var4, double var6, double var8, int var10, ParticleDisplay var11) {
      double var12 = -var0;

      while (var12 < var0) {
         for (double var14 = -var0; var14 < var0; var14 += var4) {
            double var16 = 0.0;
            double var18 = 0.0;
            double var20 = (var14 - var6) / var2;
            double var22 = (var12 - var8) / var2;

            int var24;
            for (var24 = var10; var18 * var18 + var16 * var16 <= 4.0 && var24 > 0; var24--) {
               double var25 = var18 * var18 - var16 * var16 + var20;
               var16 = 2.0 * var18 * var16 + var22;
               var18 = var25;
            }

            if (var24 == 0) {
               var11.spawn(var14, var12, 0.0);
            }
         }

         var12 += var4;
      }
   }

   public static void julia(double var0, double var2, int var4, double var5, double var7, ParticleDisplay var9) {
      double var10 = -0.7;
      double var12 = 0.27015;

      for (double var14 = -var0; var14 < var0; var14 += 0.1) {
         for (double var16 = -var0; var16 < var0; var16 += 0.1) {
            double var18 = 1.5 * (var0 - var0 / 2.0) / (0.5 * var2 * var0) + var5;
            double var20 = (var16 - var0 / 2.0) / (0.5 * var2 * var0) + var7;

            int var22;
            for (var22 = var4; var18 * var18 + var20 * var20 < 4.0 && var22 > 0; var22--) {
               double var23 = var18 * var18 - var20 * var20 + var10;
               var20 = 2.0 * var18 * var20 + var12;
               var18 = var23;
            }

            java.awt.Color var25 = new java.awt.Color((var22 << 21) + (var22 << 10) + var22 * 8);
            var9.withColor(var25, 0.8F).spawn(var14, var16, 0.0);
         }
      }
   }

   public static List<BooleanSupplier> star(
      final int var0,
      int var1,
      double var2,
      final double var4,
      final double var6,
      final double var8,
      final boolean var10,
      final int var11,
      final ParticleDisplay var12
   ) {
      final double var13 = 6.283185307179586 / var0;
      final double var15 = 3.141592653589793 / var2;
      final ThreadLocalRandom var17 = var10 ? null : ThreadLocalRandom.current();
      ArrayList var18 = new ArrayList();

      for (int var19 = 0; var19 < var1 * 2; var19++) {
         final double var20 = var19 * 3.141592653589793 / var1;
         var18.add(new BooleanSupplier() {
            double vein = 0.0;
            double theta = 0.0;
            boolean done = false;

            @Override
            public boolean getAsBoolean() {
               if (this.done) {
                  return false;
               }

               int var1x = var11;

               while (var1x-- != 0) {
                  this.theta = this.theta + var15;
                  double var2x = (var10 ? this.vein : var17.nextDouble(0.0, var8)) * var4;
                  if (var10) {
                     this.vein = this.vein + var8;
                  }

                  Vector var4x = new Vector(Math.cos(this.theta), 0.0, Math.sin(this.theta));
                  var4x.multiply((var4 - var2x) * var6 / var4);
                  var4x.setY(var6 + var2x);
                  ParticleDisplay.rotateAround(var4x, ParticleDisplay.Axis.X, var20);

                  for (int var5 = 0; var5 < var0; var5++) {
                     ParticleDisplay.rotateAround(var4x, ParticleDisplay.Axis.Y, var13);
                     var12.spawn(var4x);
                  }
               }

               if (this.theta >= 6.283185307179586) {
                  this.done = true;
                  return false;
               } else {
                  return true;
               }
            }
         });
      }

      return var18;
   }

   public static List<BukkitTask> star(
      Plugin var0, int var1, int var2, double var3, double var5, double var7, double var9, boolean var11, int var12, ParticleDisplay var13
   ) {
      ArrayList var14 = new ArrayList();

      for (final BooleanSupplier var16 : star(var1, var2, var3, var5, var7, var9, var11, var12, var13)) {
         var14.add((new BukkitRunnable() {
            public void run() {
               if (!var16.getAsBoolean()) {
                  this.cancel();
               }
            }
         }).runTaskTimerAsynchronously(var0, 0L, 1L));
      }

      return var14;
   }

   public static void eye(double var0, double var2, double var4, double var6, ParticleDisplay var8) {
      double var9 = 3.141592653589793 / var4;
      double var11 = 3.141592653589793 / var6;
      double var13 = 0.0;

      for (double var15 = 0.0; var15 < var11; var15 += var9) {
         double var17 = var0 * Math.sin(var6 * var15);
         double var19 = var2 * Math.sin(var6 * -var15);
         var8.spawn(var13, var17, 0.0);
         var8.spawn(var13, var19, 0.0);
         var13 += 0.1;
      }
   }

   public static void illuminati(double var0, double var2, ParticleDisplay var4) {
      polygon(3, 1, var0, 1.0 / (var0 * 30.0), 0.0, var4);
      eye(var0 / 4.0, var0 / 4.0, 30.0, var2, var4.cloneWithLocation(0.3, 0.0, var0 / 1.8).rotate(1.5707963267948966, 1.5707963267948966, 0.0));
      circle(var0 / 5.0, var0 * 5.0, var4.cloneWithLocation(0.3, 0.0, 0.0));
   }

   public static void polygon(int var0, int var1, double var2, double var4, double var6, ParticleDisplay var8) {
      for (int var9 = 0; var9 < var0; var9++) {
         double var10 = Math.toRadians(360.0 / var0 * var9);
         double var12 = Math.toRadians(360.0 / var0 * (var9 + var1));
         double var14 = Math.cos(var10) * var2;
         double var16 = Math.sin(var10) * var2;
         double var18 = Math.cos(var12) * var2;
         double var20 = Math.sin(var12) * var2;
         double var22 = var18 - var14;
         double var24 = var20 - var16;
         double var26 = 0.0;

         while (var26 < 1.0 + var6) {
            double var28 = var14 + var22 * var26;
            double var30 = var16 + var24 * var26;
            var8.spawn(var28, 0.0, var30);
            var26 += var4;
         }
      }
   }

   public static void neopaganPentagram(double var0, double var2, double var4, ParticleDisplay var6, ParticleDisplay var7) {
      polygon(5, 2, var0, var2, var4, var6);
      circle(var0 + 0.5, var2 * 1000.0, var7);
   }

   public static void atom(int var0, double var1, double var3, ParticleDisplay var5, ParticleDisplay var6) {
      double var7 = 3.141592653589793 / var0;

      for (double var9 = 0.0; var0 > 0; var9 += var7) {
         var5.rotate(ParticleDisplay.Rotation.of(var9, ParticleDisplay.Axis.Z));
         circle(var1, var3, var5);
         var0--;
      }

      sphere(var1 / 3.0, var3 / 2.0, var6);
   }

   public static BooleanSupplier meguminExplosion(final double var0, final ParticleDisplay var2) {
      final BooleanSupplier var3 = spread(30, 2, var2.getLocation(), var2.getLocation().clone().add(0.0, 10.0, 0.0), 5.0, 5.0, 5.0, var2);
      return new BooleanSupplier() {
         boolean first = true;

         @Override
         public boolean getAsBoolean() {
            if (this.first) {
               this.first = false;
               Particles.polygon(10, 4, var0, 0.02, 0.3, var2);
               Particles.polygon(10, 3, var0 / (var0 - 1.0), 0.5, 0.0, var2);
               Particles.circle(var0, 40.0, var2);
            }

            return var3.getAsBoolean();
         }
      };
   }

   public static BukkitTask meguminExplosion(Plugin var0, double var1, ParticleDisplay var3) {
      final BooleanSupplier var4 = meguminExplosion(var1, var3);
      return (new BukkitRunnable() {
         public void run() {
            if (!var4.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(var0, 0L, 1L);
   }

   public static BooleanSupplier explosionWave(final double var0, final ParticleDisplay var2, final ParticleDisplay var3) {
      return new BooleanSupplier() {
         static final double addition = 0.3141592653589793;
         final double rateDiv = 3.141592653589793 / var0;
         double times = 0.7853981633974483;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            this.times += 0.3141592653589793;
            double var1 = 0.0;

            while (var1 <= 6.283185307179586) {
               double var3x = this.times * Math.cos(var1);
               double var5 = 2.0 * Math.exp(-0.1 * this.times) * Math.sin(this.times) + 1.5;
               double var7 = this.times * Math.sin(var1);
               var2.spawn(var3x, var5, var7);
               var1 += 0.04908738521234052;
               var3x = this.times * Math.cos(var1);
               var7 = this.times * Math.sin(var1);
               var3.spawn(var3x, var5, var7);
               var1 += this.rateDiv;
            }

            if (this.times > 20.0) {
               this.done = true;
               return false;
            } else {
               return true;
            }
         }
      };
   }

   public static BukkitTask explosionWave(Plugin var0, double var1, ParticleDisplay var3, ParticleDisplay var4) {
      final BooleanSupplier var5 = explosionWave(var1, var3, var4);
      return (new BukkitRunnable() {
         public void run() {
            if (!var5.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(var0, 0L, 1L);
   }

   private static BufferedImage getImage(Path var0) {
      if (!Files.exists(var0)) {
         return null;
      }

      try {
         return ImageIO.read(Files.newInputStream(var0, StandardOpenOption.READ));
      } catch (IOException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   private static CompletableFuture<BufferedImage> getScaledImage(Path var0, int var1, int var2) {
      return CompletableFuture.supplyAsync(() -> {
         BufferedImage var3 = getImage(var0);
         if (var3 == null) {
            return null;
         }

         int var4 = var2;
         int var5 = var1;
         if (var3.getWidth() > var3.getHeight()) {
            var4 = var1 * var3.getHeight() / var3.getWidth();
         } else {
            var5 = var2 * var3.getWidth() / var3.getHeight();
         }

         BufferedImage var6 = new BufferedImage(var1, var2, 2);
         Graphics2D var7 = var6.createGraphics();
         var7.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         var7.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var7.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var7.drawImage(var3, 0, 0, var5, var4, null);
         var7.dispose();
         return var6;
      });
   }

   public static CompletableFuture<Map<double[], Color>> renderImage(Path var0, int var1, int var2, double var3) {
      return getScaledImage(var0, var1, var2).thenCompose(var4 -> renderImage(var4, var1, var2, var3));
   }

   public static CompletableFuture<Map<double[], Color>> renderImage(BufferedImage var0, int var1, int var2, double var3) {
      return CompletableFuture.supplyAsync(() -> {
         if (var0 == null) {
            return null;
         }

         int var3x = var0.getWidth();
         int var4 = var0.getHeight();
         double var5 = var3x / 2.0;
         double var7 = var4 / 2.0;
         HashMap var9 = new HashMap();

         for (int var10 = 0; var10 < var4; var10++) {
            for (int var11 = 0; var11 < var3x; var11++) {
               int var12 = var0.getRGB(var11, var10);
               if (var12 >> 24 != 0) {
                  java.awt.Color var13 = new java.awt.Color(var12);
                  int var14 = var13.getRed();
                  int var15 = var13.getGreen();
                  int var16 = var13.getBlue();
                  double[] var17 = new double[]{(var11 - var5) * var3, (var10 - var7) * var3};
                  Color var18 = Color.fromRGB(var14, var15, var16);
                  var9.put(var17, var18);
               }
            }
         }

         return var9;
      });
   }

   public static BooleanSupplier displayRenderedImage(
      final Map<double[], Color> var0, final Callable<Location> var1, final int var2, final int var3, final int var4, final float var5
   ) {
      return new BooleanSupplier() {
         int times = var2;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            try {
               Particles.displayRenderedImage(var0, (Location)var1.call(), var3, var4, var5);
            } catch (Exception var2x) {
               var2x.printStackTrace();
            }

            if (this.times-- <= 0) {
               this.done = true;
               return false;
            } else {
               return true;
            }
         }
      };
   }

   public static BukkitTask displayRenderedImage(
      Plugin var0, Map<double[], Color> var1, Callable<Location> var2, int var3, long var4, int var6, int var7, float var8
   ) {
      final BooleanSupplier var9 = displayRenderedImage(var1, var2, var3, var6, var7, var8);
      return (new BukkitRunnable() {
         public void run() {
            if (!var9.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(var0, 0L, var4);
   }

   public static void displayRenderedImage(Map<double[], Color> var0, Location var1, int var2, int var3, float var4) {
      World var5 = var1.getWorld();
      double var7 = var1.getYaw();
      BlockFace var6;
      if (var7 >= 135.0 || var7 < -135.0) {
         var6 = BlockFace.NORTH;
      } else if (var7 >= -135.0 && var7 < -45.0) {
         var6 = BlockFace.EAST;
      } else if (var7 >= -45.0 && var7 < 45.0) {
         var6 = BlockFace.SOUTH;
      } else {
         if (!(var7 >= 45.0) || !(var7 < 135.0)) {
            throw new IllegalArgumentException("Unknown rotation yaw: " + var7);
         }

         var6 = BlockFace.WEST;
      }

      for (Entry var10 : var0.entrySet()) {
         DustOptions var11 = new DustOptions((Color)var10.getValue(), var4);
         double[] var12 = (double[])var10.getKey();
         double var13;
         double var15;
         double var17;
         switch (var6) {
            case NORTH:
               var13 = var1.getX() + var12[0];
               var15 = var1.getY() - var12[1];
               var17 = var1.getZ();
               break;
            case EAST:
               var13 = var1.getX();
               var15 = var1.getY() - var12[1];
               var17 = var1.getZ() + var12[0];
               break;
            case SOUTH:
               var13 = var1.getX() - var12[0];
               var15 = var1.getY() - var12[1];
               var17 = var1.getZ();
               break;
            case WEST:
               var13 = var1.getX();
               var15 = var1.getY() - var12[1];
               var17 = var1.getZ() - var12[0];
               break;
            default:
               throw new AssertionError("Invalid facing: " + var6);
         }

         Location var19 = new Location(var5, var13, var15, var17);
         var5.spawnParticle(XParticle.DUST.get(), var19, var2, 0.0, 0.0, 0.0, var3, var11);
      }
   }

   public static void saveImage(BufferedImage var0, Path var1) {
      try {
         ImageIO.write(var0, "png", Files.newOutputStream(var1, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   public static CompletableFuture<BufferedImage> stringToImage(Font var0, java.awt.Color var1, String var2) {
      return CompletableFuture.supplyAsync(() -> {
         BufferedImage var3 = new BufferedImage(1, 1, 2);
         Graphics2D var4 = var3.createGraphics();
         var4.setFont(var0);
         FontRenderContext var5 = var4.getFontMetrics().getFontRenderContext();
         Rectangle2D var6 = var0.getStringBounds(var2, var5);
         var4.dispose();
         var3 = new BufferedImage((int)Math.ceil(var6.getWidth()), (int)Math.ceil(var6.getHeight()), 2);
         var4 = var3.createGraphics();
         var4.setColor(var1);
         var4.setFont(var0);
         var4.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var4.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
         var4.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
         var4.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
         var4.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         var4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
         FontMetrics var7 = var4.getFontMetrics();
         var4.drawString(var2, 0, var7.getAscent());
         var4.dispose();
         return var3;
      });
   }
}
