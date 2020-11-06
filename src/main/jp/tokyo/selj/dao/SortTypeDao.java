 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = SortType.class)
public interface SortTypeDao {

    public List findAll();

    @Arguments( { "sortTypeId" })
    public SortType findById(int id);

    public void insert(SortType type);

    @Sql("delete from sortType")
    public void deleteAll();
}
