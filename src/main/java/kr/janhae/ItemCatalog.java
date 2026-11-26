package kr.janhae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

public final class ItemCatalog {
    private static final Map<String, Spec> ALL = new LinkedHashMap<>();

    static {
        weapon("plank_club", Material.WOODEN_SWORD, "각목", "못이 박힌 부러진 각목", 5, 1.0, 40, 0, 80, true, false);
        weapon("riot_baton", Material.WOODEN_SWORD, "진압봉", "늘어나는 검은 진압봉", 6, 2.0, 90, 0, 180, true, false);
        weapon("frying_pan", Material.IRON_SWORD, "후라이팬", "그을린 철 팬. 손잡이가 짧다", 6, 1.6, 70, 0, 160, true, false);
        weapon("fire_axe", Material.IRON_AXE, "소방 도끼", "빨간 도끼. 날에 이가 빠졌다", 9, 0.9, 200, 0, 250, true, false);
        weapon("lab_cutter", Material.IRON_SWORD, "실험실 커터", "노란 손잡이의 긴 메스", 6, 2.4, 260, 0, 200, true, false);
        weapon("rust_sword", Material.IRON_SWORD, "녹슨 검", "주황 녹이 슨 철검", 7, 1.6, 350, 0, 250, true, false);
        weapon("patrol_crossbow", Material.CROSSBOW, "순찰 석궁", "칠이 벗겨진 검은 석궁", 0, 0, 480, 0, 320, true, false);
        weapon("rust_pickaxe", Material.IRON_PICKAXE, "녹슨 곡괭이", "흙과 녹이 섞인 철곡괭이", 4, 1.2, 220, 0, 250, true, false);

        armor("work", "작업복", "바랜 흰 가운", new int[] {30, 70, 60, 30}, new int[] {1, 3, 2, 1}, 0, 80, true);
        armor("riot", "진압복", "번호가 벗겨진 검은 진압 장비", new int[] {120, 260, 220, 120}, new int[] {2, 6, 5, 2}, 0, 240, true);
        armor("sealed", "밀봉 방호복", "노란 방호복. 헬멧은 흐린 바이저", new int[] {0, 0, 0, 0}, new int[] {3, 8, 6, 3}, 2, 400, false);

        Spec.of("bandage", Material.PAPER, "붕대", "얼룩이 남은 흰 붕대").price(25).food(1, 0.1f, 1.1f, true).register();
        Spec.of("lab_ration", Material.BREAD, "연구소 식량", "찌그러진 은색 캔").price(10).food(6, 0.7f, 1.6f, false).register();
        Spec.of("boiled_water", Material.GLASS_BOTTLE, "끓인 물", "실험실 플라스크에 담긴 물").price(8).drink(1, 0.2f, 1.2f, false).register();
        Spec.of("painkiller", Material.HONEY_BOTTLE, "진통제", "라벨이 지워진 알약 병").price(40).drink(0, 0f, 1.0f, true).register();
        Spec.of("antidote", Material.GLASS_BOTTLE, "억제제", "탁한 초록 액체").price(50).drink(0, 0f, 1.2f, true).register();
        Spec.of("chem_light", Material.GLOWSTONE_DUST, "화학등", "흔들면 잠시 밤이 걷힌다").price(30).food(0, 0f, 0.6f, true).stack(16).register();
        Spec.of("field_arrow", Material.ARROW, "현장 화살", "깃이 닳은 제식 화살. 석궁에 쓴다").price(4).stack(64).register();

        weapon("culture_shard", Material.IRON_SWORD, "배양 파편", "마른 피가 묻은 유리 단검", 6, 2.6, 0, 80, 120, false, false);
        weapon("femur_mace", Material.IRON_SWORD, "대퇴골 둔기", "긴 뼈 끝에 조직이 뭉쳤다", 11, 0.9, 0, 150, 200, false, false);
        weapon("rib_knife", Material.IRON_SWORD, "갈비 나이프", "갈비뼈를 엮어 만든 칼", 8, 1.6, 0, 200, 220, false, false);
        weapon("needle_lance", Material.IRON_SWORD, "주사 창", "긴 바늘이 박힌 막대", 7, 2.2, 0, 180, 180, false, false);
        weapon("spine_saw", Material.IRON_SWORD, "척추 톱", "척추가 톱니처럼 선 검", 12, 1.2, 0, 400, 280, false, false);
        weapon("hazmat_breaker", Material.IRON_AXE, "밀봉 파쇄기", "방호복 위를 찢는 무거운 도끼", 13, 0.85, 0, 350, 400, false, false);

        weapon("specimen_saw", Material.IRON_AXE, "표본 절단기", "연구소 원형 톱. 날이 크다", 15, 0.8, 80000, 0, 900, false, true);
        weapon("last_vial", Material.IRON_SWORD, "마지막 시료", "검날에 유리 시료관이 박혀 있다. 처치하면 체력이 조금 돌아온다", 8, 1.6, 60000, 0, 700, false, true);
        weapon("state_rifle", Material.CROSSBOW, "국가 제식 소총", "석궁 구조의 짧은 소총. 장전이 빠르고 화살이 무겁다", 0, 0, 90000, 0, 800, false, true);
        weapon("white_coat_reaper", Material.IRON_SWORD, "백의의 리퍼", "흰 천이 감긴 낫검. 처치하면 체력이 조금 돌아온다", 14, 1.0, 100000, 0, 900, false, true);
        weapon("seal_syringe", Material.IRON_SWORD, "봉인 주사", "거대한 주사기. 맞은 상대가 잠시 느려진다", 9, 1.8, 70000, 0, 700, false, true);
        Spec.of("riot_shield", Material.SHIELD, "진압 방패", "긁힌 진압 방패").price(75000).durability(480).legendary().shield().register();

        material("infected_tissue", Material.ROTTEN_FLESH, "감염 조직", "탁한 녹색 살점", 8, 1, false);
        material("mutated_bone", Material.BONE, "변이 뼈", "휘고 구멍이 난 뼈", 8, 1, false);
        material("spore_sac", Material.SLIME_BALL, "포자 주머니", "노란 막에 녹색 점이 있다", 10, 1, false);
        material("tainted_cloth", Material.PHANTOM_MEMBRANE, "오염된 천", "누군가의 옷이었던 천", 6, 1, false);
        Spec.of("sealed_sample", Material.GLASS_BOTTLE, "???", "라벨이 찢긴 시료관. 감정소에서만 정체를 알 수 있다").salvage(-1).glint().register();
        material("unstable_core", Material.PRISMARINE_CRYSTALS, "불안정한 핵", "제작소에서 4개분으로 계산되는 핵", 120, 4, true);
        Spec.of("sample_kit", Material.BOOK, "채집 키트", "연구원이 현장을 기록하는 수첩. 오른클릭으로 조사한다").salvage(15).register();

        Spec.of("tissue_grill", Material.COOKED_BEEF, "조직 구이", "탄 살점 꼬치. 먹으면 크게 회복한다").salvage(12).food(5, 0.6f, 1.6f, false).register();
        Spec.of("bone_broth", Material.BEETROOT_SOUP, "뼈 육수", "뼈가 담긴 컵. 잠시 흡수 체력이 생긴다").salvage(12).drink(4, 0.5f, 1.6f, false).register();
        Spec.of("spore_fry", Material.COOKED_CHICKEN, "포자 튀김", "노란 튀김. 잠시 발이 빨라진다").salvage(12).food(4, 0.4f, 1.4f, false).register();
    }

