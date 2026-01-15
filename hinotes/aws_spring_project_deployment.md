# Spring Boot Project Deployment on AWS (End-to-End Guide)

## 1. Prerequisites
*   **Java 17+ installed** (locally and on EC2)
*   **Maven installed**
*   **AWS CLI installed and configured** (`aws configure`)
*   **A running RDS Instance** (MySQL/PostgreSQL) with a known Endpoint.
*   **An S3 bucket** created.

## 2. Infrastructure Setup (Review)
Ensure you have the following architecture:
1.  **VPC**: One Custom VPC with **Public Subnets** (for Web/Bastion) and **Private Subnets** (for App/DB).
2.  **RDS**: Running in the Private Subnet, Multi-AZ enabled for production.
3.  **Security Groups**:
    *   `SG-LoadBalancer`: Allow Inbound 80/443 from `0.0.0.0/0`.
    *   `SG-AppServer`: Allow Inbound 8080 from `SG-LoadBalancer`.
    *   `SG-Database`: Allow Inbound 3306/5432 from `SG-AppServer`.

## 3. Preparation & Build
1.  **Configure Application**:
    Update `src/main/resources/application.properties` (or use System Properties on run):
    ```properties
    server.port=8080
    spring.datasource.url=jdbc:mysql://${RDS_ENDPOINT}:3306/mydb
    spring.datasource.username=${DB_USER}
    spring.datasource.password=${DB_PASS}
    spring.jpa.hibernate.ddl-auto=update
    ```
2.  **Build the JAR**:
    ```bash
    mvn clean package -DskipTests
    # Output: target/my-app-0.0.1-SNAPSHOT.jar
    ```

## 4. Deployment Options

### Option A: Manual (EC2 + User Data)
The simplest way to start.
1.  **Upload JAR to S3**:
    ```bash
    aws s3 cp target/my-app.jar s3://my-deploy-bucket/v1/app.jar
    ```
2.  **Launch EC2**:
    *   Select Amazon Linux 2023.
    *   **IAM Role**: Attach role with `AmazonS3ReadOnlyAccess`.
    *   **User Data**:
        ```bash
        #!/bin/bash
        dnf update -y
        dnf install java-17-amazon-corretto -y
        aws s3 cp s3://my-deploy-bucket/v1/app.jar /home/ec2-user/app.jar
        export RDS_ENDPOINT=my-rds-endpoint.us-east-1.rds.amazonaws.com
        export DB_USER=admin
        export DB_PASS=SecretPassword123!
        java -jar /home/ec2-user/app.jar > /var/log/app.log 2>&1 &
        ```

### Option B: AWS Elastic Beanstalk (PaaS)
1.  **Install EB CLI**: `pip install awsebcli`
2.  **Initialize**: `eb init -p java-17 my-app-env`
3.  **Configure Vars**: `eb setenv RDS_ENDPOINT=... DB_USER=...`
4.  **Deploy**: `eb create` (This automatically creates EC2, ALB, ASG, and SGs).

### Option C: Docker (ECS/Fargate)
1.  **Dockerfile**:
    ```dockerfile
    FROM amazoncorretto:17
    COPY target/my-app.jar app.jar
    ENTRYPOINT ["java", "-jar", "/app.jar"]
    ```
2.  **Build & Push to ECR**:
    ```bash
    aws ecr create-repository --repository-name my-app
    docker build -t my-app .
    docker tag my-app:latest <account_id>.dkr.ecr.us-east-1.amazonaws.com/my-app:latest
    docker push <account_id>.dkr.ecr.us-east-1.amazonaws.com/my-app:latest
    ```
3.  **Run Task**: Create an ECS Fargate Cluster and run the Task Definition pointing to this ECR image.

## 5. CI/CD with AWS CodePipeline
Automate the deployment.
1.  **Source Stage**: Connect GitHub/CodeCommit repo. Triggers on commit.
2.  **Build Stage**: Use **AWS CodeBuild**.
    *   `buildspec.yml`: Runs `mvn package`.
3.  **Deploy Stage**: Use **AWS CodeDeploy** (for EC2) or ECS Deploy.

## 6. Monitoring
*   **CloudWatch Logs**: Install CloudWatch Agent on EC2 to stream `/var/log/app.log` to CloudWatch.
*   **CloudWatch Metrics**: CPU, Memory, Disk I/O.
*   **Spring Boot Actuator**: Expose metrics to Prometheus/Grafana or CloudWatch.
