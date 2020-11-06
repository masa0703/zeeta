package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.DaoMetaDataFactory;
import org.seasar.dao.impl.AbstractDao;

public class Doc2DaoImpl extends AbstractDao implements Doc2Dao {
    public Doc2DaoImpl(DaoMetaDataFactory daoMetaDataFactory) {
        super(daoMetaDataFactory);
    }

	public List<Doc> findByText(String[] keywords) {
        final String SQL="LOWER(docCont) like LOWER(?)";
        String sql = "";
        String sep = "";
        for(String keyword:keywords){
        	sql = sql + sep + SQL;
        	sep = " and ";
        }
        sql = sql + " order by docid";
		
		return getEntityManager().find(sql, keywords);
	}
// 使わないのでコメントアウト
//	public List<Doc> findByTitleOrText(String[] keywords) {
//        final String SQL_TITLE="LOWER(docTitle) like LOWER(?)";
//        final String SQL_TEXT ="LOWER(docCont) like LOWER(?)";
//        String sql = "(";
//        String sep = "";
//        String[] newKeywords = new String[keywords.length * 2];
//        int i=0;
//        for(String keyword:keywords){
//        	newKeywords[i++] = keyword;
//        	sql = sql + sep + SQL_TITLE;
//        	sep = " and ";
//        }
//        sql = sql + ") or (";
//        i = 0;
//        sep = "";
//        for(String keyword:keywords){
//        	newKeywords[keywords.length + i++] = keyword;
//        	sql = sql + sep + SQL_TEXT;
//        	sep = " and ";
//        }
//        sql = sql + ") order by docid";
//		
//		return getEntityManager().find(sql, newKeywords);
//	}

}
