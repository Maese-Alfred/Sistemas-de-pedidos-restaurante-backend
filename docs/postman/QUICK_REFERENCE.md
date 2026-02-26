# 📋 Referencia Rápida - HTTP API

## 🔍 Quick Reference Card

### Tabla de Endpoints - Order Service (Port 8080)

| Método | Endpoint | Auth | 200 | 201 | 204 | 400 | 401 | 404 | 503 | Descripción |
|--------|----------|------|-----|-----|-----|-----|-----|-----|-----|-------------|
| `GET` | `/menu` | ❌ | ✅ | - | - | - | - | - | ✅ | Obtener menú activo |
| `POST` | `/orders` | ❌ | - | ✅ | - | ✅ | - | ✅ | ✅ | Crear pedido |
| `GET` | `/orders/{id}` | ❌ | ✅ | - | - | - | - | ✅ | - | Obtener pedido |
| `GET` | `/orders` | ❌ | ✅ | - | - | - | - | - | - | Listar todos |
| `GET` | `/orders?status=...` | 🔒 | ✅ | - | - | ✅ | ✅ | - | - | Filtrar por estado |
| `PATCH` | `/orders/{id}/status` | 🔒 | ✅ | - | - | ✅ | ✅ | ✅ | - | Actualizar estado |
| `DELETE` | `/orders/{id}` | 🔒 | - | - | ✅ | - | ✅ | ✅ | - | Eliminar pedido |
| `DELETE` | `/orders` | 🔒 | - | - | ✅ | - | ✅ | - | - | Eliminar todos |

### Tabla de Endpoints - Report Service (Port 8082)

| Método | Endpoint | Auth | 200 | 400 | Descripción |
|--------|----------|------|-----|-----|-------------|
| `GET` | `/reports?startDate=...&endDate=...` | ❌ | ✅ | ✅ | Obtener reporte por rango de fechas |

**Leyenda:**
- ✅ = Retorna este código
- ❌ = Sin autenticación
- 🔒 = Requiere `X-Kitchen-Token: cocina123`

---

## 🎯 Verbos HTTP - Uso Semántico

| Verbo HTTP | Uso Correcto | Idempotente | Body Request | Body Response |
|------------|--------------|-------------|--------------|---------------|
| **GET** | Leer/Consultar datos | ✅ Sí | ❌ No | ✅ Sí |
| **POST** | Crear nuevo recurso | ❌ No | ✅ Sí | ✅ Sí |
| **PATCH** | Actualización parcial | ❌ No | ✅ Sí | ✅ Sí |
| **DELETE** | Eliminar recurso | ✅ Sí | ❌ No | ❌ No (204) |

### Idempotencia

✅ **Idempotente** = Ejecutar múltiples veces produce el mismo resultado
- `GET /orders/{id}` → Siempre retorna el mismo pedido
- `DELETE /orders/{id}` → Primera vez elimina, siguientes retornan 404

❌ **No Idempotente** = Cada ejecución produce un resultado diferente
- `POST /orders` → Cada llamada crea un nuevo pedido con ID distinto

---

## 📊 Códigos de Estado HTTP

### Éxito (2xx)

| Código | Nombre | Cuándo usar | Body |
|--------|--------|-------------|------|
| **200** | OK | Operación exitosa con datos | ✅ Sí |
| **201** | Created | Recurso creado exitosamente | ✅ Sí |
| **204** | No Content | Operación exitosa sin datos | ❌ No |

### Error Cliente (4xx)

| Código | Nombre | Cuándo usar | Ejemplo |
|--------|--------|-------------|---------|
| **400** | Bad Request | Datos inválidos, validación fallida | tableId negativo, items vacío, fecha inválida |
| **401** | Unauthorized | Token faltante o inválido | Sin header X-Kitchen-Token |
| **404** | Not Found | Recurso no encontrado | Pedido o producto no existe |

### Error Servidor (5xx)

| Código | Nombre | Cuándo usar | Ejemplo |
|--------|--------|-------------|---------|
| **500** | Internal Server Error | Error inesperado | Exception no controlada |
| **503** | Service Unavailable | Servicio externo caído | PostgreSQL, RabbitMQ down |

