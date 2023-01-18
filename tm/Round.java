package tm;

public class Round {
    public final int fire;
    public final int water;
    public final int earth;
    public final int air;
    public final int priests;
    public final Resources income;
    public final int d;
    public final int tp;
    public final int shsa;
    public final int te;
    public final int spade;
    public final int town;

    public static final Round fireW = new Round(2, 0, 0, 0, 0, Resources.w1, 0, 0, 5, 0, 0, 0);
    public static final Round firePw = new Round(4, 0, 0, 0, 0, Resources.pw4, 2, 0, 0, 0, 0, 0);
    public static final Round waterP = new Round(0, 4, 0, 0, 0, Resources.p1, 2, 0, 0, 0, 0, 0);
    public static final Round waterS = new Round(0, 4, 0, 0, 0, Resources.spade, 0, 3, 0, 0, 0, 0);
    public static final Round earthC = new Round(0, 0, 1, 0, 0, Resources.c1, 0, 0, 0, 0, 2, 0);
    public static final Round earthS = new Round(0, 0, 4, 0, 0, Resources.spade, 0, 0, 0, 0, 0, 5);
    public static final Round airW = new Round(0, 0, 0, 2, 0, Resources.w1, 0, 0, 5, 0, 0, 0);
    public static final Round airS = new Round(0, 0, 0, 4, 0, Resources.spade, 0, 3, 0, 0, 0, 0);
    public static final Round priestC = new Round(0, 0, 0, 0, 1, Resources.c2, 0, 0, 0, 4, 0, 0);

    private Round(int fire, int water, int earth, int air, int priests, Resources income, int d, int tp, int shsa, int te, int spade, int town) {
        this.fire = fire;
        this.water = water;
        this.earth = earth;
        this.air = air;
        this.priests = priests;
        this.income = income;
        this.d = d;
        this.tp = tp;
        this.shsa = shsa;
        this.te = te;
        this.spade = spade;
        this.town = town;
    }
}
