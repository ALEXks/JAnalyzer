package analyzer.json;

import java.util.List;

public class StatJson
{
    public boolean is_multi = false;
    public int nproc;
    public boolean iscomp;
    public String p_heading;
    public List<ProcInfoJson> proc;
    public List<IntervalJson> inter;

    // unions with stats in this StatJson
    public void unionWithStats(List<StatJson> stats) {
        nproc += stats.stream().mapToInt(a -> a.nproc).reduce(0, Integer::sum);
        p_heading = "Multi-" + nproc;

        for (StatJson stat: stats) {
            proc.addAll(stat.proc);
        }

        for (int i = 0; i < inter.size(); ++i) {
            for (StatJson stat: stats) {
                if (i >= stat.inter.size() || stat.inter.get(i).id.expr != inter.get(i).id.expr) {
                    System.out.println("[WARN] intervals don't match");
                    continue;
                }
                inter.get(i).addInterval(stat.inter.get(i));
            }
        }
    }
}