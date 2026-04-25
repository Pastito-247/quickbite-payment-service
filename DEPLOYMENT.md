# QuickBite Payment Service - Deployment Guide

## Overview
Microservicio de pagos para la plataforma QuickBite con seguridad JWT, integración real con pasarelas de pago y comunicación entre microservicios.

## 🚀 Features Implemented

### Security & Authentication
- **JWT Authentication Filter** with real token validation
- **Role-based Access Control** (CUSTOMER, ADMIN, KITCHEN_STAFF)
- **Data Tokenization** for sensitive payment information
- **HTTPS/TLS** configuration for production

### Payment Gateway Integration
- **Webpay Plus** real API integration with fallback simulation
- **MercadoPago** preferences, payments, and refunds
- **Wallet Service** for virtual balance management
- **Circuit Breaker** pattern for fault tolerance

### Microservice Communication
- **Order Service Client** with OpenFeign
- **Real-time notifications** for payment status changes
- **Async transaction logging** for audit trails

## 📋 Environment Variables

### Required for Production
```bash
# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-min-256-bits
JWT_EXPIRATION=86400000

# Webpay Configuration
WEBPAY_API_KEY=your-webpay-api-key
WEBPAY_API_SECRET=your-webpay-api-secret
WEBPAY_COMMERCE_CODE=your-commerce-code
WEBPAY_ENVIRONMENT=production

# MercadoPago Configuration
MERCADOPAGO_ACCESS_TOKEN=your-mp-access-token
MERCADOPAGO_PUBLIC_KEY=your-mp-public-key
MERCADOPAGO_ENVIRONMENT=production

# SSL/TLS Configuration
SERVER_SSL_ENABLED=true
SERVER_SSL_KEYSTORE=/path/to/keystore.p12
SERVER_SSL_KEYSTORE_PASSWORD=your-keystore-password
SERVER_SSL_KEYSTORE_TYPE=PKCS12
SERVER_SSL_KEY_ALIAS=quickbite-payment

# Microservice URLs
ORDER_SERVICE_URL=https://order-service.quickbite.com
KITCHEN_SERVICE_URL=https://kitchen-service.quickbite.com
```

### Optional Development Variables
```bash
# Use default values for development
SERVER_SSL_ENABLED=false
SERVER_HTTP_PORT=8082
WEBPAY_ENVIRONMENT=integration
MERCADOPAGO_ENVIRONMENT=sandbox
```

## 🔧 Configuration Files

### Application Properties
The service uses environment variables with sensible defaults:

- **Database**: MySQL with connection pooling
- **Circuit Breaker**: Resilience4j with 50% failure threshold
- **Security**: Spring Security with JWT filter
- **Actuator**: Health checks and metrics enabled

### Docker Configuration
```bash
# Build and run
docker-compose up -d

# View logs
docker-compose logs -f payment-service

# Scale for load testing
docker-compose up -d --scale payment-service=3
```

## 🔒 Security Implementation

### JWT Token Structure
```json
{
  "sub": "username",
  "userId": "user-uuid",
  "roles": ["CUSTOMER"],
  "iat": 1640995200,
  "exp": 1641081600
}
```

### API Endpoint Security
| Endpoint | Required Roles | Description |
|----------|----------------|-------------|
| `POST /api/payments/process` | CUSTOMER, ADMIN | Process payment |
| `POST /api/payments/{id}/refund` | ADMIN, KITCHEN_STAFF | Refund payment |
| `GET /api/payments/**` | CUSTOMER, ADMIN, KITCHEN_STAFF | View payments |
| `POST /api/wallets/**` | CUSTOMER, ADMIN | Wallet operations |
| `GET /api/transactions/**` | ADMIN, KITCHEN_STAFF | Transaction history |

### Data Tokenization
- Sensitive data encrypted with AES-256
- Field-level tokenization for card numbers, CVV, etc.
- Secure key management through environment variables

## 🌐 API Integration

### Webpay Plus Integration
```java
// Real API calls to Transbank
POST https://webpay3g.transbank.cl/rswebpaytransaction/api/webpay/v1.0/transactions
```

### MercadoPago Integration
```java
// Preference creation
POST https://api.mercadopago.com/checkout/preferences

// Payment processing
GET https://api.mercadopago.com/v1/payments/search
```

## 📊 Monitoring & Health Checks

### Actuator Endpoints
- `GET /actuator/health` - Service health status
- `GET /actuator/info` - Service information
- `GET /actuator/metrics` - Performance metrics

### Circuit Breaker Monitoring
- Automatic fallback to simulation mode
- 50% failure rate threshold
- 30-second open state timeout

## 🚀 Deployment Steps

### 1. Environment Setup
```bash
export JWT_SECRET="your-production-jwt-secret"
export WEBPAY_API_KEY="your-webpay-key"
export MERCADOPAGO_ACCESS_TOKEN="your-mp-token"
export SERVER_SSL_ENABLED=true
```

### 2. Database Setup
```sql
CREATE DATABASE quickbite_payment_db;
-- Tables auto-created by Hibernate
```

### 3. SSL Certificate Setup
```bash
# Generate keystore (for development)
keytool -genkeypair -alias quickbite-payment -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 3650
```

### 4. Service Deployment
```bash
# Using Docker Compose
docker-compose up -d

# Or using Maven
mvn spring-boot:run
```

## 🔍 Testing

### Health Check
```bash
curl http://localhost:8082/actuator/health
```

### Payment Processing Test
```bash
curl -X POST http://localhost:8082/api/payments/process \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "amount": 15000,
    "currency": "CLP",
    "paymentMethod": "WEBPAY"
  }'
```

## 📝 Troubleshooting

### Common Issues
1. **JWT Token Invalid**: Check JWT_SECRET configuration
2. **Payment Gateway Error**: Verify API keys and environment
3. **Database Connection**: Ensure MySQL is running and accessible
4. **SSL Certificate Issues**: Check keystore path and password

### Log Analysis
```bash
# View application logs
docker-compose logs -f payment-service | grep ERROR

# Monitor circuit breaker events
grep "CircuitBreaker" application.log
```

## 🔄 Next Steps

The payment service is ready for integration with:
- **Order Service** (already integrated)
- **Kitchen Service** (pending)
- **User Management Service** (pending)
- **Notification Service** (pending)

## 📞 Support

For deployment issues:
1. Check environment variables
2. Verify database connectivity
3. Review application logs
4. Validate SSL certificates

---

**Status**: ✅ Production Ready  
**Last Updated**: April 25, 2026  
**Version**: 1.0.0
