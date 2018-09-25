package com.dadaabc.sync.elasticsearch.listener;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.dadaabc.sync.elasticsearch.event.DadaDeleteCanalEvent;
import com.dadaabc.sync.elasticsearch.model.DadaIndexTypeModel;
import com.dadaabc.sync.elasticsearch.model.DataDatabaseTableModel;
import com.dadaabc.sync.elasticsearch.service.DadaElasticsearchService;
import com.dadaabc.sync.elasticsearch.service.DadaMappingService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Veelur
 * @version 1.0
 */
@Component
public class DadaDeleteCanalListener extends DadaAbstractCanalListener<DadaDeleteCanalEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DadaDeleteCanalListener.class);

    @Autowired
    private DadaElasticsearchService elasticsearchService;

    @Override
    protected void doSync(DataDatabaseTableModel dbModel, DadaIndexTypeModel esModel, RowData rowData) {
        List<Column> columns = rowData.getBeforeColumnsList();
        String primaryKey = Optional.ofNullable(dbModel.getPkStr()).orElse("id");
        Column idColumn = columns.stream().filter(column ->
                column.getIsKey() && primaryKey.equals(column.getName())).findFirst().orElse(null);
        if (idColumn == null || StringUtils.isBlank(idColumn.getValue())) {
            logger.error("insert_column_find_null_warn insert从column中找不到主键" +
                    ",database=" + dbModel.getDatabase() + ",table=" + dbModel.getTable() +
                    ",pkStr=" + dbModel.getPkStr());
            return;
        }
        logger.info("insert_column_id_info insert主键id,database=" + dbModel.getDatabase() +
                ",table=" + dbModel.getTable() + ",id=" + idColumn.getValue());
        Boolean main = dbModel.getMain();
        if (null != main && main) {
            elasticsearchService.deleteById(esModel.getIndex(), esModel.getType(), idColumn.getValue());
        } else {
            //删除es中的部分字段信息,置为null
            Map<String, Object> dataMap = parseColumnsToNullMap(dbModel, columns);
            elasticsearchService.updateById(esModel.getIndex(), esModel.getType(), idColumn.getValue(), dataMap);
        }
        logger.info("insert_es_info 同步es插入操作成功！database=" + dbModel.getDatabase()
                + ",table=" + dbModel.getTable() + ",id=" + idColumn.getValue());
    }
}