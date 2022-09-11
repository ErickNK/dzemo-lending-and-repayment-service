package com.flycode.lendingandrepaymentservice.configs;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;

@Configuration
public class SftpConfig {

    @Autowired
    Environment environment;

    @Bean
    public SSHClient setupSshj() throws IOException {
        SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect(environment.getRequiredProperty("data-dump-job.remote-host.host"),
                environment.getRequiredProperty("data-dump-job.remote-host.port", Integer.class));
        client.authPassword(
                environment.getRequiredProperty("data-dump-job.remote-host.username"),
                environment.getRequiredProperty("data-dump-job.remote-host.password")
        );
        return client;
    }
}
