package graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class GraphUtility {
    public static Graph transitiveClouserOf(Graph g) {
        int n = g.getN();
        Graph reach = new Graph(g);

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    reach.setEdge(i, j,
                            (reach.getEdge(i, j)!=0) ||
                             ((reach.getEdge(i, k)!=0) && (reach.getEdge(k,j)!=0))?1:0);
                }
            }
        }
        return reach;
    }

    public static Set<Integer> initialClique(Graph g) {
        Set<Integer> ret = new HashSet<>();
        int n = g.getN();
        Set<Integer> ancestors;
        for(int i=0;i<n;++i) {
            ancestors = getAncestors(g, i);
            boolean flag = true;
            for(Integer a : ancestors)
                if(!getAncestors(g, a).contains(i)) flag=false;
            if(flag) ret.add(i);
        }
        return ret;
    }

    public static Set<Integer> getAncestors(Graph g, int p) {
        int n = g.getN(),v;
        if(p<0 || p>=n) throw new IndexOutOfBoundsException();
        Set<Integer> ret = new HashSet<>();
        LinkedList<Integer> toVisit = new LinkedList<>();
        toVisit.add(p);

        while(!toVisit.isEmpty()) {
            v = toVisit.removeFirst();
            for(int i=0;i<n;++i) {
                if(g.getEdge(i,v)==1 && !ret.contains(i)) {
                    ret.add(i);  
                    toVisit.add(i);
                }
            }
        }
        return ret;
    }



    public static void main(String[] args) {
        int n=4;
        Graph g = new Graph(n);
        g.setEdge(0, 1, 1);
        g.setEdge(0, 2, 1);
        g.setEdge(1, 3, 1);
        g.setEdge(1, 2, 1);
        g.setEdge(2, 3, 1);
        g.setEdge(3, 1, 1);
        g.setEdge(1, 0, 1);
        g.setEdge(2, 0, 1);

        System.out.println(g);

        Graph gPlus = transitiveClouserOf(g);

        System.out.println(gPlus);

        System.out.println(getAncestors(g, 2));


        System.out.println(initialClique(gPlus));

    }
    
}

    
