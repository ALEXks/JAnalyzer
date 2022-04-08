package analyzer;

import analyzer.stat.Interval;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.util.List;

public class IntervalComparePane extends AnchorPane {
    private List<Interval> intervals;
    private List<String> pHeadings;

    public IntervalComparePane(Object load) {
        super((Node) load);
    }

    public List<Interval> getIntervals() {
        return intervals;
    }
    public void setIntervals(List<Interval> intervals) {
        this.intervals = intervals;
    }

    public List<String> getPHeadings() {return pHeadings;}
    public void setPHeadings(List<String> pHeadings) {this.pHeadings = pHeadings;}
}
