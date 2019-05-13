public class FloodingEvidence extends Evidence {

    int vertex;

    public FloodingEvidence(boolean isTrue, int v) {
        super();
        this.type = 1;
        this.isTrue = isTrue;
        this.vertex = v;
    }

}
