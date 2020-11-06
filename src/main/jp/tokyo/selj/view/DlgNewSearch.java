package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.tree.TreeNode;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.Doc2Dao;
import jp.tokyo.selj.dao.DocDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.model.DocModel.DocProcessor;
import jp.tokyo.selj.view.PnlNodeList.SelectionLintener;

import org.apache.log4j.Logger;

public class DlgNewSearch extends JDialog implements ItemListener{
	Logger log = Logger.getLogger(this.getClass());  //  @jve:decl-index=0:

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JPanel pnlHeader = null;

	private PnlNodeList pnlNodeList = null;

	private JPanel pnlSearchType = null;

	private JButton cmdNodeSearch = null;

	ButtonGroup gprSearchType_ = new ButtonGroup();  //  @jve:decl-index=0:
	DocDao docDao_;  //  @jve:decl-index=0:
	Doc2Dao doc2Dao_;
	
	private JLabel dspMsg = null;

	private JTextField inpSearchStr = null;

	private JPanel jPanel = null;

	/*
	 * 属性毎の検索
	 */
	static abstract class Searcher {
		String name_;
		DlgNewSearch owner_;
		String[] lastKeywords_ = null;
		public Searcher(DlgNewSearch owner, String name){
			name_ = name;
			owner_ = owner;
		}
		public abstract List<Doc> searchDB(String str);
		abstract boolean compareDoc(Doc doc, String str);
		boolean compareDocWholeWord(Doc doc, String searchStr){
			throw new RuntimeException("compareDocWholeWord()が呼ばれてるでぇ");
		}
		void optionCtrl(Component wholeWord, Component descentSearch){
		}
		public String getUsage(){
			return "";
		}
		public String toString(){ return name_; }
		public String[] getLastKeywords(){
			return lastKeywords_;
		}
	}
	static abstract class DefaultSearcher extends Searcher {
		public DefaultSearcher(DlgNewSearch owner, String name){
			super(owner, name);
		}
		public List<Doc> searchDB(String searchStr){
			List<Doc> docs = new ArrayList<Doc>();
			Doc doc = new Doc();
			doc.clearAll();
			
//			searchStr = searchStr.trim();
			
			lastKeywords_ = new String[]{searchStr};
			if(searchStr.length() > 0){
				searchStr = setPercent(searchStr);
			}
			doc = setDoc(doc, searchStr);

			if(owner_.inpCheckWholeWord.isEnabled() && owner_.inpCheckWholeWord.isSelected()){
				docs = owner_.docDao_.findWholeWord(doc);
			}else{
				docs = owner_.docDao_.find(doc);
			}
			return docs;
		}
		
		Doc setDoc(Doc doc, String searchStr){
			return doc;
		}
		void optionCtrl(Component wholeWord, Component descentSearch){
			wholeWord.setEnabled(true);
		}

		String setPercent(String searchStr){
			if(owner_.inpCheckWholeWord.isEnabled() && owner_.inpCheckWholeWord.isSelected()){
				//何も付けない
			}else {
				searchStr = "%" + jp.tokyo.selj.model.Util.cnvLikeWord(searchStr) + "%";
			}
			return searchStr;
		}
		String[] setPercent(String[] keywords){
			for(int i=0; i<keywords.length; i++){
				keywords[i] = setPercent(keywords[i]);
			}
			return keywords;
		}
		String[] splitSearchStr(String searchStr){
			//検索文字列を配列に分割
			String[] keywords = null;
			searchStr = searchStr.trim();
			searchStr = searchStr.replace("　", " ");
			if(searchStr.length() > 0){
				keywords = searchStr.split(" ");
			}
			return keywords;
		}
		
	}
	
