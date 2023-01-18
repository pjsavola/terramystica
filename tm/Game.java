package tm;

import tm.action.PlaceInitialDwellingAction;
import tm.action.SelectBonAction;
import tm.faction.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import tm.action.Action;

public class Game extends JPanel {
    public enum Phase { INITIAL_DWELLINGS, INITIAL_BONS, ACTIONS, END };

    private final List<Player> players = new ArrayList<>();
    private final Random random = new Random();
    private final List<Integer> bons;
    private final List<Round> rounds;
    private final int[] bonusCoins;
    private final List<Integer> favs;
    private final List<Integer> towns;
    final boolean[] usedPowerActions = new boolean[6];
    private final Grid mapPanel;
    private final Cults cultPanel;
    private final PowerActions powerActionPanel;
    private final TurnOrder turnOrderPanel;
    private final Rounds roundPanel;
    private final Pool pool;
    private int round;

    private final List<Player> turnOrder = new ArrayList<>();
    private final List<Player> nextTurnOrder = new ArrayList<>();

    public Phase phase = Phase.INITIAL_DWELLINGS;

    public Game(int playerCount, String[] mapData) {
        final List<Integer> allBons = new ArrayList<>(IntStream.range(1, 11).boxed().toList());
        Collections.shuffle(allBons, random);
        bons = new ArrayList<>(allBons.stream().limit(playerCount + 3).sorted().toList());
        bonusCoins = new int[3];

        favs = new ArrayList<>(IntStream.range(1, 5).boxed().toList());
        for (int i = 5; i < 13; ++i) {
            favs.add(i);
            favs.add(i);
            favs.add(i);
        }

        towns = new ArrayList<>();
        for (int i = 1; i < 9; ++i) {
            towns.add(i);
            if (i != 6 && i != 8) {
                towns.add(i);
            }
        }

        final List<Round> allRounds = new ArrayList<>(List.of(Round.fireW, Round.firePw, Round.waterP, Round.waterS, Round.earthC, Round.earthS, Round.airW, Round.airS, Round.priestC));
        int spadeRound;
        do {
            Collections.shuffle(allRounds, random);
            spadeRound = allRounds.indexOf(Round.earthC) + 1;
        } while (spadeRound == 5 || spadeRound == 6);
        rounds = allRounds.stream().limit(6).toList();

        final List<Faction> allFactions = new ArrayList<>(List.of(new Alchemists(), new Auren(), new ChaosMagicians(), new Cultists(), new Darklings(), new Dwarves(), new Engineers(), new Fakirs(), new Giants(), new Halflings(), new Mermaids(), new Nomads(), new Swarmlings(), new Witches()));
        Collections.shuffle(allFactions, random);

        Player chaosMagiciansPlayer = null;
        Player nomadsPlayer = null;
        while (playerCount-- > 0) {
            final Player player = new Player(this);
            final Faction faction = allFactions.remove(allFactions.size() - 1);
            player.selectFaction(faction, 20);
            allFactions.removeIf(f -> f.getHomeType() == faction.getHomeType());
            players.add(player);
            nextTurnOrder.add(0, player);
            if (faction instanceof ChaosMagicians) {
                chaosMagiciansPlayer = player;
            } else {
                turnOrder.add(player);
                if (faction instanceof Nomads) {
                    nomadsPlayer = player;
                }
            }
        }
        for (int i = turnOrder.size() - 1; i >= 0; --i) {
            turnOrder.add(turnOrder.get(i));
        }
        if (nomadsPlayer != null) {
            turnOrder.add(nomadsPlayer);
        }
        if (chaosMagiciansPlayer != null) {
            turnOrder.add(chaosMagiciansPlayer);
        }

        mapPanel = new Grid(this, mapData);
        cultPanel = new Cults(players);
        powerActionPanel = new PowerActions(usedPowerActions);
        turnOrderPanel = new TurnOrder(this, turnOrder, nextTurnOrder);
        roundPanel = new Rounds(rounds);
        pool = new Pool(this, bons, bonusCoins, favs, towns);

        final JPanel mapAndCults = new JPanel();
        mapAndCults.add(mapPanel);
        mapAndCults.add(cultPanel);
        add(mapAndCults);

        final JPanel actsAndTurnOrder = new JPanel();
        actsAndTurnOrder.add(powerActionPanel);
        actsAndTurnOrder.add(turnOrderPanel);
        actsAndTurnOrder.setLayout(new BoxLayout(actsAndTurnOrder, BoxLayout.Y_AXIS));

        final JPanel actsTurnOrderAndRounds = new JPanel();
        actsTurnOrderAndRounds.add(actsAndTurnOrder);
        actsTurnOrderAndRounds.add(roundPanel);

        add(actsTurnOrderAndRounds);
        for (Player player : players) {
            player.setBackground(new Color(0xDDDDDD));
            player.setBorder(new LineBorder(Color.BLACK));
            add(player);
        }
        add(pool);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        start();
    }

    public void start() {
    }

    public List<Integer> getAvailableBons() {
        return bons;
    }

    public void selectBon(Player player, int bonIndex) {
        int coins = 0;
        if (bons.size() == 3) {
            coins = bonusCoins[bonIndex];
            bonusCoins[bonIndex] = 0;
        }
        final int newBon = bons.get(bonIndex);
        final int oldBon = player.pickBon(newBon, coins);
        if (oldBon != 0) {
            bons.set(bonIndex, oldBon);
        } else {
            bons.remove(bonIndex);
        }
    }

    public void nextRound() {
        for (int i = 0; i < 6; ++i) {
            usedPowerActions[i] = false;
        }
        for (Player player : players) {
            player.startRound(rounds.get(round));
        }
        for (int i = 0; i < bonusCoins.length; ++i) {
            ++bonusCoins[i];
        }
        ++round;
        roundPanel.round = round;
        turnOrder.addAll(nextTurnOrder);
        nextTurnOrder.clear();
    }

    public boolean cultOccupied(int cult) {
        for (Player player : players) {
            if (player.getCultSteps(cult) == 10) {
                return true;
            }
        }
        return false;
    }

    public void hexClicked(int row, int col) {
        if (phase == Phase.INITIAL_DWELLINGS && !turnOrder.isEmpty()) {
            final Action action = new PlaceInitialDwellingAction(turnOrder.get(0), mapPanel, row, col);
            if (action.canExecute()) {
                action.execute();
                turnOrder.remove(0);
                if (turnOrder.isEmpty()) {
                    turnOrder.addAll(nextTurnOrder);
                    nextTurnOrder.clear();
                    for (int i = turnOrder.size() - 1; i >= 0; --i) {
                        nextTurnOrder.add(turnOrder.get(i));
                    }
                    phase = Phase.INITIAL_BONS;
                }
                repaint();
            }
        }
    }

    public void bonClicked(int index) {
        if (phase == Phase.INITIAL_BONS && !turnOrder.isEmpty()) {
            final Action action = new SelectBonAction(turnOrder.get(0), this, index);
            if (action.canExecute()) {
                action.execute();
                turnOrder.remove(0);
                if (turnOrder.isEmpty()) {
                    turnOrder.addAll(nextTurnOrder);
                    nextTurnOrder.clear();
                    phase = Phase.ACTIONS;
                    nextRound();
                }
                repaint();
            }
        }
    }
}
