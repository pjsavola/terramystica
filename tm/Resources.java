package tm;

public class Resources {
    public final int coins;
    public final int workers;
    public final int priests;
    public final int power;

    public Resources(int coins, int workers, int priests, int power) {
        this.coins = coins;
        this.workers = workers;
        this.priests = priests;
        this.power = power;
    }

    public static final Resources zero = new Resources(0, 0, 0, 0);
    public static final Resources spade = new Resources(0, 0, 0, 0);
    public static final Resources w1 = fromWorkers(1);
    public static final Resources w2 = fromWorkers(2);
    public static final Resources w3 = fromWorkers(3);
    public static final Resources w4 = fromWorkers(4);
    public static final Resources c1 = fromCoins(1);
    public static final Resources c2 = fromCoins(2);
    public static final Resources c3 = fromCoins(3);
    public static final Resources c4 = fromCoins(4);
    public static final Resources c5 = fromCoins(5);
    public static final Resources c6 = fromCoins(6);
    public static final Resources c7 = fromCoins(7);
    public static final Resources p1 = fromPriests(1);
    public static final Resources p2 = fromPriests(2);
    public static final Resources pw1 = fromPower(1);
    public static final Resources pw2 = fromPower(2);
    public static final Resources pw3 = fromPower(3);
    public static final Resources pw4 = fromPower(4);
    public static final Resources pw5 = fromPower(5);
    public static final Resources pw6 = fromPower(6);
    public static final Resources pw8 = fromPower(8);
    public static final Resources pw12 = fromPower(12);
    public static final Resources c2pw1 = c2.combine(pw1);
    public static final Resources c3pw1 = c3.combine(pw1);
    public static final Resources c4pw1 = c4.combine(pw1);
    public static final Resources c2pw2 = c2.combine(pw2);
    public static final Resources c3pw2 = c3.combine(pw2);

    public static final Resources c1w1 = c1.combine(w1);
    public static final Resources c2w1 = c2.combine(w1);
    public static final Resources c4w1 = c4.combine(w1);
    public static final Resources c3w2 = c3.combine(w2);
    public static final Resources c5w2 = c5.combine(w2);
    public static final Resources c6w2 = c6.combine(w2);
    public static final Resources c4w3 = c4.combine(w3);
    public static final Resources c6w3 = c6.combine(w3);
    public static final Resources c8w3 = Resources.fromCoins(8).combine(w3);
    public static final Resources c4w4 = c4.combine(w4);
    public static final Resources c6w4 = c6.combine(w4);
    public static final Resources c8w4 = Resources.fromCoins(8).combine(w4);
    public static final Resources c10w4 = Resources.fromCoins(10).combine(w4);
    public static final Resources c8w5 = Resources.fromCoins(8).combine(Resources.fromWorkers(5));
    public static final Resources c4p1 = c4.combine(p1);
    public static final Resources c5w2p1 = c5.combine(w2).combine(p1);
    public static final Resources c1w2p1 = c1.combine(w2).combine(p1);
    public static final Resources w1pw3 = w1.combine(pw3);
    public static final Resources w2pw1 = w2.combine(pw1);

    public static Resources fromCoins(int coins) {
        return new Resources(coins, 0, 0, 0);
    }

    public static Resources fromWorkers(int workers) {
        return new Resources(0, workers, 0, 0);    }

    public static Resources fromPriests(int priests) {
        return new Resources(0, 0, priests, 0);    }

    public static Resources fromPower(int power) {
        return new Resources(0, 0, 0, power);
    }

    public Resources combine(Resources income) {
        return new Resources(coins + income.coins, workers + income.workers, priests + income.priests, power + income.power);
    }
}
