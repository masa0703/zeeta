package jp.tokyo.selj.view;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;

import jp.tokyo.selj.dao.Review;
import jp.tokyo.selj.dao.SelJDaoContainer;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class LstReviews extends JList {
	Logger log = Logger.getLogger(this.getClass());
	
	public static final String DOUBLE_CLICK_ACTION_KEY = "double click action"; 
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;
	private JPopupMenu mnuPopup = null;  //  @jve:decl-index=0:visual-constraint="110,13"
	
//	private class MyListCellRenderer extends PnlOutput implements ListCellRenderer {
//		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//			Output output = (Output)value;
//			this.setOutput(output);
//			this.setOpaque(isSelected);		//不透明にすることで選択状態を表す。（透明だと白くなる）
//			return this;
//		}
//
//	}
	private class MyListCellRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			Review review = (Review)value;
			String line = "";
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
			line = formatter.format(review.getReviewDate());
			line += ", "+review.getRemark();
			label.setText(line);
			return label;
		}
	}

	class MyMouseListener implements MouseListener {
    	public void mouseClicked(java.awt.event.MouseEvent e) {
    		if(e.getClickCount() == 2){
    			Action action = getActionMap().get(DOUBLE_CLICK_ACTION_KEY);
    			if(action != null){
    				action.actionPerformed(null);
    			}
    		}
    	}
    	public void mousePressed(java.awt.event.MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON3){	//右ボタン
				getMnuPopup().show(LstReviews.this, e.getX(), e.getY());
			}
    	}
    	public void mouseReleased(java.awt.event.MouseEvent e) {
    	}
    	public void mouseEntered(java.awt.event.MouseEvent e) {
    	}
    	public void mouseExited(java.awt.event.MouseEvent e) {
    	}
	}
	
//	private ActionMap actionMap_;
	public JPopupMenu getPopupMenu(){
		return getMnuPopup();
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setCellRenderer(new MyListCellRenderer());
        this.addMouseListener(new MyMouseListener());
	}

	public LstReviews(){
		super();
		initialize();
	}

	public void setup() {
		//何もすることがなくなったけど、一応残しておく
	}

	/**
	 * This method initializes mnuWorkList	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getMnuPopup() {
		if (mnuPopup == null) {
			mnuPopup = new JPopupMenu();
		}
		return mnuPopup;
	}
}
