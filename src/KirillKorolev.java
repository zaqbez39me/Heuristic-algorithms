import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;


public class KirillKorolev {

    public static void getResult(File file, GameMap gameMap, SpyGlass spyGlass, boolean backtracking) throws IOException {
        Actor actor = new Actor(gameMap, gameMap.getJackSparrow().getCoordinates(), spyGlass);
        Algorithm algorithm;
        if (backtracking)
            algorithm = new Backtracking(actor);
        else
            algorithm = new AStar(actor);
        PrintStream printStream = new PrintStream(file);
        long start = System.nanoTime();
        List<Coordinates> result = algorithm.execute(gameMap.getMatrix().get(0).get(0),
                gameMap.getDeadMansChest().getCoordinates());
        long resultTime = System.nanoTime() - start;
        if (result.isEmpty())
            printStream.println("Lose");
        else{
            printStream.println("Win");
            printStream.println(result.size() - 1);
            for(Coordinates coordinates: result)
                printStream.printf("[%d,%d] ", coordinates.y, coordinates.x);
            printStream.println();
            if(!algorithm.isKrakenAlive)
                gameMap.killKraken();
            gameMap.printMap(printStream, result);
            printStream.printf("%d ms", resultTime / 1_000_000);
        }
    }

    private static void dialog() throws IOException {
        Scanner scanner = new Scanner(System.in);
        GameMap gameMap = new GameMap();
        Scanner line;
        int mode = -1;
        do {
            try {
                System.out.println("""
                Choose mode:
                1) Generate from the file "input.txt"
                2) Generate from the console
                3) Generate randomly
                4) Get statistical tests""");
                mode = scanner.nextInt();
                switch (mode) {
                    case 1:
                        scanner = new Scanner(new FileReader("input.txt"));
                    case 2:
                        while (true) {
                            try {
                                String temp = (scanner.next() + scanner.nextLine()).replace("[", "").replace("]",
                                        "").replace(",", " ");
                                line = new Scanner(temp);
                                JackSparrow jack_sparrow = new JackSparrow(line.nextInt(), line.nextInt(), gameMap);
                                DavyJones davy_jones = new DavyJones(line.nextInt(), line.nextInt(), gameMap);
                                Kraken kraken = new Kraken(line.nextInt(), line.nextInt(), gameMap);
                                Rock rock = new Rock(line.nextInt(), line.nextInt(), gameMap);
                                DeadMansChest deadMansChest = new DeadMansChest(line.nextInt(), line.nextInt(), gameMap);
                                Tortuga tortuga = new Tortuga(line.nextInt(), line.nextInt(), gameMap);
                                if (gameMap.initialize(jack_sparrow, davy_jones, kraken, rock, deadMansChest, tortuga)) {
                                    break;
                                } else
                                    System.out.println("Invalid data! Please, try again!");
                            } catch (Exception exception) {
                                System.out.println("Invalid data! Please, try again!");
                            }
                        }
                        break;
                    case 3:
                        Random random = new Random();
                        gameMap = new GameMap();
                        gameMap.generate(random);
                        break;
                    case 4:
                        int numberOfTests = 1000;
                        File statistics = new File("outputStatistics.txt");
                        PrintStream printStream = new PrintStream(statistics);
                        printStream.println("Total statistics:");
                        StatisticalTest statisticalTest = new StatisticalTest(numberOfTests);
                        statisticalTest.execute(printStream, AlgorithmValues.A_STAR);
                        statisticalTest = new StatisticalTest(numberOfTests);
                        statisticalTest.execute(printStream, AlgorithmValues.A_STAR_SUPER);
                        statisticalTest = new StatisticalTest(numberOfTests);
                        statisticalTest.execute(printStream, AlgorithmValues.BACKTRACKING);
                        statisticalTest = new StatisticalTest(numberOfTests);
                        statisticalTest.execute(printStream, AlgorithmValues.BACKTRACKING_SUPER);
                        return;
                }
            }catch (Exception exception){
                System.out.println("Invalid data! Please, try again!");
            }
        } while (mode > 4 || mode < 1);
        SpyGlass spyGlass = null;
        int spyGlassType = -1;
        do {
            try {
                System.out.println("Enter spy glass type id(1 - SpyGlass, 2 - SuperSpyGlass):");
                spyGlassType = scanner.nextInt();
                switch (spyGlassType) {
                    case 1 -> spyGlass = new UsualSpyGlass(gameMap.getMatrix());
                    case 2 -> spyGlass = new SuperSpyGlass(gameMap.getMatrix());
                }
            }catch (Exception exception){
                    System.out.println("Invalid data! Please, try again!");
            }
        } while (spyGlassType > 2 || spyGlassType < 1);
        File fileAStarr = new File("outputAStar.txt"),
                fileBacktracking = new File("outputBacktracking.txt");
        getResult(fileAStarr, gameMap, spyGlass, false);
        gameMap.clearMap();
        switch (spyGlassType) {
            case 1 -> spyGlass = new UsualSpyGlass(gameMap.getMatrix());
            case 2 -> spyGlass = new SuperSpyGlass(gameMap.getMatrix());
        }
        getResult(fileBacktracking, gameMap, spyGlass, true);
    }

    public static void main(String[] args) throws IOException {
        dialog();
    }
}

enum ObjectValues  {

