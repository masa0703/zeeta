package jp.tokyo.selj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.transaction.TransactionManager;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.DocDao;
import jp.tokyo.selj.dao.DocStr;
import jp.tokyo.selj.dao.DocStrDao;
import jp.tokyo.selj.dao.DocStrSearchCondition;
import jp.tokyo.selj.dao.SelJDaoContainer;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DocModel extends DefaultTreeModel{
	Logger log = Logger.getLogger(this.getClass());
	
	public static class DoNothingException extends RuntimeException{
		DocNode node_;
		DoNothingException(DocNode nd){
			node_ = nd;
		}
		public DocNode getDocNode(){
			return node_;
		}
	}
	
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;
	DocDao docDao_ = null;
	DocStrDao docStrDao_ = null;
	TransactionManager trn_ = null;
	Comparator docComparator_ =
		new Comparator<Doc>(){
			public int compare(Doc o1, Doc o2) {
				return (int)(o1.getDocId() - o2.getDocId());
			}
		};

	static final String ROOT_TITLE = "（新規データ）";
	
	public static interface NodeProcessor{
		/** @return true-続行、false-停止 */
		public boolean process(DocNode node);
	}
	public static interface DocProcessor{
		/** @return true-続行、false-停止 */
		public boolean process(Doc doc, List<Doc> parents);
	}
	public DocModel(){
		super(null);
		docDao_ = (DocDao) daoCont_.getComponent(DocDao.class);
		docStrDao_ = (DocStrDao) daoCont_.getComponent(DocStrDao.class);
    	trn_ = (TransactionManager)daoCont_.getComponent(TransactionManager.class);
    }
	public DocStr getDocStr(long parent, long child){
		return docStrDao_.find(parent, child);
	}	
	
	public void initialize() {
		//root要件を追加
		List roots = docDao_.findByDocTypeId(Doc.ROOT_TYPE);
		if( roots.size() <= 0 ){	//ルートなし（新規作成時）
			//一旦DBに書き込んで再読み込み
			Doc tempRoot = new Doc();
			tempRoot.setDocTypeId(Doc.ROOT_TYPE);
			tempRoot.setDocTitle(ROOT_TITLE);
			tempRoot.setDocCont("created on "+new Date());
			docDao_.insert(tempRoot);
			roots = docDao_.findByDocTypeId(Doc.ROOT_TYPE);
		}
		initialize( ((Doc)roots.get(0)).getDocId() );
		
	}
	public void initialize(long docId) {
		//s2daoは、関連テーブル１つ分のリンクしかjoinしないため、ここでDocをリロード
		Doc doc = docDao_.findByDocId(docId);
		DocNode node = new DocNode(doc);

		//子要件を追加
		DocNode top = addChildDocFromDb(node);
		
		//孫要件を追加
		top = addMagoDocFromDb(top);
		
		setRoot(top);
	}
	//TreeNode上でdocと一致するノードを全てreloadする
	public void docChanged(Doc doc){
		log.trace("start");
		class Reloader implements NodeProcessor{
			Doc doc_;
			int parents_ = -1;
			public Reloader(Doc doc){
				doc_ = doc;
			}
			public boolean process(DocNode targetNode){
				Doc target = targetNode.getDoc();
				if( doc_.getDocId() == target.getDocId()){
					target.copyDoc(doc_);
					//親の数をここで入れておく
					if( parents_ == -1){
						targetNode.resetParentCount(docStrDao_);
						parents_ = targetNode.getParentCount();
					}else{
						targetNode.setParentCount(parents_);
					}
					nodeChanged(targetNode);
				}
				return true;	//続行
			}
		}
		processAllNode(new Reloader(doc));
		log.trace("end");
	}
	
	public DocNode searchFirstDocNode(long id){
		log.trace("start");
		class Searcher implements NodeProcessor{
			long id_;
			DocNode node_;
			public Searcher(long id){
				id_ = id;
			}
			public boolean process(DocNode node){
				Doc target = node.getDoc();
				if( id_ == target.getDocId()){
					node_ = node;
					return false;	//終了
				}
				return true;	//続行
			}
		}
		Searcher proc = new Searcher(id);
		processAllNode(proc);
		log.trace("end");
		return proc.node_;
	}
	public List<Long> getPathTo(DocNode startNode, long id){
		List<Long> path = new ArrayList<Long>();
		path = getPathTo(path, startNode.getDoc(), id);
		if( path != null){
			path.add(0, startNode.getDoc().getDocId());
		}else{
			throw new AppException("カレントノードから id=" + id 
					+" へ至るパスが、他のセッションによって変更されています。");
		}
		return path;
	}
	public List<Long> getPathTo(List<Long> path, Doc startDoc, long id){
		List<DocStr> children = docStrDao_.findByOyaDocId(startDoc.getDocId());
		if( children != null){
			for(DocStr docStr: children){
				if(docStr.getKoDocId() == id){
					path.add(id);
					return path;
				}
			}
			//見つからなかった場合子供を検索
			for(DocStr docStr: children){
				Doc doc = docDao_.findByDocId(docStr.getKoDocId());
				List<Long> ret = getPathTo( path, doc, id);
				if(ret != null){
					path.add(0, doc.getDocId());
					return path;
				}
			}
		}
		return null;
	}	
	public List<Long> getPathToDocRoot(long id){
		log.trace("start");
		List<Long> path = new ArrayList<Long>();
		path = getPathToDocRoot(path, id);
		
		//先頭がRootタイプか検査する
		if( path.size() <= 0 ){
			throw new RuntimeException("getPathToDocRoot()のエントリが0");
		}else{
			Doc doc = docDao_.findByDocId(path.get(0));
			if( doc == null ){
				throw new AppException("id=" + path.get(0) 
						+" は、他のセッションによって既に削除されています。");
			}else{
				if( doc.getDocTypeId() != Doc.ROOT_TYPE){
					throw new AppException("id=" + id 
							+" へ至るパスが、他のセッションによって変更されています。");
				}
			}
		}
		log.trace("end");
		return path;
	}
	public List<Long> getPathToDocRoot(List<Long> path, long id){
		List<DocStr> parents = docStrDao_.findByKoDocId(id);
		
		if(parents.size() > 0){
			//一番浅いPathを選択する
			int size = Integer.MAX_VALUE;
			List<Long> miniPath = new ArrayList<Long>();
			for(int i=0; i<parents.size(); i++){
				List<Long> temp = new ArrayList<Long>(path);
				temp = getPathToDocRoot(temp, parents.get(i).getOyaDocId());
				if( size > temp.size() ){
					miniPath.clear();
					miniPath.addAll(temp);
					size = temp.size();
				}
			}
			path.clear();
			path.addAll(miniPath);
		}
		path.add(id);
		return path;
	}	
	
	//root配下の全ノードに対しLeafからNodeProcessorを行う
	public void processAllNode(NodeProcessor procNode){
		DocNode node = (DocNode)getRoot();
		processAllNode(node, procNode);
	}
	//指定node配下の全ノードに対しLeafからNodeProcessorを行う
	public boolean processAllNode(DocNode node, NodeProcessor procNode){
		Enumeration children = node.children();
		while(children.hasMoreElements()){
			DocNode child = (DocNode)children.nextElement();
			if( ! processAllNode(child, procNode) ){
				break;
			}
		}
		return procNode.process(node);
	}
	
	//指定node配下の全ノードに対し枝からNodeProcessorを行う
	public boolean processAllNode2(DocNode node, NodeProcessor procNode){
		if( ! procNode.process(node) ){
			return false;
		}
		boolean ret = true;
		Enumeration children = node.children();
		while(children.hasMoreElements()){
			DocNode child = (DocNode)children.nextElement();
			processAllNode2(child, procNode);
			if( ! ret ){
				break;
			}
		}
		return ret;
	}
	//DB上の指定doc配下の全Docに対し枝からDocProcessorを行う
	public void processAllDoc2(Doc startDoc, DocProcessor procDoc, List<Doc> parents){
		log.trace("start");
		if( ! procDoc.process(startDoc, parents) ){
			return;		//子供は処理しない
		}

		//get children
		List<DocStr> children = docStrDao_.findDocStr(
			new DocStrSearchCondition(startDoc.getDocId(),
				(startDoc.getSortType()==null)? null:startDoc.getSortType().getOrderSent())
		);
		List<Doc> tempParents = new ArrayList<Doc>(parents); 
		tempParents.add(startDoc);
		for(int i=0;i < children.size() ; i++){
			DocStr docStr = children.get(i);
			
			//注：要件構造が取得する子要件・親要件は、SotrTypeが読み込まれていない！
			Doc doc = docDao_.findByDocId(docStr.getKoDocId());
			processAllDoc2(doc, procDoc, tempParents);
		}
		log.trace("end");
//		return oyaNode;
	}


	public DocNode addMagoDocFromDb(DocNode node){
		log.trace("start");
		Enumeration children =node.children();
		while(children.hasMoreElements()){
			DocNode child = (DocNode)children.nextElement();
			child = addChildDocFromDb(child);
		}
		log.trace("end");
		return node;
	}
	public DocNode addChildDocFromDb(DocNode oyaNode){
		log.trace("start");
//		long t = System.currentTimeMillis();
		
		if(oyaNode == null){
			throw new IllegalArgumentException("node is null.");
		}
		if(oyaNode.getDoc() == null){
			throw new IllegalArgumentException("node.getDoc() is null.");
		}
		if(oyaNode.getChildCount() > 0){	//すでに子供がいる場合は、追加しない
			return oyaNode;
		}
		
//		DocDao docDao = (DocDao) daoCont_.getComponent(DocDao.class);
//		DocStrDao docStrDao = (DocStrDao) daoCont_.getComponent(DocStrDao.class);
		Doc oya = oyaNode.getDoc();

//		log.debug("oya="+oya);	
		List<DocStr> children = docStrDao_.findDocStr(
					new DocStrSearchCondition(oya.getDocId(),
							(oya.getSortType()==null)? null:oya.getSortType().getOrderSent())
				);
		
//		if(children.isEmpty()){
//			return oyaNode;
//		}
//		long[] ids = new long[children.size()];
//		for(int i=0;i < children.size() ; i++){
//			ids[i] = children.get(i).getKoDocId();
//		}
//		DocDao docDao = (DocDao) daoCont_.getComponent(DocDao.class);
//		List<Doc> kos = docDao.findByDocIds(ids);
//
//		for(Doc doc: kos){
//			DocNode newDocNode = new DocNode(doc);
//			newDocNode.resetParentCount(docStrDao_);
//			oyaNode.add(newDocNode);
//		}
		
		for(int i=0;i < children.size() ; i++){
			DocStr yk = children.get(i);
			
			//注：要件構造が取得する子要件・親要件は、SotrTypeが読み込まれていない！
			Doc doc = docDao_.findByDocId(yk.getKoDocId());
			DocNode newDocNode = new DocNode(doc);
			newDocNode.resetParentCount(docStrDao_);
			oyaNode.add(newDocNode);
		}
//		log.debug(" time = " + (System.currentTimeMillis() - t));
		log.trace("end");
		return oyaNode;
	}
	//descent search用（全世代）
	public DocNode addParentDocFromDbAll(DocNode koNode){
		log.trace("start");
		
		if(koNode == null){
			throw new IllegalArgumentException("node is null.");
		}
		if(koNode.getDoc() == null){
			throw new IllegalArgumentException("node.getDoc() is null.");
		}
		if(koNode.getChildCount() > 0){	//すでに親（実際は子供）がいる場合は、追加しない
			return koNode;
		}
		
		Doc ko = koNode.getDoc();

		List<DocStr> parents = docStrDao_.findOyaDocStr(
					new DocStrSearchCondition(ko.getDocId(),
							(ko.getSortType()==null)? null:ko.getSortType().getOrderSent())
				);
		
		for(int i=0;i < parents.size() ; i++){
			DocStr yk = parents.get(i);
			
			//注：要件構造が取得する子要件・親要件は、SotrTypeが読み込まれていない！
			Doc doc = docDao_.findByDocId(yk.getOyaDocId());
			DocNode newDocNode = new DocNode(doc);
			newDocNode.resetParentCount(docStrDao_);
			koNode.add(newDocNode);
			
			if(!newDocNode.getDoc().isRoot()){	//ルートノード以外の場合は、更に親を追加
				addParentDocFromDbAll(newDocNode);
			}
		}
		log.trace("end");
		return koNode;
	}
	// expectNodeのidと一致する、expectNode以外のノードを読み込みなおす
	void refreshChildrenFromDb(DocNode expectNode){
		long id = expectNode.getDoc().getDocId();
		log.trace("start id="+id);
		class Inserter implements NodeProcessor{
			DocNode expectNode_;
			long oyaNodeId_;
			public Inserter(long oyaNodeId, DocNode expectNode){
				oyaNodeId_ = oyaNodeId;
				expectNode_ = expectNode;
			}
			public boolean process(DocNode node){
				if(expectNode_ == node){
					return true;
				}
				Doc nodeDoc = node.getDoc();
				if( oyaNodeId_ == nodeDoc.getDocId()){
					node.removeAllChildren();
					reload(node);
					//子要件を追加
					addChildDocFromDb(node);
					//孫要件を追加
					addMagoDocFromDb(node);
				}
				return true;	//続行
			}
		}
		Inserter proc = new Inserter(id, expectNode);
		processAllNode(proc);
		log.trace("end");
	}
	// idと一致する、ノードの親数をcountに更新する
	void refreshParentCount(long id, int count){
		log.trace("start id="+id);
		class ParentCountSetter implements NodeProcessor{
			long targetId_;
			int count_;
			public ParentCountSetter(long id, int count){
				targetId_ = id;
				count_ = count;
			}
			public boolean process(DocNode node){
				Doc nodeDoc = node.getDoc();
				if( targetId_ == nodeDoc.getDocId()){
					node.setParentCount(count_);
				}
				return true;	//続行
			}
		}
		ParentCountSetter proc = new ParentCountSetter(id, count);
		processAllNode(proc);
		log.trace("end");
	}
	// idと一致する、ノードの作業数をcountに更新する
	public void refreshWorkCount(long id, int count){
		log.trace("start id="+id);
		class WorkParentCountSetter implements NodeProcessor{
			long targetId_;
			int count_;
			public WorkParentCountSetter(long id, int count){
				targetId_ = id;
				count_ = count;
			}
			public boolean process(DocNode node){
				Doc nodeDoc = node.getDoc();
				if( targetId_ == nodeDoc.getDocId()){
					nodeDoc.setWorkCount(count_);
				}
				return true;	//続行
			}
		}
		WorkParentCountSetter proc = new WorkParentCountSetter(id, count);
		processAllNode(proc);
		log.trace("end");
	}
		
	public DocNode insertDocFromAnotherProcessTrns(DocNode oyaNode, DocNode node, boolean copyCopyData)
		throws AppException{
		
		log.trace("start");
		boolean roolbackFlag = false;
		try{
			trn_.begin();
			Map<Long,Long> idMap = new HashMap<Long,Long>();
			DocNode newDocNode = null;
//			try{
				newDocNode = insertDocFromAnotherProcess2(idMap, oyaNode, node, copyCopyData);
//			}catch(DoNothingException e){
				//既に存在するノードの場合は、この例外がスローされるので無視する。
//				newDocNode = node;
//			}
			trn_.commit();
			log.trace("end");
			return newDocNode;
		}catch(AppException e){
			roolbackFlag = true;
			throw e;
		}catch(Throwable e){
			roolbackFlag = true;
			throw new RuntimeException(e);
		}finally{
			if(roolbackFlag){
				try {
					trn_.rollback();
				} catch (Exception e1) {
					//ここの例外は無視
				}
			}
		}
	}
	DocNode insertDocFromAnotherProcess2(Map<Long,Long> idMap, DocNode oyaNode, DocNode impNode, boolean copyCopyData)
		throws AppException{
		
		Doc impDoc = impNode.getDoc();
		if(impDoc.getDocTypeId() == Doc.ROOT_TYPE){
			impDoc.setDocTypeId(0);	//ルートだったらクリア
		}
		boolean isInsertedDoc = false;
    	long impDocId = impDoc.getDocId();
    	if( idMap.containsKey(impDocId)){
    		impDoc.setDocId(idMap.get(impDocId));
    		isInsertedDoc = true;
    	}else{
    		if(copyCopyData){
        		impDoc = setExistDocId(impDoc);
    		}else{
    			impDoc.setDocId(-1);
    		}
    	}
    	DocNode newDocNode = impNode;
    	try{
    		newDocNode = insertDoc2(oyaNode, impDoc);
		}catch(DoNothingException e){
			//既に存在するノードの場合は、この例外がスローされるので無視する。
    		if(copyCopyData){
        		//CopyDataバッチでコピーして編集後に再度オリジナルZeetaへコピーした場合
    		}else{
    			throw e;
    		}
		}

		idMap.put(impDocId, newDocNode.getDoc().getDocId());
		
		if( isInsertedDoc ){	//一旦追加済みのDocの場合、子供を追加する必要はない
			return newDocNode;
		}

		Enumeration<DocNode> children = impNode.children();
    	while(children.hasMoreElements()){
    		DocNode child = children.nextElement();
    		insertDocFromAnotherProcess2(idMap, newDocNode, child, copyCopyData);
    	}
		return newDocNode;
	}
	private Doc setExistDocId(Doc doc) {
		long newId = -1;
		//idだけで検索してもコピー元でも追加している別なノードがhitする可能性があるので
		//その他の属性も一致しているものを検索する。ただし、text部分は、更新されている
		//可能性があるので不一致でもかまわないとしている。
		List<Doc> existDocs = docDao_.findSameDoc(doc);
		if(existDocs != null){
			switch(existDocs.size()){
			case 0:
				break;
			case 1:
				newId = existDocs.get(0).getDocId();
//log.debug("impId=" + doc.getDocId() + ", existId="+newId);
//if( doc.getDocId() != newId){
//	log.debug("@@@@@@@@@@@@@@@@@ ちがうぞぇ doc="+doc);
//}
				break;
			default:
				throw new AppException("<html>text以外が全く同じNodeが複数存在します。" +
						"<pre>" +
						" doc="+doc);
			}
		}
		doc.setDocId(newId);
		return doc;
	}

	public DocNode insertDoc(DocNode oyaNode, Doc doc) 
	throws AppException{
		log.trace("start");
		DocNode newDocNode = insertDoc2(oyaNode, doc);
		log.trace("end");
		return newDocNode;
	}
	DocNode insertDoc2(DocNode oyaNode, Doc doc) 
		throws AppException{
			
		boolean isNewNode = false;
		//insert into DB
		if(docDao_.findByDocId(doc.getDocId())== null){
			log.debug("存在しないのでinsert new="+doc);
			if(log.isDebugEnabled()) log.debug("\tnewDoc="+doc);
			doc.check();
			docDao_.insert(doc);
			isNewNode = true;
		}

		//Node link 
		if( !linkDoc(oyaNode, doc, !isNewNode) ){
			//このケースは、oyaNode配下に既に存在するのでlinkDocしなかったケース
			Enumeration children = oyaNode.children();
			while(children.hasMoreElements()){
				DocNode existNode = (DocNode)children.nextElement();
				if(existNode.getDoc().getDocId() == doc.getDocId()){
					//このままexistNodeをリタンするとctrl+Xで移動の場合、
					//existNodeを削除してしまうバグがあった。
					//このため、同じ位置に移動する場合は、例外をスローして
					//通知することにする。
//					return existNode;
					throw new DoNothingException(existNode);
				}
			}
			//oyaNodeが新しいノード場合は、見つからないので下で作成する
		}
		
		//Model更新
		DocNode newDocNode = new DocNode(doc);
		insertNodeInto(newDocNode, oyaNode, oyaNode.getChildCount());
		newDocNode.resetParentCount(docStrDao_);
		
//		if(!isNewNode){
			//他の親の表示を更新
			refreshChildrenFromDb( oyaNode );
			refreshParentCount(
					newDocNode.getDoc().getDocId(),
					newDocNode.getParentCount()
					);
//		}
		return newDocNode;
		
	}
	// true-linkを作成した、false-linkを作成していない
	boolean linkDoc(DocNode oyaNode, Doc doc, boolean needCheck) 
		throws AppException{
		Doc oyaDoc = oyaNode.getDoc();

		if( needCheck ){
			//追加できるかチェックする
			switch(canInsert(oyaDoc, doc)){
			case 1:		//すでにlinkがある
				return false;
			case 0:
				break;
			default:
				log.error("循環参照　p="+oyaDoc+",c="+doc);
				throw new AppException("循環参照になるためノードの関連を作成できません。");
			}
		}
		
		DocStr yk = new DocStr();
		yk.setOyaDocId(oyaDoc.getDocId());
		yk.setKoDocId(doc.getDocId());
		//SEQ
		yk.setSEQ(docStrDao_.getMaxSeq(oyaDoc.getDocId())+1);
		log.debug("insert doc="+yk);
		
		docStrDao_.insert(yk);
		
		return true;
	}
	// ret = 1:すでに関係がある、0:関係がないので作成してよい、-1:循環参照
	int canInsert(Doc oyaDoc, Doc doc){
		Set<Long> idSet = new HashSet<Long>();
		
		//oyaDocの子供を集める
		List kouzouList = docStrDao_.findByOyaDocId(oyaDoc.getDocId());
		for(Iterator<DocStr> it = kouzouList.iterator(); it.hasNext();){
			idSet.add(it.next().getKoDocId());
		}
		
		//ここで一旦チェック
		if(idSet.contains(doc.getDocId())){
			return 1;
		}
		
		//親要件の先祖を全て集める
		idSet = collectParent(idSet, oyaDoc.getDocId());

		//再度チェック
		if(idSet.contains(doc.getDocId())){
			return -1;
		}
		
		return 0;
	}
	Set<Long> collectParent(Set<Long> idSet, long oyaDocId){
		List kouzouList = docStrDao_.findByKoDocId(oyaDocId);
		for(Iterator<DocStr> it = kouzouList.iterator(); it.hasNext();){
			DocStr yk = it.next();
			idSet = collectParent(idSet, yk.getOyaDocId());
		}
		idSet.add(oyaDocId);
		return idSet;
	}
	void removeDocNodeFromParent(long id, long oyaId){
		log.trace("start");
		class Remover implements NodeProcessor{
			long id_;
			long oyaId_;
			public Remover(long id, long oyaId){
				id_ = id;
				oyaId_ = oyaId;
			}
			public boolean process(DocNode node){
				Doc target = node.getDoc();
				if( id_ == target.getDocId()){
					DocNode parent = (DocNode)node.getParent();
					if(oyaId_ == parent.getDoc().getDocId()){
						removeNodeFromParent((MutableTreeNode)node);
					}
				}
				return true;	//続行
			}
		}
		Remover proc = new Remover(id, oyaId);
		processAllNode(proc);
		log.trace("end");
	}

	public void deleteDoc(DocNode removeNode) {
		log.trace("start");
		Doc doc = removeNode.getDoc();
		Doc parent = ((DocNode)removeNode.getParent()).getDoc();
		deleteDoc(parent.getDocId(), doc);

		//モデルの更新（Treeに読み込まれているノード全てを削除）
		removeDocNodeFromParent(
				removeNode.getDoc().getDocId(),
				parent.getDocId()
				);
		removeNode.resetParentCount(docStrDao_);
		if(removeNode.getParentCount() > 0){	//まだ誰かの子供になっている
			refreshParentCount(
					removeNode.getDoc().getDocId(),
					removeNode.getParentCount()
					);
		}
		log.trace("end");
	}
	public void deleteDocTrns(DocNode removeNode) {
		log.trace("start");
		try{
			trn_.begin();
			deleteDoc(removeNode);		
			trn_.commit();
			log.trace("end");
		}catch(Throwable e){
			try {
				trn_.rollback();
			} catch (Exception e1) {
				//ここの例外は無視
			}
			throw new RuntimeException(e);
		}
	}
	void deleteDoc(long parentId, Doc removeDoc){
		long removeId = removeDoc.getDocId();
		//要件構造の削除
		docStrDao_.delete(new DocStr(parentId, removeId));
		
		//他の親が存在しなければ要件を削除
		int parentCount = docStrDao_.getParentCount(removeId);
		if(parentCount <= 0){
			//まず子供を削除
			List<DocStr> children = docStrDao_.findByOyaDocId(removeId);
			for(int i=0; i<children.size(); i++){
				//ここで再読み込みするのは、いかかがなものかと思われるが、
				//削除の場合だけなのでよしとする。
				Doc koDoc = docDao_.findByDocId(children.get(i).getKoDocId());
				deleteDoc(removeId, koDoc);
			}
			//自身を削除
			docDao_.delete(removeDoc);
		}
	}

	public Doc reloadDoc(DocNode node) {
		log.trace("start");
		if(log.isDebugEnabled()) log.debug("\tDocId="+node.getDoc().getDocId());
		Doc newDoc = docDao_.findByDocId(
					node.getDoc().getDocId()
				);
		if(newDoc != null){
			node.setUserObject(newDoc);	//新しいdocに入れ替える
			docChanged(newDoc);
		}else{
			//消されてもうた。どないしよ。JTree上から消せばええんや
			node.removeFromParent();
		}
		log.trace("end");
		return newDoc;
	}

	public void updateDoc(Doc doc) {
		log.trace("start");
		doc.check();
		if(log.isDebugEnabled()) log.debug("\t更新前　doc="+doc);
		docDao_.update(doc);
		if(log.isDebugEnabled()) log.debug("\t更新後　doc="+doc);
//		youkenChanged(youken); 
		log.trace("end");
	}

	public List<Doc> getParents(long id) {
		log.trace("start");
		List<DocStr> parents = docStrDao_.findByKoDocId(id);
		List<Doc> ret = new ArrayList<Doc>();
		for(int i=0;i<parents.size();i++){
			long oyaId = parents.get(i).getOyaDocId();
			ret.add(docDao_.findByDocId(oyaId));
		}
		Collections.sort(ret, docComparator_);
		log.trace("end");
		return ret;
	}

	public void renumberSequence(DocNode oyaNode) {
		log.trace("start");
		Enumeration<DocNode> children = oyaNode.children();
		int seq = 0;
		Doc oyaDoc = oyaNode.getDoc();
			
		while(children.hasMoreElements()){
			seq++;
			Doc koDoc = children.nextElement().getDoc();
		    DocStr yk = docStrDao_.find(oyaDoc.getDocId(), koDoc.getDocId());
		    if( yk == null ){
		    	throw new AppException("このレベルのツリー構造が変更されています。\n" +
		    			"親ノードを選択してrefreshしてください。");
		    }
		    yk.setSEQ(seq);
		    docStrDao_.update(yk);
			log.debug("koDoc="+koDoc);
		}
		
		log.trace("end");
	}

}
