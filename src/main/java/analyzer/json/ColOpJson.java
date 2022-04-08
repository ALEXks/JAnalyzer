package analyzer.json;

public class ColOpJson {
    public double ncall;
    public double comm;
    public double real_comm;
    public double synch;
    public double time_var;
    public double overlap;

    public void addColOp(ColOpJson colOp) {
        ncall += colOp.ncall;
        comm += colOp.comm;
        real_comm += colOp.real_comm;
        synch += colOp.synch;
        time_var += colOp.time_var;
        overlap += colOp.overlap;
    }
}
