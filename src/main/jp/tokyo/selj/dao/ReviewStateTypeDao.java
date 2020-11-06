 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = ReviewStateType.class)
public interface ReviewStateTypeDao {

    @Query ("order by reviewStateTypeId")
    public List findAll();
    
    @Arguments({"reviewStateTypeId"})
    public ReviewStateType findById(int id);

    public void insert(ReviewStateType reviewStateType);
    public void update(ReviewStateType reviewStateType);

    @Sql("delete from reviewStateType")
    public void deleteAll();

    @Sql("delete from reviewStateType where reviewStateTypeId=?")
    public void deleteById(int id);
}
