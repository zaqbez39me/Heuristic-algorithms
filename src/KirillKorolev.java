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

class Algorithm{
    int[][] possibleMoves;
    int minimum;
    public ArrayList<ArrayList<GameMap.Node>> matrix;
    private ArrayList<Coordinates> best_path;

    public int getHeuristic(Coordinates from, Coordinates to){
        return Math.max(Math.abs(to.x - from.x), Math.abs(to.y - from.y));
    }

    public boolean inBoundaries(Coordinates coordinates){
        return 0 <= coordinates.x && coordinates.x <= 8 &&
                0 <= coordinates.y && coordinates.y <= 8;
    }

    public ArrayList<Coordinates> getBestPath(){
        return this.best_path;
    }

    Algorithm(ArrayList<ArrayList<GameMap.Node>> matrix){
        possibleMoves = new int[][]{{1, 1}, {1, 0}, {0, 1}, {1, -1}, {-1, 1}, {0, -1}, {-1, 0}, {-1, -1}};
        this.matrix = matrix;
        minimum = Integer.MAX_VALUE;
    }

    public GameMap.Node getMatrixNode(Coordinates coordinates){
        return matrix.get(coordinates.y).get(coordinates.x);
    }

    public void setBestPath(ArrayList<Coordinates> best_path) {
        this.best_path = best_path;
    }
}

class DFS extends Algorithm{
    private boolean anyPathFound = false;
    private final Coordinates tortugaCoordinates;
    DFS(ArrayList<ArrayList<GameMap.Node>> matrix, Coordinates tortugaCoordinates) {
        super(matrix);
        this.tortugaCoordinates = tortugaCoordinates;
    }

    private boolean isAround(Coordinates coordinates, char t){
        for(int i = 0; i < 8; ++i){
            if(inBoundaries(new Coordinates(coordinates.y + possibleMoves[i][0], coordinates.x + possibleMoves[i][1]))) {
                if (this.matrix.get(coordinates.y + possibleMoves[i][0]).get(coordinates.x + possibleMoves[i][1]).getId() == t)
                    return true;
            }
        }
        return false;
    }

    public void dfs(Coordinates start, GameMap.Node currentNode, int currentValue, Coordinates target, boolean haveRum, boolean krakenAlive){
        if(currentValue + getHeuristic(currentNode.getCoordinates(), target) > this.minimum)
            return;
        if(currentNode.getCoordinates().y == target.y && currentNode.getCoordinates().x == target.x && minimum > currentValue){
            anyPathFound = true;
            minimum = currentValue;
            GameMap.Node temp_node = currentNode;
            ArrayList<Coordinates> temp_path = new ArrayList<>();
            while(temp_node.getCoordinates().x != start.x || temp_node.getCoordinates().y != start.y){
                temp_path.add(temp_node.getCoordinates());
                temp_node = temp_node.getParent();
            }
            temp_path.add(temp_node.getCoordinates());
            this.setBestPath(temp_path);
            return;
        }
        currentNode.setVisited(true);
        for(int i = 0; i < 8; ++i){
            Coordinates coordinates = new Coordinates(currentNode.getCoordinates().y + possibleMoves[i][0],
                    currentNode.getCoordinates().x + possibleMoves[i][1]);
            if(inBoundaries(coordinates)) {
                GameMap.Node nextNode = getMatrixNode(coordinates);
                if (!nextNode.getVisited()) {
                    if(nextNode.howDanger() == 0) {
                        nextNode.setParent(currentNode);
                        dfs(start, nextNode, currentValue + 1, target, haveRum, krakenAlive);
                    } else if ((!krakenAlive || isAround(currentNode.getCoordinates(), 'K')) && haveRum && nextNode.howDanger() == 1){
                        nextNode.setParent(currentNode);
                        dfs(start, nextNode, currentValue + 1, target, true, false);
                    } else if(!krakenAlive && isAround(nextNode.getCoordinates(), 'K') && nextNode.howDanger() == 1){
                        nextNode.setParent(currentNode);
                        dfs(start, nextNode, currentValue + 1, target, true, false);
                    }
                }
            }
        }
        if(anyPathFound)
            currentNode.setVisited(false);
    }

