package butterfly;


import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.util.Random;


public class ButterflyAcc extends Butterfly {

    private Int2ObjectOpenHashMap<IntOpenHashSet> srcToDsts = new Int2ObjectOpenHashMap(); // graph composed of the sampled edges
    private Int2ObjectOpenHashMap<IntOpenHashSet> dstToSrcs = new Int2ObjectOpenHashMap(); // graph composed of the sampled edges
    private Long2ObjectOpenHashMap<IntOpenHashSet> edgeToIndex = new Long2ObjectOpenHashMap(); // edge to the index of cell that the edge is stored in
    private Int2DoubleOpenHashMap nodeToButterflies_S = new Int2DoubleOpenHashMap(); // local butterfly counts of node partition src
    private Int2DoubleOpenHashMap nodeToButterflies_D = new Int2DoubleOpenHashMap(); // local butterfly counts of node partition dst
    private double globalButterfly = 0; // global butterfly count

    private long s = 0;         // number of processed edges
    private int samplenum = 0;  // number of current sampled edges
    private int nb = 0;         // number of uncompensated deletions
    private int ng = 0;         // number of uncompensate deletions

    private final int k; // maximum number of samples
    private final int[][] samples; // sampled edges

    private final Random random;
    private final boolean lowerBound;

    /**
     *
     * @param memoryBudget maximum number of samples
     * @param seed random seed
     */
    public ButterflyAcc(final int memoryBudget, final int seed) {
        this(memoryBudget, seed, true);
    }

    public ButterflyAcc(final int memoryBudget, final int seed, final boolean lowerBound) {
        random = new Random(seed);
        this.k = memoryBudget;
        samples = new int[2][this.k];
        nodeToButterflies_S.defaultReturnValue(0);
        nodeToButterflies_D.defaultReturnValue(0);
        this.lowerBound = lowerBound;
    }

    @Override
    public void processAddition(final int src, final int dst) {
        processEdge(src, dst, true);
    }

    @Override
    public void processDeletion(final int src, final int dst) {
        processEdge(src, dst, false);
    }

    public void processEdge(int src, int dst, boolean add) {

        count(src, dst, add); //count the added or deleted butterflies

        if (add) {

            //sample edge start
            if (ng + nb == 0) {
                if (samplenum < k) {
                    addEdge(src, dst);
                    samplenum += 1;
                }
                else if (random.nextDouble() < k / (s + 1.0)) {
                    int index = random.nextInt(edgeToIndex.size());
                    deleteEdge(samples[0][index], samples[1][index]); // remove a random edge from the samples
                    samplenum -= 1;
                    addEdge(src, dst); // store the sampled edge
                    samplenum += 1;
                }
            }
            else if (random.nextDouble() < nb / (nb + ng + 0.0)){
                addEdge(src, dst); // store the sampled edge
                samplenum += 1;
                nb--;
            }
            else {
                ng--;
            }
        }

        else {

            long key = ((long)src * Integer.MAX_VALUE) + dst;
            if (edgeToIndex.containsKey(key)) {
                deleteEdge(src, dst); // remove the edge from the samples
                samplenum -= 1;
                nb++;
            }
            else {
                ng++;
            }
        }

        if (add) {
            s++;
        }
        else {
            s--;
        }


        return;
    }

    /**
     * Store a sampled edge
     * @param src source node of the given edge
     * @param dst destination node of the given edge
     */
    private void addEdge(int src, int dst) {

        samples[0][samplenum] = src;
        samples[1][samplenum] = dst;

    	long key = (((long)src * Integer.MAX_VALUE) + dst);
        if (!edgeToIndex.containsKey(key)){
            edgeToIndex.put(key, new IntOpenHashSet());
        }
        edgeToIndex.get(key).add(samplenum);

        if (edgeToIndex.get(key).size() == 1){
            if (!srcToDsts.containsKey(src)) {
                srcToDsts.put(src, new IntOpenHashSet());
            }
            srcToDsts.get(src).add(dst);

            if (!dstToSrcs.containsKey(dst)) {
                dstToSrcs.put(dst, new IntOpenHashSet());
            }
            dstToSrcs.get(dst).add(src);
        }
    }

    /**
     * Remove an edge from the samples
     * @param src source node of the given edge
     * @param dst destination node of the given edge
     */
    private void deleteEdge(int src, int dst) {

		long key = ((long)src * Integer.MAX_VALUE) + dst;
        IntOpenHashSet map = edgeToIndex.get(key);
        for (int dum : map) {
            int index = dum;
            map.remove(index);

            if (map.isEmpty()){
                edgeToIndex.remove(key);
                map = srcToDsts.get(src);
                map.remove(dst);
                if (map.isEmpty()) {
                    srcToDsts.remove(src);
                }
                map = dstToSrcs.get(dst);
                map.remove(src);
                if (map.isEmpty()) {
                    dstToSrcs.remove(dst);
                }
            }

            if (index < (samplenum - 1)) {
                int newSrc = samples[0][index] = samples[0][samplenum - 1];
                int newDst = samples[1][index] = samples[1][samplenum - 1];
                long newKey = ((long) newSrc * Integer.MAX_VALUE) + newDst;
                edgeToIndex.get(newKey).remove(samplenum - 1);
                edgeToIndex.get(newKey).add(index);
            }

            break;
        }

    }

