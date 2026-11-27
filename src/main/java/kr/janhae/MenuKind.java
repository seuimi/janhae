package kr.janhae;

public enum MenuKind {
    SHOP,
    SMITH,
    WORKSHOP,
    WAREHOUSE,
    APPRAISAL,
    PAWN,
    MARKET,
    CODEX,
    COOK,
    ADMIN_HOME,
    ADMIN_PLAYERS,
    ADMIN_PLAYER,
    ADMIN_MARKET,
    ADMIN_GIVE,
    ADMIN_WAREHOUSE,
    ADMIN_WIPE;

    public boolean admin() {
        return name().startsWith("ADMIN_");
    }
}
