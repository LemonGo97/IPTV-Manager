package com.lemongo97.iptv.iptvmanager.config;

import org.quartz.ee.sqlite.SQLiteJobStore;
import org.quartz.ee.simple.SimpleThreadPool;
import org.quartz.simpl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Quartz 配置
 * 使用 SQLite 作为 JobStore 存储 Quartz 相关表
 */
@Configuration
public class QuartzConfig {

    @Value("${quartz.job-store-class:org.quartz.ee.sqlite.SQLiteJobStore}")
    private String jobStoreClass;

    @Value("${quartz.driverDelegateClass:org.quartz.impl.jdbcjobstore.StdJDBCDelegate}")
    private String driverDelegateClass;

    @Value("${quartz.jobSelectTriggerType:jdbc}")
    private String jobSelectTriggerType;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJobFactory(springBeanJobFactory());
        factory.setQuartzProperties(quartzProperties());
        factory.setAutoStartup(true);
        return factory;
    }

    @Bean
    public org.quartz.simpl.SpringBeanJobFactory springBeanJobFactory() {
        org.quartz.simpl.SpringBeanJobFactory jobFactory = new org.quartz.simpl.SpringBeanJobFactory();
        jobFactory.setApplicationContext(getApplicationContext());
        return jobFactory;
    }

    private Properties quartzProperties() {
        Properties props = new Properties();
        props.put("org.quartz.scheduler.instanceName", "IPTVManagerScheduler");
        props.put("org.quartz.scheduler.instanceId", "AUTO");
        props.put("org.quartz.scheduler.skipUpdateCheck", "true");
        props.put("org.quartz.jobStore.class", jobStoreClass);
        props.put("org.quartz.jobStore.driverDelegateClass", driverDelegateClass);
        props.put("org.quartz.jobStore.selectWithLockToSchedule", jobSelectTriggerType);
        props.put("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
        props.put("org.quartz.threadPool.threadCount", "5");
        props.put("org.quartz.threadPool.threadPriority", "5");
        props.put("org.quartz.jobStore.misfireThreshold", "5000");
        props.put("org.quartz.jobStore.txIsolationLevelSerializable", "true");
        props.put("org.quartz.jobStore.acquireTriggersWithinLock", "true");
        return props;
    }
}
