package analyzer.json;

public class InterTimesJson {
    public double prod_cpu;
    public double prod_sys;
    public double prod_io;
    public double prod_time;
    public double exec_time;
    public double sys_time;
    public double efficiency;
    public double lost_time;
    public double insuf;
    public double insuf_user;
    public double insuf_sys;
    public double comm;
    public double real_comm;
    public double comm_start;
    public double idle;
    public double load_imb;
    public double synch;
    public double time_var;
    public double overlap;
    public double thr_user_time;
    public double thr_sys_time;
    public double gpu_time_prod;
    public double gpu_time_lost;
    public long nproc;
    public long threadsOfAllProcs;

    // additional GPU accumulated metrics
    public long gpu_num;
    public double gpu_sys_time;
    public double gpu_efficiency;

    public void addTimes(InterTimesJson times) {
        prod_cpu += times.prod_cpu;
        prod_sys += times.prod_sys;
        prod_io += times.prod_io;
        prod_time += times.prod_time;
        exec_time = Math.max(exec_time, times.exec_time);
        sys_time += times.sys_time;
        efficiency = (efficiency + times.efficiency) / 2; // average
        lost_time += times.lost_time;
        insuf += times.insuf;
        insuf_user += times.insuf_user;
        insuf_sys += times.insuf_user;
        comm += times.comm;
        real_comm += times.real_comm;
        comm_start += times.comm_start;
        idle += times.idle;
        load_imb += times.load_imb;
        synch += times.synch;
        time_var += times.time_var;
        overlap += times.overlap;
        thr_user_time += times.thr_user_time;
        thr_sys_time += times.thr_sys_time;
        gpu_time_prod += times.gpu_time_prod;
        gpu_time_lost += times.gpu_time_lost;
        nproc += times.nproc;
        threadsOfAllProcs += times.threadsOfAllProcs;

        gpu_num += times.gpu_num;
        gpu_sys_time += times.gpu_sys_time;
        gpu_efficiency = (gpu_efficiency + times.gpu_efficiency) / 2; // average
    }
}