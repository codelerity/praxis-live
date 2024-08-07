/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2024 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 *
 *
 * This file incorporates code from Apache NetBeans Visual Library, covered by
 * the following terms :
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.praxislive.ide.pxr.graph.scene;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 */
class PraxisGraphLayout<N> extends GraphLayout<N, EdgeID<N>> {

    public static final boolean TRACE = false;
    public static final boolean CHECK = false;    // Iterations
    public static final int SWEEP_ITERATIONS = 3;
    public static final int CROSSING_ITERATIONS = 3;    // Options default settings 
    public static final int DUMMY_WIDTH = 10;
    public static final int X_OFFSET = 30;
    public static final int LAYER_OFFSET = 40;    // Options
    private int dummyWidth;
    private int yOffset;
    private int layerOffset;
    private int layerCount;    // Variables
    private PraxisGraphScene<N> graph;
    private List<LayoutNode> nodes;
    private Collection<N> nodesSubset = null;
    private HashMap<N, LayoutNode> vertexToLayoutNode;
    private Set<EdgeID<N>> reversedLinks;
    private List<LayoutNode>[] layers;
    private boolean animate = false;
    private boolean invert = true;

    public PraxisGraphLayout(boolean animate,
            boolean inverted, int xOffset, int layerOffset) {

        dummyWidth = DUMMY_WIDTH;

        // scene is not used yet. It will be used when the container agnostic feature
        // is put into the NBVL
        this.animate = animate;

        if (xOffset > 0) {
            this.yOffset = xOffset;
        } else {
            this.yOffset = X_OFFSET;
        }

        if (layerOffset > 0) {
            this.layerOffset = layerOffset;
        } else {
            this.layerOffset = LAYER_OFFSET;
        }
        
        this.invert = inverted;
    }

    public PraxisGraphLayout(boolean animate, boolean inverted) {
        this(animate, inverted, X_OFFSET, LAYER_OFFSET);
    }

    public PraxisGraphLayout(boolean animate) {
        this(animate, false);
    }

    public PraxisGraphLayout() {
        this(false);
    }

    private class LayoutNode {

        public int x;
        public int y;
        public int width;
        public int height;
        public int layer = -1;
        public int xOffset;
        public int yOffset;
        public int bottomYOffset;
        public N vertex; // Only used for non-dummy nodes, otherwise null
        public List<LayoutEdge> preds = new ArrayList<LayoutEdge>();
        public List<LayoutEdge> succs = new ArrayList<LayoutEdge>();
        public int pos = -1; // Position within layer
        public float crossingNumber;

        public String toString() {
            return "Node " + vertex;
        }
    }

    private class LayoutEdge {

        public LayoutNode from;
        public LayoutNode to;
        public int relativeFrom;
        public int relativeTo;
        public EdgeID<N> link;
        private int indexFrom;
        private int indexTo;
    }

    private abstract class AlgorithmPart {

        public void start() {
            if (CHECK) {
                preCheck();
            }

            long start = 0;
            if (TRACE) {
                System.out.println("##################################################");
                System.out.println("Starting part " + this.getClass().getName());
                start = System.currentTimeMillis();
            }
            
            run();
            
            if (TRACE) {
                System.out.println("Timing for " + this.getClass().getName() + " is " + (System.currentTimeMillis() - start));
                printStatistics();
            }

            if (CHECK) {
                postCheck();
            }
        }

        protected abstract void run();

        protected void printStatistics() {
        }

        protected void postCheck() {
        }

        protected void preCheck() {
        }
    }

