package tm;

import tm.action.Action;
import tm.faction.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

public class GameData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static class Pair {
        public Faction faction;
        public String action;
        public int idx;
    }

    public static final List<Faction> allFactions = List.of(new Acolytes(), new Alchemists(), new Auren(), new ChaosMagicians(), new Cultists(), new Darklings(), new Dragonlords(), new Dwarves(), new Engineers(), new Fakirs(), new Giants(), new Halflings(), new IceMaidens(), new Mermaids(), new Nomads(), new Riverwalkers(), new Shapeshifters(), new Swarmlings(), new Witches(), new Yetis());
    public static final Map<String, Integer> revisedStartingVPs = new HashMap<>();

    static {
        revisedStartingVPs.put("Darklings", 15);
        revisedStartingVPs.put("Cultists", 16);
        revisedStartingVPs.put("Engineers", 16);
        revisedStartingVPs.put("ChaosMagicians", 19);
        revisedStartingVPs.put("Mermaids", 19);
        revisedStartingVPs.put("Nomads", 19);
        revisedStartingVPs.put("Witches", 19);
        revisedStartingVPs.put("Dwarves", 20);
        revisedStartingVPs.put("Halflings", 20);
        revisedStartingVPs.put("IceMaidens", 20);
        revisedStartingVPs.put("Riverwalkers", 20); // TODO: Check
        revisedStartingVPs.put("Shapeshifters", 20); // TODO: Check
        revisedStartingVPs.put("Swarmlings", 22);
        revisedStartingVPs.put("Yetis", 22);
        revisedStartingVPs.put("Acolytes", 23);
        revisedStartingVPs.put("Dragonlords", 24);
        revisedStartingVPs.put("Giants", 25);
        revisedStartingVPs.put("Alchemists", 27);
        revisedStartingVPs.put("Auren", 27);
        revisedStartingVPs.put("Fakirs", 33);
    }

    String[] mapData;
    boolean useRevisedStartingVPs;
    boolean useAuction;
    boolean chooseFactions;
    boolean turnOrderVariant;

    final int playerCount;
    List<String> playerNames;
    Map<String, Integer> results;

    final List<Integer> factionsIndices;
    final List<Integer> bons;
    final List<Integer> towns;
    final List<Integer> roundIndices;
    String extraScoring;
    final transient Deque<Pair> actionFeed = new ArrayDeque<>();
    final transient Deque<Pair> leechFeed = new ArrayDeque<>();
    transient List<Action> history;
    final transient Map<String, Faction> factionMap = new HashMap<>();
    transient boolean silentMode;
    final transient List<String> logs = new ArrayList<>();

    public GameData(List<String> playerNames, int seed) {
        this.playerNames = playerNames;
        this.playerCount = playerNames.size();
        final Random random = new Random(seed);

        final List<Integer> allRounds = new ArrayList<>(Round.snellmanMapping.length);
        int spadeRound = -1;
        for (int i = 0; i < Round.snellmanMapping.length; ++i) {
            allRounds.add(i);
            if (Round.snellmanMapping[i] == Round.earthC) {
                spadeRound = i;
            }
        }
        do {
            Collections.shuffle(allRounds, random);
        } while (allRounds.get(4) == spadeRound || allRounds.get(5) == spadeRound);
        roundIndices = allRounds.stream().limit(6).toList();

        towns = new ArrayList<>();
        for (int i = 1; i < 9; ++i) {
            towns.add(i);
            if (i != 6 && i != 8) {
                towns.add(i);
            }
        }

        bons = new ArrayList<>(playerCount + 3);
        final List<Integer> allBons = new ArrayList<>(IntStream.range(1, 11).boxed().toList());
        Collections.shuffle(allBons, random);
        allBons.stream().limit(playerCount + 3).sorted().forEach(bons::add);

        if (chooseFactions) {
            factionsIndices = Collections.emptyList();
        } else {
            final Set<Hex.Type> selectedColors = new HashSet<>();
            final List<Faction> shuffledFactions = new ArrayList<>(allFactions);
            Collections.shuffle(shuffledFactions);
            factionsIndices = new ArrayList<>(playerCount);
            for (Faction faction : shuffledFactions) {
                if (selectedColors.add(faction.getHomeType())) {
                    factionsIndices.add(allFactions.indexOf(faction));
                }
                if (factionsIndices.size() == playerCount) {
                    break;
                }
            }
        }
        turnOrderVariant = true;
    }

    public GameData(Scanner scanner) {
        mapData = MapData.mapsByName.get("Base").getData();
        factionsIndices = new ArrayList<>();
        roundIndices = new ArrayList<>(6);
        towns = new ArrayList<>();
        for (int i = 1; i < 6; ++i) {
            towns.add(i);
            towns.add(i);
        }

        turnOrderVariant = false;
        bons = new ArrayList<>(IntStream.range(1, 10).boxed().toList());
        final Map<String, Faction> factionMap = new HashMap<>();
        playerCount = readInput(scanner);
        if (playerCount == 0) {
            throw new RuntimeException("Invalid input");
        }
        if (playerCount != factionsIndices.size()) {
            throw new RuntimeException("Invalid number of factions");
        }
        if (playerCount + 3 != bons.size()) {
            throw new RuntimeException("Invalid number of bons");
        }
        if (roundIndices.size() != 6) {
            throw new RuntimeException("Invalid number of rounds");
        }
    }

    private int readInput(Scanner scanner) {
        playerNames = new ArrayList<>();
        int players = 0;
        boolean start = false;
        boolean scanningResults = false;
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine().trim().toLowerCase();
            if (!start) {
                if (line.startsWith("default game options")) {
                    start = true;
                } else if (line.startsWith("the game is over")) {
                    scanningResults = true;
                } else if (scanningResults) {
                    if (line.startsWith("info")) {
                        scanningResults = false;
                    } else {
                        final int nameStart = line.indexOf('(') + 1;
                        final int nameEnd = line.lastIndexOf(')');
                        final String name = line.substring(nameStart, nameEnd);
                        final int pts = Integer.parseInt(line.substring(nameEnd + 1).trim());
                        if (results == null) {
                            results = new HashMap<>();
                        }
                        results.put(name, pts);
                        //System.err.println(name + " " + pts);
                    }
                }
                continue;
            }
            if (line.startsWith("option variable-turn-order")) {
                turnOrderVariant = true;
            } else if (line.startsWith("option shipping-bonus")) {
                bons.add(10);
            } else if (line.startsWith("map ")) {
                final String id = line.split("\\t")[0].substring(4);
                if (!MapData.mapsById.containsKey(id)) {
                    throw new RuntimeException("Unknown map: " + id);
                }
                mapData = MapData.mapsById.get(id).getData();
                if (MapData.mapsById.get(id).getName().equals("Base")) {
                    useRevisedStartingVPs = true;
                }
            } else if (line.startsWith("option mini-expansion-1")) {
                towns.add(6);
                towns.add(7);
                towns.add(7);
                towns.add(8);
            } else if (line.matches("round [1-6] scoring: score[1-9]\\d*, .*")) {
                final int scoring = line.split(" ")[3].charAt(5) - '0';
                if (scoring > Round.snellmanMapping.length) {
                    throw new RuntimeException("Undefined round: " + scoring);
                }
                roundIndices.add(scoring - 1);
            } else if (line.matches("removing tile bon\\d.*")) {
                final int bon = Integer.parseInt(line.split("[ \\t]")[2].substring(3));
                if (!bons.remove((Integer) bon)) {
                    throw new RuntimeException("Unknown bon: " + bon);
                }
            } else if (line.matches("player \\d: .*")) {
                ++players;
                final String name = line.split(" ")[2].split("\\t")[0];
                playerNames.add(name);
            } else if (line.matches("[a-z]*\\t.*") && line.endsWith("setup")) {
                final String factionName = line.split("\\t")[0];
                final Faction faction = allFactions.stream().filter(f -> f.getClass().getSimpleName().equalsIgnoreCase(factionName)).findFirst().orElse(null);
                if (faction != null) {
                    factionsIndices.add(0, allFactions.indexOf(faction));
                    factionMap.put(factionName, faction);
                } else {
                    throw new RuntimeException("Faction " + factionName + " not found");
                }
            } else if (line.matches("added final scoring tile: .*")) {
                extraScoring = line.split(" ")[4].split("\\t")[0];
            } else {
                final String[] s = line.split("\\t");
                if (s.length == 0) throw new RuntimeException("Empty line: " + line);
                final Faction faction = factionMap.get(s[0]);
                if (faction != null) {
                    final String actionLine = s[s.length - 1];
                    boolean skip = false;
                    for (int i = 0; i < s.length; ++i) {
                        final String str = s[i];
                        if (str.matches("0/0/[1-9][0-9]* pw")) {
                            if (actionLine.matches("decline.*")) {
                                // Skip redundant decline
                                skip = true;
                                break;
                            } else if (actionLine.matches("leech.*") && s[i - 1].isEmpty()) {
                                // Skip redundant leech
                                skip = true;
                                break;
                            }
                            break;
                        }
                    }
                    if (skip) continue;
                    if (actionLine.equals("[opponent accepted power]")) continue;
                    if (actionLine.equals("[all opponents declined power]")) continue;
                    if (actionLine.matches("\\+\\dvp for (fire|water|earth|air)")) continue;
                    if (actionLine.matches("\\+[1-9]\\d*vp for network")) continue;
                    if (actionLine.equals("score_resources")) continue;
                    final String[] actions = actionLine.split("\\. ");
                    for (String action : actions) {
                        if (action.equals("all_income_for_faction")) continue;
                        if (action.equals("other_income_for_faction")) continue;
                        if (action.equals("cult_income_for_faction")) continue;
                        if (action.equals("wait")) continue;

                        final Pair pair = new Pair();
                        pair.faction = faction;
                        pair.action = action;
                        pair.idx = leechFeed.size() + actionFeed.size() + 1;
                        if (Game.Parser.leechPattern.matcher(action).matches()) {
                            leechFeed.addLast(pair);
                        } else {
                            actionFeed.addLast(pair);
                        }
                    }
                }
            }
        }
        return players;
    }

    public List<Faction> getFactions() {
        return factionsIndices.stream().map(allFactions::get).toList();
    }

    public List<Round> getRounds() {
        return roundIndices.stream().map(i -> Round.snellmanMapping[i]).toList();
    }

    public void printAndClearLogs() {
        for (String s : logs) {
            System.err.println(s);
        }
        logs.clear();
    }
}
