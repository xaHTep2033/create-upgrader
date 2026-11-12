package dev.upgrader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public final class Prices {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("create-upgrader-prices.json");
    private static Map<String,Integer> values = new TreeMap<>();
    private static final Set<String> DENY = Set.of("air","barrier","light","structure_void","structure_block","jigsaw","command_block","chain_command_block","repeating_command_block","debug_stick","knowledge_book","spawner","trial_spawner","vault","bedrock","end_portal_frame","reinforced_deepslate","budding_amethyst","petrified_oak_slab","player_head","frogspawn");
    public static int get(Item item) { return values.getOrDefault(BuiltInRegistries.ITEM.getKey(item).toString(),0); }
    public static void load() {
        Map<String,Integer> next = new TreeMap<>();
        try {
            if (Files.exists(FILE)) try (Reader r=Files.newBufferedReader(FILE)) {
                Map<String,Integer> saved=GSON.fromJson(r,new TypeToken<Map<String,Integer>>(){}.getType());
                if(saved==null) throw new IOException("Empty JSON");
                next.putAll(saved);
            }
            for(ResourceLocation id:BuiltInRegistries.ITEM.keySet()) {
                String key=id.toString(); next.putIfAbsent(key, initial(id));
                Integer v=next.get(key);
                if(v==null || v<0) throw new IOException("Bad price: "+key);
                next.put(key,Math.min(v,19999));
            }
            next.put("minecraft:dragon_egg",20000);
            next.put("create_upgrader:upgrader",0);
            Files.createDirectories(FILE.getParent());
            try(Writer w=Files.newBufferedWriter(FILE)){GSON.toJson(next,w);}
            values=next;
        } catch(Exception e){throw new IllegalStateException("Cannot load upgrader prices",e);}
    }
    private static int initial(ResourceLocation id) {
        String p=id.getPath();
        if(id.getNamespace().equals("create_upgrader")) return 0;
        if(id.getNamespace().equals("minecraft")) {
            if(DENY.contains(p)||p.endsWith("_spawn_egg")||p.startsWith("infested_")) return 0;
            if(p.equals("dragon_egg")) return 20000;
            if(Set.of("dirt","stone","cobblestone","sand","gravel","stick").contains(p)||p.endsWith("_planks")) return 1;
            if(p.endsWith("_log")||p.endsWith("_wood")||p.endsWith("_stem")) return 4;
            if(p.contains("netherite")) return p.endsWith("_block")?18000:3000;
            if(p.equals("nether_star")) return 12000;
            if(p.equals("elytra")) return 15000;
            if(p.equals("enchanted_golden_apple")) return 10000;
            if(p.equals("dragon_head")) return 8000;
            if(p.equals("diamond_block")) return 2700;
            if(p.equals("diamond")) return 300;
            if(p.equals("emerald_block")) return 900;
            if(p.equals("emerald")) return 100;
            if(p.equals("iron_block")) return 180;
            if(p.equals("gold_block")) return 360;
            if(p.equals("copper_block")) return 72;
            if(p.equals("iron_ingot")) return 20;
            if(p.equals("gold_ingot")) return 40;
            if(p.equals("copper_ingot")) return 8;
            if(p.equals("iron_nugget")) return 2;
            if(p.equals("gold_nugget")) return 4;
            if(p.contains("diamond")) return 1800;
            if(p.contains("shulker")) return 1500;
            if(p.contains("end")) return 500;
            if(p.contains("blaze")||p.contains("ghast")) return 250;
            if(p.contains("redstone")||p.contains("quartz")||p.contains("lapis")) return 25;
            return 30;
        }
        if(p.contains("creative")||p.contains("debug")||p.contains("incomplete")||p.contains("spawn_egg")) return 0;
        if(p.contains("engine")||p.contains("controller")||p.contains("cannon")||p.contains("propeller")) return 14000;
        if(p.contains("precision")||p.contains("computer")||p.contains("assembler")) return 12000;
        if(p.contains("motor")||p.contains("bearing")||p.contains("generator")) return 10000;
        if(p.contains("machine")||p.contains("press")||p.contains("mixer")||p.contains("deployer")) return 8000;
        if(p.contains("ingot")||p.contains("sheet")||p.contains("plate")) return 1200;
        if(p.contains("nugget")) return 150;
        if(p.contains("shaft")||p.contains("cogwheel")||p.contains("belt")||p.contains("pipe")) return 2000;
        return 5000;
    }
}
