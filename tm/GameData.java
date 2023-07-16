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

    final int playerCount;
    final List<Faction> factions;
    final List<Integer> bons;
    final List<Round> rounds;
    final Deque<Pair> actionFeed = new ArrayDeque<>();
    final Deque<Pair> leechFeed = new ArrayDeque<>();
    final boolean turnOrderVariant;

    public GameData(int playerCount, int seed) {
        this.playerCount = playerCount;
        final Random random = new Random(seed);

        final List<Round> allRounds = new ArrayList<>(List.of(Round.fireW, Round.firePw, Round.waterP, Round.waterS, Round.earthC, Round.earthS, Round.airW, Round.airS, Round.priestC));
        int spadeRound;
        do {
            Collections.shuffle(allRounds, random);
            spadeRound = allRounds.indexOf(Round.earthC) + 1;
        } while (spadeRound == 5 || spadeRound == 6);
        rounds = allRounds.stream().limit(6).toList();

        bons = new ArrayList<>(playerCount + 3);
        final List<Integer> allBons = new ArrayList<>(IntStream.range(1, 11).boxed().toList());
        Collections.shuffle(allBons, random);
        allBons.stream().limit(playerCount + 3).sorted().forEach(bons::add);

        factions = new ArrayList<>(allFactions);
        Collections.shuffle(factions, random);
        while (factions.size() > playerCount) factions.remove(factions.size() - 1);
        turnOrderVariant = true;
    }

    public GameData(String inputFile) {
        int players = 0;
        factions = new ArrayList<>();
        rounds = new ArrayList<>(6);
        bons = new ArrayList<>(IntStream.range(1, 11).boxed().toList());
        final Map<String, Faction> factionMap = new HashMap<>();
        try {
            final Scanner scanner = new Scanner(new File(inputFile));
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                if (line.matches("Round \\d scoring: SCORE\\d, .*")) {
                    final int scoring = line.split(" ")[3].charAt(5) - '0' - 1;
                    rounds.add(Round.snellmanMapping[scoring]);
                } else if (line.matches("Removing tile BON\\d.*")) {
                    final int bon = Integer.parseInt(line.split("[ \\t]")[2].substring(3));
                    bons.remove((Integer) bon);
                } else if (line.matches("Player \\d: .*")) {
                    ++players;
                } else if (line.matches("[a-z]*\\t.*") && line.endsWith("setup")) {
                    final String faction = line.split("\\t")[0];
                    allFactions.stream().filter(f -> f.getName().equalsIgnoreCase(faction)).findFirst().ifPresent(f -> {
                        factions.add(0, f);
                        factionMap.put(faction, f);
                    });
                } else {
                    final String[] s = line.split("\\t");
                    if (s.length == 0) throw new RuntimeException("Empty line: " + line);
                    final Faction faction = factionMap.get(s[0]);
                    if (faction != null) {
                        final String actionLine = s[s.length - 1];
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
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (players != factions.size()) {
            throw new RuntimeException("Invalid number of factions");
        }
        if (players + 3 != bons.size()) {
            throw new RuntimeException("Invalid number of bons");
        }
        if (rounds.size() != 6) {
            throw new RuntimeException("Invalid number of rounds");
        }
        playerCount = players;
        turnOrderVariant = false;
    }
}
