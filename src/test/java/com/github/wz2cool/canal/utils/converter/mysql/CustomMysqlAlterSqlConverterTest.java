package com.github.wz2cool.canal.utils.converter.mysql;

import com.github.wz2cool.canal.utils.converter.mysql.decorator.AlterSqlConverterDecorator;
import com.github.wz2cool.canal.utils.converter.mysql.decorator.DefaultValueDecorator;
import com.github.wz2cool.canal.utils.converter.mysql.decorator.ReplaceNameDecorator;
import com.github.wz2cool.canal.utils.generator.AbstractSqlTemplateGenerator;
import com.github.wz2cool.canal.utils.generator.MysqlSqlTemplateGenerator;
import com.github.wz2cool.canal.utils.model.CanalRowChange;
import com.github.wz2cool.canal.utils.model.SqlTemplate;
import com.google.common.collect.Lists;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.Before;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author penghai
 */
public class CustomMysqlAlterSqlConverterTest {
    private AbstractSqlTemplateGenerator sqlTemplateGenerator;
    private CanalRowChange canalRowChange;

    @Before
    public void init() {
        List<AlterSqlConverterDecorator> decorators = Lists.newArrayList();
        decorators.add(new DefaultValueDecorator());
        decorators.add(new ReplaceNameDecorator("qa_penghai", "t_com_info"));
        MysqlAlterSqlConverter mysqlAlterSqlConverter = new MysqlAlterSqlConverter(decorators);
        sqlTemplateGenerator = new MysqlSqlTemplateGenerator(mysqlAlterSqlConverter);
        canalRowChange = new CanalRowChange();
        canalRowChange.setId(1L);
        canalRowChange.setDatabase("dmdc");
        canalRowChange.setTable("t_com_info");
        canalRowChange.setType("ALTER");
        canalRowChange.setRowDataList(Lists.newArrayList());
        canalRowChange.setEs(System.currentTimeMillis());
        canalRowChange.setDdl(true);
    }

    @org.junit.Test
    public void testRenameColumn() throws JSQLParserException {
        canalRowChange.setSql("-- 对卖数据的表操作已二次确认\n" +
                "ALTER TABLE `dmdc`.`t_com_info`\n" +
                "ADD COLUMN `paid_in_capital` decimal(20, 4) NULL COMMENT '实缴资本(万元)' AFTER `all_dm_udic_status`,\n" +
                "ADD COLUMN `registration_organization` varchar(600) NULL COMMENT '注册机构' AFTER `paid_in_capital`,\n" +
                "ADD COLUMN `approval_date` date NULL COMMENT '核准日期' AFTER `registration_organization`,\n" +
                "ADD COLUMN `main_business` varchar(4000) NULL COMMENT '主营业务' AFTER `approval_date`,\n" +
                "MODIFY COLUMN `com_eng_short_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '公司英文简称' AFTER `com_eng_name`");
        List<SqlTemplate> sqlTemplates = sqlTemplateGenerator.listDDLSqlTemplates(canalRowChange);
        assertEquals(5, sqlTemplates.size());
    }

}
