 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = OutputPropType.class)
public interface OutputPropTypeDao {

    @Query ("order by seq")
    public List<OutputPropType> findAll();
    
    @Arguments({"outputPropTypeId"})
    public OutputPropType findById(long id);

    @Sql("select distinct opt.* "
		+"from outputproptype opt "
		+"left join outputtypeProptype otpt on "
		+"	otpt.outputproptypeid=opt.outputproptypeid "
		+"left join outputtype ot on "
		+"	otpt.outputtypeid=ot.outputtypeid "
		+"where ot.worktypeid IN /*worktypeids*/(1) "
		+"order by opt.seq "
		)
    public List<OutputPropType> findByWorkTypeId(long[] ids);
    
    public void insert(OutputPropType wpt);
    public void update(OutputPropType wpt);
    public void delete(OutputPropType wpt);

    @Sql("delete from outputPropType where outputPropTypeId=?")
    public void deleteById(long id);

    @Sql("delete from outputPropType")
    public void deleteAll();

}
