package tm;

import tm.action.*;
import tm.faction.Acolytes;
import tm.faction.Faction;
import tm.faction.Giants;
import tm.faction.Riverwalkers;

import java.util.ArrayList;
import java.util.List;

public class AIUtil {

    public static int getPowerActionThreat(Player p, List<Player> turnOrder, int act) {
        int canTake = 0;
        for (Player player : turnOrder) {
            if (player == p) continue;

            final int requiredPower = PowerActions.getRequiredPower(player, act);
            if (player.canAffordPower(requiredPower)) {
                ++canTake;
            }
        }
        return canTake;
    }

    public static int getCultThreat(Player p, List<Player> turnOrder, Game game, int cult) {
        final int contestedSpot = game.cultPanel.isCultSpotFree(cult, 3) ? 3 : (game.cultPanel.isCultSpotFree(cult, 2) ? 2 : 1);
        if (contestedSpot == 1) return 0;

        final int spotCount = game.cultPanel.getFreeCultSpotCount(cult, contestedSpot);
        int canSend = 0;
        for (Player player : turnOrder) {
            if (player == p) continue;

            if (player.canSendPriestToCult()) {
                ++canSend;
            }
        }
        if (spotCount >= canSend) {
            return 0;
        }
        return canSend;
    }

    public static int getDigThreat(Player p, List<Player> turnOrder, Game game, Hex hex) {
        if (hex.getStructure() != null) return 0;
        if (hex.getType() == Hex.Type.ICE) return 0;
        if (hex.getType() == Hex.Type.VOLCANO) return 0;

        int threatLevel = 0;
        for (Player player : turnOrder) {
            if (player == p) continue;

            final Hex.Type effectiveType = player.getHomeType() == Hex.Type.ICE ? game.getIceColor() : player.getHomeType();
            final boolean jump = !game.isReachable(hex, player) && game.isJumpable(hex, player);
            if (game.isReachable(hex, player) || jump) {
                final int cost;
                if (player.getFaction() instanceof Riverwalkers) {
                    if (player.unlockedTerrain[hex.getType().ordinal()]) {
                        ++threatLevel;
                    } else if (player.canAfford(Resources.fromCoins(game.isHomeType(hex.getType()) ? 2 : 1).combine(Resources.pw5))) {
                        ++threatLevel;
                    }
                } else {
                    if (player.getHomeType() == Hex.Type.VOLCANO) {
                        cost = game.getVolcanoDigCost(hex, player);
                    } else {
                        cost = Math.max(1, player.getFaction() instanceof Giants ? 2 : DigAction.getSpadeCost(hex, effectiveType));
                    }
                    if (player.canDig(cost, jump)) {
                        ++threatLevel;
                    }
                }
            }
        }
        return threatLevel;
    }

    public static void updateReachableHexMap() {
        final List<List<Hex>> reachableHexes = new ArrayList<>();

    }

    private static void add(List<Action> possibleActions, Action action, Game game, Player player) {
        action.setData(game, player);
        if (action.validatePhase() && action.canExecute()) {
            possibleActions.add(action);
        }
    }