	//ID検索
	private Searcher TARGET_ID= new Searcher(this, "ID"){
		public List<Doc> searchDB(String searchStr){
			try {
				List<Doc> docs = new ArrayList<Doc>();
				Doc result
				 	= docDao_.findByDocId(Long.parseLong(searchStr.trim()));
				if(result != null){
					docs.add(result);
				}else{
					owner_.inpSearchStr.setBackground(Color.YELLOW);
				}
				docDao_.findByDocId(0);	//なんのため？
				return docs;
			}catch(Exception e){
				throw new AppException("IDは数値で指定ください");
			}
		}
		void optionCtrl(Component wholeWord, Component descentSearch){
			wholeWord.setEnabled(false);
		}
		boolean compareDoc(Doc doc, String searchStr){
			try{
				return doc.getDocId() == Long.parseLong(searchStr.trim());
			}catch(Exception e){
				throw new AppException("IDは数値で指定ください");
			}
		}
		boolean compareDocWholeWord(Doc doc, String searchStr){
			return compareDoc(doc, searchStr);
		}
		public String getUsage(){
			return "IDを数値で指定してください";
		}
		public String[] getLastKeywords(){
			return null;
		}
	};
	//タイトル検索
	private Searcher TARGET_TITLE= new DefaultSearcher(this, "Title"){
		Doc setDoc(Doc doc, String searchStr){
			doc.setDocTitle(searchStr);
			return doc;
		}
		boolean compareDoc(Doc doc, String searchStr){
			lastKeywords_ = new String[]{searchStr};
			return doc.getDocTitle().toLowerCase().indexOf(searchStr.toLowerCase())  >= 0;
		}
		boolean compareDocWholeWord(Doc doc, String searchStr){
			lastKeywords_ = new String[]{searchStr};
			return doc.getDocTitle().equalsIgnoreCase(searchStr);
		}
		public String getUsage(){
			return "タイトルを「指定した文字が含まれる」で検索";
		}
	};
	
	//Text検索2
	private Searcher TARGET_TEXT= new DefaultSearcher(this, "Text"){
		
		public List<Doc> searchDB(String searchStr){
			List<Doc> docs = new ArrayList<Doc>();
			//検索文字列を配列に分割
			String[] keywords = splitSearchStr(searchStr);
			lastKeywords_ = keywords.clone();
			keywords = setPercent(keywords);

			docs = owner_.doc2Dao_.findByText(keywords);

			return docs;
		}
		boolean compareDoc(Doc doc, String searchStr){
			if(doc.getDocCont() == null){
				return false;
			}
			String[] keywords = splitSearchStr(searchStr);
			boolean ret=true;
			for(String keyword:keywords){
				if(doc.getDocCont().toLowerCase().indexOf(keyword.toLowerCase()) <= -1){
					ret = false;
					break;
				}
			}
			return ret;
		}
		public String getUsage(){
			return "テキストを「指定した文字が含まれる」で検索。スペース区切りでAND検索。";
		}
		void optionCtrl(Component wholeWord, Component descentSearch){
			wholeWord.setEnabled(false);
		}
	};

