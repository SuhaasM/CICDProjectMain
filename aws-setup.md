# AWS Deployment Setup

## Prerequisites
1. AWS Account
2. EC2 instance (t2.medium or higher recommended)
3. Docker and Docker Compose installed on EC2
4. Security Group with ports 80, 12345, and 3306 open

## EC2 Setup Instructions

1. Launch an EC2 instance with Amazon Linux 2 or Ubuntu
2. Connect to your instance:
   ```
   ssh -i "your-key.pem" ec2-user@your-instance-public-dns
   ```

3. Install Docker:
   ```
   sudo yum update -y
   sudo amazon-linux-extras install docker -y
   sudo service docker start
   sudo systemctl enable docker
   sudo usermod -a -G docker ec2-user
   ```

4. Install Docker Compose:
   ```
   sudo curl -L "https://github.com/docker/compose/releases/download/v2.18.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   sudo chmod +x /usr/local/bin/docker-compose
   ```

5. Create GitHub repository secrets:
   - `AWS_ACCESS_KEY_ID`: Your AWS access key
   - `AWS_SECRET_ACCESS_KEY`: Your AWS secret key
   - `AWS_REGION`: Your AWS region (e.g., us-east-1)
   - `EC2_HOST`: Your EC2 instance public DNS
   - `EC2_USERNAME`: Your EC2 username (e.g., ec2-user)
   - `SSH_PRIVATE_KEY`: Your EC2 SSH private key
   - `DOCKER_HUB_USERNAME`: Your Docker Hub username
   - `DOCKER_HUB_TOKEN`: Your Docker Hub token

## Manual Deployment (if needed)

1. SSH into your EC2 instance
2. Clone the repository:
   ```
   git clone https://github.com/SuhaasM/CICDProjectMain.git
   cd CICDProjectMain
   ```

3. Run with Docker Compose:
   ```
   docker-compose up -d
   ```

4. Access your application at:
   ```
   http://your-ec2-public-dns
   ```