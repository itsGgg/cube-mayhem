package model.tools;

import model.level.tiles.TileType;

public class Constants {
    public static final class Window {
        public static final int DEFAULT_WIDTH = 1600;
        public static final int DEFAULT_HEIGHT = 1000;
        public static final String TITLE = "Cube Mayhem";
    }

    public static final class InventoryPanel {
        public static final int DEFAULT_WIDTH_INVENTORY = Window.DEFAULT_WIDTH;
        public static final int DEFAULT_HEIGHT_INVENTORY = Window.DEFAULT_HEIGHT / 6;
    }

    public static final class TerminalCode {
        public static final String BLOCKED = "#";
        public static final String PATH = ".";
        public static final String TRANSPARENT_VISIBLE = "V";
        public static final String TRANSPARENT_INVISIBLE = "I";
        public static final String RESTRICT = "R";
    }

    public static final class Tile {
        public static final int SIZE = 100;
        public static final int SPACING = 5;
        public static final int COUNT = TileType.values().length;
    }

    public static final class Cube {
        public static final String CUBE_NAME = "cube";
        public static final int SIZE = 100;
        public static final int SHIFT = 5;
    }

    public static final class Path {
        public static final String RESSOURCES = "src/view/ressources/";
        public static final String SOUNDS = RESSOURCES + "sounds/";
        public static final String LEVEL = "Levels/";
        public static final String SAVE_DIRECTORY = "SaveData/";
        public static final String GAME_PROGRESS_DIRECTORY = SAVE_DIRECTORY + "UserProgress/";
        public static final String EDITOR_LEVELS_DIRECTORY = SAVE_DIRECTORY + "EditorLevels/";
    }

    public static final class Case {
        public static final String[] colors = {
                "gray", // BLOCKED
                "white", // PATH
//                "lightGray", // TRANSPARENT
               "white", // TRANSPARENT
                "magenta" // RESTRICT
        };

        public static final java.awt.Color GRAY = new java.awt.Color(230, 230, 230);
        public static final java.awt.Color WHITE = new java.awt.Color(210, 210, 210);
        public static final java.awt.Color TRANSPARENT = new java.awt.Color(210, 210, 210, 100);
        public static final java.awt.Color TRANSPARENT_VISIBLE = new java.awt.Color(190, 240, 255, 255);
        public static final java.awt.Color RESTRICT;

        static {
            try {
                RESTRICT = (java.awt.Color) java.awt.Color.class.getField(colors[3]).get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final class Color {
        public static final java.awt.Color ARROW = new java.awt.Color(244, 152, 46);
        public static final java.awt.Color ARROW_SHADOW = new java.awt.Color(185, 105, 20);
        public static final java.awt.Color SLIDE = new java.awt.Color(90, 120, 250);
        public static final java.awt.Color SLIDE_SHADOW = new java.awt.Color(60, 80, 170);
        public static final java.awt.Color SPIN = new java.awt.Color(102, 253, 63);
        public static final java.awt.Color SPIN_SHADOW = new java.awt.Color(75, 190, 50);
        public static final java.awt.Color TELEPORTATION = new java.awt.Color(121, 79, 249);
        public static final java.awt.Color TELEPORTATION_SHADOW = new java.awt.Color(80, 55, 150);
        public static final java.awt.Color SWITCH_OFF = new java.awt.Color(94, 94, 94);
        public static final java.awt.Color SWITCH_OFF_SHADOW = new java.awt.Color(94, 94, 94);
        public static final java.awt.Color MENU_BG = new java.awt.Color(51, 55, 64);
    }    

    public static final class Colors3D {
        public static final java.awt.Color UTGRAY = new java.awt.Color(60, 60, 60);
        public static final java.awt.Color UTGRAYSHADOW = new java.awt.Color(40, 40, 40);
        public static final java.awt.Color UTEDGE = new java.awt.Color(60, 60, 60);
        public static final java.awt.Color GRAYSHADOW = new java.awt.Color(180, 180, 180);
        public static final java.awt.Color WHITESHADOW = new java.awt.Color(160, 160, 160);
        public static final java.awt.Color BG_CUBE_GRAY1 =  new java.awt.Color(95, 95, 95);
        public static final java.awt.Color BG_CUBE_GRAY2 =  new java.awt.Color(110, 110, 110);
        public static final java.awt.Color BG_CUBE_GRAY3 =  new java.awt.Color(125, 125, 125);
        public static final java.awt.Color BG_CUBE_OUTLINE =  new java.awt.Color(50, 50, 50);
        public static final java.awt.Color BG_GRAY1 =  new java.awt.Color(70, 70, 70);
        public static final java.awt.Color BG_GRAY2 =  new java.awt.Color(75, 75, 75);
        public static final java.awt.Color BG_GRAY3 =  new java.awt.Color(80, 80, 80);
    }

    public static class Buttons {
        public static class ButtonsDisplayMode {
            public static final String BUTTON_NAME = "spriteDisplayMode";
            public static final int B_WIDTH_DEFAULT = 140;
            public static final int B_HEIGHT_DEFAULT = 56;
            public static final int B_SPACING = 100;
            public static final int B_NB_BUTTON = 3;
            // public static final String[] BUTTON_TEXTS = {
            // "JOUER",
            // "EDITEUR",
            // "TERMINAL"
            // };
        }

        public static class ButtonsMenuGraphic {
            public static final String BUTTON_NAME = "button_atlas";
            public static final int B_WIDTH_DEFAULT = 121;
            public static final int B_HEIGHT_DEFAULT = 41;
            public static final int B_SPACING = 40;
            public static final int B_NB_BUTTON = 3;
        }
    }

    public static class Strings {
        public static final String GAME_ASCII= "_________      ___.               _____                .__                    \r\n" + //
                "\\_   ___ \\ __ _\\_ |__   ____     /     \\ _____  ___.__.|  |__   ____   _____  \r\n" + //
                "/    \\  \\/|  |  \\ __ \\_/ __ \\   /  \\ /  \\\\__  \\<   |  ||  |  \\_/ __ \\ /     \\ \r\n" + //
                "\\     \\___|  |  / \\_\\ \\  ___/  /    Y    \\/ __ \\\\___  ||   Y  \\  ___/|  Y Y  \\\r\n" + //
                " \\______  /____/|___  /\\___  > \\____|__  (____  / ____||___|  /\\___  >__|_|  /\r\n" + //
                "        \\/          \\/     \\/          \\/     \\/\\/          \\/     \\/      \\/ ";
    }
}
