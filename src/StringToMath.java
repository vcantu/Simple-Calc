
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;


public class StringToMath extends JApplet {
	
	JTextArea text;
	
	public void init() {
		JLabel label = new JLabel("Answer: ");
		JTextArea ta = new JTextArea(5,30);
        ta.setMargin(new Insets(5,5,5,5));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					label.setText("Answer: " + calc(ta.getText()));
					ta.setText("");
				}
			}
        	
        });
        JPanel panel = new JPanel();
        panel.add(ta);
        JPanel south = new JPanel();
        south.add(label);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel);
        getContentPane().add(south, "South");
	}
	
	//When run as Java Application
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		while(scan.hasNext()) {
			System.out.println(calc(scan.nextLine()));
		}
	}
	
	private static String calc(String str) {
		try {
			return eval(split(str));
		}
		catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}
	
	//for debugging purposes
	private static String print(ArrayList<String> lst) {
		String s = "";
		for (String S : lst) {
			s += S + " ";
		}
		return s;
	}
	
	private static String eval(ArrayList<String> lst) {
		if (lst.size() == 1)
			return lst.get(0);
		//PEMDAS
		//look for open parenthesis
		for (int i=0; i<lst.size(); i++) {
			String curr = lst.get(i);
			if (isOpener(curr)) {
				lst.remove(i);
				return eval(merge(new ArrayList<String>(lst.subList(0, i)),
								  close(new ArrayList<String>(lst.subList(i, lst.size())))));
			}
		}
		//look for functions
		for (int i=0; i<lst.size(); i++) {
			String curr = lst.get(i);
			if (isFunction(curr)) {
				if (curr.equalsIgnoreCase("sin"))
					lst.set(i, String.valueOf(Math.sin(Double.valueOf(lst.remove(i+1)))));
				else if (curr.equalsIgnoreCase("cos"))
					lst.set(i, String.valueOf(Math.cos(Double.valueOf(lst.remove(i+1)))));
				else if (curr.equalsIgnoreCase("tan"))
					lst.set(i, String.valueOf(Math.tan(Double.valueOf(lst.remove(i+1)))));
				else if (curr.equalsIgnoreCase("abs"))
					lst.set(i, String.valueOf(Math.abs(Double.valueOf(lst.remove(i+1)))));
				else if (curr.equalsIgnoreCase("floor"))
					lst.set(i, String.valueOf(Math.floor(Double.valueOf(lst.remove(i+1)))));
				else if (curr.equalsIgnoreCase("ceil"))
					lst.set(i, String.valueOf(Math.ceil(Double.valueOf(lst.remove(i+1)))));
				return eval(lst);
			}				
		}
		//look for ^
		for (int i=0; i<lst.size(); i++) {
			String curr = lst.get(i);
			if (curr.equals("^")) {
				lst.set(i-1, String.valueOf(Math.pow(Double.valueOf(lst.remove(i-1)), Double.valueOf(lst.remove(i)) )));
				return eval(lst);
			}
		}
		//look for * or / or nothing
		for (int i=0; i<lst.size(); i++) {
			String curr = lst.get(i);
			if (curr.equals("*") || curr.equals("/") || curr.equals("%")) {
				if (curr.equals("*"))
					lst.set(i-1, String.valueOf(Double.valueOf(lst.remove(i-1)) * Double.valueOf(lst.remove(i))));
				else if (curr.equals("/"))
					lst.set(i-1, String.valueOf(Double.valueOf(lst.remove(i-1)) / Double.valueOf(lst.remove(i))));
				else
					lst.set(i-1, String.valueOf(Double.valueOf(lst.remove(i-1)) % Double.valueOf(lst.remove(i))));
				return eval(lst);
			}
			else if (i < lst.size()-1 && isNumber(curr) && isNumber(lst.get(i+1))) {
				lst.set(i, String.valueOf(Double.valueOf(lst.get(i)) * Double.valueOf(lst.remove(i+1))));
				return eval(lst);
			}
		}
		//look for + -
		for (int i=0; i<lst.size(); i++) {
			String curr = lst.get(i);
			if (curr.equals("+") || curr.equals("-")) {
				if (curr.equals("+")) {
					lst.set(i-1, String.valueOf(Double.valueOf(lst.remove(i-1)) + Double.valueOf(lst.remove(i))));
				}
				else {
					try {
						lst.set(i-1, String.valueOf(Double.valueOf(lst.remove(i-1)) - Double.valueOf(lst.remove(i))));
					}
					catch (Exception e) {
						lst.set(i, String.valueOf(Double.valueOf(lst.remove(i+1)) * -1));
					}
				}
				return eval(lst);
			}
		}
		return "Error";
	}
	
	private static ArrayList<String> close(ArrayList<String> lst) {
		//look for close parenthesis
		int open = 0;
		for (int i=0; i<lst.size(); i++) {
			String curr = lst.get(i);
			if (isOpener(curr))
				open++;
			else if (isCloser(curr)) {
				if (open == 0) {
					lst.set(i, eval(new ArrayList<String>(lst.subList(0, i))));
					for (int j=0; j<i; j++) {
						lst.remove(0);
					}
					break;
				}
				else
					open--;
			}
		}
		return lst;
	}
	
	private static ArrayList<String> merge(ArrayList<String> l1, ArrayList<String> l2) {
		for (String s : l2) {
			l1.add(s);
		}
		return l1;
	}
	
	private static ArrayList<String> split(String str) {
		ArrayList<String> res = new ArrayList<String>();
		res.add(str.substring(0, 1));
		if (str.length()>1) {
			for (int i=1; i<str.length(); i++) {
				char curr = str.charAt(i);
				//is number
				if (Character.isDigit(curr) || curr == '.') {
					if (Character.isDigit(str.charAt(i-1)) || str.charAt(i-1) == '.')//if previous is also number
						res.set(res.size()-1, res.get(res.size()-1).concat(str.substring(i, i+1)));
					else
						res.add(str.substring(i, i+1));
				}
				else if (Character.isAlphabetic(curr)) {
					if (Character.isAlphabetic(str.charAt(i-1)))//if previous is also number
						res.set(res.size()-1, res.get(res.size()-1).concat(str.substring(i, i+1)));
					else
						res.add(str.substring(i, i+1));
				}
				else
					res.add(str.substring(i, i+1));
			}
			res = fix(res);
		}
		return res;
	}
	private static ArrayList<String> fix(ArrayList<String> res) {
		res = removeSpaces(res);
		res = fixWords(res);
		res = fixNegatives(res);
		return res;
	}
	private static ArrayList<String> removeSpaces(ArrayList<String> res) {
		for (int i=0; i<res.size(); i++) {
			res.set(i, res.get(i).replaceAll("\\s", ""));//removes white space
			if (res.get(i).length() <= 0) {
				res.remove(i); i--;
			}
		}
		return res;
	}
	private static ArrayList<String> fixNegatives(ArrayList<String> res) {
		for (int i=0; i<res.size()-1; i++) {
			if (res.get(i).equals("-") && (i == 0 || (!isNumber(res.get(i-1))
					&& !isCloser(res.get(i-1)))) && isNumber(res.get(i+1))) {
				res.remove(i);
				res.set(i, "-".concat(res.get(i)));
			}
		}
		return res;
	}
	private static ArrayList<String> fixWords(ArrayList<String> res) {
		for (int i=0; i<res.size(); i++) {
			String curr = res.get(i);
			if (curr.equalsIgnoreCase("pi"))
				res.set(i, String.valueOf(Math.PI));
			else if (curr.equalsIgnoreCase("e"))
				res.set(i, String.valueOf(Math.E));
		}
		return res;
	}
	
	private static boolean isNumber(String s) {
		return s.matches("^\\-?[1-9]\\d{0,2}(\\.\\d*)?$");
	}
	private static boolean isOperator(String s) {
		return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/")
			|| s.equals("^") || s.equals("%");
	}
	private static boolean isOpener(String s) {
		return s.equals("(") || s.equals("[") || s.equals("{");
	}
	private static boolean isCloser(String s) {
		return s.equals(")") || s.equals("]") || s.equals("}");
	}
	private static boolean isFunction(String s) {
		return s.equalsIgnoreCase("sin") || s.equalsIgnoreCase("cos") || s.equalsIgnoreCase("tan") ||
			   s.equalsIgnoreCase("abs") || s.equalsIgnoreCase("floor") || s.equalsIgnoreCase("ceil");
	}
}
