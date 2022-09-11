package com.flycode.lendingandrepaymentservice.batch;

import com.flycode.lendingandrepaymentservice.dtos.Response;
import com.flycode.lendingandrepaymentservice.utils.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class DataDumpingJob {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job sftpDataDumbingJob;

    @Async
    public CompletableFuture<Response<Boolean>> execute(Principal principal) {
        try {

            LogHelper.builder(log)
                    .logMsg("Running data dumping job")
                    .info();

            JobExecution execution = jobLauncher.run(
                    sftpDataDumbingJob,
                    new JobParametersBuilder()
                            .addString("requester", principal.getName())
                            .addDate("date", new Date())
                            .toJobParameters()
            );

            LogHelper.builder(log)
                    .logMsg("Data dump job finished")
                    .logDetailedMsg("Job Status: " + execution.getExitStatus())
                    .error();
            return CompletableFuture.completedFuture(Response.successResponse(Boolean.TRUE));
        } catch (Exception exception) {
            LogHelper.builder(log)
                    .logMsg("Error when fetching elevators")
                    .logDetailedMsg(exception.getMessage())
                    .error();

            Response<Boolean> response = new Response<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null,
                    exception.getMessage()
            );
            return CompletableFuture.completedFuture(response);
        }
    }

}