	//作成者検索
	private Searcher TARGET_CREATOR= new DefaultSearcher(this, "Creator"){
		Doc setDoc(Doc doc, String searchStr){
			if(searchStr.trim().length() <= 0){
				doc.setUserName("");
			}else{
				doc.setUserName(searchStr);
			}
			return doc;
		}
		boolean compareDoc(Doc doc, String searchStr){
			lastKeywords_ = new String[]{searchStr};
			return doc.getUserName().toLowerCase().indexOf(searchStr.toLowerCase())  >= 0;
		}
		boolean compareDocWholeWord(Doc doc, String searchStr){
			lastKeywords_ = new String[]{searchStr};
			return doc.getUserName().equalsIgnoreCase(searchStr);
		}
		public String getUsage(){
			return "作成者を「指定した文字が含まれる」で検索";
		}
		public String[] getLastKeywords(){
			return null;
		}
	};
	//日付検索タイプ
	enum CompType { 
		BEFORE_EQ("<="){
			public List<Doc> searchDB(DocDao docDao, DocDao.DateCondition dc){
				dc.from = null;
				return docDao.findByDateEQ(dc);
			}
		}, 
		AFTER_EQ(">="){
			public List<Doc> searchDB(DocDao docDao, DocDao.DateCondition dc){
				dc.to = null;
				return docDao.findByDateEQ(dc);
			}
		},  
		BEFORE("<"){
			public List<Doc> searchDB(DocDao docDao, DocDao.DateCondition dc){
				dc.from = null;
				return docDao.findByDate(dc);
			}
		},  
		AFTER(">"){
			public List<Doc> searchDB(DocDao docDao, DocDao.DateCondition dc){
				dc.to = null;
				return docDao.findByDate(dc);
			}
		},   
		EQUAL("="){
			public List<Doc> searchDB(DocDao docDao, DocDao.DateCondition dc){
				return docDao.findByDateEQ(dc);
			}
		};  
		//---------------------------
		private String name_;
		private CompType (String name) {
		    name_ = name;
		}
		public String toString() {
		    return name_;
		}
		public List<Doc> searchDB(DocDao docDao, DocDao.DateCondition dc){
			return null;
		}
	};
	//日付検索
	private Searcher TARGET_DATE= new Searcher(this, "Date"){
		String PATTERN = "yyyy/MM/dd HH:mm:ss";
		DateFormat df = new SimpleDateFormat (PATTERN);
		CompType compType_;
		
		public List<Doc> searchDB(String searchStr){
			if(searchStr==null || searchStr.trim().length() <= 0){
				throw new AppException(getUsage());
			}

			newSarchStrAndSetCompType(searchStr);
			return compType_.searchDB(owner_.docDao_, makeDateCondition(searchStr));
		}
		DocDao.DateCondition makeDateCondition(String searchStr){
			String dt1=searchStr, dt2=searchStr;
			//ハイフン区切りで期間指定
			int ind = searchStr.indexOf("-");
			if(ind > -1){
				dt1 = searchStr.substring(0, ind).trim();
				dt2 = searchStr.substring(ind+1, searchStr.length()).trim();
			}
			DocDao.DateCondition dc = new DocDao.DateCondition();
			if(dt1 != null && dt1.length() > 0){
				dc.from = cnvToTimestamp(dt1 + " 00:00:00");
			}
			if(dt2 != null && dt2.length() > 0){
				dc.to = cnvToTimestamp(dt2 + " 23:59:59");
			}
			return dc;
		}
		boolean compareDoc(Doc doc, String searchStr){
			boolean ret = true;
			DocDao.DateCondition dc = makeDateCondition(searchStr);
			if(dc.from != null){
				ret = dc.from.equals(doc.getNewDate()) | dc.from.before(doc.getNewDate());
			}
			if(dc.to != null){
				ret = ret && (dc.to.equals(doc.getNewDate()) | dc.to.after(doc.getNewDate()));
			}
			return ret;
		}
		void optionCtrl(Component wholeWord, Component descentSearch){
			wholeWord.setEnabled(false);
		}
		boolean compareDocWholeWord(Doc doc, String searchStr){
			return compareDoc(doc, searchStr);
		}
		String newSarchStrAndSetCompType(String str){
			compType_ = CompType.EQUAL;
// **** 演算子機能は止めた
//			str = str.trim();
//			for(CompType ct : CompType.values()){
//				int ind = str.indexOf(ct.toString());
//				if( ind > -1 ){
//					compType_ = ct;
//					str = str.substring(ct.toString().length()).trim();
//					break;
//				}
//			}
			return str;
		}
		Timestamp cnvToTimestamp(String str){
			str = newSarchStrAndSetCompType(str);
			
			Date d = null;
			try {
				d = df.parse(str);
				return new Timestamp(d.getTime());
			} catch (ParseException e) {
				throw new AppException(getUsage());
			}
		}
		public String getUsage(){
			return "yyyy/MM/dd形式で日付検索。[from] - [to]で期間指定。例）2008/8/1、2008/8/1-、-2008/8/1、2008/8/1-2008/8/31";
		}
		public String[] getLastKeywords(){
			return null;
		}
	};
	
	//タイトルorText検索
	private Searcher TARGET_TITLE_OR_TEXT= new DefaultSearcher(this, "Title or Text"){
		Doc setDoc(Doc doc, String searchStr){
			doc.setDocCont(searchStr);
			doc.setDocTitle(searchStr);
			return doc;
		}
		boolean compareDoc(Doc doc, String searchStr){
			lastKeywords_ = new String[]{searchStr};
			return doc.getDocTitle().toLowerCase().indexOf(searchStr.toLowerCase())  >= 0
				| ((doc.getDocCont()==null)? 
						false:doc.getDocCont().toLowerCase().indexOf(searchStr.toLowerCase())  >= 0);
		}
		public String getUsage(){
			return "タイトルとテキスト部を「指定した文字が含まれる」で検索します";
		}
		void optionCtrl(Component wholeWord, Component descentSearch){
			wholeWord.setEnabled(false);
		}
	};

