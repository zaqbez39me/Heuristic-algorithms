import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

enum ObjectValues  {

    JACK_SPARROW('J'),
    DAVY_JONES('D'),
    KRAKEN('K'),
    ROCK('R'),
    DEAD_MANS_CHEST('C'),
    TORTUGA('T');

    public final char value;

    ObjectValues(char value) {
        this.value = value;
    }
}
class Coordinates {
    public int y, x;

    public Coordinates(int y, int x){
        this.x = x;
        this.y = y;
    }
    public boolean equals(Coordinates coordinates) {
        return this.x == coordinates.x && this.y == coordinates.y;
    }

    public void setCoordinates(int y, int x){
        this.x = x;
        this.y = y;
    }
}

class GameMap{
    private List<Map_Object> map_objects = new ArrayList<>(6);
    private Jack_Sparrow jack_sparrow;
    private Davy_Jones davy_jones;
    private Kraken kraken;
    private Rock rock;
    private DeadMansChest deadMansChest;
    private Tortuga tortuga;
    private ArrayList<ArrayList<Character>> matrix;

    public GameMap() {

    }

    public boolean initialize(Jack_Sparrow jack_sparrow, Davy_Jones davy_jones, Kraken kraken, Rock rock, DeadMansChest deadMansChest, Tortuga tortuga){
        for (Map_Object map_object: Arrays.asList(jack_sparrow, davy_jones, kraken, rock, deadMansChest, tortuga))
            if(!checked_insert(map_object)){
                this.map_objects = new ArrayList<>(6);
                return false;
            }
        this.form_matrix();
        return true;
    }

    private void form_matrix(){
        this.matrix = new ArrayList<>(81);
        for(int i = 0; i < 9; ++i){
            matrix.add(new ArrayList<>());
            for(int j = 0; j < 9; ++j)
                matrix.get(i).add('_');
        }
        for(Map_Object map_object: this.map_objects) {
            matrix.get(map_object.getCoordinates().y).set(map_object.getCoordinates().x, map_object.get_id());
            for(Coordinates perception: map_object.get_perception_zone())
                if(matrix.get(perception.y).get(perception.x) == '_')
                    matrix.get(perception.y).set(perception.x, '*');
        }
    }

    public boolean try_any_insertion_starting(Map_Object map_object, int x_begin, int y_begin){
        for(; y_begin < 9; ++y_begin){
            for (; x_begin < 9; ++x_begin){
                map_object.getCoordinates().setCoordinates(y_begin, x_begin);
                if(checked_insert(map_object)) return true;
            }
        }
        for (y_begin = 0; y_begin < 9; ++y_begin){
            for (x_begin = 0; x_begin < 9; ++x_begin){
                map_object.getCoordinates().setCoordinates(y_begin, x_begin);
                if(checked_insert(map_object)) return true;
            }
        }
        return false;
    }

    public boolean generate(Random generator){
        if(!checked_insert(new Jack_Sparrow(0, 0, this)))
            return false;
        List<Map_Object> new_map_objects =  Arrays.asList(new Davy_Jones(this), new Kraken(this),
                new Rock(this), new DeadMansChest(this), new Tortuga(this));
        for(Map_Object map_object: new_map_objects){
            int temp = generator.nextInt(0, 81);
            int i = temp / 9, j = temp % 9;
            map_object.setCoordinates(new Coordinates(j, i));
            if(!try_any_insertion_starting(map_object, j, i))
                return false;
        }
        this.form_matrix();
        return true;
    }

    public void print_map(){
        for(int i = 0; i < 9; ++i){
            for(int j = 0; j < 9; ++j){
                System.out.print(this.matrix.get(i).get(j));
            }
            System.out.println();
        }
    }

    public boolean validate(){
        for (Map_Object map_object : map_objects)
            if(!map_object.validate()) return false;
        return true;
    }

    public boolean checked_insert(Map_Object new_map_object){
        if (new_map_object.validate()){
            insert(new_map_object);
            return true;
        }
        return false;
    }

