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

3. Run java application. Application will run at port http://localhost:8082.

## OpenAPI
- Apis are documented using OpenAPI spring library 'springdoc'. UI to view docs can be found under
http://localhost:8082/swagger-ui/index.html

## Concept

The service allows registered and logged-in user to request for a loan. The system checks if loan due date beyond current date.
The system also checks if user is not blacklisted as a defaulter. If the loan amount is more than the customers Loan Limit the request 
is rejected. 

If an active loan for the customer is found, the due date is check to see if the customer has defaulted, if so they are blacklisted and active
loan is cleared. If not passed due date, the amount is checked against the balance from what the Loan Limit for the customer is and the debt on
the active loan. If the balance is not enough to cover the requested amount, the request is denied.

To pre-populate the service with test users. Execute: http://localhost:8082/api/auth/populate-records.
Users must be logged in /api/auth/login. Requests for /api/loans/request-loan and /api/loans/pay-loan require
logged-in user.

The service also allows generation of dump files of loans from the database via /api/loans/data-dump. The request is 
initated by an Admin. A csv file is generated and uploaded on a sftp server initialized earlier via docker-compose.

