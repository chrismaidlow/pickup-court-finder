package edu.msu.carro228.pickup;

import com.google.firebase.database.IgnoreExtraProperties;

import java.sql.Time;
import java.util.Date;

@IgnoreExtraProperties
public class Game {
    public static String TABLE = "pickup_games";
    public static String NAME = "name";
    public static String DESCRIPTION = "description";
    public static String TYPE = "gameType";
    public static String SKILL_LEVEL = "difficulty";
    public static String MAX_PLAYERS = "maxPlayers";
    public static String COST = "cost";
    public static String LATITUDE = "lat";
    public static String LONGITUDE = "lon";
    public static String CURRENT_PLAYERS = "currentPlayers";
    public static String START = "start";
    public static String DATE = "date";

    public String name;
    public String description;
    public String gameType;
    public String difficulty;
    public int maxPlayers;
    public String cost;
    public double lat;
    public double lon;
    public int currentPlayers;
    public String start;
    public String date;

    public Game() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Game(String gameType, String difficulty, String name, int maxPlayers, String description, String cost, double lat, double lon, int currentPlayers, String start, String date) {
        this.name = name;
        this.description = description;
        this.gameType = gameType;
        this.description = description;
        this.difficulty = difficulty;
        this.maxPlayers = maxPlayers;
        this.cost = cost;
        this.lat = lat;
        this.lon = lon;
        this.currentPlayers = currentPlayers;
        this.start = start;
        this.date = date;
    }
}
