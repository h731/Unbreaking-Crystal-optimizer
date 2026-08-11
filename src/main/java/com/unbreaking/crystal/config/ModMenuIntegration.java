package com.unbreaking.crystal.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("Crystal Performance Optimizer"));

            builder.setSavingRunnable(CrystalConfig::save);

            ConfigCategory general = builder.getOrCreateCategory(Text.literal("Performance"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Anchor Visual Effects Optimizer"), CrystalConfig.enableAnchorOptimizer)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> CrystalConfig.enableAnchorOptimizer = newValue)
                    .build());

            // الخيار الجديد المضاف دون المساس بالباقي
            general.addEntry(entryBuilder.startBooleanToggle(Text.literal("0 Delay Clicks (Better on Linux)"), CrystalConfig.enableZeroDelayClicks)
                    .setTooltip(Text.literal("Bypasses input delay using GLFW directly for instant clicks."))
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> CrystalConfig.enableZeroDelayClicks = newValue)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Disable Crystal Movement (FPS Boost)"), CrystalConfig.disableCrystalMovement)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> CrystalConfig.disableCrystalMovement = newValue)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Optimize Crystal Rendering"), CrystalConfig.optimizeCrystalRendering)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> CrystalConfig.optimizeCrystalRendering = newValue)
                    .build());

            return builder.build();
        };
    }
}