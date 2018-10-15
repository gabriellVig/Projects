import java.awt.*;       // Using AWT's Graphics and Color
import java.awt.event.*; // Using AWT event classes and listener interfaces
import javax.swing.*;    // Using Swing's components and containers

import java.util.Random;

/** Custom Drawing Code Template */
// A Swing application extends javax.swing.JFrame
public class Tegneprogram extends JFrame {
   // Define constants
   public static final int CANVAS_WIDTH  = 720;
   public static final int CANVAS_HEIGHT = 720;

   // Declare an instance of the drawing canvas,
   // which is an inner class called DrawCanvas extending javax.swing.JPanel.
   private DrawCanvas canvas;

   // Constructor to set up the GUI components and event handlers
   public Tegneprogram() {
      canvas = new DrawCanvas();    // Construct the drawing canvas
      canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

      // Set the Drawing JPanel as the JFrame's content-pane
      Container cp = getContentPane();
      cp.add(canvas);
      // or "setContentPane(canvas);"

      setDefaultCloseOperation(EXIT_ON_CLOSE);   // Handle the CLOSE button
      pack();              // Either pack() the components; or setSize()
      setTitle("......");  // "super" JFrame sets the title
      setVisible(true);    // "super" JFrame show
   }

   private class Line{
     final int x1;
     final int y1;
     final int x2;
     final int y2;
     final int[] center;

     public Line(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.center = new int[] {(x1 + x2)/2, (y1 + y2)/2};
      }
    // X = (x_1 + x_2)/2
    // Y = (y_1 + y_2)/2
    public int[] getCenter(){
      // int[] x_y = new int[2];
      // x_y[0] = (x1 + x2)/2;
      // x_y[1] = (y1 + y2)/2;
      return center;
    }
  }

   /**
    * Define inner class DrawCanvas, which is a JPanel used for custom drawing.
    */
   private class DrawCanvas extends JPanel {
      // Override paintComponent to perform your own painting
      @Override
      public void paintComponent(Graphics g) {
         super.paintComponent(g);     // paint parent's background
         setBackground(Color.BLACK);  // set background color for this JPanel

         LSD program = new LSD(g);
         program.kjor(null);
      }

      public void recursivePainter(Graphics g, Line[] input, int counter){
        if(counter == 0){
          return;
        }

        Line[] output = new Line[4];
        for(int i = 0; i < output.length; i++){
          if(i < 3){
            output[i] = new Line(
            input[i].center[0],
            input[i].center[1],
            input[i+1].center[0],
            input[i+1].center[1]);
          } else {
            output[i] = new Line(
            input[i].center[0],
            input[i].center[1],
            input[0].center[0],
            input[0].center[1]);
          }
        }
        int[] x_vals = new int[3];
        int[] y_vals = new int[3];
        Random r = new Random(1000);

        for(int i = 0; i < x_vals.length; i++){
          x_vals = new int[3];
          y_vals = new int[3];
          if(i < 3){
            x_vals[0] = input[i].center[0];//center a
            y_vals[0] = input[i].center[1];//center a
            x_vals[1] = input[i].x2;//x2 a
            y_vals[1] = input[i].y2;//y2 a
            x_vals[2] = input[i+1].center[0];//center b
            x_vals[2] = input[i+1].center[1];//center b
          } else {
            x_vals[0] = input[i].center[0];//center a
            y_vals[0] = input[i].center[1];//center a
            x_vals[1] = input[i].x2;//x2 a
            y_vals[1] = input[i].y2;//y2 a
            x_vals[2] = input[0].center[0];//center b
            x_vals[2] = input[0].center[1];//center b
          }
          for(int ii = 0; ii < 3; ii++){
            System.out.format("xy%d: %2d, %2d\n",ii, x_vals[ii], y_vals[ii]);
          }
          Color random = new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
          g.setColor(random);
          g.drawPolygon(x_vals, y_vals, 3);
        }

        // repaint();
        // recursivePainter(g, output, counter - 1);
      }
   }

   // The entry main method
   public static void main(String[] args) {
      // Run the GUI codes on the Event-Dispatching thread for thread safety
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            new Tegneprogram(); // Let the constructor do the job
         }
      });
   }
}