	//Text検索2
//	private Searcher TARGET_TITLE_OR_TEXT2= new DefaultSearcher(this, "Title or Text"){
//		
//		public List<Doc> searchDB(String searchStr){
//			List<Doc> docs = new ArrayList<Doc>();
//			//検索文字列を配列に分割
//			String[] keywords = splitSearchStr(searchStr);
//
//			docs = owner_.doc2Dao_.findByTitleOrText(keywords);
//
//			return docs;
//		}
//		
//		boolean compareDoc(Doc doc, String searchStr){
//			if(compareTitle(doc, searchStr)){
//				return true;
//			}else{
//				return compareText(doc, searchStr);
//			}
//		}
//		boolean compareTitle(Doc doc, String searchStr){
//			if(doc.getDocCont() == null){
//				return false;
//			}
//			String[] keywords = splitSearchStr(searchStr);
//			boolean ret=true;
//			//Text
//			for(String keyword:keywords){
//				if(doc.getDocTitle().toLowerCase().indexOf(keyword.toLowerCase()) <= -1){
//					ret = false;
//					break;
//				}
//			}
//			return ret;
//		}
//		boolean compareText(Doc doc, String searchStr){
//			if(doc.getDocCont() == null){
//				return false;
//			}
//			String[] keywords = splitSearchStr(searchStr);
//			boolean ret=true;
//			//Text
//			for(String keyword:keywords){
//				if(doc.getDocCont().toLowerCase().indexOf(keyword.toLowerCase()) <= -1){
//					ret = false;
//					break;
//				}
//			}
//			return ret;
//		}
//		public String getUsage(){
//			return "タイトルとテキスト部を「指定した文字が含まれる」で検索。スペース区切りでAND検索。";
//		}
//		void optionCtrl(Component wholeWord, Component descentSearch){
//			wholeWord.setEnabled(false);
//		}
//	};

	class DocWithParentPath extends Doc{
		List<Doc> parents_;
		DocWithParentPath(Doc doc, List<Doc> parents){
			copyDoc(doc);
			parents_ = parents;
		}
	}
	