    private ItemCatalog() {}

    static void register(Spec spec) {
        if (ALL.put(spec.id, spec) != null) {
            throw new IllegalStateException("duplicate item " + spec.id);
        }
    }

    private static void weapon(String id, Material material, String name, String flavor, double attack, double speed, int price, int salvage, int durability, boolean smith, boolean legendary) {
        var builder = Spec.of(id, material, name, flavor).attack(attack, speed).durability(durability).gear();
        if (price > 0) {
            builder.price(price);
        }
        if (salvage != 0) {
            builder.salvage(salvage);
        }
        if (smith) {
            builder.smith();
        }
        if (legendary) {
            builder.legendary();
        }
        builder.register();
    }

    private static void material(String id, Material material, String name, String flavor, int salvage, int origin, boolean glint) {
        var builder = Spec.of(id, material, name, flavor).salvage(salvage).origin(origin);
        if (glint) {
            builder.glint();
        }
        builder.register();
    }

    private static void armor(String set, String name, String flavor, int[] prices, int[] armor, double toughness, int durability, boolean shop) {
        String[] parts = {"helmet", "chestplate", "leggings", "boots"};
        String[] partNames = {"투구", "흉갑", "바지", "부츠"};
        Material[] mats = {Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS};
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int[] sealedSalvage = {160, 320, 280, 160};
        for (int i = 0; i < 4; i++) {
            var builder = Spec.of(set + "_" + parts[i], mats[i], name + " " + partNames[i], flavor)
                    .armor(set, slots[i], armor[i], toughness)
                    .durability(durability);
            if (shop) {
                builder.price(prices[i]).smith();
            } else {
                builder.salvage(sealedSalvage[i]);
            }
            builder.register();
        }
    }

    public static Spec get(String id) {
        return ALL.get(id);
    }

    public static Collection<Spec> all() {
        return Collections.unmodifiableCollection(ALL.values());
    }

    public static List<Spec> shop() {
        List<Spec> list = new ArrayList<>();
        for (Spec spec : ALL.values()) {
            if (spec.price > 0 && !spec.legendary) {
                list.add(spec);
            }
        }
        return list;
    }

    public static List<Spec> legendaries() {
        List<Spec> list = new ArrayList<>();
        for (Spec spec : ALL.values()) {
            if (spec.legendary) {
                list.add(spec);
            }
        }
        return list;
    }

    public static List<String> legendaryIds() {
        List<String> ids = new ArrayList<>();
        for (Spec spec : legendaries()) {
            ids.add(spec.id);
        }
        return ids;
    }

    public static List<Spec> workshopPool(int tier) {
        List<String> ids = switch (tier) {
            case 8 -> List.of("culture_shard", "femur_mace");
            case 16 -> List.of("rib_knife", "needle_lance");
            case 32 -> List.of("spine_saw", "hazmat_breaker", "sealed_helmet", "sealed_chestplate", "sealed_leggings", "sealed_boots");
            default -> List.of();
        };
        List<Spec> list = new ArrayList<>();
        for (String id : ids) {
            Spec spec = ALL.get(id);
            if (spec != null) {
                list.add(spec);
            }
        }
        return list;
    }

    public static List<Spec> appraisalPool() {
        List<Spec> list = new ArrayList<>();
        list.addAll(workshopPool(8));
        list.addAll(workshopPool(16));
        list.addAll(workshopPool(32));
        for (Spec spec : shop()) {
            if (spec.smith && (spec.isWeapon() || spec.isArmor() || spec.gear)) {
                list.add(spec);
            }
        }
        return list;
    }

    public static List<String> cookMaterials() {
        return List.of("infected_tissue", "mutated_bone", "spore_sac");
    }

    public static String cookResult(String materialId) {
        return switch (materialId) {
            case "infected_tissue" -> "tissue_grill";
            case "mutated_bone" -> "bone_broth";
            case "spore_sac" -> "spore_fry";
            default -> null;
        };
    }
}
