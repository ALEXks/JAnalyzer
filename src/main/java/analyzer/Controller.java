package analyzer;

import analyzer.characteristics.CharacteristicsTreeView;
import analyzer.charts.GPUStatChartWrapperPane;
import analyzer.charts.ProcStatChart;
import analyzer.stat.Interval;
import analyzer.stat.Stat;
import analyzer.stat.StatRow;
import analyzer.utils.Utils;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
<<<<<<< HEAD:src/main/java/Controller.java
import javafx.util.Duration;
import json.GPUTimesJson;
=======
import analyzer.json.GPUTimesJson;
>>>>>>> a88ec91c11bc50ad579e45f3956eb1e1d7844c20:src/main/java/analyzer/Controller.java
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.PopOver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.System.exit;
import static java.lang.System.in;

public class Controller {

    //-----  STATIC SECTION  -----//
    public static ArrayList<Stat> compareList = new ArrayList<>();

    @FXML private TabPane tabPane;
    @FXML private Tab showStatTab;
    @FXML private TextArea statIntervalText;
    @FXML private Label statLabel;
    @FXML private TreeView<IntervalPane> statTreeView;
    @FXML private TreeView<IntervalComparePane> statCompareTreeView;
    @FXML private SplitPane compareSplitPane;
    @FXML private SplitPane statSplitPane;
    @FXML private TableView<StatRow> statTableView;
    @FXML private StackedBarChart statCompareChart;
    @FXML private LineChart<String, Double> statCompareGPUChart;
    @FXML private MenuButton sortMenu;
    @FXML private ChoiceBox<String> compareTypeChoiceBox;
    @FXML private ToggleButton showCompareTreeButton;
    @FXML private Button resetStatButton;
    @FXML private Button resetCompareStatButton;
    @FXML private VBox GPUVBox;
    @FXML private SplitPane GPUSplitPane;
    @FXML private ScrollPane GPUScrollPane;
    @FXML private ToggleButton procAnalysisButton;

    @FXML private ScrollPane mainCharsScrollPane;
    @FXML private VBox mainCharsVBox;
    @FXML private VBox mainCharsGPUVBox;
    @FXML private CharacteristicsTreeView statAnalysisView;
    @FXML private CharacteristicsTreeView statAnalysisGPUView;
    @FXML private Label characteristicLabel;

    @FXML private AnchorPane statChartAnchorPane;
    private ProcStatChart procStatChart;
    private GPUStatChartWrapperPane gpuStatChartWrapperPane;
    ChooseProcButton chooseProcButton;
    Interval fullRootInterval;

    private FileChooser fileChooser;
    private Window primaryStage;

    //----  analyzer.Filter menu  -----//
    @FXML private Menu significantMenu;
    private final HashMap<String, Supplier<Predicate<Interval>>> significantFilterNameToPred =
            new HashMap<String, Supplier<Predicate<Interval>>>() {{
        put("Время выполнения", () -> {
            Interval rootInterval = getStatTreeRootInterval();
            if (rootInterval == null)
                return Filter.truePredicate;
            return interval -> interval.info.times.exec_time >= 0.05 * rootInterval.info.times.exec_time;
        });
        put("Потерянное время", () -> {
            Interval rootInterval = getStatTreeRootInterval();
            if (rootInterval == null)
                return Filter.truePredicate;
            return interval -> interval.info.times.lost_time >= 0.05 * rootInterval.info.times.lost_time;
        });
    }};
    private final int numSignificantFilters = significantFilterNameToPred.size();

    private ArrayList<Filter> significantFilters;

    public List<Filter> getFilters(){
        // TODO: if any new filters add here
        return significantFilters;
    }

    enum CompareType {
        lostTime,
        GPU
    }

    enum LostTimeType {
        insufSys,
        insufUser,
        idle,
        comm
    }

    Pattern pData = Pattern.compile("data.");
    Pattern pDataPart = Pattern.compile("default-color.");

    private double lostTime = 0;
    private double lostTimeGPU = 0;
    private double lostCompareTime = 0;
//    private double GPUCompareTime = 0;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public CompareType curCompareType = CompareType.lostTime;
    public Integer curProc = -1;

    private static final String
            numProcSort = "Кол-во процессоров",
            lostTimeSort = "Потерянное время",
            execTimeSort = "Время выполнения",
            coefSort = "Коэф. эффективности";

    private static final String
            lostTimeChartType = "Потерянное время",
            GPUChartType = "Сравнение GPU";

    private void selectTab(int tabIndex) {
        tabPane.getSelectionModel().select(tabIndex);
    }

