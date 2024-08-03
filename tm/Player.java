package tm;

import tm.action.*;
import tm.faction.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Player extends JPanel {

    public enum PendingType {
        SELECT_FAV("Select Fav", false),
        SELECT_TOWN("Select Town", false),
        CONVERT_W2P("Convert W -> P", true),
        PLACE_BRIDGE("Place Bridge", true),
        USE_SPADES("Use Spades", true),
        BUILD("Build Dwelling", true),
        SANDSTORM("Sandstorm", true),
        FREE_TP("Free TP", true),
        FREE_D("Free D", true),
        CHOOSE_CULTS("Choose Cults", true),
        CULT_STEP("Cult Step", false),
        UNLOCK_TERRAIN("Unlock Terrain", false);

        private final String description;
        private final boolean skippable;

        private PendingType(String description, boolean skippable) {
            this.description = description;
            this.skippable = skippable;
        }

        public String getDescription() {
            return description;
        }

        public boolean isSkippable() {
            return skippable;
        }
    };

    private final Game game;

    private int coins;
    private int workers;
    private int priests;
    public int maxPriests = 7;
    private final int[] power = new int[3];
    private int keys;
    private int points = 20;
    private Resources favorIncome = Resources.zero;
    private final int[] cultSteps = new int[4];
    private static final int[] cumulativeCultPower = { 0, 0, 0, 1, 1, 3, 3, 5, 5, 5, 8 };
    private Faction faction;
    private int bridgesLeft = 3;

    private final boolean[] ownedFavors = new boolean[12];
    private boolean passed;
    private Round round;
    private int pendingSpades;
    private int pendingFavors;
    private int pendingWorkerToPriestConversions;
    private int pendingBridges;
    private int pendingTowns;
    private final Deque<Integer> pendingLeechQueue = new ArrayDeque<>(2);
    List<Hex> pendingBuilds = null;
    public boolean pendingSandstorm;
    public boolean pendingFreeTradingPost;
    public boolean pendingFreeDwelling;
    public int pendingCultSteps;
    public int pendingTerrainUnlock;
    public final boolean[] maxedCults = new boolean[4];
    private boolean rangeUsedForDigging;
    public boolean allowExtraSpades;

    public boolean usedFactionAction;
    public boolean[] usedFav6 = new boolean[1];

    private int dwellings;
    private int tradingPosts;
    private int temples;
    private int strongholds;
    private int sanctuaries;
    private int shipping;
    private int digging = 3;
    private int range = 1;
    private Resources jumpCost = Resources.zero;
    private int initialFav;

    private final List<Integer> bons = new ArrayList<>();
    private final List<Integer> favs = new ArrayList<>();
    private final List<Integer> towns = new ArrayList<>();
    private final PlayerInfo data;
    private final Pool pool;
    private final String name;
    public boolean[] unlockedTerrain;

    public Player(Game game, String name) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.game = game;
        this.name = name;
        data = new PlayerInfo();
        pool = new Pool(game, this, bons, null, favs, towns, game.bonUsed, usedFav6);
        add(data);
        add(pool);
    }

    public void reset() {
        coins = 0;
        workers = 0;
        priests = 0;
        maxPriests = 7;
        keys = 0;
        favorIncome = Resources.zero;
        bridgesLeft = 3;
        Arrays.fill(ownedFavors, false);
        passed = false;
        round = null;
        pendingSpades = 0;
        pendingFavors = 0;
        pendingWorkerToPriestConversions = 0;
        pendingBridges = 0;
        pendingTowns = 0;
        pendingBuilds = null;
        pendingSandstorm = false;
        pendingFreeTradingPost = false;
        pendingFreeDwelling = false;
        pendingCultSteps = 0;
        pendingTerrainUnlock = 0;
        for (int i = 0; i < 4; ++i) maxedCults[i] = false;
        allowExtraSpades = false;
        rangeUsedForDigging = false;
        dwellings = 0;
        tradingPosts = 0;
        temples = 0;
        strongholds = 0;
        sanctuaries = 0;
        shipping = 0;
        range = 1;
        jumpCost = Resources.zero;
        usedFactionAction = false;
        usedFav6[0] = false;
        bons.clear();
        favs.clear();
        towns.clear();
        unlockedTerrain = null;
        if (game.phase == Game.Phase.INITIAL_DWELLINGS) {
            selectFaction(faction);
            if (initialFav > 0) {
                game.selectFav(this, initialFav);
            }
            if (game.getVariableColor() != null) {
                unlockedTerrain[game.getVariableColor().ordinal()] = true;
            }
        } else {
            faction = null;
        }
    }

    public int getWorkers() {
        return workers;
    }

    public int getCultSteps(int cult) {
        return cultSteps[cult];
    }

    public int getRemainingKeys() {
        return keys;
    }

    public Faction getFaction() {
        return faction;
    }

    public Hex.Type getHomeType() {
        if (faction.getHomeType() == Hex.Type.VARIABLE && game.getVariableColor() != null) {
            return game.getVariableColor();
        }
        return faction.getHomeType();
    }

    public int getShipping() {
        final int tmp = bons.isEmpty() ? 0 : (bons.get(0) == 4 ? 1 : 0);
        return shipping + tmp;
    }

    public int getRange() {
        return range;
    }

    public int getPoints() {
        return points;
    }

    public void selectFaction(Faction faction) {
        this.faction = faction;
        this.points = game.getStartingVictoryPoints(faction);
        power[0] = faction.getInitialPowerTokenCount();
        power[1] = 0;
        power[2] = 0;
        if (faction instanceof Dwarves) {
            range = 2;
            jumpCost = Resources.w2;
        } else if (faction instanceof Fakirs) {
            range = 2;
            jumpCost = Resources.p1;
        }
        shipping = faction instanceof Mermaids ? 1 : 0;
        digging = 3;
        addIncome(faction.getInitialIncome());
        final int[] initialCultSteps = faction.getInitialCultSteps();
        System.arraycopy(initialCultSteps, 0, cultSteps, 0, cultSteps.length);
        game.factionPicked(this, faction);
        if (faction instanceof IceMaidens) {
            ++pendingFavors;
        } else if (faction instanceof Riverwalkers) {
            maxPriests = 1;
            unlockedTerrain = new boolean[7];
            shipping = 1;
        }
    }

    public void addFavor(int number) {
        if (pendingFavors == 0)
            throw new RuntimeException("Unable to choose favor");

        if (ownedFavors[number - 1])
            throw new RuntimeException("Duplicate favor added");

        addCultSteps(Favs.favCults[number - 1]);
        favorIncome = favorIncome.combine(Favs.getFavIncome(number));
        ownedFavors[number - 1] = true;
        --pendingFavors;
        favs.add(number);
        if (game.phase == Game.Phase.INITIAL_DWELLINGS) {
            initialFav = number;
        }
        refreshSize();
    }

    public boolean canAddFavor(int number) {
        return pendingFavors > 0 && !ownedFavors[number - 1];
    }

    public boolean hasFavor(int number) {
        return ownedFavors[number - 1];
    }

    public void placeInitialDwelling() {
        ++dwellings;
    }

    public boolean canBuildDwelling(boolean useRange) {
        if (useRange && jumpCost == Resources.zero) {
            throw new RuntimeException("Cannot jump to build");
        }
        return dwellings < 8 && (canAfford(useRange ? jumpCost.combine(faction.getDwellingCost()) : faction.getDwellingCost()) || pendingFreeDwelling);
    }

    public void buildDwelling() {
        if (!canBuildDwelling(false))
            throw new RuntimeException("Unable to build more dwellings");

        ++dwellings;
        points += round.d;
        if (ownedFavors[10]) {
            points += 2;
        }
        if (pendingFreeDwelling) {
            pendingFreeDwelling = false;
        } else {
            pay(faction.getDwellingCost());
        }
    }

    public boolean canBuildTradingPost(boolean expensive) {
        return tradingPosts < 4 && dwellings > 0 && (canAfford(expensive ? faction.getExpensiveTradingPostCost() : faction.getTradingPostCost()) || pendingFreeTradingPost);
    }

    public void buildTradingPost(boolean expensive) {
        if (!canBuildTradingPost(expensive))
            throw new RuntimeException("Unable to build more trading posts");

        ++tradingPosts;
        --dwellings;
        points += round.tp;
        if (ownedFavors[9]) {
            points += 3;
        }
        if (pendingFreeTradingPost) {
            pendingFreeTradingPost = false;
        } else {
            pay(expensive ? faction.getExpensiveTradingPostCost() : faction.getTradingPostCost());
        }
    }

    public boolean canBuildTemple() {
        return temples < 3 && tradingPosts > 0 && canAfford(faction.getTempleCost());
    }

    public void buildTemple() {
        if (!canBuildTemple())
            throw new RuntimeException("Unable to build more temples");

        ++temples;
        --tradingPosts;
        points += round.te;
        pay(faction.getTempleCost());
        final List<Integer> favOptions = game.getSelectableFavs(this);
        final int newFavs = Math.max(favOptions.size(), faction instanceof ChaosMagicians ? 2 : 1);
        if (newFavs == favOptions.size()) {
            // Automatically add the new favors because there are no other choices.
            favOptions.forEach(this::addFavor);
        } else {
            pendingFavors += newFavs;
        }
    }

    public boolean canBuildStronghold() {
        return strongholds < 1 && tradingPosts > 0 && canAfford(faction.getStrongholdCost());
    }

    public void buildStronghold() {
        if (!canBuildStronghold())
            throw new RuntimeException("Unable to build stronghold");

        ++strongholds;
        --tradingPosts;
        points += round.shsa;
        pay(faction.getStrongholdCost());

        if (faction instanceof Alchemists) {
            addIncome(Resources.pw12);
        } else if (faction instanceof Auren) {
            ++pendingFavors;
        } else if (faction instanceof Cultists) {
            points += 7;
        } else if (faction instanceof Darklings) {
            pendingWorkerToPriestConversions = 3;
            if (!game.rewinding && !game.importing) {
                final int workers = game.getCurrentPlayer().getWorkers();
                final String[] choices = IntStream.range(0, Math.min(3, workers) + 1).boxed().sorted((a, b) -> b - a).map(Object::toString).toArray(String[]::new);
                final int response = JOptionPane.showOptionDialog(game, "Convert W to P...", "Darklings SH Conversion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, null);
                if (response >= 0 && response < choices.length) {
                    game.resolveAction(new DarklingsConvertAction(response));
                }
            }
        } else if (faction instanceof Dragonlords) {
            power[0] += game.getPlayerCount();
        } else if (faction instanceof Dwarves) {
            jumpCost = Resources.w1;
        } else if (faction instanceof Fakirs) {
            ++range;
        } else if (faction instanceof Halflings) {
            addSpades(3, false);
        } else if (faction instanceof Mermaids) {
            if (faction.getMaxShipping() > shipping) {
                ++shipping;
                points += faction.getAdvanceShippingPoints(shipping);
            }
        }
    }

    public boolean canBuildSanctuary() {
        return sanctuaries < 1 && temples > 0 && canAfford(faction.getSanctuaryCost());
    }

    public void buildSanctuary() {
        if (!canBuildSanctuary())
            throw new RuntimeException("Unable to build sanctuary");

        ++sanctuaries;
        --temples;
        points += round.shsa;
        pay(faction.getSanctuaryCost());
        final List<Integer> favOptions = game.getSelectableFavs(this);
        final int newFavs = Math.max(favOptions.size(), faction instanceof ChaosMagicians ? 2 : 1);
        if (newFavs == favOptions.size()) {
            // Automatically add the new favors because there are no other choices.
            favOptions.forEach(this::addFavor);
        } else {
            pendingFavors += newFavs;
        }
    }

    public void foundTown(int number) {
        ++keys;
        points += Towns.getTownPoints(number);
        addIncome(Towns.getTownIncome(number));
        switch (number) {
            case 5 -> {
                addCultSteps(new int[] { 1, 1, 1, 1 });
            }
            case 6 -> {
                ++keys;
                addCultSteps(new int[] { 2, 2, 2, 2 });
            }
            case 7 -> {
                if (faction.getMaxShipping() > shipping) {
                    ++shipping;
                    points += faction.getAdvanceShippingPoints(shipping);
                } else if (faction instanceof Fakirs) {
                    ++range;
                }
            }
        }
        if (faction instanceof Witches) {
            points += 5;
        } else if (faction instanceof Swarmlings) {
            addIncome(Resources.w3);
        }
        points += round.town;
        towns.add(number);
        --pendingTowns;

        // You can pick fav5 or build SA to complete town, but max out cult before receiving the key.
        for (int i = 0; i < 4; ++i) {
            if (cultSteps[i] == 9 && maxedCults[i]) {
                cultSteps[i] = addPowerFromCultSteps(9, 1, game.cultOccupied(i));
                if (keys == 0) {
                    break;
                }
            }
        }
        refreshSize();
    }

    public void convert(Resources r) {
        if (r.power > 0)
            throw new RuntimeException("Trying to convert power to power");

        final int power = ConvertAction.getPowerCost(r);
        if (!canAffordPower(power))
            throw new RuntimeException("Not enough power for conversion");

        payPower(power);
        addIncome(r);
    }

    public boolean canUseAction(int act) {
        if (game.usedPowerActions[act - 1] && (!(faction instanceof Yetis) || !hasStronghold())) return false;

        if (act == 1 && bridgesLeft == 0) return false;

        return canAffordPower(PowerActions.getRequiredPower(this, act));
    }

    public void usePowerAction(int act) {
        if (!canUseAction(act))
            throw new RuntimeException("Unable to afford action " + act);

        payPower(PowerActions.getRequiredPower(this, act));
        switch (act) {
            case 1 -> ++pendingBridges;
            case 2 -> addIncome(Resources.p1);
            case 3 -> addIncome(Resources.w2);
            case 4 -> addIncome(Resources.c7);
            case 5 -> addSpades(1, true);
            case 6 -> addSpades(2, true);
        }
        game.usedPowerActions[act - 1] = true;
    }

    public boolean canAdvanceShipping() {
        return faction.getMaxShipping() > shipping && canAfford(faction.getAdvanceShippingCost());
    }

    public void advanceShipping() {
        if (!canAdvanceShipping())
            throw new RuntimeException("Trying to advance shipping too much");

        ++shipping;
        points += faction.getAdvanceShippingPoints(shipping);
        pay(faction.getAdvanceShippingCost());
    }

    public boolean canAdvanceDigging() {
        return faction.getMinDigging() < digging && canAfford(faction.getAdvanceDiggingCost());
    }

    public void advanceDigging() {
        if (!canAdvanceDigging())
            throw new RuntimeException("Trying to advance digging too much");

        --digging;
        points += 6;
        pay(faction.getAdvanceDiggingCost());
    }

    public boolean canLeech() {
        return power[0] > 0 || power[1] > 0;
    }

    public int getMaxLeech() {
        return Math.min(points + 1, power[0] * 2 + power[1]);
    }

    public void acceptLeech() {
        if (!pendingLeechQueue.isEmpty()) {
            final int pendingLeech = pendingLeechQueue.removeFirst();
            final int unused = addPower(pendingLeech);
            if (unused < pendingLeech) {
                points -= pendingLeech - unused - 1;
            }
        }
    }

    public void declineLeech() {
        pendingLeechQueue.removeFirst();
    }

    public void addPendingLeech(int amount) {
        pendingLeechQueue.addLast(Math.min(amount, points + 1));
    }

    public void addPendingBuild(Hex hex) {
        if (dwellings < 8) {
            if (pendingBuilds == null) {
                pendingBuilds = new ArrayList<>();
                pendingBuilds.add(hex);
            } else if (!pendingBuilds.isEmpty()) {
                pendingBuilds.add(hex);
            }
        }
    }

    public boolean hasPendingBuild(Hex hex) {
        return pendingBuilds != null && pendingBuilds.contains(hex);
    }

    public void clearPendingBuilds() {
        pendingBuilds.clear();
    }

    public int getPendingLeech() {
        return pendingLeechQueue.isEmpty() ? 0 : pendingLeechQueue.peekFirst();
    }

    private int addPower(int amount) {
        while (amount > 0 && power[0] > 0) {
            ++power[1];
            --power[0];
            --amount;
        }
        while (amount > 0 && power[1] > 0) {
            ++power[2];
            --power[1];
            --amount;
        }
        return amount;
    }

    public void addCultSteps(int[] cults) {
        for (int i = 0; i < 4; ++i) {
            if (cultSteps[i] < 10 && cultSteps[i] + cults[i] >= 10 && !game.cultOccupied((i))) {
                maxedCults[i] = true;
            }
            cultSteps[i] = addPowerFromCultSteps(cultSteps[i], cults[i], game.cultOccupied(i));
        }
    }

    public void setCultSteps(int[] cults) {
        for (int i = 0; i < 4; ++i) {
            cultSteps[i] = cults[i];
        }
    }

    public boolean canSendPriestToCult() {
        return priests > 0;
    }

    public void sendPriestToCult(int cult, int amount) {
        --priests;
        if (amount > 1) {
            --maxPriests;
        }
        if (faction instanceof Acolytes && strongholds > 0) {
            ++amount;
        }
        cultSteps[cult] = addPowerFromCultSteps(cultSteps[cult], amount, game.cultOccupied(cult));
    }

    private void addIncomeFromCults(int steps, int requiredSteps, Resources income) {
        if (requiredSteps > 0) {
            int rewardCount = steps / requiredSteps;
            while (rewardCount-- > 0) {
                addIncome(income);
            }
        }
    }

    private int addPowerFromCultSteps(int oldValue, int addition, boolean occupied) {
        if (addition > 0 && oldValue < 10) {
            int newValue = Math.min(10, oldValue + addition);
            if (newValue == 10 && (occupied || keys == 0)) {
                --newValue;
            }
            if (newValue > oldValue) {
                addPower(cumulativeCultPower[newValue] - cumulativeCultPower[oldValue]);
                if (newValue == 10) {
                    --keys;
                }
                return newValue;
            }
        }
        return oldValue;
    }

    public void addIncome(Resources income) {
        coins += income.coins;
        workers += income.workers;
        if (faction instanceof Riverwalkers) {
            /*
            int possibleCheapUnlocks = 0;
            int possibleExpensiveUnlocks = 0;
            if (unlockedTerrain != null) {
                for (int i = 0; i < 7; ++i) {
                    if (!unlockedTerrain[i]) {
                        if (game.isHomeType(Hex.Type.values()[i])) {
                            ++possibleExpensiveUnlocks;
                        } else {
                            ++possibleCheapUnlocks;
                        }
                    }
                }
            }
            int coinsLeft = coins;
            int priestsLeft = income.priests;
            while (priestsLeft > 0) {
                if (possibleCheapUnlocks > 0) {
                    if (coinsLeft > 0) {
                        --coinsLeft;
                        --priestsLeft;
                        --possibleCheapUnlocks;
                        ++pendingTerrainUnlock;
                    } else {
                        break;
                    }
                } else if (possibleExpensiveUnlocks > 0) {
                    if (coinsLeft > 1) {
                        coinsLeft -= 2;
                        --priestsLeft;
                        --possibleExpensiveUnlocks;
                        ++pendingTerrainUnlock;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            priests = Math.min(priests + priestsLeft, maxPriests);
             */
            pendingTerrainUnlock += income.priests;
        } else {
            addPriests(income.priests);
        }
        addPower(income.power);
        if (income == Resources.spade) {
            addSpades(1, false);
        }
    }

    public void addPriests(int priests) {
        this.priests = Math.min(this.priests + priests, maxPriests);
    }

    public boolean canConvert(int priests, int workers, int pointsToCoins, int pointsFromCoins) {
        final int priestsToCoins = Math.min(priests, workers);
        if (this.priests >= priests && this.workers >= workers - priestsToCoins && (pointsToCoins == 0 || (faction instanceof Alchemists && points >= pointsToCoins))) {
            if (pointsFromCoins > 0) {
                final int ratio = faction instanceof Alchemists ? 2 : 3;
                return (coins + workers + priestsToCoins) * ratio >= pointsFromCoins;
            }
            return true;
        }
        return false;
    }

    public void convert(int priests, int workers, int pointsToCoins, int pointsFromCoins) {
        this.priests -= priests;
        this.workers -= workers;
        this.workers += priests;
        this.coins += workers;
        int ratio = 3;
        if (faction instanceof Alchemists) {
            points -= pointsToCoins;
            coins += pointsToCoins;
            ratio = 2;
        }
        this.points += pointsFromCoins;
        this.coins -= ratio * pointsFromCoins;
    }

    public int autoConvert() {
        burn(getMaxBurn());
        convert(Resources.fromCoins(power[2]));
        convert(priests, workers + priests, 0, 0);
        final int ratio = faction instanceof Alchemists ? 2 : 3;
        final int vp = coins / ratio;
        convert(0, 0, 0, vp);
        return vp;
    }

    public void score(int vp) {
        points += vp;
    }

    public void dig(int amount) {
        if (faction instanceof Darklings) {
            if (priests < amount) {
                throw new RuntimeException("Cannot afford to dig " + amount);
            }
            points += 2 * amount;
            priests -= amount;
        } else {
            if (workers < amount * digging)
                throw new RuntimeException("Cannot afford to dig " + amount);

            if ((amount + pendingSpades) % 2 != 0 && faction instanceof Giants)
                throw new RuntimeException("Giants can only dig even amounts");

            workers -= amount * digging;
        }
        addSpades(amount, false);
    }

    public void volcanoDig(int amount, int cult) {
        if (cult >= 0) {
            if (cultSteps[cult] < amount) {
                throw new RuntimeException("Cannot afford to dig using cult steps");
            }
            cultSteps[cult] -= amount;
        } else { // Dragonlords
            while (amount-- > 0) {
                if (power[0] > 0) {
                    --power[0];
                } else if (power[1] > 0) {
                    --power[1];
                } else if (power[2] > 0 ) {
                    --power[2];
                } else {
                    throw new RuntimeException("Cannot afford to dig" + amount);
                }
            }
        }
        pendingSpades = 1;
        allowExtraSpades = false;
    }

    public boolean canDig(int amount, boolean useRange) {
        if (useRange && jumpCost == Resources.zero) {
            throw new RuntimeException("Cannot use range to dig");
        }
        if (faction instanceof Darklings) {
            return priests >= amount;
        } else if (faction instanceof Dragonlords) {
            return power[0] + power[1] + power[2] >= amount;
        } else if (faction instanceof Acolytes) {
            for (int i = 0; i < 4; ++i) {
                if (cultSteps[i] >= amount) {
                    return true;
                }
            }
            return false;
        } else {
            if ((amount + pendingSpades) % 2 != 0 && faction instanceof Giants) return false;
            if (useRange) {
                if (rangeUsedForDigging) {
                    return false;
                }
                final Resources totalCost = jumpCost.combine(Resources.fromWorkers(digging * amount));
                return canAfford(totalCost);
            }
            return workers >= digging * amount;
        }
    }

    public void addSpades(int amount, boolean allowExtraSpades) {
        if (faction.getHomeType() == Hex.Type.VOLCANO) {
            if (faction instanceof Acolytes) {
                pendingCultSteps += amount;
            } else if (faction instanceof Dragonlords) {
                power[0] += amount;
            }
            points += amount * round.spade;
        } else if (!(faction instanceof Riverwalkers)) {
            pendingSpades += amount;
            this.allowExtraSpades = allowExtraSpades;
        }
    }

    public int getPendingSpades() {
        return pendingSpades;
    }

    public void useSpades(int amount) {
        if (pendingSpades < amount)
            throw new RuntimeException("Trying to use too many spades");

        pendingSpades -= amount;
        if (faction.getHomeType() != Hex.Type.VOLCANO) {
            // TODO: Maybe volcano factions shouldn't call this method at all
            points += amount * round.spade;
            allowExtraSpades = false;
            if (faction instanceof Halflings) {
                points += amount;
            } else if (faction instanceof Alchemists && strongholds > 0) {
                addPower(2 * amount);
            }
        }
        if (pendingSpades == 0) {
            rangeUsedForDigging = false;
        }
    }

    public void useRange(boolean dig) {
        if (!canAfford(jumpCost))
            throw new RuntimeException("Unable to afford range usage");

        if (dig) {
            rangeUsedForDigging = true;
        }
        pay(jumpCost);
        points += 4;
    }

    public boolean canUseRange() {
        return canAfford(jumpCost);
    }

    private Resources getIncome() {
        Resources income = faction.getBaseIncome().combine(favorIncome);
        for (int i = 0; i < dwellings; ++i) {
            income = income.combine(faction.getDwellingIncome(i));
        }
        for (int i = 0; i < tradingPosts; ++i) {
            income = income.combine(faction.getTradingPostIncome(i));
        }
        for (int i = 0; i < temples; ++i) {
            income = income.combine(faction.getTempleIncome(i));
        }
        if (strongholds > 0) {
            income = income.combine(faction.getStrongholdIncome());
        }
        if (sanctuaries > 0) {
            income = income.combine(faction.getSanctuaryIncome());
        }
        return income;
    }

    private void addRoundIncome() {
        addIncome(favorIncome);
        addIncome(faction.getBaseIncome());
        for (int i = 0; i < dwellings; ++i) {
            addIncome(faction.getDwellingIncome(i));
        }
        for (int i = 0; i < tradingPosts; ++i) {
            addIncome(faction.getTradingPostIncome(i));
        }
        addIncome(Bons.getBonIncome(bons.get(0))); // Receive coins before any priests so Riverwalker terrain unlock would work correctly
        for (int i = 0; i < temples; ++i) {
            addIncome(faction.getTempleIncome(i));
        }
        if (strongholds > 0) {
            addIncome(faction.getStrongholdIncome());
        }
        if (sanctuaries > 0) {
            addIncome(faction.getSanctuaryIncome());
        }
    }

    public void addCultIncome(Round round) {
        addIncomeFromCults(cultSteps[0], round.fire, round.income);
        addIncomeFromCults(cultSteps[1], round.water, round.income);
        addIncomeFromCults(cultSteps[2], round.earth, round.income);
        addIncomeFromCults(cultSteps[3], round.air, round.income);
        addIncomeFromCults(7 - maxPriests, round.priests, round.income);
    }

    public void startRound(Round round) {
        this.round = round;
        addRoundIncome();
        usedFav6[0] = false;
        passed = false;
        usedFactionAction = false;
    }

    public void burn(int amount) {
        if (amount > getMaxBurn())
            throw new RuntimeException("Unable to burn " + amount);

        power[1] -= 2 * amount;
        power[2] += amount;
    }

    public int getMaxBurn() {
        return power[1] / 2;
    }

    public boolean canAffordPower(int powerCost) {
        return power[2] + getMaxBurn() >= powerCost;
    }

    public int getNeededBurn(int powerCost) {
        return Math.max(0, powerCost - power[2]);
    }

    public boolean canAfford(Resources r) {
        return coins >= r.coins && workers >= r.workers && priests >= r.priests && canAffordPower(r.power);
    }

    public void pay(Resources r) {
        if (!canAfford(r))
            throw new RuntimeException("Unable to afford payment");

        coins -= r.coins;
        workers -= r.workers;
        priests -= r.priests;
        payPower(r.power);
    }

    private void payPower(int power) {
        while (this.power[2] < power) {
            burn(1);
        }
        this.power[2] -= power;
        this.power[0] += power;
    }

    public void pass() {
        switch (bons.get(0)) {
            case 6 -> points += 4 * (strongholds + sanctuaries);
            case 7 -> points += 2 * tradingPosts;
            case 9 -> points += dwellings;
            case 10 -> points += 3 * shipping;
        }
        if (ownedFavors[11]) {
            final int[] fav12 = { 0, 2, 3, 3, 4 };
            points += fav12[tradingPosts];
        }
        if (faction instanceof Engineers && strongholds > 0) {
            points += (3 - bridgesLeft) * 3;
        }
        if (faction instanceof IceMaidens && strongholds > 0) {
            points += 3 * temples;
        }
        passed = true;
    }

    public int pickBon(int newBon, int coins) {
        if (bons.isEmpty()) {
            bons.add(0);
            refreshSize();
        }
        final int oldBon = bons.set(0, newBon);
        this.coins += coins;
        return oldBon;
    }

    public int removeBon() {
        final int bon = bons.remove(0);
        refreshSize();
        return bon;
    }

    public int getBon() {
        return bons.get(0);
    }

    class PlayerInfo extends JPanel {

        PlayerInfo() {
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (faction.getPowerAction(strongholds > 0) != null) {
                        if (PowerActions.actionClicked(e.getX() - 250, e.getY() - 24)) {
                            if (faction instanceof Auren && game.getCurrentPlayer().getFaction() instanceof Auren) {
                                if (CultStepAction.isSourceValid(CultStepAction.Source.ACTA, game, Player.this)) {
                                    final int cult = Cults.selectCult(game, 2, false);
                                    if (cult >= 0 && cult < 4) {
                                        game.resolveAction(new CultStepAction(cult, 2, CultStepAction.Source.ACTA));
                                    }
                                }
                            } else if (faction instanceof Engineers && game.getCurrentPlayer().getFaction() instanceof Engineers) {
                                game.resolveAction(new EngineersBridgeAction());
                            } else if (faction instanceof Mermaids && game.getCurrentPlayer().getFaction() instanceof Mermaids) {
                                game.highlightMermaidTownSpots();
                            } else if (faction instanceof ChaosMagicians && game.getCurrentPlayer().getFaction() instanceof ChaosMagicians) {
                                if (!usedFactionAction) {
                                    game.resolveAction(new ChaosMagiciansDoubleAction());
                                }
                            } else if (faction instanceof Giants && game.getCurrentPlayer().getFaction() instanceof Giants) {
                                game.resolveAction(new SpadeAction(SpadeAction.Source.ACTG));
                            } else if (faction instanceof Nomads && game.getCurrentPlayer().getFaction() instanceof Nomads) {
                                game.resolveAction(new NomadsSandstormAction());
                            } else if (faction instanceof Swarmlings && game.getCurrentPlayer().getFaction() instanceof Swarmlings) {
                                game.resolveAction(new SwarmlingsFreeTradingPostAction());
                            } else if (faction instanceof Witches && game.getCurrentPlayer().getFaction() instanceof Witches) {
                                game.resolveAction(new WitchesFreeDwellingAction());
                            }
                        }
                    }
                    if (faction instanceof Riverwalkers && game.getVariableColor() != null) {
                        final int x = e.getX() - 335;
                        final int y = e.getY() - 25;
                        final int circleRadius = 8;
                        final int wheelRadius = 25;
                        double angle = Math.PI * 1.5;
                        for (int i = 0; i < 7; ++i) {
                            final Hex.Type circleType = Hex.Type.values()[(game.getVariableColor().ordinal() - i + 7) % 7];
                            final int dx = (int) (Math.cos(angle) * wheelRadius + 0.5);
                            final int dy = (int) (Math.sin(angle) * wheelRadius + 0.5);
                            final int px = wheelRadius + dx + circleRadius;
                            final int py = wheelRadius + dy + circleRadius;
                            final int distX = px - x;
                            final int distY = py - y;
                            if (distX * distX + distY * distY <= circleRadius * circleRadius) {
                                game.resolveAction(new UnlockTerrainAction((unlockedTerrain[circleType.ordinal()] ? null : circleType)));
                                break;
                            }
                            angle += 2 * Math.PI / 7;
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
        }
        @Override
        public void paint(Graphics g) {
            int dx = 5;
            int dy = 5;
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            final boolean myTurn = game.isMyTurn(Player.this);
            final boolean passed = Player.this.passed && game.phase != Game.Phase.END;
            if (faction != null) {
                Color factionColor = getHomeType().getBuildingColor();
                if (passed) {
                    factionColor = new Color(factionColor.getRed(), factionColor.getGreen(), factionColor.getBlue(), 50);
                }
                g.setColor(factionColor);
                g.fillRect(dx, dy, 400, 16);
                paintColorWheel(g, 335, 25);
                g.setColor(getHomeType().getFontColor());
                String factionName = faction.getName();
                if (!name.isEmpty()) {
                    factionName += " (" + name + ")";
                }
                if (myTurn && game.phase != Game.Phase.END) {
                    if (game.phase == Game.Phase.CONFIRM_ACTION) {
                        final String pending = game.getCurrentPlayer().getPendingActions().stream().map(PendingType::getDescription).collect(Collectors.joining(" / ")).toUpperCase();
                        final String txt = pending.isEmpty() ? "CONFIRM TURN" : pending;
                        factionName += " - " + txt;
                    } else {
                        factionName = "> " + factionName;
                    }
                } else if (passed) {
                    factionName += ", passed";
                }
                g.drawString(factionName, dx + 3, dy + 12);

                dy += 18;
                g.setColor(Color.BLACK);
                String data = coins + " c, " + workers + " w, ";
                // TODO: Color priests red if maxed out
                data += priests + "/" + maxPriests + " p";
                data += ", " + points + " vp, ";
                data += power[0] + "/" + power[1] + "/" + power[2] + " pw";
                g.drawString(data, dx, dy + 12);
                dy += 16;
                data = "dig level " + (3 - digging) + "/" + (3 - faction.getMinDigging()) + ", ";
                if (faction instanceof Fakirs) {
                    data += "range " + (range - 1) + "/4";
                } else if (faction instanceof Dwarves) {
                    data += "range " + (range - 1) + "/1";
                } else {
                    data += "ship level " + shipping + "/" + faction.getMaxShipping();
                }
                g.drawString(data, dx, dy + 12);
                dy += 20;

                g.setColor(new Color(0xCCCCCC));
                for (int i = 0; i < 3; ++i) {
                    g.fillRect(dx + 72 * i, dy, 36, 36);
                }
                dy += 3;
                g.setColor(Color.BLACK);
                g.drawString("D", dx + 7, dy + 12);
                g.drawString("TP", dx + 43, dy + 12);
                g.drawString("TE", dx + 79, dy + 12);
                g.drawString("SH", dx + 115, dy + 12);
                g.drawString("SA", dx + 151, dy + 12);
                dy += 16;
                g.setColor(dwellings == 8 ? Color.RED : Color.BLACK);
                g.drawString(dwellings + "/8", dx + 7, dy + 12);
                g.setColor(tradingPosts == 4 ? Color.RED : Color.BLACK);
                g.drawString(tradingPosts + "/4", dx + 43, dy + 12);
                g.setColor(temples == 3 ? Color.RED : Color.BLACK);
                g.drawString(temples + "/3", dx + 79, dy + 12);
                g.setColor(Color.BLACK);
                g.drawString(strongholds + "/1", dx + 115, dy + 12);
                g.drawString(sanctuaries + "/1", dx + 151, dy + 12);
                dy += 30;
                Resources income = getIncome();
                // TODO: Color overflowing priests and power in red
                g.drawString("Income:   " + income.coins + " c   " + income.workers + " w   " + income.priests + " p   " + income.power + " pw", dx, dy + 12);

                if (faction.getPowerAction(strongholds > 0) != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    final Color oldColor = g.getColor();
                    final Stroke oldStroke = g2d.getStroke();
                    PowerActions.drawPowerAction(g2d, 250, 24, faction.getPowerAction(strongholds > 0), usedFactionAction);
                    g.setColor(oldColor);
                    g2d.setStroke(oldStroke);
                }
            } else {
                String displayName = name;
                if (myTurn) displayName = "> " + displayName;
                g.setColor(Color.BLACK);
                g.drawString(displayName, dx +4, dy + 12);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            final int itemsPerRow = 6;
            final int itemCount = (int) (bons.size() + favs.stream().distinct().count() + towns.stream().distinct().count());
            final int rows = (itemCount - 1) / itemsPerRow + 1;
            final int minHeight = 20 + 105 * rows;
            return new Dimension(410, Math.max(128, minHeight));
        }
    }

    public void convertWorkersToPriests(int amount) {
        pendingWorkerToPriestConversions = 0;
        addIncome(Resources.fromPriests(amount));
        pay(Resources.fromWorkers(amount));
    }

    void addPendingTowns(int amount) {
        pendingTowns += amount;
    }

    public Set<PendingType> getPendingActions() {
        final Set<PendingType> result = getSkippablePendingActions();
        if (pendingTowns > 0) result.add(PendingType.SELECT_TOWN);
        if (pendingFavors > 0) result.add(PendingType.SELECT_FAV);
        if (pendingCultSteps > 0) result.add(PendingType.CULT_STEP);
        return result;
    }

    public Set<PendingType> getSkippablePendingActions() {
        final Set<PendingType> result = new HashSet<>();
        if (pendingSpades > 0) result.add(PendingType.USE_SPADES);
        if (pendingBridges > 0) result.add(PendingType.PLACE_BRIDGE);
        if (pendingWorkerToPriestConversions > 0) result.add(PendingType.CONVERT_W2P);
        if (pendingBuilds != null && !pendingBuilds.isEmpty()) result.add(PendingType.BUILD);
        if (pendingSandstorm) result.add(PendingType.SANDSTORM);
        if (pendingFreeTradingPost) result.add(PendingType.FREE_TP);
        if (pendingFreeDwelling) result.add(PendingType.FREE_D);
        if (ChooseMaxedCultsAction.actionNeeded(game) && pendingTowns == 0 && pendingFavors == 0) result.add(PendingType.CHOOSE_CULTS);
        if (pendingTerrainUnlock > 0) result.add(PendingType.UNLOCK_TERRAIN);
        return result;
    }

    public void clearPendingActions(Set<PendingType> types) {
        for (PendingType type : types) {
            switch (type) {
                case SELECT_TOWN -> pendingTowns = 0;
                case SELECT_FAV -> pendingFavors = 0;
                case USE_SPADES -> pendingSpades = 0;
                case PLACE_BRIDGE -> pendingBridges = 0;
                case CONVERT_W2P -> pendingWorkerToPriestConversions = 0;
                case BUILD -> pendingBuilds = null;
                case SANDSTORM -> pendingSandstorm = false;
                case FREE_TP -> pendingFreeTradingPost = false;
                case FREE_D -> pendingFreeDwelling = false;
                case CHOOSE_CULTS -> {
                    for (int i = 0; i < 4; ++i) maxedCults[i] = false;
                }
                case CULT_STEP -> pendingCultSteps = 0;
                case UNLOCK_TERRAIN -> pendingTerrainUnlock = 0;
            }
        }
    }

    public int getBridgesLeft() {
        return bridgesLeft;
    }

    public void placeBridge(Hex hex1, Hex hex2) {
        --pendingBridges;
        --bridgesLeft;
        game.bridgePlaced(new Bridge(this, hex1, hex2));
    }

    public void getEngineerBridge() {
        workers -= 2;
        ++pendingBridges;
    }

    public boolean hasStronghold() {
        return strongholds > 0;
    }

    @Override
    public String toString() {
        return faction == null ? name : faction.getName();
    }

    public void refreshSize() {
        final Dimension oldDim = pool.getSize();
        final Dimension newDim = pool.getPreferredSize();
        if (oldDim.getHeight() != newDim.getHeight()) {
            game.packNeeded = true;
        }
        if (!oldDim.equals(newDim)) {
            pool.setSize(newDim);
        }
    }

    public void paintColorWheel(Graphics g, int x, int y) {
        final int circleRadius = 8;
        final int wheelRadius = 25;
        final Hex.Type type = faction.getHomeType();
        int ordinal = type.ordinal();
        if (type == Hex.Type.ICE) {
            if (game.getIceColor() == null) return;

            ordinal = game.getIceColor().ordinal();
            g.setColor(Color.BLACK);
            g.fillOval(x + wheelRadius, y + wheelRadius - circleRadius, circleRadius * 2, circleRadius * 2);
            g.setColor(Hex.Type.ICE.getHexColor());
            g.fillOval(x + wheelRadius + 1, y + wheelRadius - circleRadius + 1, circleRadius * 2 - 2, circleRadius * 2 - 2);
        } else if (type == Hex.Type.VOLCANO) {
            if (game.getVolcanoColor() == null) return;

            ordinal = game.getVolcanoColor().ordinal();
            g.setColor(Color.BLACK);
            g.fillOval(x + wheelRadius, y + wheelRadius, circleRadius * 2, circleRadius * 2);
            g.setColor(Hex.Type.VOLCANO.getHexColor());
            g.fillOval(x + wheelRadius + 1, y + wheelRadius + 1, circleRadius * 2 - 2, circleRadius * 2 - 2);
        } else if (type == Hex.Type.VARIABLE) {
            if (game.getVariableColor() == null) return;

            ordinal = game.getVariableColor().ordinal();
        }
        double angle = Math.PI * 1.5;
        for (int i = 0; i < 7; ++i) {
            boolean unlocked = true;
            final Hex.Type circleType = Hex.Type.values()[(ordinal - i + 7) % 7];
            if (faction instanceof Riverwalkers && !unlockedTerrain[circleType.ordinal()]) {
                unlocked = false;
            }
            final int dx = (int) (Math.cos(angle) * wheelRadius + 0.5);
            final int dy = (int) (Math.sin(angle) * wheelRadius + 0.5);
            g.setColor(Color.BLACK);
            if (!unlocked) g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), 50));
            g.fillOval(x + wheelRadius + dx, y + wheelRadius + dy, circleRadius * 2, circleRadius * 2);
            g.setColor(circleType.getHexColor());
            if (!unlocked) g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), 50));
            g.fillOval(x + wheelRadius + dx + 1, y + wheelRadius + dy + 1, circleRadius * 2 - 2, circleRadius * 2 - 2);
            angle += 2 * Math.PI / 7;
        }
    }
}
