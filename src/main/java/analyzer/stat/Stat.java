package analyzer.stat;

import analyzer.ErrorDialog;
import analyzer.LibraryImport;
import analyzer.Main;
import analyzer.json.IntervalJson;
import analyzer.json.StatJson;
import analyzer.json.UseStatJson;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Stat implements Cloneable, Comparable<Stat>{

    public StatJson info;
    public Interval interval;
    public String dir;

    public Stat(){}

    public int compareTo(@NotNull Stat compareStat) {
        return 1;
    }

    public Stat clone(){
        Stat res;
        try {
            res = (Stat) super.clone();
        } catch (Exception e) {
            System.out.println("Couldn't clone analyzer.stat.Stat");
            return null;
        }
        res.info = info; // TODO: clone
        res.interval = interval.clone();
        res.dir = dir;
        return res;
    }

    public Stat(StatJson statJson, String dir, boolean withText){
        this.dir = dir;
        info = statJson;
        interval = new Interval(info.inter, dir, withText);
    }

    public Stat(String stat, String dir, boolean withText){
        this.dir = dir;
        info = UseStatJson.GetStat(stat);
        interval = new Interval(info.inter, dir, withText);
    }

    public Stat(String stat){
        info = UseStatJson.GetStat(stat);
        interval = new Interval(info.inter, dir, false);
    }

    public String getHeader(){
        return interval.info.id.pname + "  |  " + info.p_heading + "  |  "
                + String.format("%.3f", interval.info.times.exec_time) + "s";
    }

    private void updateInterval(){
        interval = new Interval(info.inter, dir, false);
    }

    public static void cutIntervals(List<List<Interval>> statsIntervals){
        List<Interval> intervals = statsIntervals.get(0);
        ArrayList<Interval> remInters = new ArrayList<>();
        for (Interval inter : intervals) {
            int type = inter.info.id.t;
            int expr = inter.info.id.expr;
            ArrayList<List<Interval>> intersectInters = new ArrayList<>();
            intersectInters.add(inter.intervals);
            boolean found = true;
            for (int j = 0; found && j < statsIntervals.size(); ++j) {
                int offset = 0;
                while (offset < statsIntervals.get(j).size()) {
                    if (statsIntervals.get(j).get(offset).info.id.t == type &&
                            statsIntervals.get(j).get(offset).info.id.expr == expr) {
                        intersectInters.add(statsIntervals.get(j).get(offset).intervals);
                        break;
                    }
                    ++offset;
                }
                found = offset < statsIntervals.get(j).size();
            }
            if (found) {
                cutIntervals(intersectInters);
            } else {
                remInters.add(inter);
            }
        }
        if (!remInters.isEmpty())
            intervals.removeAll(remInters);
    }

    // функция подрезает дерево statIntervals под структуру дерева intervals
    // intervals - образец, statInters - обрезаемое дерево
    public static void rmRedIntervals(List<Interval> intervals, List<Interval> statIntervals){
        ArrayList<Interval> remInters = new ArrayList<>();
        if (intervals.size() == 0) {
            statIntervals.clear();
            return;
        }
        int i = 0;
        for (Interval inter : statIntervals) {
            if (i < intervals.size() && inter.info.id.t == intervals.get(i).info.id.t &&
                    inter.info.id.expr == intervals.get(i).info.id.expr) {
                // нашли очередной интервал из intervals
                // обрезаем его в рекурсивном вызове
                rmRedIntervals(intervals.get(i).intervals, inter.intervals);
                ++i;
            } else {
                remInters.add(inter);
            }
        }
        // удаляем все лишние интервалы
        statIntervals.removeAll(remInters);
    }

    public static void intersectStats(ArrayList<Stat> stats){
        if (stats.size() <= 1)
            return;
        cutIntervals(stats.stream().map(elt -> elt.interval.intervals).collect(Collectors.toList()));
        Stat stat = stats.get(0);
        for (int i = 1; i < stats.size(); ++i){
            rmRedIntervals(stat.interval.intervals, stats.get(i).interval.intervals);
        }
    }

    @Override
    public String toString() {
        return getHeader() + "\n" + interval.toString();
    }

    public void save(String statText, String fileLocDir) throws IOException {
        dir = Main.StatDirPath + this.hashCode();
        Files.createDirectory(Paths.get(dir));

        FileWriter writer = new FileWriter(dir + "/stat.analyzer.json");
        writer.write(statText);
        writer.close();

        if (fileLocDir.equals(""))
            return;

        for (IntervalJson inter : this.info.inter)
        {
            if (!Files.exists(Paths.get(dir + '/' + inter.id.pname)))
                try
                {
                    if (Files.exists(Paths.get(fileLocDir + '/' + inter.id.pname)))
                        Files.copy(Paths.get(fileLocDir + '/' + inter.id.pname),
                                Paths.get(dir + '/' + inter.id.pname));
                }
                catch (Exception e)
                {
                    System.out.println("Could not copy file '" + inter.id.pname + "'");
                }
        }
    }

    private ErrorDialog notValid(String name)
    {
        return new ErrorDialog("Невалидное имя статистики \"" + name + "\".\n" +
                               "Необходимо, чтобы имя файла было вида <name>_<rank>.gz+.");
    }

    public void parseMulti(List<File> files) throws Exception {
        if (files.size() <= 1)
            throw new Exception("Illegal files count");

        HashMap<String, File> names = new HashMap<>();
        String base = "";
        for (File f : files) {
            String name = f.getName();
            names.put(name, f);
            int idx = name.lastIndexOf('_');
            if (idx < 0 || idx >= name.length()) {
                notValid(f.getName()).showDialog();
                throw new Exception("Invalid name: \"" + f.getName() + "\"");
            }

            if (base.length() == 0)
                base = name.substring(0, idx);
            else {
                if (!base.equals(name.substring(0, idx))) {
                    notValid(f.getName()).showDialog();
                    throw new Exception("Invalid name: \"" + f.getName() + "\"");
                }
            }
        }

        ArrayList<Stat> stats = new ArrayList<>(files.size() - 1);
        for (int i = 0; i < files.size() - 1; ++i)
            stats.add(new Stat());

        for (int z = 0; z < files.size(); ++z) {
            String key = base + "_" + z + ".gz+";
            if (!names.containsKey(key)){
                notValid(key).showDialog();
                throw new Exception("Not found file: \"" + key + "\"");
            }

            String res = null;
            if (Main.ois != null && Main.oos != null)
                res = Main.readStat(names.get(key).getAbsolutePath());
            else
                res = LibraryImport.readStat(names.get(key).getAbsolutePath());

            if (res == null) {
                ErrorDialog errorDialog = new ErrorDialog("Не удалось прочитать статистику \"" + names.get(key).getName() + "\".");
                errorDialog.showDialog();
                throw new Exception("Error in readStat");
            }

            if (z == 0)
                info = UseStatJson.GetStat(res);
            else
                stats.set(z - 1, new Stat(res));
        }

        info.is_multi = true;
        info.unionWithStats(stats.stream().map(a -> a.info).collect(Collectors.toList()));
        updateInterval();

        save(UseStatJson.GetJson(info), "");
    }

    //    public analyzer.stat.Stat(File path, boolean withText)
//    {
//        dir = Path.GetDirectoryName(path);
//        StreamReader reader = new StreamReader(path);
//        string analyzer.json = reader.ReadToEnd();
//        reader.Close();
//        Info = UseStatJson.GetStat(analyzer.json);
//        analyzer.stat.Interval = new analyzer.stat.Interval(Info.inter, Dir, withText);
//    }

//    public analyzer.stat.Stat(String analyzer.json, String dir, boolean withText)
//    {
//        this.dir = dir;
//        info = UseStatJson.GetStat(analyzer.json);
//        analyzer.interval = new analyzer.stat.Interval(info.inter, dir, withText);
//    }

}