    //-----  INIT SECTION  -----//

    public void initController(Window primaryStage) {
        this.primaryStage = primaryStage;

        try {
            initChooseProcButton();
            initStatCharts();
            initFilters();
            initStatTable();
            resetLoadedStat();
            resetCompareStat();
            initSortMenu();
            initCompareTypeChoiceBox();
            initStatAnalysisViews();
            initFileChooser();
        } catch (Exception e) {
            System.out.println("could not init main controller: " + e.toString());
            exit(1);
        }
    }

    private void initStatAnalysisViews() throws Exception{
        statAnalysisView = new CharacteristicsTreeView();
        try {
            ((VBox) mainCharsVBox.getChildren().get(0)).getChildren().add(statAnalysisView);
        } catch (Exception e) {
            throw new Exception("error initializing statAnalysisView: " + e.toString());
        }

        statAnalysisGPUView = new CharacteristicsTreeView();
        mainCharsGPUVBox.getChildren().add(statAnalysisGPUView);
    }

    public void initChooseProcButton(){
        chooseProcButton = new ChooseProcButton(this::selectProcs, this::resetSelectProcs);

        try {
            ((AnchorPane) showStatTab.getContent()).getChildren().add(chooseProcButton);
        } catch (Exception e) {
            System.out.println("[ERROR] in initChooseProcButton: " + e.toString());
            return;
        }

        AnchorPane.setTopAnchor(chooseProcButton, 8.);
        AnchorPane.setRightAnchor(chooseProcButton, 315.);
    }

    public void initFilters() {
        significantFilters = new ArrayList<>(numSignificantFilters);
        List<String> significantFiltersNames = new ArrayList<>(significantFilterNameToPred.keySet());
        String filterName;

        for (int i = 0; i < numSignificantFilters; ++i) {
            filterName = significantFiltersNames.get(i);
            significantFilters.add(
                    new Filter(this, filterName, significantFilterNameToPred.get(filterName))
            );
        }
        significantMenu.getItems().addAll(significantFilters);
    }

    private void initCompareTypeChoiceBox() {
        compareTypeChoiceBox.getItems().clear();
        compareTypeChoiceBox.getItems().addAll(lostTimeChartType, GPUChartType);
        compareTypeChoiceBox.getSelectionModel().select(0);
        compareTypeChoiceBox.setOnAction(event -> {
            switch (compareTypeChoiceBox.getSelectionModel().getSelectedItem()) {
                case lostTimeChartType:
                    switchCompareType(CompareType.lostTime);
                    break;
                case GPUChartType:
                    switchCompareType(CompareType.GPU);
            }
        });
    }

    private void initFileChooser() {
        fileChooser = new FileChooser();

        fileChooser.setTitle("Выберите статистику(и) для загрузки");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    private void initStatCharts() {
        procStatChart = new ProcStatChart(this::resetProc, this::selectProc);
        statChartAnchorPane.getChildren().add(procStatChart);
        procStatChart.setVisible(false);

        gpuStatChartWrapperPane = new GPUStatChartWrapperPane();
        statChartAnchorPane.getChildren().add(gpuStatChartWrapperPane);
        gpuStatChartWrapperPane.setVisible(false);
    }

    private void initSortMenu() {
        sortMenu.getItems().clear();
        MenuItem numProc = new MenuItem(numProcSort);
        MenuItem lostTime = new MenuItem(lostTimeSort);
        MenuItem execTime = new MenuItem(execTimeSort);
        MenuItem coef = new MenuItem(coefSort);
        sortMenu.getItems().addAll(numProc, lostTime, execTime, coef);
        numProc.setOnAction(event -> compareSort(numProcSort));
        lostTime.setOnAction(event -> compareSort(lostTimeSort));
        execTime.setOnAction(event -> compareSort(execTimeSort));
        coef.setOnAction(event -> compareSort(coefSort));
    }

    private void initStatTable() {
        statTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (Object o : statTableView.getColumns()) {
            TableColumn<StatRow, String> tc = (TableColumn<StatRow, String>) o;
            switch (tc.getText()) {
                case "Статистика выполнения":
                    tc.setCellValueFactory(new PropertyValueFactory<>("statInfo"));
                    break;
                case "Дата загрузки":
                    tc.setCellValueFactory(new PropertyValueFactory<>("creationTime"));
                    break;
            }
        }
        LoadStatList();
    }

    //-----  INIT SECTION END  -----//

    private void switchCompareType(CompareType cType) {
        if (curCompareType == cType)
            return;
        switch (cType) {
            case lostTime:
                curCompareType = CompareType.lostTime;
                statCompareChart.setVisible(true);
                statCompareGPUChart.setVisible(false);
                break;
            case GPU:
                curCompareType = CompareType.GPU;
                statCompareChart.setVisible(false);
                statCompareGPUChart.setVisible(true);
                break;
        }
        TreeItem<IntervalComparePane> selectedItem = statCompareTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null)
            initCompareChart(selectedItem.getValue().getIntervals(), selectedItem.getValue().getPHeadings());
    }

