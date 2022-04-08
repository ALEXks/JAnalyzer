package analyzer;

import analyzer.stat.Interval;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class IntervalPane extends AnchorPane {
    private Interval interval;

    public IntervalPane(Object load) {
        super((Node) load);
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }
}
