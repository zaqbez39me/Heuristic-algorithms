import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

enum ObjectValues  {

    JACK_SPARROW('J'),
    DAVY_JONES('D'),
    KRAKEN('K'),
    ROCK('R'),
    DEAD_MANS_CHEST('C'),
    TORTUGA('T'),
    SEA('_'),
    PERCEPTION_ZONE('*'),
    PATH('@');

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

    public boolean equals(int y, int x){
        return this.x == x && this.y == y;
    }
    public void setCoordinates(int y, int x){
        this.x = x;
        this.y = y;
    }

    public GameMap.Node getByCoordinates(List<List<GameMap.Node>> list){
        return list.get(y).get(x);
    }

    public void setByCoordinates(List<List<GameMap.Node>> list, GameMap.Node node){
        list.get(y).set(x, node);
    }

    public boolean getByCoordinates(boolean [][] matrix){
        return matrix[y][x];
    }

    public void setByCoordinates(boolean [][] matrix, boolean value){
        matrix[y][x] = value;
    }

    public Coordinates getSum(int y, int x){
        return new Coordinates(this.y + y, this.x + x);
    }

}

class Pair<K,V>{
    private K first;
    private V second;
    Pair(K first, V second){
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public void setSecond(V second) {
        this.second = second;
    }
}

class Algorithm{
    int[][] possibleMoves, krakenPerception, krakenSafe;
    int minimum;
    boolean isKrakenAlive = true;
    public Actor actor;
    private LinkedList<Coordinates> best_path;
    public boolean anyPathFound = false;

    public int getHeuristic(Coordinates from, Coordinates to){
        return Math.max(Math.abs(to.x - from.x), Math.abs(to.y - from.y));
    }

    public static boolean inBoundaries(Coordinates coordinates){
        return 0 <= coordinates.x && coordinates.x <= 8 &&
                0 <= coordinates.y && coordinates.y <= 8;
    }

    public LinkedList<Coordinates> getBestPath(){
        return this.best_path;
    }

    Algorithm(Actor actor){
        this.actor = actor;
        possibleMoves = new int[][]{{1, 1}, {1, 0}, {0, 1}, {1, -1}, {-1, 1}, {0, -1}, {-1, 0}, {-1, -1}};
        krakenPerception = new int[][]{{1, 0}, {0, 1}, {0, -1}, {-1, 0}};
        krakenSafe = new int[][]{{1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
        minimum = Integer.MAX_VALUE;
    }

    public LinkedList<Coordinates> getPath(Coordinates start, Coordinates target){
        GameMap.Node tempNode = target.getByCoordinates(actor.getMapInMemory());
        LinkedList<Coordinates> result = new LinkedList<>();
        if(tempNode != null) {
            while (tempNode != null && !tempNode.getCoordinates().equals(start)) {
                result.addFirst(tempNode.getCoordinates());
                tempNode = tempNode.getParent();
            }
            if(tempNode != null)
                result.addFirst(tempNode.getCoordinates());
        }
        return result;
    }

    public GameMap.Node getMatrixNode(Coordinates coordinates){
        return coordinates.getByCoordinates(this.actor.getMapInMemory());
    }

    public void setBestPath(LinkedList<Coordinates> best_path) {
        this.best_path = best_path;
    }

    boolean isKrakenSafe(Coordinates coordinates){
        for (int[] cell : krakenSafe) {
            Coordinates sum = coordinates.getSum(cell[0], cell[1]);
            if (inBoundaries(sum)) {
                if (sum.getByCoordinates(actor.getMapInMemory()) != null &&
                        sum.getByCoordinates(actor.getMapInMemory()).getId() == ObjectValues.KRAKEN.value)
                    return true;
            }
        }
        return false;
    }

    boolean isKrakenPerception(Coordinates coordinates){
        for (int[] cell : krakenPerception) {
            Coordinates sum = coordinates.getSum(cell[0], cell[1]);
            if (inBoundaries(sum)) {
                if (sum.getByCoordinates(actor.getMapInMemory()) != null &&
                        sum.getByCoordinates(actor.getMapInMemory()).getId() == ObjectValues.KRAKEN.value)
                    return true;
            }
        }
        if(inBoundaries(coordinates))
            return coordinates.getByCoordinates(actor.getMapInMemory()).getId() == ObjectValues.KRAKEN.value;
        return false;
    }

    void clearVisited(){
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                GameMap.Node node = new Coordinates(i, j).getByCoordinates(actor.getMapInMemory());
                if (node != null)
                    node.setVisited(false);
            }
        }
    }

