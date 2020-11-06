 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = DocStr.class)
public interface DocStrDao {

    @Arguments( { "oyaDocId", "koDocId" })
    public DocStr find(long oyaYoukenID, long koYoukenId);

    @Arguments( { "oyaDocId" })
    public List<DocStr> findByOyaDocId(long oyaYoukenID);

//    @Query("oyaDocId=/*$dt.oyaDocId*/ /*IF $dt.orderByString!=null*/order by /*$dt.orderByString*/要件構造.SEQ/*END*/")
//    public List<YoukenKouzou> find要件構造(YoukenKouzouSearchCondition dt);

    @Sql("select docStr.*,doc.docTitle,doc.userName,doc.newDate from docStr join doc on doc.docId=docStr.koDocId where oyaDocId=/*$dt.docId*/ /*IF $dt.orderByString!=null*/order by /*$dt.orderByString*/docStr.SEQ/*END*/")
    public List<DocStr> findDocStr(DocStrSearchCondition dt);

    @Sql("select docStr.*,doc.docTitle,doc.userName,doc.newDate from docStr join doc on doc.docId=docStr.oyaDocId where koDocId=/*$dt.docId*/ /*IF $dt.orderByString!=null*/order by /*$dt.orderByString*/docStr.SEQ/*END*/")
    public List<DocStr> findOyaDocStr(DocStrSearchCondition dt);

    @Arguments( { "koDocId"})
    public List<DocStr> findByKoDocId(long koYoukenID);

    public void insert(DocStr youkenKouzou);

    @Sql("delete from docStr")
    public void deleteAll();

    @Arguments( { "oyaDocId", "koDocId" })
	public void delete(DocStr youkenKouzou);
    
    @Sql("SELECT count(*) FROM docStr where koDocId=?")
    public int getParentCount(long id);
    
    @Sql("SELECT max(SEQ) FROM docStr where oyaDocId=?")
    public int getMaxSeq(long id);

    public int update(DocStr youkenKouzou);

}
