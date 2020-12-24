package logistic_map;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferStrategy;
import java.util.*;

import javax.swing.JFrame;

public class LogisticMap extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;

	// Width and height of window
	private static final int WIDTH = 1100, HEIGHT = 800;
	private int HEIGHT_OFFSET = 100;
	private int WIDTH_OFFSET = 50;
	
	// Do not touch
	private Thread thread;
	private boolean running = false;
	
	
	
	private List<Point> points = new ArrayList<Point>();
	
	private int NUM_VALUES = 22; // Number of intervals on bottom
	private double[] values = new double[NUM_VALUES];
	
	private final int MAX_DECIMALS = 8; // When to stop iterating, must be less than max byte length of double
	private final int MAX_ITERATIONS = 10000;

	private void init() {
		
		double width = (MAX - MIN) / NUM_VALUES;
		for (int i = 0; i < NUM_VALUES; i++) {
			values[i] = round(MIN + (i * width), 1);
		}
		
		
		generatePoints();
		
	}
	
	// Changeable values
	private double MIN = 0.0; // Left bound
	private double MAX = 4.4; // Right bound
	private double x0 = 0.5; // The starting population
	
	// If you set higher than 1000, MAX cannot be > 3.6 otherwise it takes a ton of time to load
	// However, 1000 still looks very good
	private double accuracy = 1000; // Must be power of 10, higher number = higher accuracy
	
	private void generatePoints() {
		for (double j = MIN * accuracy; j <= MAX * accuracy; j += 1) {
			double i = j / accuracy; // Weird glitch with doubles in for loops
			
			for (double value: generateValuesForPoint(i)) {
				points.add(new Point((int)Math.round(translatePoint(i, MIN, MAX, 0, WIDTH - WIDTH_OFFSET)), (int)Math.round(translatePoint(value, 0, 1.0, 0, HEIGHT - HEIGHT_OFFSET))));
			}

		}
	}
	
	private List<Double> generateValuesForPoint(double r) {
		
		List<Double> tempValues = new ArrayList<Double>();
		
		tempValues.add(x0);
		
		int count = 0;
		
		while (true) {
			double xNew = logisticEquation(r, tempValues.get(count));
			if (tempValues.contains(xNew)) {
				for (int i = 0; i < tempValues.size(); i++) {
					if (tempValues.get(i) == xNew) {
						count = i;
						break;
					}
				}
				break;
			} else if (xNew == 0 || firstThreeDigitsAreEqual(xNew, 0.000)) {
				tempValues.clear();
				tempValues.add(0.0);
				return tempValues;
			} else if (xNew == 1 || firstThreeDigitsAreEqual(xNew, 1.000)) {
				tempValues.clear();
				tempValues.add(1.0);
				return tempValues;
			} else {
				tempValues.add(xNew);
			}
			
			if (count > MAX_ITERATIONS) {
				break;
			}
			
			count++;
		}
		
		if (count < tempValues.size())  {

			
			return tempValues.subList(count, tempValues.size());
		}
		
		
		
		return tempValues;
		
	}
	
	// x1 = rx0(1 - x0), can change x0 above
	private double logisticEquation(double r, double xPrev) {
		double xNext = r * xPrev * (1 - xPrev);
		return round(xNext, MAX_DECIMALS);
	}
	
	private double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = (long) Math.floor(value);
		return (double) tmp / factor;
	}
	
	private boolean firstThreeDigitsAreEqual(double d1, double d2) {
		String s1 = Double.toString(d1);
		String s2 = Double.toString(d2);
		if (s1.length() >= 5 && s2.length() >= 5) {
			if (s1.substring(3, 5) == s2.substring(3, 5)) {
				return true;
			}
		}
		return false;
	}

	// Point's current pos, Point's current leftbound/rightbound, and requested leftbound/rightbound
	private double translatePoint(double n, double firstBound, double secondBound, double thirdBound, double fourthBound) {
		double ratio = (n - firstBound) / (secondBound - firstBound);
		
		return (fourthBound - thirdBound) * ratio;

	}
	
	// Do not touch below here unless you can make an efficient rendering system
	private synchronized void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}

	private synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	// Game loop because I used a game template for the rendering
	public void run() {
		init();
		while(running) {
			render();
		}
		stop();
	}
	
	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null) {
			this.createBufferStrategy(2);
			return;
		}
		Graphics g = (Graphics2D) bs.getDrawGraphics().create();

		g.setColor(Color.white);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		g.setColor(Color.black);
		g.setFont(new Font("arial", Font.PLAIN, 20));
		g.drawString("x0 = " + x0, 20, 30);
		
		g.translate(20, HEIGHT - HEIGHT_OFFSET);
		
		for (Point p: points) {
			//System.out.println("x " + p.getX() + " y: " + p.getY());
			g.drawOval((int)p.getX(), (int)-p.getY(), 1, 1);
		}
		
		int width = (WIDTH - WIDTH_OFFSET) / NUM_VALUES;
		for (int i = 0; i < NUM_VALUES; i++) {
			g.setFont(new Font("arial", Font.PLAIN, 15));
			g.drawString(Double.toString(values[i]), i * width - 10, HEIGHT_OFFSET - 60);
		}
		
		
		g.dispose();
		bs.show();
	}
	
	public static void main(String args[]) {
		JFrame frame = new JFrame("Logistic Map");
		frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		frame.setMaximumSize(new Dimension(WIDTH, HEIGHT));
		frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		LogisticMap logistic = new LogisticMap();
		frame.add(logistic);
		frame.setVisible(true);
		logistic.start();
		
	}

}