    @Override
    protected void performGraphLayout(UniversalGraph<N, EdgeID<N>> graph) {

        this.graph = (PraxisGraphScene<N>) graph.getScene();

        vertexToLayoutNode = new HashMap<N, LayoutNode>();
        reversedLinks = new HashSet<EdgeID<N>>();
        nodes = new ArrayList<LayoutNode>();

        // #############################################################
        // Step 1: Build up data structure
        new BuildDatastructure().start();

        // #############################################################
        // STEP 2: Reverse edges, handle backedges
        new ReverseEdges().start();

        // #############################################################
        // STEP 3: Assign layers
        new AssignLayers().start();

        // #############################################################
        // STEP 4: Create dummy nodes
        new CreateDummyNodes().start();

        // #############################################################
        // STEP 5: Crossing Reduction
        new CrossingReduction().start();

        // #############################################################
        // STEP 7: Assign X coordinates
        //new AssignXCoordinates().start();
        new AssignYCoordinates().start();

        // #############################################################
        // STEP 6: Assign Y coordinates
        new AssignXCoordinates().start();

        // #############################################################
        // STEP 8: Write back to interface
        new WriteResult().start();
    }

    @Override
    protected void performNodesLayout(UniversalGraph<N, EdgeID<N>> arg0, Collection<N> arg1) {

        this.nodesSubset = arg1;
        this.performGraphLayout(arg0);

    }

    private class BuildDatastructure extends AlgorithmPart {

        protected void run() {
            // Set up nodes
            Collection<N> vertices;
            if (nodesSubset == null) {
                vertices = graph.getNodes();
            } else {
                vertices = nodesSubset;
            }
            for (N v : vertices) {
                LayoutNode node = new LayoutNode();
                Widget w = graph.findWidget(v);
                assert w != null;
                Rectangle r = w.getBounds();
                if (r == null) {
                    r = w.getPreferredBounds();
                }
                Dimension size = r.getSize();
                node.width = (int) size.getWidth();
                node.height = (int) size.getHeight();
                node.vertex = v;
                nodes.add(node);
                vertexToLayoutNode.put(v, node);
            }

            // Set up edges
            Collection<EdgeID<N>> links = graph.getEdges();
            for (EdgeID<N> l : links) {
                LayoutEdge edge = new LayoutEdge();
                assert vertexToLayoutNode.containsKey(graph.getEdgeSource(l).getParent());
                assert vertexToLayoutNode.containsKey(graph.getEdgeTarget(l).getParent());

                if (invert) {
                    edge.to = vertexToLayoutNode.get(graph.getEdgeSource(l).getParent());
                    edge.from = vertexToLayoutNode.get(graph.getEdgeTarget(l).getParent());
                } else {
                    edge.from = vertexToLayoutNode.get(graph.getEdgeSource(l).getParent());
                    edge.to = vertexToLayoutNode.get(graph.getEdgeTarget(l).getParent());
                }
                
                Widget w = graph.findWidget(graph.getEdgeSource(l).getParent());

                assert w != null;
                Rectangle r = w.getBounds();
                if (r == null) {
                    r = w.getPreferredBounds();
                }
                Dimension size = r.getSize();
                edge.relativeFrom = size.height / 2;
                edge.indexFrom = new ArrayList<PinID<N>>(graph.getNodePins(graph.getEdgeSource(l).getParent())).indexOf(graph.getEdgeSource(l));
                w = graph.findWidget(graph.getEdgeTarget(l).getParent());
                assert w != null;
                r = w.getBounds();
                if (r == null) {
                    r = w.getPreferredBounds();
                }
                size = r.getSize();
                edge.relativeTo = size.height / 2;
                edge.indexTo = new ArrayList<PinID<N>>(graph.getNodePins(graph.getEdgeTarget(l).getParent())).indexOf(graph.getEdgeTarget(l));
                edge.link = l;

                edge.from.succs.add(edge);
                edge.to.preds.add(edge);

            }
        }

        public void postCheck() {

            assert vertexToLayoutNode.keySet().size() == nodes.size();
            assert nodes.size() == graph.getNodes().size();

            for (N v : graph.getNodes()) {

                LayoutNode node = vertexToLayoutNode.get(v);
                assert node != null;

                for (LayoutEdge e : node.succs) {
                    assert e.from == node;
                }

                for (LayoutEdge e : node.preds) {
                    assert e.to == node;
                }
            }
        }
    }

    private class ReverseEdges extends AlgorithmPart {

        private HashSet<LayoutNode> visited;
        private HashSet<LayoutNode> active;

