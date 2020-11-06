 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;


@S2Dao(bean = CheckState.class)
public interface CheckStateDao {

    @Query( "order by checkStateId")
    public List<CheckState> findAll();

    @Arguments({"checkStateId"})
    public CheckState findById(int id);
}
