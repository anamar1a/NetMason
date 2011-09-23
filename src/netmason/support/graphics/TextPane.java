package netmason.support.graphics;

import javax.swing.*;
import java.awt.*;

class TextPane {
	public static void main(String[] args) {
		JFrame jf = new JFrame();
		JTextArea textArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textArea);
		jf.getContentPane().add(BorderLayout.CENTER, scrollPane);
		jf.setBounds(100, 100, 400, 300);
		jf.setVisible(true);

	}
}