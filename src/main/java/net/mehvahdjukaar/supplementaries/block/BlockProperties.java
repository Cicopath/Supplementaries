package net.mehvahdjukaar.supplementaries.block;

import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.IStringSerializable;

public class BlockProperties {

    //TODO: I hope nobody is reading this

    //blockstate properties
    public static final BooleanProperty EXTENDING = BooleanProperty.create("extending");
    public static final IntegerProperty HOUR = IntegerProperty.create("hour", 0, 23);
    public static final BooleanProperty TILE = BooleanProperty.create("tile");
    public static final IntegerProperty TILE_3 = IntegerProperty.create("tile_3", 0, 2);
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
    public static final BooleanProperty HAS_JAR = BooleanProperty.create("has_jar");
    // it's detecting incoming laser and its distance
    public static final IntegerProperty RECEIVING = IntegerProperty.create("laser_receiving", 0, 15);
    public static final IntegerProperty LIGHT_LEVEL_0_15 = IntegerProperty.create("light_level", 0, 15);
    public static final BooleanProperty HAS_ITEM = BooleanProperty.create("has_item");
    public static final IntegerProperty EXTENSION = IntegerProperty.create("extension", 0, 2);
    public static final EnumProperty<RopeAttachment> ATTACHMENT = EnumProperty.create("attachment", RopeAttachment.class);
    public static final EnumProperty<RopeAttachment> CONNECTION_NORTH = EnumProperty.create("north_connection", RopeAttachment.class);
    public static final EnumProperty<RopeAttachment> CONNECTION_SOUTH = EnumProperty.create("south_connection", RopeAttachment.class);
    public static final EnumProperty<RopeAttachment> CONNECTION_EAST = EnumProperty.create("east_connection", RopeAttachment.class);
    public static final EnumProperty<RopeAttachment> CONNECTION_WEST = EnumProperty.create("west_connection", RopeAttachment.class);
    public static final BooleanProperty KNOT = BooleanProperty.create("knot");
    public static final IntegerProperty POISON = IntegerProperty.create("poison", 0, 15);
    public static final BooleanProperty TIPPED = BooleanProperty.create("tipped");
    public static final IntegerProperty PANCAKES_1_8 = IntegerProperty.create("pancakes", 1, 8);
    public static final BooleanProperty HAS_HONEY = BooleanProperty.create("has_honey");
    public static final EnumProperty<Topping> TOPPING = EnumProperty.create("topping",Topping.class);
    public static final EnumProperty<Winding> WINDING = EnumProperty.create("winding",Winding.class);
    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");

    public enum RopeAttachment implements IStringSerializable{

        NONE("none"), //default /no attachment
        BLOCK("block"), //block attachment / pillar attachment
        //WALL("wall"), //wall attachment
        //POST("post"), //post attachment (druid craft ie)
        KNOT("knot"),
        FENCE("fence"); //fence attachment
        private final String name;
        RopeAttachment(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

        public boolean isNone(){
            return this==NONE;
        }
        public boolean isBlock(){
            return this==BLOCK;
        }

        public boolean isKnot() {return  this==KNOT;}
    }

    public enum Topping implements IStringSerializable{
        NONE("none"),
        HONEY("honey"),
        SYRUP("syrup"),
        CHOCOLATE("chocolate");


        private final String name;
        Topping(String name) {
            this.name = name;
        }

        @Override
        public String getString() {
            return this.name;
        }
    }
    public enum Winding implements IStringSerializable{
        NONE("none"),
        CHAIN("chain"),
        ROPE("rope");

        private final String name;
        Winding(String name) {
            this.name = name;
        }

        @Override
        public String getString() {
            return this.name;

        }
    }

}
