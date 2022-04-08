package analyzer.stat;

import analyzer.stat.Stat;

public class StatRow {
    protected String statInfo;
    protected String creationTime;
    protected Stat stat;

    public StatRow(){}

    public StatRow(String statInfo, String creationTime, Stat stat) {
        this.creationTime = creationTime;
        this.statInfo = statInfo;
        this.stat = stat;
    }

    public String getStatInfo() {
        return statInfo;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public Stat getStat() {
        return stat;
    }
}
