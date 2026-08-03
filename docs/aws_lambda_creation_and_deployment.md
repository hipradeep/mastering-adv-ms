# AWS Lambda Function Creation & Deployment Guide

This guide details three methods for creating, configuring, and deploying the **Spring Boot Automated S3 Trigger Lambda Function** directly on the **AWS Lambda Service**.

---

## 📋 Overview & Prerequisites

Before deploying, ensure you have:
1. **Built the Shaded JAR**:
   ```bash
   mvn clean package
   ```
   This generates the deployment artifact: `target/s3-trigger-service-1.0.0-SNAPSHOT.jar`.

2. **Required Lambda Configurations**:
   * **Runtime**: `Java 17 (Corretto)`
   * **Handler**: `org.springframework.cloud.function.adapter.aws.FunctionInvoker`
   * **Memory**: `1024 MB` (Recommended for Spring Boot)
   * **Timeout**: `60 seconds`
   * **Environment Variables**:
     * `MAIN_CLASS`: `com.example.s3trigger.S3TriggerApplication`
     * `SPRING_CLOUD_FUNCTION_DEFINITION`: `s3EventConsumer`
     * `PROCESSED_BUCKET_NAME`: `<your-destination-bucket-name>`

---

## 🚀 Method 1: Automated Deployment via AWS SAM (Recommended)

Using **AWS SAM (Serverless Application Model)** automates the creation of the Lambda function, S3 buckets, IAM roles, and S3 event triggers.

### 1. Build SAM Application
```bash
sam build
```

### 2. Deploy to AWS
```bash
sam deploy --guided
```

### Prompt Inputs:
* **Stack Name**: `spring-s3-trigger-stack`
* **AWS Region**: `us-east-1` (or your preferred region)
* **Confirm IAM role creation**: `y`
* **Save arguments to configuration file**: `y`

---

## 🖥 Method 2: Manual Creation via AWS Management Console

If you prefer building the function using the AWS Web Console:

### Step 1: Create the Function
1. Log in to the **AWS Management Console** and navigate to **AWS Lambda**.
2. Click **Create function**.
3. Select **Author from scratch**.
4. Configure Function Settings:
   * **Function name**: `s3-trigger-service`
   * **Runtime**: `Java 17 (Corretto)`
   * **Architecture**: `x86_64`
5. Click **Create function**.

### Step 2: Upload Application Code
1. On the function page, stay in the **Code** tab.
2. Click **Upload from** -> **.zip or .jar file**.
3. Select `target/s3-trigger-service-1.0.0-SNAPSHOT.jar` and click **Save**.

### Step 3: Configure Handler & Environment Variables
1. Scroll down to **Runtime settings** -> Click **Edit**.
2. Change **Handler** to:
   ```text
   org.springframework.cloud.function.adapter.aws.FunctionInvoker
   ```
3. Click **Save**.
4. Navigate to **Configuration** -> **Environment variables** -> Click **Edit**.
5. Add the following key-value pairs:
   | Key | Value |
   |---|---|
   | `MAIN_CLASS` | `com.example.s3trigger.S3TriggerApplication` |
   | `SPRING_CLOUD_FUNCTION_DEFINITION` | `s3EventConsumer` |
   | `PROCESSED_BUCKET_NAME` | `my-processed-destination-bucket` |
6. Click **Save**.

### Step 4: Bind S3 Trigger
1. In the **Function overview** section at the top, click **+ Add trigger**.
2. In the dropdown, select **S3**.
3. Select your **Source S3 Bucket**.
4. Set **Event type** to **All object create events (`s3:ObjectCreated:*`)**.
5. Check the recursive invocation acknowledgment checkbox.
6. Click **Add**.

---

## 💻 Method 3: Creation via AWS CLI

You can create and configure the function programmatically using the AWS CLI.

### 1. Create the Lambda Function
```bash
aws lambda create-function \
  --function-name s3-trigger-service \
  --runtime java17 \
  --role arn:aws:iam::123456789012:role/service-role/LambdaExecutionRole \
  --handler org.springframework.cloud.function.adapter.aws.FunctionInvoker \
  --zip-file fileb://target/s3-trigger-service-1.0.0-SNAPSHOT.jar \
  --timeout 60 \
  --memory-size 1024 \
  --environment "Variables={MAIN_CLASS=com.example.s3trigger.S3TriggerApplication,SPRING_CLOUD_FUNCTION_DEFINITION=s3EventConsumer,PROCESSED_BUCKET_NAME=my-processed-destination-bucket}"
```

### 2. Grant S3 Permission to Invoke Lambda
```bash
aws lambda add-permission \
  --function-name s3-trigger-service \
  --statement-id s3-trigger-permission \
  --action lambda:InvokeFunction \
  --principal s3.amazonaws.com \
  --source-arn arn:aws:s3:::my-source-upload-bucket
```

### 3. Add S3 Bucket Event Notification
```bash
aws s3api put-bucket-notification-configuration \
  --bucket my-source-upload-bucket \
  --notification-configuration '{
    "LambdaFunctionConfigurations": [
      {
        "LambdaFunctionArn": "arn:aws:lambda:us-east-1:123456789012:function:s3-trigger-service",
        "Events": ["s3:ObjectCreated:*"]
      }
    ]
  }'
```

---

## 🔍 Validation & Logs

After creating the function:
1. Upload a sample file (e.g., `data.json`) to your source S3 bucket.
2. Check **CloudWatch Logs**:
   * Open **AWS CloudWatch** -> **Log Groups** -> `/aws/lambda/s3-trigger-service`.
   * Inspect execution logs verifying that `S3ProcessorService` output is logged cleanly.
