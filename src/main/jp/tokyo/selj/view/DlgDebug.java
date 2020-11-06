package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.model.DocModel.NodeProcessor;

import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.h2.tools.Script;


public class DlgDebug extends JDialog {

	static final String FILE_NAME = "backupZeeta.backup";
	static final String USER="sa";
	static final String PASSWORD="";
	
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JPanel jPanel = null;

	private JButton cmdCountNode = null;

	private JLabel dspNodeCount = null;

	public String getTitle(){
		return "よくみつけたわね。あんた。";
	}
	//H2用バックアップ
	class ActBackup extends AbstractAction {
		public ActBackup() {
			putValue(Action.NAME, "backup db");
			putValue(Action.SHORT_DESCRIPTION, FILE_NAME+"を作成する(H2 Only)");
		}
		public void actionPerformed(ActionEvent e) {
	        String url = "jdbc:h2:db/sel";
	        try {
	    		Script.execute(url, USER, PASSWORD, FILE_NAME);
				JOptionPane.showMessageDialog(
						DlgDebug.this
						,"Backup complete.",""
						,JOptionPane.INFORMATION_MESSAGE);
			} catch (SQLException e1) {
				throw new RuntimeException("この機能は、H2&db/sel でしか使用できません。",e1);
			}
	    }

	}
	//H2用コンパクト
	class ActCompact extends AbstractAction {
		public ActCompact() {
			putValue(Action.NAME, "compct db");
			putValue(Action.SHORT_DESCRIPTION, FILE_NAME+"を作成する(H2 Only)");
		}
		public void actionPerformed(ActionEvent e) {
	        String url = "jdbc:h2:db/sel";
	        try {
//				Backup.execute(FILE_NAME, "db", "sel", false);
				Script.execute(url, USER, PASSWORD, FILE_NAME);
				DeleteDbFiles.execute("db", "sel", true);
		        RunScript.execute(url, USER, PASSWORD, FILE_NAME, null, false);
				JOptionPane.showMessageDialog(
						DlgDebug.this
						,"DB Compact complete.",""
						,JOptionPane.INFORMATION_MESSAGE);
			} catch (SQLException e1) {
				throw new RuntimeException("この機能は、H2&db/sel でしか使用できません。",e1);
			}
	    }

	}
	class ActCountNode extends AbstractAction {
		public ActCountNode() {
			putValue(Action.NAME, "countNode");
			putValue(Action.SHORT_DESCRIPTION, "展開しているNode数を共有Nodeも重複してカウントする");
		}
		public void actionPerformed(ActionEvent e) {
			viewState_.getCurrentNode();
			NodeProcessor proc = 
				new NodeProcessor(){
					int count=0;
					public boolean process(DocNode node) {
						count++;
						dspNodeCount.setText(""+count);
						return true;	//続行
					}
				};
			
			docModel_.processAllNode2( viewState_.getCurrentNode(), proc );
	    }

	}

	FrmZeetaMain mainView_;
	DocModel docModel_;
	FrmZeetaMain.ViewState viewState_;
	private JToolBar jToolBar = null;
	/**
	 * @param owner
	 */
	public DlgDebug(Frame owner) {
		super(owner);
		mainView_ = (FrmZeetaMain)owner;
		docModel_ = mainView_.docModel_;
		viewState_ = mainView_.viewState_;
		initialize();
		setResizable(false);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 150);
		this.setContentPane(getJContentPane());
		
		getCmdCountNode().setAction(new ActCountNode());
		getJToolBar().add(new ActBackup());
//		getJToolBar().add(new ActCompact());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJPanel(), BorderLayout.CENTER);
			jContentPane.add(getJToolBar(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.ipadx = 30;
			gridBagConstraints2.ipady = 20;
			gridBagConstraints2.gridy = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.ipadx = 30;
			gridBagConstraints1.ipady = 20;
			gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints1.gridy = 0;
			dspNodeCount = new JLabel();
			dspNodeCount.setBorder(new BevelBorder(BevelBorder.LOWERED));
			dspNodeCount.setText("JLabel");
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getCmdCountNode(), gridBagConstraints1);
			jPanel.add(dspNodeCount, gridBagConstraints2);
		}
		return jPanel;
	}

	/**
	 * This method initializes cmdCountNode	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdCountNode() {
		if (cmdCountNode == null) {
			cmdCountNode = new JButton();
			cmdCountNode.setText("count node");
		}
		return cmdCountNode;
	}

	/**
	 * This method initializes jToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getJToolBar() {
		if (jToolBar == null) {
			jToolBar = new JToolBar();
		}
		return jToolBar;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
