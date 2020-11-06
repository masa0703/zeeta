 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = WorkProp.class)
public interface WorkPropDao {

    @Arguments({"docId","workTypeId"})
    public List<WorkProp> findProps(
    		long docId, int workTypeId);
    
    @Query( "workProp.docId = /*wp.docId*/ and " +
    	    "workProp.workTypeId = /*wp.workTypeId*/ and " +
    	    "workProp.outputPropTypeId = /*wp.outputPropTypeId*/ and " +
    	    "workProp.jissekiFlg = /*wp.jissekiFlg*/" +
    	    " order by docId")
    public WorkProp findWithoutVersion(WorkProp wp);

    public void insert(WorkProp wp);
    public void update(WorkProp wp);
    public void delete(WorkProp wp);

    @Sql("delete from WorkProp")
    public void deleteAll();

}
