package com.github.wz2cool.canal.utils.converter.mysql;

import com.github.wz2cool.canal.utils.converter.BaseAlterSqlConverter;
import com.github.wz2cool.canal.utils.converter.IColDataTypeConverter;
import com.github.wz2cool.canal.utils.model.AlterColumnExpression;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * mysql alter sql converter without DEFAULT functionality.
 * @author penghai
 */
public class MysqlAlterSqlConverter extends BaseAlterSqlConverter {
    private final MysqlColDataTypeConverter mysqlColDataTypeConverter = new MysqlColDataTypeConverter();
    private static final String UNSIGNED = " UNSIGNED";
    private static final String COMMENT = "COMMENT ";

    @Override
    protected IColDataTypeConverter getColDataTypeConverter() {
        return this.mysqlColDataTypeConverter;
    }

    @Override
    protected Optional<String> convertToAddColumnSql(AlterColumnExpression alterColumnExpression) {
        return Optional.of(generateColumnSql(
                "ADD",
                "",
                alterColumnExpression.getTableName(),
                "",
                alterColumnExpression.getColumnName(),
                getFormattedDataType(alterColumnExpression),
                alterColumnExpression.getNullAble(),
                "",
                alterColumnExpression.getCommentText()
        ));
    }

    @Override
    protected Optional<String> convertToChangeColumnTypeSql(AlterColumnExpression alterColumnExpression) {
        return Optional.of(generateColumnSql(
                "CHANGE",
                "",
                alterColumnExpression.getTableName(),
                alterColumnExpression.getColumnName(),
                alterColumnExpression.getColumnName(),
                getFormattedDataType(alterColumnExpression),
                alterColumnExpression.getNullAble(),
                "",
                alterColumnExpression.getCommentText())
        );
    }

    @Override
    protected Optional<String> convertToRenameColumnSql(AlterColumnExpression alterColumnExpression) {
        return Optional.of(generateColumnSql(
                "CHANGE",
                "",
                alterColumnExpression.getTableName(),
                alterColumnExpression.getColOldName(),
                alterColumnExpression.getColumnName(),
                getFormattedDataType(alterColumnExpression),
                alterColumnExpression.getNullAble(),
                "",
                alterColumnExpression.getCommentText()
        ));
    }

    @Override
    protected Optional<String> convertToDropColumnSql(AlterColumnExpression alterColumnExpression) {
        String qualifiedTableName = getQualifiedTableName("", alterColumnExpression.getTableName());
        String formattedColumnName = getFormattedColumnNameOrEmpty(alterColumnExpression.getColumnName());
        String sql = String.format("ALTER TABLE %s DROP COLUMN %s;", qualifiedTableName, formattedColumnName).trim();
        return Optional.of(sql);
    }

    @Override
    protected List<String> convertToOtherColumnActionSqlList(List<AlterColumnExpression> alterColumnExpressions) {
        return new ArrayList<>();
    }

    protected String generateColumnSql(String action, String schemaName, String tableName, String oldColumnName, String newColumnName,
                                       String dataTypeString,
                                       String nullAble, String defaultValue, String commentText) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        String formattedOldColumnName = getFormattedColumnNameOrEmpty(oldColumnName);
        String formattedNewColumnName = getFormattedColumnNameOrEmpty(newColumnName);
        String formattedComment = getFormattedComment(commentText);
        return String.format("ALTER TABLE %s %s COLUMN %s %s %s %s %s %s;",
                qualifiedTableName,
                action,
                formattedOldColumnName,
                formattedNewColumnName,
                dataTypeString,
                nullAble,
                defaultValue,
                formattedComment);
    }

    protected String getFormattedDataType(AlterColumnExpression alterColumnExpression) {
        ColDataType colDataType = alterColumnExpression.getColDataType();
        return (alterColumnExpression.isUnsignedFlag())
                ? (getDataTypeString(colDataType) + UNSIGNED)
                : getDataTypeString(colDataType);
    }

    protected String getFormattedComment(String commentText) {
        return StringUtils.isBlank(commentText)
                ? ""
                : (COMMENT + commentText);
    }

    /**
     * 生成包含库名的表名字符串（如果库名不为空）
     *
     * @param schemaName 库名
     * @param tableName  表名
     * @return 完整的表名字符串，如 "`schemaName`.`tableName`"，如果库名为空则返回 "`tableName`"
     */
    protected String getQualifiedTableName(String schemaName, String tableName) {
        return (schemaName != null && !schemaName.isEmpty())
                ? (schemaName + ".`" + tableName + "`")
                : ("`" + tableName + "`");
    }

    protected String getFormattedColumnNameOrEmpty(String columnName) {
        return (StringUtils.isNotEmpty(columnName)) ? ("`" + columnName + "`") : "";
    }


}
