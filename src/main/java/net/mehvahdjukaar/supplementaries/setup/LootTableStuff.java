package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Items;
import net.minecraft.loot.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraftforge.event.LootTableLoadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.LootItem;

public class LootTableStuff {

    private static final List<BiConsumer<LootTableLoadEvent, TableType>> LOOT_INJECTS = new ArrayList<>();

    //initialize so I don't have to constantly check configs for each loot table entry
    public static void init() {
        if (RegistryConfigs.reg.GLOBE_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectGlobe);
        if (RegistryConfigs.reg.ROPE_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectRope);
        if (RegistryConfigs.reg.FLAX_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectFlax);
        if (RegistryConfigs.reg.BOMB_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectBlueBomb);
        if (RegistryConfigs.reg.BOMB_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectBomb);
        if (RegistryConfigs.reg.SLINGSHOT_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectStasis);
        if (RegistryConfigs.reg.BAMBOO_SPIKES_ENABLED.get() &&
                RegistryConfigs.reg.TIPPED_SPIKES_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectSpikes);
    }

    public static void injectLootTables(LootTableLoadEvent event) {
        ResourceLocation res = event.getName();
        String nameSpace = res.getNamespace();
        if (nameSpace.equals("minecraft") || nameSpace.equals("repurposed_structures")) {
            TableType type = LootHelper.getType(res.toString());
            if (type != TableType.OTHER) {
                LOOT_INJECTS.forEach(i -> i.accept(event, type));
            }
        }
    }

    public enum TableType {
        OTHER,
        MINESHAFT,
        SHIPWRECK,
        PILLAGER,
        DUNGEON,
        PYRAMID,
        STRONGHOLD,
        TEMPLE,
        TEMPLE_DISPENSER,
        IGLOO,
        MANSION,
        FORTRESS,
        BASTION,
        RUIN,
        SHIPWRECK_STORAGE,
        END_CITY
    }

    private static class LootHelper {

        static boolean RS = CompatHandler.repurposed_structures;

        public static TableType getType(String name) {
            if (isShipwreck(name)) return TableType.SHIPWRECK;
            if (isShipwreckStorage(name)) return TableType.SHIPWRECK_STORAGE;
            if (isMineshaft(name)) return TableType.MINESHAFT;
            if (isDungeon(name)) return TableType.DUNGEON;
            if (isTemple(name)) return TableType.TEMPLE;
            if (isTempleDispenser(name)) return TableType.TEMPLE_DISPENSER;
            if (isOutpost(name)) return TableType.PILLAGER;
            if (isStronghold(name)) return TableType.STRONGHOLD;
            if (isFortress(name)) return TableType.FORTRESS;
            if (isEndCity(name)) return TableType.END_CITY;
            return TableType.OTHER;
        }

        private static final Pattern RS_SHIPWRECK = Pattern.compile("repurposed_structures:chests/shipwreck/\\w*/treasure_chest");

        private static boolean isShipwreck(String s) {
            return s.equals(BuiltInLootTables.SHIPWRECK_TREASURE.toString()) || RS && RS_SHIPWRECK.matcher(s).matches();
        }

        private static final Pattern RS_SHIPWRECK_STORAGE = Pattern.compile("repurposed_structures:chests/shipwreck/\\w*/supply_chest");

        private static boolean isShipwreckStorage(String s) {
            return s.equals(BuiltInLootTables.SHIPWRECK_SUPPLY.toString()) || RS && RS_SHIPWRECK_STORAGE.matcher(s).matches();
        }

        private static boolean isMineshaft(String s) {
            return s.equals(BuiltInLootTables.ABANDONED_MINESHAFT.toString()) || RS && s.contains("repurposed_structures:chests/mineshaft");
        }

        private static boolean isOutpost(String s) {
            return s.equals(BuiltInLootTables.PILLAGER_OUTPOST.toString()) || RS && s.contains("repurposed_structures:chests/outpost");
        }

        private static boolean isDungeon(String s) {
            return s.equals(BuiltInLootTables.SIMPLE_DUNGEON.toString()) || RS && s.contains("repurposed_structures:chests/dungeon");
        }

        private static final Pattern RS_TEMPLE = Pattern.compile("repurposed_structures:chests/temple/\\w*_chest");

        private static boolean isTemple(String s) {
            return s.equals(BuiltInLootTables.JUNGLE_TEMPLE.toString()) || RS && RS_TEMPLE.matcher(s).matches();
        }

        private static final Pattern RS_TEMPLE_DISPENSER = Pattern.compile("repurposed_structures:chests/temple/\\w*_dispenser");

        private static boolean isTempleDispenser(String s) {
            return s.equals(BuiltInLootTables.JUNGLE_TEMPLE.toString()) || RS && RS_TEMPLE_DISPENSER.matcher(s).matches();
        }

        private static boolean isStronghold(String s) {
            return s.equals(BuiltInLootTables.STRONGHOLD_CROSSING.toString()) || RS && s.contains("repurposed_structures:chests/stronghold/nether_storage_room");
        }

        private static boolean isFortress(String s) {
            return s.equals(BuiltInLootTables.NETHER_BRIDGE.toString()) || RS && s.contains("repurposed_structures:chests/fortress");
        }

