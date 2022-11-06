package baseIDP.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import baseIDP.graph.Graph;
import baseIDP.graph.GraphUtility;

public class Process extends Thread{
    // the number of pocesses
    private static int nProc;

    // the process waits for L-1 messages
    private static int L;

    // this BlockingQueue simulates the message passing between processes
    private static ArrayList<BlockingQueue<Message>> firstMessageBuffers;
    private static ArrayList<BlockingQueue<Message>> secondMessageBuffers; 

    private int id;

    // the graphs used by the protocol 
    private Graph g,gPlus;

    private int initialValue,finalDecision;

    public Process(int id) {
        this.id = id;
    }
    


    private Random random = new Random();

    public void run() {

        // simulate a sleep and the possibility of a failure
        randomSleep();
        if(randomError()) {
            System.out.println(id+" : error...shutting down...");
            return;
        }
        BlockingQueue<Message> myFirstBuffer = firstMessageBuffers.get(id);
        BlockingQueue<Message> mySecondBuffer = secondMessageBuffers.get(id);

        Message firstMessage = new FirstMessage(this.id);

        // send its id to all processes
        for(int i=0;i<nProc;++i)
            if(i!=this.id)
                try {
                    firstMessageBuffers.get(i).put(firstMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

        // read the first L-1 messages
        List<Message> messages = new LinkedList<>();
        for(int i=0;i<L-1;++i)
            try {
                messages.add(myFirstBuffer.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        // the ids of the ancestors
        Set<Integer> ancenstors = messages.stream()
                                .map(x->x.getSenderId())
                                .collect(Collectors.toSet());


        // choose initial value
        initialValue = (random.nextDouble()>=0.5) ? 1 : 0;
        System.out.println(id+" : initial value : "+initialValue);

        // send its id, ancestors and initial value to the other processes
        Message secondMessage = new SecondMessage(id, ancenstors,initialValue);
        for(int i=0;i<nProc;++i) {
            //if(i!=this.id) doesn't work
                try {
                    secondMessageBuffers.get(i).put(secondMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        
        
        Set<Integer> toWait = new HashSet<>(ancenstors);

        List<SecondMessage> unorderedMessages = new ArrayList<>();
        Message m = new FirstMessage(-1);
        SecondMessage sm;
        Map<Integer,Set<Integer>> map = new HashMap<>();
        Map<Integer,Integer> values = new HashMap<>();

        while(!toWait.isEmpty()) {
            List<SecondMessage> toRemove = new ArrayList<>();
            for(SecondMessage usm : unorderedMessages) {
                if(toWait.contains(usm.getSenderId())) {
                    toWait.remove(usm.getSenderId());
                    toWait.addAll(usm.getAncestors());
                    map.put(usm.getSenderId(), usm.getAncestors());
                    values.put(usm.getSenderId(), usm.getInitialValue());
                    toRemove.add(usm);
                }
            }
            unorderedMessages.removeAll(toRemove);

            boolean flag = true;
            try {
                m = mySecondBuffer.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sm = (SecondMessage) m;
            if(toWait.contains(sm.getSenderId())) {
                flag=false;
                toWait.remove(sm.getSenderId());
                toWait.addAll(sm.getAncestors());
                map.put(sm.getSenderId(),sm.getAncestors());
                values.put(sm.getSenderId(), sm.getInitialValue());
            }
            
            
            if(flag)
                unorderedMessages.add(sm);

            toWait.removeAll(map.keySet());
        }

        int activeProc;
        ancenstors.addAll(map.keySet());
        activeProc=ancenstors.size();
        if(!ancenstors.contains(id)) activeProc++;

        g = new Graph(nProc);
        



        for(Message mex : messages)
            if(mex instanceof FirstMessage) {
                FirstMessage fm =  ((FirstMessage) mex);
                this.g.setEdge(fm.getSenderId(), this.id,1);
            }
            else {
                throw new IllegalArgumentException("Not enough message received");
            }
        
        for(Integer k : map.keySet())
            for(Integer j : map.get(k))
                this.g.setEdge(j,k,1);
        
        gPlus = GraphUtility.transitiveClouserOf(g);

        Set<Integer> initialClique = GraphUtility.initialClique(gPlus);

        double result = initialClique.stream()
                            .filter(x->ancenstors.contains(x))
                            .map(x->values.get(x))
                            .reduce(0,(x,y)->x+y) / (double) activeProc;
        
        finalDecision = (result>=0.5) ? 1 : 0;

        System.out.println(id+" : My final decision is : "+finalDecision);

    }


    private boolean randomError() {
        return random.nextDouble()>=0.8;
            
    }
    private void randomSleep() {
        int sleepTime = 5000;
        if(id==4)
            sleepTime=20000;
        try {
            
            Thread.sleep(random.nextInt(sleepTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setConfigurations(int nProc) {
        Process.nProc=nProc;
        Process.L = (int) Math.ceil((nProc+1)/(double)2);
        Process.firstMessageBuffers = new ArrayList<BlockingQueue<Message>>();
        Process.secondMessageBuffers = new ArrayList<BlockingQueue<Message>>();
        for(int i=0; i<nProc; i++) {
            firstMessageBuffers.add(new LinkedBlockingQueue<>()); 
            secondMessageBuffers.add(new LinkedBlockingQueue<>()); 

        }

    }

    public int getInitialValue() {
        return initialValue;
    }
    
    public int getFinalDecision() {
        return this.finalDecision;
    }
    
}

interface Message {
    int getSenderId();
    
}

class FirstMessage implements Message{
    private int senderId;

    public FirstMessage(int senderId) {
        this.senderId = senderId;
    }

    public int getSenderId() {
        return senderId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + senderId;
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
        FirstMessage other = (FirstMessage) obj;
        if (senderId != other.senderId)
            return false;
        return true;
    }
}

class SecondMessage implements Message {

    private int senderId;
    private Set<Integer> ancestors;
    private int initialValue;

    
    public SecondMessage(int senderId, Set<Integer> ancestors,int initialValue) {
        this.senderId = senderId;
        this.ancestors = new HashSet<>(ancestors);
        this.initialValue = initialValue;
    }
    


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + senderId;
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
        SecondMessage other = (SecondMessage) obj;
        if (senderId != other.senderId)
            return false;
        return true;
    }



    public int getSenderId() {
        return this.senderId;
    }


    public Set<Integer> getAncestors() {
        return ancestors;
    }


    public int getInitialValue() {
        return initialValue;
    }
    

}