    private void insert(Map_Object new_map_object){
        this.map_objects.add(new_map_object);
        if (new_map_object.get_id() == ObjectValues.JACK_SPARROW.value) {
            this.jack_sparrow = (Jack_Sparrow) new_map_object;
        }  else if(new_map_object.get_id() == ObjectValues.DAVY_JONES.value){
            this.davy_jones = (Davy_Jones) new_map_object;
        } else if (new_map_object.get_id() == ObjectValues.KRAKEN.value) {
            this.kraken = (Kraken) new_map_object;
        } else if (new_map_object.get_id() == ObjectValues.ROCK.value) {
            this.rock = (Rock) new_map_object;
        } else if (new_map_object.get_id() == ObjectValues.DEAD_MANS_CHEST.value){
            this.deadMansChest = (DeadMansChest) new_map_object;
        } else if (new_map_object.get_id() == ObjectValues.TORTUGA.value) {
            this.tortuga = (Tortuga) new_map_object;
        }
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
    private final GameMap map;
    private Coordinates coordinates;

    public Map_Object(GameMap map){
        this.map = map;
    }
    public Map_Object(int y, int x, GameMap map) {
        this.coordinates = new Coordinates(y, x);
        this.map = map;
    }

    public char get_id(){
        return '_';
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

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public List<Coordinates> get_perception_zone(){
        return new ArrayList<>();
    }

    public boolean validate() {
        return true;
    }
}

class Jack_Sparrow extends Map_Object{

    public Jack_Sparrow(GameMap map){
        super(map);
    }
    public Jack_Sparrow(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public char get_id(){
        return ObjectValues.JACK_SPARROW.value;
    }

    @Override
    public boolean validate(){
        for(Map_Object map_object: Arrays.asList(this.getMap().getKraken(), this.getMap().getRock(),
                this.getMap().getDeadMansChest(), this.getMap().getDavy_jones()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.inside(this)) return false;
        return true;
    }
}

class Davy_Jones extends Map_Object{
    public Davy_Jones(GameMap map){
        super(map);
    }

    public Davy_Jones(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public char get_id(){
        return ObjectValues.DAVY_JONES.value;
    }

    @Override
    public boolean in_danger_zone(Map_Object map_object){
        Coordinates coordinates = map_object.getCoordinates();
        int x_difference = Math.abs(coordinates.x - this.getCoordinates().x),
        y_difference = Math.abs(coordinates.y - this.getCoordinates().y);
        return x_difference <= 1 && y_difference <= 1;
    }

    @Override
    public List<Coordinates> get_perception_zone(){
        ArrayList<Coordinates> perception_zone = new ArrayList<>();
        int y = this.getCoordinates().y, x = this.getCoordinates().x;
        for(int i = -1; i < 2; ++i){
            for(int j = -1; j < 2; ++j){
                if(i != 0 || j != 0){
                    int perception_x = x + i, perception_y = y + j;
                    if(perception_x >= 0 && perception_x <= 8 && perception_y >= 0 && perception_y <= 8)
                        perception_zone.add(new Coordinates(perception_y, perception_x));
                }
            }
        }
        return perception_zone;
    }

    @Override
    public boolean validate(){
        for(Map_Object map_object: Arrays.asList(this.getMap().getTortuga(), this.getMap().getDeadMansChest(),
                this.getMap().getKraken(), this.getMap().getJack_sparrow(), this.getMap().getRock()))
            if(map_object != null && map_object.getCoordinates() != null && map_object.inside(this)) return false;
        return true;
    }
}

class Kraken extends Map_Object{
    public Kraken(GameMap map){
        super(map);
    }
    public Kraken(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public char get_id(){
        return ObjectValues.KRAKEN.value;
    }


    @Override
    public List<Coordinates> get_perception_zone(){
        ArrayList<Coordinates> perception_zone = new ArrayList<>();
        int y = this.getCoordinates().y, x = this.getCoordinates().x;
        for(int i = -1; i < 2; ++i){
            for(int j = -1; j < 2; ++j){
                if((i != 0 || j != 0) && (Math.abs(i) + Math.abs(j)) < 2){
                    int perception_x = x + i, perception_y = y + j;
                    if(perception_x >= 0 && perception_x <= 8 && perception_y >= 0 && perception_y <= 8)
                        perception_zone.add(new Coordinates(perception_y, perception_x));
                }
            }
        }
        return perception_zone;
    }
    @Override
    public boolean in_danger_zone(Map_Object map_object){
        Coordinates coordinates = map_object.getCoordinates();
        return Math.abs(coordinates.x - this.getCoordinates().x) + Math.abs(coordinates.y - this.getCoordinates().y) <= 1;
    }

    @Override
    public boolean validate(){
        for(Map_Object map_object: Arrays.asList(this.getMap().getTortuga(), this.getMap().getDeadMansChest(),
                this.getMap().getDavy_jones(), this.getMap().getJack_sparrow()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.inside(this)) return false;
        return true;
    }
}

class Rock extends Map_Object{

    public Rock(GameMap map){
        super(map);
    }
    public Rock(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public char get_id(){
        return ObjectValues.ROCK.value;
    }

    @Override
    public boolean validate(){
        for(Map_Object map_object: Arrays.asList(this.getMap().getTortuga(), this.getMap().getDeadMansChest(),
                this.getMap().getDavy_jones(), this.getMap().getJack_sparrow()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.inside(this))
                return false;
        return true;
    }
}

class DeadMansChest extends Map_Object{
    public DeadMansChest(GameMap map){
        super(map);
    }

    public DeadMansChest(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public char get_id(){
        return ObjectValues.DEAD_MANS_CHEST.value;
    }

    @Override
    public boolean validate(){
        if(this.getMap().getJack_sparrow() != null && this.getMap().getJack_sparrow().getCoordinates() != null &&
                this.getMap().getJack_sparrow().inside(this)) return false;
        for(Map_Object map_object: Arrays.asList(this.getMap().getKraken(), this.getMap().getDavy_jones()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.in_danger_zone(this))
                return false;
        return true;
    }
}

class Tortuga extends Map_Object{
    public Tortuga(GameMap map){
        super(map);
    }
    public Tortuga(int x, int y, GameMap map) {
        super(x, y, map);
    }

    @Override
    public char get_id(){
        return ObjectValues.TORTUGA.value;
    }

    @Override
    public boolean validate(){
        for(Map_Object map_object: Arrays.asList(this.getMap().getDeadMansChest(), this.getMap().getRock()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.inside(this)) {
                return false;
            }
        for(Map_Object map_object: Arrays.asList(this.getMap().getKraken(), this.getMap().getDavy_jones()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.in_danger_zone(this)) return false;
        return true;
    }
}

public class KirillKorolev {

    private static void dialog() throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        GameMap gameMap = new GameMap();
        Scanner line;
        System.out.println("Choose map generation:\n" +
                "1) From file \"input.txt\"\n" +
                "2) From console\n" +
                "3) Generate randomly");
        switch (scanner.nextInt()){
            case 1:
                scanner = new Scanner(new FileReader("input.txt"));
            case 2:
                while (true) {
                    scanner.nextLine();
                    String temp = scanner.nextLine().replace("[", "").replace("]",
                            "").replace(",", " ");
                    line = new Scanner(temp);
                    Jack_Sparrow jack_sparrow = new Jack_Sparrow(line.nextInt(), line.nextInt(), gameMap);
                    Davy_Jones davy_jones = new Davy_Jones(line.nextInt(), line.nextInt(), gameMap);
                    Kraken kraken = new Kraken(line.nextInt(), line.nextInt(), gameMap);
                    Rock rock = new Rock(line.nextInt(),line.nextInt(), gameMap);
                    DeadMansChest deadMansChest = new DeadMansChest(line.nextInt(), line.nextInt(), gameMap);
                    Tortuga tortuga = new Tortuga(line.nextInt(), line.nextInt(), gameMap);
                    if (gameMap.initialize(jack_sparrow, davy_jones, kraken, rock, deadMansChest, tortuga))
                        break;
                    else
                        System.out.println("Invalid data! Please, try again!");
                }
                break;
            case 3:
                Random random = new Random();
                long start = System.currentTimeMillis();
                for(int i = 0; i < 1000; ++i)
                    gameMap.generate(random);
                System.out.println(System.currentTimeMillis() - start);
        }
        gameMap.print_map();
    }

    public static void main(String[] args) throws FileNotFoundException {
        dialog();
    }
}