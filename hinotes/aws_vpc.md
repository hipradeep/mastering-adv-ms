# AWS VPC (Virtual Private Cloud) Deep Dive

## 1. Core Components
*   **VPC**: Logically isolated section of the AWS Cloud. Defined by a CIDR block (e.g., `10.0.0.0/16`).
*   **Subnet**: A segment of the VPC's IP address range where you launch resources. Mapped to a single Availability Zone.
    *   **Public Subnet**: Has a route to an Internet Gateway (IGW).
    *   **Private Subnet**: No direct route to the Internet.

## 2. Routing
*   **Route Table**: Contains rules (routes) used to determine where network traffic is directed.
*   **Internet Gateway (IGW)**: Enables communication between instances in your VPC and the internet.
*   **NAT Gateway**: Allows instances in a **Private Subnet** to connect to the internet (e.g., for software updates) but prevents the internet from initiating a connection to those instances.

## 3. Security Layers
| Feature | Security Group | Network ACL (NACL) |
| :--- | :--- | :--- |
| **Level** | Instance Level | Subnet Level |
| **State** | **Stateful** (Return traffic allowed) | **Stateless** (Must explicitly allow return) |
| **Rules** | Allow only | Allow and Deny |
| **Use Case** | Primary firewall for EC2/RDS | Additional layer of defense |

## 4. Connectivity Options
*   **VPC Peering**: Connect two VPCs (even in different accounts) privately.
*   **Transit Gateway**: Hub-and-spoke design for connecting many VPCs and on-premise networks.
*   **VPN (Site-to-Site)**: Encrypted connection over the public internet between on-premise & AWS.
*   **Direct Connect**: Dedicated physical fiber connection (No public internet).

## 5. Spring Boot & Microservices Perspectives
### Microservice Isolation
*   **Architecture**:
    *   **Public Subnet**: Load Balancer (ALB), Bastion Host.
    *   **Private Subnet**: Spring Boot EC2 Instances, RDS Database, ElastiCache.
*   **Reasoning**: Direct internet access to the backend is blocked. Attack surface is minimized.

### Outbound Access
*   Your Spring Boot app in the private subnet might need to call external APIs (e.g., Stripe, SendGrid).
*   **Solution**: Route traffic through a **NAT Gateway** in the public subnet.

### VPC Endpoints (PrivateLink)
*   **Scenario**: Your app needs to access S3 or SQS. By default, traffic goes over the public internet (via NAT).
*   **Optimization**: Use a **VPC Endpoint**. Traffic stays entirely within the AWS network.
    *   **Gateway Endpoint**: S3 and DynamoDB.
    *   **Interface Endpoint**: SQS, SNS, Kinesis, etc.