---

## 🔐 Autenticación

### Header Requerido en Endpoints Protegidos

```http
X-Kitchen-Token: cocina123
```

### Endpoints por Nivel de Acceso

| Acceso | Endpoints |
|--------|-----------|
| **Público** | `GET /menu`, `POST /orders`, `GET /orders/{id}`, `GET /orders`, `GET /reports` |
| **Cocina** | `GET /orders?status=...`, `PATCH /orders/{id}/status`, `DELETE /orders/{id}`, `DELETE /orders` |

---

## 📝 Ejemplos Rápidos

**Nota:** En los JSON bodies, reemplaza los placeholders (YOUR_TABLE_ID, YOUR_PRODUCT_ID, etc.) con tus valores reales.

### Order Service (Port 8080)

#### 1. Obtener Menú
```http
GET http://localhost:8080/menu
```

#### 2. Crear Pedido
```http
POST http://localhost:8080/orders
Content-Type: application/json

{"tableId": YOUR_TABLE_ID, "items": [{"productId": YOUR_PRODUCT_ID, "quantity": YOUR_QUANTITY}]}
```
*Reemplaza YOUR_TABLE_ID, YOUR_PRODUCT_ID y YOUR_QUANTITY con valores reales (ejemplo: 5, 1, 2)*

#### 3. Consultar Pedido
```http
GET http://localhost:8080/orders/YOUR_ORDER_UUID
```
*Reemplaza YOUR_ORDER_UUID con el ID que obtuviste al crear el pedido*

#### 4. Filtrar por Estado (Cocina)
```http
GET http://localhost:8080/orders?status=PENDING,IN_PREPARATION
X-Kitchen-Token: cocina123
```

#### 5. Actualizar Estado (Cocina)
```http
PATCH http://localhost:8080/orders/YOUR_ORDER_UUID/status
Content-Type: application/json
X-Kitchen-Token: cocina123

{"status": "IN_PREPARATION"}
```
*Reemplaza YOUR_ORDER_UUID con el ID del pedido*

#### 6. Eliminar Pedido (Cocina)
```http
DELETE http://localhost:8080/orders/YOUR_ORDER_UUID
X-Kitchen-Token: cocina123
```
*Reemplaza YOUR_ORDER_UUID con el ID del pedido*

### Report Service (Port 8082)

#### 7. Obtener Reporte por Rango de Fechas
```http
GET http://localhost:8082/reports?startDate=YOUR_START_DATE&endDate=YOUR_END_DATE
```
*Reemplaza YOUR_START_DATE y YOUR_END_DATE con fechas en formato YYYY-MM-DD (ejemplo: 2024-01-01 y 2024-12-31)*

#### 8. Obtener Reporte del Mes Actual
```http
GET http://localhost:8082/reports?startDate=2024-02-01&endDate=2024-02-29
```

---

## 🔄 Estados del Pedido

```
PENDING → IN_PREPARATION → READY
```

| Estado | Descripción | Quién lo establece |
|--------|-------------|-------------------|
| `PENDING` | Recién creado | Sistema (POST /orders) |
| `IN_PREPARATION` | En cocina | Cocina (PATCH status) |
| `READY` | Listo para servir | Cocina (PATCH status) |

---

## 🧪 Validaciones

### POST /orders

| Campo | Validación | Mensaje de Error (400) |
|-------|------------|----------------------|
| `tableId` | > 0 | "Table ID must be positive" |
| `items` | length ≥ 1 | "Order must contain at least one item" |
| `items[].productId` | Existe en DB | "Product not found with id: X" (404) |
| `items[].quantity` | > 0 | "Quantity must be positive" |
| `items[].note` | ≤ 255 chars | "Note too long" |

### PATCH /orders/{id}/status

| Campo | Validación | Mensaje de Error |
|-------|------------|------------------|
| `status` | PENDING \| IN_PREPARATION \| READY | "Invalid status value" (400) |
| `id` | Existe en DB | "Order not found with id: X" (404) |

### GET /reports

