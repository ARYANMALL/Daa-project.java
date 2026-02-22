

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class MSTVisualizer extends JFrame {
    private static final int NODE_RADIUS = 20;
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;
    
    private JPanel canvas;
    private JTextArea resultArea;
    private JComboBox<String> algorithmChoice;
    
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private Node selectedNode = null;
    private Node firstNode = null;
    private boolean isAddingEdge = false;
    private JTextField edgeNameField, weightField;
    private Node startNode = null;

    public MSTVisualizer() {
        setTitle("Minimum Spanning Tree Visualizer");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Canvas for drawing graph
        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph(g);
            }
        };
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        canvas.setBackground(Color.WHITE);
        canvas.addMouseListener(new GraphMouseListener());
        canvas.addMouseMotionListener(new GraphMouseMotionListener());
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton addNodeBtn = new JButton("Add Node");
        JButton addEdgeBtn = new JButton("Add Edge");
        JButton runAlgorithmBtn = new JButton("Run Algorithm");
        JButton clearBtn = new JButton("Clear All");
        
        algorithmChoice = new JComboBox<>(new String[]{"Kruskal's Algorithm", "Prim's Algorithm"});
        
        addNodeBtn.addActionListener(e -> isAddingEdge = false);
        addEdgeBtn.addActionListener(e -> isAddingEdge = true);
        runAlgorithmBtn.addActionListener(e -> runAlgorithm());
        clearBtn.addActionListener(e -> clearAll());
        
        controlPanel.add(addNodeBtn);
        controlPanel.add(addEdgeBtn);
        controlPanel.add(new JLabel("Algorithm:"));
        controlPanel.add(algorithmChoice);
        controlPanel.add(runAlgorithmBtn);
        controlPanel.add(clearBtn);
        
        // Edge input fields
        JPanel edgeInputPanel = new JPanel();
        edgeInputPanel.add(new JLabel("Edge Name:"));
        edgeNameField = new JTextField(3);
        edgeInputPanel.add(edgeNameField);
        edgeInputPanel.add(new JLabel("Weight:"));
        weightField = new JTextField(3);
        edgeInputPanel.add(weightField);
        
        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        
        // Main layout
        add(controlPanel, BorderLayout.NORTH);
        add(edgeInputPanel, BorderLayout.SOUTH);
        add(canvas, BorderLayout.CENTER);
        add(resultScroll, BorderLayout.EAST);
    }
    
    private void drawGraph(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw edges first
        for (Edge edge : edges) {
            drawEdge(g2d, edge);
        }
        
        // Then draw nodes
        for (Node node : nodes) {
            drawNode(g2d, node);
        }
        
        // Draw current edge being created
        if (firstNode != null && selectedNode != null && isAddingEdge) {
            g2d.setColor(Color.GRAY);
            g2d.drawLine(firstNode.x, firstNode.y, selectedNode.x, selectedNode.y);
        }
    }
    
    private void drawNode(Graphics2D g, Node node) {
        // Draw node circle
        if (node == startNode) {
            g.setColor(Color.GREEN);
        } else if (node.inMST) {
            g.setColor(Color.CYAN);
        } else {
            g.setColor(Color.BLUE);
        }
        g.fillOval(node.x - NODE_RADIUS, node.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        
        // Draw node border
        g.setColor(Color.BLACK);
        g.drawOval(node.x - NODE_RADIUS, node.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        
        // Draw node label
        g.drawString(node.name, node.x - 5, node.y + 5);
    }
    
    private void drawEdge(Graphics2D g, Edge edge) {
        Node from = edge.from;
        Node to = edge.to;
        
        // Draw line
        if (edge.inMST) {
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(3));
        } else {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1));
        }
        g.drawLine(from.x, from.y, to.x, to.y);
        
        // Draw weight
        g.setColor(Color.BLACK);
        int midX = (from.x + to.x) / 2;
        int midY = (from.y + to.y) / 2;
        g.drawString(edge.name + ":" + edge.weight, midX, midY);
    }
    
    private void runAlgorithm() {
        if (nodes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please create a graph first!");
            return;
        }
        
        // Reset MST flags
        for (Node node : nodes) {
            node.inMST = false;
        }
        for (Edge edge : edges) {
            edge.inMST = false;
        }
        
        String selectedAlgorithm = (String) algorithmChoice.getSelectedItem();
        List<Edge> mstEdges = new ArrayList<>();
        
        if (selectedAlgorithm.equals("Kruskal's Algorithm")) {
            mstEdges = runKruskal();
        } else {
            mstEdges = runPrim();
        }
        
        // Mark edges in MST
        for (Edge mstEdge : mstEdges) {
            mstEdge.inMST = true;
            mstEdge.from.inMST = true;
            mstEdge.to.inMST = true;
        }
        
        // Display results
        displayResults(mstEdges);
        canvas.repaint();
    }
    
    private List<Edge> runKruskal() {
        List<Edge> mstEdges = new ArrayList<>();
        Collections.sort(edges, Comparator.comparingInt(e -> e.weight));
        
        UnionFind uf = new UnionFind(nodes.size());
        Map<Node, Integer> nodeIndex = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            nodeIndex.put(nodes.get(i), i);
        }
        
        for (Edge edge : edges) {
            int u = nodeIndex.get(edge.from);
            int v = nodeIndex.get(edge.to);
            
            if (uf.find(u) != uf.find(v)) {
                uf.union(u, v);
                mstEdges.add(edge);
                
                if (mstEdges.size() == nodes.size() - 1) {
                    break;
                }
            }
        }
        
        return mstEdges;
    }
    
    private List<Edge> runPrim() {
        List<Edge> mstEdges = new ArrayList<>();
        if (nodes.isEmpty()) return mstEdges;
        
        // Set first node as start node if not set
        if (startNode == null) {
            startNode = nodes.get(0);
        }
        
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));
        Set<Node> inMST = new HashSet<>();
        inMST.add(startNode);
        
        // Add all edges from start node to priority queue
        for (Edge edge : edges) {
            if (edge.from == startNode || edge.to == startNode) {
                pq.add(edge);
            }
        }
        
        while (!pq.isEmpty() && mstEdges.size() < nodes.size() - 1) {
            Edge edge = pq.poll();
            Node newNode = null;
            
            if (inMST.contains(edge.from) && !inMST.contains(edge.to)) {
                newNode = edge.to;
            } else if (inMST.contains(edge.to) && !inMST.contains(edge.from)) {
                newNode = edge.from;
            }
            
            if (newNode != null) {
                mstEdges.add(edge);
                inMST.add(newNode);
                
                // Add all edges from new node to priority queue
                for (Edge e : edges) {
                    if ((e.from == newNode || e.to == newNode) && 
                        !(inMST.contains(e.from) && inMST.contains(e.to))) {
                        pq.add(e);
                    }
                }
            }
        }
        
        return mstEdges;
    }
    
    private void displayResults(List<Edge> mstEdges) {
        StringBuilder sb = new StringBuilder();
        int totalWeight = 0;
        
        sb.append("Minimum Spanning Tree Edges:\n");
        for (Edge edge : mstEdges) {
            sb.append(edge.from.name)
              .append(" -- ")
              .append(edge.to.name)
              .append(" (Weight: ")
              .append(edge.weight)
              .append(")\n");
            totalWeight += edge.weight;
        }
        
        sb.append("\nTotal Weight: ").append(totalWeight);
        resultArea.setText(sb.toString());
    }
    
    private void clearAll() {
        nodes.clear();
        edges.clear();
        selectedNode = null;
        firstNode = null;
        startNode = null;
        resultArea.setText("");
        canvas.repaint();
    }
    
    private Node getNodeAt(int x, int y) {
        for (Node node : nodes) {
            if (Math.sqrt(Math.pow(node.x - x, 2) + Math.pow(node.y - y, 2)) <= NODE_RADIUS) {
                return node;
            }
        }
        return null;
    }
    
    // Helper class for Union-Find (Disjoint Set Union)
    private class UnionFind {
        private int[] parent;
        
        public UnionFind(int size) {
            parent = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }
        
        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }
        
        public void union(int x, int y) {
            parent[find(x)] = find(y);
        }
    }
    
    private class GraphMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                // Right click to set start node for Prim's
                Node node = getNodeAt(e.getX(), e.getY());
                if (node != null) {
                    startNode = node;
                    canvas.repaint();
                }
                return;
            }
            
            if (!isAddingEdge) {
                // Add new node
                String name = JOptionPane.showInputDialog("Enter node name (A, B, C, ...):");
                if (name != null && !name.trim().isEmpty()) {
                    nodes.add(new Node(e.getX(), e.getY(), name));
                    canvas.repaint();
                }
            } else {
                // Add edge between nodes
                Node node = getNodeAt(e.getX(), e.getY());
                if (node != null) {
                    if (firstNode == null) {
                        firstNode = node;
                    } else if (node != firstNode) {
                        String edgeName = edgeNameField.getText().trim();
                        String weightStr = weightField.getText().trim();
                        
                        if (edgeName.isEmpty() || weightStr.isEmpty()) {
                            JOptionPane.showMessageDialog(null, 
                                "Please enter both edge name and weight!");
                            return;
                        }
                        
                        try {
                            int weight = Integer.parseInt(weightStr);
                            edges.add(new Edge(firstNode, node, edgeName, weight));
                            firstNode = null;
                            edgeNameField.setText("");
                            weightField.setText("");
                            canvas.repaint();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, 
                                "Please enter a valid integer weight.");
                        }
                    }
                }
            }
        }
    }
    
    private class GraphMouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            selectedNode = getNodeAt(e.getX(), e.getY());
            canvas.repaint();
        }
    }
    
    private static class Node {
        int x, y;
        String name;
        boolean inMST = false;
        
        Node(int x, int y, String name) {
            this.x = x;
            this.y = y;
            this.name = name;
        }
    }
    
    private static class Edge {
        Node from, to;
        String name;
        int weight;
        boolean inMST = false;
        
        Edge(Node from, Node to, String name, int weight) {
            this.from = from;
            this.to = to;
            this.name = name;
            this.weight = weight;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MSTVisualizer gui = new MSTVisualizer();
            gui.setVisible(true);
        });
    }
}