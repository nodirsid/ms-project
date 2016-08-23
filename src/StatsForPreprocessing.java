public class StatsForPreprocessing {
    private long prepExecTime; //in nanoseconds
    private int noOfNodesScanned;
    private boolean readFromFile;

    public long getPrepExecTime() {
        return prepExecTime;
    }

    public int getNoOfNodesScanned() {
        return noOfNodesScanned;
    }

    public boolean isReadFromFile() {
        return readFromFile;
    }

    public void setPrepExecTime(long prepExecTime) {
        this.prepExecTime = prepExecTime;
    }

    public void setNoOfNodesScanned(int noOfNodesScanned) {
        this.noOfNodesScanned = noOfNodesScanned;
    }

    public void setReadFromFile(boolean readFromFile) {
        this.readFromFile = readFromFile;
    }
}
