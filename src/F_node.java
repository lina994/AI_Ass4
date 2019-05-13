public class F_node extends Node {

    int vertex;

    public F_node(int indexInNetwork, int v) {
        super(indexInNetwork);
        this.vertex = v;
        this.type = 1;
    }

    // copy constructor
    public F_node(F_node fNode) {
        super(fNode.indexInNetwork);
        this.vertex = fNode.vertex;
        this.type = 1;
    }

}
