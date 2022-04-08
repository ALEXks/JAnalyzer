package analyzer;

import analyzer.stat.Interval;
import javafx.scene.control.CheckMenuItem;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Filter extends CheckMenuItem {

    public static Predicate<Interval> truePredicate = t -> true;
    private Predicate<Interval> curFilter = truePredicate;
    private final Controller controller;


    public Filter(Controller controller, String name, Supplier<Predicate<Interval>> supplier) {
        super();
        setText(name);
        this.controller = controller;

        setOnAction(e -> {
            curFilter = (isSelected()) ? supplier.get() : truePredicate;
            Interval rootInterval = controller.getStatTreeRootInterval();
            execAllFilters(rootInterval, controller.getFilters());
            controller.initStatTree(rootInterval, false);
        });
    }

    public Predicate<Interval> getCurFilter(){
        return curFilter;
    }

    public static void execAllFilters(Interval interval, List<Filter> filters) {
        filters.stream().map(Filter::getCurFilter).reduce(Predicate::and)
            .ifPresent(intervalPredicate -> filter(interval, intervalPredicate));
    }

    public static void resetAllFilters(Interval interval) {
        interval.setVisible(true);
        for (Interval inter : interval.intervals)
            resetAllFilters(inter);
    }

    private static void filter(Interval interval, Predicate<Interval> pred){
        interval.setVisible(pred.test(interval));
        for (Interval inter : interval.intervals) {
            filter(inter, pred);
        }
    }

}
