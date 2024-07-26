package tm;

public class ReplayFailure extends Exception {
    private final int lastActionBeforeFailure;

    public ReplayFailure(Exception e, int lastActionBeforeFailure) {
        super(e);
        this.lastActionBeforeFailure = lastActionBeforeFailure;
    }

    public int getLastActionBeforeFailure() {
        return lastActionBeforeFailure;
    }
}
