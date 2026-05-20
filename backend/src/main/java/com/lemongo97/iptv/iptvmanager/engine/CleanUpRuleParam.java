package com.lemongo97.iptv.iptvmanager.engine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class CleanUpRuleParam {

    protected String field;
    protected String label;
    protected Type type;
    protected String placeholder;
    protected boolean required;


    public CleanUpRuleParam(String field, String label, Type type) {
        this.field = field;
        this.label = label;
        this.type = type;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DynamicInputParam extends CleanUpRuleParam {
        public DynamicInputParam(String field, String label) {
            super(field, label, Type.DYNAMIC_INPUT);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DynamicInputPairParam extends CleanUpRuleParam {

        private String keyField;
        private String keyPlaceholder;

        private String valueField;
        private String valuePlaceholder;

        public DynamicInputPairParam(String field, String label) {
            super(field, label, Type.DYNAMIC_PAIR_INPUT);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class InputParam extends CleanUpRuleParam {
        public InputParam(String field, String label) {
            super(field, label, Type.INPUT);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SwitchParam extends CleanUpRuleParam {
        public SwitchParam(String field, String label) {
            super(field, label, Type.SWITCH);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SelectParam extends CleanUpRuleParam {

        private List<SelectParamOption> options;

        public SelectParam(String field, String label) {
            super(field, label, Type.SELECT);
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class SelectParamOption {
            private String value;
            private String label;
        }

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class NumberParam extends CleanUpRuleParam {
        public NumberParam(String field, String label) {
            super(field, label, Type.NUMBER);
        }
    }

    public enum Type {
        INPUT,
        SELECT,
        NUMBER,
        SWITCH,
        DYNAMIC_INPUT,
        DYNAMIC_PAIR_INPUT
    }
}
