package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import jp.tokyo.selj.common.DateTextFormatter;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.model.DocNode;

import org.apache.log4j.Logger;

public class PnlNodeList extends JPanel {
	Logger log = Logger.getLogger(this.getClass());
	
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;

	public static interface SelectionLintener{
		public void process(Doc youken);
		public void setRootNode(DocNode rootNode);
	}
	SelectionLintener sl_;
	
	class ActSelect extends AbstractAction {
		public ActSelect() {
			putValue(Action.NAME, "select");
		}
		public void actionPerformed(ActionEvent e) {
			if(sl_ == null){
				return;
			}
			JTable table = (JTable)e.getSource();
			int row = table.getSelectedRow();
			DocTableModel docTableModel = (DocTableModel)table.getModel();
			Doc doc = docTableModel.getDoc(row);
			if(doc != null){
				try{
					sl_.process(doc);
				}catch(NullPointerException ex){
					JOptionPane.showMessageDialog(
							SwingUtilities.getWindowAncestor(PnlNodeList.this),
							"<html>" +
							"他のZeetaで追加されたノードです。<br>" +
							"現在選択中のノードの親ノードを選択しF5キーでリフレッシュしてから" +
							"再度実行してください。<br>" +
							"同じ症状が出る場合は、更にその親をリフレッシュしてください。" ,
							""
							,JOptionPane.WARNING_MESSAGE);

				}
			}
		}
	}
	Action actSelect_ = new ActSelect();  //  @jve:decl-index=0:
	private JTable dspNodes2 = null;

	/**
	 * This is the default constructor
	 */
	public PnlNodeList() {
		super();
		initialize();
	}
	public List<Doc> getDocs(){
		return docs_;
	}

	
	class DocTableModel extends AbstractTableModel {
		List<Doc> docs_;
		String[] names_ = {"Id","Title","Text", "Creator", "Date"};
		public DocTableModel(List<Doc> docs){
			docs_ = docs;
		}
		public Doc getDoc(int row) {
			if( row < 0 ){
				return null;
			}
			return docs_.get(row);
		}
		public int getColumnCount() { return 5; }
		public int getRowCount() { return docs_.size();}
		public Object getValueAt(int row, int col) { 
			Object ret ;
			switch(col){
			case	0:
				ret = new Long(docs_.get(row).getDocId());
				break;
			case	1:
				ret = docs_.get(row).getDocTitle();
				break;
			case	2:
				ret = docs_.get(row).getDocCont();
				break;
			case	3:
				ret = docs_.get(row).getUserName();
				break;
			case	4:
				ret = DateTextFormatter.dateToString(docs_.get(row).getNewDate());
				break;
			default:
				ret = "(unknown col:"+col+")";
			}
			return ret; 
		}
		
		@Override
		public String getColumnName(int column) {
			return names_[column];
		}

		public void setDoc(List<Doc> docs) {
			docs_ = docs;
			fireTableDataChanged();
		}
		public void clear() {
			setDoc(new ArrayList<Doc>());
		}
    };
    
    boolean isFirstSetup_ = true;
	static int[] INIT_WIDTHS = {70,100,150,40};
	List<Doc> docs_;
	
	public void setup(List<Doc> docs, SelectionLintener sl){
		docs_ = docs;
		int [] widths = new int[INIT_WIDTHS.length];
		if(isFirstSetup_){
			System.arraycopy(INIT_WIDTHS, 0, widths, 0, INIT_WIDTHS.length);
			DocTableModel docModel = new DocTableModel(docs);
			dspNodes2.setModel(docModel);
			dspNodes2.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			isFirstSetup_ = false;
			//Enterキーでjump
			dspNodes2.getInputMap().
				put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "select");
			dspNodes2.getActionMap().put("select", actSelect_);
		}else{
			//カラム幅を記憶
			TableColumnModel colM = getDspNodes2().getColumnModel();
			for(int i=0; i<INIT_WIDTHS.length; i++){
				widths[i] = colM.getColumn(i).getWidth();
			}
		};
		TableColumnModel colM = getDspNodes2().getColumnModel();
		
		DocTableModel docTableModel = (DocTableModel)dspNodes2.getModel();
		docTableModel.setDoc(docs);

		//これは毎回やらないとだめなようだ
		colM.getColumn(0).setMaxWidth(INIT_WIDTHS[0]);
		colM.getColumn(0).setResizable(false);
		colM.getColumn(1).setMinWidth(INIT_WIDTHS[1]);

		for(int i=0; i<(widths.length-0); i++){
			colM.getColumn(i).setPreferredWidth(widths[i]);	//setWidth()じゃだめなようだ
		}

		sl_ = sl;
		dspNodes2.setEnabled(true);
		//選択状態にする
		if(docs.size() > 0){
			dspNodes2.getSelectionModel().setSelectionInterval(0, 0);
			dspNodes2.scrollRectToVisible(
					dspNodes2.getCellRect(0, 0, false));
		}
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(400, 100);
		this.setLayout(new BorderLayout());
		this.add(getJScrollPane(), BorderLayout.CENTER);
		
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getDspNodes2());
		}
		return jScrollPane;
	}
	public void clear() {
//		getDspNodes2().setListData(Collections.EMPTY_LIST.toArray());
		if((dspNodes2.getModel() != null) && 
			 (dspNodes2.getModel() instanceof DocTableModel) ){
			((DocTableModel)dspNodes2.getModel()).clear();
		}
	}
	public void requestFocusToList() {
		dspNodes2.requestFocus();
	}
	/**
	 * This method initializes dspNodes2	
	 * 	
	 * @return javax.swing.JTable	
	 */
	public JTable getDspNodes2() {
		if (dspNodes2 == null) {
			dspNodes2 = new JTable();
			dspNodes2.setToolTipText("ダブルクリック、EnterキーでMain Treeをポイントします。");
			dspNodes2.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if(e.getClickCount() > 1){
						actSelect_.actionPerformed(
								new ActionEvent(dspNodes2,0,"(none)"));
					}
				}
			});
		}
		return dspNodes2;
	}

}
