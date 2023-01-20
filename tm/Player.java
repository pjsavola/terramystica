package tm;

import tm.action.ConvertAction;
import tm.faction.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Player extends JPanel {

    private final Game game;

    private int coins;
    private int workers;
    private int priests;
    private int maxPriests = 7;
    private final int[] power = new int[3];
    private int keys;
    private int points = 20;
    private Resources favorIncome = Resources.zero;
    private final int[] cultSteps = new int[4];
    private static final int[] cumulativeCultPower = { 0, 0, 0, 1, 1, 3, 3, 5, 5, 5, 8 };
    private static final int[] actionCost = { 3, 3, 4, 4, 4, 6 };
    private Faction faction;
    private int bridgesLeft = 3;

    private final boolean[] ownedFavors = new boolean[12];
    private boolean passed;
    private Round round;
    private int pendingSpades;
    private int pendingFavors;
    private int pendingWorkerToPriestConversions;
    private int pendingBridges;

    private int dwellings;
    private int tradingPosts;
    private int temples;
    private int strongholds;
    private int sanctuaries;
    private int shipping;
    private int digging = 3;
    private int range = 1;
    private Resources jumpCost = Resources.zero;

    private final List<Integer> bons = new ArrayList<>();
    private final List<Integer> favs = new ArrayList<>();
    private final List<Integer> towns = new ArrayList<>();
    private final PlayerInfo data;
    private final Pool pool;

    public Player(Game game) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.game = game;
        data = new PlayerInfo();
        pool = new Pool(game, bons, null, favs, towns);
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
        dwellings = 0;
        tradingPosts = 0;
        temples = 0;
        strongholds = 0;
        sanctuaries = 0;
        shipping = 0;
        range = 1;
        jumpCost = Resources.zero;
        bons.clear();
        favs.clear();
        towns.clear();
        selectFaction(faction, 20);
    }

    public int getCultSteps(int cult) {
        return cultSteps[cult];
    }

    public Faction getFaction() {
        return faction;
    }

    public Hex.Type getHomeType() {
        return faction.getHomeType();
    }

    public void selectFaction(Faction faction, int points) {
        this.faction = faction;
        this.points = points;
        power[0] = 12;
        power[1] = 0;
        power[2] = 0;
        if (faction instanceof Dwarves) {
            range = 2;
            jumpCost = Resources.w2;
        } else if (faction instanceof Fakirs) {
            range = 2;
            jumpCost = Resources.p1;
        } else if (faction instanceof Mermaids) {
            shipping = 1;
        }
        addIncome(faction.getInitialIncome());
        final int[] initialCultSteps = faction.getInitialCultSteps();
        System.arraycopy(initialCultSteps, 0, cultSteps, 0, cultSteps.length);
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
    }

    public void placeInitialDwelling() {
        ++dwellings;
    }

    public boolean canBuildDwelling() {
        return dwellings < 8 && canAfford(faction.getDwellingCost());
    }

    public void buildDwelling() {
        if (!canBuildDwelling())
            throw new RuntimeException("Unable to build more dwellings");

        ++dwellings;
        points += round.d;
        if (ownedFavors[10]) {
            points += 2;
        }
        pay(faction.getDwellingCost());
    }

    public boolean canBuildTradingPost(boolean expensive) {
        return tradingPosts < 4 && dwellings > 0 && canAfford(expensive ? faction.getExpensiveTradingPostCost() : faction.getTradingPostCost());
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
        pay(expensive ? faction.getExpensiveTradingPostCost() : faction.getTradingPostCost());
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
        ++pendingFavors;
        if (faction instanceof ChaosMagicians) {
            ++pendingFavors;
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
            pendingWorkerToPriestConversions = Math.min(workers, 3);
        } else if (faction instanceof Dwarves) {
            jumpCost = Resources.w1;
        } else if (faction instanceof Fakirs) {
            ++range;
        } else if (faction instanceof Halflings) {
            pendingSpades += 3;
        } else if (faction instanceof Mermaids) {
            if (canAdvanceShipping()) {
                advanceShipping();
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
        ++pendingFavors;
        if (faction instanceof ChaosMagicians) {
            ++pendingFavors;
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
                if (canAdvanceShipping()) {
                    advanceShipping();
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
        if (game.usedPowerActions[act - 1]) return false;

        if (act == 1 && bridgesLeft == 0) return false;

        return canAffordPower(actionCost[act - 1]);
    }

    public void usePowerAction(int act) {
        if (!canUseAction(act))
            throw new RuntimeException("Unable to afford action " + act);

        payPower(actionCost[act - 1]);
        switch (act) {
            case 1 -> {
                ++pendingBridges;
                --bridgesLeft;
            }
            case 2 -> addIncome(Resources.p1);
            case 3 -> addIncome(Resources.w2);
            case 4 -> addIncome(Resources.c7);
            case 5 -> ++pendingSpades;
            case 6 -> pendingSpades += 2;
        }
        game.usedPowerActions[act - 1] = true;
    }

    public boolean canAdvanceShipping() {
        return faction.getMaxShipping() > shipping;
    }

    public void advanceShipping() {
        if (!canAdvanceDigging())
            throw new RuntimeException("Trying to advance shipping too much");

        ++shipping;
        points += faction.getAdvanceShippingPoints(shipping);
    }

    public boolean canAdvanceDigging() {
        return faction.getMinDigging() < digging;
    }

    public void advanceDigging() {
        if (!canAdvanceDigging())
            throw new RuntimeException("Trying to advance digging too much");

        --digging;
        points += 6;
    }

    public boolean canLeech() {
        return power[0] > 0 || power[1] > 0;
    }

    public void leech(int amount) {
        final int unused = addPower(amount);
        if (unused < amount) {
            points -= amount - unused - 1;
        }
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

    private void addCultSteps(int[] cults) {
        for (int i = 0; i < 4; ++i) {
            cultSteps[i] = addPowerFromCultSteps(cultSteps[i], cults[i], game.cultOccupied(i));
        }
    }

    public void sendPriestToCult(int cult, int amount) {
        cultSteps[cult] = addPowerFromCultSteps(cultSteps[cult], amount, game.cultOccupied(cult));
        if (amount > 1) {
            --maxPriests;
        }
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

    private void addIncome(Resources income) {
        coins += income.coins;
        workers += income.workers;
        priests = Math.min(priests + income.priests, maxPriests);
        addPower(income.power);
        if (income == Resources.spade) {
            ++pendingSpades;
        }
    }

    public boolean canConvert(int priests, int workers, int points) {
        final int priestsToCoins = Math.min(priests, workers);
        return this.priests >= priests && this.workers >= workers - priestsToCoins && (points == 0 || (faction instanceof Alchemists && this.points >= points));
    }

    public void convert(int priests, int workers, int points) {
        this.priests -= priests;
        this.workers -= workers;
        this.workers += priests;
        this.coins += workers;
        if (faction instanceof Alchemists) {
            this.points -= points;
            this.coins += points;
        }
    }

    public void dig(int amount) {
        if (workers < amount * digging)
            throw new RuntimeException("Cannot afford to dig " + amount);

        if (amount % 2 != 0 && faction instanceof Giants)
            throw new RuntimeException("Giants can only dig even amounts");

        pendingSpades += amount;
        workers -= amount * digging;
    }

    public void useSpades(int amount) {
        if (pendingSpades < amount)
            throw new RuntimeException("Trying to use too many spades");

        pendingSpades -= amount;
        points += amount * round.spade;
        if (faction instanceof Halflings) {
            points += amount;
        } else if (faction instanceof Alchemists && strongholds > 0) {
            addPower(2 * amount);
        }
    }

    public void useRange() {
        if (!canAfford(jumpCost))
            throw new RuntimeException("Unable to afford range usage");

        pay(jumpCost);
        points += 4;
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

    private void addRoundIncome(Round round) {
        addIncome(favorIncome);
        addIncome(faction.getBaseIncome());
        for (int i = 0; i < dwellings; ++i) {
            addIncome(faction.getDwellingIncome(i));
        }
        for (int i = 0; i < tradingPosts; ++i) {
            addIncome(faction.getTradingPostIncome(i));
        }
        for (int i = 0; i < temples; ++i) {
            addIncome(faction.getTempleIncome(i));
        }
        if (strongholds > 0) {
            addIncome(faction.getStrongholdIncome());
        }
        if (sanctuaries > 0) {
            addIncome(faction.getSanctuaryIncome());
        }
        addIncome(Bons.getBonIncome(bons.get(0)));
        addIncomeFromCults(cultSteps[0], round.fire, round.income);
        addIncomeFromCults(cultSteps[1], round.water, round.income);
        addIncomeFromCults(cultSteps[2], round.earth, round.income);
        addIncomeFromCults(cultSteps[3], round.air, round.income);
        addIncomeFromCults(7 - maxPriests, round.priests, round.income);
    }

    public void startRound(Round round) {
        this.round = round;
        addRoundIncome(round);
        passed = false;
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
        passed = true;
    }

    public int pickBon(int newBon, int coins) {
        if (bons.isEmpty()) {
            bons.add(0);
            pool.setSize(pool.getPreferredSize());
        }
        final int oldBon = bons.set(0, newBon);
        this.coins += coins;
        return oldBon;
    }

    class PlayerInfo extends JPanel {
        @Override
        public void paint(Graphics g) {
            int dx = 5;
            int dy = 5;
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            Color factionColor = faction.getHomeType().getBuildingColor();
            final boolean myTurn = game.isMyTurn(Player.this);
            final boolean passed = Player.this.passed && game.phase != Game.Phase.END;
            if (passed && !myTurn) {
                factionColor = new Color(factionColor.getRed(), factionColor.getGreen(), factionColor.getBlue(), 50);
            }
            g.setColor(factionColor);
            g.fillRect(dx, dy, 300, 16);
            g.setColor(faction.getHomeType().getFontColor());
            String factionName = faction.getName();
            if (myTurn) {
                if (game.phase == Game.Phase.CONFIRM_ACTION) {
                    factionName += " - CONFIRM TURN";
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
                PowerActions.drawPowerAction(g2d, 250, 24, faction.getPowerAction(strongholds > 0), false);
                g.setColor(oldColor);
                g2d.setStroke(oldStroke);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, 128);
        }
    }
}
