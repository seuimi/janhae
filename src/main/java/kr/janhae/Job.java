package kr.janhae;

import net.kyori.adventure.text.format.NamedTextColor;

public enum Job {
    RESEARCHER("연구원", "researcher", NamedTextColor.AQUA, 20.0, 1.0),
    SURVIVOR("생존가", "survivor", NamedTextColor.RED, 16.0, 1.15),
    COOK("요리사", "cook", NamedTextColor.GREEN, 20.0, 1.05);

    public final String korean;
    public final String id;
    public final NamedTextColor color;
    public final double maxHealth;
    /** 스코어보드에 보이는 상시 공격 배율. 생존가의 미등록 추가 피해는 여기 없다. */
    public final double attackMultiplier;

    Job(String korean, String id, NamedTextColor color, double maxHealth, double attackMultiplier) {
        this.korean = korean;
        this.id = id;
        this.color = color;
        this.maxHealth = maxHealth;
        this.attackMultiplier = attackMultiplier;
    }

    public static Job byId(String id) {
        if (id == null) {
            return null;
        }
        for (Job job : values()) {
            if (job.id.equalsIgnoreCase(id) || job.name().equalsIgnoreCase(id) || job.korean.equals(id)) {
                return job;
            }
        }
        return null;
    }

    public int firstDiscoveryXp() {
        return switch (this) {
            case RESEARCHER -> 200;
            case COOK -> 80;
            case SURVIVOR -> 40;
        };
    }

    public int repeatKillXp() {
        return this == RESEARCHER ? 25 : 15;
    }

    public String description() {
        return switch (this) {
            case RESEARCHER -> "멸망한 이 도시에, 바이러스를 연구하러 왔습니다.\n여러 곳을 조사하고, 시체를 다루고, 새로운 생명체를 기록할 때마다 경험치를 크게 얻습니다.\n전투 자체는 다른 직업보다 특출나지 않습니다.";
            case SURVIVOR -> "여러 나라를 떠돌다 이 땅에 닿았습니다.\n몸은 다른 이들보다 약하지만, 다쳤을 때의 응급처치와 순간 판단은 가장 빠릅니다.\n처음 보는 생명체 앞에서는 공격이 더 거세집니다.";
            case COOK -> "체력과 공격은 무난한 편입니다.\n새로운 생명체에게서 나온 부산물로 레시피를 연구해, 다른 직업이 버틸 음식을 만듭니다.";
        };
    }
}
