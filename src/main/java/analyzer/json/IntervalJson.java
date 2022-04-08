package analyzer.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.IntPredicate;

public class IntervalJson
{
    public IdentJson id = new IdentJson();
    public InterTimesJson times = new InterTimesJson();
    public List<ColOpJson> col_op = new ArrayList<>();
    public List<ProcTimesJson> proc_times = new ArrayList<>();

    public void addInterval(IntervalJson inter) {
        times.addTimes(inter.times);

        for (int i = 0; i < col_op.size(); ++i) {
            if (i >= inter.col_op.size()) {
                System.out.println("[WARN] col_op wrong size: " + inter.col_op.size());
                break;
            }
            col_op.get(i).addColOp(inter.col_op.get(i));
        }

        proc_times.addAll(inter.proc_times);

        if (proc_times.size() != times.nproc)
            System.out.println("[WARN] proc_times wrong size: " + proc_times.size() + ", must be " + times.nproc);
    }

    public IntervalJson getIntervalForProcs(IntPredicate procPred) {
        // TODO: fix trouble with colOp
        IntervalJson inter = new IntervalJson();
        inter.id = id;
        for (int i = 0; i < times.nproc; ++i) {
            if (procPred.test(i)) {
                inter.proc_times.add(proc_times.get(i));
            }
        }

        inter.recountAggregated();

        return inter;
    }

    private void recountAggregated() {
        times.prod_cpu = proc_times.stream().mapToDouble(p -> p.prod_cpu).sum();
        times.prod_sys = proc_times.stream().mapToDouble(p -> p.prod_sys).sum();
        times.prod_io = proc_times.stream().mapToDouble(p -> p.prod_io).sum();
        times.prod_time = times.prod_cpu + times.prod_sys + times.prod_io;
        OptionalDouble exec_time = proc_times.stream().mapToDouble(p -> p.exec_time).max();
        times.exec_time = exec_time.isPresent() ? exec_time.getAsDouble() : 0;
        times.sys_time = proc_times.stream().mapToDouble(p -> p.exec_time).sum();
        times.efficiency = times.prod_time / times.sys_time; // TODO: ?
        times.lost_time = proc_times.stream().mapToDouble(p -> p.lost_time).sum();
        times.insuf_user = proc_times.stream().mapToDouble(p -> p.insuf_user).sum();
        times.insuf_sys = proc_times.stream().mapToDouble(p -> p.insuf_sys).sum();
        times.insuf = times.insuf_user + times.insuf_sys;
        times.comm = proc_times.stream().mapToDouble(p -> p.comm).sum();
        times.real_comm = proc_times.stream().mapToDouble(p -> p.real_comm).sum();
        //times.comm_start = proc_times.stream().mapToDouble(p -> p.comm_start).sum();
        times.idle = proc_times.stream().mapToDouble(p -> p.idle).sum();
        times.load_imb = proc_times.stream().mapToDouble(p -> p.load_imb).sum();
        times.synch = proc_times.stream().mapToDouble(p -> p.synch).sum();
        times.time_var = proc_times.stream().mapToDouble(p -> p.time_var).sum();
        times.overlap = proc_times.stream().mapToDouble(p -> p.overlap).sum();
        times.thr_user_time = proc_times.stream().mapToDouble(p -> p.thr_user_time).sum();
        times.thr_sys_time = proc_times.stream().mapToDouble(p -> p.thr_sys_time).sum();
        times.gpu_time_prod = proc_times.stream().mapToDouble(p -> p.gpu_time_prod).sum();
        times.gpu_time_lost = proc_times.stream().mapToDouble(p -> p.gpu_time_lost).sum();
        times.nproc = proc_times.size();
        times.threadsOfAllProcs = proc_times.stream().mapToLong(p -> (p.num_threads <= 0)?1:p.num_threads).sum();

        calculateAdditionalMetrics();
    }

    public void calculateAdditionalMetrics(){
        Optional<Long> gpu_num_opt = proc_times.stream().map(x->x.num_gpu).reduce(Long::sum);
        times.gpu_num = gpu_num_opt.isPresent()?gpu_num_opt.get():0;
        times.gpu_sys_time = times.gpu_time_lost + times.gpu_time_prod;
        times.gpu_efficiency = times.gpu_time_prod / times.gpu_sys_time;
    }

}