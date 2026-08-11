package com.unbreaking.crystal.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CrystalConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "unbreakingcrystaloptimizer.json");

    // الخصائص الخاصة بتحسين الأداء والرسوميات واستجابة المدخلات
    public static boolean enableAnchorOptimizer = true;
    public static boolean disableCrystalMovement = true;
    public static boolean optimizeCrystalRendering = true;
    public static boolean enableZeroDelayClicks = true; // 0 delay clicks (better on linux)

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    enableAnchorOptimizer = data.enableAnchorOptimizer;
                    disableCrystalMovement = data.disableCrystalMovement;
                    optimizeCrystalRendering = data.optimizeCrystalRendering;
                    enableZeroDelayClicks = data.enableZeroDelayClicks;
                }
            } catch (Exception e) {
                System.out.println("فشل تحميل إعدادات المود، سيتم استخدام الإعدادات الافتراضية.");
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            ConfigData data = new ConfigData();
            data.enableAnchorOptimizer = enableAnchorOptimizer;
            data.disableCrystalMovement = disableCrystalMovement;
            data.optimizeCrystalRendering = optimizeCrystalRendering;
            data.enableZeroDelayClicks = enableZeroDelayClicks;
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ConfigData {
        boolean enableAnchorOptimizer = CrystalConfig.enableAnchorOptimizer;
        boolean disableCrystalMovement = CrystalConfig.disableCrystalMovement;
        boolean optimizeCrystalRendering = CrystalConfig.optimizeCrystalRendering;
        boolean enableZeroDelayClicks = CrystalConfig.enableZeroDelayClicks;
    }
}