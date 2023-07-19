package tm;

import tm.action.*;
import tm.action.Action;
import tm.faction.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Game extends JPanel {
    public enum Phase { PICK_FACTIONS, AUCTION_FACTIONS, INITIAL_DWELLINGS, INITIAL_BONS, ACTIONS, CONFIRM_ACTION, LEECH, END };

    private final GameData gameData;
    private final List<Player> players = new ArrayList<>();
    private final List<Integer> bons = new ArrayList<>();
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
    private int cultIncome;

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

    private final String[] mapData;

    boolean rewinding;
    boolean importing;

    private final Menu actionMenu;

    private final JFrame frame;

    public Game(JFrame frame, String[] mapData, GameData gameData, Menu actionMenu) {
        this.frame = frame;
        this.mapData = mapData;
        this.gameData = gameData;
        this.actionMenu = actionMenu;

        mapPanel = new Grid(this, mapData);
        cultPanel = new Cults(this, players);
        powerActionPanel = new PowerActions(this, usedPowerActions);
        turnOrderPanel = new TurnOrder(this, turnOrder, nextTurnOrder, leechTurnOrder);
        roundPanel = new Rounds(gameData.rounds);
        pool = new Pool(this, null, bons, bonusCoins, favs, towns, bonUsed, null);
        reset();
        replay(gameData.actionFeed, gameData.leechFeed);
        addComponents();
    }

    private void reset() {
        phase = Phase.INITIAL_DWELLINGS;
        round = 0;
        cultIncome = 0;
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
        Arrays.fill(bonusCoins, 0);
        bons.clear();
        bons.addAll(gameData.bons);
        final List<Faction> factions = new ArrayList<>(gameData.factions);

        if (!players.isEmpty()) {
            // Reuse existing players but sort them back to the original order.
            turnOrder.clear();
            nextTurnOrder.clear();
            for (int i = factions.size() - 1; i >= 0; --i) {
                final Faction faction = factions.get(i);
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
            while (players.size() < gameData.playerCount) {
                final Player player = new Player(this);
                final Faction faction = factions.remove(factions.size() - 1);
                player.selectFaction(faction, 20);
                factions.removeIf(f -> f.getHomeType() == faction.getHomeType());
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
            boolean freeAction = action.isFree();
            resolveAction(action);
            if (phase == Phase.CONFIRM_ACTION) {
                for (int j = i + 1; j < history.size(); ++j) {
                    final Action futureAction = history.get(j);
                    if (futureAction.getPlayer() != action.getPlayer()) {
                        break;
                    }
                    final boolean futureActionFree = futureAction.isFree();
                    if (freeAction || futureActionFree) {
                        if (!futureActionFree) {
                            freeAction = false;
                        }
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
            if (oldBon - 1 < bonUsed.length) {
                bonUsed[oldBon - 1] = false;
            }
        } else {
            bons.remove(bonIndex);
        }
    }

    public void finalPass(Player player) {
        bons.add(player.removeBon());
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
            System.err.println("--- End Scoring ---");
            phase = Phase.END;
            endScoring();
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
                player.startRound(gameData.rounds.get(round - 1));
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

    public Set<Hex> getClickableHexes() {
        final Player player = getCurrentPlayer();
        if (player == null) return Collections.emptySet();

        switch (phase) {
            case INITIAL_DWELLINGS -> {
                return mapPanel.getAllHexes().stream().filter(hex -> hex.isEmpty() && hex.getType() == player.getHomeType()).collect(Collectors.toSet());
            }
            case CONFIRM_ACTION -> {
                final Set<Hex> result = new HashSet<>();
                player.getPendingActions().forEach(type -> {
                    switch (type) {
                        case FREE_D -> mapPanel.getAllHexes().stream().filter(hex -> hex.isEmpty() && hex.getType() == player.getHomeType()).forEach(result::add);
                        case FREE_TP -> mapPanel.getAllHexes().stream().filter(hex -> hex.getStructure() == Hex.Structure.DWELLING && hex.getType() == player.getHomeType()).forEach(result::add);
                        case BUILD -> {
                            if (player.pendingBuilds != null) result.addAll(player.pendingBuilds);
                        }
                        case USE_SPADES -> {
                            mapPanel.getReachableTiles(player).stream().filter(h -> h.getStructure() == null).forEach(result::add);
                            if (player.pendingBuilds == null) {
                                if (player.canUseRange() && !resolvingCultSpades()) {
                                    result.addAll(mapPanel.getJumpableTiles(player));
                                }
                            } else {
                                player.pendingBuilds.forEach(result::remove);
                            }
                        }
                        case SANDSTORM -> result.addAll(getSandstormTiles(player));
                        case PLACE_BRIDGE -> {}
                    }
                });
                return result;
            }
            default -> {
                return Collections.emptySet();
            }
        }
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
                final boolean freeD = pendingActions.contains(Player.PendingType.FREE_D);
                final boolean freeTP = pendingActions.contains(Player.PendingType.FREE_TP);
                final boolean build = pendingActions.contains(Player.PendingType.BUILD);
                final boolean dig = pendingActions.contains(Player.PendingType.USE_SPADES);
                final boolean sandstorm = pendingActions.contains(Player.PendingType.SANDSTORM);
                if (sandstorm) {
                    resolveAction(new SandstormAction(hex));
                } else if (freeD || freeTP) {
                    resolveAction(new BuildAction(row, col, freeD ? Hex.Structure.DWELLING : Hex.Structure.TRADING_POST));
                } else if (build || dig) {
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
            if (requiredDigging > 0 && player.getPendingSpades() > 0 && !player.allowExtraSpades) continue;
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

    public boolean resolveAction(Action action) {
        final Player player = getCurrentPlayer();
        if (player == null) return false;

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
            return true;
        }
        if (rewinding || importing) {
            System.err.println("!!! " + action + " failed");
        }
        return false;
    }

    public void confirmTurn() {
        if (phase == Phase.CONFIRM_ACTION) {
            final Set<Player.PendingType> skippablePendingActions = getCurrentPlayer().getSkippablePendingActions();
            if (getCurrentPlayer().getPendingActions().isEmpty() || !skippablePendingActions.isEmpty()) {
                if (!skippablePendingActions.isEmpty()) {
                    if (rewinding) {
                        // Forfeit actions are explicit in the history when rewinding.
                        return;
                    }
                    resolveAction(new ForfeitAction());
                }
                getCurrentPlayer().pendingBuilds = null;
                phase = Phase.ACTIONS;
                endTurn();
                refresh();
            }
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
            if (!rewinding && !importing) {
                System.err.println(getCurrentPlayer().getFaction().getName() + ": " + action);
            }
            action.confirmed();
        }
        newActions.clear();

        if (phase != Phase.LEECH) {
            final Player player = turnOrder.remove(0);
            if (resolvingCultSpades()) {
                if (turnOrder.isEmpty()) {
                    nextRound();
                }
            } else if (leechTrigger != null && leechTurnOrder.isEmpty()) {
                leechTrigger = null;
            } else if (pendingPass) {
                if (phase == Phase.ACTIONS) {
                    if (gameData.turnOrderVariant) {
                        nextTurnOrder.add(player);
                    } else {
                        if (nextTurnOrder.isEmpty()) {
                            nextTurnOrder.add(player);
                        } else {
                            final int myIdx = gameData.factions.indexOf(player.getFaction());
                            final int startPlayerIdx = gameData.factions.indexOf(nextTurnOrder.get(0).getFaction());
                            final int delta = (startPlayerIdx - myIdx + gameData.playerCount) % gameData.playerCount;
                            boolean added = false;
                            for (int i = 0; i < nextTurnOrder.size(); ++i) {
                                final int idx = gameData.factions.indexOf(nextTurnOrder.get(i).getFaction());
                                final int d = (startPlayerIdx - idx + gameData.playerCount) % gameData.playerCount;
                                if (delta < d) {
                                    nextTurnOrder.add(i, player);
                                    added = true;
                                    break;
                                }
                            }
                            if (!added) {
                                nextTurnOrder.add(player);
                            }
                        }
                    }
                }
                if (turnOrder.isEmpty()) {
                    if (cultIncome == round) {
                        if (cultIncome > 0 && cultIncome < 6) {
                            for (Player p : nextTurnOrder) {
                                p.addCultIncome(gameData.rounds.get(cultIncome - 1));
                                if (p.getPendingSpades() > 0) {
                                    turnOrder.add(p);
                                }
                            }
                        }
                        ++cultIncome;
                    }
                    if (turnOrder.isEmpty()) {
                        nextRound();
                    } else {
                        phase = Phase.CONFIRM_ACTION;
                    }
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
                        if (!rewinding && !importing) {
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
        final Set<Hex> clickables = getClickableHexes();
        mapPanel.getAllHexes().forEach(h -> h.highlight = clickables.contains(h));
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

    public Set<Hex> getBridgeNeighbors(Hex hex) {
        return mapPanel.getBridgeNeighbors(hex);
    }

    public Set<Hex> getSandstormTiles(Player player) {
        final Set<Hex> result = new HashSet<>();
        mapPanel.getAllHexes().stream().filter(h -> h.getStructure() != null && h.getType() == player.getHomeType()).forEach(h -> {
            h.getNeighbors().stream().filter(n -> n.getType() != Hex.Type.WATER && n.getType() != player.getHomeType() && n.getStructure() == null).forEach(result::add);
        });
        return result;
    }

    public boolean resolvingCultSpades() {
        return round > 0 && cultIncome > round;
    }

    private void endScoring() {
        for (Player player : players) {
            final int vp = player.autoConvert();
            System.err.println(player + " VP from resources: " + vp);
        }

        for (int i = 0; i < 4; ++i) {
            final int cult = i;
            final List<Player> sorted = players.stream().sorted((p1, p2) -> p2.getCultSteps(cult) - p1.getCultSteps(cult)).toList();
            final int[] rewards = { 8, 4, 2 };
            int rewardIdx = 0;
            for (int j = 0; j < sorted.size(); ) {
                final Player player = sorted.get(j);
                final int steps = player.getCultSteps(cult);
                if (steps == 0) {
                    break;
                }
                int totalReward = rewards[rewardIdx++];
                int k = j + 1;
                while (k < sorted.size()) {
                    if (sorted.get(k).getCultSteps(cult) < steps) {
                        break;
                    }
                    totalReward += rewardIdx >= rewards.length ? 0 : rewards[rewardIdx++];
                    ++k;
                }
                int tiedPlayers = k - j;
                final int playerReward = totalReward / tiedPlayers;
                while (j < k) {
                    final Player p = sorted.get(j);
                    p.score(playerReward);
                    System.err.println(p + " " + playerReward + " VP from from " + Cults.getCultName(cult));
                    if (++j >= sorted.size()) {
                        break;
                    }
                }
                if (rewardIdx >= rewards.length) {
                    break;
                }
            }
        }

        final Map<Player, Integer> networkSizes = new HashMap<>();
        for (Player player : players) {
            networkSizes.put(player, mapPanel.getNetworkSize(player));
        }
        final List<Player> sorted = players.stream().sorted((p1, p2) -> networkSizes.get(p2) - networkSizes.get(p1)).toList();
        final int[] rewards = { 18, 12, 6 };
        int rewardIdx = 0;
        for (int j = 0; j < sorted.size(); ) {
            final Player player = sorted.get(j);
            final int steps = networkSizes.get(player);
            int totalReward = rewards[rewardIdx++];
            int k = j + 1;
            while (k < sorted.size()) {
                if (networkSizes.get(sorted.get(k)) < steps) {
                    break;
                }
                totalReward += rewardIdx >= rewards.length ? 0 : rewards[rewardIdx++];
                ++k;
            }
            final int tiedPlayers = k - j;
            final int playerReward = totalReward / tiedPlayers;
            while (j < k) {
                final Player p = sorted.get(j);
                p.score(playerReward);
                System.err.println(p + " " + playerReward + " VP from from network");
                if (++j >= sorted.size()) {
                    break;
                }
            }
            if (rewardIdx >= rewards.length) {
                break;
            }
        }
        refresh();
    }


    private static final String cultRegex = "([Ff][Ii][Rr][Ee]|[Ww][Aa][Tt][Ee][Rr]|[Ee][Aa][Rr][Tt][Hh]|[Aa][Ii][Rr])";
    private static final String hexRegex = "[A-Za-z][1-9][0-9]*";
    private static final String resourceRegex = "([Pp][Ww]|[Ww]|[Cc]|[Pp]|[Vv][Pp])";
    private static final Pattern buildPattern = Pattern.compile("[Bb][Uu][Ii][Ll][Dd] " + hexRegex);
    private static final Pattern transformPattern = Pattern.compile("[Tt][Rr][Aa][Nn][Ss][Ff][Oo][Rr][Mm] " + hexRegex + "( [Tt][Oo] .*)?");
    public static final Pattern leechPattern = Pattern.compile("([Ll][Ee][Ee][Cc][Hh]|[Dd][Ee][Cc][Ll][Ii][Nn][Ee]) [1-9][0-9]* from [A-Za-z]*");
    private static final Pattern cultStepPattern = Pattern.compile("\\+[1-9]*" + cultRegex);
    private static final Pattern passPattern = Pattern.compile("[Pp][Aa][Ss][Ss]( [Bb][Oo][Nn][1-9][0-9]*)*");
    private static final Pattern digPattern = Pattern.compile("[Dd][Ii][Gg] \\d");
    private static final Pattern upgradePattern = Pattern.compile("[Uu][Pp][Gg][Rr][Aa][Dd][Ee] " + hexRegex + " to ([Tt][Pp]|[Tt][Ee]|[Ss][Hh]|[Ss][Aa])");
    private static final Pattern actionPattern = Pattern.compile("[Aa][Cc][Tt][Ii][Oo][Nn] ([Aa][Cc][Tt][1-6AaCcEeGgNnSsWw]|[Bb][Oo][Nn][1-2]|[Ff][Aa][Vv]6)");
    private static final Pattern priestPattern = Pattern.compile("[Ss][Ee][Nn][Dd] [Pp] to " + cultRegex + "( for [1-3])*");
    private static final Pattern favorPattern = Pattern.compile("\\+[Ff][Aa][Vv][1-9][0-9]*");
    private static final Pattern townPattern = Pattern.compile("\\+[Tt][Ww][1-9]");
    private static final Pattern burnPattern = Pattern.compile("[Bb][Uu][Rr][Nn] [1-9][0-9]*");
    private static final Pattern bridgePattern = Pattern.compile("[Bb][Rr][Ii][Dd][Gg][Ee] " + hexRegex + ":" + hexRegex);
    private static final Pattern convertPattern = Pattern.compile("[Cc][Oo][Nn][Vv][Ee][Rr][Tt] [1-9][0-9]*" + resourceRegex + " to [1-9][0-9]*" + resourceRegex);
    private static final Pattern advancePattern = Pattern.compile("[Aa][Dd][Vv][Aa][Nn][Cc][Ee] ([Dd][Ii][Gg]|[Ss][Hh][Ii][Pp])");
    private static final Pattern forfeitAction = Pattern.compile("\\-([Ss][Pp][Aa][Dd][Ee]|[Bb][Rr][Ii][Dd][Gg][Ee])");
    private static final Pattern connectPattern = Pattern.compile("[Cc][Oo][Nn][Nn][Ee][Cc][Tt] [Rr][0-9]*");

    private int findCult(String cultName) {
        for (int i = 0; i < 4; ++i) {
            if (Cults.getCultName(i).equalsIgnoreCase(cultName)) {
                return i;
            }
        }
        return -1;
    }

    public void replayLeech(Faction from) {
        while (phase == Phase.LEECH) {
            if (getCurrentPlayer() != null) {
                final Iterator<GameData.Pair> it = leechFeed.iterator();
                while (it.hasNext()) {
                    final GameData.Pair pair = it.next();
                    if (pair.faction == getCurrentPlayer().getFaction()) {
                        if (!leechPattern.matcher(pair.action).matches()) {
                            throw new RuntimeException("Invalid leech");
                        }
                        final String[] s = pair.action.split(" ");
                        final boolean accept = s[0].equalsIgnoreCase("Leech");
                        final Faction faction = gameData.factions.stream().filter(f -> f.getClass().getSimpleName().equalsIgnoreCase(s[3])).findAny().orElse(null);
                        if (faction == null) {
                            throw new RuntimeException("Faction not found " + s[3]);
                        }
                        if (faction == from) {
                            System.err.println(pair.faction.getName() + ": " + pair.action);
                            resolveAction(new LeechAction(accept));
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }
    }

    private Deque<GameData.Pair> leechFeed;
    private Deque<GameData.Pair> actionFeed;
    private Deque<String> actions;
    int pendingDigging;
    CultStepAction.Source pendingCultSource = null;
    int counter = 0;

    private void postponeActions() {
        while (!actions.isEmpty()) {
            final GameData.Pair pair = new GameData.Pair();
            pair.faction = getCurrentPlayer().getFaction();
            pair.action = actions.removeLast();
            actionFeed.addFirst(pair);
        }
    }

    private void replayAction(Action action) {
        final Player player = getCurrentPlayer();
        if (!resolveAction(action)) {
            postponeActions();
            if (phase == Phase.CONFIRM_ACTION) {
                final Faction faction = getCurrentPlayer().getFaction();
                confirmTurn();
                replayLeech(faction);
            }
            if (!actions.isEmpty()) {
                throw new RuntimeException("Action stack not cleared");
            }
            pendingDigging = 0;
            pendingCultSource = null;
            if (player != getCurrentPlayer()) {
                throw new RuntimeException("Player changed " + action);
            }
            if (!resolveAction(action)) {
                System.err.println("Failure " + action);
                counter = Main.maxReplayActionCount;
                return;
            }
        }
        ++counter;
        System.err.println(counter + " -- " + player.getFaction().getName() + ": " + action);
    }

    public void replay(Deque<GameData.Pair> actionFeed, Deque<GameData.Pair> leechFeed) {
        this.actionFeed = actionFeed;
        this.leechFeed = leechFeed;

        importing = true;
        int setupCompleteCount = 0;
        while (getCurrentPlayer() != null) {
            final Player player = getCurrentPlayer();
            actions = new ArrayDeque<>();
            final Iterator<GameData.Pair> it = actionFeed.iterator();
            while (it.hasNext()) {
                final GameData.Pair pair = it.next();
                if (pair.action.equalsIgnoreCase("done")) {
                    it.remove();
                    break;
                }
                if (pair.faction == player.getFaction()) {
                    actions.addLast(pair.action);
                    it.remove();
                } else if (!actions.isEmpty()) {
                    break;
                }
            }
            while (!actions.isEmpty()) {
                final String action = actions.removeFirst();
                //System.err.println(getCurrentPlayer().getFaction().getName() + ": " + action);
                if (buildPattern.matcher(action).matches()) {
                    final Point p = mapPanel.getPoint(action.split(" ")[1]);
                    if (setupCompleteCount == 0) {
                        replayAction(new PlaceInitialDwellingAction(p.x, p.y));
                    } else {
                        final Hex hex = mapPanel.getHex(p.x, p.y);
                        if (pendingDigging > 0 && !player.hasPendingBuild(hex)) {
                            replayAction(new DigAction(hex, player.getHomeType(), mapPanel.getJumpableTiles(player).contains(hex)));
                        } else if (player.getPendingActions().contains(Player.PendingType.SANDSTORM)) {
                            replayAction(new SandstormAction(hex));
                        }
                        replayAction(new BuildAction(p.x, p.y, Hex.Structure.DWELLING));
                    }
                } else if (transformPattern.matcher(action).matches()) {
                    final String[] s = action.split(" ");
                    final Point p = mapPanel.getPoint(s[1]);
                    final Hex hex = mapPanel.getHex(p.x, p.y);
                    if (player.getPendingActions().contains(Player.PendingType.SANDSTORM)) {
                        replayAction(new SandstormAction(hex));
                    } else {
                        final Hex.Type type;
                        if (s.length <= 3) {
                            final Hex.Type home = player.getHomeType();
                            final Hex.Type hexType = hex.getType();

                            int delta = 0;
                            for (int i = 1; i <= 3; ++i) {
                                if (Hex.Type.values()[(hexType.ordinal() + i) % 7] == home) {
                                    delta = Math.min(i, resolvingCultSpades() ? player.getPendingSpades() : pendingDigging);
                                    break;
                                }
                            }
                            for (int i = 1; i <= 3; ++i) {
                                if (Hex.Type.values()[(hexType.ordinal() - i + 7) % 7] == home) {
                                    delta = -Math.min(i, resolvingCultSpades() ? player.getPendingSpades() : pendingDigging);
                                    break;
                                }
                            }
                            final int ordinal = (hexType.ordinal() + delta + 7) % 7;
                            type = Hex.Type.values()[ordinal];
                        } else {
                            type = Arrays.stream(Hex.Type.values()).filter(h -> h.name().equalsIgnoreCase(s[3])).findAny().orElse(null);
                        }
                        final int cost = player.getFaction() instanceof Giants ? 2 : DigAction.getSpadeCost(hex, type);
                        if (!resolvingCultSpades()) {
                            if (pendingDigging < cost) {
                                throw new RuntimeException("Not enough spades");
                            }
                            pendingDigging -= cost;
                        }
                        replayAction(new DigAction(hex, type, mapPanel.getJumpableTiles(player).contains(hex)));
                    }
                } else if (passPattern.matcher(action).matches()) {
                    final String[] s = action.split(" ");
                    if (s.length == 1) {
                        replayAction(new PassAction());
                    } else {
                        final int bon = Integer.parseInt(action.split(" ")[1].substring(3));
                        boolean found = false;
                        for (int idx = 0; idx < bons.size(); ++idx) {
                            if (bons.get(idx) == bon) {
                                replayAction(new SelectBonAction(idx));
                                ++setupCompleteCount;
                                found = true;
                                break;
                            }
                        }
                        if (!found) throw new RuntimeException("Bon not available " + bon);
                    }
                } else if (digPattern.matcher(action).matches()) {
                    pendingDigging += Integer.parseInt(action.split(" ")[1]);
                } else if (upgradePattern.matcher(action).matches()) {
                    final Point p = mapPanel.getPoint(action.split(" ")[1]);
                    final String type = action.split(" ")[3];
                    Hex.Structure structure = null;
                    if (type.equalsIgnoreCase("TP")) structure = Hex.Structure.TRADING_POST;
                    if (type.equalsIgnoreCase("TE")) structure = Hex.Structure.TEMPLE;
                    if (type.equalsIgnoreCase("SH")) structure = Hex.Structure.STRONGHOLD;
                    if (type.equalsIgnoreCase("SA")) structure = Hex.Structure.SANCTUARY;
                    replayAction(new BuildAction(p.x, p.y, structure));
                    if (structure == Hex.Structure.STRONGHOLD && player.getFaction() instanceof Halflings) {
                        pendingDigging = 3;
                    }
                } else if (actionPattern.matcher(action).matches()) {
                    final String[] s = action.split(" ");
                    final String str = s[1].toLowerCase();
                    if (str.startsWith("act")) {
                        switch (str.charAt(3)) {
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                                if (str.charAt(3) == '5' || str.charAt(3) == '6') {
                                    pendingDigging = str.charAt(3) - '4';
                                }
                                replayAction(new SelectPowerActionAction(str.charAt(3) - '0'));
                                break;
                            case 'a':
                                pendingCultSource = CultStepAction.Source.ACTA;
                                break;
                            case 'c':
                                replayAction(new ChaosMagiciansDoubleAction());
                                break;
                            case 'e':
                                replayAction(new EngineersBridgeAction());
                                break;
                            case 'g':
                                pendingDigging = 2;
                                replayAction(new SpadeAction(SpadeAction.Source.ACTG));
                                break;
                            case 'n':
                                replayAction(new NomadsSandstormAction());
                                break;
                            case 's':
                                replayAction(new SwarmlingsFreeTradingPostAction());
                                break;
                            case 'w':
                                replayAction(new WitchesFreeDwellingAction());
                                break;
                        }
                    } else if (str.startsWith("bon")) {
                        switch (str.charAt(3)) {
                            case '1' -> {
                                pendingDigging = 1;
                                replayAction(new SpadeAction(SpadeAction.Source.BON1));
                            }
                            case '2' -> pendingCultSource = CultStepAction.Source.BON2;
                        }
                    } else {
                        pendingCultSource = CultStepAction.Source.FAV6;
                    }
                } else if (cultStepPattern.matcher(action).matches()) {
                    if (pendingCultSource == null) {
                        if (player.getFaction() instanceof Cultists) {
                            postponeActions();
                            if (phase != Phase.ACTIONS) {
                                final GameData.Pair pair = new GameData.Pair();
                                pair.faction = player.getFaction();
                                pair.action = action;
                                actionFeed.addFirst(pair);
                                break;
                            } else {
                                pendingCultSource = CultStepAction.Source.LEECH;
                            }
                        } else {
                            throw new RuntimeException("Unknown cult step source");
                        }
                    }
                    int cultIdx = 1;
                    int amount = 1;
                    if (Character.isDigit(action.charAt(1))) {
                        amount = 2;
                        cultIdx = 2;
                    }
                    final int cult = findCult(action.substring(cultIdx));
                    replayAction(new CultStepAction(cult, amount, pendingCultSource));
                    pendingCultSource = null;
                } else if (priestPattern.matcher(action).matches()) {
                    final String[] s = action.split(" ");
                    final int cult = findCult(s[3]);
                    int steps = 1;
                    if (s.length == 4) {
                        if (cultPanel.isCultSpotFree(cult, 3)) steps = 3;
                        else if (cultPanel.isCultSpotFree(cult, 2)) steps = 2;
                    } else {
                        steps = Integer.parseInt(s[5]);
                    }
                    replayAction(new PriestToCultAction(cult, steps));
                } else if (favorPattern.matcher(action).matches()) {
                    replayAction(new SelectFavAction(Integer.parseInt(action.substring(4))));
                } else if (townPattern.matcher(action).matches()) {
                    replayAction(new SelectTownAction(Integer.parseInt(action.substring(3))));
                } else if (burnPattern.matcher(action).matches()) {
                    replayAction(new BurnAction(Integer.parseInt(action.split(" ")[1])));
                } else if (bridgePattern.matcher(action).matches()) {
                    final String[] s = action.split(" ");
                    final Point p1 = mapPanel.getPoint(s[1].split(":")[0]);
                    final Point p2 = mapPanel.getPoint(s[1].split(":")[1]);
                    replayAction(new PlaceBridgeAction(mapPanel.getHex(p1.x, p1.y), mapPanel.getHex(p2.x, p2.y)));
                } else if (convertPattern.matcher(action).matches()) {
                    final String[] s = action.split(" ");
                    String from = s[1];
                    String to = s[3];
                    int fromCount = 0;
                    int toCount = 0;
                    for (int i = 0; i < from.length(); ++i) {
                        if (from.charAt(i) < '0' || from.charAt(i) > '9') {
                            fromCount = Integer.parseInt(from.substring(0, i));
                            from = from.substring(i);
                            break;
                        }
                    }
                    for (int i = 0; i < to.length(); ++i) {
                        if (to.charAt(i) < '0' || to.charAt(i) > '9') {
                            toCount = Integer.parseInt(to.substring(0, i));
                            to = to.substring(i);
                            break;
                        }
                    }
                    int workersToPriests = 0;
                    int priestsToWorkers = 0;
                    int workersToCoins = 0;
                    int pointsToCoins = 0;
                    int pointsFromCoins = 0;
                    Resources power = Resources.zero;
                    if (from.equalsIgnoreCase("pw")) {
                        if (to.equalsIgnoreCase("p")) {
                            power = Resources.fromPriests(toCount);
                        } else if (to.equalsIgnoreCase("w")) {
                            power = Resources.fromWorkers(toCount);
                        } else if (to.equalsIgnoreCase("c")) {
                            power = Resources.fromCoins(toCount);
                        }
                    } else if (from.equalsIgnoreCase("p")) {
                        priestsToWorkers = fromCount;
                        if (to.equalsIgnoreCase("c")) {
                            workersToCoins = fromCount;
                        }
                    }
                    else if (from.equalsIgnoreCase("w")) {
                        if (to.equalsIgnoreCase("p")) {
                            workersToPriests = fromCount;
                        } else {
                            workersToCoins = fromCount;
                        }
                    }
                    else if (from.equalsIgnoreCase("vp")) {
                        pointsToCoins = fromCount;
                    }
                    if (to.equalsIgnoreCase("vp")) {
                        pointsFromCoins = toCount;
                    }
                    if (workersToPriests > 0) {
                        replayAction(new DarklingsConvertAction(workersToPriests));
                    } else {
                        replayAction(new ConvertAction(power, priestsToWorkers, workersToCoins, pointsToCoins, pointsFromCoins));
                    }
                    pendingDigging = 0;
                } else if (advancePattern.matcher(action).matches()) {
                    final String[] s = action.split(" ");
                    replayAction(new AdvanceAction(s[1].equalsIgnoreCase("dig")));
                } else if (forfeitAction.matcher(action).matches()) {
                    replayAction(new ForfeitAction());
                } else if (connectPattern.matcher(action).matches()) {
                    final String id = action.split(" ")[1];
                    final Hex hex = mapPanel.getAllHexes().stream().filter(h -> h.getId().equals(id)).findAny().orElse(null);
                    replayAction(new MermaidsTownAction(hex));
                } else {
                    System.err.println("Unhandled action: " + action);
                    break;
                }
                if (player.getPendingActions().isEmpty() && pendingCultSource == null && pendingDigging == 0) {
                    if (!actions.isEmpty()) {
                        if (!convertPattern.matcher(actions.getFirst()).matches() && !burnPattern.matcher(actions.getFirst()).matches()) {
                            postponeActions();
                        }
                    }
                }
            }
            if (phase == Phase.CONFIRM_ACTION) {
                final Faction faction = player.getFaction();
                confirmTurn();
                replayLeech(faction);
            }
            if (counter >= Main.maxReplayActionCount) {
                break;
            }
            if (!actions.isEmpty()) {
                throw new RuntimeException("Action stack not cleared");
            }
            pendingDigging = 0;
            pendingCultSource = null;
            if (phase == Phase.END) {
                break;
            }
        }
        importing = false;
    }

    public int[] getVictoryPoints() {
        return players.stream().sorted((p1, p2) -> gameData.factions.indexOf(p2.getFaction()) - gameData.factions.indexOf(p1.getFaction())).mapToInt(Player::getPoints).toArray();
    }
}