        protected void run() {

            // Remove self-edges, TODO: Special treatment
            for (LayoutNode node : nodes) {
                ArrayList<LayoutEdge> succs = new ArrayList<LayoutEdge>(node.succs);
                for (LayoutEdge e : succs) {
                    assert e.from == node;
                    if (e.to == node) {
                        node.succs.remove(e);
                        node.preds.remove(e);
                    }
                }
            }

            // Start DFS and reverse back edges
            visited = new HashSet<LayoutNode>();
            active = new HashSet<LayoutNode>();
            for (LayoutNode node : nodes) {
                DFS(node);
            }
        }

        private void DFS(LayoutNode startNode) {
            if (visited.contains(startNode)) {
                return;
            }
            Stack<LayoutNode> stack = new Stack<LayoutNode>();
            stack.push(startNode);

            while (!stack.empty()) {
                LayoutNode node = stack.pop();

                if (visited.contains(node)) {
                    // Node no longer active
                    active.remove(node);
                    continue;
                }

                // Repush immediately to know when no longer active
                stack.push(node);
                visited.add(node);
                active.add(node);

                ArrayList<LayoutEdge> succs = new ArrayList<LayoutEdge>(node.succs);
                for (LayoutEdge e : succs) {
                    if (active.contains(e.to)) {
                        assert visited.contains(e.to);
                        // Encountered back edge
                        reverseEdge(e);
                    } else if (!visited.contains(e.to)) {
                        stack.push(e.to);
                    }
                }
            }
        }

        private void reverseAllInputs(LayoutNode node) {
            for (LayoutEdge e : node.preds) {
                assert !reversedLinks.contains(e.link);
                reversedLinks.add(e.link);
                node.succs.add(e);
                e.from.preds.add(e);
                e.from.succs.remove(e);
                int oldRelativeFrom = e.relativeFrom;
                int oldRelativeTo = e.relativeTo;
                e.to = e.from;
                e.from = node;
                e.relativeFrom = oldRelativeTo;
                e.relativeTo = oldRelativeFrom;
            }
            node.preds.clear();
        }

        private void reverseEdge(LayoutEdge e) {
            assert !reversedLinks.contains(e.link);
            reversedLinks.add(e.link);

            LayoutNode oldFrom = e.from;
            LayoutNode oldTo = e.to;
            int oldRelativeFrom = e.relativeFrom;
            int oldRelativeTo = e.relativeTo;

            e.from = oldTo;
            e.to = oldFrom;
            e.relativeFrom = oldRelativeTo;
            e.relativeTo = oldRelativeFrom;

            oldFrom.succs.remove(e);
            oldFrom.preds.add(e);
            oldTo.preds.remove(e);
            oldTo.succs.add(e);
        }

        public void postCheck() {

            for (LayoutNode n : nodes) {

                Queue<LayoutNode> queue = new LinkedList<LayoutNode>();
                for (LayoutEdge e : n.succs) {
                    LayoutNode s = e.to;
                    queue.add(s);
                    visited.add(s);
                }

                HashSet<LayoutNode> visited = new HashSet<LayoutNode>();
                while (!queue.isEmpty()) {
                    LayoutNode curNode = queue.remove();

                    for (LayoutEdge e : curNode.succs) {
                        assert e.to != n;
                        if (!visited.contains(e.to)) {
                            queue.add(e.to);
                            visited.add(e.to);
                        }
                    }
                }
            }
        }
    }

    private class AssignLayers extends AlgorithmPart {

        public void preCheck() {
            for (LayoutNode n : nodes) {
                assert n.layer == -1;
            }
        }

