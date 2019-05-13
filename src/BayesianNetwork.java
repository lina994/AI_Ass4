import java.util.LinkedList;

public class BayesianNetwork {

    LinkedList<Node> nodes;

    public BayesianNetwork() {
        super();
        nodes = new LinkedList<Node>();
    }

    public static BayesianNetwork fresh_copy(BayesianNetwork original_copy) {
        BayesianNetwork fresh = new BayesianNetwork();
        copyNodes(original_copy, fresh);
        copyConnections(original_copy, fresh);
        return fresh;
    }

    private static void copyNodes(BayesianNetwork original, BayesianNetwork copy) {
        Node oldNode;
        for (int i = 0; i < original.nodes.size(); i++) {
            oldNode = original.nodes.get(i);
            if (oldNode.type == 1) {
                copy.nodes.add(new F_node((F_node) oldNode));
            } else if (oldNode.type == 2) {
                copy.nodes.add(new B_node((B_node) oldNode));
            } else if (oldNode.type == 3) {
                copy.nodes.add(new E_node((E_node) oldNode));
            }
        }
    }

    private static void copyConnections(BayesianNetwork original_copy, BayesianNetwork fresh) {
        Node oldNode;
        Node freshNode;
        int index;
        LinkedList<Node> originalParentsList;
        LinkedList<Node> originalChildrenList;
        for (int i = 0; i < original_copy.nodes.size(); i++) {
            freshNode = fresh.nodes.get(i);
            oldNode = original_copy.nodes.get(i);
            freshNode.probabilityTable = matrix_copy(oldNode.probabilityTable);

            originalParentsList = original_copy.nodes.get(i).parents;
            for (int j = 0; j < originalParentsList.size(); j++) {
                index = originalParentsList.get(j).indexInNetwork;
                freshNode.parents.add(fresh.nodes.get(index));
            }

            originalChildrenList = original_copy.nodes.get(i).children;
            for (int j = 0; j < originalChildrenList.size(); j++) {
                index = originalChildrenList.get(j).indexInNetwork;
                freshNode.children.add(fresh.nodes.get(index));
            }

        }
    }

    public static double[][] matrix_copy(double[][] original) {
        double fresh[][] = new double[original.length][0];
        for (int i = 0; i < original.length; i++) {
            fresh[i] = original[i].clone();
        }
        return fresh;
    }

    // public void printProbabilityTable() {
    // for (Node n : nodes) {
    // n.printProbabilityTable();
    // }
    // }

}
