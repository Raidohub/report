package com.robotto.report.infrastructure.execute.importer;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.read.listener.ReadListener;
import com.robotto.base.infrastructure.general.exception.ValidationException;
import com.robotto.base.infrastructure.general.utils.ValidatorUtil;
import com.robotto.report.infrastructure.execute.bean.BaseImport;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/11/14 13:38
 **/
public class ReadListenerExt<T extends BaseImport> implements ReadListener<T> {

    private final Logger logger = LoggerFactory.getLogger(ReadListenerExt.class);

    private final String EXCEPTION_MSG_TEMPLATE = "第%d行，第%s列解析异常，数据为:%s";
    private final String BIZ_ERROR_MSG_TEMPLATE = "第%d ~ %d行导入失败，%s";
    private final String ERROR_MSG_TEMPLATE = "第%d行导入失败，%s";

    private final BlockingQueue<List<T>> queue = new LinkedBlockingDeque<>();
    private final ExecutorService singleExecutor;
    private final Meta<T> meta;

    public ReadListenerExt(Meta<T> meta, ExecutorService singleExecutor) {
        this.meta = meta;
        this.singleExecutor = singleExecutor;
    }

    @Override
    public void onException(Exception e, AnalysisContext context) {
        if (e instanceof ExcelDataConvertException) {
            ExcelDataConvertException ex = (ExcelDataConvertException) e;
            String errorMsg = String.format(EXCEPTION_MSG_TEMPLATE, ex.getRowIndex(), ex.getColumnIndex(), ex.getCellData());
            this.meta.addResolveError(errorMsg);
        } else if (e instanceof ValidationException) {
            ValidationException ex = (ValidationException) e;
            this.meta.addBizError(ex.getMessage());
        }
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
//        this.validateForm(data, context.readRowHolder().getRowIndex());

        if (this.meta.dataReady(data)) {
            this.insert(context);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!this.meta.getDataList().isEmpty()) {
            logger.info("batch insert rest of data");
            this.meta.setFuture(this.insert(context));
        }
    }

    private void validateForm(T data, int rowIndex) {
        String errorMsg = ValidatorUtil.validate(data).stream().map(String::toString).collect(Collectors.joining());
        errorMsg = StringUtils.isNotBlank(errorMsg) ? errorMsg : this.meta.getValidateForm().apply(data);
        if (StringUtils.isNotBlank(errorMsg)) {
            throw new ValidationException(String.format(ERROR_MSG_TEMPLATE, rowIndex, errorMsg));
        }
    }

    private CompletableFuture<List<String>> insert(AnalysisContext context) {
        int endIndex = context.readRowHolder().getRowIndex();
        int startIndex = endIndex - this.meta.getDataList().size() + 1;

        queue.offer(this.meta.getDataList());
        this.meta.setDataList(new LinkedList<>());

        return CompletableFuture.supplyAsync(() -> this.meta.getBatchInsert().apply(queue.poll(), this.meta.getImportLogPo()), this.singleExecutor)
                .exceptionally(ex -> {
                    this.meta.addBizError(String.format(BIZ_ERROR_MSG_TEMPLATE, startIndex, endIndex, ex.getCause().getMessage()));
                    return null;
                });
    }
}