        protected void run() {
            HashSet<LayoutNode> set = new HashSet<LayoutNode>();
            for (LayoutNode n : nodes) {
                if (n.preds.size() == 0) {
                    set.add(n);
                    n.layer = 0;
                }
            }

            int z = 1;
            HashSet<LayoutNode> newSet = new HashSet<LayoutNode>();
            HashSet<LayoutNode> failed = new HashSet<LayoutNode>();
            while (!set.isEmpty()) {

                newSet.clear();
                failed.clear();

                for (LayoutNode n : set) {

                    for (LayoutEdge se : n.succs) {
                        LayoutNode s = se.to;
                        if (!newSet.contains(s) && !failed.contains(s)) {
                            boolean ok = true;
                            for (LayoutEdge pe : s.preds) {
                                LayoutNode p = pe.from;
                                if (p.layer == -1) {
                                    ok = false;
                                    break;
                                }
                            }

                            if (ok) {
                                newSet.add(s);
                            } else {
                                failed.add(s);
                            }
                        }
                    }

                }

                for (LayoutNode n : newSet) {
                    n.layer = z;
                }

                // Swap sets
                HashSet<LayoutNode> tmp = set;
                set = newSet;
                newSet = tmp;
                z += 1;
            }

            optimize(set);

            layerCount = z - 1;
        }

        public void optimize(HashSet<LayoutNode> set) {

            for (LayoutNode n : set) {
                if (n.preds.size() == 0 && n.succs.size() > 0) {
                    int minLayer = n.succs.get(0).to.layer;
                    for (LayoutEdge e : n.succs) {
                        minLayer = Math.min(minLayer, e.to.layer);
                    }

                    n.layer = minLayer - 1;
                }
            }

        }

        public void printStatistics() {
            //for(LayoutNode n : nodes) {
            //	System.out.println(n + " on layer " + n.layer);
            //}
        }

        public void postCheck() {
            for (LayoutNode n : nodes) {
                assert n.layer >= 0;
                assert n.layer < layerCount;
                for (LayoutEdge e : n.succs) {
                    assert e.from.layer < e.to.layer;
                }
            }
        }
    }

    private class CreateDummyNodes extends AlgorithmPart {

        private int oldNodeCount;

        protected void preCheck() {
            for (LayoutNode n : nodes) {
                for (LayoutEdge e : n.succs) {
                    assert e.from != null;
                    assert e.from == n;
                    assert e.from.layer < e.to.layer;
                }

                for (LayoutEdge e : n.preds) {
                    assert e.to != null;
                    assert e.to == n;
                }
            }
        }

        protected void run() {
            oldNodeCount = nodes.size();
            ArrayList<LayoutNode> currentNodes = new ArrayList<LayoutNode>(nodes);
            for (LayoutNode n : currentNodes) {
                for (LayoutEdge e : n.succs) {
                    processSingleEdge(e);
                }
            }
        }

        private void processSingleEdge(LayoutEdge e) {
            LayoutNode n = e.from;
            if (e.to.layer > n.layer + 1) {
                LayoutEdge last = e;
                for (int i = n.layer + 1; i < last.to.layer; i++) {
                    last = addBetween(last, i);
                }
            }
        }

        private LayoutEdge addBetween(LayoutEdge e, int layer) {
            LayoutNode n = new LayoutNode();
            n.width = 0;
            n.height = dummyWidth;
            n.layer = layer;
            n.preds.add(e);
            nodes.add(n);
            LayoutEdge result = new LayoutEdge();
            n.succs.add(result);
            result.from = n;
            result.relativeFrom = n.height / 2;
            result.to = e.to;
            result.relativeTo = e.relativeTo;
            e.relativeTo = n.height / 2;
            e.to.preds.remove(e);
            e.to.preds.add(result);
            e.to = n;
            return result;
        }

        public void printStatistics() {
            System.out.println("Dummy nodes created: " + (nodes.size() - oldNodeCount));
        }

