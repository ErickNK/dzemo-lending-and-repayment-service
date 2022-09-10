package com.flycode.lendingandrepaymentservice.batch;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

//@Configuration
public class BatchConfigs {
//    @Bean
    public JobRepository getJobRepository() throws Exception {
        var embeddedDatasource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("/org/springframework/batch/core/schema-drop-h2.sql")
                .addScript("/org/springframework/batch/core/schema-h2.sql")
                .build();

        var jdbcTransactionManager = new JdbcTransactionManager();
        jdbcTransactionManager.setDataSource(embeddedDatasource);
        jdbcTransactionManager.afterPropertiesSet();

        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(embeddedDatasource);
        factory.setTransactionManager(jdbcTransactionManager);
        factory.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ");
        return factory.getObject();
    }
}
