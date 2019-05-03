package com.github.wz2cool.canal.utils.converter;

import com.github.wz2cool.canal.utils.model.AlterColumnExpression;
import com.github.wz2cool.canal.utils.model.EnhancedAlterOperation;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.create.table.ColDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AlterColumnSqlConverterBase {
    public abstract List<String> convert(Alter mysqlAlter);

    /**
     * Get list of AlterColumnExpression for mysql.
     *
     * @param mysqlAlter
     * @return
     */
    protected List<AlterColumnExpression> getMysqlAlterColumnExpressions(Alter mysqlAlter) {
        List<AlterColumnExpression> result = new ArrayList<>();
        if (mysqlAlter == null) {
            return result;
        }

        List<AlterExpression> alterExpressions = mysqlAlter.getAlterExpressions();
        List<AlterExpression> alterExpressionsForColumn = new ArrayList<>();
        String tableName = cleanText(mysqlAlter.getTable().getName());
        for (AlterExpression alterExpression : alterExpressions) {
            String optionalSpecifier = alterExpression.getOptionalSpecifier();
            if ("COLUMN".equalsIgnoreCase(optionalSpecifier)
                    || (alterExpression.getColDataTypeList() != null && !alterExpression.getColDataTypeList().isEmpty())
                    || (alterExpression.getOperation() == AlterOperation.DROP && alterExpression.getColumnName() != null)) {
                alterExpressionsForColumn.add(alterExpression);
            }
        }

        if (alterExpressionsForColumn.isEmpty()) {
            return result;
        }

        for (AlterExpression alterExpression : alterExpressionsForColumn) {
            List<AlterColumnExpression> alterColumnExpressions =
                    getMysqlAlterColumnExpressions(tableName, alterExpression);
            result.addAll(alterColumnExpressions);
        }

        return result;
    }

    private List<AlterColumnExpression> getMysqlAlterColumnExpressions(
            final String tableName, final AlterExpression alterExpression) {
        List<AlterColumnExpression> result = new ArrayList<>();
        if (alterExpression == null) {
            return result;
        }

        AlterOperation operation = alterExpression.getOperation();
        String columnName = alterExpression.getColumnName();
        String colOldName = alterExpression.getColOldName();
        List<AlterExpression.ColumnDataType> colDataTypeList = alterExpression.getColDataTypeList();

        Optional<AlterColumnExpression> dropColumnExpression =
                getDropColumnExpression(operation, tableName, columnName);
        dropColumnExpression.ifPresent(result::add);

        List<AlterColumnExpression> renameColumnExpressions =
                getRenameColumnExpressions(operation, tableName, colOldName, colDataTypeList);
        result.addAll(renameColumnExpressions);

        List<AlterColumnExpression> addColumnExpressions =
                getAddColumnExpressions(operation, tableName, colDataTypeList);
        result.addAll(addColumnExpressions);

        List<AlterColumnExpression> changeColumnTypeExpressions =
                getChangeColumnTypeExpressions(operation, tableName, colOldName, colDataTypeList);
        result.addAll(changeColumnTypeExpressions);
        return result;
    }

    private List<AlterColumnExpression> getAddColumnExpressions(
            final AlterOperation alterOperation,
            final String tableName,
            final List<AlterExpression.ColumnDataType> columnDataTypes) {
        List<AlterColumnExpression> result = new ArrayList<>();
        if (alterOperation != AlterOperation.ADD || columnDataTypes == null || columnDataTypes.isEmpty()) {
            return result;
        }

        for (AlterExpression.ColumnDataType columnDataType : columnDataTypes) {
            AlterColumnExpression addColumnExpression = new AlterColumnExpression();
            String columnName = columnDataType.getColumnName();
            ColDataType colDataType = columnDataType.getColDataType();
            addColumnExpression.setTableName(cleanText(tableName));
            addColumnExpression.setColumnName(cleanText(columnName));
            addColumnExpression.setOperation(EnhancedAlterOperation.ADD_COLUMN);
            addColumnExpression.setColDataType(colDataType);
            result.add(addColumnExpression);
        }
        return result;
    }

    private List<AlterColumnExpression> getRenameColumnExpressions(
            final AlterOperation alterOperation,
            final String tableName,
            final String colOldName,
            final List<AlterExpression.ColumnDataType> columnDataTypes
    ) {
        List<AlterColumnExpression> result = new ArrayList<>();
        if (alterOperation != AlterOperation.CHANGE
                || colOldName == null
                || columnDataTypes == null
                || columnDataTypes.isEmpty()) {
            return result;
        }

        for (AlterExpression.ColumnDataType columnDataType : columnDataTypes) {
            String columnName = columnDataType.getColumnName();
            if (columnName.equals(colOldName)) {
                // ignore if column name not change.
                continue;
            }

            AlterColumnExpression renameColumnExpression = new AlterColumnExpression();
            renameColumnExpression.setTableName(cleanText(tableName));
            renameColumnExpression.setColumnName(cleanText(columnName));
            renameColumnExpression.setColOldName(cleanText(colOldName));
            renameColumnExpression.setOperation(EnhancedAlterOperation.RENAME_COLUMN);
            result.add(renameColumnExpression);
        }
        return result;
    }

    private List<AlterColumnExpression> getChangeColumnTypeExpressions(
            final AlterOperation alterOperation,
            final String tableName,
            final String colOldName,
            final List<AlterExpression.ColumnDataType> columnDataTypes) {
        List<AlterColumnExpression> result = new ArrayList<>();
        if (alterOperation != AlterOperation.CHANGE || columnDataTypes == null || columnDataTypes.isEmpty()) {
            return result;
        }

        for (AlterExpression.ColumnDataType columnDataType : columnDataTypes) {
            AlterColumnExpression changeTypeColumnExpression = new AlterColumnExpression();
            String columnName = columnDataType.getColumnName();
            if (!columnName.equals(colOldName)) {
                // need check rename
                continue;
            }

            ColDataType colDataType = columnDataType.getColDataType();
            changeTypeColumnExpression.setTableName(cleanText(tableName));
            changeTypeColumnExpression.setColumnName(cleanText(columnName));
            changeTypeColumnExpression.setOperation(EnhancedAlterOperation.CHANGE_COLUMN_TYPE);
            changeTypeColumnExpression.setColDataType(colDataType);
            result.add(changeTypeColumnExpression);
        }
        return result;
    }

    private Optional<AlterColumnExpression> getDropColumnExpression(
            final AlterOperation alterOperation,
            final String tableName,
            final String columnName
    ) {
        if (alterOperation != AlterOperation.DROP || columnName == null) {
            return Optional.empty();
        }

        AlterColumnExpression dropColumnExpression = new AlterColumnExpression();
        dropColumnExpression.setTableName(cleanText(tableName));
        dropColumnExpression.setColumnName(cleanText(columnName));
        dropColumnExpression.setOperation(EnhancedAlterOperation.DROP_COLUMN);
        return Optional.of(dropColumnExpression);
    }

    private String cleanText(String element) {
        return element.replace("`", "");
    }
}
