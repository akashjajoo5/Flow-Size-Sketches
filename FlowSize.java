import java.util.*;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedWriter;

class FlowSize {
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of counters (k)");
        int k = sc.nextInt();
        System.out.println("Enter size of array (w)");
        int w = sc.nextInt();
        System.out.println("Enter number of bits for Active Counter");
        int ac = sc.nextInt();
        
        int[] hashFunction = new int[k];
        int[][] counters = new int[k][w];

        Set <Integer> uniqueHash = new HashSet<>();
        int j = 0;
        while (j < k) {
            int randNum = getRandom();
            if(!uniqueHash.contains(randNum)) {
                hashFunction[j++] = randNum;
                uniqueHash.add(randNum);
            }
        }

        try {
            File file = new File("project3input.txt");
            sc = new Scanner(file);
        } catch (Exception e) {
            e.getStackTrace();
            return;
        }

        int n = Integer.parseInt(sc.nextLine());

        String[] flowMap = new String[n];
        j = 0;
        Map <String, Packet> mapping = new HashMap<>();
        Set <Integer> uniqueFlowID = new HashSet<>();
        while (sc.hasNextLine()) {
            String[] str = sc.nextLine().split("\\s+");
            int flowId = getRandom();
            while (uniqueFlowID.contains(flowId)) {
                flowId = getRandom();
            }
            mapping.put(str[0], new Packet(Integer.parseInt(str[1]), flowId));
            uniqueFlowID.add(flowId);
            flowMap[j++] = str[0];
        }

        countMin(counters, hashFunction, mapping, flowMap);
        for (int[] c: counters) {
            Arrays.fill(c, 0);
        }
        counterSketch(counters, hashFunction, mapping, flowMap);
        activeCounter(ac);
    }

    public static void countMin(int[][] counters, int[] hashFunction, Map<String, Packet> mapping, String[] flowMap) {
        for (String s: mapping.keySet()) {
            Packet p = mapping.get(s);
            int flowId = p.flowId;
            for (int i = 0; i < hashFunction.length; i++) {
                int hashedVal = (hashFunction[i] ^ flowId) % counters[i].length;
                if(hashedVal < 0) {
                    hashedVal += counters[i].length;
                }
                counters[i][hashedVal] += p.numPackets;
            }
        }

        float avgError = 0;
        for (String s: mapping.keySet()) {
            Packet p = mapping.get(s);
            int flowId = p.flowId;
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < hashFunction.length; i++) {
                int hashedVal = (hashFunction[i] ^ flowId) % counters[i].length;
                if(hashedVal < 0) {
                    hashedVal += counters[i].length;
                }
                min = Math.min(min, counters[i][hashedVal]);
            }
            p.put(min);
            avgError += p.estimatedSize - p.numPackets; 
        }

        Arrays.sort(flowMap, new Comparator<String>() {
            @Override
            public int compare (String a, String b) {
                return Integer.compare(mapping.get(b).estimatedSize, mapping.get(a).estimatedSize);
            }
        });
        printToFile(avgError/mapping.size(), mapping, "countMin_output", flowMap);
    }

    public static void counterSketch(int[][] counters, int[] hashFunction, Map<String, Packet> mapping, String[] flowMap) {
        for (String s: mapping.keySet()) {
            Packet p = mapping.get(s);
            int flowId = p.flowId;
            for (int i = 0; i < hashFunction.length; i++) {
                int hashedVal = (hashFunction[i] ^ flowId) % counters[i].length;
                if(hashedVal < 0) {
                    if(hashedVal < 0) {
                        hashedVal += counters[0].length;
                    }
                    counters[i][hashedVal] += p.numPackets;
                } else {
                    counters[i][hashedVal] -= p.numPackets;
                }
            }
        }

        float avgError = 0;
        for (String s: mapping.keySet()) {
            Packet p = mapping.get(s);
            int flowId = p.flowId;
            List<Integer> e = new ArrayList<>();
            for (int i = 0; i < hashFunction.length; i++) {
                int hashedVal = (hashFunction[i] ^ flowId) % counters[i].length;
                if(hashedVal < 0) {
                    if(hashedVal < 0) {
                        hashedVal += counters[0].length;
                    }
                    e.add(counters[i][hashedVal]);
                } else {
                    e.add(-1 * counters[i][hashedVal]);
                }
            }
            Collections.sort(e);
            if(e.size() % 2 == 0) {
                int e1 = e.get(e.size()/2);
                int e2 = e.get(e.size()/2 - 1);
                p.put((e1+e2) / 2);
            } else {
                p.put(e.get(e.size()/2));
            }
            avgError += Math.abs(p.estimatedSize - p.numPackets);
        }

        Arrays.sort(flowMap, new Comparator<String>() {
            @Override
            public int compare (String a, String b) {
                return Integer.compare(mapping.get(b).estimatedSize, mapping.get(a).estimatedSize);
            }
        });
        printToFile(avgError/mapping.size(), mapping, "counterSketch_output", flowMap);
    }

    public static void activeCounter(int size) {
        int cn = 0, ce = 0;
        for (int i = 0; i < 1000000; i++) {
            if(new Random().nextInt((int)Math.pow(2, ce)) == 0) {
                cn++;
                if(cn == (int)Math.pow(2, size/2)) {
                    cn = cn>>1;
                    ce++;
                }
            } 
        }
        try {
            FileWriter file = new FileWriter("activeCounter_output.txt");
            BufferedWriter output = new BufferedWriter(file);
            
            output.write("Final value of Active counter in decimal: "+(cn * (int)Math.pow(2, ce)));          
            output.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public static int[] getBinary(int n, int bits) {
        int[] arr = new int[bits];
        int j = 0;
        while (n >= 1) {
           arr [bits-j-1] = n % 2;
           n /= 2;
           j++;
        }
        return arr;
    }

    public static void printToFile(float ans, Map<String,Packet> mapping, String filename, String[] flowMap) {
        try {
            FileWriter file = new FileWriter(filename+".txt");
            BufferedWriter output = new BufferedWriter(file);
            
            output.write("The average error among all flows- "+ans+"\n");
            output.write("\nThe flows with largest estimated sizes (Top 100)");
            int count = 0;
            for (String s : flowMap) {
                Packet p = mapping.get(s);
                output.write("\nFlow ID: "+s+" Estimated size: "+p.estimatedSize+" True size: "+p.numPackets);
                if(count++ == 100) {
                    break;
                }
            }
            output.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
    
    public static int getRandom() {
        Random r = new Random();
        return r.nextInt(Integer.MAX_VALUE) + Integer.MIN_VALUE/2;
    }
}

class Packet {
    int numPackets, flowId, estimatedSize;
    Packet (int numPackets, int flowId) {
        this.numPackets = numPackets;
        this.flowId = flowId;
    }

    void put (int estimatedSize) {
        this.estimatedSize = estimatedSize;
    }
}