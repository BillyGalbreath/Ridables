package net.pl3x.bukkit.ridables.util;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.types.Type;
import net.minecraft.server.v1_13_R1.BiomeBase;
import net.minecraft.server.v1_13_R1.DataConverterRegistry;
import net.minecraft.server.v1_13_R1.DataConverterTypes;
import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityTypes;
import net.minecraft.server.v1_13_R1.Item;
import net.minecraft.server.v1_13_R1.ItemMonsterEgg;
import net.minecraft.server.v1_13_R1.MinecraftKey;
import net.minecraft.server.v1_13_R1.RegistryID;
import net.minecraft.server.v1_13_R1.RegistryMaterials;
import net.minecraft.server.v1_13_R1.RegistrySimple;
import net.minecraft.server.v1_13_R1.World;
import net.minecraft.server.v1_13_R1.WorldGenDungeons;
import net.minecraft.server.v1_13_R1.WorldGenFeatureSwampHut;
import net.minecraft.server.v1_13_R1.WorldGenMonument;
import net.minecraft.server.v1_13_R1.WorldGenNether;
import net.pl3x.bukkit.ridables.Logger;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;

public class RegistryHax {
    private static Field materials_field_a;
    private static Field registry_field_b;
    private static Field registry_field_c;
    private static Field registry_field_d;
    private static Field materials_field_b;
    private static Field simple_field_c;
    private static Field field_modifiers;
    private static Field item_field_d;
    private static Method registry_method_d;
    private static Method registry_method_e;

    static {
        try {
            materials_field_a = RegistryMaterials.class.getDeclaredField("a");
            materials_field_a.setAccessible(true);
            registry_field_b = RegistryID.class.getDeclaredField("b");
            registry_field_b.setAccessible(true);
            registry_field_c = RegistryID.class.getDeclaredField("c");
            registry_field_c.setAccessible(true);
            registry_field_d = RegistryID.class.getDeclaredField("d");
            registry_field_d.setAccessible(true);
            materials_field_b = RegistryMaterials.class.getDeclaredField("b");
            materials_field_b.setAccessible(true);
            simple_field_c = RegistrySimple.class.getDeclaredField("c");
            simple_field_c.setAccessible(true);
            field_modifiers = Field.class.getDeclaredField("modifiers");
            field_modifiers.setAccessible(true);
            item_field_d = ItemMonsterEgg.class.getDeclaredField("d");
            item_field_d.setAccessible(true);
            registry_method_d = RegistryID.class.getDeclaredMethod("d", Object.class);
            registry_method_d.setAccessible(true);
            registry_method_e = RegistryID.class.getDeclaredMethod("e", int.class);
            registry_method_e.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException ignore) {
        }
    }

    public static void injectNewEntityTypes(String name, String extend_from, Class<? extends Entity> clazz, Function<? super World, ? extends Entity> function) {
        Logger.debug("Attempting to inject new entity: &3" + name);
        Logger.debug("Injecting new datatypes");
        Map<Object, Type<?>> dataTypes = (Map<Object, Type<?>>) DataConverterRegistry.a().getSchema(15190).findChoiceType(DataConverterTypes.n).types();
        dataTypes.put("minecraft:" + name, dataTypes.get("minecraft:" + extend_from));
        Logger.debug("Injecting new EntityTypes");
        EntityTypes.a(name, EntityTypes.a.a(clazz, function));
        Logger.info("Successfully injected new entity: &a" + name);
    }

