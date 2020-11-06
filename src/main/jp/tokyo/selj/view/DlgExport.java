package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.util.ClipboardStringWriter;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DlgExport extends JDialog {

	Logger log = Logger.getLogger(this.getClass());  //  @jve:decl-index=0:
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JTabbedPane jTabbedPane = null;

	private JPanel jPanel = null;

	private JTextField inpFilePath = null;

	private JButton cmdExport = null;

	private JButton cmdChooseFile = null;
	
	ButtonGroup buttonGroupIndent_ = new ButtonGroup();  //  @jve:decl-index=0:
	ButtonGroup buttonGroupTo_ = new ButtonGroup();  //  @jve:decl-index=0:
	
	Doc doc_;  //  @jve:decl-index=0:
	DocModel docModel_;  //  @jve:decl-index=0:
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
//	WorkPropDao workPropDao_ = null;
//	OutputPropTypeDao outputPropTypeDao_ = null;

	//ファイル選択フィルター
	class CsvFilter extends FileFilter{
		@Override
		public boolean accept(File f) {
			return f.getName().endsWith(".csv");
		}
		@Override
		public String getDescription() {
			return "*.csv";
		}
	}
	
	//ファイル選択
	class ActChooseFile extends AbstractAction {
		JFileChooser chooser_=null;
		public ActChooseFile(){
			putValue(Action.NAME, "choose file");
			putValue(Action.SHORT_DESCRIPTION, "出力先");
		}
		public void actionPerformed(ActionEvent e) {
			if(chooser_ == null){
			    chooser_ = new JFileChooser();
			    chooser_.setFileFilter(new CsvFilter());
			}
			int returnVal = chooser_.showOpenDialog(DlgExport.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				getInpFilePath().setText(
						chooser_.getSelectedFile().getPath()
				);
			}
		}
	}
	Action actChooseFile_ = new ActChooseFile();  //  @jve:decl-index=0:

	//CSV Export
	class ActExportToCsv extends AbstractAction {
		public ActExportToCsv(){
			putValue(Action.NAME, "export");
			putValue(Action.SHORT_DESCRIPTION, "CSV/TSVファイル作成");
		}
		public void actionPerformed(ActionEvent ev) {
			Writer writer = null;
			try{ 
				//出力先
				if(inpSelectFile.isSelected()){
					if("".equals( inpFilePath.getText().trim() )){
						throw new AppException("ファイルパスを入力してください");
					}
					
					writer = new FileWriter(inpFilePath.getText());
				}else if(inpSelectClipboard.isSelected()){
					writer = new ClipboardStringWriter();
				}
				exportToCsv(writer, inpDepth.getValue());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}finally{
				if(writer != null){
					try {
						writer.close();
					} catch (IOException e) {
						log.error("file close error", e);
					}
				}
			}
			JOptionPane.showMessageDialog(
					DlgExport.this
					,"export完了",""
					,JOptionPane.INFORMATION_MESSAGE);
			
			setVisible(false);
		}
	}
	Action actExportToCsv_ = new ActExportToCsv();  //  @jve:decl-index=0:
	
	private JPanel jPanel1 = null;
	private JRadioButton inpSelectTab = null;
	private JTextField inpSpecString = null;
	private JPanel jPanel2 = null;
	private JRadioButton inpSelectFile = null;
	private JRadioButton inpSelectClipboard = null;
	private JPanel jPanel3 = null;
	private JCheckBox inpChkDocText = null;
	private JPanel jPanel4 = null;
	private JCheckBox inpChkDate = null;
	private JPanel jPanel5 = null;
	private JCheckBox inpChkId = null;
	private JCheckBox inpChkTitle = null;
	private JCheckBox inpChkUser = null;
	private JPanel jPanel6 = null;
	private JRadioButton inpSelectComma = null;
	private JSlider inpDepth = null;
	private JCheckBox inpExpectDoubleExport = null;
	private JTextField inpExpectCaption = null;
	private JCheckBox inpChkTab = null;
	private JCheckBox inpNoExpandLinkNode = null;
	/**
	 * @param owner
	 */
	public DlgExport(Frame owner) {
		super(owner, true);
		initialize();
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		getInpSpecString().selectAll();		//なんか意味ないかも
	}

	void exportToCsv(Writer writer, int depth) {
		log.trace("start");
		
		if( !(
				inpChkId.isSelected()
				|| inpChkTitle.isSelected()
				|| inpChkDocText.isSelected()
				|| inpChkUser.isSelected()
				|| inpChkDate.isSelected()
			 )
		){
			throw new AppException("1つ以上のcolumnをチェックしてください");
		}
		
		
		class CsvMaker implements DocModel.DocProcessor{
			Writer writer_;
			int depth_;
			Set<Long> calcDocIds = new HashSet<Long>();
			SimpleDateFormat formatter_ = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			public CsvMaker(Writer writer, int depth){
				writer_ = writer;
				depth_ = depth;
			}
			public boolean process(Doc doc, List<Doc> parents) {
				if(depth_ > 0 && parents.size() > depth_){
					return false;
				}
				boolean ret = true;
				if(inpExpectDoubleExport.isSelected()){	//一度展開したデータは、再度展開しない
					if(calcDocIds.contains(doc.getDocId())){
						doc.setDocTitle(
								inpExpectCaption.getText()
								+doc.getDocTitle());
						ret = false;	//子供は処理しない
					}
				}
				if(inpNoExpandLinkNode.isSelected()){	//リンクノードは展開しない
					//ただし、リンクノードの子供は出力するので「親がリンクノードか？」で判定
					if(parents.size() > 0){
						if(parents.get(parents.size()-1).getDocTypeId() == Doc.LINK_TYPE ){
							ret = false;	//子供は処理しない
						}
					}
				}
				
				calcDocIds.add(doc.getDocId());
				try{
					String separatorType = "";
					String separator = "";
					String doubleQuote = "\"";

					if(inpSelectTab.isSelected()){
						separatorType = "\t";
					}else{
						separatorType = ",";
					}
					
					
					//---Id
					if(inpChkId.isSelected()){
//						writer_.write(doubleQuote);
						writer_.write(""+doc.getDocId());
//						writer_.write(doubleQuote);
						separator = separatorType;
					}
					
					//---DocTitle
					if(inpChkTitle.isSelected()){
						writer_.write(separator);
//						writer_.write(doubleQuote);
						//親の数分indentを付加
						for(int i=0; i<parents.size(); i++){
							if(getInpChkTab().isSelected()){
								writer_.write("\t");
							}else{
								writer_.write(inpSpecString.getText());
							}
						}
						writer_.write(doc.getDocTitle());
//						writer_.write(doubleQuote);
						separator = separatorType;
					}
					
					//---DocCont
					if(inpChkDocText.isSelected()){
						writer_.write(separator);
						if(doc.getDocCont()!=null){
							writer_.write(doubleQuote);
							writer_.write(cnvDoubleQuote(doc.getDocCont()));
							writer_.write(doubleQuote);
						}
						separator = separatorType;
					}
					//---user
					if(inpChkUser.isSelected()){
						writer_.write(separator);
//						writer_.write(doubleQuote);
						if(doc.getUserName() != null){
							writer_.write(doc.getUserName());
						}
//						writer_.write(doubleQuote);
						separator = separatorType;
					}
					
					//---NewDate
					if(inpChkDate.isSelected()){
						writer_.write(separator);
//						writer_.write(doubleQuote);
						writer_.write(formatter_.format(doc.getNewDate()));
//						writer_.write(doubleQuote);
						separator = separatorType;
					}
					
					writer_.write(System.getProperty("line.separator"));
					
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return ret;	//続行
			}
		}
		CsvMaker proc = new CsvMaker(writer, depth);
		docModel_.processAllDoc2(doc_, proc, new ArrayList<Doc>());
	
		log.trace("end");
	}
	String cnvDoubleQuote(String src){
		return (src==null)? null:src.replaceAll("\"", "\"\"");
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(566, 322);
		this.setResizable(false);
		this.setTitle("export/summary");
		this.setMinimumSize(new Dimension(480, 320));
		this.setContentPane(getJContentPane());
		buttonGroupIndent_.add(getInpSelectTab());
		buttonGroupIndent_.add(getInpSelectComma());
		buttonGroupTo_.add(getInpSelectFile());
		buttonGroupTo_.add(getInpSelectClipboard());
		inpSelectFile.setSelected(true);	//inpSelectFileのchangeエベントを動作させる
		inpSelectClipboard.setSelected(true);
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
			jContentPane.add(getJTabbedPane(), BorderLayout.CENTER);
			jContentPane.add(getJPanel3(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("CSV/TSV", null, getJPanel(), null);
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints.gridy = 0;
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.gridx = 0;
			gridBagConstraints51.anchor = GridBagConstraints.WEST;
			gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints51.ipadx = 0;
			gridBagConstraints51.weightx = 0.5;
			gridBagConstraints51.gridwidth = 3;
			gridBagConstraints51.gridy = 1;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.gridx = 0;
			gridBagConstraints41.anchor = GridBagConstraints.WEST;
			gridBagConstraints41.gridwidth = 1;
			gridBagConstraints41.fill = GridBagConstraints.NONE;
			gridBagConstraints41.gridy = 0;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 2;
			gridBagConstraints11.fill = GridBagConstraints.NONE;
			gridBagConstraints11.gridwidth = 1;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints11.gridy = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.insets = new Insets(10, 0, 0, 0);
			gridBagConstraints2.gridwidth = 3;
			gridBagConstraints2.gridy = 4;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getCmdExport(), gridBagConstraints2);
			jPanel.add(getJPanel1(), gridBagConstraints11);
			jPanel.add(getJPanel2(), gridBagConstraints41);
			jPanel.add(getJPanel5(), gridBagConstraints51);
			jPanel.add(getJPanel6(), gridBagConstraints);
		}
		return jPanel;
	}

	/**
	 * This method initializes inpFilePath	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpFilePath() {
		if (inpFilePath == null) {
			inpFilePath = new JTextField();
			inpFilePath.setPreferredSize(new Dimension(4, 20));
		}
		return inpFilePath;
	}

	/**
	 * This method initializes cmdExport	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdExport() {
		if (cmdExport == null) {
			cmdExport = new JButton();
			cmdExport.setAction(actExportToCsv_);
		}
		return cmdExport;
	}

	/**
	 * This method initializes cmdChooseFile	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdChooseFile() {
		if (cmdChooseFile == null) {
			cmdChooseFile = new JButton();
			cmdChooseFile.setText("...");
			cmdChooseFile.setFont(new Font("Dialog", Font.PLAIN, 12));
			cmdChooseFile.setPreferredSize(new Dimension(20, 20));
			cmdChooseFile.setAction(actChooseFile_);
		}
		return cmdChooseFile;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
//			jPanel1.setPreferredSize(new Dimension(233, 30));
			jPanel1.setLayout(new BoxLayout(getJPanel1(), BoxLayout.X_AXIS));
			jPanel1.setBorder(BorderFactory.createTitledBorder(null, "indent", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.PLAIN, 12), new Color(51, 51, 51)));
			jPanel1.add(getInpSpecString(), null);
			jPanel1.add(getInpChkTab(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes inpSelectTab	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getInpSelectTab() {
		if (inpSelectTab == null) {
			inpSelectTab = new JRadioButton("",true);
			inpSelectTab.setText("Tab");
			inpSelectTab.setSelected(false);
		}
		return inpSelectTab;
	}

	/**
	 * This method initializes inpSpecString	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpSpecString() {
		if (inpSpecString == null) {
			inpSpecString = new JTextField();
			inpSpecString.setPreferredSize(new Dimension(50, 20));
			inpSpecString.setText("    ");
		}
		return inpSpecString;
	}

	public void setup(Doc doc, DocModel docModel) {
		doc_ = doc;
		docModel_ = docModel;
//		workPropDao_ = (WorkPropDao)daoCont_.getComponent(WorkPropDao.class);
//		outputPropTypeDao_= (OutputPropTypeDao)daoCont_.getComponent(OutputPropTypeDao.class);
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints8.gridy = 0;
			gridBagConstraints8.anchor = GridBagConstraints.WEST;
			gridBagConstraints8.gridx = 1;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.gridy = 0;
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.setBorder(BorderFactory.createTitledBorder(null, "export to", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.PLAIN, 12), new Color(51, 51, 51)));
			jPanel2.add(getInpSelectFile(), gridBagConstraints7);
			jPanel2.add(getInpSelectClipboard(), gridBagConstraints8);
		}
		return jPanel2;
	}

	/**
	 * This method initializes inpSelectCsvFile	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getInpSelectFile() {
		if (inpSelectFile == null) {
			inpSelectFile = new JRadioButton();
			inpSelectFile.setText("file");
//			inpSelectFile.setSelected(true);
			inpSelectFile.addChangeListener(
				new ChangeListener(){
					public void stateChanged(ChangeEvent e) {
						getInpFilePath().setEnabled(inpSelectFile.isSelected());
						getCmdChooseFile().setEnabled(inpSelectFile.isSelected());
					}
				}
			);
		}
		return inpSelectFile;
	}

	/**
	 * This method initializes inpSelectClipboard	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getInpSelectClipboard() {
		if (inpSelectClipboard == null) {
			inpSelectClipboard = new JRadioButton();
			inpSelectClipboard.setText("clipboard");
		}
		return inpSelectClipboard;
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.anchor = GridBagConstraints.WEST;
			gridBagConstraints10.gridy = 2;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.gridy = 1;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.gridx = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.gridy = 1;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.ipadx = 0;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.gridx = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.insets = new Insets(1, 0, 2, 0);
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.gridx = 0;
			jPanel3 = new JPanel();
			jPanel3.setLayout(new GridBagLayout());
			jPanel3.add(getJPanel4(), gridBagConstraints4);
			jPanel3.add(getInpDepth(), gridBagConstraints5);
			jPanel3.add(getInpExpectDoubleExport(), gridBagConstraints6);
			jPanel3.add(getInpExpectCaption(), gridBagConstraints9);
			jPanel3.add(getInpNoExpandLinkNode(), gridBagConstraints10);
		}
		return jPanel3;
	}

	/**
	 * This method initializes inpChkDocText	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpChkDocText() {
		if (inpChkDocText == null) {
			inpChkDocText = new JCheckBox();
			inpChkDocText.setText("text");
		}
		return inpChkDocText;
	}

	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "export column", TitledBorder.LEADING, TitledBorder.TOP, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51));
			titledBorder.setTitleFont(new Font("Dialog", Font.PLAIN, 12));
			jPanel4 = new JPanel();
			jPanel4.setLayout(new BoxLayout(getJPanel4(), BoxLayout.X_AXIS));
			jPanel4.setBorder(titledBorder);
			jPanel4.add(getInpChkId(), null);
			jPanel4.add(getInpChkTitle(), null);
			jPanel4.add(getInpChkDocText(), null);
			jPanel4.add(getInpChkUser(), null);
			jPanel4.add(getInpChkDate(), null);
		}
		return jPanel4;
	}

	/**
	 * This method initializes inpChkDate	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpChkDate() {
		if (inpChkDate == null) {
			inpChkDate = new JCheckBox();
			inpChkDate.setText("date");
		}
		return inpChkDate;
	}

	/**
	 * This method initializes jPanel5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			TitledBorder titledBorder1 = BorderFactory.createTitledBorder(null, "file path", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null);
			titledBorder1.setBorder(null);
			titledBorder1.setTitleFont(new Font("Dialog", Font.PLAIN, 12));
			jPanel5 = new JPanel();
			jPanel5.setLayout(new BorderLayout());
			jPanel5.setBorder(titledBorder1);
			jPanel5.add(getInpFilePath(), BorderLayout.CENTER);
			jPanel5.add(getCmdChooseFile(), BorderLayout.EAST);
		}
		return jPanel5;
	}

	/**
	 * This method initializes inpChkId	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpChkId() {
		if (inpChkId == null) {
			inpChkId = new JCheckBox();
			inpChkId.setText("id");
		}
		return inpChkId;
	}

	/**
	 * This method initializes inpChkTitle	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpChkTitle() {
		if (inpChkTitle == null) {
			inpChkTitle = new JCheckBox();
			inpChkTitle.setText("title");
			inpChkTitle.setSelected(true);
		}
		return inpChkTitle;
	}

	/**
	 * This method initializes inpChkUser	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpChkUser() {
		if (inpChkUser == null) {
			inpChkUser = new JCheckBox();
			inpChkUser.setText("user");
		}
		return inpChkUser;
	}

	/**
	 * This method initializes jPanel6	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel6() {
		if (jPanel6 == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.insets = new Insets(0, 0, 0, 22);
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			jPanel6 = new JPanel();
			jPanel6.setLayout(new GridBagLayout());
			jPanel6.setBorder(BorderFactory.createTitledBorder(null, "separator", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.PLAIN, 12), new Color(51, 51, 51)));
			jPanel6.add(getInpSelectComma(), gridBagConstraints1);
			jPanel6.add(getInpSelectTab(), gridBagConstraints3);
		}
		return jPanel6;
	}

	/**
	 * This method initializes inpSelectComma	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getInpSelectComma() {
		if (inpSelectComma == null) {
			inpSelectComma = new JRadioButton();
			inpSelectComma.setText("comma");
			inpSelectComma.setSelected(true);
		}
		return inpSelectComma;
	}

	/**
	 * This method initializes inpDepth	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getInpDepth() {
		if (inpDepth == null) {
			inpDepth = new JSlider();
			inpDepth.setBorder( 
				new TitledBorder(null, "export depth", 
						TitledBorder.LEADING, TitledBorder.TOP, 
						new Font("Dialog", Font.PLAIN, 12), new Color(51, 51, 51))
			);
			inpDepth.setMinimum(0);
			inpDepth.setMaximum(20);
			inpDepth.setMinorTickSpacing(1);
			inpDepth.setMajorTickSpacing(5);
			inpDepth.setPaintTicks(true);
			inpDepth.setPaintLabels(false);
			inpDepth.setToolTipText("0の場合は末端の階層までexportします");
			inpDepth.setSnapToTicks(true);
			inpDepth.setValue(0);
		}
		return inpDepth;
	}

	/**
	 * This method initializes inpExpectDoubleExport	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpExpectDoubleExport() {
		if (inpExpectDoubleExport == null) {
			inpExpectDoubleExport = new JCheckBox();
			inpExpectDoubleExport.setText("共有ノードは2度exportしない ⇒");
		}
		return inpExpectDoubleExport;
	}

	/**
	 * This method initializes inpExpectCaption	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpExpectCaption() {
		if (inpExpectCaption == null) {
			inpExpectCaption = new JTextField();
			inpExpectCaption.setBorder(new TitledBorder("2度目以降のプレフィクス"));
			inpExpectCaption.setText("［既出］->");
		}
		return inpExpectCaption;
	}

	/**
	 * This method initializes inpChkTab	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpChkTab() {
		if (inpChkTab == null) {
			inpChkTab = new JCheckBox();
			inpChkTab.setText("tab");
			inpChkTab.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					getInpSpecString().setEnabled(!inpChkTab.isSelected());
				}
			});
		}
		return inpChkTab;
	}

	/**
	 * This method initializes inpNoExpandLinkNode	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpNoExpandLinkNode() {
		if (inpNoExpandLinkNode == null) {
			inpNoExpandLinkNode = new JCheckBox();
			inpNoExpandLinkNode.setText("link node配下は展開しない");
		}
		return inpNoExpandLinkNode;
	}
	

}  //  @jve:decl-index=0:visual-constraint="10,10"