    JACK_SPARROW('J'),
    DAVY_JONES('D'),
    KRAKEN('K'),
    ROCK('R'),
    DEAD_MANS_CHEST('C'),
    TORTUGA('T'),
    SEA('_'),
    PERCEPTION_ZONE('*'),
    KRAKEN_AND_ROCK('S'),
    TORTUGA_AND_JACK('G'),
    PATH('@');

    public final char value;

    ObjectValues(char value) {
        this.value = value;
    }
}

enum AlgorithmValues{
    A_STAR,
    A_STAR_SUPER,
    BACKTRACKING,
    BACKTRACKING_SUPER
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

abstract class Algorithm{
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

    boolean checkForKrakenOnArray(Coordinates coordinates, int[][] array){
        for (int[] cell : array) {
            Coordinates sum = coordinates.getSum(cell[0], cell[1]);
            if (inBoundaries(sum)) {
                if (sum.getByCoordinates(actor.getMapInMemory()) != null &&
                        (sum.getByCoordinates(actor.getMapInMemory()).getId() == ObjectValues.KRAKEN.value ||
                                sum.getByCoordinates(actor.getMapInMemory()).getId() == ObjectValues.KRAKEN_AND_ROCK.value)
                )
                    return true;
            }
        }
        return false;
    }
    boolean isKrakenSafe(Coordinates coordinates){
        return checkForKrakenOnArray(coordinates, this.krakenSafe);
    }

    boolean isKrakenPerception(Coordinates coordinates){
        if(checkForKrakenOnArray(coordinates, this.krakenPerception))
            return true;
        if(inBoundaries(coordinates))
            return coordinates.getByCoordinates(actor.getMapInMemory()).getId() == ObjectValues.KRAKEN.value;
        return false;
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

    abstract LinkedList<Coordinates> execute(GameMap.Node currentNode, Coordinates target);
}

class AStar extends Algorithm{
    AStar(Actor actor) {
        super(actor);
    }

    public void aStar(GameMap.Node currentNode, Coordinates target){
        boolean [][]closed = new boolean[9][9];
        currentNode.setCurrentG(0);
        currentNode.setCurrentF(getHeuristic(currentNode.getCoordinates(), target));
        actor.setCoordinates(currentNode.getCoordinates());
        actor.explore();
        Comparator<Node<Integer,  GameMap.Node>> comparator = (x, y) ->
                x.getKey() > y.getKey() ? 1 : (x.getKey().equals(y.getKey()) ?
                        (Integer.compare(getHeuristic(x.getValue().getCoordinates(), target),
                                getHeuristic(y.getValue().getCoordinates(), target)))
                        : -1);
        PriorityQueue<Integer,  GameMap.Node> openSet = new PriorityQueue<>(comparator);
        openSet.insert(new Node<>(0, currentNode));
        while (!openSet.isEmpty()){
            Node<Integer, GameMap.Node> currentEntry = openSet.extractMin();
            currentEntry.getValue().getCoordinates().setByCoordinates(closed, true);
            GameMap.Node cameFrom = currentEntry.getValue();
            if(cameFrom.getId() == ObjectValues.TORTUGA.value) {
                cameFrom.haveRum = true;
                if (isKrakenSafe(cameFrom.getCoordinates()))
                    cameFrom.krakenAlive = false;
            }
            for (int[] possibleMove : possibleMoves) {
                Coordinates coordinates = cameFrom.getCoordinates().getSum(possibleMove[0], possibleMove[1]);
                if(inBoundaries(coordinates)) {
                    GameMap.Node cameTo = coordinates.getByCoordinates(actor.getMapInMemory());
                    if (coordinates.equals(target) && cameTo.howDanger() == 0) {
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
                            if(cameTo.getId() == ObjectValues.TORTUGA.value){
                                cameTo.haveRum = true;
                                if (isKrakenSafe(cameTo.getCoordinates()))
                                    cameTo.krakenAlive = false;
                            }
                            int newCurrentG = cameFrom.getCurrentG() + 1;
                            int oldF = cameTo.getCurrentF();
                            int newCurrentF = newCurrentG + getHeuristic(coordinates, target);
                            if (cameTo.getCurrentG() == Integer.MAX_VALUE || oldF > newCurrentF) {
                                openSet.insert(new Node<>(newCurrentF, cameTo));
                                cameTo.setCurrentG(newCurrentG);
                                cameTo.setCurrentF(newCurrentF);
                                cameTo.setParent(cameFrom);
                                cameTo.haveRum = cameFrom.haveRum;
                                cameTo.krakenAlive = !(cameFrom.haveRum && isKrakenSafe(cameFrom.getCoordinates())) && cameFrom.krakenAlive;
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
            boolean withTortugaAlive = true, withoutTortugaAlive = true;
            aStar(currentNode, target);
            if (target.getByCoordinates(actor.getMapInMemory()) != null)
                withoutTortugaAlive = target.getByCoordinates(actor.getMapInMemory()).krakenAlive;
            LinkedList<Coordinates> withoutTortuga = null;
            LinkedList<Coordinates> toTortuga = null;
            if (anyPathFound)
                withoutTortuga = getPath(currentNode.getCoordinates(), target);
            clearForAStar(true);
            this.anyPathFound = false;
            aStar(actor.findTortuga().getByCoordinates(actor.getMapInMemory()), target);
            LinkedList<Coordinates> fromTortuga = null;
            if(anyPathFound)
                fromTortuga = getPath(actor.findTortuga(), target);
            if (target.getByCoordinates(actor.getMapInMemory()) != null && anyPathFound) {
                clearForAStar(false);
                this.anyPathFound = false;
                aStar(currentNode, actor.findTortuga());
                if(this.anyPathFound) {
                    toTortuga = getPath(currentNode.getCoordinates(), actor.findTortuga());
                    if (target.getByCoordinates(actor.getMapInMemory()) != null) {
                        withTortugaAlive = !target.getByCoordinates(actor.getMapInMemory()).krakenAlive ||
                                !actor.findTortuga().getByCoordinates(actor.getMapInMemory()).krakenAlive;
                    }
                }
                else
                    if(withoutTortuga == null)
                        return new LinkedList<>();
                if(toTortuga != null && fromTortuga != null)
                    for (int i = toTortuga.size() - 2; i >= 0; --i)
                        fromTortuga.addFirst(toTortuga.get(i));
            }
            if(withoutTortuga == null) {
                this.isKrakenAlive = withTortugaAlive;
                return Objects.requireNonNullElseGet(fromTortuga, LinkedList::new);
            } else {
                if (fromTortuga != null && !fromTortuga.isEmpty()){
                    if(fromTortuga.size() < withoutTortuga.size() && toTortuga != null) {
                        this.isKrakenAlive = withTortugaAlive;
                        return fromTortuga;
                    } else {
                        this.isKrakenAlive = withoutTortugaAlive;
                        return withoutTortuga;
                    }
                } else {
                    this.isKrakenAlive = withoutTortugaAlive;
                    return withoutTortuga;
                }
            }
        } else {
            return new LinkedList<>();
        }
    }
}

class Backtracking extends Algorithm{

    short[][] best_values = new short[9][9];

    Backtracking(Actor actor) {
        super(actor);
        for (short[] best_value : best_values) Arrays.fill(best_value, Short.MAX_VALUE);
    }

    private void clearVisited(){
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                GameMap.Node node = new Coordinates(i, j).getByCoordinates(actor.getMapInMemory());
                if (node != null)
                    node.setVisited(false);
                best_values[i][j] = Short.MAX_VALUE;
            }
        }
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
                        if(isKrakenSafe(nextNode.getCoordinates()))
                            backtracking(start, nextNode, currentValue + 1, target,
                                    true, false);
                        else
                            backtracking(start, nextNode, currentValue + 1, target,
                                    true, krakenAlive);
                    else
                        backtracking(start, nextNode, currentValue + 1, target,
                                haveRum, krakenAlive);
                } else if ((!krakenAlive || isKrakenSafe(currentNode.getCoordinates()) && haveRum)
                        && nextNode.howDanger() == 1 && isKrakenPerception(nextNode.getCoordinates())) {
                    nextNode.setParent(currentNode);
                    backtracking(start, nextNode, currentValue + 1, target, true, false);
                }
            }
        }
    }