    void clearForAStar(boolean haveRum){
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                GameMap.Node node = new Coordinates(i, j).getByCoordinates(actor.getMapInMemory());
                if (node != null) {
                    node.krakenAlive = true;
                    node.haveRum = haveRum;
                    node.setParent(null);
                    node.setCurrentG(Integer.MAX_VALUE);
                    node.setCurrentF(Integer.MAX_VALUE);
                }
            }
        }
    }
}

class AStar extends Algorithm{
    AStar(Actor actor) {
        super(actor);
    }

    public void aStar(GameMap.Node currentNode, Coordinates target){
        boolean [][]closed = new boolean[9][9];
        currentNode.setCurrentG(0);
        actor.setCoordinates(currentNode.getCoordinates());
        actor.explore();
        Comparator<Pair<Integer,  GameMap.Node>> comparator = (x, y) ->
                x.getFirst() > y.getFirst() ? 1 : (x.getFirst().equals(y.getFirst()) ?
                        (Integer.compare(getHeuristic(x.getSecond().getCoordinates(), target),
                                getHeuristic(y.getSecond().getCoordinates(), target)))
                        : -1);
        PriorityQueue<Pair<Integer,  GameMap.Node>> openSet
                = new PriorityQueue<>(comparator);
        openSet.add(new Pair<>(0, currentNode));
        while (!openSet.isEmpty()){
            Pair<Integer, GameMap.Node> currentEntry = openSet.poll();
            currentEntry.getSecond().getCoordinates().setByCoordinates(closed, true);
            GameMap.Node cameFrom = currentEntry.getSecond();
            for (int[] possibleMove : possibleMoves) {
                Coordinates coordinates = cameFrom.getCoordinates().getSum(possibleMove[0], possibleMove[1]);
                if(inBoundaries(coordinates)) {
                    GameMap.Node cameTo = coordinates.getByCoordinates(actor.getMapInMemory());
                    if (coordinates.equals(target)) {
                        coordinates.getByCoordinates(actor.getMapInMemory()).setParent(cameFrom);
                        this.anyPathFound = true;
                        cameTo.krakenAlive = cameFrom.krakenAlive;
                        return;
                    } else if(!coordinates.getByCoordinates(closed)){
                        if(cameTo.howDanger() == 0 || (!cameFrom.krakenAlive ||
                                isKrakenSafe(cameFrom.getCoordinates()) && cameFrom.haveRum)
                                && cameTo.howDanger() == 1 && isKrakenPerception(cameTo.getCoordinates())) {
                            actor.setCoordinates(coordinates);
                            actor.explore();
                            if(cameFrom.getId() == ObjectValues.TORTUGA.value)
                                cameFrom.haveRum = true;
                            int newCurrentG = cameFrom.getCurrentG() + 1;
                            int oldF = cameTo.getCurrentF();
                            int newCurrentF = newCurrentG + getHeuristic(coordinates, target);
                            if (cameTo.getCurrentG() == Integer.MAX_VALUE || oldF > newCurrentF) {
                                openSet.add(new Pair<>(newCurrentF, cameTo));
                                cameTo.setCurrentG(newCurrentG);
                                cameTo.setCurrentF(newCurrentF);
                                cameTo.setParent(cameFrom);
                                cameTo.haveRum = cameFrom.haveRum;
                                cameTo.krakenAlive = (cameFrom.haveRum || !isKrakenSafe(cameFrom.getCoordinates())) && cameFrom.krakenAlive;
                            }
                        }
                    }
                }
            }
        }
    }

