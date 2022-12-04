package com.robotto.report.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/5/20 10:07
 **/
@Setter
@Getter
@ConfigurationProperties(prefix = "config.report")
public class ReportProp {

    private String fileNameTemplateCsv = "csv%d.csv";
    private String fileNameTemplate = "excel%d.xlsx";
    private String fileSheetTemplate = "sheet%d";
    private String fileErrorTemplate;
    private int importAsyncLimit = 100_000;
    private int importBatchLimit = 10_000;
    private long exportFileSize = 5_000;
    private long exportSheetSize = 2_500;
    private int exportAsyncThreshold = 1_200_000;
    private int exportFileThreshold = 10_000;

    public String sheetName(int sheetNo) {
        return String.format(fileSheetTemplate, sheetNo);
    }

    public String fileName(int sheetNo) {
        return String.format(fileNameTemplate, sheetNo);
    }

    public int calFileNum(long searchCount) {
        return this.exportFileThreshold < searchCount ? (int) Math.ceil(searchCount / (double) this.exportFileSize) : 1;
    }

    public int calSheetNum(long searchCount) {
        return this.calFileNum(searchCount) == 1 ?  (int) Math.ceil(searchCount / (double) this.exportSheetSize)
                :  (int) Math.ceil(this.exportFileSize / (double) this.exportSheetSize);
    }
}
