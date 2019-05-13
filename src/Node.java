import java.util.LinkedList;

public abstract class Node {
    int indexInNetwork;
    double[][] probabilityTable;
    LinkedList<Node> parents;
    LinkedList<Node> children;
    int type; // 1.F 2.B 3.E

    public Node(int indexInNetwork) {
        super();
        this.indexInNetwork = indexInNetwork;
        this.parents = new LinkedList<Node>();
        this.children = new LinkedList<Node>();
    }

}