    public void execute(GameMap.Node currentNode, Coordinates target){
        dfs(currentNode.getCoordinates(), currentNode, 0, target, false, true);
        if(!this.anyPathFound) {
            for(int i = 0; i < 9; ++i)
                for(int j = 0; j < 9; ++j)
                    matrix.get(i).get(j).setVisited(false);
            this.anyPathFound = true;
            dfs(currentNode.getCoordinates(), currentNode, 0, tortugaCoordinates, false, true);
            if(this.getBestPath() != null) {
                ArrayList<Coordinates> wayToTortuga = (ArrayList<Coordinates>) this.getBestPath().clone();
                int temp = this.minimum;
                this.minimum = Integer.MAX_VALUE;
                this.anyPathFound = false;
                dfs(tortugaCoordinates, this.matrix.get(tortugaCoordinates.y).get(tortugaCoordinates.x), temp, target, true, true);
                wayToTortuga.addAll(this.getBestPath());
                this.setBestPath(wayToTortuga);
            }
        }
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

    private ArrayList<ArrayList<Node>> matrix;

    public static class Node{
        private char id;
        private short danger = 0;
        private boolean visited = false;
        private Node parent;
        private final Coordinates coordinates;
        private int current_value;

        public Node(short danger, Coordinates coordinates, char id) {
            this.danger = danger;
            this.coordinates = coordinates;
            this.id = id;
        }

        public void increaseDanger() {
            this.danger++;
        }


        public void setCurrent_value(int current_value) {
            this.current_value = current_value;
        }

        public int getCurrent_value() {
            return current_value;
        }

        public char getId() {
            return id;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Node getParent() {
            return parent;
        }

        public Coordinates getCoordinates() {
            return coordinates;
        }

        public void setVisited(boolean visited) {
            this.visited = visited;
        }

        public boolean getVisited(){
            return this.visited;
        }

        public short howDanger(){
            return this.danger;
        }
    }

    public GameMap() {

    }

    public boolean initialize(Jack_Sparrow jack_sparrow, Davy_Jones davy_jones, Kraken kraken, Rock rock, DeadMansChest deadMansChest, Tortuga tortuga){
        for (Map_Object map_object: Arrays.asList(jack_sparrow, davy_jones, kraken, rock, deadMansChest, tortuga))
            if(!checked_insert(map_object)){
                this.map_objects = new ArrayList<>(6);
                return false;
            }
        make_matrix();
        return true;
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
        make_matrix();
        return true;
    }

    public void make_matrix(){
        matrix = new ArrayList<>(81);
        for(int i = 0; i < 9; ++i) {
            matrix.add(new ArrayList<>());
            for(int j = 0; j < 9; ++j)
                matrix.get(i).add(null);
        }
        for(Map_Object map_object: this.map_objects) {
            matrix.get(map_object.getCoordinates().y).set(map_object.getCoordinates().x, new Node((short) (map_object.enemy ? 1 : 0), map_object.getCoordinates(), map_object.get_id()));
            for(Coordinates perception: map_object.get_perception_zone())
                if(matrix.get(perception.y).get(perception.x) == null)
                    matrix.get(perception.y).set(perception.x, new Node((short) 1,  perception, '*'));
                else
                    matrix.get(perception.y).get(perception.x).increaseDanger();
        }
        for(int i = 0; i < 9; ++i) {
            for(int j = 0; j < 9; ++j)
                if(this.matrix.get(i).get(j) == null)
                    matrix.get(i).set(j, new Node((short) 0, new Coordinates(i, j), '_'));
        }
    }

    public ArrayList<ArrayList<Node>> getMatrix() {
        return matrix;
    }

    public void print_map(){
        ArrayList<ArrayList<Character>> matrix = new ArrayList<>(81);
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
        for(int i = 0; i < 9; ++i){
            for(int j = 0; j < 9; ++j){
                System.out.printf("%c ", matrix.get(i).get(j));
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
    public boolean enemy = false;
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
    public Jack_Sparrow(int y, int x, GameMap map) {
        super(y, x, map);
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

    public Davy_Jones(int y, int x, GameMap map) {
        super(y, x, map);
        enemy = true;
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
    public Kraken(int y, int x, GameMap map) {
        super(y, x, map);
        enemy = true;
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
    public Rock(int y, int x, GameMap map) {
        super(y, x, map);
        enemy = true;
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
        long time = 0;
        switch (scanner.nextInt()){
            case 1:
                scanner = new Scanner(new FileReader("input.txt"));
            case 2:
                while (true) {
                    String temp = (scanner.next() + scanner.nextLine()).replace("[", "").replace("]",
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
                long start = System.nanoTime();
                for(int i = 0; i < 1000; ++i) {
                    gameMap = new GameMap();
                    gameMap.generate(random);
                    DFS dfs = new DFS(gameMap.getMatrix(), gameMap.getTortuga().getCoordinates());
                    long startT = System.nanoTime();
                    dfs.execute(gameMap.getMatrix().get(0).get(0), gameMap.getDeadMansChest().getCoordinates());
                    time += System.nanoTime() - startT;
                }
                System.out.println(time / 1000/ 1000000.0);
        }
        gameMap.print_map();
        DFS dfs = new DFS(gameMap.getMatrix(), gameMap.getTortuga().getCoordinates());
        dfs.execute(gameMap.getMatrix().get(0).get(0), gameMap.getDeadMansChest().getCoordinates());
        if (dfs.getBestPath() == null)
            System.out.println("No path!");
        else{
            System.out.println(dfs.minimum);
            for(int i = 0; i < dfs.getBestPath().size(); ++i)
                System.out.printf("%d %d\n", dfs.getBestPath().get(i).y, dfs.getBestPath().get(i).x);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        dialog();
    }
}