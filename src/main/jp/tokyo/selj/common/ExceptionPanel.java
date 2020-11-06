package jp.tokyo.selj.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


class ExceptionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	private JScrollPane jScrollPane1 = null;
	private JLabel dspMessage = null;
	private JTextArea dspStacktrace = null;

	/**
	 * This is the default constructor
	 */
	public ExceptionPanel() {
		super();
		initialize();
	}
	public ExceptionPanel(Throwable e) {
		this();
		dspMessage.setText(e.getMessage());
		if(e instanceof AppException){
			getJScrollPane1().setVisible(false);
			this.setPreferredSize(new Dimension(480, 50));
		}else{
			getJScrollPane1().setVisible(true);
			String st = null;
			try {
				st = stackTraceToString(e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			getDspStacktrace().setText(st);
			this.setPreferredSize(new Dimension(480, 480));
		}
	}
	String stackTraceToString(Throwable e) throws IOException{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		PrintStream s = new PrintStream(b);
		e.printStackTrace(s);
		s.close();
		b.close();
		return new String(b.toByteArray());
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(420, 300);
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(420, 100));
		this.add(getJScrollPane(), BorderLayout.NORTH);
		this.add(getJScrollPane1(), BorderLayout.CENTER);
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			dspMessage = new JLabel();
			dspMessage.setText("JLabel");
			jScrollPane = new JScrollPane();
			jScrollPane.setPreferredSize(new Dimension(56, 50));
			jScrollPane.setViewportView(dspMessage);
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getDspStacktrace());
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes dspStacktrace	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getDspStacktrace() {
		if (dspStacktrace == null) {
			dspStacktrace = new JTextArea();
			dspStacktrace.setEditable(false);
		}
		return dspStacktrace;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