    public LinkedList<Coordinates> execute(GameMap.Node currentNode, Coordinates target){
        if(currentNode.howDanger() == 0) {
            boolean tortugaOnStart = currentNode.getId() == ObjectValues.TORTUGA.value;
            actor.setCoordinates(currentNode.getCoordinates());
            actor.explore();
            if(tortugaOnStart)
                for (int i = 0; i < 9; ++i)
                    for (int j = 0; j < 9; ++j) {
                        GameMap.Node node = new Coordinates(i, j).getByCoordinates(actor.getMapInMemory());
                        if (node != null)
                            node.haveRum = true;
                    }
            aStar(currentNode, target);
            if (target.getByCoordinates(actor.getMapInMemory()) != null)
                this.isKrakenAlive = target.getByCoordinates(actor.getMapInMemory()).krakenAlive;
            if (target.getByCoordinates(actor.getMapInMemory()) == null) {
                clearForAStar(true);
                aStar(actor.findTortuga().getByCoordinates(actor.getMapInMemory()), target);
                LinkedList<Coordinates> fromTortuga =
                        getPath(actor.findTortuga(), target);
                if (target.getByCoordinates(actor.getMapInMemory()) != null)
                    this.isKrakenAlive = target.getByCoordinates(actor.getMapInMemory()).krakenAlive;
                if (target.getByCoordinates(actor.getMapInMemory()) != null) {
                    clearForAStar(false);
                    aStar(currentNode, actor.findTortuga());
                    LinkedList<Coordinates> toTortuga = getPath(currentNode.getCoordinates(), actor.findTortuga());
                    for (int i = 0; i < toTortuga.size() - 1; ++i)
                        fromTortuga.addFirst(toTortuga.get(i));
                    return fromTortuga;
                }
                return new LinkedList<Coordinates>();
            }
        } else {
            return new LinkedList<>();
        }
        return getPath(currentNode.getCoordinates(), target);
    }
}

class DFS extends Algorithm{

    DFS(Actor actor) {
        super(actor);
    }

    void visitCoordinates(Coordinates start, Coordinates coordinates, Coordinates target, int currentValue,
                          GameMap.Node currentNode, boolean haveRum, boolean krakenAlive){
        if(inBoundaries(coordinates)) {
            actor.setCoordinates(coordinates);
            actor.explore();
            GameMap.Node nextNode = getMatrixNode(coordinates);
            if (nextNode.notVisited()) {
                if(nextNode.howDanger() == 0) {
                    nextNode.setParent(currentNode);
                    if(nextNode.getId() == ObjectValues.TORTUGA.value)
                        dfs(start, nextNode, currentValue + 1, target,
                                true, krakenAlive);
                    else
                        dfs(start, nextNode, currentValue + 1, target,
                                haveRum, krakenAlive);
                } else if ((!krakenAlive || isKrakenSafe(currentNode.getCoordinates()) && haveRum)
                        && nextNode.howDanger() == 1 && isKrakenPerception(nextNode.getCoordinates())) {
                    nextNode.setParent(currentNode);
                    dfs(start, nextNode, currentValue + 1, target, true, false);
                }
            }
        }
    }

    public void pathExists(Coordinates start, GameMap.Node currentNode, int currentValue, Coordinates target, boolean haveRum, boolean krakenAlive){
        actor.setCoordinates(currentNode.getCoordinates());
        actor.explore();
        if(currentValue + getHeuristic(currentNode.getCoordinates(), target) > this.minimum)
            return;
        if(actor.getCoordinates().equals(target) && minimum > currentValue){
            anyPathFound = true;
            return;
        }
        currentNode.setVisited(true);
        for (int[] possibleMove : this.possibleMoves) {
            Coordinates coordinates = currentNode.getCoordinates().getSum(possibleMove[0], possibleMove[1]);
            if(inBoundaries(coordinates)) {
                actor.setCoordinates(coordinates);
                actor.explore();
                GameMap.Node nextNode = getMatrixNode(coordinates);
                if (nextNode.notVisited()) {
                    if(nextNode.howDanger() == 0) {
                        nextNode.setParent(currentNode);
                        if(nextNode.getId() == ObjectValues.TORTUGA.value)
                            pathExists(start, nextNode, currentValue + 1, target,
                                    true, krakenAlive);
                        else
                            pathExists(start, nextNode, currentValue + 1, target,
                                    haveRum, krakenAlive);
                    } else if ((!krakenAlive || isKrakenSafe(currentNode.getCoordinates()) && haveRum)
                            && nextNode.howDanger() == 1 && isKrakenPerception(nextNode.getCoordinates())) {
                        nextNode.setParent(currentNode);
                        pathExists(start, nextNode, currentValue + 1, target, true, false);
                    }
                }
            }
        }
    }

