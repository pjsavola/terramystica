package tm;

import tm.action.*;
import tm.action.Action;
import tm.faction.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Game extends JPanel {
    public enum Phase {PICK_FACTIONS, AUCTION_FACTIONS, INITIAL_DWELLINGS, INITIAL_BONS, ACTIONS, CONFIRM_ACTION, LEECH, END}

    ;

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
    public boolean resolvingTerrainUnlock;

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
    private Hex.Type iceColor;
    private Hex.Type volcanoColor;
    private Hex.Type variableColor;

    public Phase phase;
    private boolean factionsPicked;

    final String[] mapData;

    boolean rewinding;
    boolean importing;

    JMenu[] menus;
    boolean packNeeded;

    private final JFrame frame;

    public static void open(JFrame frame, Scanner inputScanner) throws ReplayFailure {
        open(frame, new GameData(inputScanner));
    }

    public static void open(JFrame frame, GameData gameData) throws ReplayFailure {
        final Game game = new Game(frame, gameData);
        final int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
        final int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        final JScrollPane jsp = new JScrollPane(game, v, h);
        frame.setContentPane(jsp);
        frame.pack();
        game.refresh();
        game.queryLeechTriggerAction();
    }

    public Game(JFrame frame, GameData gameData) throws ReplayFailure {
        this.frame = frame;
        this.mapData = gameData.mapData;
        this.gameData = gameData;
        final JMenuBar menuBar = new JMenuBar();
        Menus.initializeMenus(this);
        for (JMenu menu : menus) menuBar.add(menu);
        frame.setJMenuBar(menuBar);

        mapPanel = new Grid(this, mapData);
        cultPanel = new Cults(this, players);
        powerActionPanel = new PowerActions(this, usedPowerActions);
        turnOrderPanel = new TurnOrder(this, turnOrder, nextTurnOrder, leechTurnOrder);
        roundPanel = new Rounds(gameData.getRounds());
        pool = new Pool(this, null, bons, bonusCoins, favs, towns, bonUsed, null);
        if (gameData.history != null) {
            history.addAll(gameData.history);
            rewind();
        } else {
            reset();
            final Parser parser = new Parser();
            try {
                parser.replay(gameData.actionFeed, gameData.leechFeed);
            } catch (Exception e) {
                throw new ReplayFailure(e, parser.counter);
            }
        }
        addComponents();
    }

    private void reset() {
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
        packNeeded = false;
        resolvingTerrainUnlock = false;

        favs.clear();
        IntStream.range(1, 5).boxed().forEach(favs::add);
        for (int i = 5; i < 13; ++i) {
            favs.add(i);
            favs.add(i);
            favs.add(i);
        }

        towns.clear();
        towns.addAll(gameData.towns);
        Arrays.fill(bonusCoins, 0);
        bons.clear();
        bons.addAll(gameData.bons);
        final List<Faction> factions = new ArrayList<>(gameData.getFactions());

        if (!players.isEmpty()) {
            // Reuse existing players but sort them back to the original order.
            factionsPicked = !gameData.chooseFactions;
            phase = factionsPicked ? Phase.INITIAL_DWELLINGS : Phase.PICK_FACTIONS;
            for (Player player : players) {
                player.reset();
            }
            if (factionsPicked) {
                turnOrder.clear();
                nextTurnOrder.clear();
                players.sort((p1, p2) -> factions.indexOf(p2.getFaction()) - factions.indexOf(p1.getFaction()));
                setupTurnOrder();
            } else {
                iceColor = null;
                volcanoColor = null;
                variableColor = null;
                turnOrder.clear();
                nextTurnOrder.clear();
                for (int i = 0; i < gameData.playerNames.size(); ++i) {
                    final String name = gameData.playerNames.get(i);
                    for (int j = i; j < players.size(); ++j) {
                        if (players.get(j).toString().equals(name)) {
                            if (i != j) {
                                final Player tmp = players.get(j);
                                players.set(j, players.get(i));
                                players.set(i, tmp);
                            }
                            break;
                        }
                    }
                }
                turnOrder.addAll(players);
            }
        } else if (gameData.chooseFactions) {
            factionsPicked = false;
            phase = Phase.PICK_FACTIONS;
            while (players.size() < gameData.playerCount) {
                final String name = gameData.playerNames.get(players.size());
                final Player player = new Player(this, name);
                players.add(player);
                turnOrder.add(player);
            }
            if (!rewinding) {
                showFactionPopup();
            }
        } else {
            factionsPicked = true;
            phase = Phase.INITIAL_DWELLINGS;
            while (players.size() < gameData.playerCount) {
                final String name = gameData.playerNames.get(players.size());
                final Player player = new Player(this, name);
                final Faction faction = factions.remove(factions.size() - 1);
                player.selectFaction(faction);
                // Ice and Volcano colors are already selected, so we'll need to remove the color selection turn.
                switch (faction.getHomeType()) {
                    case ICE -> {
                        if (turnOrder.remove(0).getHomeType() != Hex.Type.ICE) {
                            throw new RuntimeException("Internal error");
                        }
                    }
                    case VOLCANO -> {
                        if (turnOrder.remove(turnOrder.size() - 1).getHomeType() != Hex.Type.VOLCANO) {
                            throw new RuntimeException("Internal error");
                        }
                    }
                    case VARIABLE -> {
                        if (turnOrder.remove(0).getFaction().getHomeType() != Hex.Type.VARIABLE) {
                            throw new RuntimeException("Internal error");
                        }
                    }
                }
                factions.removeIf(f -> f.getHomeType() == faction.getHomeType());
                players.add(player);
            }
            setupTurnOrder();
        }
    }

    private void setupTurnOrder() {
        Player chaosMagiciansPlayer = null;
        Player nomadsPlayer = null;
        for (Player player : players) {
            nextTurnOrder.add(0, player);
            if (player.getFaction() instanceof ChaosMagicians) {
                chaosMagiciansPlayer = player;
            } else {
                turnOrder.add(player);
                if (player.getFaction() instanceof Nomads) {
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
        if (gameData.extraScoring != null) {
            final JLabel extraScoring = new JLabel(getAbbreviation(gameData.extraScoring));
            actsTurnOrderAndRounds.add(extraScoring);
        }
        add(actsTurnOrderAndRounds);

        for (Player player : players) {
            player.setBackground(new Color(0xDDDDDD));
            player.setBorder(new LineBorder(Color.BLACK));
            add(player);
        }
        add(pool);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public boolean canRewind() {
        return !newActions.isEmpty();
    }

    public void rewind() {
        if (!newActions.isEmpty()) {
            log("Undoing following moves: " + newActions);
        }
        rewinding = true;
        final List<Action> history = new ArrayList<>(this.history);
        reset();
        for (int i = 0; i < history.size(); ++i) {
            final Action action = history.get(i);
            resolveAction(action);
            if (phase == Phase.CONFIRM_ACTION) {
                boolean freeAction = action.isFree();
                for (int j = i + 1; j < history.size(); ++j) {
                    final Action futureAction = history.get(j);
                    if (futureAction.getPlayer() != action.getPlayer()) {
                        break;
                    }
                    boolean futureActionFree = futureAction.isFree();
                    if (futureActionFree && futureAction instanceof UnlockTerrainAction) { // TODO: Implement better fix
                        futureActionFree = futureAction.getPlayer().pendingTerrainUnlock > 0;
                    }
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
        queryLeechTriggerAction();
        if (resolvingCultSpades()) {
            selectPendingCultSteps();
        }
        if (!factionsPicked) {
            if (turnOrder.get(0).getFaction() == null) {
                showFactionPopup();
            } else {
                Hex.Type type;
                do {
                    type = FactionButton.pickReplacedColor(frame, this, false);
                } while (type == null);
                resolveAction(new PickColorAction(type));
            }
        }
        refresh();
    }

    private void queryLeechTriggerAction() {
        if (leechTrigger != null && leechTurnOrder.isEmpty()) {
            if (leechTrigger.getFaction() instanceof Cultists) {
                final int cult = Cults.selectCult(this, 1, true);
                resolveAction(new CultStepAction(cult, 1, CultStepAction.Source.LEECH));
            } else if (leechTrigger.getFaction() instanceof Shapeshifters) {
                final int response = leechTrigger.getPoints() > 0 ? JOptionPane.showConfirmDialog(this, "Gain token to bowl 3 for 1vp?", "Leech accepted", JOptionPane.YES_NO_OPTION) : JOptionPane.NO_OPTION;
                resolveAction(new ShapeshifterPowerAction(response == JOptionPane.YES_OPTION));
            }
        }
    }

    public boolean isValidBonIndex(int index) {
        return index >= 0 && index < bons.size();
    }

    public int getBon(int index) {
        return isValidBonIndex(index) ? bons.get(index) : 0;
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
            log("--- End Scoring ---");
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
                player.startRound(getRound(round));
                if (player.pendingTerrainUnlock > 0) {
                    turnOrder.add(0, player);
                    phase = Phase.CONFIRM_ACTION;
                    resolvingTerrainUnlock = true;
                }
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
                return mapPanel.getAllHexes().stream().filter(hex -> {
                    if (!hex.isEmpty()) return false;

                    if (player.getHomeType() == Hex.Type.ICE) {
                        return getIceColor() == hex.getType();
                    } else if (player.getHomeType() == Hex.Type.VOLCANO) {
                        return getVolcanoColor() == hex.getType();
                    } else {
                        if (player.getFaction() instanceof Riverwalkers) {
                            if (hex.getNeighbors().stream().noneMatch(h -> h.getType() == Hex.Type.WATER)) {
                                return false;
                            }
                        }
                        return hex.getType() == player.getHomeType();
                    }
                }).collect(Collectors.toSet());
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
                        case PLACE_BRIDGE -> {
                        }
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
            resolveAction(new MermaidsTownAction(row, col));
            return;
        }
        if (getCurrentPlayer().pendingTerrainUnlock > 0) {
            return;
        }

        switch (phase) {
            case INITIAL_DWELLINGS -> resolveAction(new PlaceInitialDwellingAction(row, col));
            case ACTIONS -> {
                final Hex.Type homeType = getCurrentPlayer().getHomeType();
                if ((hex.getType() != homeType || button == MouseEvent.BUTTON3) && !(getCurrentPlayer().getFaction() instanceof Riverwalkers)) {
                    handleDigging(hex, row, col);
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
                    resolveAction(new SandstormAction(row, col));
                } else if (freeD || freeTP) {
                    resolveAction(new BuildAction(row, col, freeD ? Hex.Structure.DWELLING : Hex.Structure.TRADING_POST));
                } else if (build || dig) {
                    if (build) {
                        if (getCurrentPlayer().hasPendingBuild(hex)) {
                            resolveAction(new BuildAction(row, col, Hex.Structure.DWELLING));
                        }
                    }
                    if (dig) {
                        handleDigging(hex, row, col);
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
                                    final Point p = mapPanel.getPoint(bridgeEnd.getId());
                                    resolveAction(new PlaceBridgeAction(row, col, p.x, p.y));
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

    private void handleDigging(Hex hex, int row, int col) {
        if (hex.getStructure() != null || getCurrentPlayer().hasPendingBuild(hex)) {
            return;
        }
        if (hex.getType() == Hex.Type.ICE || hex.getType() == Hex.Type.VOLCANO) {
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
        if (player.getFaction().getHomeType() == Hex.Type.VOLCANO) {
            final int cost = getVolcanoDigCost(hex, player);
            if (player.canDig(cost, jump)) {
                if (player.getFaction() instanceof Acolytes) {
                    final List<String> options = new ArrayList<>();
                    for (int i = 0; i < 4; ++i) {
                        if (player.getCultSteps(i) >= cost) {
                            options.add(Cults.getCultName(i));
                        }
                    }
                    final String[] choices = options.toArray(String[]::new);
                    final int response = JOptionPane.showOptionDialog(this, "Spend " + cost + " cult steps from ...", "Choose cult", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, null);
                    if (response >= 0 && response < options.size()) {
                        for (int i = 0; i < 4; ++i) {
                            if (Cults.getCultName(i).equals(choices[response])) {
                                resolveAction(new DigAction(row, col, Hex.Type.VOLCANO, i));
                                break;
                            }
                        }
                    }
                } else {
                    resolveAction(new DigAction(row, col, Hex.Type.VOLCANO, jump));
                }
            }
        } else {
            int options = 0;
            final boolean iceDig = player.getFaction().getHomeType() == Hex.Type.ICE;
            final JDialog popup = new JDialog(frame, true);
            final JPanel terraformPanel = new JPanel();
            final int ordinal = hex.getType().ordinal();
            final Hex.Type[] result = new Hex.Type[1];
            for (int i = ordinal + 4; i < ordinal + 11; ++i) {
                final Hex.Type type = Hex.Type.values()[i % 7];
                if (type == hex.getType() && (type != iceColor || !iceDig)) continue;

                final int requiredSpades = Math.max(1, player.getFaction() instanceof Giants ? 2 : DigAction.getSpadeCost(hex, type));
                final int requiredDigging = Math.max(0, requiredSpades - player.getPendingSpades());
                if (!player.canDig(requiredDigging, jump)) continue;
                if (requiredDigging > 0 && player.getPendingSpades() > 0 && !player.allowExtraSpades) continue;

                final Hex.Type finalType = iceDig && type == iceColor ? Hex.Type.ICE : type;
                if (player.getPendingSpades() > 1 && finalType != player.getHomeType() && requiredSpades < player.getPendingSpades())
                    continue;

                terraformPanel.add(new TerrainButton(popup, hex.getId(), finalType, requiredDigging, result));
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
            resolveAction(new DigAction(row, col, result[0], jump));
        }
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
            chooseCultToMax();
            selectPendingCultSteps();
            return true;
        }
        if (rewinding || importing) {
            // This may be okay if it means we need to confirm previous turn to pass some phase validations later.
            log("!!! " + action + " failed");
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
                phase = factionsPicked ? Phase.ACTIONS : Phase.PICK_FACTIONS;
                endTurn();
                refresh();
                if (resolvingCultSpades()) {
                    selectPendingCultSteps();
                }
                if (!rewinding && !factionsPicked && !turnOrder.isEmpty() && turnOrder.get(0).getFaction() != null && ((turnOrder.get(0).getFaction().getHomeType() == Hex.Type.ICE && iceColor == null) || (turnOrder.get(0).getFaction().getHomeType() == Hex.Type.VARIABLE && variableColor == null))) {
                    Hex.Type type;
                    do {
                        type = FactionButton.pickReplacedColor(frame, this, false);
                    } while (type == null);
                    resolveAction(new PickColorAction(type));
                }
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
                log(getCurrentPlayer().getFaction().getName() + ": " + action);
            }
            action.confirmed();
        }
        newActions.clear();

        if (phase != Phase.LEECH) {
            final Player player = turnOrder.remove(0);
            if (resolvingTerrainUnlock) {
                resolvingTerrainUnlock = false;
            } else if (resolvingCultSpades()) {
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
                            final List<Faction> factions = gameData.getFactions();
                            final int myIdx = factions.indexOf(player.getFaction());
                            final int startPlayerIdx = factions.indexOf(nextTurnOrder.get(0).getFaction());
                            final int delta = (startPlayerIdx - myIdx + gameData.playerCount) % gameData.playerCount;
                            boolean added = false;
                            for (int i = 0; i < nextTurnOrder.size(); ++i) {
                                final int idx = factions.indexOf(nextTurnOrder.get(i).getFaction());
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
                if (phase == Phase.PICK_FACTIONS) {
                    if (turnOrder.isEmpty()) {
                        factionsPicked = true;
                        phase = Phase.INITIAL_DWELLINGS;
                        setupTurnOrder();
                    } else if (!rewinding) {
                        if (turnOrder.get(0).getFaction() == null) {
                            showFactionPopup();
                        } else {
                            repaint();
                            Hex.Type type;
                            do {
                                type = FactionButton.pickReplacedColor(frame, this, false);
                            } while (type == null);
                            resolveAction(new PickColorAction(type));
                        }
                    }
                } else if (turnOrder.isEmpty()) {
                    if (cultIncome == round) {
                        if (cultIncome > 0 && cultIncome < 6) {
                            for (Player p : nextTurnOrder) {
                                p.addCultIncome(getRound(cultIncome));
                                if (p.getPendingSpades() > 0 || p.pendingCultSteps > 0 || p.pendingTerrainUnlock > 0) {
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
                            repaint();
                            if (leechTrigger.getFaction() instanceof Cultists) {
                                final int cult = Cults.selectCult(this, 1, true);
                                resolveAction(new CultStepAction(cult, 1, CultStepAction.Source.LEECH));
                            } else if (leechTrigger.getFaction() instanceof Shapeshifters) {
                                final int response = leechTrigger.getPoints() > 0 ? JOptionPane.showConfirmDialog(this, "Gain token to bowl 3 for 1vp?", "Leech accepted", JOptionPane.YES_NO_OPTION) : JOptionPane.NO_OPTION;
                                resolveAction(new ShapeshifterPowerAction(response == JOptionPane.YES_OPTION));
                            }
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

            // Automatically leech 1
            if (!importing && !rewinding && leechTrigger == null) {
                final Player player = leechTurnOrder.get(0);
                if (Math.min(player.getPendingLeech(), player.getMaxLeech()) == 1) {
                    resolveAction(new LeechAction(true));
                }
            }
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
                    if (activePlayer.getFaction() instanceof Cultists || activePlayer.getFaction() instanceof Shapeshifters) {
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
        for (JMenu menu : menus) {
            final int count = menu.getItemCount();
            for (int i = 0; i < count; ++i) {
                final JMenuItem item = menu.getItem(i);
                final boolean enable;
                if (item instanceof ActionMenuItem) {
                    enable = getCurrentPlayer() != null && ((ActionMenuItem) item).canExecute(this);
                } else {
                    enable = true;
                }
                item.setEnabled(enable);
            }
        }
        final Set<Hex> clickables = getClickableHexes();
        mapPanel.getAllHexes().forEach(h -> h.setHighlight(clickables.contains(h)));
        if (packNeeded) {
            frame.pack();
            packNeeded = false;
        } else {
            repaint();
        }
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
                options.forEach(h -> h.setHighlight(true));
            }
        }
        repaint();
    }

    public void clearHighlights() {
        mapPanel.getAllHexes().forEach(h -> h.setHighlight(false));
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
            log(player + " VP from resources: " + vp);
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
                    log(p + " " + playerReward + " VP from from " + Cults.getCultName(cult));
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
        resolveEndGameScoring(networkSizes, "network");
        if (gameData.extraScoring != null) {
            networkSizes.clear();
            for (Player player : players) {
                int result = 0;
                switch (gameData.extraScoring) {
                    case "connected-sa-sh-distance" -> result = mapPanel.getSASHDistance(player);
                    case "building-on-edge" -> result = mapPanel.getEdgeCount(player);
                    case "connected-distance" -> result = mapPanel.getMaxDistance(player);
                    case "connected-clusters" -> result = mapPanel.getClusterCount(player);
                }
                networkSizes.put(player, result);
            }
            resolveEndGameScoring(networkSizes, gameData.extraScoring);
        }
        refresh();
    }

    private void resolveEndGameScoring(Map<Player, Integer> results, String identifier) {
        final List<Player> sorted = players.stream().sorted((p1, p2) -> results.get(p2) - results.get(p1)).toList();
        final int[] rewards = { 18, 12, 6 };
        int rewardIdx = 0;
        for (int j = 0; j < sorted.size(); ) {
            final Player player = sorted.get(j);
            final int steps = results.get(player);
            if (steps == 0) {
                break;
            }
            int totalReward = rewards[rewardIdx++];
            int k = j + 1;
            while (k < sorted.size()) {
                if (results.get(sorted.get(k)) < steps) {
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
                log(p + " " + playerReward + " VP from from " + identifier);
                if (++j >= sorted.size()) {
                    break;
                }
            }
            if (rewardIdx >= rewards.length) {
                break;
            }
        }
    }

    class Parser {
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
        private static final Pattern actionPattern = Pattern.compile("[Aa][Cc][Tt][Ii][Oo][Nn] ([Aa][Cc][Tt][1-6AaCcEeGgNnSsWw]|[Bb][Oo][Nn][1-2]|[Ff][Aa][Vv]6|[Aa][Cc][Tt][Hh][56])");
        private static final Pattern priestPattern = Pattern.compile("[Ss][Ee][Nn][Dd] [Pp] to " + cultRegex + "( for [1-3])*");
        private static final Pattern favorPattern = Pattern.compile("\\+[Ff][Aa][Vv][1-9][0-9]*");
        private static final Pattern townPattern = Pattern.compile("\\+[1-9]?[Tt][Ww][1-9]");
        private static final Pattern burnPattern = Pattern.compile("[Bb][Uu][Rr][Nn] [1-9][0-9]*");
        private static final Pattern bridgePattern = Pattern.compile("[Bb][Rr][Ii][Dd][Gg][Ee] " + hexRegex + ":" + hexRegex);
        private static final Pattern convertPattern = Pattern.compile("[Cc][Oo][Nn][Vv][Ee][Rr][Tt] ([1-9][0-9]*)? ?" + resourceRegex + " to ([1-9][0-9]*)? ?" + resourceRegex);
        private static final Pattern advancePattern = Pattern.compile("[Aa][Dd][Vv][Aa][Nn][Cc][Ee] ([Dd][Ii][Gg].*|[Ss][Hh][Ii][Pp].*)");
        private static final Pattern forfeitAction = Pattern.compile("-([Ss][Pp][Aa][Dd][Ee]|[Bb][Rr][Ii][Dd][Gg][Ee])");
        private static final Pattern connectPattern = Pattern.compile("[Cc][Oo][Nn][Nn][Ee][Cc][Tt] [Rr][0-9]*");
        private static final Pattern darklingPattern = Pattern.compile("\\+[1-9]? ?[Pp]");
        private static final Pattern pickColorPattern = Pattern.compile("[Pp][Ii][Cc][Kk]-[Cc][Oo][Ll][Oo][Rr] .*");
        private static final Pattern payCultPattern = Pattern.compile("-[34] ?" + cultRegex);
        private static final Pattern unlockTerrainPattern = Pattern.compile("[Uu][Nn][Ll][Oo][Cc][Kk]-[Tt][Ee][Rr][Rr][Aa][Ii][Nn] .*");
        private static final Pattern gainP3pattern = Pattern.compile("[Gg][Aa][Ii][Nn] [Pp]3 [Ff][Oo][Rr] 1?[Vv][Pp]");
        private static final Pattern refuseP3pattern = Pattern.compile("-[Gg][Aa][Ii][Nn]_[Pp]3_[Ff][Oo][Rr]_[Vv][Pp]");

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
                    int mustLeechBeforeIdx = 0;
                    for (GameData.Pair pair : actionFeed) {
                        if (pair.idx == 0) continue;

                        if (pair.faction == getCurrentPlayer().getFaction()) {
                            mustLeechBeforeIdx = pair.idx;
                            break;
                        }
                    }
                    boolean found = false;
                    final Iterator<GameData.Pair> it = leechFeed.iterator();
                    while (it.hasNext()) {
                        final GameData.Pair pair = it.next();
                        if (mustLeechBeforeIdx > 0 && pair.idx >= mustLeechBeforeIdx) {
                            resolveAction(new LeechAction(false));
                            found = true;
                            break;
                        }
                        if (pair.faction == getCurrentPlayer().getFaction()) {
                            if (!leechPattern.matcher(pair.action).matches()) {
                                throw new RuntimeException("Invalid leech");
                            }
                            final String[] s = pair.action.split(" ");
                            final boolean accept = s[0].equalsIgnoreCase("Leech");
                            final Faction faction = gameData.getFactions().stream().filter(f -> f.getClass().getSimpleName().equalsIgnoreCase(s[3])).findAny().orElse(null);
                            if (faction == null) {
                                throw new RuntimeException("Faction not found " + s[3]);
                            }
                            if (faction == from) {
                                log(pair.faction.getName() + ": " + pair.action);
                                resolveAction(new LeechAction(accept));
                                it.remove();
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        throw new RuntimeException("Leech not found");
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
                    throw new RuntimeException("Failed to execute: " + action);
                }
            }
            ++counter;
            log(counter + " -- " + player.getFaction().getName() + ": " + action);
        }

        public void replay(Deque<GameData.Pair> actionFeed, Deque<GameData.Pair> leechFeed) {
            if (actionFeed.isEmpty()) return;

            this.actionFeed = actionFeed;
            this.leechFeed = leechFeed;

            importing = true;
            final List<Action> postponedAcolyteActions = new ArrayList<>();
            Point acolyteDig = null;
            Boolean shapeshifterSHAction = null;
            int setupCompleteCount = 0;
            while (getCurrentPlayer() != null) {
                final Player player = getCurrentPlayer();
                actions = new ArrayDeque<>();
                if (player == leechTrigger) {
                    if (player.getFaction() instanceof Cultists) {
                        boolean found = false;
                        final Iterator<GameData.Pair> it = actionFeed.iterator();
                        while (it.hasNext()) {
                            final GameData.Pair pair = it.next();
                            if (pair.faction == player.getFaction() && cultStepPattern.matcher(pair.action).matches()) {
                                it.remove();
                                actionFeed.addFirst(pair);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            throw new RuntimeException("Cultist cult step for accepted leech not found");
                        }
                    } else if (player.getFaction() instanceof Shapeshifters) {
                        boolean found = false;
                        final Iterator<GameData.Pair> it = actionFeed.iterator();
                        while (it.hasNext()) {
                            final GameData.Pair pair = it.next();
                            if (pair.faction == player.getFaction()) {
                                 if (gainP3pattern.matcher(pair.action).matches() || refuseP3pattern.matcher(pair.action).matches()) {
                                     it.remove();
                                     actionFeed.addFirst(pair);
                                     found = true;
                                     break;
                                 }
                            }
                        }
                        if (!found) {
                            throw new RuntimeException("Shapeshifter decision for accepted leech not found");
                        }
                    } else {
                        throw new RuntimeException("Accepted leech triggering an action");
                    }
                }
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
                //System.err.println(getCurrentPlayer().getFaction().getName() + ": " + actions);
                while (!actions.isEmpty()) {
                    final String action = actions.removeFirst();
                    if (buildPattern.matcher(action).matches()) {
                        final Point p = mapPanel.getPoint(action.split(" ")[1]);
                        if (setupCompleteCount == 0) {
                            replayAction(new PlaceInitialDwellingAction(p.x, p.y));
                        } else {
                            final Hex hex = mapPanel.getHex(p.x, p.y);
                            if (pendingDigging > 0 && !player.hasPendingBuild(hex)) {
                                final Hex.Type effectiveType = player.getFaction().getHomeType() == Hex.Type.ICE ? iceColor : player.getHomeType();
                                final int cost = player.getFaction().getHomeType() == Hex.Type.VOLCANO ? 1 : Math.max(1, player.getFaction() instanceof Giants ? 2 : DigAction.getSpadeCost(hex, effectiveType));
                                if (pendingDigging < cost) {
                                    throw new RuntimeException("Not enough spades");
                                }
                                pendingDigging -= cost;
                                if (player.getFaction() instanceof Acolytes) {
                                    acolyteDig = p;
                                } else {
                                    replayAction(new DigAction(p.x, p.y, player.getHomeType(), mapPanel.getJumpableTiles(player).contains(hex)));
                                }
                            } else if (player.getPendingActions().contains(Player.PendingType.SANDSTORM)) {
                                replayAction(new SandstormAction(p.x, p.y));
                            }
                            final Action buildAction = new BuildAction(p.x, p.y, Hex.Structure.DWELLING);
                            if (acolyteDig != null) {
                                postponedAcolyteActions.add(buildAction);
                            } else {
                                replayAction(buildAction);
                            }
                        }
                    } else if (transformPattern.matcher(action).matches()) {
                        final String[] s = action.split(" ");
                        final Point p = mapPanel.getPoint(s[1]);
                        final Hex hex = mapPanel.getHex(p.x, p.y);
                        if (player.getPendingActions().contains(Player.PendingType.SANDSTORM)) {
                            replayAction(new SandstormAction(p.x, p.y));
                        } else {
                            final Hex.Type type = getTransformTerrain(s, player, hex, pendingDigging);
                            final Hex.Type effectiveType = type == Hex.Type.ICE ? iceColor : type;
                            final int cost = player.getFaction().getHomeType() == Hex.Type.VOLCANO ? 1 : Math.max(1, player.getFaction() instanceof Giants ? 2 : DigAction.getSpadeCost(hex, effectiveType));
                            if (!resolvingCultSpades()) {
                                if (pendingDigging < cost) {
                                    throw new RuntimeException("Not enough spades");
                                }
                                pendingDigging -= cost;
                            }
                            if (player.getFaction() instanceof Acolytes) {
                                acolyteDig = p;
                            } else {
                                replayAction(new DigAction(p.x, p.y, type, mapPanel.getJumpableTiles(player).contains(hex)));
                            }
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

                                    // Partial digging of multiple hexes is not allowed by the rules. JMystica enforces
                                    // that digging towards home terrain is done first but Snellman allows arbitrary order.
                                    // Here we switch the action order if partial digging is done first.
                                    if (str.charAt(3) == '6' && actions.size() > 1) {
                                        final Iterator<String> actionIterator = actions.iterator();
                                        final String action1 = actionIterator.next();
                                        if (transformPattern.matcher(action1).matches()) {
                                            final String[] transform1 = action1.split(" ");
                                            final Point p1 = mapPanel.getPoint(transform1[1]);
                                            final Hex hex1 = mapPanel.getHex(p1.x, p1.y);
                                            final Hex.Type type1 = getTransformTerrain(transform1, player, hex1, 2);
                                            final Hex.Type effectiveType1 = type1 == Hex.Type.ICE ? iceColor : type1;
                                            final int cost = Math.max(1, player.getFaction() instanceof Giants ? 2 : DigAction.getSpadeCost(hex1, effectiveType1));
                                            if (cost == 1 && type1 != player.getHomeType()) {
                                                final String action2 = actionIterator.next();
                                                if (transformPattern.matcher(action2).matches()) {
                                                    final String[] transform2 = action2.split(" ");
                                                    final Point p2 = mapPanel.getPoint(transform2[1]);
                                                    final Hex hex2 = mapPanel.getHex(p2.x, p2.y);
                                                    final Hex.Type type2 = getTransformTerrain(transform2, player, hex2, 2);
                                                    if (type2 == player.getHomeType()) {
                                                        final String a1 = actions.pollFirst();
                                                        final String a2 = actions.pollFirst();
                                                        actions.addFirst(a1);
                                                        actions.addFirst(a2);
                                                        System.err.println("Swapping actions 2");
                                                    } else {
                                                        throw new RuntimeException("Partial digging of multiple hexes");
                                                    }
                                                } else if (buildPattern.matcher(action2).matches()) {
                                                    final String a1 = actions.pollFirst();
                                                    final String a2 = actions.pollFirst();
                                                    actions.addFirst(a1);
                                                    actions.addFirst(a2);
                                                    //System.err.println(gameData.playerNames);
                                                    //System.err.println("Swapping " + player + ": " + a1 + " <-> " + a2);
                                                }
                                            }
                                        }
                                    }
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
                                case 'h':
                                    shapeshifterSHAction = switch (str.charAt(4)) {
                                        case '5' -> true;
                                        case '6' -> false;
                                        default -> throw new RuntimeException("Invalid Shapeshifter SH action");
                                    };
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
                            } else if (player.getFaction() instanceof Acolytes) {
                                // TODO: More validation
                                pendingCultSource = CultStepAction.Source.ACOLYTES;
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
                        final int fav = Integer.parseInt(action.substring(4));
                        if (player.getFaction() instanceof IceMaidens && phase == Phase.INITIAL_DWELLINGS) {
                            selectFav(player, fav);
                        } else {
                            replayAction(new SelectFavAction(fav));
                        }
                    } else if (townPattern.matcher(action).matches()) {
                        int amount = 1;
                        int idx = 3;
                        if (Character.isDigit(action.charAt(1))) {
                            amount = action.charAt(1) - '0';
                            ++idx;
                        }
                        while (amount-- > 0) {
                            final Action townAction = new SelectTownAction(Integer.parseInt(action.substring(idx)));
                            if (acolyteDig != null) {
                                postponedAcolyteActions.add(townAction);
                            } else {
                                replayAction(townAction);
                            }
                        }
                    } else if (burnPattern.matcher(action).matches()) {
                        replayAction(new BurnAction(Integer.parseInt(action.split(" ")[1])));
                    } else if (bridgePattern.matcher(action).matches()) {
                        final String[] s = action.split(" ");
                        final Point p1 = mapPanel.getPoint(s[1].split(":")[0]);
                        final Point p2 = mapPanel.getPoint(s[1].split(":")[1]);
                        replayAction(new PlaceBridgeAction(p1.x, p1.y, p2.x, p2.y));
                    } else if (convertPattern.matcher(action).matches()) {
                        String s = action.substring("convert".length()).trim();
                        int idx = 0;
                        int fromCount = 0;
                        if (Character.isDigit(s.charAt(idx))) {
                            while (Character.isDigit(s.charAt(idx))) {
                                fromCount = fromCount * 10 + s.charAt(idx) - '0';
                                ++idx;
                            }
                            while (Character.isWhitespace(s.charAt(idx))) {
                                ++idx;
                            }
                        } else {
                            fromCount = 1;
                        }
                        s = s.substring(idx);
                        idx = s.indexOf(' ');
                        String from = s.substring(0, idx);
                        s = s.substring(idx + 4); // skip " to "

                        idx = 0;
                        int toCount = 0;
                        if (Character.isDigit(s.charAt(idx))) {
                            while (Character.isDigit(s.charAt(idx))) {
                                toCount = toCount * 10 + s.charAt(idx) - '0';
                                ++idx;
                            }
                            while (Character.isWhitespace(s.charAt(idx))) {
                                ++idx;
                            }
                        } else {
                            toCount = 1;
                        }

                        String to = s.substring(idx);
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
                        // pendingDigging = 0; This was added in "Fix turn order issues" commit for unknown reason, likely not needed.
                    } else if (advancePattern.matcher(action).matches()) {
                        final String[] s = action.split(" ");
                        replayAction(new AdvanceAction(s[1].toLowerCase().startsWith("dig")));
                    } else if (forfeitAction.matcher(action).matches()) {
                        replayAction(new ForfeitAction());
                    } else if (connectPattern.matcher(action).matches()) {
                        final String id = action.split(" ")[1];
                        final Point p = mapPanel.getPoint(id);
                        replayAction(new MermaidsTownAction(p.x, p.y));
                    } else if (darklingPattern.matcher(action).matches()) {
                        int count = 1;
                        for (int i = 0; i < action.length(); ++i) {
                            if (Character.isDigit(action.charAt(i))) {
                                count = action.charAt(i) - '0';
                                break;
                            }
                        }
                        replayAction(new DarklingsConvertAction(count));
                    } else if (pickColorPattern.matcher(action).matches()) {
                        final Hex.Type color = Hex.Type.valueOf(action.split(" ")[1].toUpperCase());
                        if (shapeshifterSHAction != null) {
                            replayAction(new ShapeshifterColorAction(shapeshifterSHAction, color));
                            shapeshifterSHAction = null;
                        } else {
                            if (player.getFaction().getHomeType() == Hex.Type.ICE) {
                                iceColor = color;
                            } else if (player.getFaction().getHomeType() == Hex.Type.VOLCANO) {
                                volcanoColor = color;
                            } else if (player.getFaction().getHomeType() == Hex.Type.VARIABLE) {
                                variableColor = color;
                                player.initialUnlockedTerrainIndex = color.ordinal();
                                if (player.unlockedTerrain != null) {
                                    player.unlockedTerrain[player.initialUnlockedTerrainIndex] = true;
                                }
                            }
                        }
                    } else if (payCultPattern.matcher(action).matches()) {
                        if (acolyteDig != null) {
                            final String cult = action.substring(2).trim();
                            final int cultIdx = findCult(cult);
                            replayAction(new DigAction(acolyteDig.x, acolyteDig.y, Hex.Type.VOLCANO, cultIdx));
                            acolyteDig = null;
                        } else {
                            throw new RuntimeException("Illegal move");
                        }
                        for (Action postponedAction : postponedAcolyteActions) {
                            replayAction(postponedAction);
                        }
                        postponedAcolyteActions.clear();
                    } else if (unlockTerrainPattern.matcher(action).matches()) {
                        final String s = action.split(" ")[1].toUpperCase();
                        final Hex.Type color = s.equals("GAIN-PRIEST") ? null : Hex.Type.valueOf(s);
                        replayAction(new UnlockTerrainAction(color));
                    } else if (gainP3pattern.matcher(action).matches()) {
                        replayAction(new ShapeshifterPowerAction(true));
                    } else if (refuseP3pattern.matcher(action).matches()) {
                        replayAction(new ShapeshifterPowerAction(false));
                    } else {
                        log("Unhandled action: " + action);
                        break;
                    }
                    if (player.getPendingActions().isEmpty() && pendingCultSource == null && pendingDigging == 0) {
                        if (!actions.isEmpty()) {
                            if (!convertPattern.matcher(actions.getFirst()).matches() && !burnPattern.matcher(actions.getFirst()).matches() && !connectPattern.matcher(actions.getFirst()).matches()) {
                                postponeActions();
                            }
                        }
                    }
                }
                if (phase == Phase.CONFIRM_ACTION && player == getCurrentPlayer()) {
                    final Faction faction = player.getFaction();
                    confirmTurn();
                    replayLeech(faction);
                }
                if (counter >= JMystica.maxReplayActionCount) {
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
    }

    private Hex.Type getTransformTerrain(String[] splitAction, Player player, Hex hex, int pendingDigging) {
        final Hex.Type type;
        if (splitAction.length <= 3) {
            final Hex.Type home = player.getHomeType();
            final Hex.Type hexType = hex.getType();
            if (hexType == home) {
                throw new RuntimeException("Invalid implicit transform");
            }

            if (player.getFaction() instanceof Giants) {
                type = home;
            } else {
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
            }
        } else {
            String typeName = splitAction[3].toUpperCase();
            final String finalTypeName = typeName.equals("GREY") ? "GRAY" : typeName;
            type = Arrays.stream(Hex.Type.values()).filter(h -> h.name().equals(finalTypeName)).findAny().orElse(null);
        }
        return type;
    }

    public boolean validateVictoryPoints() {
        if (gameData.results != null) {
            final int[] vps = new int[gameData.playerNames.size()];
            for (int i = 0; i < gameData.playerNames.size(); ++i) {
                vps[i] = gameData.results.get(gameData.playerNames.get(i));
            }
            return Arrays.equals(vps, getVictoryPoints());
        }
        return true;
    }

    public int[] getVictoryPoints() {
        final List<Faction> factions = gameData.getFactions();
        return players.stream().sorted((p1, p2) -> factions.indexOf(p2.getFaction()) - factions.indexOf(p1.getFaction())).mapToInt(Player::getPoints).toArray();
    }

    public Set<Integer> getSelectedOrdinals() {
        final Set<Integer> selectedOrdinals = getSelectedBaseOrdinals();
        if (iceColor != null) {
            selectedOrdinals.add(iceColor.ordinal());
        }
        if (volcanoColor != null) {
            selectedOrdinals.add(volcanoColor.ordinal());
        }
        return selectedOrdinals;
    }

    public Set<Integer> getSelectedBaseOrdinals() {
        final Set<Integer> selectedOrdinals = new HashSet<>();
        for (Player p : players) {
            if (p.getFaction() != null) {
                selectedOrdinals.add(p.getFaction().getHomeType().ordinal());
            }
        }
        if (variableColor != null) {
            selectedOrdinals.add(variableColor.ordinal());
        }
        return selectedOrdinals;
    }

    public Stream<Faction> getSelectableFactions() {
        final Set<Hex.Type> selectedColors = new HashSet<>();
        for (Player p : players) {
            if (p.getFaction() != null) {
                selectedColors.add(p.getFaction().getHomeType());
            }
        }
        if (selectedColors.contains(Hex.Type.ICE)) {
            selectedColors.add(iceColor);
        }
        if (selectedColors.contains(Hex.Type.VOLCANO)) {
            selectedColors.add(volcanoColor);
        }
        if (selectedColors.contains(Hex.Type.VARIABLE)) {
            selectedColors.add(variableColor);
        }
        return GameData.allFactions.stream().filter(f -> !selectedColors.contains(f.getHomeType()));
    }

    public int getStartingVictoryPoints(Faction faction) {
        return gameData.useRevisedStartingVPs ? GameData.revisedStartingVPs.get(faction.getClass().getSimpleName()) : 20;
    }

    private void showFactionPopup() {
        final JDialog factionPopup = new JDialog(frame);
        final JPanel factionPanel = new JPanel();
        final List<Faction> selectableFactions = getSelectableFactions().sorted(Comparator.comparingInt(f -> f.getHomeType().ordinal())).toList();
        selectableFactions.forEach(f -> {
            final int count = factionPanel.getComponentCount();
            if (count % 2 == 0) {
                factionPanel.add(new FactionButton(factionPopup, this, f), count / 2);
            } else {
                factionPanel.add(new FactionButton(factionPopup, this, f));
            }
        });
        factionPanel.setLayout(new GridLayout(2, selectableFactions.size() / 2));
        factionPopup.setTitle("Select Faction, " + getCurrentPlayer());
        factionPopup.setContentPane(factionPanel);
        factionPopup.setLocationRelativeTo(frame);
        factionPopup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        factionPopup.pack();
        factionPopup.setVisible(true);
    }

    public void save() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        fileChooser.setAcceptAllFileFilterUsed(false);
        final FileNameExtensionFilter filter = new FileNameExtensionFilter("JMystica Game", JMystica.gameFileExtension);
        fileChooser.setFileFilter(filter);
        fileChooser.setFileSystemView(new JtmFileSystemView());
        final int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (fileChooser.getFileFilter() == filter && !filter.accept(fileToSave)) {
                fileToSave = new File(fileToSave.getAbsolutePath() + "." + JMystica.gameFileExtension);
            }
            if (fileToSave.exists()) {
                final int option = JOptionPane.showConfirmDialog(null, fileToSave.getName() + " already exists. Do you want to save anyway?", "File exists", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                if (option != JOptionPane.OK_OPTION) {
                    return;
                }
            }
            try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(fileToSave))) {
                stream.writeObject(gameData);
                stream.writeInt(history.size());
                for (Action action : history) {
                    stream.writeObject(action);
                }
                stream.flush();
                log("Saved game to " + fileToSave.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void log(String s) {
        if (gameData.silentMode) {
            gameData.logs.add(s);
        } else {
            System.err.println(s);
        }
    }

    private void buildCombos(Set<Set<Integer>> allCombos, Set<Integer> combo, Set<Integer> possibleCults, int count) {
        if (count > 0) {
            for (int i = 0; i < 4; ++i) {
                if (!combo.contains(i) && possibleCults.contains(i)) {
                    final Set<Integer> newCombo = new HashSet<>(combo);
                    newCombo.add(i);
                    buildCombos(allCombos, newCombo, possibleCults, count - 1);
                }
            }
        } else {
            allCombos.add(combo);
        }
    }

    public void selectPendingCultSteps() {
        if (!rewinding && !importing && phase == Phase.CONFIRM_ACTION) {
            if (!turnOrder.isEmpty() && turnOrder.get(0).pendingCultSteps > 0) {
                final int cult = Cults.selectCult(this, 1, true);
                resolveAction(new CultStepAction(cult, 1, CultStepAction.Source.ACOLYTES));
            }
        }
    }

    public void chooseCultToMax() {
        if (!rewinding && !importing && phase == Phase.CONFIRM_ACTION) {
            final Player player = getCurrentPlayer();
            final Set<Player.PendingType> types = player.getPendingActions();
            if (types.contains(Player.PendingType.CHOOSE_CULTS)) {
                final List<JRadioButton> buttons = new ArrayList<>();
                final ButtonGroup group = new ButtonGroup();
                final Set<Integer> possibleCults = new HashSet<>();
                int maxedCults = 0;
                for (int i = 0; i < 4; ++i) {
                    if (player.maxedCults[i]) {
                        possibleCults.add(i);
                        if (player.getCultSteps(i) >= 10) {
                            ++maxedCults;
                        }
                    }
                }
                final Set<Set<Integer>> allCombos = new HashSet<>();
                buildCombos(allCombos, new HashSet<>(), possibleCults, maxedCults);
                for (Set<Integer> combo : allCombos) {
                    final JRadioButton button = new JRadioButton(combo.stream().map(Cults::getCultName).collect(Collectors.joining(", ")));
                    button.setSelected(combo.stream().map(player::getCultSteps).allMatch(i -> i >= 10));
                    buttons.add(button);
                    group.add(button);
                }
                // TODO: Scroll up so that cults are visible
                final Object[] message = new Object[1 + buttons.size()];
                message[0] = "Choose which cults to max";
                for (int i = 0; i < buttons.size(); ++i) message[i + 1] = buttons.get(i);
                final int option = JOptionPane.showConfirmDialog(this, message, "Choose Cults", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                if (option == JOptionPane.OK_OPTION) {
                    final boolean[] choices = new boolean[4];
                    int choice = 0;
                    for (Set<Integer> combo : allCombos) {
                        if (buttons.get(choice++).isSelected()) {
                            for (int cult : combo) {
                                choices[cult] = true;
                            }
                        }
                    }
                    resolveAction(new ChooseMaxedCultsAction(choices));
                }
            }
        }
    }

    private static String getAbbreviation(String extraScoring) {
        return switch (extraScoring) {
            case "connected-sa-sh-distance" -> "SA-SH";
            case "building-on-edge" -> "edge";
            case "connected-distance" -> "distance";
            case "connected-clusters" -> "clusters";
            default -> null;
        };
    }

    public int getVolcanoDigCost(Hex hex, Player player) {
        final int cost;
        if (player.getFaction() instanceof Dragonlords) {
            cost = isHomeType(hex.getType()) ? 2 : 1;
        } else if (player.getFaction() instanceof Acolytes) {
            cost = isHomeType(hex.getType()) ? 4 : 3;
        } else {
            throw new RuntimeException("Unimplemented Volcano faction");
        }
        return cost;
    }

    public boolean isHomeType(Hex.Type type) {
        for (Player p : players) {
            if (type == p.getFaction().getHomeType()) {
                return true;
            }
        }
        return false;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void setIceColor(Hex.Type type) {
        iceColor = type;
    }

    public void setVolcanoColor(Hex.Type type) {
        volcanoColor = type;
    }

    public void setVariableColor(Hex.Type type) {
        mapPanel.getAllHexes().forEach(hex -> {
            if (hex.getStructure() != null && hex.getType() == variableColor) {
                hex.setType(type);
            }
        });
        variableColor = type;
    }

    public Hex.Type getIceColor() {
        return iceColor;
    }

    public Hex.Type getVolcanoColor() {
        return volcanoColor;
    }

    public Hex.Type getVariableColor() {
        return variableColor;
    }

    public void factionPicked(Player player, Faction faction) {
        switch (faction.getHomeType()) {
            case ICE -> turnOrder.add(0, player);
            case VOLCANO -> turnOrder.add(player);
            case VARIABLE -> turnOrder.add(0, player);
        }
    }

    public Player getPlayer(Hex.Type homeType) {
        for (Player player : players) {
            if (player.getHomeType() == homeType) {
                return player;
            }
        }
        return null;
    }

    public Round getRound(int round) {
        return gameData.getRounds().get(round - 1);
    }

    public List<Integer> getSpadeDistanceCounts(Player player) {
        return mapPanel.getSpadeDistanceCounts(player, this);
    }
}
