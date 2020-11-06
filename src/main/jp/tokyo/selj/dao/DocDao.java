 package jp.tokyo.selj.dao;

import java.sql.Timestamp;
import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.NoPersistentProperty;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;

@S2Dao(bean = Doc.class)
public interface DocDao {
	public class DateCondition {
		public Timestamp from;
		public Timestamp getFrom(){return from;}
		public Timestamp to;
		public Timestamp getTo(){return to;}
	}

    @Arguments( { "docTypeId" })
    public List findByDocTypeId(int typeId);

//    @Arguments( { "docId" })
//    public Doc findByDocId(long docId);

    @Sql("select doc.* " +
    	",sortType.sorttypeid AS sorttypeid_0" +
    	",sortType.ordersent AS ordersent_0" +
    	",sortType.sorttypename AS sorttypename_0" +
		",w.workCount " +
    	" from doc ,sortType" +
    	", (select count(*) as workCount from work where work.docid=?) w " +
    	" where doc.docid=?  and doc.sortTypeId = sortType.sortTypeId")
    public Doc findByDocId(long docId);

    //以下のメソッドは、高速化のために作成してみたが、H2では逆にかなり遅くなったうえに
    //postgresではシンタックスエラーになるため止めた。
    //使っていたのは、DocModel#addChildDocFromDb, DocModel#addAllParentDocFromDb
//    @Sql("select doc.* " +
//        	",sortType.sorttypeid AS sorttypeid_0" +
//        	",sortType.ordersent AS ordersent_0" +
//        	",sortType.sorttypename AS sorttypename_0" +
//    		",(select count(*) from work where work.docid=doc.docid ) workCount " +
//        	" from  (select * from doc where docid  in /*docIds*/(1,2)) doc"+
//        	" ,sortType" +
//        	" where doc.sortTypeId = sortType.sortTypeId")
//    public List<Doc> findByDocIds(long[] docIds);
    
    @Sql("select doc.* " +
        	",sortType.sorttypeid AS sorttypeid_0" +
        	",sortType.ordersent AS ordersent_0" +
        	",sortType.sorttypename AS sorttypename_0" +
        	" from doc ,sortType" +
        	", (select docId from work where outputid=?) w " +
        	" where doc.docid=w.docId and doc.sortTypeId = sortType.sortTypeId")
    public List<Doc> findByOutputId(long outputId);

    @NoPersistentProperty("workCount")
    public void insert(Doc docId);
    @NoPersistentProperty("workCount")
	public void update(Doc docId);

    @Sql("delete from doc")
    public void deleteAll();

    @Arguments( { "docId" })
	public void delete(Doc docId);

    @Query("/*BEGIN*/"+
    	    "/*IF dto.docTitle!=null*/ LOWER(docTitle) like LOWER(/*dto.docTitle*/) /*END*/" +
    	    "/*IF dto.docCont!=null and dto.docCont!=\"\"*/ or LOWER(docCont) like LOWER(/*dto.docCont*/) /*END*/ " +
    	    "/*IF dto.docCont==\"\"*/ or docCont is null or docCont=''/*END*/" +
    	    "/*IF dto.userName!=null and dto.userName!=\"\"*/ or LOWER(userName) like LOWER(/*dto.userName*/) /*END*/" +
    	    "/*IF dto.userName==\"\"*/ or userName is null or userName=''/*END*/" +
    	    "/*END*/" +
    	    " order by docId")
    public List<Doc> find(Doc doc);
    @Query("/*BEGIN*/"+
    	    "/*IF dto.docTitle!=null*/ LOWER(docTitle)=LOWER(/*dto.docTitle*/) /*END*/" +
    	    "/*IF dto.docCont!=null and dto.docCont!=\"\"*/ or LOWER(docCont)=LOWER(/*dto.docCont*/) /*END*/ " +
    	    "/*IF dto.docCont==\"\"*/ or docCont is null or docCont=''/*END*/" +
    	    "/*IF dto.userName!=null and dto.userName!=\"\"*/ or LOWER(userName)=LOWER(/*dto.userName*/) /*END*/" +
    	    "/*IF dto.userName==\"\"*/ or userName is null or userName=''/*END*/" +
    	    "/*END*/" +
    	    " order by docId")
    public List<Doc> findWholeWord(Doc doc);
    @Query("/*BEGIN*/"+
    	    "/*IF dto.from!=null*/ newDate>=/*dto.from*/ /*END*/" +
    	    "/*IF dto.to!=null*/ and newDate<=/*dto.to*/ /*END*/ " +
    	    "/*END*/" +
    	    " order by docId")
    public List<Doc> findByDateEQ(DateCondition dc);
    
    @Query("/*BEGIN*/"+
    	    "/*IF dto.from!=null*/ newDate>/*dto.from*/ /*END*/" +
    	    "/*IF dto.to!=null*/ and newDate</*dto.to*/ /*END*/ " +
    	    "/*END*/" +
    	    " order by docId")
    public List<Doc> findByDate(DateCondition dc);
    
    @Query(
    		"doc.docId = /*dto.docId*/ " +
    		"and doc.docTitle = /*dto.docTitle*/ " +
    		"and doc.docTypeId = /*dto.docTypeId*/ " +
    		"and doc.newDate = /*dto.newDate*/ " +
    		"/*IF dto.userName!=null*/ and doc.userName = /*dto.userName*/  " +
    		"--ELSE and doc.userName is null/*END*/ " +
    		"and doc.sortTypeId = /*dto.sortTypeId*/ " 
    	    )
    public List<Doc> findSameDoc(Doc doc);

}
