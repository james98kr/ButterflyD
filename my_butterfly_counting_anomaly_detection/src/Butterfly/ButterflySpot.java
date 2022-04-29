package butterfly;


import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

import java.util.LinkedList;
import java.util.Queue;


public class ButterflySpot {

    /**
     * size of the time window in seconds﻿ during which edges are maintained
     */
    private int timeWindow;

    /**
     * butterfly counting module
     */
    private final ButterflyAcc module;

    /**
     * list of (timestamp when an edge should be removed, the source and destination of the edge)
     */
    private Queue<Pair<Long, int[]>> deleteQueue = new LinkedList();

    /**
     * total sum of global butterflies & total number of edges processed
     */
    private double sum_count;
    private double sum_log_count;
    private double numofedge;
    private double numoftrue;
    private double threshold;
    private double prevcount;
    private int alerton;

    /**
     * @param memoryBudget maximum number of sampled edges
     * @param timeWindow size of the time window in seconds﻿ during which edges are maintained
     * @param seed random seed
     */
    public ButterflySpot(final int memoryBudget, final int timeWindow, final int seed){
        this.module = new ButterflyAcc(memoryBudget, seed);
        this.timeWindow = timeWindow;
        this.sum_count = 0;
        this.sum_log_count = 0;
        this.numofedge = 0;
        this.numoftrue = 0;
        this.threshold = 1;
        this.prevcount = 0;
        this.alerton = 0;
    }

    /**
     * Process an edge with a timestamp
     * @param src source node of the given edge
     * @param dst destination node of the given edge
     * @param timestamp timestamp of the edge in milliseconds
     * @return whether the current estimated count of global butterfly exceeds condition of distribution
     */
    public boolean process(int src, int dst, long timestamp) {

        while(!deleteQueue.isEmpty() && deleteQueue.peek().getKey() <= timestamp) {
            Pair<Long, int[]> pair = deleteQueue.poll();
            int[] entryToDelete = pair.getValue();
            module.processEdge(entryToDelete[0], entryToDelete[1], false);
        }
        module.processEdge(src, dst, true);
        deleteQueue.add(new Pair<Long, int[]>(timestamp + timeWindow, new int[]{src, dst}));


        double current_count = module.getGlobalButterfly();
        double s = 0;
        double shape = 0;
        double scale = 0;
        numofedge += 1;
        sum_count += current_count;
        GammaDistribution gamma = new GammaDistribution(1, 1);
        
        if (prevcount == current_count) {
            return false;
        }
        
        if (current_count != 0) {
        	sum_log_count += Math.log(current_count);
            if (sum_count != 0 && sum_log_count != 0) {
            	s = Math.log(sum_count / numofedge) - sum_log_count / numofedge;
                shape = (3 - s + Math.sqrt((s-3)*(s-3) + 24*s)) / (12 * s);
                scale = sum_count / numofedge / shape;
                if (shape > 0 && scale > 0) {
                    gamma = new GammaDistribution(shape, scale);
                }
                else {
                    return false;
                }
                if (gamma.cumulativeProbability(current_count) > (1-Math.pow(10,-threshold))) {
                	prevcount = current_count;
                	numoftrue += 1;
                	if (numoftrue/numofedge < 0.05 && threshold > 1){
                		threshold -= 1;
                	}
                	else if (numoftrue/numofedge >= 0.05){
                		threshold += 1;
                	}

                	return true;
                }
                else {
                	prevcount = current_count;
                	return false;
                }
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    /**
     * Get estimated global butterfly count in the maintained graph.
     * Spikes in this estimated count tend to indicate the occurrence of sudden dense subgraphs
     * @return estimate of global butterfly count
     */
    public double getGlobalButterfly() {
        return module.getGlobalButterfly();
    }

    /**
     * Get estimated local butterfly counts in the node partition src of maintained graph.
     * These estimated counts tend to indicate the contribution of each node to sudden dense subgraph
     * @return estimates of local butterfly counts
     */
    public Int2DoubleOpenHashMap getLocalButterfly_S() {
        return module.getLocalButterfly_S();
    }

    public double mean(){
        return sum_count / numofedge;
    }
    public double numofedgereturn(){
        return numofedge;
    }

    public double threshold(){
    	return threshold;
    }

    /**
     * Get estimated local butterfly counts in the node partition dst of maintained graph.
     * These estimated counts tend to indicate the contribution of each node to sudden dense subgraph
     * @return estimates of local butterfly counts
     */
    public Int2DoubleOpenHashMap getLocalButterfly_D() {
        return module.getLocalButterfly_D();
    }

}