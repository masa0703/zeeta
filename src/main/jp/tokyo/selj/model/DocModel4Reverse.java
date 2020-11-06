package jp.tokyo.selj.model;

import java.util.Enumeration;
import java.util.List;

import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.DocStr;
import jp.tokyo.selj.dao.DocStrSearchCondition;

public class DocModel4Reverse extends DocModel{
	public void initializeReverseModel(DocNode curNode) {
		DocNode node = new DocNode(curNode.getDoc());	//素のNodeを作成せにゃあかん
		
		//親要件を全て追加
//		DocNode top = addAllParentDocFromDb(node);
		
		//子要件を追加
		DocNode top = addParentDocFromDb(node);
		
		//孫要件を追加
		top = addMagoDocFromDb(top);
		
		setRoot(top);
		reload(top);
		
	}
	/*
	//0.9.04以前に逆ツリー表示に使用していた
	public DocNode addAllParentDocFromDb(DocNode node){
		log.trace("start");
		if(node == null){
			throw new IllegalArgumentException("node is null.");
		}
		
		DocDao docDao = (DocDao) daoCont_.getComponent(DocDao.class);
		DocStrDao docStrDao = (DocStrDao) daoCont_.getComponent(DocStrDao.class);

		Doc doc = node.getDoc();
		List<DocStr> parents = docStrDao.findOyaDocStr(
					new DocStrSearchCondition(doc.getDocId(),
							(doc.getSortType()==null)? null:doc.getSortType().getOrderSent())
				);

//		if(parents.isEmpty()){
//			return node;
//		}
//		long[] ids = new long[parents.size()];
//		for(int i=0;i < parents.size() ; i++){
//			ids[i] = parents.get(i).getOyaDocId();
//		}
//		List<Doc> oyas = docDao.findByDocIds(ids);
//		for(int i=0;i < oyas.size() ; i++){
//			DocNode newDocNode = new DocNode(oyas.get(i));
//			newDocNode.resetParentCount(docStrDao_);
//			
//			addAllParentDocFromDb(newDocNode);
//			
//			node.add(newDocNode);
//		}
		
		for(int i=0;i < parents.size() ; i++){
			DocStr docStr = parents.get(i);
			
			//注：要件構造が取得する子要件・親要件は、SotrTypeが読み込まれていない！
			Doc oyaDoc = docDao.findByDocId(docStr.getOyaDocId());
			
			DocNode newDocNode = new DocNode(oyaDoc);
			newDocNode.resetParentCount(docStrDao_);
			
			addAllParentDocFromDb(newDocNode);
			
			node.add(newDocNode);
		}
		log.trace("end");
		return node;
	}
*/
	
	//逆ツリー用
	public DocNode addMagoDocFromDb(DocNode node){
		log.trace("start");
		Enumeration parents =node.children();
		while(parents.hasMoreElements()){
			DocNode parent = (DocNode)parents.nextElement();
			parent = addParentDocFromDb(parent);
		}
		log.trace("end");
		return node;
	}
	//逆ツリー用（１世代）
	public DocNode addParentDocFromDb(DocNode koNode){
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
		}
		log.trace("end");
		return koNode;
	}

}