        private static boolean isEndCity(String s) {
            return s.equals(BuiltInLootTables.END_CITY_TREASURE.toString());
        }
    }


    public static void tryInjectGlobe(LootTableLoadEvent e, TableType type) {
        if (type == TableType.SHIPWRECK) {
            float chance = (float) ServerConfigs.cached.GLOBE_TREASURE_CHANCE;
            LootPool pool = LootPool.lootPool()
                    .name("supplementaries_injected_globe")
                    .setRolls(ConstantIntValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(chance))
                    .add(LootItem.lootTableItem(ModRegistry.GLOBE_ITEM.get()).setWeight(1))
                    .build();
            e.getTable().addPool(pool);
        }
    }

    public static void tryInjectRope(LootTableLoadEvent e, TableType type) {

        if (type == TableType.MINESHAFT) {
            float chance = 0.35f;
            LootPool pool = LootPool.lootPool()
                    .name("supplementaries_injected_rope")
                    .apply(SetItemCountFunction.setCount(RandomValueBounds.between(5.0F, 17.0F)))
                    .setRolls(ConstantIntValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(chance))
                    .add(LootItem.lootTableItem(ModRegistry.ROPE_ITEM.get()).setWeight(1))
                    .build();
            e.getTable().addPool(pool);
        }
    }

    public static void tryInjectFlax(LootTableLoadEvent e, TableType type) {
        float chance;
        float min = 1;
        float max = 3;
        if (type == TableType.MINESHAFT) {
            chance = 0.15f;
        } else if (type == TableType.DUNGEON) {
            chance = 0.23f;
        } else if (type == TableType.SHIPWRECK_STORAGE) {
            chance = 0.2f;
        } else if (type == TableType.PILLAGER) {
            chance = 1f;
            min = 2;
            max = 5;
        } else return;

        LootPool pool = LootPool.lootPool()
                .name("supplementaries_injected_flax")
                .apply(SetItemCountFunction.setCount(RandomValueBounds.between(min, max)))
                .setRolls(ConstantIntValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(LootItem.lootTableItem(ModRegistry.FLAX_SEEDS_ITEM.get()).setWeight(1))
                .build();
        e.getTable().addPool(pool);
    }

    public static void tryInjectBlueBomb(LootTableLoadEvent e, TableType type) {
        float chance;
        if (type == TableType.STRONGHOLD) {
            chance = 0.03f;
        } else if (type == TableType.MINESHAFT) {
            chance = 0.028f;
        } else if (type == TableType.TEMPLE) {
            chance = 0.07f;
        } else if (type == TableType.FORTRESS) {
            chance = 0.035f;
        } else if (type == TableType.DUNGEON) {
            chance = 0.01f;
        } else return;

        LootPool pool = LootPool.lootPool()
                .name("supplementaries_injected_blue_bomb")
                .setRolls(ConstantIntValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(LootItem.lootTableItem(ModRegistry.BOMB_BLUE_ITEM.get()).setWeight(1))
                .build();
        e.getTable().addPool(pool);

    }

    public static void tryInjectBomb(LootTableLoadEvent e, TableType type) {
        float chance;
        if (type == TableType.STRONGHOLD) {
            chance = 0.25f;
        } else if (type == TableType.MINESHAFT) {
            chance = 0.12f;
        } else if (type == TableType.TEMPLE) {
            chance = 0.10f;
        } else if (type == TableType.FORTRESS) {
            chance = 0.145f;
        } else return;
        LootPool pool = LootPool.lootPool()
                .name("supplementaries_injected_bomb")
                .apply(SetItemCountFunction.setCount(RandomValueBounds.between(1F, 3.0F)))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(LootItem.lootTableItem(ModRegistry.BOMB_ITEM.get()).setWeight(1))
                .build();
        e.getTable().addPool(pool);
    }

    public static void tryInjectSpikes(LootTableLoadEvent e, TableType type) {
        if (type == TableType.TEMPLE) {

            float chance = 0.38f;
            LootPool pool = LootPool.lootPool()
                    .name("supplementaries_injected_spikes")
                    .setRolls(ConstantIntValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(chance))
                    .add(LootItem.lootTableItem(ModRegistry.BAMBOO_SPIKES_ITEM.get()).setWeight(4))
                    .add(LootItem.lootTableItem(ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get()).setWeight(3)
                            .apply(SetNbtFunction.setTag(
                                    Util.make(new CompoundTag(), (c) -> c.putString("Potion", "minecraft:poison"))
                            ))
                            .apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.9F)))
                    )
                    .build();
            e.getTable().addPool(pool);
        }
    }

    public static void tryInjectStasis(LootTableLoadEvent e, TableType type) {

        if (type == TableType.END_CITY) {
            float chance = 0.25f;

            LootPool pool = LootPool.lootPool()
                    .name("supplementaries_injected_stasis")
                    .setRolls(ConstantIntValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(chance))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                            .apply(SetNbtFunction.setTag(
                                    (EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModRegistry.STASIS_ENCHANTMENT.get(), 1)))
                                    .getOrCreateTag()
                            ))
                            .setWeight(1))
                    .build();
            e.getTable().addPool(pool);
        }
    }


}
