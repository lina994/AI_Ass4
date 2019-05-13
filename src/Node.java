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

//    public void printProbabilityTable() {
//        System.out.println("index is " + indexInNetwork);
//        System.out.println("type: " + type);
//        if (probabilityTable != null) {
//            for (int k = 0; k < probabilityTable.length; k++) {
//                for (int j = 0; j < probabilityTable[0].length; j++) {
//                    System.out.printf("%.4f", probabilityTable[k][j]);
//                    System.out.printf(" ");
//                }
//                System.out.println();
//            }
//        }
//        if (children != null)
//            for (int j = 0; j < children.size(); j++)
//                System.out.println("child: " + children.get(j).indexInNetwork);
//        if (parents != null)
//            for (int j = 0; j < parents.size(); j++)
//                System.out.println("parent: " + parents.get(j).indexInNetwork);
//    }
    
}