    public static boolean injectReplacementEntityTypes(String name, EntityTypes entityTypes, MinecraftKey key, EntityTypes<?> newType, Material spawnEggMaterial) {
        Logger.debug("Attempting to inject replacement entity: &3" + name);
        try {
            RegistryID<EntityTypes<?>> registry = (RegistryID<EntityTypes<?>>) materials_field_a.get(EntityTypes.REGISTRY);
            int id = registry.getId(entityTypes);

            Logger.debug("Detected original id: " + id);

            Object[] array_b = (Object[]) registry_field_b.get(registry);
            Object[] array_d = (Object[]) registry_field_d.get(registry);

            if (id < 0) {
                Logger.debug("&cInvalid id detected. Trying again!");
                for (int i = 0; i < array_d.length; i++) {
                    if (array_d[i] != null) {
                        if (array_d[i] == entityTypes) {
                            Logger.debug("Found EntityTypes id by reference");
                            id = i;
                            break;
                        }
                        if (((EntityTypes) array_d[i]).d().equals("minecraft:" + name)) {
                            Logger.debug("&cFound EntityTypes id using name but not reference! What?!");
                            id = i;
                            break;
                        }
                    }
                }
                Logger.debug("New detected id: " + id);
            }

            Logger.debug("EntityType at id " + id + ": " + ((EntityTypes) array_d[id]).d());

            int oldIndex = -1;
            for (int i = 0; i < array_b.length; i++) {
                if (array_b[i] != null) {
                    if (array_b[i] == entityTypes) {
                        //array_b[i] = null; // do not remove old reference (might be causing issues?)
                        oldIndex = i;
                        break;
                    }
                    if (((EntityTypes) array_b[i]).d().equals("minecraft:" + name)) {
                        Logger.debug("&cFound EntityTypes oldIndex using name but not reference! What?!");
                        //array_b[i] = null; // do not remove old reference (might be causing issues?)
                        oldIndex = i;
                        break;
                    }
                }
            }

            Logger.debug("Detected oldIndex: " + oldIndex);

            if (oldIndex < 0) {
                Logger.debug("&cInvalid oldIndex detected. Trying again!");
                array_b = (Object[]) registry_field_b.get(registry);
                for (int i = 0; i < array_b.length; i++) {
                    if (array_b[i] != null) {
                        if (array_b[i] == entityTypes) {
                            //array_b[i] = null; // do not remove old reference (might be causing issues?)
                            oldIndex = i;
                            break;
                        }
                        if (((EntityTypes) array_b[i]).d().equals("minecraft:" + name)) {
                            Logger.debug("&cFound EntityTypes oldIndex using name but not reference! What?!");
                            //array_b[i] = null; // do not remove old reference (might be causing issues?)
                            oldIndex = i;
                            break;
                        }
                    }
                }
                Logger.debug("New detected oldIndex: " + oldIndex);
            }

            Logger.debug("EntityType at oldIndex " + oldIndex + ": " + ((EntityTypes) array_b[oldIndex]).d());

            int newIndex = (int) registry_method_e.invoke(registry, registry_method_d.invoke(registry, newType));
            Logger.debug("Generated newIndex: " + newIndex);

            EntityTypes e = (EntityTypes) array_b[newIndex];
            Logger.debug("EntityType at newIndex " + newIndex + ": " + (e == null ? "null" : "&c" + e.d()));

            Logger.debug("Injecting new EntityTypes to b[newIndex]: " + newIndex);
            array_b[newIndex] = newType;
            Logger.debug("Injecting new EntityTypes to d[id]: " + id);
            array_d[id] = newType;

            int[] array_c = (int[]) registry_field_c.get(registry);
            if (oldIndex >= 0) {
                Logger.debug("Removing c[oldIndex] reference: " + oldIndex + ":0");
                array_c[oldIndex] = 0;
            } else {
                Logger.debug("&cSkipping c[oldIndex] reference: " + oldIndex + ":0");
            }
            Logger.debug("Injecting c[newIndex] reference: " + newIndex + ":" + id);
            array_c[newIndex] = id;

            Logger.debug("Updating RegistryMaterials mapB");
            Map<EntityTypes<?>, MinecraftKey> map_b_old = (Map<EntityTypes<?>, MinecraftKey>) materials_field_b.get(EntityTypes.REGISTRY);
            Map<EntityTypes<?>, MinecraftKey> map_b_new = HashBiMap.create();
            for (Map.Entry<EntityTypes<?>, MinecraftKey> entry : map_b_old.entrySet()) {
                if (entry.getKey() != entityTypes) map_b_new.put(entry.getKey(), entry.getValue());
                else map_b_new.put(newType, key);
            }

            Logger.debug("Updating RegistrySimple mapC");
            Map<MinecraftKey, EntityTypes<?>> map_c = (Map<MinecraftKey, EntityTypes<?>>) simple_field_c.get(EntityTypes.REGISTRY);
            map_c.put(key, newType);

            Logger.debug("Updating EntityTypes static field");
            Field types_field = getField(entityTypes);
            types_field.setAccessible(true);
            field_modifiers.setInt(types_field, types_field.getModifiers() & ~Modifier.FINAL);
            types_field.set(null, newType);

            registry_field_b.set(registry, array_b);
            registry_field_c.set(registry, array_c);
            registry_field_d.set(registry, array_d);
            materials_field_a.set(EntityTypes.REGISTRY, registry);
            materials_field_b.set(EntityTypes.REGISTRY, map_b_new);
            simple_field_c.set(EntityTypes.REGISTRY, map_c);

            if (spawnEggMaterial != null) {
                Logger.debug("Updating spawn egg reference");
                Item spawnEgg = CraftItemStack.asNMSCopy(new ItemStack(spawnEggMaterial)).getItem();
                item_field_d.set(spawnEgg, newType);
            }
            return true;
        } catch (IllegalAccessException | InvocationTargetException | ArrayIndexOutOfBoundsException e) {
            Logger.error("Could not inject new ridable entity to registry! Restart your server to try again! "
                    + "(&e" + name + "&c)");
            e.printStackTrace();
            return false;
        }
    }

    private static Field getField(EntityTypes entityTypes) {
        for (Field field : EntityTypes.class.getDeclaredFields()) {
            try {
                if (entityTypes == field.get(null)) {
                    return field;
                }
            } catch (IllegalAccessException ignore) {
            }
        }
        throw new IllegalArgumentException("Could not get Field of " + entityTypes.getClass().getSimpleName());
    }

    public static void rebuildWorldGenMobs() {
        setPrivateFinalField(WorldGenDungeons.class, "b", new EntityTypes[]{EntityTypes.SKELETON, EntityTypes.ZOMBIE, EntityTypes.ZOMBIE, EntityTypes.SPIDER});
        setPrivateFinalField(WorldGenFeatureSwampHut.class, "b", Lists.newArrayList(new BiomeBase.BiomeMeta[]{new BiomeBase.BiomeMeta(EntityTypes.WITCH, 1, 1, 1)}));
        setPrivateFinalField(WorldGenMonument.class, "b", Lists.newArrayList(new BiomeBase.BiomeMeta[]{new BiomeBase.BiomeMeta(EntityTypes.GUARDIAN, 1, 2, 4)}));
        setPrivateFinalField(WorldGenNether.class, "b", Lists.newArrayList(new BiomeBase.BiomeMeta[]{new BiomeBase.BiomeMeta(EntityTypes.BLAZE, 10, 2, 3), new BiomeBase.BiomeMeta(EntityTypes.ZOMBIE_PIGMAN, 5, 4, 4), new BiomeBase.BiomeMeta(EntityTypes.WITHER_SKELETON, 8, 5, 5), new BiomeBase.BiomeMeta(EntityTypes.SKELETON, 2, 5, 5), new BiomeBase.BiomeMeta(EntityTypes.MAGMA_CUBE, 3, 4, 4)}));
    }

    private static void setPrivateFinalField(Class clazz, String name, Object value) {
        Logger.debug("Rebuilding world gen mob features for: " + clazz.getSimpleName());
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            field_modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
