public class BlockageEvidence extends Evidence {

    String edgeName;

    public BlockageEvidence(boolean isTrue, String e) {
        super();
        this.type = 2;
        this.isTrue = isTrue;
        this.edgeName = e;
    }

}