    public void pathExists(Coordinates start, GameMap.Node currentNode, int currentValue, Coordinates target, boolean haveRum, boolean krakenAlive){
        actor.setCoordinates(currentNode.getCoordinates());
        actor.explore();
        if(currentValue + getHeuristic(currentNode.getCoordinates(), target) > this.minimum)
            return;
        if(actor.getCoordinates().equals(target) && minimum > currentValue) {
            this.setBestPath(getPath(start, target));
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
                        if(nextNode.getId() == ObjectValues.TORTUGA.value) {
                            pathExists(start, nextNode, currentValue + 1, target,
                                    true, krakenAlive);
                        }
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

    public void backtracking(Coordinates start, GameMap.Node currentNode, int currentValue, Coordinates target, boolean haveRum, boolean krakenAlive){
        actor.setCoordinates(currentNode.getCoordinates());
        actor.explore();
        if(currentValue + getHeuristic(currentNode.getCoordinates(), target) >= this.minimum || currentValue > 24)
            return;
        if(best_values[currentNode.getCoordinates().y][currentNode.getCoordinates().x] < currentValue)
            return;
        else
            best_values[currentNode.getCoordinates().y][currentNode.getCoordinates().x] = (short) currentValue;


        if(actor.getCoordinates().equals(target) && minimum > currentValue){
            anyPathFound = true;
            this.isKrakenAlive = krakenAlive;
            minimum = currentValue;
            this.setBestPath(getPath(start, target));
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
            LinkedList<Coordinates> throughTortuga = null;
            if (!tortugaOnStart){
                pathExists(currentNode.getCoordinates(), currentNode, 0, actor.findTortuga(), false, true);
                clearVisited();
                if (this.anyPathFound) {
                    this.minimum = Integer.MAX_VALUE;
                    backtracking(currentNode.getCoordinates(), currentNode, 0, actor.findTortuga(), false, true);
                    LinkedList<Coordinates> wayToTortuga = null;
                    if(this.getBestPath() != null)
                        wayToTortuga = (LinkedList<Coordinates>) this.getBestPath().clone();
                    int temp = this.minimum;
                    clearVisited();
                    this.minimum = Integer.MAX_VALUE;
                    this.anyPathFound = false;
                    pathExists(actor.findTortuga(), actor.findTortuga().getByCoordinates(actor.getMapInMemory()),
                            temp, target, true, true);
                    if (this.anyPathFound) {
                        clearVisited();
                        this.minimum = Integer.MAX_VALUE;
                        backtracking(actor.findTortuga(), actor.findTortuga().getByCoordinates(actor.getMapInMemory()),
                                temp, target, true, true);
                        for (int i = Objects.requireNonNull(wayToTortuga).size() - 2; i >= 0; --i)
                            this.getBestPath().addFirst(wayToTortuga.get(i));
                        throughTortuga = getBestPath();
                        clearVisited();
                        this.minimum = Integer.MAX_VALUE;
                    }
                }
            }
            this.setBestPath(null);
            pathExists(currentNode.getCoordinates(), currentNode, 0, target, tortugaOnStart, true);
            if(this.anyPathFound) {
                clearVisited();
                this.minimum = Integer.MAX_VALUE;
                backtracking(currentNode.getCoordinates(), currentNode, 0, target, tortugaOnStart, true);
            }
            if(throughTortuga != null) {
                if(getBestPath() != null) {
                    if (getBestPath().size() < throughTortuga.size())
                        return getBestPath();
                    else
                        return throughTortuga;
                } else
                    return throughTortuga;
            }
            else
                if(getBestPath() != null)
                    return getBestPath();
        }
        return new LinkedList<>();
    }
}

class GameMap{

    private List<MapObject> map_objects = new ArrayList<>(6);
    private JackSparrow jack_sparrow;
    private DavyJones davy_jones;
    private Kraken kraken;
    private Rock rock;
    private DeadMansChest deadMansChest;
    private Tortuga tortuga;

    private List<List<Node>> matrix;

    public static class Node{
        private char id;
        private short danger;
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

    public void clearMap(){
        makeMatrix();
    }

    public GameMap() {

    }

    public boolean initialize(JackSparrow jack_sparrow, DavyJones davy_jones, Kraken kraken, Rock rock, DeadMansChest deadMansChest, Tortuga tortuga){
        for (MapObject map_object: Arrays.asList(jack_sparrow, davy_jones, kraken, rock, deadMansChest, tortuga))
            if(!checkedInsert(map_object)){
                this.map_objects = new ArrayList<>(6);
                return false;
            }
        makeMatrix();
        return true;
    }

    public boolean tryAnyInsertionStarting(MapObject map_object, int x_begin, int y_begin){
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
        if(!checkedInsert(new JackSparrow(0, 0, this)))
            return;
        List<MapObject> new_map_objects =  Arrays.asList(new DavyJones(this), new Kraken(this),
                new Rock(this), new DeadMansChest(this), new Tortuga(this));
        for(MapObject map_object: new_map_objects){
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
            krakenCoordinates.getByCoordinates(this.matrix).setId(ObjectValues.SEA.value);
        else
            if(krakenCoordinates.getByCoordinates(this.matrix).getId() == ObjectValues.KRAKEN_AND_ROCK.value)
                krakenCoordinates.getByCoordinates(this.matrix).setId(ObjectValues.ROCK.value);
            else
                krakenCoordinates.getByCoordinates(this.matrix).setId(ObjectValues.SEA.value);
    }

    public void makeMatrix(){
        matrix = new ArrayList<>(81);
        for(int i = 0; i < 9; ++i) {
            matrix.add(new ArrayList<>());
            for(int j = 0; j < 9; ++j)
                matrix.get(i).add(null);
        }
        for(MapObject map_object: this.map_objects) {
            Node currentNode = map_object.getCoordinates().getByCoordinates(matrix);
            if(currentNode != null) {
                currentNode.increaseDanger();
                if(currentNode.getId() == ObjectValues.KRAKEN.value && map_object.get_id() == ObjectValues.ROCK.value ||
                   currentNode.getId() == ObjectValues.ROCK.value && map_object.get_id() == ObjectValues.KRAKEN.value)
                    currentNode.setId(ObjectValues.KRAKEN_AND_ROCK.value);
                else if(currentNode.getId() == ObjectValues.JACK_SPARROW.value && map_object.get_id() == ObjectValues.TORTUGA.value ||
                        currentNode.getId() == ObjectValues.TORTUGA.value && map_object.get_id() == ObjectValues.JACK_SPARROW.value)
                    currentNode.setId(ObjectValues.TORTUGA_AND_JACK.value);
                else
                    currentNode.setId(map_object.get_id());
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

    public void printMap(PrintStream printStream, List<Coordinates> path) {
        for(Coordinates coordinates: path)
            matrix.get(coordinates.y).get(coordinates.x).setId(ObjectValues.PATH.value);
        printStream.print("  ");
        for (int i = 0; i < 9; ++i)
            printStream.printf("%d ", i);
        printStream.println();
        for(int i = 0; i < 9; ++i){
            printStream.printf("%d ", i);
            for(int j = 0; j < 9; ++j){
                printStream.printf("%c ", matrix.get(i).get(j).getId());
            }
            printStream.println();
        }
    }

    public boolean checkedInsert(MapObject new_map_object){
        if (new_map_object.validate()){
            insert(new_map_object);
            return true;
        }
        return false;
    }

    private void insert(MapObject new_map_object){
        this.map_objects.add(new_map_object);
        if (new_map_object.get_id() == ObjectValues.JACK_SPARROW.value) {
            this.jack_sparrow = (JackSparrow) new_map_object;
        }  else if(new_map_object.get_id() == ObjectValues.DAVY_JONES.value){
            this.davy_jones = (DavyJones) new_map_object;
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

    public JackSparrow getJackSparrow() {
        return jack_sparrow;
    }

    public DavyJones getDavyJones() {
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

class MapObject {
    public boolean enemy = false;
    private final GameMap map;
    private Coordinates coordinates;

    public MapObject(GameMap map){
        this.map = map;
    }
    public MapObject(int y, int x, GameMap map) {
        this.coordinates = new Coordinates(y, x);
        this.map = map;
    }

    public char get_id(){
        return '_';
    }

    public boolean inside(MapObject map_object){
        return this.coordinates.equals(map_object.coordinates);
    }

    public boolean in_danger_zone(MapObject map_object){
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

/**
 * <h2>Class of a Jack Sparrow</h2>
 * This the class of the Jack Sparrow {@code MapObject} with its own position validator. It is used in
 * {@code GameMap} generation and initialization.
 */
class JackSparrow extends MapObject {

    public JackSparrow(int y, int x, GameMap map) {
        super(y, x, map);
    }

    @Override
    public char get_id(){
        return ObjectValues.JACK_SPARROW.value;
    }

    @Override
    public boolean validate(){
        for(MapObject map_object: Arrays.asList(this.getMap().getKraken(), this.getMap().getRock(),
                this.getMap().getDeadMansChest(), this.getMap().getDavyJones()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.inside(this)) return false;
        return true;
    }
}

/**
 * <h2>Class of a Davy Jones</h2>
 * This the class of the Davy Jones {@code MapObject} with its own position validator. It is used in
 * {@code GameMap} generation and initialization. Also provides an ability to get specific perception zone.
 */
class DavyJones extends MapObject {
    public DavyJones(GameMap map){
        super(map);
    }

    public DavyJones(int y, int x, GameMap map) {
        super(y, x, map);
        enemy = true;
    }

    @Override
    public char get_id(){
        return ObjectValues.DAVY_JONES.value;
    }

    @Override
    public boolean in_danger_zone(MapObject map_object){
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
        for(MapObject map_object: Arrays.asList(this.getMap().getTortuga(), this.getMap().getDeadMansChest(),
                this.getMap().getKraken(), this.getMap().getJackSparrow(), this.getMap().getRock()))
            if(map_object != null && map_object.getCoordinates() != null && map_object.inside(this)) return false;
        return true;
    }
}

/**
 * <h2>Class of a Kraken</h2>
 * This the class of the Rock {@code MapObject} with its own position validator. It is used in
 * {@code GameMap} generation and initialization. Also provides an ability to get specific perception zone.
 */
class Kraken extends MapObject {
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
    public boolean in_danger_zone(MapObject map_object){
        Coordinates coordinates = map_object.getCoordinates();
        return Math.abs(coordinates.x - this.getCoordinates().x) + Math.abs(coordinates.y - this.getCoordinates().y) <= 1;
    }

    @Override
    public boolean validate(){
        for(MapObject map_object: Arrays.asList(this.getMap().getTortuga(), this.getMap().getDeadMansChest(),
                this.getMap().getDavyJones(), this.getMap().getJackSparrow()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.inside(this)) return false;
        return true;
    }
}

/**
 * <h2>Class of a Rock</h2>
 * This the class of the Rock {@code MapObject} with its own position validator. It is used in
 * {@code GameMap} generation and initialization.
 */
class Rock extends MapObject {

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
        for(MapObject map_object: Arrays.asList(this.getMap().getTortuga(), this.getMap().getDeadMansChest(),
                this.getMap().getDavyJones(), this.getMap().getJackSparrow()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.inside(this))
                return false;
        return true;
    }
}

/**
 * <h2>Class of a Tortuga</h2>
 * This the class of the Dead Man's chest {@code MapObject} with its own position validator. It is used in
 * {@code GameMap} generation and initialization.
 */
class DeadMansChest extends MapObject {
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
        if(this.getMap().getJackSparrow() != null && this.getMap().getRock() != null &&
                this.getMap().getRock().getCoordinates() != null &&
                this.getMap().getJackSparrow().getCoordinates() != null &&
                this.getMap().getJackSparrow().inside(this) ||
                this.getMap().getRock().inside(this)
        )
            return false;
        for(MapObject map_object: Arrays.asList(this.getMap().getKraken(), this.getMap().getDavyJones()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.in_danger_zone(this))
                return false;
        return true;
    }
}

/**
 * <h2>Class of a Tortuga</h2>
 * This the class of the Tortuga {@code MapObject} with its own position validator. It is used in
 * {@code GameMap} generation and initialization.
 */
class Tortuga extends MapObject {
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
        for(MapObject map_object: Arrays.asList(this.getMap().getDeadMansChest(), this.getMap().getRock()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.inside(this)) {
                return false;
            }
        for(MapObject map_object: Arrays.asList(this.getMap().getKraken(), this.getMap().getDavyJones()))
            if(map_object != null && map_object.getCoordinates() != null &&
                    map_object.in_danger_zone(this)) return false;
        return true;
    }
}

/**
 * <h3>Enumeration of targets for Compass</h3>
 */
enum CompassTarget {
    TORTUGA,
    DEAD_MANS_CHEST
}

/**
 * <h2>Class of a compass</h2>
 * This the class that provides the main agent an ability to find coordinates of Tortuga and Dead Man's Chest.
 * To interact with main method {@code show} you need to provide a target in {@code CompassTarget} form.
 */
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


/**
 * <h2>Class of a main actor</h2>
 * This the class that provides the main logic for an agent. Here are implemented different methods, which provides
 * abstraction over our technical assignment. Actor can find Tortuga or Dead Man's Chest using special methods of
 * {@code Compass}. It also allows main agent to explore map using his {@code SpyGlass} and save all this data on
 * his own map.
 */
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
            this.mapInMemory.add(new ArrayList<>(9));
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
    private final HashMap<Long, Integer> timePopularity;

    StatisticalTest(int numberOfTests) {
        this.numberOfTests = numberOfTests;
        times = new ArrayList<>(numberOfTests);
        timePopularity = new HashMap<>(numberOfTests);
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

    public void execute(PrintStream printStream, AlgorithmValues algorithmValue){
        Random random = new Random();
        GameMap gameMap;
        long start;
        Actor actor;
        SpyGlass spyGlass;
        Algorithm algorithm;
        int numberOfWins = 0, numberOfLoses = 0;
        for (int i = 0; i < this.numberOfTests; ++i){
            gameMap = new GameMap();
            gameMap.generate(random);
            if(algorithmValue == AlgorithmValues.A_STAR_SUPER || algorithmValue == AlgorithmValues.BACKTRACKING_SUPER)
                spyGlass = new SuperSpyGlass(gameMap.getMatrix());
            else
                spyGlass = new UsualSpyGlass(gameMap.getMatrix());
            actor =  new Actor(gameMap, gameMap.getJackSparrow().getCoordinates(), spyGlass);
            if(algorithmValue == AlgorithmValues.A_STAR || algorithmValue == AlgorithmValues.A_STAR_SUPER)
                algorithm = new AStar(actor);
            else
                algorithm = new Backtracking(actor);
            start = System.nanoTime();
            List<Coordinates> result = algorithm.execute(gameMap.getMatrix().get(0).get(0),actor.findDeadMansChest());
            long time = System.nanoTime() - start;
            times.add(time);
            if(result.isEmpty())
                ++numberOfLoses;
            else
                ++numberOfWins;
            int howPopularTime = timePopularity.getOrDefault(time, 0);
            timePopularity.put(time, howPopularTime + 1);
        }
        double mean = findMean();
        double mode = findMode();
        times.sort(Long::compareTo);
        double median = findMedian();
        double standardDeviation = getStandardDeviation(mean);
        printStream.println();
        switch (algorithmValue){
            case A_STAR -> printStream.println("A* (SPYGLASS) results:");
            case A_STAR_SUPER -> printStream.println("A* (SUPER SPYGLASS) results:");
            case BACKTRACKING -> printStream.println("Backtracking (SPYGLASS) results:");
            case BACKTRACKING_SUPER -> printStream.println("Backtracking (SUPER SPYGLASS) results:");
        }
        printStream.printf("""
                        \tMean: %f ms
                        \tMode: %f ms
                        \tMedian: %f ms
                        \tStandard deviation: %f ms
                        \tNumber of wins: %d
                        \tNumber of loses: %d
                        """,
                mean, mode, median, standardDeviation, numberOfWins, numberOfLoses);
    }
}


/**
 * <h1>Priority Queue interface</h1>
 * @param <K> - Key type template
 * @param <V> - Value type template
 */
interface IPriorityQueue<K extends Comparable<K>,V> {
    void insert(Node<K, V> item);
    Node<K, V> findMin();
    Node<K, V> extractMin();
    void decreaseKey(Node<K, V> item, K newKey) throws Exception;
    void delete(Node<K, V> item);
    void union(PriorityQueue<K, V> anotherQueue);
}

/**
 * <h1>DoublyLinkedCircularList for nodes class</h1>
 * @param <K> - Key type template
 * @param <V> - Value type template
 */
class DoublyLinkedCircularList<K, V> implements Iterable<Node<K, V>>{
    Node<K, V> head;
    Node<K, V> back;
    int size;
    boolean isEmpty;

    /**
     * <h2>DoublyLinkedCircularList Iterator class</h2>
     */
    public class Iterator implements java.util.Iterator<Node<K, V>> {
        private Node<K, V> current_node;
        private final DoublyLinkedCircularList<K, V> collection;
        private int index;
        private final int iterable_size;

        /**
         * <h2>Iterator constructor</h2>
         * @param collection {@code DoublyLinkedCircularList <K, V>}
         */
        public Iterator (DoublyLinkedCircularList<K, V> collection){
            this.current_node = collection.head;
            this.collection = collection;
            this.index = 0;
            this.iterable_size = collection.size;
        }

        /**
         * <h2>Iterator has next</h2>
         * @return {@code boolean} hasNext
         */
        @Override
        public boolean hasNext() {
            return index < iterable_size;
        }

        /**
         * <h2>Get next iterator value</h2>
         * @return next {@code Node<K, V>}
         */
        @Override
        public Node<K, V> next() {
            Node<K, V> temp = this.current_node;
            this.current_node = this.current_node.right;
            ++index;
            return temp;
        }

    }

    /**
     * <h2>empty DoublyLinkedCircularList constructor</h2>
     */
    public DoublyLinkedCircularList(){
        this.head = this.back = null;
        this.size = 0;
        isEmpty = true;
    }

    /**
     * <h2>DoublyLinkedCircularList constructor</h2>
     * @param first_entry {@code Node<K, V>}
     */
    public DoublyLinkedCircularList(Node<K, V> first_entry){
        this.head = this.back = first_entry;
        this.head.left = this.head;
        this.head.right = this.head;
        this.size = 1;
        this.isEmpty = false;
    }

    /**
     * <h2>List add {@code Node<K, V>} item</h2>
     */
    public void add(Node<K, V> item){
        if(this.isEmpty) {
            this.head = this.back = item;
            this.head.left = this.head;
            this.head.right = this.head;
        } else {
            item.left = this.back;
            this.back.right = item;
            this.back = item;
            this.head.left = item;
            item.right = this.head;
        }
        this.isEmpty = false;
        ++size;
    }

    /**
     * <h2>Remove {@code Node<K, V>}</h2>
     */
    public void remove(Node<K, V> item){
        if(size != 0) {
            if(item == this.back){
                this.back = this.back.left;
                this.head.left = this.back;
                this.back.right = this.head;
            } else if(item == this.head){
                this.head = this.head.right;
                this.back.right = this.head;
                this.head.left = this.back;
            } else{
                item.left.right = item.right;
                item.right.left = item.left;
            }
        }
    }

    /**
     * <h2>Remove {@code Node<K, V>} and get next {@code Node<K, V>}</h2>
     * @param item - item to delete
     * @return next {@code Node<K, V>}
     */
    public Node<K, V> remove_and_get_next(Node<K, V> item){
        if(size != 0) {
            remove(item);
            if(--size == 0) this.isEmpty = true;
            return item.right;
        }
        return null;
    }

    /**
     * <h2>Print list method</h2>
     */
    public void print(){
        if(!isEmpty) {
            for (Node<K, V> kvNode : this) {
                System.out.print(kvNode.getValue() + " ");
            }
        }
        System.out.println();
    }

    /**
     * <h2>getter DoublyLinkedCircularList<K, V> iterator</h2>
     * @return {@code DoublyLinkedCircularList<K, V>} iterator
     */
    @Override
    public Iterator iterator() {
        return new Iterator(this);
    }
}

/**
 * <h1>Node class</h1>
 * @param <K> - Key type template
 * @param <V> - Value type template
 */
class Node<K, V>{
    private int degree;
    private boolean loser;
    private Node<K, V> parent;
    private DoublyLinkedCircularList<K, V>  children;
    private K key;
    private final V value;
    protected Node<K, V> left;
    protected Node<K, V> right;
    private DoublyLinkedCircularList<K, V> siblings_list;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
        this.left = null;
        this.right = null;
    }

    public int getDegree() {
        return degree;
    }

    public boolean isLoser() {
        return loser;
    }

    public Node<K, V> getParent() {
        return parent;
    }

    public DoublyLinkedCircularList<K, V> getChildren() {
        return children;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public DoublyLinkedCircularList<K, V> getSiblings_list() {
        return siblings_list;
    }

    public void setLoser(boolean loser) {
        this.loser = loser;
    }

    public void setParent(Node<K, V> parent) {
        this.parent = parent;
    }

    public void setChildren(DoublyLinkedCircularList<K, V> children) {
        this.children = children;
    }

    public void setSiblings_list(DoublyLinkedCircularList<K, V> siblings_list) {
        this.siblings_list = siblings_list;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public void  setKey(K key){
        this.key = key;
    }
}

/**
 * <h1>Priority Queue class</h1>
 * @param <K> - Key type template
 * @param <V> - Value type template
 */
class PriorityQueue<K extends Comparable<K>, V> implements IPriorityQueue<K, V>{
    //number of nodes
    private int n;
    //minimal node
    private Node <K, V> min;
    //list of roots

    private final Comparator<Node<K, V>> comparator;
    DoublyLinkedCircularList<K, V> root_list;
    //fibonacci number
    private final double fibonacci_number = Math.log((1 + Math.sqrt(5))/ 2);

    /**
     * <h2>Priority Queue constructor</h2>
     */
    public PriorityQueue (Comparator<Node<K,V>> comparator) {
        this.comparator = comparator;
        this.min = null;
        this.n = 0;
    }

    /**
     * <h2>Get max possible {@code int} degree</h2>
     * @return {@code int} degree
     */
    private int get_max_degree(){
        return (int) Math.ceil(Math.log(this.n) / this.fibonacci_number);
    }

    /**
     * <h2>Consolidate</h2>
     */
    private void consolidate(){
        int max_degree = get_max_degree() + 1;
        //create an array for all possible degrees
        List<Node<K, V>> A = new ArrayList<>(max_degree);
        int i = 0;
        //fill an array with zeros
        for(;i < max_degree;++i)
            A.add(null);
        //iterate root list
        for(Node<K, V> node : this.root_list){
            Node<K, V> x = node;
            int d = x.getDegree();
            //loop to merge all nodes with a same degree
            while (A.get(d) != null){
                Node<K, V> y = A.get(d);
                //merge nodes
                if(comparator.compare(x,y) > 0){
                    heap_link(x, y);
                    x = y;
                } else
                    heap_link(y, x);
                A.set(d, null);
                ++d;
            }
            A.set(d, x);
        }
        this.min = null;
        for(i = 0; i < max_degree; ++i){
            Node<K, V> current_node = A.get(i);
            if(current_node != null){
                if(this.min == null){
                    this.root_list = new DoublyLinkedCircularList<K, V>(current_node);
                    this.min = current_node;
                } else{
                    this.root_list.add(current_node);
                    if(this.comparator.compare(current_node, this.min) <= 0)
                        this.min = current_node;
                }
            }
        }
    }

    /**
     * <h2>Heap link</h2>
     * @param new_child {@code Node<K, V>}
     * @param new_parent {@code Node<K, V>}
     */
    private void heap_link(Node<K, V> new_child, Node<K, V> new_parent){
        this.root_list.remove(new_child);
        if(new_parent.getChildren() != null) {
            new_parent.getChildren().add(new_child);
            new_child.setSiblings_list(new_parent.getChildren());
        }else{
            DoublyLinkedCircularList<K, V> new_list = new DoublyLinkedCircularList<>(new_child);
            new_child.setSiblings_list(new_list);
            new_parent.setChildren(new_list);
        }
        new_child.setParent(new_parent);
        new_parent.setDegree(new_parent.getDegree() + 1);
        new_parent.setParent(null);
        new_child.setLoser(false);
    }

    /**
     * <h2>Cut {@code Node<K, V>}</h2>
     * @param parent - {@code Node<K, V>}
     * @param child - {@code Node<K, V>}
     */
    private void cut(Node<K, V> parent, Node<K, V> child){
        parent.getChildren().remove(child);
        parent.setDegree(parent.getDegree() - 1);
        this.root_list.add(child);
        child.setSiblings_list(this.root_list);
        child.setParent(null);
        child.setLoser(false);
    }

    /**
     * <h2>Recursive cut {@code Node<K, V>}</h2>
     * @param node - {@code Node<K, V>}
     */
    private void recursive_cut(Node<K, V> node){
        Node<K, V> parent = node.getParent();
        if(parent != null){
            if(!parent.isLoser())
                parent.setLoser(true);
            else {
                cut(parent, node);
                recursive_cut(parent);
            }
        }
    }

    /**
     * <h2>Find minimal {@code Node<K, V>}</h2>
     * @return minimal {@code Node<K, V>}
     */
    @Override
    public Node<K, V> findMin() {
        return this.min;
    }

    /**
     * <h2>Extract minimal {@code Node<K, V>} from PriorityQueue</h2>
     * @return minimal {@code Node<K, V>}
     */
    @Override
    public Node<K, V> extractMin() {
        Node<K, V> temp_node = this.min;
        if(temp_node != null){
            if(temp_node.getChildren() != null) {
                for (Node<K, V> child : temp_node.getChildren()) {
                    this.root_list.add(child);
                    child.setParent(null);
                }
            }
            Node<K, V> right_from_removed = this.root_list.remove_and_get_next(temp_node);
            if(temp_node.equals(right_from_removed)){
                this.min = null;
            } else{
                this.min = right_from_removed;
                consolidate();
            }
            --this.n;
        }
        return temp_node;
    }

    /**
     * <h2>Priority Queue insertion method</h2>
     * @param item {@code Node<K, V>}
     */
    @Override
    public void insert(Node<K, V> item) {
        item.setDegree(0);
        item.setParent(null);
        item.setChildren(null);
        item.setLoser(false);
        if(this.min == null){
            this.root_list = new DoublyLinkedCircularList<>();
            item.setSiblings_list(this.root_list);
            this.root_list.add(item);
            this.min = item;
        } else{
            item.setSiblings_list(this.root_list);
            this.root_list.add(item);
            if(this.comparator.compare(item, this.min) < 0)
                this.min = item;
        }
        ++this.n;
    }

    /**
     * <h2>Decrease {@code K} key</h2>
     * @param item - {@code Node<K, V>}
     * @param newKey - {@code K}
     */
    @Override
    public void decreaseKey(Node<K, V> item, K newKey) throws Exception {
        if(newKey.compareTo(item.getKey()) > 0)
            throw new Exception("New key is larger than current node key");
        item.setKey(newKey);
        Node<K, V> parent = item.getParent();
        if(parent != null && this.comparator.compare(item, parent) < 0){
            cut(parent, item);
            recursive_cut(parent);
        }
        if(this.comparator.compare(item, this.min) < 0)
            this.min = item;
    }

    /**
     * <h2>Delete node</h2>
     * @param item - {@code Node<K, V>}
     */
    @Override
    public void delete(Node<K, V> item) {
        if(item == this.min)
            extractMin();
        else{
            Node<K, V> parent = item.getParent();
            if(parent != null){
                cut(parent, item);
                recursive_cut(parent);
            }
            for (Node<K, V> iter : item.getChildren())
                this.root_list.add(iter);
            this.root_list.remove(item);
        }
    }

    /**
     * <h2>Union of PriorityQueues</h2>
     * @param anotherQueue - {@code PriorityQueue<K, V>}
     */
    @Override
    public void union(PriorityQueue<K, V> anotherQueue) {
        if(this.n > anotherQueue.n){
            for(Node<K, V> node : anotherQueue.min.getSiblings_list()){
                this.insert(node);
            }
        } else{
            for(Node<K, V> node : this.root_list){
                anotherQueue.insert(node);
            }
            this.n = anotherQueue.n;
            this.min = anotherQueue.min;
        }
    }

    /**
     * <h2>Check whether the priority queue is empty</h2>
     **/
    public boolean isEmpty(){
        return this.min == null;
    }
}
