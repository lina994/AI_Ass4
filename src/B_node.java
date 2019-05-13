public class B_node extends Node {

    int vertex1;
    int vertex2;
    double weight;
    String name;

    public B_node(int indexInNetwork, int v1, int v2, double w, String name) {
        super(indexInNetwork);
        this.vertex1 = v1;
        this.vertex2 = v2;
        this.weight = w;
        this.type = 2;
        this.name = name;
    }

    // copy constructor
    public B_node(B_node bNode) {
        super(bNode.indexInNetwork);
        this.vertex1 = bNode.vertex1;
        this.vertex2 = bNode.vertex2;
        this.weight = bNode.weight;
        this.type = 2;
        this.name = bNode.name;
    }

}