| Parámetro | Validación | Mensaje de Error |
|-----------|------------|------------------|
| `startDate` | Formato YYYY-MM-DD | "Invalid date format" (400) |
| `endDate` | Formato YYYY-MM-DD | "Invalid date format" (400) |
| `startDate` | ≤ endDate | "Start date must be before end date" (400) |

---

## 📦 Estructura de Respuestas

### Pedido (OrderResponse)
```json
{
  "id": "uuid",
  "tableId": number,
  "status": "PENDING | IN_PREPARATION | READY",
  "items": [
    {
      "id": number,
      "productId": number,
      "quantity": number,
      "note": "string | null"
    }
  ],
  "createdAt": "ISO 8601 timestamp",
  "updatedAt": "ISO 8601 timestamp"
}
```

### Producto (ProductResponse)
```json
{
  "id": number,
  "name": "string",
  "description": "string",
  "price": number,
  "category": "string",
  "imageUrl": "string",
  "isActive": boolean
}
```

### Reporte (ReportResponse)
```json
{
  "totalReadyOrders": number,
  "totalRevenue": number,
  "productBreakdown": [
    {
      "productId": number,
      "productName": "string",
      "quantitySold": number,
      "totalAccumulated": number
    }
  ]
}
```

### Error (ErrorResponse)
```json
{
  "timestamp": "ISO 8601 timestamp",
  "status": number,
  "error": "string",
  "message": "string"
}
```

---

## 🎨 Headers Comunes

### Request Headers

| Header | Valor | Cuándo |
|--------|-------|--------|
| `Content-Type` | `application/json` | POST, PATCH |
| `X-Kitchen-Token` | `cocina123` | Endpoints protegidos |

### Response Headers

| Header | Valor | Siempre |
|--------|-------|---------|
| `Content-Type` | `application/json` | ✅ (excepto 204) |

---

## ⚡ Tips para Postman

### Configuración Inicial Requerida

Antes de usar la colección, debes reemplazar estos placeholders:

| Placeholder | Ejemplo | Dónde Encontrarlo |
|-------------|---------|--------------------|
| `YOUR_ORDER_SERVICE_URL` | `http://localhost:8080` | URL de tu order-service |
| `YOUR_REPORT_SERVICE_URL` | `http://localhost:8082` | URL de tu report-service |
| `YOUR_KITCHEN_TOKEN` | `cocina123` | Token configurado en tu servidor |
| `YOUR_ORDER_UUID` | `a1b2...` | ID de respuesta al crear pedido |

### Scripts de Test Útiles

**Validar status code:**
```javascript
pm.test("Status 201", () => pm.response.to.have.status(201));
```

**Validar campo existe:**
```javascript
pm.test("Has ID", () => {
    pm.expect(pm.response.json()).to.have.property('id');
});
```

**Mostrar ID en consola:**
```javascript
console.log("Order ID:", pm.response.json().id);
```

**Copiar valor automáticamente al portapapeles (requiere extensión):**
```javascript
// Muestra el ID en consola para copiarlo manualmente
var orderId = pm.response.json().id;
console.log("Copia este UUID:", orderId);
```

---

## 🐛 Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| Could not send request | Placeholder no reemplazado | Reemplaza `YOUR_ORDER_SERVICE_URL` con URL real |
| Connection refused | Servidor no ejecutándose | `docker compose up -d` |
| 401 Unauthorized | Token no configurado | Reemplaza `YOUR_KITCHEN_TOKEN` con token real |
| 404 Not Found | UUID no válido | Reemplaza `YOUR_ORDER_UUID` con UUID real |
| 400 Bad Request | Datos inválidos | Revisar validaciones del request |
| 503 Service Unavailable | DB/RabbitMQ caídos | Verificar logs de Docker |

---

## 📎 Resources

- **Order Service:** YOUR_ORDER_SERVICE_URL (ejemplo: http://localhost:8080)
- **Report Service:** YOUR_REPORT_SERVICE_URL (ejemplo: http://localhost:8082)
- **RabbitMQ:** http://localhost:15672
- **Docs:** `/docs/API_REST_GUIA_POSTMAN.md`

---

**Tip:** Antes de usar esta referencia, asegúrate de haber configurado todos los placeholders en tu colección de Postman