	Runnable focusToResult_ = new Runnable(){
		public void run() {
			pnlNodeList.requestFocusToList();
		}
	};
	class ActNodeSearch extends AbstractAction {
		Searcher lastSearcher_;
		//検索結果の表示アクションで呼び出される
		jp.tokyo.selj.view.PnlNodeList.SelectionLintener selectionLintener_ =
			new SelectionLintener(){
				public void process(Doc doc) {
					inpSearchStr.setBackground(Color.WHITE);	//Text検索で別な色になっているかもしれないのでリセット
					FrmZeetaMain main = (FrmZeetaMain)getOwner();
					DocNode n = main.showDocNode(doc.getDocId(), false);
//					main.showDetailAndSelectWord(n, inpSearchStr.getText());
					main.showDetailAndSelectWord(n, lastSearcher_.getLastKeywords());
					SwingUtilities.invokeLater(focusToResult_);
				}
				public void setRootNode(DocNode rootNode){}
				
			};
		//検索結果の表示アクションで呼び出される
		jp.tokyo.selj.view.PnlNodeList.SelectionLintener selectionLintener2_ =
			new SelectionLintener(){
				DocNode rootNode_;
				public void process(Doc doc) {
					inpSearchStr.setBackground(Color.WHITE);	//Text検索で別な色になっているかもしれないのでリセット
					FrmZeetaMain main = (FrmZeetaMain)getOwner();
					DocNode n = main.showDocNode(rootNode_, doc.getDocId(), 
								((DocWithParentPath)doc).parents_, false);
//					main.showDetailAndSelectWord(n, inpSearchStr.getText());
					main.showDetailAndSelectWord(n, lastSearcher_.getLastKeywords());
					SwingUtilities.invokeLater(focusToResult_);
				}
				public void setRootNode(DocNode rootNode){
					rootNode_ = rootNode;
				}
			};
		public ActNodeSearch() {
			putValue(Action.NAME, "node");
			putValue(Action.SHORT_DESCRIPTION, "ノードを検索(enter)");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/nodeSrch.gif")));

		}
		public void actionPerformed(ActionEvent e) {
			inpSearchStr.setBackground(Color.WHITE);	//Text検索で別な色になっているかもしれないのでリセット
			
			String str = inpSearchStr.getText();
			Searcher searcher = (Searcher)inpTarget.getSelectedItem();
			lastSearcher_ = searcher;
			
			setCursor(Util.WAIT_CURSOR);
			try{
				//検索
				List<Doc> docs = null;
				if(inpSelDescentSearch.isSelected()){
					docs = searchUnderSelectedNode(searcher, str);
					FrmZeetaMain main = (FrmZeetaMain)getOwner();
					selectionLintener2_.setRootNode(main.viewState_.getCurrentNode());
					pnlNodeList.setup(docs, selectionLintener2_);
				}else{
					docs = searcher.searchDB(str);
					pnlNodeList.setup(docs, selectionLintener_);
				}
				if(docs.size() <= 0){
					inpSearchStr.setBackground(Color.YELLOW);
				}
				pnlNodeList.requestFocusToList();
			}catch(AppException ex){
				inpSearchStr.selectAll();
				SwingUtilities.invokeLater(requestFocusToInpDocCont_);
				throw ex;
			}finally{
				setCursor(Cursor.getDefaultCursor());
			}
			dspMsg.setText("hit node count : " + pnlNodeList.getDocs().size());
		}
		List<Doc> searchUnderSelectedNode(Searcher searcher, String searchStr){
			//普通に階層を辿って検索するととてつもなく遅い
			//そのため、一旦普通に検索してから、その親にカレントノードがふくまれているか
			//検査する方式とする
			
			class TempSearcher{
				Doc targetDoc_;
				List<Doc> path_ = null;
				TempSearcher(DocNode targetDocNode){
					targetDoc_ = targetDocNode.getDoc();
				}
				boolean isExist(DocNode node){
					if( isExist2(node) ){
						path_.add(node.getDoc());
						return true;
					}
					return false;
				}
				//nodeの親にtargetDoc_が含まれているかを検査する
				boolean isExist2(DocNode node){
					if( node.getDoc().getDocId() == targetDoc_.getDocId()){
						path_ = new ArrayList<Doc>();
						return true;
					}else{
						Enumeration<DocNode> children = (Enumeration<DocNode>)node.children();
						while( children.hasMoreElements()){
							DocNode child = children.nextElement();
							if(child.isRoot()){
								//do nothing
							}else if(isExist2(child)){
								path_.add(child.getDoc());
								return true;
							}
						}
					}
					return false;
				}
			}
			//カレントノード
			FrmZeetaMain main = (FrmZeetaMain)getOwner();
			DocNode curNode = main.viewState_.getCurrentNode(); 
			TempSearcher ts = new TempSearcher(curNode);
			
			
			//まずは、フツーに検索
			List<Doc> searchResult = searcher.searchDB(searchStr);
			
			//検索結果個々の親にcurNodeが、含まれていたらresultに登録
			List<Doc> result = new ArrayList<Doc>();
int cnt=0;			
			for(Doc doc:searchResult){
log.debug(""+(++cnt)+"/"+searchResult.size());
				DocNode revTop = new DocNode(doc);	//docのコピーを作成
				revTop = main.docModel_.addParentDocFromDbAll(revTop);	//revTopは、docの親達の塊なのだ
log.debug("\t"+"revTop 完成");
				if( ts.isExist(revTop)){
					result.add(
						new DocWithParentPath(revTop.getDoc(), ts.path_)
					);
				}
log.debug("\t"+"isExist 完了");
			}

			return result;
			
/*			
			class DescentSearcher implements DocProcessor{
				List<Doc> docs_ = new ArrayList<Doc>();
				String searchStr_;
				Searcher searcher_;
				public DescentSearcher(Searcher searcher, String str){
					searchStr_ = str.toLowerCase();
					searcher_ = searcher;
				}
				public boolean process(Doc doc, List<Doc> parents){

					boolean hit=false;
					if(inpCheckWholeWord.isEnabled() && inpCheckWholeWord.isSelected()){
						if(searcher_.compareDocWholeWord(doc, searchStr_)){
							hit = true;
							addDoc(doc, parents);
						}
					}else{
						if(searcher_.compareDoc(doc, searchStr_)){
							hit = true;
							addDoc(doc, parents);
						}
					}
					if( searcher_ == TARGET_ID && hit){
						return false;
					}else{
						return true;	//続行
					}
				}
				void addDoc(Doc doc, List<Doc> parents){
					for(Doc elm: docs_){
						if(elm.getDocId() == doc.getDocId()){
							if( ((DocWithParentPath)elm).parents_.size() > parents.size() ){
								//パスの浅い方を選ぶ
								elm.copyDoc(doc);
								List<Doc> newPath = new ArrayList<Doc>(parents);
								newPath.add(doc);
								((DocWithParentPath)elm).parents_ = newPath;
								return;
							}else{
								return;
							}
						}
					}
					List<Doc> newPath = new ArrayList<Doc>(parents);
					newPath.add(doc);
					docs_.add(new DocWithParentPath(doc, newPath));
				}
			}
			DescentSearcher proc = new DescentSearcher(searcher, searchStr);
			
			FrmZeetaMain main = (FrmZeetaMain)getOwner();
			main.docModel_.processAllDoc2(main.viewState_.getCurrentNode().getDoc(), 
						proc, new ArrayList<Doc>());
			return proc.docs_;
*/
			
//			selectionLintener2_.setRootNode(main.viewState_.getCurrentNode());
//			pnlNodeList.setup(proc.docs_, selectionLintener2_);
//			pnlNodeList.requestFocusToList();
		}
		void searchDb(String searchStr){
		}
	}
	Action actSearch_ = new ActNodeSearch();  //  @jve:decl-index=0:

	class ActTextSearch extends AbstractAction {
		public ActTextSearch() {
			putValue(Action.NAME, "in text");
			putValue(Action.SHORT_DESCRIPTION, "選択中ノードのテキストを検索");
			putValue(Action.SMALL_ICON, 
					new ImageIcon(getClass().getResource("/image/textSrch.gif")));

		}
		public void actionPerformed(ActionEvent e) {
			//検索文字列を指定しているかチェック
			String str = inpSearchStr.getText();
			if(str == null || str.trim().length() <= 0){
				pnlNodeList.clear();
				return;
			}
			if( ((FrmZeetaMain)getOwner()).findText(str) ){
				inpSearchStr.setBackground(Color.WHITE);
			}else{
				//見つからない場合
				inpSearchStr.setBackground(Color.YELLOW);
			}
		}
	}
	Action actTextSearch_ = new ActTextSearch();  //  @jve:decl-index=0:
	
	/**
	 * This is the default constructor
	 */
	public DlgNewSearch(Frame owner) {
		super(owner);
		initialize();
	}
	PreferenceWindowHelper pref_;

	private JCheckBox inpCheckWholeWord = null;

	private JCheckBox inpSelDescentSearch = null;

	private JPanel cntButtons = null;

	private JButton cmdTextSearch = null;

	private JComboBox inpTarget = null;