    @Override
    public double getGlobalButterfly() {
        return globalButterfly;
    }

    @Override
    public Int2DoubleOpenHashMap getLocalButterfly_S() {
        return nodeToButterflies_S;
    }

    @Override
    public Int2DoubleOpenHashMap getLocalButterfly_D() {
        return nodeToButterflies_D;
    }

    /**
     * counts triangles with the given edge and update estimates
     * @param src the source node of the given edge
     * @param dst the destination node of the given edge
     * @param add true for addition and false for deletion
     */
    private void count(int src, int dst, boolean add) {

        // if this edge has a new node, there cannot be any triangles
        if (!srcToDsts.containsKey(src) || !dstToSrcs.containsKey(dst)) {
            return;
        }

        IntOpenHashSet srcSet = srcToDsts.get(src);
        IntOpenHashSet dstSet = dstToSrcs.get(dst);
        IntOpenHashSet tempSet;

        final double y = Math.min(k, s + nb + ng);
        final double weight = (y / (s + nb + ng)) * ((y - 1) / (s + nb + ng - 1)) * ((y - 2) / (s + nb + ng - 2));

        if (add) { // process the addition

            double count = 0;
            double butterflyweight = 0;
            for (int neighbor1 : srcSet) {
                tempSet = dstToSrcs.get(neighbor1);
                for (int neighbor2 : tempSet) {
                    if (dstSet.contains(neighbor2)) {
                    	int srctoone = edgeToIndex.get(((long)src * Integer.MAX_VALUE) + neighbor1).size();
                    	int dsttotwo = edgeToIndex.get(((long)neighbor2 * Integer.MAX_VALUE) + dst).size();
                    	int onetotwo = edgeToIndex.get(((long)neighbor2 * Integer.MAX_VALUE) + neighbor1).size();
                    	butterflyweight = srctoone * dsttotwo * onetotwo;
                        count += butterflyweight;
                        nodeToButterflies_D.addTo(neighbor1, (butterflyweight / weight));
                        nodeToButterflies_S.addTo(neighbor2, (butterflyweight / weight));
                    }
                }
            }

            if (count > 0) {
                double weightSum = count * (1 / weight);
                nodeToButterflies_S.addTo(src, weightSum);
                nodeToButterflies_D.addTo(dst, weightSum);
                globalButterfly += weightSum;
            }

        }

        else if (lowerBound){ // process the deletion with lower bounding

            double count = 0;
            double butterflyweight = 0;
            for (int neighbor1 : srcSet) {
                tempSet = dstToSrcs.get(neighbor1);
                for (int neighbor2 : tempSet) {
                    if (dstSet.contains(neighbor2)) {
                        int srctoone = edgeToIndex.get(((long)src * Integer.MAX_VALUE) + neighbor1).size();
                        int dsttotwo = edgeToIndex.get(((long)neighbor2 * Integer.MAX_VALUE) + dst).size();
                        int onetotwo = edgeToIndex.get(((long)neighbor2 * Integer.MAX_VALUE) + neighbor1).size();
                        butterflyweight = srctoone * dsttotwo * onetotwo;
                        count += butterflyweight;
                        double value = nodeToButterflies_D.addTo(neighbor1, (-butterflyweight / weight));
                        if (value < weight) {
                            nodeToButterflies_D.put(neighbor1, 0);
                        }
                        value = nodeToButterflies_S.addTo(neighbor2, (-butterflyweight / weight));
                        if (value < weight) {
                            nodeToButterflies_S.put(neighbor2, 0);
                        }
                    }
                }
            }

            if (count > 0) {
                final double weightSum = count * (1 / weight);
                double value = nodeToButterflies_S.addTo(src, -weightSum);
                if (value < weightSum) {
                    nodeToButterflies_S.put(src, 0);
                }
                value = nodeToButterflies_D.addTo(dst, -weightSum);
                if (value < weightSum) {
                    nodeToButterflies_D.put(dst, 0);
                }
                globalButterfly -= weightSum;
                globalButterfly = Math.max(0, globalButterfly);
            }

        }

        else { // process the deletion without lower bounding

            double count = 0;
            double butterflyweight = 0;
            for (int neighbor1 : srcSet) {
                tempSet = dstToSrcs.get(neighbor1);
                for (int neighbor2 : tempSet) {
                    if (dstSet.contains(neighbor2)) {
                        int srctoone = edgeToIndex.get(((long)src * Integer.MAX_VALUE) + neighbor1).size();
                        int dsttotwo = edgeToIndex.get(((long)neighbor2 * Integer.MAX_VALUE) + dst).size();
                        int onetotwo = edgeToIndex.get(((long)neighbor2 * Integer.MAX_VALUE) + neighbor1).size();
                    	butterflyweight = srctoone * dsttotwo * onetotwo;
                        count += butterflyweight;
                        nodeToButterflies_D.addTo(neighbor1, (-butterflyweight / weight));
                        nodeToButterflies_S.addTo(neighbor2, (-butterflyweight / weight));
                    }
                }
            }

            if (count > 0) {
                double weightSum = count * (1 / weight);
                nodeToButterflies_S.addTo(src, -weightSum);
                nodeToButterflies_D.addTo(dst, -weightSum);
                globalButterfly += weightSum;
            }

        }

    }

}
