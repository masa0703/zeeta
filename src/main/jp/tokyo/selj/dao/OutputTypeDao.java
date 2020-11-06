 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = OutputType.class)
public interface OutputTypeDao {

    @Query( "order by SEQ")
    public List findAll();

    public void insert(OutputType outputType);
    public void update(OutputType outputType);

    @Sql("delete from outputType")
    public void deleteAll();
    
    @Query( "workType.workTypeId = ? order by SEQ")
    public List<OutputType> findByWorkTypeId(long docId);

    @Arguments ({"outputTypeId"})
	public OutputType findByOutputTypeId(long id);

    @Sql("delete from outputType where workTypeId=?")
	public void deleteByWorkTypeId(long id);

    @Sql("delete from outputType where outputTypeId=?")
	public void deleteByOutputTypeId(long id);
}
