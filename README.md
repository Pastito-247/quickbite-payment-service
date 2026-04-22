# QuickBite Payment Service

Microservicio de pagos para la plataforma QuickBite, parte de la arquitectura de microservicios para gestión de restaurantes de comida rápida.

## Tecnologías

- **Java 17**
- **Spring Boot 4.0.5**
- **Spring Data JPA**
- **MySQL 8.0**
- **Resilience4j** (Circuit Breaker)
- **Lombok**
- **Docker & Docker Compose**

## Patrones de Diseño Implementados

- **Repository Pattern**: Para la persistencia de datos con JPA
- **Factory Method**: Para la creación de instancias de pasarelas de pago (Webpay, MercadoPago, Wallet)
- **Circuit Breaker**: Para tolerancia a fallos en comunicación con pasarelas externas

## Funcionalidades

- Procesamiento de pagos con múltiples pasarelas (Webpay, MercadoPago, Billetera Virtual)
- Gestión de billetera virtual de usuarios
- Historial de transacciones para auditorías
- Reembolso de pagos
- Trazabilidad de pagos por ID de orden

## Endpoints API

### Pagos
- `POST /api/payments/process` - Procesar un pago
- `GET /api/payments/{id}` - Obtener pago por ID
- `GET /api/payments/order/{orderId}` - Obtener pago por orden
- `GET /api/payments/status/{status}` - Obtener pagos por estado
- `POST /api/payments/{id}/refund` - Reembolsar pago

### Billetera
- `GET /api/wallets/{userId}` - Obtener billetera de usuario
- `POST /api/wallets/{userId}` - Crear billetera para usuario
- `POST /api/wallets/{userId}/deposit` - Depositar en billetera
- `POST /api/wallets/{userId}/withdraw` - Retirar de billetera

### Transacciones
- `GET /api/transactions/payment/{paymentId}` - Obtener transacciones por pago
- `GET /api/transactions/{id}` - Obtener transacción por ID

## Ejecución Local

### Prerrequisitos
- Java 17
- Maven 3.9+
- MySQL 8.0

### Configuración de Base de Datos
Crear base de datos MySQL:
```sql
CREATE DATABASE quickbite_payment_db;
```

Configurar en `payment-service/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/quickbite_payment_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

### Ejecutar
```bash
cd payment-service
mvn spring-boot:run
```

El servicio estará disponible en `http://localhost:8082`

## Ejecución con Docker

### Construir y levantar servicios
```bash
docker-compose up -d
```

### Ver logs
```bash
docker-compose logs -f payment-service
```

### Detener servicios
```bash
docker-compose down
```

### Detener y eliminar volúmenes
```bash
docker-compose down -v
```

## Estructura del Proyecto

```
payment-service/
├── src/main/java/com/quickbite/payment_service/
│   ├── controller/          # Controladores REST
│   ├── dto/                 # Data Transfer Objects
│   ├── entity/              # Entidades JPA
│   ├── repository/          # Repositorios (Repository Pattern)
│   ├── service/             # Servicios de negocio
│   ├── factory/             # Factory Method para pasarelas
│   ├── gateway/             # Implementaciones de pasarelas
│   ├── exception/           # Excepciones personalizadas
│   └── enums/               # Enumeraciones
└── src/main/resources/
    └── application.properties
```

## Integración con Pasarelas de Pago

Las pasarelas de pago están implementadas como simulaciones. Para integración real con:

- **Webpay**: Implementar la lógica en `WebpayGateway.java`
- **MercadoPago**: Implementar la lógica en `MercadoPagoGateway.java`

## Seguridad

- Los datos sensibles de pago se manejan a través de tokenización (a implementar)
- Configuración de HTTPS/TLS recomendada para producción
- Implementación de JWT para autenticación (pendiente de integración con servicio IAM)

## Estado del Servicio

El servicio expone un health check en el puerto 8082.
