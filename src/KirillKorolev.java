import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class Coordinates{
    private final int x, y;
    Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean equals(Coordinates coordinates){
        return this.x == coordinates.x && this.y == coordinates.y;
    }
}
class GameMap{
    private final List<Map_Object> map_objects;
    private final Jack_Sparrow jack_sparrow;
    private final Davy_Jones davy_jones;
    private final Kraken kraken;
    private final Rock rock;
    private final DeadMansChest deadMansChest;
    private final Tortuga tortuga;

    GameMap(Jack_Sparrow jack_sparrow, Davy_Jones davy_jones, Kraken kraken, Rock rock, DeadMansChest deadMansChest, Tortuga tortuga) {
        this.jack_sparrow = jack_sparrow;
        this.davy_jones = davy_jones;
        this.kraken = kraken;
        this.rock = rock;
        this.deadMansChest = deadMansChest;
        this.tortuga = tortuga;
        this.map_objects = new ArrayList<>(Arrays.asList(jack_sparrow, davy_jones, kraken, rock, deadMansChest, tortuga));
    }

    public boolean validate(){
        for (Map_Object map_object : map_objects)
            if(map_object.validate()) return false;
        return true;
    }

    public Jack_Sparrow getJack_sparrow() {
        return jack_sparrow;
    }

    public Davy_Jones getDavy_jones() {
        return davy_jones;
    }

    public Kraken getKraken() {
        return kraken;
    }

    public Rock getRock() {
        return rock;
    }

    public Tortuga getTortuga() {
        return tortuga;
    }

    public DeadMansChest getDeadMansChest() {
        return deadMansChest;
    }
}

class Map_Object{
    private boolean valid_place = false;
    private GameMap map;
    private Coordinates coordinates;

    public Map_Object(int x, int y, GameMap map) {
        this.coordinates = new Coordinates(x, y);
        this.map = map;
    }

    public boolean inside(Map_Object map_object){
        return this.coordinates.equals(map_object.coordinates);
    }

    public boolean in_danger_zone(Map_Object map_object){
        return inside(map_object);
    }

    public GameMap getMap() {
        return map;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public boolean getValid_place(){
        return this.valid_place;
    }

    public void setValid_place(boolean valid_place) {
        this.valid_place = valid_place;
    }

    public boolean validate() {
        return true;
    }
}

class Tortuga extends Kraken{
    public Tortuga(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public boolean validate(){
        if(this.getMap().getDeadMansChest().inside(this)) return false;
        for(Map_Object map_object: Arrays.asList(this.getMap().getKraken(), this.getMap().getDavy_jones()))
            if(map_object.in_danger_zone(this)) return false;
        this.setValid_place(true);
        return true;
    }
}

class Jack_Sparrow extends Map_Object{

    public Jack_Sparrow(int x, int y, GameMap map) {
        super(x, y, map);
    }
}

class Davy_Jones extends Map_Object{

    public Davy_Jones(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public boolean in_danger_zone(Map_Object map_object){
        Coordinates coordinates = map_object.getCoordinates();
        int x_difference = Math.abs(coordinates.getX() - this.getCoordinates().getX()),
        y_difference = Math.abs(coordinates.getY() - this.getCoordinates().getY());
        return x_difference <= 1 && y_difference <= 1;
    }

    @Override
    public boolean validate(){
        for(Map_Object map_object: Arrays.asList(this.getMap().getTortuga(), this.getMap().getDeadMansChest(),
                this.getMap().getKraken(), this.getMap().getJack_sparrow(), this.getMap().getRock()))
            if(!map_object.getValid_place() && map_object.inside(this)) return false;
        this.setValid_place(true);
        return true;
    }
}

class DeadMansChest extends Map_Object{

    public DeadMansChest(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public boolean validate(){
        if(!this.getMap().getJack_sparrow().getValid_place() &&
                this.getMap().getJack_sparrow().inside(this)) return false;
        for(Map_Object map_object: Arrays.asList(this.getMap().getKraken(), this.getMap().getDavy_jones()))
            if(map_object.in_danger_zone(this)) return false;
        this.setValid_place(true);
        return true;
    }
}

class Kraken extends Map_Object{
    public Kraken(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public boolean in_danger_zone(Map_Object map_object){
        Coordinates coordinates = map_object.getCoordinates();
        return Math.abs(coordinates.getX() - this.getCoordinates().getX()) == 1 &&
                coordinates.getY() == this.getCoordinates().getY() ||
                Math.abs(coordinates.getY() - this.getCoordinates().getY()) == 1 &&
                        coordinates.getX() == this.getCoordinates().getX() || coordinates.equals(this.getCoordinates());
    }

    @Override
    public boolean validate(){
        for(Map_Object map_object: Arrays.asList(this.getMap().getTortuga(), this.getMap().getDeadMansChest(),
                this.getMap().getDavy_jones(), this.getMap().getJack_sparrow()))
            if(!map_object.getValid_place() && map_object.inside(this)) return false;
        this.setValid_place(true);
        return true;
    }
}

class Rock extends Map_Object{

    public Rock(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public boolean validate(){
        for(Map_Object map_object: Arrays.asList(this.getMap().getTortuga(), this.getMap().getDeadMansChest(),
                this.getMap().getDavy_jones(), this.getMap().getJack_sparrow()))
            if(!map_object.getValid_place() && map_object.inside(this)) return false;
        this.setValid_place(true);
        return true;
    }
}

public class KirillKorolev {

    public static void main(String[] args) {

    }
}