package com.flycode.lendingandrepaymentservice.configs;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
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

//    @Bean
//    public ChannelSftp setupJsch() throws JSchException {
//        JSch jsch = new JSch();
//        Session jschSession = jsch.getSession(
//                environment.getRequiredProperty("data-dump-job.remote-host.username"),
//                environment.getRequiredProperty("data-dump-job.remote-host.url")
//        );
//        jschSession.setPassword(environment.getRequiredProperty("data-dump-job.remote-host.password"));
//        jschSession.connect();
//        return (ChannelSftp) jschSession.openChannel("sftp");
//    }

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