    public void compareSort(String typeCompare) {
        if (compareList.size() == 0)
            return;
        List<Interval> selectedIntervals = statCompareTreeView.getSelectionModel().getSelectedItem()
                .getValue().getIntervals();
        compareList = (ArrayList<Stat>) IntStream.range(0, selectedIntervals.size())
                .boxed().sorted((i, j) -> {
                    Interval o1 = selectedIntervals.get(i), o2 = selectedIntervals.get(j);
                    switch (typeCompare) {
                        case numProcSort:
                            return (int) Math.signum(o1.info.times.nproc - o2.info.times.nproc);
                        case lostTimeSort:
                            return (int) Math.signum(o1.info.times.lost_time - o2.info.times.lost_time);
                        case execTimeSort:
                            return (int) Math.signum(o1.info.times.exec_time - o2.info.times.exec_time);
                        case coefSort:
                            return (int) Math.signum(o1.info.times.efficiency - o2.info.times.efficiency);
                    }
                    return 0;
                })
                .map(compareList::get).collect(Collectors.toList());
        updateCompareRoot(statCompareTreeView.getRoot(),
                compareList.stream().map(elt -> elt.interval).collect(Collectors.toList()),
                compareList.stream().map(elt -> elt.info.p_heading).collect(Collectors.toList()));
        IntervalComparePane selectedItem = statCompareTreeView.getSelectionModel().getSelectedItem().getValue();
        initCompareChart(selectedItem.getIntervals(), selectedItem.getPHeadings());
    }



    //------  Recursive function to get the root for statTreeView  ------//
    private TreeItem<IntervalPane> getRootWithChildren(Interval interval) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        IntervalPane p = new IntervalPane(fxmlLoader.load(getClass().getResource("statTreeItem.fxml").openStream()));
        StatTreeItemController controller = fxmlLoader.getController();
        controller.init(interval.info.times.exec_time, interval.info.times.efficiency, interval.getType());

        String style = interval.getGradient(lostTime);
        if (lostTimeGPU > 0 && (style.equals("")
                || style.equals("-fx-background-color: linear-gradient(to right, transparent, transparent)"))) {
            style = interval.getGradientGPU(lostTimeGPU);
        }
        p.setStyle(style);