        public void postCheck() {
            ArrayList<LayoutNode> currentNodes = new ArrayList<LayoutNode>(nodes);
            for (LayoutNode n : currentNodes) {
                for (LayoutEdge e : n.succs) {
                    assert e.from.layer == e.to.layer - 1;
                }
            }

            for (int i = 0; i < layers.length; i++) {
                assert layers[i].size() > 0;
                for (LayoutNode n : layers[i]) {
                    assert n.layer == i;
                }
            }
        }
    }
    private Comparator<LayoutNode> crossingNodeComparator = new Comparator<LayoutNode>() {

        public int compare(LayoutNode n1, LayoutNode n2) {
            float f = n1.crossingNumber - n2.crossingNumber;
            if (f < 0) {
                return -1;
            } else if (f > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    private class CrossingReduction extends AlgorithmPart {

        public void preCheck() {
            for (LayoutNode n : nodes) {
                assert n.layer < layerCount;
            }
        }

        protected void run() {

            layers = new List[layerCount];

            for (int i = 0; i < layerCount; i++) {
                layers[i] = new ArrayList<LayoutNode>();
            }


            // Generate initial ordering
            HashSet<LayoutNode> visited = new HashSet<LayoutNode>();
            for (LayoutNode n : nodes) {
                if (n.layer == 0) {
                    layers[0].add(n);
                    visited.add(n);
                } else if (n.preds.size() == 0) {
                    layers[n.layer].add(n);
                    visited.add(n);
                }
            }

            for (int i = 0; i < layers.length - 1; i++) {
                for (LayoutNode n : layers[i]) {
                    for (LayoutEdge e : n.succs) {
                        if (!visited.contains(e.to)) {
                            visited.add(e.to);
                            layers[i + 1].add(e.to);
                        }
                    }
                }
            }


            updatePositions();
//
            // Optimize
            for (int i = 0; i < CROSSING_ITERATIONS; i++) {
                upSweep();
                downSweep();
            }
        }

        private void updatePositions() {

            for (int i = 0; i < layers.length; i++) {
                int z = 0;
                for (LayoutNode n : layers[i]) {
                    n.pos = z;
                    z++;
                }
            }
        }

        private void downSweep() {

            // Downsweep
            for (int i = 1; i < layerCount; i++) {

                for (LayoutNode n : layers[i]) {

                    float sum = 0.0f;
                    for (LayoutEdge e : n.preds) {
                        float cur = e.from.pos;
                        if (e.from.height != 0 && e.relativeFrom != 0) {
                            cur += (float) e.relativeFrom / (float) (e.from.height);
                        }

                        sum += cur;
                    }

                    if (n.preds.size() > 0) {
                        sum /= n.preds.size();
                        n.crossingNumber = sum;
                    //if(n.vertex == null) n.crossingNumber += layers[i].size();
                    } else {
                        n.crossingNumber = Integer.MAX_VALUE;
                    }
                }

                Collections.sort(layers[i], crossingNodeComparator);

                int z = 0;
                for (LayoutNode n : layers[i]) {
                    n.pos = z;
                    z++;
                }
            }
        }

        private void upSweep() {
            // Upsweep
            for (int i = layerCount - 1; i >= 0; i--) {

                for (LayoutNode n : layers[i]) {

                    float sum = 0.0f;
                    for (LayoutEdge e : n.succs) {
                        float cur = e.to.pos;
                        if (e.to.height != 0 && e.relativeTo != 0) {
                            cur += ((float) e.relativeTo - ((float)(e.indexTo * 20))) / (float) (e.to.height);
                        }

                        sum += cur;
                    }

                    if (n.succs.size() > 0) {
                        sum /= n.succs.size();
                        n.crossingNumber = sum;
                    //if(n.vertex == null) n.crossingNumber += layers[i].size();
                    } else {
                        n.crossingNumber = Integer.MAX_VALUE;
                    }

                }

                Collections.sort(layers[i], crossingNodeComparator);

                int z = 0;
                for (LayoutNode n : layers[i]) {
                    n.pos = z;
                    z++;
                }
            }
        }

        private int evaluate() {
            // TODO: Implement efficient evaluate / crossing min
            return 0;
        }

        public void postCheck() {

            HashSet<LayoutNode> visited = new HashSet<LayoutNode>();
            for (int i = 0; i < layers.length; i++) {
                for (LayoutNode n : layers[i]) {
                    assert !visited.contains(n);
                    assert n.layer == i;
                    visited.add(n);
                }
            }

        }
    }
    private final Comparator<LayoutNode> nodePositionComparator = new Comparator<LayoutNode>() {

        @Override
        public int compare(LayoutNode n1, LayoutNode n2) {
            int res = n1.pos - n2.pos;
            if (res == 0) {
                res = n1.toString().compareTo(n2.toString());
//                if (res == 0) {
//                    res = System.identityHashCode(n1) - System.identityHashCode(n2);
//                }
            }
            return res;
        }
    };
    private final Comparator<LayoutNode> nodeProcessingDownComparator = new Comparator<LayoutNode>() {

        @Override
        public int compare(LayoutNode n1, LayoutNode n2) {
            if (n1.vertex == null && n2.vertex == null) {
                int res = n1.toString().compareTo(n2.toString());
//                if (res == 0) {
//                    res = System.identityHashCode(n1) - System.identityHashCode(n2);
//                }
                return res;
            }
            if (n1.vertex == null) {
                return -1;
            }
            if (n2.vertex == null) {
                return 1;
            }
            int res = n1.preds.size() - n2.preds.size();
            if (res == 0 && !n1.preds.isEmpty()) {
                res = res = n1.toString().compareTo(n2.toString());
//                if (res == 0) {
//                    res = System.identityHashCode(n1) - System.identityHashCode(n2);
//                }
            }
            return res;
        }
    };
    private final Comparator<LayoutNode> nodeProcessingUpComparator = new Comparator<LayoutNode>() {

        @Override
        public int compare(LayoutNode n1, LayoutNode n2) {
            if (n1.vertex == null && n2.vertex == null) {
                int res = n1.toString().compareTo(n2.toString());
//                if (res == 0) {
//                    res = System.identityHashCode(n1) - System.identityHashCode(n2);
//                }
                return res;
            }
            if (n1.vertex == null) {
                return -1;
            }
            if (n2.vertex == null) {
                return 1;
            }
            int res = n1.succs.size() - n2.succs.size();
            if (res == 0) {
                res = n1.toString().compareTo(n2.toString());
//                if (res == 0) {
//                    res = System.identityHashCode(n1) - System.identityHashCode(n2);
//                }
            }
            return res;
        }
    };

    private class AssignYCoordinates extends AlgorithmPart {

        private ArrayList<Integer>[] space;
        private ArrayList<LayoutNode>[] downProcessingOrder;
        private ArrayList<LayoutNode>[] upProcessingOrder;

        private void initialPositions() {
            for (LayoutNode n : nodes) {
                n.y = space[n.layer].get(n.pos);
            }
        }

        protected void run() {

            space = new ArrayList[layers.length];
            downProcessingOrder = new ArrayList[layers.length];
            upProcessingOrder = new ArrayList[layers.length];

            for (int i = 0; i < layers.length; i++) {
                space[i] = new ArrayList<Integer>();
                downProcessingOrder[i] = new ArrayList<LayoutNode>();
                upProcessingOrder[i] = new ArrayList<LayoutNode>();

                int curY = 0;
                for (LayoutNode n : layers[i]) {
                    space[i].add(curY);
                    curY += n.height + yOffset;
                    downProcessingOrder[i].add(n);
                    upProcessingOrder[i].add(n);
                }

                Collections.sort(downProcessingOrder[i], nodeProcessingDownComparator);
                Collections.sort(upProcessingOrder[i], nodeProcessingUpComparator);
            }

            initialPositions();
            for (int i = 0; i < SWEEP_ITERATIONS; i++) {
//                sweepDown();
//                sweepUp();
//                sweepDown();
            }
//
//            sweepDown();
//            sweepUp();
        }

        private int calculateOptimalDown(LayoutNode n) {

            List<Integer> values = new ArrayList<Integer>();
            if (n.preds.size() == 0) {
                return n.y;
            }
            for (LayoutEdge e : n.preds) {
                int cur = e.from.y + e.relativeFrom - e.relativeTo;
                values.add(cur);
            }
            return median(values);
        }

        private int calculateOptimalUp(LayoutNode n) {

            List<Integer> values = new ArrayList<Integer>();
            if (n.succs.size() == 0) {
                return n.y;
            }
            for (LayoutEdge e : n.succs) {
                int cur = e.to.y + e.relativeTo - e.relativeFrom;
                values.add(cur);
            }
            return median(values);
        }

        private int median(List<Integer> values) {
            Collections.sort(values);
            if (values.size() % 2 == 0) {
                return (values.get(values.size() / 2 - 1) + values.get(values.size() / 2)) / 2;
            } else {
                return values.get(values.size() / 2);
            }
        }

        private void sweepUp() {
            for (int i = layers.length - 2; i >= 0; i--) {
                NodeColumn r = new NodeColumn(space[i]);
                for (LayoutNode n : upProcessingOrder[i]) {
                    int optimal = calculateOptimalUp(n);
                    r.insert(n, optimal);
                }
            }
        }

        private void sweepDown() {
            for (int i = 1; i < layers.length; i++) {
                NodeColumn r = new NodeColumn(space[i]);
                for (LayoutNode n : downProcessingOrder[i]) {
                    int optimal = calculateOptimalDown(n);
                    r.insert(n, optimal);
                }
            }
        }
    }

    private class NodeColumn {

        private TreeSet<LayoutNode> treeSet;
        private ArrayList<Integer> space;

        public NodeColumn(ArrayList<Integer> space) {
            treeSet = new TreeSet<LayoutNode>(nodePositionComparator);
            this.space = space;
        }

        public int offset(LayoutNode n1, LayoutNode n2) {
            int v1 = space.get(n1.pos) + n1.height;
            int v2 = space.get(n2.pos);
            return v2 - v1;
        }

        public void insert(LayoutNode n, int pos) {

            SortedSet<LayoutNode> headSet = treeSet.headSet(n);
            SortedSet<LayoutNode> tailSet = treeSet.tailSet(n);

            LayoutNode topNeighbour = null;
            int minY = Integer.MIN_VALUE;
            if (!headSet.isEmpty()) {
                topNeighbour = headSet.last();
                minY = topNeighbour.y + topNeighbour.height + offset(topNeighbour, n);
            }

            LayoutNode bottomNeighbour = null;
            int maxY = Integer.MAX_VALUE;
            if (!tailSet.isEmpty()) {
                bottomNeighbour = tailSet.first();
                maxY = bottomNeighbour.y - offset(n, bottomNeighbour) - n.height;
            }

            assert minY <= maxY;

            if (pos >= minY && pos <= maxY) {
                n.y = pos;
            } else if (Math.abs((long) pos - (long) minY) < Math.abs((long) pos - (long) maxY)) {
                assert minY != Integer.MIN_VALUE;
                n.y = minY;
            } else {
                assert maxY != Integer.MAX_VALUE;
                n.y = maxY;
            }

            treeSet.add(n);
        }
    }

    private class AssignXCoordinates extends AlgorithmPart {

        protected void run() {
            int curX = 0;
            //maxLayerHeight = new int[layers.length];
            for (int i = 0; i < layers.length; i++) {
                int maxWidth = 0;
                int baseLine = 0;
                int bottomBaseLine = 0;
                for (LayoutNode n : layers[i]) {
                    maxWidth = Math.max(maxWidth, n.width - n.xOffset - n.bottomYOffset);
                    baseLine = Math.max(baseLine, n.xOffset);
//                    bottomBaseLine = Math.max(bottomBaseLine, n.bottomYOffset);
                }

                for (LayoutNode n : layers[i]) {
                    if (n.vertex == null) {
                        // Dummy node => set height to line height
                        n.x = curX;
                        n.width = maxWidth + baseLine + bottomBaseLine;
                    } else {
                        n.x = curX + baseLine + (maxWidth - (n.width - n.xOffset - n.bottomYOffset)) / 2 - n.xOffset;
                    }
                }

                curX += maxWidth + baseLine + bottomBaseLine;
                curX += layerOffset;
            }
        }
    }

    private class WriteResult extends AlgorithmPart {

        private int pointCount;

        protected void run() {

            HashMap<N, Point> vertexPositions = new HashMap<N, Point>();
            HashMap<EdgeID<N>, List<Point>> linkPositions = new HashMap<EdgeID<N>, List<Point>>();
            for (N v : graph.getNodes()) {
                LayoutNode n = vertexToLayoutNode.get(v);
                assert !vertexPositions.containsKey(v);
                vertexPositions.put(v, new Point(n.x + n.xOffset, n.y + n.yOffset));
            }

//            for (LayoutNode n : nodes) {
//
//                for (LayoutEdge e : n.succs) {
//                    if (e.link != null) {
//                        E link = e.link;
//                        ArrayList<Point> points = new ArrayList<Point>();
//
//                        Point p = new Point(e.from.x + e.relativeFrom, e.from.y + e.from.height - e.from.bottomYOffset);
//                        points.add(p);
//
//                        LayoutNode cur = e.to;
//                        LayoutNode other = e.from;
//                        LayoutEdge curEdge = e;
//                        while (cur.vertex == null && cur.succs.size() != 0) {
//                            //if(points.size() > 1 && points.get(points.size() -1).x == cur.x + cur.width/2 && points.get(points.size() - 2).x == cur.x + cur.width/2) {
//                            //	points.remove(points.size() - 1);
//                            //}
//
//                            points.add(new Point(cur.x + cur.width / 2, cur.y));
//                            //if(points.size() > 1 && points.get(points.size() -1).x == cur.x + cur.width/2 && points.get(points.size() - 2).x == cur.x + cur.width/2) {
//                            //	points.remove(points.size() - 1);
//                            //	}
//                            points.add(new Point(cur.x + cur.width / 2, cur.y + cur.height));
//                            if (cur.succs.size() == 0) {
//                                break;
//                            }
//                            assert cur.succs.size() == 1;
//                            curEdge = cur.succs.get(0);
//                            cur = curEdge.to;
//                        }
//
//                        p = new Point(cur.x + curEdge.relativeTo, cur.y + cur.yOffset);
//                        points.add(p);
//
//                        if (reversedLinks.contains(link)) {
//                            Collections.reverse(points);
//                        }
//
//                        linkPositions.put(e.link, points);
//
//                        // No longer needed!
//                        e.link = null;
//                    }
//                }
//            }

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            for (N v : vertexPositions.keySet()) {
                Point p = vertexPositions.get(v);
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
            }

//            for (E l : linkPositions.keySet()) {
//                List<Point> points = linkPositions.get(l);
//                for (Point p : points) {
//                    if (p != null) {
//                        minX = Math.min(minX, p.x);
//                        minY = Math.min(minY, p.y);
//                    }
//                }
//
//            }

            for (N v : vertexPositions.keySet()) {
                Point p = vertexPositions.get(v);
                p.x -= minX;
                p.y -= minY;
                p.x += 50;
                p.y += 50;
                Widget w = graph.findWidget(v);
                if (animate) {
                    graph.getSceneAnimator().animatePreferredLocation(w, p);
                } else {
                    w.setPreferredLocation(p);
                }
            }

//            for (E l : linkPositions.keySet()) {
//                List<Point> points = linkPositions.get(l);
//
//                for (Point p : points) {
//                    if (p != null) {
//                        p.x -= minX;
//                        p.y -= minY;
//                    }
//                }
//
//                //Kris - this is a hack to reverse the order of the control points
//                // that were created by the algorithm. This is used when the graph
//                // is inverted.
//                if (invert && points.size() > 3) {
//                    int numPoints = points.size();
//                    ArrayList<Point> invertedPoints = new ArrayList<Point>(numPoints);
//
//                    invertedPoints.add(points.get(0));
//
//                    for (int i = numPoints - 2; i > 0; i--) {
//                        invertedPoints.add(points.get(i));
//                    }
//
//                    invertedPoints.add(points.get(numPoints - 1));
//
//                    points = invertedPoints;
//                }
//
//                Widget w = graph.getScene().findWidget(l);
//                if (w instanceof ConnectionWidget) {
//                    ConnectionWidget cw = (ConnectionWidget) w;
//                    cw.setControlPoints(points, true);
//                }
//
//            }
            
            graph.getScene().validate();
            graph.getScene().repaint();
            graph.getScene().revalidate();
        }

        protected void printStatistics() {
            System.out.println("Number of nodes: " + nodes.size());
            int edgeCount = 0;
            for (LayoutNode n : nodes) {
                edgeCount += n.succs.size();
            }
            System.out.println("Number of edges: " + edgeCount);
            System.out.println("Number of points: " + pointCount);
        }
    }
}
