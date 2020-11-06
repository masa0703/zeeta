package jp.tokyo.selj.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class DocumentUpdateListener implements DocumentListener,
		ActionListener, ListDataListener {
	protected boolean isDarty_ = false;

	public void reset() {
		isDarty_ = false;
	}
	protected void setDarty(){
		isDarty_ = true;
	}

	public boolean isDarty() {
		return isDarty_;
	}

	public void changedUpdate(DocumentEvent e) {
		setDarty();
	}

	public void insertUpdate(DocumentEvent e) {
		setDarty();
	}

	public void removeUpdate(DocumentEvent e) {
		setDarty();
	}

	public void actionPerformed(ActionEvent e) {
		setDarty();
	}

	public void contentsChanged(ListDataEvent e) {
		setDarty();
	}

	public void intervalAdded(ListDataEvent e) {
		setDarty();
	}

	public void intervalRemoved(ListDataEvent e) {
		setDarty();
	}
};
