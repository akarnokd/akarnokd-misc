package hu.akarnokd.misc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BoxOColor {
    public static class Board extends JPanel {
        
        @Override
        public void paint(Graphics g) 
        {
           g.setColor(Color.black); 
           g.fillRect(0, 0, 420, 500); 
           
        }
    }
    public static class Game {

        /**
         * @param args the command line arguments
         */
        
        Board board = new Board(); 
        
        public Game(){
            JFrame frame = new JFrame(); 
            frame.setSize(420,500); 
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
            frame.setLocationRelativeTo(null); 
            frame.setResizable(false); 
            frame.add(board,BorderLayout.CENTER); 
            frame.setTitle("Pac-Man"); 
            frame.setVisible(true); 
        }
        
    }
    public static void main(String[] args) {
        // TODO code application logic here
        new Game(); 
        
    }
}
