/*  JFontChooser.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 06/04/2001
 *
 *  $Id: JFontChooser.java 6491 2005-01-11 13:51:38Z ian $
 *
 */

package jp.tokyo.selj.common;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class FontChooser extends JPanel {

  public FontChooser() {
		this(UIManager.getFont("Button.font"));
	}

	public FontChooser(Font initialFont) {
		initLocalData();
		initGuiComponents();
		initListeners();
		setFontValue(initialFont);
	}// public JFontChooser(Font initialFont)

	public static Font showDialog(Component parent, String title,
			Font initialfont) {

		Window windowParent;
		if (parent instanceof Window){
			windowParent = (Window) parent;
		}else{
			windowParent = SwingUtilities.getWindowAncestor(parent);
		}
		if (windowParent == null){
			throw new IllegalArgumentException(
					"The supplied parent component has no window ancestor");
		}
		final JDialog dialog;
		if (windowParent instanceof Frame){
			dialog = new JDialog((Frame) windowParent, title, true);
		}else{
			dialog = new JDialog((Dialog) windowParent, title, true);
		}
		dialog.getContentPane().setLayout(
				new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));

		final FontChooser fontChooser = new FontChooser(initialfont);
		dialog.getContentPane().add(fontChooser);

		JButton okBtn = new JButton("OK");
		JButton cancelBtn = new JButton("Cancel");
		JPanel buttonsBox = new JPanel();
		buttonsBox.setLayout(new BoxLayout(buttonsBox, BoxLayout.X_AXIS));
		buttonsBox.add(Box.createHorizontalGlue());
		buttonsBox.add(okBtn);
		buttonsBox.add(Box.createHorizontalStrut(30));
		buttonsBox.add(cancelBtn);
		buttonsBox.add(Box.createHorizontalGlue());
		dialog.getContentPane().add(buttonsBox);
		dialog.pack();
		fontChooser.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				dialog.pack();
			}
		});
		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				fontChooser.setFontValue(null);
			}
		});

		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		return fontChooser.getFontValue();
	}// showDialog

	protected void initLocalData() {

	}

	protected void initGuiComponents() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		familyCombo = new JComboBox(GraphicsEnvironment
				.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		familyCombo
				.setSelectedItem(UIManager.getFont("Label.font").getFamily());

		sizeCombo = new JComboBox(new String[] { "8","9","10","11","12","14",
				"16","18","20","22","24","26","28","36","48","72"});
		sizeCombo.setSelectedItem(new Integer(UIManager.getFont("Label.font")
				.getSize()).toString());
		sizeCombo.setPreferredSize(new Dimension(60, 25));


		italicChk = new JCheckBox("<html><i>Italic</i></html>", false);
		boldChk = new JCheckBox("<html><b>Bold</b></html>", false);

		JPanel fontBox = new JPanel();
		fontBox.setLayout(new BoxLayout(fontBox, BoxLayout.X_AXIS));
		fontBox.add(familyCombo);
		fontBox.add(sizeCombo);
		fontBox.setBorder(BorderFactory.createTitledBorder(" Font "));
		add(fontBox);
		add(Box.createVerticalStrut(10));

		JPanel effectsBox = new JPanel();
		effectsBox.setLayout(new BoxLayout(effectsBox, BoxLayout.X_AXIS));
		effectsBox.add(italicChk);
		effectsBox.add(boldChk);
		effectsBox.setBorder(BorderFactory.createTitledBorder(" Effects "));
		add(effectsBox);
		add(Box.createVerticalStrut(10));

		sampleTextArea = new JTextArea("Type your sample here...");
		JPanel samplePanel = new JPanel();
		samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.X_AXIS));
		// samplePanel.add(new JScrollPane(sampleTextArea));
		samplePanel.add(sampleTextArea);
		samplePanel.setBorder(BorderFactory.createTitledBorder(" Sample "));
		add(samplePanel);
		add(Box.createVerticalStrut(10));
	}// initGuiComponents()

	protected void initListeners() {
		familyCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFont();
			}
		});

		sizeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFont();
			}
		});

		boldChk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFont();
			}
		});

		italicChk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFont();
			}
		});
	}// initListeners()

	protected void updateFont() {
		Map fontAttrs = new HashMap();
		fontAttrs.put(TextAttribute.FAMILY, (String) familyCombo
				.getSelectedItem());
		fontAttrs.put(TextAttribute.SIZE, new Float((String) sizeCombo
				.getSelectedItem()));

		if (boldChk.isSelected())
			fontAttrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		else
			fontAttrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);

		if (italicChk.isSelected())
			fontAttrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
		else
			fontAttrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);

		Font newFont = new Font(fontAttrs);
		Font oldFont = fontValue;
		fontValue = newFont;
		sampleTextArea.setFont(newFont);
		String text = sampleTextArea.getText();
		sampleTextArea.setText("");
		sampleTextArea.setText(text);
		sampleTextArea.repaint(100);
		firePropertyChange("fontValue", oldFont, newFont);
	}// updateFont()

	/**
	 * 189 * Test code 190
	 */
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		final JFrame frame = new JFrame("Foo frame");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JButton btn = new JButton("Show dialog");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(showDialog(frame, "Fonter", UIManager
						.getFont("Button.font")));
			}
		});
		frame.getContentPane().add(btn);
		frame.setSize(new Dimension(300, 300));
		frame.setVisible(true);
		System.out.println("Font: " + UIManager.getFont("Button.font"));
		showDialog(frame, "Fonter", UIManager.getFont("Button.font"));
	}// main

	public void setFontValue(java.awt.Font newfontValue) {
		this.fontValue = newfontValue;
		if(newfontValue == null){
			return;
		}
		boldChk.setSelected(newfontValue.isBold());
		italicChk.setSelected(newfontValue.isItalic());
		familyCombo.setSelectedItem(newfontValue.getName());
		sizeCombo.setSelectedItem(Integer.toString(newfontValue.getSize()));
	}

	public java.awt.Font getFontValue() {
		return fontValue;
	}

	JComboBox familyCombo;

	JCheckBox italicChk;

	JCheckBox boldChk;

	JComboBox sizeCombo;

	JTextArea sampleTextArea;

	private java.awt.Font fontValue;
}// class JFontChooser extends JPanel
