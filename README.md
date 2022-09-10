# Lending And Repayment Service Tech Challenge

This service allows users to take loans and repay loans.

## Prerequisites
1. Docker and Docker Compose installed on machine.
2. Java 17 installed on machine.

## Setup
1. Docker Compose issued to create services that the whole application requires. 
   - To start up services, navigate to root directory of project.
   - ```shell
     $ cd docker && sudo docker-compose -f docker-compose.dev.yml up -d mysql sftp
     ```
2. Find SQL schema scripts to setup database on root directory
   - execute scripts on mysql docker container created by docker-compose under localhost:3306

3. Run java application