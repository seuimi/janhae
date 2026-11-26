package kr.janhae;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public final class SpeciesBook {
    public enum Preset {
        HAZMAT("방호복"),
        RIOT("폭동 진압"),
        WANDERER("방랑자"),
        RESEARCHER("연구원 시체"),
        CIVILIAN("민간인");

        public final String korean;

        Preset(String korean) {
            this.korean = korean;
        }
    }

    public record Entry(String id, String name) {}

    public static final List<Entry> ALL = new ArrayList<>();

    static {
        EntityType[] types = {
                EntityType.ZOMBIE,
                EntityType.HUSK,
                EntityType.DROWNED,
                EntityType.ZOMBIE_VILLAGER,
                EntityType.SKELETON,
                EntityType.STRAY,
                EntityType.BOGGED,
                EntityType.WITHER_SKELETON
        };
        for (EntityType type : types) {
            for (Preset preset : Preset.values()) {
                String id = id(type, preset);
                ALL.add(new Entry(id, preset.korean + " " + typeName(type)));
            }
        }
    }

    private SpeciesBook() {}

    public static String id(EntityType type, Preset preset) {
        return type.key().asString() + ":" + preset.name();
    }

    public static String typeName(EntityType type) {
        return switch (type) {
            case ZOMBIE -> "좀비";
            case HUSK -> "허스크";
            case DROWNED -> "드라운드";
            case ZOMBIE_VILLAGER -> "주민 좀비";
            case SKELETON -> "스켈레톤";
            case STRAY -> "스트레이";
            case BOGGED -> "보그드";
            case WITHER_SKELETON -> "위더 스켈레톤";
            default -> type.key().value();
        };
    }

    public static boolean dressed(EntityType type) {
        for (Entry entry : ALL) {
            if (entry.id.startsWith(type.key().asString() + ":")) {
                return true;
            }
        }
        return false;
    }
}
