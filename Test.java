import algorithm.Process;
public class Test {
    public static void main(String[] args) {
        int nProc = 5;
        Process.setConfigurations(nProc);
        
        for(int i=0;i<5;++i)
            new Process(i).start();
    }
    
}
