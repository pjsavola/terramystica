package tm;

import tm.action.Action;
import tm.action.BuildAction;
import tm.faction.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

public class GameData {

    public static class Pair {
        public Faction faction;
        public String action;
    }

    private static final List<Faction> allFactions = List.of(new Alchemists(), new Auren(), new ChaosMagicians(), new Cultists(), new Darklings(), new Dwarves(), new Engineers(), new Fakirs(), new Giants(), new Halflings(), new Mermaids(), new Nomads(), new Swarmlings(), new Witches());
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
        revisedStartingVPs.put("Swarmlings", 22);
        revisedStartingVPs.put("Giants", 25);
        revisedStartingVPs.put("Alchemists", 27);
        revisedStartingVPs.put("Auren", 27);
        revisedStartingVPs.put("Fakirs", 33);
    }

    String[] mapData;
    boolean useRevisedStartingVPs;
    final int playerCount;
    List<String> playerNames;
    final List<Faction> factions;
    final List<Integer> bons;
    final List<Integer> towns;
    final List<Round> rounds;
    final Deque<Pair> actionFeed = new ArrayDeque<>();
    final Deque<Pair> leechFeed = new ArrayDeque<>();
    boolean turnOrderVariant;
    final Map<String, Faction> factionMap = new HashMap<>();

    public GameData(List<String> playerNames, int seed) {
        this.playerNames = playerNames;
        this.playerCount = playerNames.size();
        final Random random = new Random(seed);

        final List<Round> allRounds = new ArrayList<>(List.of(Round.fireW, Round.firePw, Round.waterP, Round.waterS, Round.earthC, Round.earthS, Round.airW, Round.airS, Round.priestC));
        int spadeRound;
        do {
            Collections.shuffle(allRounds, random);
            spadeRound = allRounds.indexOf(Round.earthC) + 1;
        } while (spadeRound == 5 || spadeRound == 6);
        rounds = allRounds.stream().limit(6).toList();

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

        final Set<Hex.Type> selectedColors = new HashSet<>();
        final List<Faction> shuffledFactions = new ArrayList<>(allFactions);
        Collections.shuffle(shuffledFactions);
        factions = new ArrayList<>(playerCount);
        for (Faction faction : shuffledFactions) {
            if (selectedColors.add(faction.getHomeType())) {
                factions.add(faction);
            }
            if (factions.size() == playerCount) {
                break;
            }
        }
        turnOrderVariant = true;
    }

    public GameData(Scanner scanner) {
        mapData = MapData.mapsByName.get("Base").getData();
        factions = new ArrayList<>();
        rounds = new ArrayList<>(6);
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
        if (playerCount != factions.size()) {
            throw new RuntimeException("Invalid number of factions");
        }
        if (playerCount + 3 != bons.size()) {
            throw new RuntimeException("Invalid number of bons");
        }
        if (rounds.size() != 6) {
            throw new RuntimeException("Invalid number of rounds");
        }
    }

    private int readInput(Scanner scanner) {
        playerNames = new ArrayList<>();
        int players = 0;
        boolean start = false;
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine().trim();
            if (line.startsWith("Default game options")) {
                start = true;
                continue;
            } else if (!start) {
                continue;
            }
            if (line.startsWith("option variable-turn-order")) {
                turnOrderVariant = true;
            } else if (line.startsWith("option shipping-bonus")) {
                bons.add(10);
            } else if (line.startsWith("map ")) {
                mapData = MapData.mapsById.get(line.split("\\t")[0].substring(4)).getData();
            } else if (line.startsWith("option mini-expansion-1")) {
                towns.add(6);
                towns.add(7);
                towns.add(7);
                towns.add(8);
            } else if (line.matches("Round \\d scoring: SCORE\\d, .*")) {
                final int scoring = line.split(" ")[3].charAt(5) - '0' - 1;
                rounds.add(Round.snellmanMapping[scoring]);
            } else if (line.matches("Removing tile BON\\d.*")) {
                final int bon = Integer.parseInt(line.split("[ \\t]")[2].substring(3));
                bons.remove((Integer) bon);
            } else if (line.matches("Player \\d: .*")) {
                ++players;
                playerNames.add(line.split(" ")[2]);
            } else if (line.matches("[a-z]*\\t.*") && line.endsWith("setup")) {
                final String factionName = line.split("\\t")[0];
                final Faction faction = allFactions.stream().filter(f -> f.getClass().getSimpleName().equalsIgnoreCase(factionName)).findFirst().orElse(null);
                if (faction != null) {
                    factions.add(0, faction);
                    factionMap.put(factionName, faction);
                } else {
                    System.err.println(factionName + " not found");
                }
            } else {
                final String[] s = line.split("\\t");
                if (s.length == 0) throw new RuntimeException("Empty line: " + line);
                final Faction faction = factionMap.get(s[0]);
                if (faction != null) {
                    final String actionLine = s[s.length - 1];
                    boolean skip = false;
                    for (int i = 0; i < s.length; ++i) {
                        final String str = s[i];
                        if (str.matches("0/0/[1-9][0-9]* PW")) {
                            if (actionLine.matches("[Dd][Ee][Cc][Ll][Ii][Nn][Ee].*")) {
                                // Skip redundant decline
                                skip = true;
                                break;
                            } else if (actionLine.matches("[Ll][Ee][Ee][Cc][Hh].*") && s[i - 1].isEmpty()) {
                                // Skip redundant leech
                                skip = true;
                                break;
                            }
                            break;
                        }
                    }
                    if (skip) continue;
                    if (actionLine.equals("other_income_for_faction")) continue;
                    if (actionLine.equals("cult_income_for_faction")) continue;
                    if (actionLine.equals("[opponent accepted power]")) continue;
                    if (actionLine.equals("[all opponents declined power]")) continue;
                    if (actionLine.matches("\\+\\dvp for (FIRE|WATER|EARTH|AIR)")) continue;
                    if (actionLine.matches("\\+[1-9][0-9]*vp for network")) continue;
                    if (actionLine.equals("score_resources")) continue;
                    final String[] actions = actionLine.split("\\. ");
                    for (String action : actions) {
                        final Pair pair = new Pair();
                        pair.faction = faction;
                        pair.action = action;
                        if (Game.leechPattern.matcher(action).matches()) {
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
}
