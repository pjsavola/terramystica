package tm;

import tm.action.Action;

import java.util.ArrayList;
import java.util.List;

public class DecisionNode {
    private final Action action;
    private int score; // Score after executing the action
    private boolean valid;
    private final List<DecisionNode> children = new ArrayList<>();

    public DecisionNode(Action action) {
        this.action = action;
    }

    public void addChild(DecisionNode node) {
        children.add(node);
    }

    public void setScore(int score) {
        this.score = score;
        valid = true;
    }

    public void getBestActions(List<Action> stack, int[] bestScore, List<List<Action>> results) {
        if (valid) {
            if (score > bestScore[0]) {
                bestScore[0] = score;
                results.clear();
            }
            if (score == bestScore[0]) {
                results.add(new ArrayList<>(stack));
            }
        }
        for (DecisionNode child : children) {
            stack.add(child.action);
            child.getBestActions(stack, bestScore, results);
            stack.remove(stack.size() - 1);
        }
    }
}
