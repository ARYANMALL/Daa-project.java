
// Matrix-Chain Multiplication GUI Solution
import java.awt.*;
import javax.swing.*;

public class MatrixChainGUI extends JFrame {
    private JTextField inputField;
    private JTextArea resultArea;
    
    public MatrixChainGUI() {
        setTitle("Matrix Chain Multiplication");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Matrix Dimensions:"));
        inputField = new JTextField("30,35,15,5,10,20,25", 25);
        inputPanel.add(inputField);
        
        JButton calculateBtn = new JButton("Calculate");
        calculateBtn.addActionListener(e -> calculate());
        inputPanel.add(calculateBtn);
        
        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        // Layout
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
    }
    
    private void calculate() {
        try {
            String[] dims = inputField.getText().split(",");
            int[] p = new int[dims.length];
            for (int i = 0; i < dims.length; i++) {
                p[i] = Integer.parseInt(dims[i].trim());
            }
            
            int n = p.length - 1;
            int[][] m = new int[n+1][n+1];
            int[][] s = new int[n+1][n+1];
            
            for (int l = 2; l <= n; l++) {
                for (int i = 1; i <= n-l+1; i++) {
                    int j = i+l-1;
                    m[i][j] = Integer.MAX_VALUE;
                    for (int k = i; k < j; k++) {
                        int q = m[i][k] + m[k+1][j] + p[i-1]*p[k]*p[j];
                        if (q < m[i][j]) {
                            m[i][j] = q;
                            s[i][j] = k;
                        }
                    }
                }
            }
            
            displayResults(m, s, n);
        } catch (Exception ex) {
            resultArea.setText("Invalid input format");
        }
    }
    
    private void displayResults(int[][] m, int[][] s, int n) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("m Table (Minimum Multiplications):\n");
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                if (j < i) sb.append("      ");
                else sb.append(String.format("%6d", m[i][j]));
            }
            sb.append("\n");
        }
        
        sb.append("\ns Table (Split Points):\n");
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                if (j < i) sb.append("      ");
                else sb.append(String.format("%6d", s[i][j]));
            }
            sb.append("\n");
        }
        
        sb.append("\nOptimal Parenthesization: ");
        sb.append(getParenthesization(s, 1, n));
        
        resultArea.setText(sb.toString());
    }
    
    private String getParenthesization(int[][] s, int i, int j) {
        if (i == j) return "A"+i;
        return "(" + getParenthesization(s, i, s[i][j]) + "×" + 
               getParenthesization(s, s[i][j]+1, j) + ")";
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MatrixChainGUI().setVisible(true);
        });
    }
}