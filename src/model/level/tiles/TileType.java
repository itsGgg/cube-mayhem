package model.level.tiles;

public enum TileType {
    WHITE,
    GRAY,
    WHITE_BLOCKED,
    GRAY_BLOCKED,
    START,
    END,
    ARROW,
    SLIDE,
    TELEPORTATION,
    SWITCH_OFF,
    SWITCH_ON,
    TRANSPARENT_VISIBLE,
    SPIN,
    TRANSPARENT_INVISIBLE;

    private static final String[] names = {
            "Blanc",
            "Gris",
            "Blanc bloqué",
            "Gris bloqué",
            "Début",
            "Fin",
            "Flèche",
            "Slide",
            "Téléportation",
            "Switch allumé",
            "Switch éteint",
            "Transparent visible",
            "Spin",
            "Transparent invisible"
    };
    private static final String[] symbols = {
            "0", // Blanc
            "2", // Gris
            "1", // Blanc bloqué
            "3", // Gris bloqué
            "S", // Debut
            "E", // Fin
            "=", // Fleche
            "~", // Slide
            "+", // Teleportation
            "$", // Switch allumé
            "£", // Switch éteint
            "_", // Transparent visible
            "@", // Spin
            "-", // Transparent invisible
    };

    public String getName() {
        return names[this.ordinal()];
    }

    public String getSymbol() {
        return symbols[this.ordinal()];
    }

    /**
     *  Obtient le type de la tuile par son nom
     * @param name le nom de la tuile recherchée
     */
    public static TileType fromName(String name) {
        for(TileType tileType : values()) {
            if(tileType.getName().equalsIgnoreCase(name)) {
                return tileType;
            }
        }
        return null;
    }

    /**
     *  Obtient le type de la tuile par son symbole
     * @param symbol le symbole de la tuile rechercée
     */
    public static TileType fromSymbol(String symbol) {
        for(TileType tileType : values()) {
            if(tileType.getSymbol().equals(symbol)) {
                return tileType;
            }
        }
        return null;
    }

    public boolean isDirectional() {
        return this == ARROW || this == SLIDE;
    }

    public String getCompleteName() {
        return "[" + getSymbol() + "] " + getName();
    }
}
