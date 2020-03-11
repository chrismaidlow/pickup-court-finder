package edu.msu.carro228.pickup.Utility;

public final class Utility {

    private static String ROOT = "pickup";

    public static class Games{

        private static String BASE = ".games";

        public static String MODE = ROOT + BASE + ".mode";

        public static int ERROR = -1;
        public static int NULL = 0;
        public static int JOIN = 1;
        public static int MINE = 2;
        public static int JOINED = 3;
    }
}
