/******************************************************************************
 * Copyright (C) 2017 ShenZhen ComTop Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳康拓普开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 ******************************************************************************/
package stoner.tstomp.exception;


import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * @author xuzhifan
 * @version 1.0
 * @since 2019/3/6
 */
public class StringEscapeEditor extends PropertyEditorSupport {

    private boolean escapeSQL;

    public StringEscapeEditor() {
        super();
    }

    public StringEscapeEditor(boolean escapeSQL) {
        super();
        this.escapeSQL = escapeSQL;

    }

    @Override

    public void setAsText(String text) {

        if (text == null) {
            setValue(null);
        } else {
            String value = text;
            if (escapeSQL) {
                value = StringEscapeUtils.escapeSql(value);
            }
            setValue(value);
        }
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        return value != null ? value.toString() : StringUtils.EMPTY;
    }

}

