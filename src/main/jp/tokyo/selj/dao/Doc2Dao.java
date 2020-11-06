 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.S2Dao;

@S2Dao(bean = Doc.class)
public interface Doc2Dao {
	//アノテーションが効かないらしい。ダッセー！
	public Class BEAN = Doc.class;

    public List<Doc> findByText(String[] keywords);
//    public List<Doc> findByTitleOrText(String[] keywords);

}
