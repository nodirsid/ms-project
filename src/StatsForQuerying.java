public class StatsForQuerying {
    private long qryExecTime; //in nanoseconds
    private int noOfNodesScanned;
    private int noOfArcsScanned;
    private int noOfDistImprovements; //number of effective relax operations

    //getters
    public long getQryExecTime() {
        return qryExecTime;
    }

    public int getNoOfNodesScanned() {
        return noOfNodesScanned;
    }

    public int getNoOfArcsScanned() {
        return noOfArcsScanned;
    }

    public int getNoOfDistImprovements() {
        return noOfDistImprovements;
    }

    //setters
    public void setQryExecTime(long qryExecTime) {
        this.qryExecTime = qryExecTime;
    }

    public void setNoOfNodesScanned(int noOfNodesScanned) {
        this.noOfNodesScanned = noOfNodesScanned;
    }

    public void setNoOfArcsScanned(int noOfArcsScanned) {
        this.noOfArcsScanned = noOfArcsScanned;
    }

    public void setNoOfDistImprovements(int noOfDistImprovements) {
        this.noOfDistImprovements = noOfDistImprovements;
    }
}