        p.setInterval(interval);
        if (!interval.isVisible()) {
            p.setOpacity(0.5);
        }
        TreeItem<IntervalPane> treeItem = new TreeItem<>(p);
        for (Interval inter : interval.intervals) {
            if (inter.isVisible() || inter.hasVisibleChildren()) {
                treeItem.getChildren().add(getRootWithChildren(inter));
            }
        }
        treeItem.expandedProperty().addListener((observable, oldValue, newValue) -> Utils.addBlink(treeItem));
        treeItem.setExpanded(true);
        return treeItem;
    }

    //-----  Displays Labels for Data in Chart  -----//
    private void displayLabelForData(XYChart.Data<String, Double> data) {
        StackPane bar = (StackPane) data.getNode();
        Text dataText = new Text(String.format("%.2f", data.getYValue()));
        dataText.setMouseTransparent(true);
        dataText.setPickOnBounds(false);
        dataText.setStyle("-fx-font-size: 10px;");
        bar.getChildren().add(dataText);
    }

    private void addGPUCards(List<GPUTimesJson> GPUs) {
        for (int i = 0; i < GPUs.size(); i++) {
            GPUPane gpuCard = new GPUPane();
            gpuCard.Init(GPUs.get(i), i + 1);
            GPUVBox.getChildren().add(gpuCard);
        }
    }

    //-----  Find data number from css list  -----//
    private Integer findDataNum(List<String> styleList) {
        // TODO: find also for axes
        for (String s : styleList) {
            if (pData.matcher(s).matches()) {
                return Integer.decode(s.replaceAll("[^0-9]", ""));
            }
        }
        return null;
    }

    private Integer findDataPartNum(List<String> styleList) {
        for (String s : styleList) {
            if (pDataPart.matcher(s).matches()) {
                return Integer.decode(s.replaceAll("[^0-9]", ""));
            }
        }
        return null;
    }

    //-----  Select Processor from analyzer.stat.Stat  -----//
    private void selectProc(int procNum) {
        Interval interval = getSelectedIntervalStat();
        if (procNum < 0 || interval == null
                || interval.info.proc_times.get(procNum).gpu_times == null
                || interval.info.proc_times.get(procNum).gpu_times.size() == 0) {
            resetProc(false, false);
            return;
        }
        resetProc(false, true);
        //-----  Set Divider Position  -----//
        if (GPUSplitPane.getDividerPositions()[0] > 0.98)
            GPUSplitPane.setDividerPosition(0, 0.66);
        //-----  Adjust chart style  -----//
        HashSet<Node> data = new HashSet<>(procStatChart.lookupAll(".chart-bar"));
        String dataName = ".data" + procNum;
        data.removeAll(procStatChart.lookupAll(dataName));
        for (Node n : data) {
            if (n.getStyleClass().contains("default-color0")) {
                n.setStyle("-fx-background-color: #f8e7e8;");
            }
            if (n.getStyleClass().contains("default-color1")) {
                n.setStyle("-fx-background-color: #d9a1d8;");
            }
            if (n.getStyleClass().contains("default-color2")) {
                n.setStyle("-fx-background-color: #c1dff6;");
            }
            if (n.getStyleClass().contains("default-color3")) {
                n.setStyle("-fx-background-color: #d5f5aa;");
            }
        }
        //-----  Adjust Scroll  -----//
//        GPUScrollPane.setVvalue(0);
        //-----  Add GPU cards  -----//
        addGPUCards(interval.info.proc_times.get(procNum).gpu_times);
        curProc = procNum;
    }

    //-----  Select Processes from analyzer.stat.Stat and update intervalFilterProcs -----//
    private void selectProcs() {
        if (fullRootInterval == null)
            return;

        int selectedIndex = statTreeView.getSelectionModel().getSelectedIndex();

        Interval interval = fullRootInterval.getIntervalForProcs(chooseProcButton.getChosenProcsPred());

        try {
            statTreeView.setRoot(getRootWithChildren(interval));
        } catch (Exception e) {
            System.out.println("[ERROR] error selecting processes: " + e);
        }

        statTreeView.getSelectionModel().select(selectedIndex);

        // TODO: show current proc filter
    }

    private void resetSelectProcs(){
        if (fullRootInterval == null)
            return;

        int selectedIndex = statTreeView.getSelectionModel().getSelectedIndex();

        try {
            statTreeView.setRoot(getRootWithChildren(fullRootInterval));
        } catch (Exception e) {
            System.out.println("[ERROR] error resetting processes selection: " + e);
        }

        Filter.execAllFilters(fullRootInterval, getFilters());
        statTreeView.getSelectionModel().select(selectedIndex);
    }

    //-----  Updates stacked bar chart for selected analyzer.interval  -----//
    private void updateStatChart(Interval interval) {
        if (interval.info.times.efficiency >= 0.95 && interval.info.times.gpu_num > 0) {
            gpuStatChartWrapperPane.displayData(interval);
            gpuStatChartWrapperPane.setVisible(true);
            procStatChart.setVisible(false);

            return;
        }

        procStatChart.displayLostTime(interval);
        procStatChart.setVisible(true);
        gpuStatChartWrapperPane.setVisible(false);
    }

    //-----  Initializes compare chart based on active mode  -----//
    private void initCompareChart(List<Interval> intervals, List<String> p_headings) {
        switch (curCompareType) {
            case lostTime:
                initCompareLostChart(intervals, p_headings);
                break;
            case GPU:
                initCompareGPUChart(intervals, p_headings);
                break;
        }
    }

    //-----  Initializes all compare analyzer.charts  -----//
    private void initAllCompareCharts(List<Interval> intervals, List<String> p_headings) {
        initCompareLostChart(intervals, p_headings);
        initCompareGPUChart(intervals, p_headings);
    }

    //-----  Initializes stacked bar chart for comparison selected analyzer.interval  -----//
    private void initCompareLostChart(List<Interval> intervals, List<String> p_headings) {
        XYChart.Series<String, Double> series1 = new XYChart.Series<>();
        XYChart.Series<String, Double> series2 = new XYChart.Series<>();
        XYChart.Series<String, Double> series3 = new XYChart.Series<>();
        XYChart.Series<String, Double> series4 = new XYChart.Series<>();
        series1.setName("Недостаточный параллелизм (sys)");
        series2.setName("Недостаточный параллелизм (user)");
        series3.setName("Простои");
        series4.setName("Коммуникации");

        //-----  Init lost time data  ------//
        for (int i = 0; i < intervals.size(); ++i) {
            String xname = p_headings.get(i) + "\n"
                    + String.format("%.2f", intervals.get(i).info.times.exec_time) + "\n"
                    + String.format("%.2f", intervals.get(i).info.times.efficiency) + "\n"
                    + intervals.get(i).info.id.pname + " (" + i + ")";
            series1.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.insuf_sys));
            series2.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.insuf_user));
            series3.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.idle));
            series4.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.comm));
        }

        statCompareChart.getYAxis().setAutoRanging(intervals.get(0).info.id.nlev == 0);
        statCompareChart.getYAxis().setTickMarkVisible(true);
        statCompareChart.getYAxis().setTickLabelsVisible(true);

        statCompareChart.getData().clear();
        statCompareChart.getData().addAll(series1, series2, series3, series4);

        //-----  Display labels  -----//
        for (int i = 0; i < intervals.size(); ++i) {
            displayLabelForData(series1.getData().get(i));
            displayLabelForData(series2.getData().get(i));
            displayLabelForData(series3.getData().get(i));
            displayLabelForData(series4.getData().get(i));
        }
        statCompareChart.setCategoryGap(20);

        statCompareChart.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 1) {
                    Node node = mouseEvent.getPickResult().getIntersectedNode();
                    Integer dataNum = findDataNum(node.getStyleClass());
                    Integer dataPartNum = findDataPartNum(node.getStyleClass());
                    if (dataNum == null || getSelectedIntervalsCompare() == null)
                        return;
                    Interval inter = getSelectedIntervalsCompare().get(dataNum);
                    PopOver popOver = new PopOver(new InsufPromt(inter));
                    popOver.show(node);
                    System.out.println(dataNum + " " + dataPartNum);
                }
            }
        });
    }

    //-----  Initializes line chart for GPU comparison selected analyzer.interval  -----//
    private void initCompareGPUChart(List<Interval> intervals, List<String> p_headings) {
        XYChart.Series<String, Double> seriesGPUProd = new XYChart.Series<>();
        XYChart.Series<String, Double> seriesGPULost = new XYChart.Series<>();
        XYChart.Series<String, Double> seriesExec = new XYChart.Series<>();
        seriesGPUProd.setName("Продуктивное время на GPU");
        seriesGPULost.setName("Потерянное время на GPU");
        seriesExec.setName("Время выполнения");

        //-----  Init series with GPU times data  -----//
        for (int i = 0; i < intervals.size(); ++i) {
            String xname = "GPU Units: " + intervals.get(i).getGPUNum() + "\n"
                    + p_headings.get(i) + "\n"
                    + String.format("%.2f", intervals.get(i).info.times.exec_time) + "\n"
                    + String.format("%.2f", intervals.get(i).info.times.efficiency) + "\n"
                    + intervals.get(i).info.id.pname + " (" + i + ")";

            seriesGPUProd.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.gpu_time_prod));
            seriesGPULost.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.gpu_time_lost));
            seriesExec.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.exec_time));
        }

        statCompareGPUChart.getYAxis().setAutoRanging(intervals.get(0).info.id.nlev == 0);
        statCompareGPUChart.getYAxis().setTickMarkVisible(true);
        statCompareGPUChart.getYAxis().setTickLabelsVisible(true);

        statCompareGPUChart.getData().clear();
        statCompareGPUChart.getData().addAll(seriesGPUProd, seriesGPULost, seriesExec);
    }

    //-----  Analysis  -----//

    @FXML
    public void procAnalysis() {
        if (procAnalysisButton.isSelected()) {
            enableProcAnalysis();
        } else {
            enableAnalysis();
        }
    }

    private void enableProcAnalysis() {
        Interval inter = getSelectedIntervalStat();
        setVisibleAnalysis(false);
        setVisibleProcAnalysis(true);
        if (inter == null) {
            return;
        }
        updateStatChart(inter);
        selectProc(curProc);
    }

    private void setVisibleAnalysis(boolean visible) {
        mainCharsScrollPane.setVisible(visible);
    }

    private void setVisibleProcAnalysis(boolean visible) {
        GPUSplitPane.setVisible(visible);
    }

    private void enableAnalysis() {
        Interval inter = getSelectedIntervalStat();
        setVisibleProcAnalysis(false);
        setVisibleAnalysis(true);
        if (inter == null) {
            return;
        }
        initAnalysis(inter);
    }

    public void initAnalysis(Interval inter) {
        statAnalysisView.initAnalysisCPU(inter);
        statAnalysisGPUView.initAnalysisGPU(inter);
    }

    //-----  Analysis END  -----//

    public void initStatTree(Interval rootInterval, boolean updateFullRoot) {
        lostTime = rootInterval.info.times.lost_time;
        lostTimeGPU = rootInterval.info.times.gpu_time_lost;
        TreeItem<IntervalPane> root;

        try {
            root = getRootWithChildren(rootInterval);
        } catch (Exception e) {
            System.out.println("[ERROR] error initializing analyzer.stat.Stat analyzer.interval tree: " + e);
            return;
        }

        if (updateFullRoot)
            fullRootInterval = rootInterval;

        statTreeView.setRoot(root);
        statTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        statTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        if (procAnalysisButton.isSelected()) {
                            updateStatChart(newValue.getValue().getInterval());
                            selectProc(curProc);
                        } else {
                            initAnalysis(newValue.getValue().getInterval());
                        }
                    }
        });

        statTreeView.getSelectionModel().select(0);
    }

    private void initStat(@org.jetbrains.annotations.NotNull Stat stat) {
        resetProc(true, true);
        procAnalysisButton.setSelected(false);
        setVisibleAnalysis(true);

        statLabel.setText(stat.getHeader());

        //-----  Init tree  -----//
        initStatTree(stat.interval, true);

        //-----  Adjust style  -----//
        statSplitPane.setDividerPositions(procStatChart.getWidth() / statSplitPane.getWidth(), 1);
        SplitPane.setResizableWithParent(statSplitPane.getItems().get(0), true);
        SplitPane.setResizableWithParent(statSplitPane.getItems().get(1), true);
    }

    private void updateCompareRoot(TreeItem<IntervalComparePane> item,
                                   List<Interval> intervals, List<String> pHeadings){
        item.getValue().setIntervals(intervals);
        item.getValue().setPHeadings(pHeadings);
        for (int i = 0; i < item.getChildren().size(); ++i) {
            int finalI = i;
            List<Interval> subIntervals = intervals.stream().map(elt -> elt.intervals.get(finalI)).collect(Collectors.toList());
            updateCompareRoot(item.getChildren().get(i), subIntervals, pHeadings);
        }
    }

    private TreeItem<IntervalComparePane> getRootWithChildren(List<Interval> intervals,
                                                              List<String> pHeadings) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        IntervalComparePane p;
        try {
             p = new IntervalComparePane(fxmlLoader.load(getClass().getResource("analyzer/statCompareTreeItem.fxml").openStream()));
        } catch (Exception e) {
            System.out.println("Error loading statCompareTreeItem.fxml\n" + e.toString());
            return null;
        }
        StatCompareTreeItemController controller = fxmlLoader.getController();
        int max_time_index = 0;
        int min_time_index = 0;
        for (int i = 0; i < intervals.size(); ++i) {
            if (intervals.get(max_time_index).info.times.exec_time < intervals.get(i).info.times.exec_time) {
                max_time_index = i;
            } else {
                min_time_index = i;
            }
        }
        controller.init(pHeadings.get(max_time_index), pHeadings.get(min_time_index),
                intervals.get(max_time_index).info.times.exec_time, intervals.get(min_time_index).info.times.exec_time,
                intervals.get(0).getType());
        p.setStyle(Interval.getCompareGradient(intervals, lostCompareTime));
        p.setIntervals(intervals);
        p.setPHeadings(pHeadings);
        TreeItem<IntervalComparePane> treeItem = new TreeItem<>(p);
        for (int i = 0; i < intervals.get(0).intervals.size(); ++i) {
            int finalI = i;
            List<Interval> subIntervals = intervals.stream().map(elt -> elt.intervals.get(finalI)).collect(Collectors.toList());
            treeItem.getChildren().add(getRootWithChildren(subIntervals, pHeadings));
        }
        treeItem.expandedProperty().addListener((observable, oldValue, newValue) -> Utils.addBlink(treeItem));
        treeItem.setExpanded(true);
        return treeItem;
    }

    private void initCompareIntervalTree(List<Stat> compareList) {
        //-----  Init tree  -----//
        lostCompareTime = Collections.max(compareList.stream().map(elt -> elt.interval.info.times.lost_time).collect(Collectors.toList()));
//        GPUCompareTime = Collections.max(compareList.stream()
//                .map(elt -> Math.max(Math.max(elt.analyzer.interval.info.times.gpu_time_prod, elt.analyzer.interval.info.times.gpu_time_lost),
//                        elt.analyzer.interval.info.times.exec_time)
//        ).collect(Collectors.toList()));
        List<Interval> intervals = compareList.stream().map(elt -> elt.interval).collect(Collectors.toList());
        List<String> p_headings = compareList.stream().map(elt -> elt.info.p_heading).collect(Collectors.toList());
        TreeItem<IntervalComparePane> root = getRootWithChildren(intervals, p_headings);
        statCompareTreeView.setRoot(root);

        statCompareTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        statCompareTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null)
                        initCompareChart(newValue.getValue().getIntervals(), newValue.getValue().getPHeadings());
                });
        statCompareTreeView.getSelectionModel().select(0);
        //-----  Init all analyzer.charts to set max Y values  -----//
        initAllCompareCharts(intervals, p_headings);
    }

    //-----  Get current selected analyzer.stat.Interval for loaded analyzer.stat.Stat  -----//
    public Interval getSelectedIntervalStat(){
        List<TreeItem<IntervalPane>> selectedItems =
                statTreeView.getSelectionModel().getSelectedItems();
        if (selectedItems.size() == 1)
                return selectedItems.get(0).getValue().getInterval();
        return null;
    }

    //-----  Get current selected analyzer.stat.Interval for compare stats  -----//
    public List<Interval> getSelectedIntervalsCompare(){
        List<TreeItem<IntervalComparePane>> selectedItems =
                statCompareTreeView.getSelectionModel().getSelectedItems();
        if (selectedItems.size() == 1)
            return selectedItems.get(0).getValue().getIntervals();
        return null;
    }

    //-----  Analyze chosen stat  -----//
    @FXML public void loadStat() {
        int size = statTableView.getSelectionModel().getSelectedItems().size();
        if (size != 1){
            ErrorDialog errorDialog = new ErrorDialog("Пожалуйста, выберите одну статистику для загрузки.");
            errorDialog.showDialog();
            return;
        }
        initStat(statTableView.getSelectionModel().getSelectedItem().getStat());
        setDisableLoadedStat(false);
        selectTab(1);
    }

    //-----  Add analyzer.stat.Stat to StatTableView  -----//
    private void AddStatToList(Stat stat, String creationTime)
    {
        statTableView.getItems().add(new StatRow(stat.getHeader(), creationTime, stat));
    }

    public void LoadStatList()
    {
        statTableView.getItems().clear();
        File statDir = new File(Main.StatDirPath);
        File[] dirs = statDir.listFiles((current, name) ->
                new File(current, name).isDirectory() && Pattern.compile("-?[0-9]*$").matcher(name).matches());
        if (dirs == null)
            return;
        for (File dir : dirs)
        {
            try {
                File[] files = dir.listFiles((current, name) -> Pattern.compile(".*json$").matcher(name).matches());
                if (files == null || files.length == 0)
                    throw new Exception("No analyzer.json stat file in directory " + dir.getName());
                File file = files[0];
                long cTime = ((FileTime) Files.getAttribute(file.toPath(), "creationTime")).toMillis();
                ZonedDateTime t = Instant.ofEpochMilli(cTime).atZone(ZoneId.systemDefault());
                String json = Main.readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
                Stat stat = new Stat(json, dir.getAbsolutePath(), false);
                AddStatToList(stat, dtf.format(t));
            } catch (Exception e){
                System.out.println("Error occurred loading dir " + dir + ": " + e.toString());
            }

        }
    }

    @FXML public void compareStats(){
        ObservableList<StatRow> selected = statTableView.getSelectionModel().getSelectedItems();
        if (selected.size() <= 1) {
            ErrorDialog errorDialog = new ErrorDialog("Пожалуйста, выберите две или более статистики для сравнения.");
            errorDialog.showDialog();
            return;
        }
        compareList = (ArrayList<Stat>) selected.stream().map(elt -> elt.getStat().clone()).collect(Collectors.toList());
        Stat.intersectStats(compareList);
        initCompareIntervalTree(compareList);
        setDisableCompare(false);
        selectTab(2);
    }

    public Interval getStatTreeRootInterval() {
        if (statTreeView.getRoot() != null)
            return statTreeView.getRoot().getValue().getInterval();
        return null;
    }

    //------------  RESET SECTION  ------------//

    @FXML public void resetFilters(){
        //-----  Deselect all filter CheckMenuItems  -----//
        for (Filter filter : significantFilters) {
            filter.setSelected(false);
        }

        Interval rootInterval = getStatTreeRootInterval();
        if (rootInterval != null) {
            Filter.resetAllFilters(rootInterval);
            initStatTree(rootInterval, false);
        }
    }

    private void resetCompareTypeChoiceBox() {
        compareTypeChoiceBox.getSelectionModel().select(0);
    }

    //-----  Reset selection of Processor  -----//
    private void resetProc(boolean resetDivider, boolean resetCurProc){
        if (resetCurProc)
            curProc = -1;
        for(Node n: procStatChart.lookupAll(".chart-bar"))
            n.setStyle(null);
        GPUVBox.getChildren().clear();
        if (resetDivider)
            GPUSplitPane.setDividerPositions(1);
    }

    @FXML public void resetLoadedStat() {
        enableAnalysis();
        resetFilters();
        SplitPane.setResizableWithParent(statSplitPane.getItems().get(0), false);
        SplitPane.setResizableWithParent(statSplitPane.getItems().get(1), true);
        SplitPane.setResizableWithParent(statSplitPane.getItems().get(2), false);
        SplitPane.setResizableWithParent(GPUSplitPane.getItems().get(1), false);
        resetProc(true, true);
        statLabel.setText("");
        statIntervalText.setText("");
        procStatChart.getData().clear();
        statTreeView.setRoot(null);
        setDisableLoadedStat(true);
        selectTab(0);
    }

    @FXML public void resetCompareStat() {
        statCompareChart.getData().clear();
        statCompareTreeView.setRoot(null);
        compareSplitPane.setDividerPositions(1);
        showCompareTreeButton.setSelected(false);
        setDisableCompare(true);
        resetCompareTypeChoiceBox();
        selectTab(0);
    }

    //------------   RESET SECTION END   ------------//

    //------------    DISABLE SECTION    ------------//

    private void setDisableLoadedStat(boolean disable) {
        try {
            tabPane.getTabs().get(1).setDisable(disable);
        } catch (Exception e) {
            System.out.println("Error setting 'Disable' value for Loaded analyzer.stat.Stat tab: " + e);

            procAnalysisButton.setDisable(disable);
            resetStatButton.setDisable(disable);
            statSplitPane.setDisable(disable);
        }
    }

    private void setDisableCompare(boolean disable) {
        try {
            tabPane.getTabs().get(2).setDisable(disable);
        } catch (Exception e) {
            System.out.println("Error setting 'Disable' value for Compare Stats tab: " + e);

            sortMenu.setDisable(disable);
            showCompareTreeButton.setDisable(disable);
            compareSplitPane.setDisable(disable);
            resetCompareStatButton.setDisable(disable);
            compareTypeChoiceBox.setDisable(disable);
        }

    }

    //------------  DISABLE SECTION END  ------------//

    @FXML public void compareShowIntervals() {
        if (showCompareTreeButton.isSelected()) {
            compareSplitPane.setDividerPositions((compareSplitPane.getWidth() - statCompareTreeView.getPrefWidth())
                    / compareSplitPane.getWidth());
        } else {
            statCompareTreeView.getSelectionModel().select(0);
            compareSplitPane.setDividerPositions(1);
        }
    }

    @FXML public void saveStat() throws Exception {
        List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);

        if (files == null || files.isEmpty())
            return;

        Stat stat;

        if (files.size() != 1) {
            stat = new Stat();
            try {
                stat.parseMulti(files);
            } catch (Exception e) {
                System.out.println("Error parsing multi stat: " + e.toString());
                return;
            }
            AddStatToList(stat, dtf.format(LocalDateTime.now()));
            return;
        }

        File file = files.get(0);

        String res = null;
        if (Main.ois != null && Main.oos != null)
            res = Main.readStat(file.getAbsolutePath());
        else
            res = LibraryImport.readStat(file.getAbsolutePath());
        if (res == null) {
            ErrorDialog errorDialog = new ErrorDialog("Не удалось прочитать статистику \"" + file.getName() + "\".");
            errorDialog.showDialog();
            return;
        }

        String tmpFileLocDir = file.getParentFile().getAbsolutePath();
        stat = new Stat(res);
        stat.save(res, tmpFileLocDir);

        AddStatToList(stat, dtf.format(LocalDateTime.now()));
    }

    @FXML public void deleteStat(){
        int size = statTableView.getSelectionModel().getSelectedItems().size();
        if (size == 0){
            ErrorDialog errorDialog = new ErrorDialog("Пожалуйста, выберите статистики для удаления.");
            errorDialog.showDialog();
            return;
        }
        List<StatRow> statRows = statTableView.getSelectionModel().getSelectedItems();
        for (StatRow statRow : statRows) {
            try {
                FileUtils.deleteDirectory(new File(statRow.getStat().dir));
            } catch (Exception e) {
                System.out.println("Error deleting dir " + statRow.getStat().dir);
                return;
            }
        }
        statTableView.getItems().removeAll(statRows);
    }

}
