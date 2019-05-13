public class E_node extends Node {

    int vertex;

    public E_node(int indexInNetwork, int v) {
        super(indexInNetwork);
        this.vertex = v;
        this.type = 3;
    }
    
    // copy constructor
    public E_node(E_node eNode) {
        super(eNode.indexInNetwork);
        this.vertex = eNode.vertex;
        this.type = 3;
    }

}
