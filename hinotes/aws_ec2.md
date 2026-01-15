# AWS EC2 (Elastic Compute Cloud) Deep Dive

## 1. Introduction
Amazon Elastic Compute Cloud (Amazon EC2) provides scalable computing capacity in the Amazon Web Services (AWS) cloud. It allows you to run virtual servers (EC2 instances).

## 2. Instance Types
AWS classifies instances based on their use cases.
*   **General Purpose (e.g., t3, m5)**: Balanced resources. Good for web servers, code repositories.
*   **Compute Optimized (e.g., c5, c6g)**: High performance processors. Good for batch processing, media transcoding, scientific modeling.
*   **Memory Optimized (e.g., r5, x1)**: High memory footprint. Good for real-time big data analytics, in-memory databases (Redis/Memcached).
*   **Storage Optimized (e.g., i3, d2)**: High I/O. Good for NoSQL databases, data warehousing.
*   **Accelerated Computing (e.g., p3, g4)**: Uses GPUs/FPGAs. Good for machine learning, graphics processing.

> **Naming Rule**: `m5.2xlarge` -> `m` (Family), `5` (Generation), `2xlarge` (Size).

## 3. Storage Options
### Elastic Block Store (EBS)
Persist block-level storage volumes for use with EC2 instances (like a hard drive).
*   **Volume Types**:
    *   **gp3/gp2 (General Purpose SSD)**: Balanced price/performance. Boot volumes, dev/test.
    *   **io2/io1 (Provisioned IOPS SSD)**: Highest performance. Mission-critical low-latency DBs.
    *   **st1 (Throughput Optimized HDD)**: Big data, data warehouses, log processing.
    *   **sc1 (Cold HDD)**: Lowest cost. Infrequently accessed data.
*   **Snapshots**: Point-in-time backup of an EBS volume. Stored in S3.
*   **Encryption**: EBS volumes can be encrypted at rest and in transit.

### Instance Store (Ephemeral Storage)
*   Directly attached to the host hardware.
*   **Ephemeral**: Data is **LOST** if the instance stops or terminates. Only survives reboots.
*   Use for temporary data like buffers, caches, scratch data.

## 4. Networking & Security
### Security Groups
*   **Stateful**: If you allow traffic in (Inbound), the response (Outbound) is automatically allowed.
*   Acts as a virtual firewall at the **Instance level**.
*   Default: Deny all inbound, Allow all outbound.

### Network ACLs (NACLs)
*   **Stateless**: You must explicitly allow both Inbound and Outbound traffic.
*   Acts as a firewall at the **Subnet level**.

### IP Addresses
*   **Private IP**: Retained when instance starts/stops.
*   **Public IP**: Dynamic. Changes if instance is stopped and started.
*   **Elastic IP (EIP)**: Static Public IP. You usually pay if it's allocated but not attached to a running instance.

### Placement Groups
*   **Cluster**: Pack instances close together inside an AZ (Low latency, high network throughput).
*   **Spread**: Place instances on distinct hardware (Critical apps to avoid hardware failure correlation).
*   **Partition**: Spread instances across logical partitions (Hadoop/Cassandra/Kafka).

## 5. Scaling & Load Balancing
### Elastic Load Balancing (ELB)
*   **Application Load Balancer (ALB - Layer 7)**: HTTP/HTTPS traffic. Routing based on path, headers, cookies. Good for microservices.
*   **Network Load Balancer (NLB - Layer 4)**: TCP/UDP traffic. Ultra-low latency. High throughput.
*   **Gateway Load Balancer (GLB - Layer 3)**: For third-party virtual appliances (firewalls).

### Auto Scaling Groups (ASG)
*   Automatically adjusts the number of EC2 instances to match demand.
*   **Launch Template**: Configuration for instances (AMI, Instance Type, Key Pair, Security Groups).
*   **Scaling Policies**:
    *   Target Tracking (e.g., keep CPU at 50%).
    *   Step/Simple Scaling.
    *   Scheduled Scaling.

## 6. Pricing Models
*   **On-Demand**: Pay by the second/hour. No commitment. Good for irregular short-term workloads.
*   **Reserved Instances (RI)**: 1 or 3-year commitment. Significant discount (up to 72%) vs On-Demand.
    *   *Standard*: Can't change instance family.
    *   *Convertible*: Can change instance attributes (but lower discount).
*   **Savings Plans**: Commitment to a consistent amount of usage ($/hr) for 1 or 3 years. flexible across instance types.
*   **Spot Instances**: Bid on unused capacity. Up to 90% discount. **Can be interrupted with 2 minutes notice**. Good for fault-tolerant, stateless workloads.
*   **Dedicated Hosts**: Physical server dedicated to you. For licensing compliance (BYOL) or regulatory requirements.

## 7. Interview Q&A Tips
*   **Connection Timeout**: Usually a Security Group issue (Missing Inbound rule).
*   **Connection Refused**: Application on the instance isn't running or listening on that port.
*   **"Permission Denied" (SSH key)**: Wrong private key (.pem) or wrong permissions on the key file (chmod 400).
*   **High Availability**: Use detailed monitoring, spread instances across multiple AZs, use ASG + ELB.
*   **Performance**: If disk I/O is the bottleneck, switch to specific EBS (io1/io2) or Instance Store. If CPU is bottleneck, scale up or out.

## 8. Spring Boot Deployment & Ops
### Deployment Flow
1.  **Build**: Package your application as an executable JAR (`mvn clean package`).
2.  **User Data**: Use EC2 User Data to automatically install Java and run the application on launch.
    ```bash
    #!/bin/bash
    yum update -y
    amazon-linux-extras install java-openjdk11 -y
    aws s3 cp s3://my-bucket/my-app.jar /home/ec2-user/app.jar
    java -jar /home/ec2-user/app.jar
    ```
### Configuration
*   **Security Groups**: Ensure Port **8080** (or your app port) is open in the inbound rules for the EC2 instance's Security Group to allow traffic.
*   **Load Balancing**: Configure the Application Load Balancer (ALB) Health Check to ping `/actuator/health` to ensure traffic is only sent to healthy instances.
*   **External Config**: Instead of baking secrets into `application.properties`, use **AWS Systems Manager Parameter Store** or **AWS Secrets Manager** to inject configuration at runtime.

