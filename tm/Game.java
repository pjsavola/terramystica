package tm;

import tm.action.*;
import tm.action.Action;
import tm.faction.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class Game extends JPanel {
    public enum Phase { PICK_FACTIONS, AUCTION_FACTIONS, INITIAL_DWELLINGS, INITIAL_BONS, ACTIONS, CONFIRM_ACTION, LEECH, END };

    private final List<Player> players = new ArrayList<>();
    private final List<Integer> bons = new ArrayList<>();
    private final List<Round> rounds = new ArrayList<>();
    private final int[] bonusCoins = new int[3];
    public final boolean[] bonUsed = new boolean[2];
    private final List<Integer> favs = new ArrayList<>();
    private final List<Integer> towns = new ArrayList<>();
    public final boolean[] usedPowerActions = new boolean[6];
    private final Grid mapPanel;
    public final Cults cultPanel;
    private final PowerActions powerActionPanel;
    private final TurnOrder turnOrderPanel;
    private final Rounds roundPanel;
    private final Pool pool;
    private int round;

    private final List<Player> turnOrder = new ArrayList<>();
    private final List<Player> nextTurnOrder = new ArrayList<>();
    private final List<Player> leechTurnOrder = new ArrayList<>();
    private final List<Action> history = new ArrayList<>();
    private final List<Action> newActions = new ArrayList<>();
    private boolean pendingPass;
    private boolean leechAccepted;
    public Player leechTrigger;
    private Hex bridgeEnd;
    private boolean pendingTownPlacement;
    private boolean doubleTurn;

    public Phase phase;

    private static final List<Faction> allFactions = List.of(new Alchemists(), new Auren(), new ChaosMagicians(), new Cultists(), new Darklings(), new Dwarves(), new Engineers(), new Fakirs(), new Giants(), new Halflings(), new Mermaids(), new Nomads(), new Swarmlings(), new Witches());
    private static final List<Faction> testFactions = List.of(new ChaosMagicians());

    private final String[] mapData;
    private final int playerCount;
    private final int seed;

    private boolean rewinding;

    private final Menu actionMenu;

    private final JFrame frame;

    public Game(JFrame frame, int playerCount, String[] mapData, int seed, Menu actionMenu) {
        this.frame = frame;
        this.playerCount = playerCount;
        this.mapData = mapData;
        this.seed = seed;
        this.actionMenu = actionMenu;

        mapPanel = new Grid(this, mapData);
        cultPanel = new Cults(this, players);
        powerActionPanel = new PowerActions(this, usedPowerActions);
        turnOrderPanel = new TurnOrder(this, turnOrder, nextTurnOrder, leechTurnOrder);
        roundPanel = new Rounds(rounds);
        pool = new Pool(this, null, bons, bonusCoins, favs, towns, bonUsed, null);
        reset();
        addComponents();
    }

    private void reset() {
        phase = Phase.INITIAL_DWELLINGS;
        round = 0;
        pendingPass = false;
        leechAccepted = false;
        leechTrigger = null;
        cultPanel.reset();
        Arrays.fill(usedPowerActions, false);
        history.clear();
        newActions.clear();
        leechTurnOrder.clear();
        roundPanel.round = 0;
        Arrays.fill(bonUsed, false);
        mapPanel.reset(mapData);
        bridgeEnd = null;
        pendingTownPlacement = false;

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

        final List<Faction> allFactions = new ArrayList<>(Game.testFactions);
        Collections.shuffle(allFactions, random);

        if (!players.isEmpty()) {
            // Reuse existing players but sort them back to the original order.
            turnOrder.clear();
            nextTurnOrder.clear();
            for (int i = allFactions.size() - 1; i >= 0; --i) {
                final Faction faction = allFactions.get(i);
                for (int j = players.size() - 1; j >= 0; --j) {
                    final Player player = players.get(j);
                    if (player.getFaction() == faction) {
                        nextTurnOrder.add(0, player);
                        players.remove(j);
                        break;
                    }
                }
                if (players.isEmpty()) {
                    break;
                }
            }
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
        if (newActions.isEmpty()) return;

        System.err.println("Undoing following moves: " + newActions);
        rewinding = true;
        final List<Action> history = new ArrayList<>(this.history);
        reset();
        for (int i = 0; i < history.size(); ++i) {
            final Action action = history.get(i);
            resolveAction(action);
            if (phase == Phase.CONFIRM_ACTION) {
                for (int j = i + 1; j < history.size(); ++j) {
                    final Action futureAction = history.get(j);
                    if (futureAction.isFree() && futureAction.getPlayer() == action.getPlayer()) {
                        resolveAction(futureAction);
                        ++i;
                    } else {
                        break;
                    }
                }
                confirmTurn();
            }
        }
        rewinding = false;
        if (leechTrigger != null && leechTurnOrder.isEmpty()) {
            final int cult = Cults.selectCult(this, 1, true);
            resolveAction(new CultStepAction(cult, 1, CultStepAction.Source.LEECH));
        }
        refresh();
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
            if (oldBon < bonUsed.length) {
                bonUsed[oldBon] = false;
            }
        } else {
            bons.remove(bonIndex);
        }
    }

    public boolean canSelectTown(int town) {
        return towns.contains(town);
    }

    public void selectTown(Player player, int town) {
        if (towns.remove((Integer) town)) {
            player.foundTown(town);
        }
    }

    public boolean canSelectFav(int fav) {
        return favs.contains(fav);
    }

    public void selectFav(Player player, int fav) {
        if (favs.remove((Integer) fav)) {
            player.addFavor(fav);
            if (fav == 5) {
                checkTowns(player);
            }
        }
    }

    public List<Integer> getSelectableFavs(Player player) {
        return favs.stream().distinct().filter(player::canAddFavor).toList();
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

    public void hexClicked(int row, int col, int button) {
        final Hex hex = getHex(row, col);
        if (hex == null) return;

        if (pendingTownPlacement) {
            resolveAction(new MermaidsTownAction(hex));
            return;
        }

        switch (phase) {
            case INITIAL_DWELLINGS -> resolveAction(new PlaceInitialDwellingAction(row, col));
            case ACTIONS -> {
                final Hex.Type homeType = getCurrentPlayer().getHomeType();
                if (hex.getType() != homeType || button == MouseEvent.BUTTON3) {
                    handleDigging(hex);
                    return;
                }
                final List<Hex.Structure> options = Arrays.stream(Hex.Structure.values()).filter(s -> s.getParent() == hex.getStructure()).toList();
                Hex.Structure choice = null;
                if (options.size() == 1) {
                    choice = options.get(0);
                } else if (!options.isEmpty()) {
                    final String[] choices = options.stream().map(Hex.Structure::getName).toArray(String[]::new);
                    final int response = JOptionPane.showOptionDialog(this, "Upgrade Trading Post to...", "Choose upgrade", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, null);
                    if (response >= 0 && response < options.size()) {
                        choice = options.get(response);
                    }
                }
                if (choice != null) {
                    resolveAction(new BuildAction(row, col, choice));
                }
            }
            case CONFIRM_ACTION -> {
                final Set<Player.PendingType> pendingActions = getCurrentPlayer().getPendingActions();
                final boolean build = pendingActions.contains(Player.PendingType.BUILD);
                final boolean dig = pendingActions.contains(Player.PendingType.USE_SPADES);
                if (build || dig) {
                    if (build) {
                        if (getCurrentPlayer().hasPendingBuild(hex)) {
                            resolveAction(new BuildAction(row, col, Hex.Structure.DWELLING));
                        }
                    }
                    if (dig) {
                        handleDigging(hex);
                    }
                } else if (pendingActions.contains(Player.PendingType.PLACE_BRIDGE)) {
                    final Hex.Type homeType = getCurrentPlayer().getHomeType();
                    if (bridgeEnd == null) {
                        bridgeEnd = hex;
                    } else if (bridgeEnd != hex) {
                        if (hex.getType() == homeType || bridgeEnd.getType() == homeType) {
                            boolean structureInBridgeEnd = false;
                            if (hex.getType() == homeType && hex.getStructure() != null) {
                                structureInBridgeEnd = true;
                            }
                            if (bridgeEnd.getType() == homeType && bridgeEnd.getStructure() != null) {
                                structureInBridgeEnd = true;
                            }
                            if (structureInBridgeEnd) {
                                int requiredCommonWaterNeighbors = 2;
                                if (hex.getNeighbors().size() <= 3 && bridgeEnd.getNeighbors().size() <= 3) {
                                    --requiredCommonWaterNeighbors;
                                }
                                for (Hex neighbor : bridgeEnd.getNeighbors()) {
                                    if (hex.getNeighbors().contains(neighbor) && neighbor.getType() == Hex.Type.WATER) {
                                        --requiredCommonWaterNeighbors;
                                    }
                                }
                                if (requiredCommonWaterNeighbors <= 0) {
                                    resolveAction(new PlaceBridgeAction(hex, bridgeEnd));
                                    return;
                                }
                            }
                        }
                        bridgeEnd = hex;
                    }
                }
            }
        }
    }

    private void handleDigging(Hex hex) {
        if (hex.getStructure() != null || getCurrentPlayer().hasPendingBuild(hex)) {
            return;
        }
        final Player player = getCurrentPlayer();
        boolean jump = false;
        if (!isReachable(hex, player)) {
            if (!isJumpable(hex, player)) {
                return;
            }
            jump = true;
        }
        int options = 0;
        final JDialog popup = new JDialog(frame, true);
        final JPanel terraformPanel = new JPanel();
        final int ordinal = hex.getType().ordinal();
        final Hex.Type[] result = new Hex.Type[1];
        for (int i = ordinal + 4; i < ordinal + 11; ++i) {
            final Hex.Type type = Hex.Type.values()[i % 7];
            if (type == hex.getType()) continue;

            final int requiredSpades = player.getFaction() instanceof Giants ? 2 : DigAction.getSpadeCost(hex, type);
            final int requiredDigging = Math.max(0, requiredSpades - player.getPendingSpades());
            if (!player.canDig(requiredDigging, jump)) continue;
            if (player.getPendingSpades() > 1 && type != player.getHomeType() && requiredSpades < player.getPendingSpades()) continue;

            terraformPanel.add(new TerrainButton(popup, hex.getId(), type, requiredDigging, result));
            ++options;
        }
        if (options == 0) {
            return;
        }
        popup.setTitle("Select target terrain");
        popup.setContentPane(terraformPanel);
        popup.setLocationRelativeTo(frame);
        popup.pack();
        popup.setVisible(true);
        if (result[0] == null) {
            return;
        }
        resolveAction(new DigAction(hex, result[0], jump));
    }

    public Hex getHex(int row, int col) {
        return mapPanel.getHex(row, col);
    }

    public void resolveAction(Action action) {
        final Player player = getCurrentPlayer();
        if (player == null) return;

        action.setData(this, player);
        if (action.validatePhase() && action.canExecute()) {
            action.execute();
            newActions.add(action);
            if (!action.isFree()) {
                pendingPass = action.isPass();
                if (action.needsConfirm()) {
                    phase = Phase.CONFIRM_ACTION;
                } else {
                    endTurn();
                }
            }
            refresh();
        }
    }

    public void confirmTurn() {
        if (phase == Phase.CONFIRM_ACTION) {
            final Set<Player.PendingType> pendingActions = getCurrentPlayer().getPendingActions();
            if (pendingActions.size() == 1 && pendingActions.contains(Player.PendingType.BUILD)) {
                // Fall through
            } else if (!pendingActions.isEmpty()) {
                return;
            }
            getCurrentPlayer().pendingBuilds = null;
            phase = Phase.ACTIONS;
            endTurn();
            refresh();
        }
    }

    public void confirmLeech(boolean accept) {
        if (phase == Phase.LEECH) {
            leechAccepted |= accept;
        }
    }

    public void endTurn() {
        history.addAll(newActions);
        for (Action action : newActions) {
            if (!rewinding) {
                System.err.println(getCurrentPlayer().getFaction().getName() + ": " + action);
            }
            action.confirmed();
        }
        newActions.clear();

        if (phase != Phase.LEECH) {
            final Player player = turnOrder.remove(0);
            if (leechTrigger != null && leechTurnOrder.isEmpty()) {
                leechTrigger = null;
            } else if (pendingPass) {
                if (phase == Phase.ACTIONS) {
                    nextTurnOrder.add(player);
                }
                if (turnOrder.isEmpty()) {
                    nextRound();
                }
            } else {
                if (doubleTurn) {
                    turnOrder.add(0, player);
                    doubleTurn = false;
                } else {
                    turnOrder.add(player);
                }
            }
        } else {
            leechTurnOrder.remove(0);
            if (leechTurnOrder.isEmpty()) {
                phase = Phase.ACTIONS;
                if (leechTrigger != null) {
                    if (leechAccepted) {
                        turnOrder.add(0, leechTrigger);
                        if (!rewinding) {
                            final int cult = Cults.selectCult(this, 1, true);
                            resolveAction(new CultStepAction(cult, 1, CultStepAction.Source.LEECH));
                        }
                    } else {
                        leechTrigger.addIncome(Resources.pw1);
                        leechTrigger = null;
                    }
                }
                leechAccepted = false;
            }
        }

        if (!leechTurnOrder.isEmpty()) {
            phase = Phase.LEECH;
        }
    }

    public void leechTriggered(Map<Hex.Type, Integer> leech) {
        final Player activePlayer = turnOrder.get(0);
        final int activePlayerIndex = players.indexOf(activePlayer);
        for (int i = 1; i < players.size(); ++i) {
            final Player otherPlayer = players.get((activePlayerIndex + i) % players.size());
            if (otherPlayer.canLeech()) {
                final int leechAmount = leech.getOrDefault(otherPlayer.getHomeType(), 0);
                if (leechAmount > 0) {
                    otherPlayer.addPendingLeech(leechAmount);
                    leechTurnOrder.add(otherPlayer);
                    if (activePlayer.getFaction() instanceof Cultists) {
                        leechTrigger = activePlayer;
                    }
                }
            }
        }
    }

    public boolean isMyTurn(Player player) {
        return getCurrentPlayer() == player;
    }

    public Player getCurrentPlayer() {
        if (phase == Phase.LEECH) {
            return leechTurnOrder.get(0);
        }
        return turnOrder.get(0);
    }

    public void refresh() {
        final int count = actionMenu.getItemCount();
        for (int i = 0; i < count; ++i) {
            final MenuItem item = actionMenu.getItem(i);
            final boolean enable;
            if (item instanceof ActionMenuItem) {
                enable = getCurrentPlayer() != null && ((ActionMenuItem) item).canExecute(this);
            } else {
                enable = true;
            }
            item.setEnabled(enable);
        }
        repaint();
    }

    public void bridgePlaced(Bridge bridge) {
        mapPanel.addBridge(bridge);
        bridgeEnd = null;
    }

    public boolean isReachable(Hex hex, Player player) {
        return mapPanel.getReachableTiles(player).contains(hex);
    }

    public boolean isJumpable(Hex hex, Player player) {
        return player.canUseRange() && mapPanel.getJumpableTiles(player).contains(hex);
    }

    public void checkTowns(Player player) {
        final int newTowns = mapPanel.updateTowns(player);
        player.addPendingTowns(Math.min(towns.size(), newTowns));
    }

    public boolean canPlaceMermaidTown(Hex hex, Player player) {
        return !towns.isEmpty() && mapPanel.canPlaceMermaidTown(hex, player);
    }

    public void placeMermaidTown(Hex hex, Player player) {
        pendingTownPlacement = false;
        mapPanel.updateMermaidTown(hex, player);
        player.addPendingTowns(Math.min(towns.size(), 1));
    }

    public void highlightMermaidTownSpots() {
        if (pendingTownPlacement) {
            pendingTownPlacement = false;
            clearHighlights();
        } else {
            final List<Hex> options = mapPanel.getAllHexes().stream().filter(h -> canPlaceMermaidTown(h, getCurrentPlayer())).toList();
            if (!options.isEmpty()) {
                pendingTownPlacement = true;
                options.forEach(h -> h.highlight = true);
            }
        }
        repaint();
    }

    public void clearHighlights() {
        mapPanel.getAllHexes().forEach(h -> h.highlight = false);
    }

    public void activateDoubleTurn() {
        doubleTurn = true;
    }
}
