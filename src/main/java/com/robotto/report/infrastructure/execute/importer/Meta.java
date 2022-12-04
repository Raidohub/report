package com.robotto.report.infrastructure.execute.importer;

import com.robotto.report.infrastructure.constant.ExecuteStatus;
import com.robotto.report.infrastructure.persistence.po.ImportLogPo;
import lombok.Getter;
import lombok.Setter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/11/15 10:21
 **/
@Setter
@Getter
public class Meta<T> {


    private ImportLogPo importLogPo;
    private List<List<String>> bizErrorList;
    private List<List<String>> resolveErrorList;
    private List<T> dataList;
    private BiFunction<List<T>, ImportLogPo, List<String>> batchInsert;
    private Function<T, String> validateForm;
    private CompletableFuture<List<String>> future;

    public Meta(Function<T, String> validateForm, BiFunction<List<T>, ImportLogPo, List<String>> batchInsert,
                ImportLogPo importLogPo) {
        this.validateForm = validateForm;
        this.batchInsert = batchInsert;
        this.importLogPo = importLogPo;

        this.resolveErrorList = new LinkedList<>();
        this.bizErrorList = new LinkedList<>();
        this.dataList = new LinkedList<>();
    }

    public void addResolveError(String errorMsg) {
        this.resolveErrorList.add(Collections.singletonList(errorMsg));
    }

    public void addBizError(String errorMsg) {
        this.bizErrorList.add(Collections.singletonList(errorMsg));
        this.importLogPo.setStatus(ExecuteStatus.FAILED);
    }

    public List<List<String>> getErrorList() {
        this.resolveErrorList.addAll(this.bizErrorList);
        return this.resolveErrorList;
    }

    public boolean dataReady(T data) {
        this.dataList.add(data);
        return dataList.size() > 2;
    }
}
