import protocol.Process;
public class Test {
    public static void main(String[] args) throws InterruptedException {
        int nProc = 5;
        Process.setConfigurations(nProc);
        Process[] processes = new Process[nProc];
        for(int i=0;i<5;++i) {
           processes[i] = new Process(i);
           processes[i].start();
        }
    }
    
}
