package com.flycode.lendingandrepaymentservice.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.flycode.lendingandrepaymentservice.models.Loan;
import com.flycode.lendingandrepaymentservice.models.mappers.LoanMapper;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.OpenMode;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
@EnableBatchProcessing
public class DataDumpingJobConfiguration {

    @Autowired
    Environment environment;

    @Autowired
    DataSource dataSource;

    @Autowired
    SSHClient sshClient;

    @Autowired
    Clock clock;

    private JdbcCursorItemReader<Loan> reader() {
        var sql = "SELECT * FROM loans";

        return new JdbcCursorItemReaderBuilder<Loan>()
                .dataSource(dataSource)
                .name("DataDumpingStepReader")
                .sql(sql)
                .rowMapper(new LoanMapper(clock))
                .maxRows(environment.getRequiredProperty("data-dump-job.chunk-size", Integer.class))
                .fetchSize(environment.getRequiredProperty("data-dump-job.chunk-size", Integer.class))
                .build();
    }

    private ItemWriter<Loan> writer() {
        return list -> {
            if (list.isEmpty()) { // nothing to write
                return;
            }

            // write to csv file
            List<String> stringList = new ArrayList<>();
            CsvMapper mapper = new CsvMapper();
            mapper.findAndRegisterModules();
            CsvSchema schema = mapper.schemaFor(Loan.class);

            for (Loan loan : list) {
                try {
                    String csv = mapper.writer(
                                    schema
                                            .withColumnSeparator(',')
                                            .withQuoteChar('"')
                                            .withLineSeparator("\n")
                            )
                            .writeValueAsString(loan);
                    stringList.add(csv);
                } catch (JsonProcessingException e) {
                    // TODO: log
                    throw new RuntimeException(e);
                }
            }

            File csvOutputFile = ResourceUtils.getFile(environment.getRequiredProperty("data-dump-job.local-file-location"));
            if(!csvOutputFile.exists()){
                throw new RuntimeException("Could not get data dump file");
            }

            try(PrintWriter printWriter = new PrintWriter(csvOutputFile)) {
                stringList.forEach(printWriter::println);
            }
        };
    }

    private Tasklet uploadingTask() {
        return (stepContribution, chunkContext) -> {

            File csvOutputFile = ResourceUtils.getFile(environment.getRequiredProperty("data-dump-job.local-file-location"));
            if(!csvOutputFile.exists()){
                return RepeatStatus.FINISHED;
            }

            // upload to sftp server
            var sftpClient = sshClient.newSFTPClient();
            sftpClient.open(
                    environment.getRequiredProperty("data-dump-job.remote-file-location") + "loans-dump.csv",
                    Set.of(OpenMode.CREAT)
            );
            sftpClient.put(
                    csvOutputFile.getAbsolutePath(),
                    environment.getRequiredProperty("data-dump-job.remote-file-location") + "loans-dump.csv"
            );

            sftpClient.close();
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step dumbingStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("DataDumpingStep")
                .<Loan, Loan>chunk(environment.getRequiredProperty("data-dump-job.chunk-size", Integer.class))
                .reader(reader())
                .writer(writer())
                .build();
    }

    @Bean
    public Step uploadingStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("DefaultedLoansClearingStep")
                .tasklet(uploadingTask())
                .build();
    }

    @Bean
    public Job sftpDataDumbingJob(JobBuilderFactory jobBuilderFactory, Step dumbingStep, Step uploadingStep) {
        return jobBuilderFactory.get("LoansCleaningJobConfiguration")
                .incrementer(new RunIdIncrementer())
                .flow(dumbingStep)
                .next(uploadingStep)
                .end()
                .build();
    }

}
