package com.simon.quartz.core.constant;

public interface Constants {

    /** 日志表状态 */
    interface JOBSTATE {
        /**
         * 日志表状态，初始状态，插入
         */
        static final String INIT_STATS = "I";
        /**
         * 日志表状态，成功
         */
        static final String SUCCESS_STATS = "S";
        /**
         * 日志表状态，失败
         */
        static final String ERROR_STATS = "E";
        /**
         * 日志表状态，未执行
         */
        static final String UN_STATS = "N";
    }
}
