package tm;

import tm.action.*;
import tm.action.Action;
import tm.faction.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class Game extends JPanel {
    public enum Phase { PICK_FACTIONS, AUCTION_FACTIONS, INITIAL_DWELLINGS, INITIAL_BONS, ACTIONS, CONFIRM_ACTION, END };

    private final List<Player> players = new ArrayList<>();
    private final List<Integer> bons = new ArrayList<>();
    private final List<Round> rounds = new ArrayList<>();
    private final int[] bonusCoins = new int[3];
    private final List<Integer> favs = new ArrayList<>();
    private final List<Integer> towns = new ArrayList<>();
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
    private final List<Action> history = new ArrayList<>();
    private final List<Action> newActions = new ArrayList<>();
    private boolean pendingPass;

    public Phase phase;


    private final String[] mapData;
    private final int playerCount;
    private final int seed;

    public Game(int playerCount, String[] mapData, int seed) {
        this.playerCount = playerCount;
        this.mapData = mapData;
        this.seed = seed;

        mapPanel = new Grid(this, mapData);
        cultPanel = new Cults(players);
        powerActionPanel = new PowerActions(usedPowerActions);
        turnOrderPanel = new TurnOrder(this, turnOrder, nextTurnOrder);
        roundPanel = new Rounds(rounds);
        pool = new Pool(this, bons, bonusCoins, favs, towns);
        reset();
        addComponents();
    }

    private void reset() {
        phase = Phase.INITIAL_DWELLINGS;
        round = 0;
        pendingPass = false;
        cultPanel.reset();
        Arrays.fill(usedPowerActions, false);
        history.clear();
        newActions.clear();
        roundPanel.round = 0;

        final Random random = new Random(seed);

        bons.clear();
        final List<Integer> allBons = new ArrayList<>(IntStream.range(1, 11).boxed().toList());
        Collections.shuffle(allBons, random);
        allBons.stream().limit(playerCount + 3).sorted().forEach(bons::add);
        Arrays.fill(bonusCoins, 0);

        favs.clear();
        IntStream.range(1, 5).boxed().forEach(favs::add);
        for (int i = 5; i < 13; ++i) {
            favs.add(i);
            favs.add(i);
            favs.add(i);
        }

        towns.clear();
        for (int i = 1; i < 9; ++i) {
            towns.add(i);
            if (i != 6 && i != 8) {
                towns.add(i);
            }
        }

        rounds.clear();
        final List<Round> allRounds = new ArrayList<>(List.of(Round.fireW, Round.firePw, Round.waterP, Round.waterS, Round.earthC, Round.earthS, Round.airW, Round.airS, Round.priestC));
        int spadeRound;
        do {
            Collections.shuffle(allRounds, random);
            spadeRound = allRounds.indexOf(Round.earthC) + 1;
        } while (spadeRound == 5 || spadeRound == 6);
        allRounds.stream().limit(6).forEach(rounds::add);

        final List<Faction> allFactions = new ArrayList<>(List.of(new Alchemists(), new Auren(), new ChaosMagicians(), new Cultists(), new Darklings(), new Dwarves(), new Engineers(), new Fakirs(), new Giants(), new Halflings(), new Mermaids(), new Nomads(), new Swarmlings(), new Witches()));
        Collections.shuffle(allFactions, random);

        if (!players.isEmpty()) {
            // Reuse existing players but sort them back to the original order.
            turnOrder.clear();
            nextTurnOrder.clear();
            players.stream().sorted(Comparator.comparingInt(p -> -allFactions.indexOf(p.getFaction()))).forEach(nextTurnOrder::add);
            players.clear();
            Player chaosMagiciansPlayer = null;
            Player nomadsPlayer = null;
            for (Player player : nextTurnOrder) {
                player.reset();
                players.add(0, player);
                final Faction faction = player.getFaction();
                if (faction instanceof ChaosMagicians) {
                    chaosMagiciansPlayer = player;
                } else {
                    turnOrder.add(0, player);
                    turnOrder.add(player);
                    if (faction instanceof Nomads) {
                        nomadsPlayer = player;
                    }
                }
            }
            if (nomadsPlayer != null) {
                turnOrder.add(nomadsPlayer);
            }
            if (chaosMagiciansPlayer != null) {
                turnOrder.add(chaosMagiciansPlayer);
            }
        } else {
            Player chaosMagiciansPlayer = null;
            Player nomadsPlayer = null;
            while (players.size() < playerCount) {
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
        }
    }

    private void addComponents() {
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
    }

    public void rewind() {
        final List<Action> history = new ArrayList<>(this.history);
        reset();
        for (Action action : history) {
            resolveAction(action);
            if (phase == Phase.CONFIRM_ACTION) {
                confirmTurn();
            }
        }
        repaint();
    }

    public boolean isValidBonIndex(int index) {
        return index >= 0 && index < bons.size();
    }

    public int getRound() {
        return round;
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
        if (phase == Phase.INITIAL_DWELLINGS) {
            turnOrder.addAll(nextTurnOrder);
            nextTurnOrder.clear();
            for (int i = turnOrder.size() - 1; i >= 0; --i) {
                nextTurnOrder.add(turnOrder.get(i));
            }
            phase = Phase.INITIAL_BONS;
            return;
        }
        ++round;
        roundPanel.round = round;
        turnOrder.addAll(nextTurnOrder);
        nextTurnOrder.clear();
        if (round == 1) {
            phase = Phase.ACTIONS;
        }
        if (round > 6) {
            phase = Phase.END;
        } else {
            for (int i = 0; i < 6; ++i) {
                usedPowerActions[i] = false;
            }
            for (int i = 0; i < bonusCoins.length; ++i) {
                ++bonusCoins[i];
            }
            players.clear();
            players.addAll(turnOrder);
            for (Player player : players) {
                player.startRound(rounds.get(round - 1));
            }
        }
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
        if (phase == Phase.INITIAL_DWELLINGS) {
            resolveAction(new PlaceInitialDwellingAction(row, col));
        } else if (phase == Phase.ACTIONS) {
            final Hex hex = mapPanel.getHex(row, col);
            final List<Hex.Structure> options = Arrays.stream(Hex.Structure.values()).filter(s -> s.getParent() == hex.getStructure()).toList();
            if (options.size() == 1) {
                resolveAction(new BuildAction(row, col, options.get(0)));
            } else if (!options.isEmpty()) {
                // Which upgrade?
            }
        }
    }

    public Hex getHex(int row, int col) {
        return mapPanel.getHex(row, col);
    }

    public void resolveAction(Action action) {
        if (turnOrder.isEmpty()) return;

        action.setData(this, turnOrder.get(0));
        if (action.validatePhase() && action.canExecute()) {
            action.execute();
            newActions.add(action);
            if (!action.isFree()) {
                pendingPass = action.isPass();
                if (action.needsConfirm()) {
                    phase = Phase.CONFIRM_ACTION;
                    repaint();
                } else {
                    endTurn();
                }
            }
        }
    }

    public void confirmTurn() {
        if (phase == Phase.CONFIRM_ACTION) {
            phase = Phase.ACTIONS;
            endTurn();
        }
    }

    public void endTurn() {
        history.addAll(newActions);
        newActions.clear();
        final Player player = turnOrder.remove(0);
        if (pendingPass) {
            if (phase == Phase.ACTIONS) {
                nextTurnOrder.add(player);
            }
            if (turnOrder.isEmpty()) {
                nextRound();
            }
        } else {
            turnOrder.add(player);
        }
        repaint();
    }

    public void leechTriggered(Map<Hex.Type, Integer> leech) {
        final Player activePlayer = turnOrder.get(0);
        final int activePlayerIndex = players.indexOf(activePlayer);
        for (int i = 1; i < players.size(); ++i) {
            final Player otherPlayer = players.get(activePlayerIndex % players.size());
            if (otherPlayer.canLeech()) {
                final int leechAmount = leech.getOrDefault(otherPlayer.getHomeType(), 0);
                if (leechAmount > 0) {
                    // TODO: Actually make a decision
                    otherPlayer.leech(leechAmount);
                }
            }
        }
    }

    public boolean isMyTurn(Player player) {
        return turnOrder.get(0) == player;
    }
}