	public void setup(){
		docDao_ = (DocDao)SelJDaoContainer.SEL_DAO_CONT.getComponent(DocDao.class);
		doc2Dao_ = (Doc2Dao)SelJDaoContainer.SEL_DAO_CONT.getComponent(Doc2Dao.class);
		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
	}
	public void setRequestFocusToInput(){
		getInpSearchStr().requestFocus();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(569, 200);
		this.setContentPane(getJContentPane());
		this.setTitle("search");
		this.setMinimumSize(new Dimension(500,200));	//jdk1.5以前は効かないようだ
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			BorderLayout borderLayout1 = new BorderLayout();
			borderLayout1.setHgap(5);
			borderLayout1.setVgap(1);
			jContentPane = new JPanel();
			jContentPane.setLayout(borderLayout1);
			jContentPane.add(getPnlHeader(), BorderLayout.NORTH);
			jContentPane.add(getPnlNodeList(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes pnlHeader	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPnlHeader() {
		if (pnlHeader == null) {
			dspMsg = new JLabel();
			dspMsg.setText("ID,Date以外は、「指定した文字が含まれる」で検索。Dateは、yyyy/MM/dd形式、[from] - [to]で期間指定");
			dspMsg.setFont(new Font("Dialog", Font.PLAIN, 12));
			dspMsg.setForeground(Color.BLUE);
			BorderLayout borderLayout = new BorderLayout();
			borderLayout.setHgap(0);
			borderLayout.setVgap(0);
			pnlHeader = new JPanel();
			pnlHeader.setLayout(borderLayout);
			pnlHeader.setBorder(new BevelBorder(BevelBorder.LOWERED));
			pnlHeader.add(getPnlSearchType(), BorderLayout.CENTER);
			pnlHeader.add(getPnlSearchType(), java.awt.BorderLayout.CENTER);
			pnlHeader.add(dspMsg, BorderLayout.NORTH);
			pnlHeader.add(getCntSrchStr(), BorderLayout.SOUTH);
		}
		return pnlHeader;
	}

	/**
	 * This method initializes pnlNodeList	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private PnlNodeList getPnlNodeList() {
		if (pnlNodeList == null) {
			pnlNodeList = new PnlNodeList();
		}
		return pnlNodeList;
	}

	/**
	 * This method initializes pnlSearchType	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPnlSearchType() {
		if (pnlSearchType == null) {
			pnlSearchType = new JPanel();
			pnlSearchType.setBorder(new BevelBorder(BevelBorder.LOWERED));
			pnlSearchType.setLayout(new BoxLayout(getPnlSearchType(), BoxLayout.X_AXIS));
			pnlSearchType.add(getInpCheckWholeWord(), null);
			pnlSearchType.add(getInpSelDescentSearch(), null);
		}
		return pnlSearchType;
	}

	/**
	 * This method initializes cmdNodeSearch	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdNodeSearch() {
		if (cmdNodeSearch == null) {
			cmdNodeSearch = new JButton();
			cmdNodeSearch.setAction(actSearch_);
		}
		return cmdNodeSearch;
	}
	/**
	 * This method initializes inpSearchStr	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpSearchStr() {
		if (inpSearchStr == null) {
			inpSearchStr = new JTextField();
			inpSearchStr.getInputMap().
				put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "search");
			inpSearchStr.getActionMap().put("search", actSearch_);
		}
		return inpSearchStr;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntSrchStr() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			jPanel.add(getInpSearchStr(), BorderLayout.CENTER);
			jPanel.add(getCntButtons(), BorderLayout.EAST);
			jPanel.add(getInpTarget(), BorderLayout.WEST);
		}
		return jPanel;
	}
	/**
	 * This method initializes inpCheckWholeWord	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpCheckWholeWord() {
		if (inpCheckWholeWord == null) {
			inpCheckWholeWord = new JCheckBox();
			inpCheckWholeWord.setText("whole word");
			inpCheckWholeWord.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return inpCheckWholeWord;
	}
	/**
	 * This method initializes inpSelDescentSearch	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpSelDescentSearch() {
		if (inpSelDescentSearch == null) {
			inpSelDescentSearch = new JCheckBox();
			inpSelDescentSearch.setText("descent search");
			inpSelDescentSearch.setFont(new Font("Dialog", Font.BOLD, 12));
			inpSelDescentSearch.setToolTipText("カレントノード配下を検索します。多少時間がかかります。");
		}
		return inpSelDescentSearch;
	}
	/**
	 * This method initializes cntButtons	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntButtons() {
		if (cntButtons == null) {
			cntButtons = new JPanel();
			cntButtons.setLayout(new BorderLayout());
			cntButtons.add(getCmdNodeSearch(), BorderLayout.WEST);
			cntButtons.add(getCmdTextSearch(), BorderLayout.EAST);
		}
		return cntButtons;
	}
	/**
	 * This method initializes cmdTextSearch	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdTextSearch() {
		if (cmdTextSearch == null) {
			cmdTextSearch = new JButton();
			cmdTextSearch.setAction(actTextSearch_);
		}
		return cmdTextSearch;
	}
	/**
	 * This method initializes inpTarget	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpTarget() {
		if (inpTarget == null) {
			inpTarget = new JComboBox();
			inpTarget.addItemListener(this);
			inpTarget.addItem(TARGET_TITLE_OR_TEXT);
			inpTarget.addItem(TARGET_ID);
			inpTarget.addItem(TARGET_TITLE);
			inpTarget.addItem(TARGET_TEXT);
			inpTarget.addItem(TARGET_CREATOR);
			inpTarget.addItem(TARGET_DATE);
		}
		return inpTarget;
	}
	public void itemStateChanged(ItemEvent e) {
		Searcher searcher = (Searcher)inpTarget.getSelectedItem();
//			inpSearchStr.setText(searcher.getUsage());
		inpSearchStr.setToolTipText(searcher.getUsage());
		dspMsg.setText(searcher.getUsage());
		searcher.optionCtrl(
				getInpCheckWholeWord(), 
				getInpSelDescentSearch());
		inpSearchStr.selectAll();
		SwingUtilities.invokeLater(requestFocusToInpDocCont_);
	}
	Runnable requestFocusToInpDocCont_ = new Runnable(){
		public void run(){
			inpSearchStr.requestFocus();
		}
	};
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
