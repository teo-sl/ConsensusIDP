package graph;

import java.util.Arrays;

public class Graph {
    private int[][] adiacenze;
    private int n;

    public Graph(int n) {
        this.n=n;
        this.adiacenze = new int[n][n];
        for(int i=0;i<n;++i)
            Arrays.fill(adiacenze[i],0);
    }
    public Graph(Graph g) {
        this.n = g.n;
        this.adiacenze= new int[n][n];
        for(int i=0;i<n;++i)
            for(int j=0;j<n;++j)
                this.adiacenze[i][j]=g.adiacenze[i][j];
    }

    public int getEdge(int i, int j) {
        return adiacenze[i][j];
    }

    public int getN() {
        return n;
    }
    public void setEdge(int i, int j,int value) {
        if(i>n || j>n || i<0 || j<0) throw new IllegalArgumentException("Index out of range");
        if(value!=1 && value!=0) throw new IllegalArgumentException("Value different from 0 and 1"); 
        adiacenze[i][j] = value;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(adiacenze);
        result = prime * result + n;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Graph other = (Graph) obj;
        if (!Arrays.deepEquals(adiacenze, other.adiacenze))
            return false;
        if (n != other.n)
            return false;
        return true;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(4*n*n);
        sb.append("Grafo:\n");
        sb.append("   ");
        for(int i=0;i<n;++i)
            sb.append(i+"  ");
        sb.append("\n");
        for(int i=0;i<n;++i)
            sb.append(i+" "+Arrays.toString(adiacenze[i])+"\n");
        sb.append("Size: "+n+"\n");
        return sb.toString();
    }
    

    
}