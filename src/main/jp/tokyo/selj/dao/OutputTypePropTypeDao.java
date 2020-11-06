 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = OutputTypePropType.class)
public interface OutputTypePropTypeDao {

//    @Arguments({"outputTypeId"})
    @Sql("select otpt.outputPropTypeId, otpt.outputTypeId, "
    		+"   opt.outputPropTypeId AS outputPropTypeId_0, opt.outputPropTypeName AS outputPropTypeName_0, "
    		+"   opt.seq AS seq_0, opt.unitName AS unitName_0, opt.descr AS descr_0 "
    		+" FROM outputTypePropType otpt"
        	+" LEFT OUTER JOIN outputPropType opt ON otpt.outputPropTypeId = opt.outputPropTypeId" 
    		+" where otpt.outputTypeId=?")
    public List<OutputTypePropType> findByOutputTypeId(long id);

    @Sql("insert into outputTypePropType (outputTypeId, outputPropTypeId)values(/*wtp.outputTypeId*/, /*wtp.outputPropTypeId*/)")
    public void insert(OutputTypePropType wpt);
    @Sql("update outputTypePropType outputTypeId=/*wtp.outputTypeId*/, outputPropTypeId=/*wtp.outputPropTypeId*/)")
    public void update(OutputTypePropType wpt);
    @Sql("delete from outputTypePropType where outputTypeId=/*wtp.outputTypeId*/")
    public void delete(OutputTypePropType wpt);

    @Sql("delete from outputTypePropType")
    public void deleteAll();
    @Sql("delete from outputTypePropType where outputTypeId=?")
    public void deleteByOutputTyeId(long id);

}
