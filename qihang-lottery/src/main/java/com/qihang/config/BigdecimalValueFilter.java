package com.qihang.config;

import com.alibaba.fastjson.serializer.ValueFilter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigdecimalValueFilter implements ValueFilter {
    @Override
    public Object process(Object o, String s, Object o1) {
        if (o1 != null && o1 instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) o1;
            bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_DOWN);
            return bigDecimal;
        }
        return o1;
    }
}
