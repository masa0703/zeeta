 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = WorkType.class)
public interface WorkTypeDao {

    @Query( "order by SEQ")
    public List findAll();

    public void insert(WorkType worType);
    public void update(WorkType worType);

    @Sql("delete from workType")
    public void deleteAll();

    @Arguments ({"workTypeId"})
	public WorkType findByWorkTypeId(long id);

    @Sql("delete from workType where workTypeId=?")
	public void deleteByWorkTypeId(long id);
}
