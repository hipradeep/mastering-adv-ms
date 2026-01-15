# ReactJS Project Deployment on AWS

## 1. Architecture
*   **Storage**: S3 Bucket (configured for Static Website Hosting).
*   **Delivery**: CloudFront (CDN) for HTTPS, global caching, and low latency.
*   **Domain**: Route 53 pointing to CloudFront.

## 2. Build React App
1.  **Configure Environment**:
    Inside `.env.production` (or similar):
    ```env
    REACT_APP_API_URL=https://api.mydomain.com
    ```
    *Note: Point this to your Spring Boot ALB/Domain, NOT localhost.*
2.  **Build**:
    ```bash
    npm run build
    # Output: /build directory
    ```

## 3. S3 Static Hosting
1.  **Create Bucket**: `my-react-app-production`.
2.  **Properties**: Enable **Static website hosting**.
    *   Index document: `index.html`
    *   Error document: `index.html` (Crucial for React Router client-side routing).
3.  **Permissions**:
    *   Uncheck "Block all public access".
    *   **Bucket Policy**:
        ```json
        {
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Sid": "PublicReadGetObject",
                    "Effect": "Allow",
                    "Principal": "*",
                    "Action": "s3:GetObject",
                    "Resource": "arn:aws:s3:::my-react-app-production/*"
                }
            ]
        }
        ```

## 4. CloudFront (CDN) Setup
Why? Because S3 website endpoints do not support HTTPS with custom domains.
1.  **Create Distribution**:
    *   **Origin Domain**: Select your S3 bucket endpoint.
    *   **Viewer Protocol Policy**: Redirect HTTP to HTTPS.
    *   **Custom SSL Certificate**: Request a free cert in AWS ACM (us-east-1).
2.  **Invalidations**: When you deploy new code, you MUST invalidate the cache (usually `/*`) so users see the new version immediately.

## 5. Domain Setup (Route 53)
1.  **Hosted Zone**: Ensure you have a public hosted zone for `mydomain.com`.
2.  **Create Record**:
    *   Record Name: `www` (or empty for root).
    *   Type: **A - IPv4 address**.
    *   Alias: **Yes**.
    *   Route traffic to: **Alias to CloudFront Distribution**.

## 6. Backend Integration (CORS)
Since your React app (`https://www.mydomain.com`) connects to Spring Boot (`https://api.mydomain.com`), you **MUST** enable CORS in Spring Boot.

**In Spring Boot (`WebConfig.java`):**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://www.mydomain.com", "http://localhost:3000") // Trust your frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

## 7. CI/CD Options
### Option A: AWS Amplify (Easiest)
1.  Connect GitHub Repository.
2.  Amplify automatically detects React.
3.  It handles Build, S3, CloudFront, and SSL management automatically.

### Option B: Manual / GitHub Actions
```yaml
# GitHub Actions Example Step
- name: Deploy to S3
  run: aws s3 sync build/ s3://my-react-app-production --delete

- name: Invalidate CloudFront
  run: aws cloudfront create-invalidation --distribution-id E1234567890 --paths "/*"
```
