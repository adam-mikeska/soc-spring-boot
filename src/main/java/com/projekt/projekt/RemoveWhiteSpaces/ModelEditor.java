package com.projekt.projekt.RemoveWhiteSpaces;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

public class ModelEditor extends PropertyEditorSupport {
    @Nullable
    private final String charsToDelete;
    private final boolean emptyAsNull;

    public ModelEditor(boolean emptyAsNull) {
        this.charsToDelete = null;
        this.emptyAsNull = emptyAsNull;
    }

    public ModelEditor(String charsToDelete, boolean emptyAsNull) {
        this.charsToDelete = charsToDelete;
        this.emptyAsNull = emptyAsNull;
    }

    public void setAsText(@Nullable String text) {
        if (text == null) {
            this.setValue((Object) null);
        } else {
            String value = text.trim().replaceAll("\\s+"," ");
            if (this.charsToDelete != null) {
                value = StringUtils.deleteAny(value, this.charsToDelete);
            }

            if (this.emptyAsNull && value.isEmpty()) {
                this.setValue((Object) null);
            } else {
                this.setValue(value);
            }
        }

    }

    public String getAsText() {
        Object value = this.getValue();
        return value != null ? value.toString() : "";
    }
}
