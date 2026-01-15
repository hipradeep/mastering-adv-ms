# AWS Interview Questions & Scenarios (Spring Boot Focus)

## 1. General AWS Concepts
*   **Q: What is the difference between a Region, an Availability Zone (AZ), and an Edge Location?**
    *   *A*: Region is a physical location with multiple AZs. AZ is a data center (distinct power/network). Edge Location is for CloudFront caching close to users.
*   **Q: Explain the Shared Responsibility Model.**
    *   *A*: AWS manages security **OF** the cloud (hardware, facilities). Customer manages security **IN** the cloud (data, OS patching, IAM, firewall config).
*   **Q: What is the difference between specific Instance Types (e.g., T3 vs C5)?**
    *   *A*: T3 is Burstable (good for traffic spikes). C5 is Compute Optimized (good for batch processing/scientific modeling).

## 2. Spring Boot on AWS (Scenarios)
### Scenario A: High Availability
*   **Q: How do you ensure your Spring Boot application survives a data center failure?**
    *   *A*:
        1.  **Multiple AZs**: Deploy EC2 instances in at least 2 public/private subnets across different AZs.
        2.  **Load Balancer**: Use an Application Load Balancer (ALB) to distribute traffic across these instances.
        3.  **Auto Scaling Group**: Ensure the ASG has `Min=2` and `Desired=2` (1 per AZ) to automatically replace failed instances.
        4.  **Database**: Use RDS Multi-AZ Deployment so the DB automatically fails over to the standby AZ.

### Scenario B: Security & Secrets
*   **Q: You have database credentials in `application.properties`. Why is this bad, and how do you fix it?**
    *   *A*: Hardcoding secrets is a security risk (committed to Git).
    *   *Fix*: Use **AWS Systems Manager Parameter Store** or **Secrets Manager**.
    *   *Spring Boot Impl*: Use `spring-cloud-starter-aws-parameter-store-config`. The app fetches secrets at runtime using the EC2 IAM Role.

### Scenario C: Performance & S3
*   **Q: Users complain that uploading large files to your app is slow/failing. How do you fix it?**
    *   *A*: The file is likely going Client -> Spring Boot -> S3, which blocks threads and adds latency.
    *   *Fix*: Generate a **Presigned URL** in Spring Boot. Send this URL to the Client (React). The Client uploads **directly** to S3.

## 3. Networking & VPC
*   **Q: Why can't I SSH into my EC2 instance in a Private Subnet?**
    *   *A*: It has no public IP. You need a **Bastion Host** (Jump Box) in the Public Subnet or use **AWS Systems Manager Session Manager** (preferred, no port 22 needed).
*   **Q: My Spring Boot app in a Private Subnet needs to call an external API (e.g., Stripe). It fails. Why?**
    *   *A*: Private Subnets have no route to the Internet.
    *   *Fix*: Create a **NAT Gateway** in the Public Subnet and update the Private Route Table to route `0.0.0.0/0` to the NAT Gateway.

## 4. Troubleshooting
*   **Q: "Connection Refused" vs "Connection Timed Out"**
    *   *A*:
        *   *Refused*: The request reached the server, but nothing is listening on that port (App crashed?).
        *   *Timed Out*: The request was dropped by a firewall (Security Group/NACL Issue).
*   **Q: RDS Failover happened, but my Spring Boot app is stuck.**
    *   *A*: The DB DNS record updated, but the Java JVM (DNS Cache) or Connection Pool (HikariCP) is holding onto the old IP.
    *   *Fix*: Configure HikariCP `max-lifetime` to be shorter than the DNS TTL (e.g., 10-15 minutes) so it refreshes connections.
