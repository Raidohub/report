package com.robotto.report.infrastructure.execute.converter;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.robotto.base.constant.BaseEnum;
import java.lang.reflect.Field;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/11/10 10:36
 **/
public class AbstractEnum2StringConverter<T extends BaseEnum> implements Converter<T> {

    private final Class<T> enumClazz;

    public AbstractEnum2StringConverter() {
        this.enumClazz = (Class<T>) TypeUtil.getTypeArgument(getClass(), 0);
    }

    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * 这里读的时候会调用
     *
     * @param context
     * @return
     */
    @Override
    public T convertToJavaData(ReadConverterContext<?> context) {
        String fieldValue = context.getReadCellData().getStringValue();
        for (Field field : enumClazz.getFields()) {
            T enumFiled = (T) ReflectUtil.getFieldValue(enumClazz, field);
            if (enumFiled.getName().equals(fieldValue)) {
                return enumFiled;
            }
        }
        return null;
    }

    /**
     * 这里是写的时候会调用 不用管
     *
     * @return
     */
    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<T> context) {
        return new WriteCellData<>(context.getValue().getName());
    }
}
