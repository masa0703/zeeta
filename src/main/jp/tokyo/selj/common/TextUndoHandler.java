package jp.tokyo.selj.common;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class TextUndoHandler implements UndoableEditListener {
	UndoManager undo_ = new UndoManager();
	UndoAction undoAction_ = new UndoAction();
	RedoAction redoAction_ = new RedoAction();
	
	
	class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo_.undo();
			} catch (CannotUndoException ex) {
				//OK
			}
			update();
			redoAction_.update();
		}

		void update() {
			if (undo_.canUndo()) {
				setEnabled(true);
				putValue(Action.NAME, undo_.getUndoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo_.redo();
			} catch (CannotRedoException ex) {
				//OK
			}
			update();
			undoAction_.update();
		}

		void update() {
			if (undo_.canRedo()) {
				setEnabled(true);
				putValue(Action.NAME, undo_.getRedoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}
	/**
	 * Messaged when the Document has created an edit, the edit is added to
	 * <code>undo</code>, an instance of UndoManager.
	 */
	public void undoableEditHappened(UndoableEditEvent e) {
		undo_.addEdit(e.getEdit());
		undoAction_.update();
		redoAction_.update();
	}
	public void cleanup(){
		undo_.discardAllEdits();
	}
	public void setup(JTextComponent tc){
	    if(tc.getDocument() == null){
	    	return;
	    }
	    tc.getDocument().addUndoableEditListener(this);

	    tc.getActionMap().put(undoAction_.getClass(), undoAction_);
	    tc.getActionMap().put(redoAction_.getClass(), redoAction_);
	    tc.getInputMap().put(
	    		KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK),
	    		undoAction_.getClass());
	    tc.getInputMap().put(
	    		KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK),
	    		redoAction_.getClass());

	}
}
