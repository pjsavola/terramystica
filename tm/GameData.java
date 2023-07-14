package tm;

import tm.faction.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

public class GameData {

    private static final List<Faction> allFactions = List.of(new Alchemists(), new Auren(), new ChaosMagicians(), new Cultists(), new Darklings(), new Dwarves(), new Engineers(), new Fakirs(), new Giants(), new Halflings(), new Mermaids(), new Nomads(), new Swarmlings(), new Witches());

    final int playerCount;
    final List<Faction> factions;
    final List<Integer> bons;
    final List<Round> rounds;

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
    }

    public GameData(String inputFile) {
        int players = 0;
        factions = new ArrayList<>();
        rounds = new ArrayList<>(6);
        bons = new ArrayList<>(IntStream.range(1, 11).boxed().toList());
        try {
            final Scanner scanner = new Scanner(new File(inputFile));
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                if (line.matches("Round \\d scoring: SCORE\\d, .*")) {
                    final int scoring = line.split(" ")[3].charAt(5) - '0' - 1;
                    rounds.add(Round.snellmanMapping[scoring]);
                } else if (line.matches("Removing tile BON\\d.*")) {
                    final int bon = Integer.parseInt(line.split("[ \\t]")[2].substring(3));
                    bons.remove(bon - 1);
                } else if (line.matches("Player \\d: .*")) {
                    ++players;
                } else if (line.matches("[a-z]*\\t.*") && line.endsWith("setup")) {
                    final String faction = line.split("\\t")[0];
                    allFactions.stream().filter(f -> f.getName().equalsIgnoreCase(faction)).findFirst().ifPresent(f -> factions.add(0, f));
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
    }
}