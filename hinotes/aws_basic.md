# AWS Basics & Global Infrastructure

## 1. Global Infrastructure
AWS infrastructure is built around Regions, Availability Zones (AZs), and Edge Locations.

*   **Region**: A physical location in the world where AWS has multiple Availability Zones. Regions are isolated from each other for fault tolerance and compliance.
*   **Availability Zone (AZ)**: One or more discrete data centers with redundant power, networking, and connectivity in an AWS Region. AZs are interconnected with high-bandwidth, low-latency networking.
*   **Edge Locations**: Endpoints for AWS which are used for caching content. Typically this consists of CloudFront, Amazon's Content Delivery Network (CDN).
*   **Local Zones**: Place compute, storage, database, and other select AWS services closer to end-users (e.g., for low latency apps).

## 2. Shared Responsibility Model
Security and compliance is a shared responsibility between AWS and the customer.

*   **AWS Responsibility (Security OF the Cloud)**:
    *   Physical security of data centers.
    *   Hardware and software infrastructure (servers, storage, virtualization layer).
    *   Network infrastructure (cabling, routers).
*   **Customer Responsibility (Security IN the Cloud)**:
    *   Customer data (encryption).
    *   Platform, Applications, Identity & Access Management (IAM).
    *   Operating System, Network & Firewall configuration.

## 3. Identity and Access Management (IAM)
IAM allows you to manage access to AWS services and resources securely.

*   **Key Components**:
    *   **Users**: Detailed access for individuals (has a name and credentials).
    *   **Groups**: Collections of users. Permissions assigned to a group apply to all users in that group.
    *   **Roles**: An identity that you can create that has specific permissions. Intended to be assumable by anyone who needs it (e.g., an EC2 instance, a Lambda function, or a cross-account user).
    *   **Policies**: JSON documents that define permissions (Allow/Deny actions on resources).

*   **Best Practices**:
    *   **Root Account**: Never use the root account for daily tasks. Secure it with MFA (Multi-Factor Authentication).
    *   **Principle of Least Privilege**: Grant only the permissions required to perform a task.
    *   **MFA**: Enable Multi-Factor Authentication for all users, especially the root account and privileged users.
    *   **Access Keys**: Rotate access keys regularly. Use IAM Roles for applications running on EC2 instead of storing access keys.

### Example IAM Policy (JSON)
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::example_bucket"
    }
  ]
}
```

## 4. Key Services Overview
*   **Compute**: EC2 (Virtual Servers), Lambda (Serverless), ECS/EKS (Containers).
*   **Storage**: S3 (Object Storage), EBS (Block Storage for EC2), EFS (File Storage).
*   **Database**: RDS (Relational), DynamoDB (NoSQL), ElastiCache (Caching).
*   **Networking**: VPC (Virtual Network), Route 53 (DNS), CloudFront (CDN).

## 5. Spring Boot & Microservices Perspectives
### IAM for Spring Boot
*   **Avoid Hardcoding Keys**: Never store `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` in `application.properties`.
*   **Instance Profiles**: When running Spring Boot on EC2, assign an **IAM Role** to the EC2 instance.
*   **Default Credential Provider Chain**: The AWS SDK for Java automatically looks for credentials in this order:
    1.  Environment Variables.
    2.  Java System Properties.
    3.  Web Identity Token.
    4.  ~/.aws/credentials profile.
    5.  **EC2 Instance Metadata Service** (via the attached IAM Role).

### Cloud-Native Development
*   **AWS SDK for Java**: The standard library for interacting with AWS services programmatically.
*   **Spring Cloud AWS**: A framework that simplifies AWS integration using Spring idioms (e.g., auto-configuring S3 clients, parameter store property sources).