    public static List<Action> getFeasibleActions(Game game, Player player) {
        final List<Action> possibleActions = new ArrayList<>();
        if (game.phase == Game.Phase.PICK_FACTIONS) {
            for (int i = 0; i < GameData.allFactions.size(); ++i) {
                add(possibleActions, new SelectFactionAction(i), game, player);
            }
            for (Hex.Type type : Hex.Type.values()) {
                add(possibleActions, new PickColorAction(type), game, player);
            }
        } else {
            add(possibleActions, new AdvanceAction(false), game, player);
            add(possibleActions, new AdvanceAction(true), game, player);
            add(possibleActions, new BurnAction(1), game, player);
            add(possibleActions, new ConvertAction(Resources.c1, 0, 0, 0, 0), game, player);
            add(possibleActions, new ConvertAction(Resources.w1, 0, 0, 0, 0), game, player);
            add(possibleActions, new ConvertAction(Resources.p1, 0, 0, 0, 0), game, player);
            add(possibleActions, new ConvertAction(Resources.zero, 1, 0, 0, 0), game, player);
            add(possibleActions, new ConvertAction(Resources.zero, 0, 1, 0, 0), game, player);
            add(possibleActions, new ConvertAction(Resources.zero, 0, 0, 1, 0), game, player);
            add(possibleActions, new ChaosMagiciansDoubleAction(), game, player);
            for (int i = 1; i < 16; ++i) {
                final boolean[] cultsToMax = {i % 2 == 1, (i >> 1) % 2 == 1, (i >> 2) % 2 == 1, (i >> 3) % 2 == 1};
                add(possibleActions, new ChooseMaxedCultsAction(cultsToMax), game, player);
            }
            for (int i = 0; i < 4; ++i) {
                add(possibleActions, new CultStepAction(i, 1, CultStepAction.Source.BON2), game, player);
                add(possibleActions, new CultStepAction(i, 1, CultStepAction.Source.FAV6), game, player);
                add(possibleActions, new CultStepAction(i, 2, CultStepAction.Source.ACTA), game, player);
                add(possibleActions, new CultStepAction(i, 1, CultStepAction.Source.LEECH), game, player);
                add(possibleActions, new CultStepAction(i, 1, CultStepAction.Source.ACOLYTES), game, player);
                add(possibleActions, new CultStepAction(i, 2, CultStepAction.Source.ACOLYTES), game, player);
                for (int amount = 1; amount <= 3; ++amount) {
                    add(possibleActions, new PriestToCultAction(i, amount), game, player);
                }
                add(possibleActions, new DarklingsConvertAction(i), game, player);
            }
            add(possibleActions, new EngineersBridgeAction(), game, player);
            add(possibleActions, new ForfeitAction(), game, player);
            add(possibleActions, new LeechAction(false), game, player);
            add(possibleActions, new LeechAction(true), game, player);
            for (int i = 0; i < game.mapData.length; ++i) {
                final String row = game.mapData[i];
                for (int j = 0; j < row.split(",").length; ++j) {
                    final Hex hex = game.getHex(i, j);
                    if (hex.getType() == Hex.Type.WATER) {
                        add(possibleActions, new MermaidsTownAction(i, j), game, player);
                    } else {
                        if (game.getVolcanoColor() != null && player.getHomeType() == Hex.Type.VOLCANO) {
                            if (player.getFaction() instanceof Acolytes) {
                                for (int cult = 0; cult < 4; ++cult) {
                                    add(possibleActions, new DigAction(i, j, Hex.Type.VOLCANO, cult), game, player);
                                }
                            } else {
                                add(possibleActions, new DigAction(i, j, Hex.Type.VOLCANO, false), game, player);
                                add(possibleActions, new DigAction(i, j, Hex.Type.VOLCANO, true), game, player);
                            }
                        } else {
                            for (Hex.Type type : Hex.Type.values()) {
                                if (type == Hex.Type.VOLCANO || type == Hex.Type.VARIABLE) {
                                    continue;
                                }
                                if (type == Hex.Type.ICE && (game.getIceColor() == null || player.getHomeType() != Hex.Type.ICE)) {
                                    continue;
                                }
                                add(possibleActions, new DigAction(i, j, type, false), game, player);
                                add(possibleActions, new DigAction(i, j, type, true), game, player);
                            }
                        }
                        for (Hex.Structure structure : Hex.Structure.values()) {
                            add(possibleActions, new BuildAction(i, j, structure), game, player);
                        }
                        add(possibleActions, new PlaceInitialDwellingAction(i, j), game, player);
                        add(possibleActions, new SandstormAction(i, j), game, player);
                    }
                    for (int k = 0; k < game.mapData.length; ++k) {
                        final String row2 = game.mapData[k];
                        for (int l = 0; l < row2.split(",").length; ++l) {
                            final Hex hex2 = game.getHex(k, l);
                            if (hex2.getType() != Hex.Type.WATER) {
                                add(possibleActions, new PlaceBridgeAction(i, j, k, l), game, player);
                            }
                        }
                    }
                }
            }
            add(possibleActions, new NomadsSandstormAction(), game, player);
            add(possibleActions, new PassAction(), game, player);
            for (Hex.Type type : Hex.Type.values()) {
                add(possibleActions, new ShapeshifterColorAction(false, type), game, player);
                add(possibleActions, new ShapeshifterColorAction(true, type), game, player);
                add(possibleActions, new UnlockTerrainAction(type), game, player);
            }
            for (int i = 0; i < 3 + game.getPlayerCount(); ++i) {
                add(possibleActions, new SelectBonAction(i), game, player);
            }
            for (int i = 1; i <= 12; ++i) {
                add(possibleActions, new SelectFavAction(i), game, player);
            }
            for (int i = 1; i <= 6; ++i) {
                add(possibleActions, new SelectPowerActionAction(i), game, player);
            }
            for (int i = 1; i <= 8; ++i) {
                add(possibleActions, new SelectTownAction(i), game, player);
            }
            add(possibleActions, new ShapeshifterPowerAction(false), game, player);
            add(possibleActions, new ShapeshifterPowerAction(true), game, player);
            add(possibleActions, new SpadeAction(SpadeAction.Source.BON1), game, player);
            add(possibleActions, new SpadeAction(SpadeAction.Source.ACTG), game, player);
            add(possibleActions, new SwarmlingsFreeTradingPostAction(), game, player);
            add(possibleActions, new WitchesFreeDwellingAction(), game, player);
        }
        return possibleActions;
    }
}