    public void dfs(Coordinates start, GameMap.Node currentNode, int currentValue, Coordinates target, boolean haveRum, boolean krakenAlive){
        actor.setCoordinates(currentNode.getCoordinates());
        actor.explore();
        if(currentValue + getHeuristic(currentNode.getCoordinates(), target) >= this.minimum || currentValue > 24)
            return;
        if(actor.getCoordinates().equals(target) && minimum > currentValue){
            anyPathFound = true;
            this.isKrakenAlive = krakenAlive;
            minimum = currentValue;
            LinkedList<Coordinates> temp_path = getPath(start, target);
            this.setBestPath(temp_path);
            return;
        }
        currentNode.setVisited(true);
        for (int[] possibleMove : this.possibleMoves) {
            Coordinates coordinates = currentNode.getCoordinates().getSum(possibleMove[0], possibleMove[1]);
            visitCoordinates(start, coordinates, target, currentValue, currentNode, haveRum, krakenAlive);
        }
        currentNode.setVisited(false);
    }

    public LinkedList<Coordinates> execute(GameMap.Node currentNode, Coordinates target){
        if(currentNode.howDanger() == 0) {
            boolean tortugaOnStart = currentNode.getId() == ObjectValues.TORTUGA.value;
            pathExists(currentNode.getCoordinates(), currentNode, 0, target, tortugaOnStart, true);
            clearVisited();
            if (!this.anyPathFound) {
                if (!tortugaOnStart){
                    pathExists(currentNode.getCoordinates(), currentNode, 0, actor.findTortuga(), false, true);
                    if (this.anyPathFound) {
                        clearVisited();
                        this.minimum = Integer.MAX_VALUE;
                        dfs(currentNode.getCoordinates(), currentNode, 0, actor.findTortuga(), false, true);
                        clearVisited();
                        LinkedList<Coordinates> wayToTortuga = null;
                        if(this.getBestPath() != null)
                            wayToTortuga = (LinkedList<Coordinates>) this.getBestPath().clone();
                        int temp = this.minimum;
                        this.minimum = Integer.MAX_VALUE;
                        this.anyPathFound = false;
                        pathExists(actor.findTortuga(), actor.findTortuga().getByCoordinates(actor.getMapInMemory()),
                                0, target, false, true);
                        this.minimum = Integer.MAX_VALUE;
                        if (this.anyPathFound) {
                            dfs(actor.findTortuga(), actor.findTortuga().getByCoordinates(actor.getMapInMemory()),
                                    temp, target, true, true);
                            for (int i = 0; i < wayToTortuga.size() - 1; ++i)
                                this.getBestPath().addFirst(wayToTortuga.get(i));
                            return getBestPath();
                        }
                    }
                }
            }
            else {
                dfs(currentNode.getCoordinates(), currentNode, 0, target, tortugaOnStart, true);
                return this.getBestPath();
            }
        }
        return new LinkedList<>();
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

    private List<List<Node>> matrix;

    public static class Node{
        private char id;
        private short danger = 0;
        private boolean visited = false;
        private Node parent;
        private final Coordinates coordinates;
        private int currentG, currentF;
        boolean krakenAlive = true, haveRum;

        public Node(short danger, Coordinates coordinates, char id, int currentG, int currentF) {
            this.danger = danger;
            this.coordinates = coordinates;
            this.id = id;
            this.currentG = currentG;
            this.currentF = currentF;
        }

        public void increaseDanger() {
            this.danger++;
        }


        public void setCurrentG(int current_value) {
            this.currentG = current_value;
        }

        public void setCurrentF(int currentF) {
            this.currentF = currentF;
        }

        public void setId(char id){
            this.id = id;
        }

        public int getCurrentG() {
            return currentG;
        }

        public int getCurrentF() {
            return currentF;
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

        public boolean notVisited(){
            return !this.visited;
        }

        public short howDanger(){
            return this.danger;
        }
    }

    public GameMap() {

    }

    public boolean initialize(Jack_Sparrow jack_sparrow, Davy_Jones davy_jones, Kraken kraken, Rock rock, DeadMansChest deadMansChest, Tortuga tortuga){
        for (Map_Object map_object: Arrays.asList(jack_sparrow, davy_jones, kraken, rock, deadMansChest, tortuga))
            if(!checkedInsert(map_object)){
                this.map_objects = new ArrayList<>(6);
                return false;
            }
        makeMatrix();
        return true;
    }

    public boolean tryAnyInsertionStarting(Map_Object map_object, int x_begin, int y_begin){
        for(; y_begin < 9; ++y_begin){
            for (; x_begin < 9; ++x_begin){
                map_object.getCoordinates().setCoordinates(y_begin, x_begin);
                if(checkedInsert(map_object)) return true;
            }
        }
        for (y_begin = 0; y_begin < 9; ++y_begin){
            for (x_begin = 0; x_begin < 9; ++x_begin){
                map_object.getCoordinates().setCoordinates(y_begin, x_begin);
                if(checkedInsert(map_object)) return true;
            }
        }
        return false;
    }

    public void generate(Random generator){
        if(!checkedInsert(new Jack_Sparrow(0, 0, this)))
            return;
        List<Map_Object> new_map_objects =  Arrays.asList(new Davy_Jones(this), new Kraken(this),
                new Rock(this), new DeadMansChest(this), new Tortuga(this));
        for(Map_Object map_object: new_map_objects){
            int temp = generator.nextInt(0, 81);
            int i = temp / 9, j = temp % 9;
            map_object.setCoordinates(new Coordinates(j, i));
            if(!tryAnyInsertionStarting(map_object, j, i))
                return;
        }
        makeMatrix();
    }

    public void killKraken(){
        int[][] krakenZone = new int[][]{{1, 0}, {0, 1}, {0, -1}, {-1, 0}};
        Coordinates krakenCoordinates = this.getKraken().getCoordinates();
        int y, x;
        for (int[] ints : krakenZone) {
            y = krakenCoordinates.y + ints[0];
            x = krakenCoordinates.x + ints[1];
            if(Algorithm.inBoundaries(new Coordinates(y, x)))
                if (this.matrix.get(y)
                        .get(x).howDanger() == 1)
                    this.matrix.get(y)
                            .get(x).setId(ObjectValues.SEA.value);
        }
        if(this.matrix.get(krakenCoordinates.y).get(krakenCoordinates.x).howDanger() == 1)
            this.matrix.get(krakenCoordinates.y).get(krakenCoordinates.x).setId(ObjectValues.SEA.value);
        else
            this.matrix.get(krakenCoordinates.y).get(krakenCoordinates.x).setId(ObjectValues.PERCEPTION_ZONE.value);
    }

    public void makeMatrix(){
        matrix = new ArrayList<>(81);
        for(int i = 0; i < 9; ++i) {
            matrix.add(new ArrayList<>());
            for(int j = 0; j < 9; ++j)
                matrix.get(i).add(null);
        }
        for(Map_Object map_object: this.map_objects) {
            if(matrix.get(map_object.getCoordinates().y).get(map_object.getCoordinates().x) != null) {
                matrix.get(map_object.getCoordinates().y).get(map_object.getCoordinates().x).increaseDanger();
                matrix.get(map_object.getCoordinates().y).get(map_object.getCoordinates().x).setId(map_object.get_id());
            } else {
                matrix.get(map_object.getCoordinates().y).set(map_object.getCoordinates().x, new Node((short)
                        (map_object.enemy ? 1 : 0), map_object.getCoordinates(),
                        map_object.get_id(), Integer.MAX_VALUE, Integer.MAX_VALUE));
            }
            for(Coordinates perception: map_object.get_perception_zone())
                if(matrix.get(perception.y).get(perception.x) == null)
                    matrix.get(perception.y).set(perception.x, new Node((short) 1, perception,
                            ObjectValues.PERCEPTION_ZONE.value, Integer.MAX_VALUE, Integer.MAX_VALUE));
                else
                    matrix.get(perception.y).get(perception.x).increaseDanger();
        }
        for(int i = 0; i < 9; ++i) {
            for(int j = 0; j < 9; ++j)
                if(this.matrix.get(i).get(j) == null)
                    matrix.get(i).set(j, new Node((short) 0, new Coordinates(i, j),
                            ObjectValues.SEA.value, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
    }

    public List<List<Node>> getMatrix() {
        return matrix;
    }

    public void print_map(List<Coordinates> path){
        for(Coordinates coordinates: path)
            matrix.get(coordinates.y).get(coordinates.x).setId(ObjectValues.PATH.value);
        for(int i = 0; i < 9; ++i){
            for(int j = 0; j < 9; ++j){
                System.out.printf("%c ", matrix.get(i).get(j).getId());
            }
            System.out.println();
        }
    }

    public boolean checkedInsert(Map_Object new_map_object){
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

    public Jack_Sparrow getJackSparrow() {
        return jack_sparrow;
    }

    public Davy_Jones getDavyJones() {
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
                this.getMap().getDeadMansChest(), this.getMap().getDavyJones()))
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
                this.getMap().getKraken(), this.getMap().getJackSparrow(), this.getMap().getRock()))
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
                this.getMap().getDavyJones(), this.getMap().getJackSparrow()))
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
                this.getMap().getDavyJones(), this.getMap().getJackSparrow()))
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

    public DeadMansChest(int y, int x, GameMap map) {
        super(y, x, map);
    }

    @Override
    public char get_id(){
        return ObjectValues.DEAD_MANS_CHEST.value;
    }

    @Override
    public boolean validate(){
        if(this.getMap().getJackSparrow() != null && this.getMap().getJackSparrow().getCoordinates() != null &&
                this.getMap().getJackSparrow().inside(this)) return false;
        for(Map_Object map_object: Arrays.asList(this.getMap().getKraken(), this.getMap().getDavyJones()))
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
    public Tortuga(int y, int x, GameMap map) {
        super(y, x, map);
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
        for(Map_Object map_object: Arrays.asList(this.getMap().getKraken(), this.getMap().getDavyJones()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.in_danger_zone(this)) return false;
        return true;
    }
}

enum CompassTarget {
    TORTUGA,
    DEAD_MANS_CHEST
}

class Compass{

    private final GameMap gameMap;
    public Compass(GameMap gameMap){
        this.gameMap = gameMap;
    }
    public GameMap.Node show(CompassTarget compassTarget){
        switch (compassTarget){
            case TORTUGA -> {
                return gameMap.getTortuga().getCoordinates().getByCoordinates(gameMap.getMatrix());
            } case DEAD_MANS_CHEST -> {
                return gameMap.getDeadMansChest().getCoordinates().getByCoordinates(gameMap.getMatrix());
            }
        }
        return null;
    }
}

class Actor{
    private final Compass compass;
    private final SpyGlass spyGlass;
    private Coordinates coordinates;

    private final List<List<GameMap.Node>> mapInMemory;
    boolean[][] alreadyExplored;

    public Actor(GameMap environment, Coordinates coordinates, SpyGlass spyGlass){
        this.coordinates = coordinates;
        this.compass = new Compass(environment);
        this.spyGlass = spyGlass;
        this.mapInMemory = new ArrayList<>(81);
        this.alreadyExplored = new boolean[9][9];
        for(int i = 0; i < 9; ++i){
            this.mapInMemory.add(new ArrayList<GameMap.Node>(9));
            for(int j = 0; j < 9; ++j){
                this.mapInMemory.get(i).add(null);
                this.alreadyExplored[i][j] = false;
            }
        }
        findTortuga().setByCoordinates(this.mapInMemory, compass.show(CompassTarget.TORTUGA));
    }

    public void explore(){
        if(!alreadyExplored[coordinates.y][coordinates.x]) {
            spyGlass.explore(this.coordinates, this.mapInMemory);
            alreadyExplored[coordinates.y][coordinates.x] = true;
        }
    }

    public List<List<GameMap.Node>> getMapInMemory() {
        return mapInMemory;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }


    public Coordinates findTortuga(){
        return compass.show(CompassTarget.TORTUGA).getCoordinates();
    }

    public Coordinates findDeadMansChest(){
        return compass.show(CompassTarget.DEAD_MANS_CHEST).getCoordinates();
    }
}
abstract class SpyGlass{
    List<List<GameMap.Node>> map;
    int[][] exploreArea;

    public SpyGlass(List<List<GameMap.Node>> map){
        this.map = map;
    }

    public void explore(Coordinates coordinates, List<List<GameMap.Node>> explorationMap){
        for (int[] cell : exploreArea) {
            Coordinates sum = coordinates.getSum(cell[0], cell[1]);
            if(Algorithm.inBoundaries(sum))
                if (sum.getByCoordinates(explorationMap) == null)
                    sum.setByCoordinates(explorationMap, sum.getByCoordinates(this.map));
        }
    }
}

class UsualSpyGlass extends SpyGlass{

    public UsualSpyGlass(List<List<GameMap.Node>> map) {
        super(map);
        this.exploreArea = new int[][] {{1, 1}, {1, 0}, {0, 1}, {1, -1}, {-1, 1}, {0, -1}, {-1, 0}, {-1, -1}};
    }
}

class SuperSpyGlass extends SpyGlass{

    public SuperSpyGlass(List<List<GameMap.Node>> map) {
        super(map);
        this.exploreArea = new int[][] {{2, 0}, {-2, 0}, {0, 2}, {0, -2}, {1, 1}, {1, 0},
                {0, 1}, {1, -1}, {-1, 1}, {0, -1}, {-1, 0}, {-1, -1}};
    }
}

class StatisticalTest{
    private final int numberOfTests;
    private final List<Long> times;
    private final List<Boolean> wins;
    private final HashMap<Long, Integer> timePopularity;

    StatisticalTest(int numberOfTests) {
        this.numberOfTests = numberOfTests;
        times = new ArrayList<>(numberOfTests);
        wins = new ArrayList<>(numberOfTests);
        timePopularity = new HashMap<Long, Integer>(numberOfTests);
    }

    private double findMean(){
        double result = 0;
        for(long time: this.times)
            result += time;
        return result / this.numberOfTests / 1_000_000;
    }

    private double findMode(){
        long mostPopularTime = -1;
        int maxPopularity = 0;
        for(Map.Entry<Long, Integer> entry: timePopularity.entrySet())
            if(entry.getValue() > maxPopularity){
                maxPopularity = entry.getValue();
                mostPopularTime = entry.getKey();
            }
        return (double) mostPopularTime / 1_000_000.0;
    }

    public double findMedian(){
        if(this.numberOfTests % 2 == 0){
            return (times.get(this.numberOfTests / 2) + times.get(this.numberOfTests / 2 + 1)) / 2_000_000.0;
        } else{
            return times.get(this.numberOfTests / 2) / 1_000_000.0;
        }
    }

    public double getStandardDeviation(double mean){
        double result = 0;
        for (long time: times){
            result += Math.pow(time / 1_000_000.0 - mean, 2);
        }
        return Math.sqrt(result / (this.numberOfTests - 1));
    }

    public void execute(){
        Random random = new Random();
        GameMap gameMap;
        long startT;
        Actor actor;
        SpyGlass spyGlass;
        AStar dfs;
        for (int i = 0; i < this.numberOfTests; ++i){
            gameMap = new GameMap();
            gameMap.generate(random);
            spyGlass = new UsualSpyGlass(gameMap.getMatrix());
            actor =  new Actor(gameMap, gameMap.getJackSparrow().getCoordinates(), spyGlass);
            dfs = new AStar(actor);
            startT = System.nanoTime();
            List<Coordinates> result = dfs.execute(gameMap.getMatrix().get(0).get(0),actor.findDeadMansChest());
            long time = System.nanoTime() - startT;
            times.add(time);
            wins.add(!result.isEmpty());
            int howPopularTime = timePopularity.getOrDefault(time, 0);
            timePopularity.put(time, howPopularTime + 1);
        }
        double mean = findMean();
        double mode = findMode();
        times.sort(Long::compareTo);
        double median = findMedian();
        double standardDeviation = getStandardDeviation(mean);
        System.out.println();
    }
}

public class KirillKorolev {

    private static void dialog() throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        GameMap gameMap = new GameMap();
        Scanner line;
        System.out.println("""
                Choose map generation:
                1) From file "input.txt"
                2) From console
                3) Generate randomly""");
        switch (scanner.nextInt()) {
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
                    Rock rock = new Rock(line.nextInt(), line.nextInt(), gameMap);
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
                gameMap = new GameMap();
                gameMap.generate(random);
                break;
        }
        SpyGlass spyGlass = null;
        int spyGlassType;
        do {
            System.out.println("Enter spy glass type id:");
            spyGlassType = scanner.nextInt();
            switch (spyGlassType) {
                case 1 -> spyGlass = new UsualSpyGlass(gameMap.getMatrix());
                case 2 -> spyGlass = new SuperSpyGlass(gameMap.getMatrix());
            }
        } while (spyGlassType > 2 || spyGlassType < 1);
        Actor actor = new Actor(gameMap, gameMap.getJackSparrow().getCoordinates(), spyGlass);
        AStar dfs = new AStar(actor);
        List<Coordinates> result = dfs.execute(gameMap.getMatrix().get(0).get(0), gameMap.getDeadMansChest().getCoordinates());
        gameMap.print_map(new LinkedList<>());
        if (result.isEmpty())
            System.out.println("No path!");
        else{
            if(!dfs.isKrakenAlive)
                gameMap.killKraken();
            gameMap.print_map(result);
            System.out.println(result.size() - 1);
        }
        StatisticalTest statisticalTest = new StatisticalTest(1000000);
        statisticalTest.execute();
    }

    public static void main(String[] args) throws FileNotFoundException {
        dialog();
    }
}