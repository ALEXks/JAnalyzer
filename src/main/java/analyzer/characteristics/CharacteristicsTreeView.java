package analyzer.characteristics;

import analyzer.stat.Interval;
import analyzer.utils.*;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CharacteristicsTreeView extends TreeView {
    public CharacteristicsTreeView() {
        super();
        setShowRoot(false);
        setRoot(new CharacteristicsTreeItem<>("root", "root"));
    }

    public final static double significantLostTimeCoef = 0.05;

    public void initAnalysisCPU(Interval inter){
        if (inter == null) {
            System.out.println("[WARN] null analyzer.interval in initAnalysisCPU");
            return;
        }

        Color effColor = Color.GREEN;
        if (inter.info.times.efficiency <= 0.5)
            effColor = Color.RED;
        else if (inter.info.times.efficiency <= 0.8)
            effColor = Color.ORANGE;

        Color lostColor = Color.RED;
        if (inter.info.times.lost_time <= 0.2 * inter.info.times.sys_time)
            lostColor = Color.GREEN;
        else if (inter.info.times.lost_time <= 0.5 * inter.info.times.sys_time)
            lostColor = Color.ORANGE;

        CharacteristicPane<?>
                lostTime = new CharacteristicPane<>("Потерянное время", inter.info.times.lost_time, lostColor),
                insufParallelism = new CharacteristicPane<>("Неэффективный параллелизм", inter.info.times.insuf),
                insufParallelismSys = new CharacteristicPane<>("Системный", inter.info.times.insuf_sys),
                insufParallelismUser = new CharacteristicPane<>("Пользовательский", inter.info.times.insuf_user),
                comm = new CharacteristicPane<>("Коммуникации", inter.info.times.comm),
                idleTime = new CharacteristicPane<>("Простои", inter.info.times.idle);

        List<LabeledProgressBar> labelesPBs = Arrays.asList(
                /* 0 */ new LabeledProgressBar(lostTime, inter.info.times.lost_time, inter.info.times.sys_time),
                /* 1 */ new LabeledProgressBar(comm, inter.info.times.comm, inter.info.times.lost_time),
                /* 2 */ new LabeledProgressBar(insufParallelism, inter.info.times.insuf, inter.info.times.lost_time),
                /* 3 */ new LabeledProgressBar(insufParallelismSys, inter.info.times.insuf_sys, inter.info.times.insuf),
                /* 4 */ new LabeledProgressBar(insufParallelismUser, inter.info.times.insuf_user, inter.info.times.insuf),
                /* 5 */ new LabeledProgressBar(idleTime, inter.info.times.idle, inter.info.times.lost_time)
        );

        List<TreeItem> treeItems = labelesPBs.stream().map(TreeItem::new).collect(Collectors.toList());

        treeItems.get(2).getChildren().addAll(treeItems.get(3), treeItems.get(4));
        treeItems.get(0).getChildren().addAll(treeItems.get(2), treeItems.get(1), treeItems.get(5));

        getRoot().getChildren().clear();
        getRoot().getChildren().addAll(
                new CharacteristicsTreeItem<>("Коэффициент эффективности", inter.info.times.efficiency, effColor),
                new CharacteristicsTreeItem<>("Время выполнения", inter.info.times.exec_time),
                new CharacteristicsTreeItem<>("Количество процессоров", inter.info.times.nproc),
                new CharacteristicsTreeItem<>("Общее количество нитей", inter.info.times.threadsOfAllProcs),
                new CharacteristicsTreeItem<>("Общее время", inter.info.times.sys_time),
                new CharacteristicsTreeItem<>("Полезное время", inter.info.times.prod_time),
                treeItems.get(0),
                new CharacteristicsTreeItem<>("Неравномерность загрузки", inter.info.times.load_imb));

        treeItems.get(2).expandedProperty().addListener((observable, oldValue, newValue) -> Utils.addBlink(treeItems.get(2)));
        treeItems.get(0).expandedProperty().addListener((observable, oldValue, newValue) -> Utils.addBlink(treeItems.get(0)));

        treeItems.get(0).setExpanded(inter.info.times.lost_time > significantLostTimeCoef * inter.info.times.sys_time);
    }

    public void initAnalysisGPU(Interval inter){
        if (inter == null) {
            System.out.println("[WARN] null analyzer.interval in initAnalysisGPU");
            return;
        }

        if (inter.info.times.gpu_num <= 0) {
            getRoot().getChildren().clear();
            getRoot().getChildren().add(new CharacteristicsTreeItem<>("GPU не использовались",""));
            return;
        }

        Color effColor = Color.GREEN;
        if (inter.info.times.gpu_efficiency <= 0.5)
            effColor = Color.RED;
        else if (inter.info.times.gpu_efficiency <= 0.8)
            effColor = Color.ORANGE;

        Color lostColor = Color.RED;
        if (inter.info.times.gpu_time_lost <= 0.2 * inter.info.times.gpu_sys_time)
            lostColor = Color.GREEN;
        else if (inter.info.times.gpu_time_lost <= 0.5 * inter.info.times.gpu_sys_time)
            lostColor = Color.ORANGE;

        CharacteristicPane<?>
                lostTime = new CharacteristicPane<>("Потерянное время", inter.info.times.gpu_time_lost, lostColor);
//                insufParallelism = new CharacteristicPane<>("Неэффективный параллелизм", inter.info.times.insuf),
//                insufParallelismSys = new CharacteristicPane<>("Системный", inter.info.times.insuf_sys),
//                insufParallelismUser = new CharacteristicPane<>("Пользовательский", inter.info.times.insuf_user),
//                comm = new CharacteristicPane<>("Коммуникации", inter.info.times.comm),
//                idleTime = new CharacteristicPane<>("Простои", inter.info.times.idle);

        List<LabeledProgressBar> labelesPBs = Arrays.asList(
                /* 0 */ new LabeledProgressBar(lostTime, inter.info.times.gpu_time_lost, inter.info.times.gpu_sys_time)
//                /* 1 */ new LabeledProgressBar(comm, inter.info.times.comm, inter.info.times.lost_time),
//                /* 2 */ new LabeledProgressBar(insufParallelism, inter.info.times.insuf, inter.info.times.lost_time),
//                /* 3 */ new LabeledProgressBar(insufParallelismSys, inter.info.times.insuf_sys, inter.info.times.insuf),
//                /* 4 */ new LabeledProgressBar(insufParallelismUser, inter.info.times.insuf_user, inter.info.times.insuf),
//                /* 5 */ new LabeledProgressBar(idleTime, inter.info.times.idle, inter.info.times.lost_time)
        );

        List<TreeItem> treeItems = labelesPBs.stream().map(TreeItem::new).collect(Collectors.toList());

//        treeItems.get(2).getChildren().addAll(treeItems.get(3), treeItems.get(4));
//        treeItems.get(0).getChildren().addAll(treeItems.get(2), treeItems.get(1), treeItems.get(5));

        getRoot().getChildren().clear();
        getRoot().getChildren().addAll(
                new CharacteristicsTreeItem<>("Коэффициент эффективности", inter.info.times.gpu_efficiency, effColor),
                new CharacteristicsTreeItem<>("Количество GPU", inter.info.times.gpu_num),
                new CharacteristicsTreeItem<>("Общее время", inter.info.times.gpu_sys_time),
                new CharacteristicsTreeItem<>("Полезное время", inter.info.times.gpu_time_prod),
                treeItems.get(0)
        );

//        treeItems.get(2).expandedProperty().addListener((observable, oldValue, newValue) -> Utils.addBlink(treeItems.get(2)));
//        treeItems.get(0).expandedProperty().addListener((observable, oldValue, newValue) -> Utils.addBlink(treeItems.get(0)));

        treeItems.get(0).setExpanded(inter.info.times.lost_time > significantLostTimeCoef * inter.info.times.sys_time);
    }

}

