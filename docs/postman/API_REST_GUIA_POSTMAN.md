# 📡 Guía REST API - Sistema de Pedidos de Restaurante
## Guía de Referencia para Postman

**Versión:** 1.0.0  
**Base URL:** `http://localhost:8080`  
**Última actualización:** Febrero 2026

---

## 📋 Tabla Resumen de Endpoints

| # | Método | Endpoint | Descripción | Auth Requerida | Códigos de Estado |
|---|--------|----------|-------------|----------------|-------------------|
| 1 | `GET` | `/menu` | Obtener menú activo | ❌ No | 200, 503 |
| 2 | `POST` | `/orders` | Crear nuevo pedido | ❌ No | 201, 400, 404, 503 |
| 3 | `GET` | `/orders/{id}` | Obtener pedido por ID | ❌ No | 200, 404 |
| 4 | `GET` | `/orders` | Listar todos los pedidos | ❌ No | 200 |
| 5 | `GET` | `/orders?status=...` | Filtrar pedidos (cocina) | ✅ Sí | 200, 400, 401 |
| 6 | `PATCH` | `/orders/{id}/status` | Actualizar estado | ✅ Sí | 200, 400, 401, 404 |
| 7 | `DELETE` | `/orders/{id}` | Eliminar un pedido | ✅ Sí | 204, 401, 404 |
| 8 | `DELETE` | `/orders` | Eliminar todos los pedidos | ✅ Sí | 204, 401 |

---

## 🔐 Autenticación

### Endpoints Públicos (No requieren autenticación)
- ✅ `GET /menu`
- ✅ `POST /orders`
- ✅ `GET /orders/{id}`
- ✅ `GET /orders` (sin parámetros)

### Endpoints Protegidos (Requieren Kitchen Token)
- 🔒 `GET /orders?status=...` (con filtro de estado)
- 🔒 `PATCH /orders/{id}/status`
- 🔒 `DELETE /orders/{id}`
- 🔒 `DELETE /orders`

**Header Requerido:**
```
X-Kitchen-Token: cocina123
```

**Respuesta en caso de falta de autenticación:**
```json
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "timestamp": "2026-02-24T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Kitchen authentication required"
}
```

---

## 📚 Detalle de Endpoints

### 1️⃣ GET /menu - Obtener Menú Activo

**Descripción:** Retorna todos los productos disponibles para ordenar.

**Request:**
```http
GET http://localhost:8080/menu
```

**Headers:**
```
(ninguno requerido)
```

#### ✅ Response 200 OK - Éxito

```json
[
  {
    "id": 1,
    "name": "Empanadas criollas",
    "description": "Empanadas de carne con salsa casera.",
    "price": 450,
    "category": "entradas",
    "imageUrl": "https://images.unsplash.com/photo-1603360946369-dc9bb6258143?w=400",
    "isActive": true
  },
  {
    "id": 2,
    "name": "Provoleta a la parrilla",
    "description": "Queso provolone gratinado con oregano.",
    "price": 680,
    "category": "entradas",
    "imageUrl": "https://images.unsplash.com/photo-1618164436241-4473940d1f5c?w=400",
    "isActive": true
  },
  {
    "id": 5,
    "name": "Bife de chorizo",
    "description": "Corte premium con papas rusticas.",
    "price": 1850,
    "category": "principales",
    "imageUrl": "https://images.unsplash.com/photo-1558030006-450675393462?w=400",
    "isActive": true
  }
]
```

#### ❌ Response 503 Service Unavailable - Base de datos no disponible

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Database service is temporarily unavailable"
}
```

**Códigos de Estado Posibles:**
- ✅ `200 OK` - Menú obtenido correctamente
- ❌ `503 Service Unavailable` - Error de base de datos

---

### 2️⃣ POST /orders - Crear Nuevo Pedido

**Descripción:** Crea un nuevo pedido con los items especificados. Publica un evento `order.placed` a RabbitMQ.

**Request:**
```http
POST http://localhost:8080/orders
Content-Type: application/json
```

**Body:**
```json
{
  "tableId": 5,
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "note": "Sin cebolla"
    },
    {
      "productId": 3,
      "quantity": 1,
      "note": null
    }
  ]
}
```

**Validaciones del Request:**
- `tableId`: Número entero positivo entre 1 y 20
- `items`: Array con al menos 1 elemento
- `items[].productId`: Número entero positivo (debe existir en la base de datos)
- `items[].quantity`: Número entero positivo (mayor a 0)
- `items[].note`: String opcional (máximo 255 caracteres)

#### ✅ Response 201 Created - Pedido creado exitosamente

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "tableId": 5,
  "status": "PENDING",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 2,
      "note": "Sin cebolla"
    },
    {
      "id": 2,
      "productId": 3,
      "quantity": 1,
      "note": null
    }
  ],
  "createdAt": "2026-02-24T10:30:00",
  "updatedAt": "2026-02-24T10:30:00"
}
```

#### ❌ Response 400 Bad Request - Validación fallida

**Ejemplo 1: tableId inválido**
```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Table ID must be positive"
}
```

**Ejemplo 2: Items vacío**
```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Order must contain at least one item"
}
```

**Ejemplo 3: Cantidad inválida**
```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Quantity must be positive"
}
```

#### ❌ Response 404 Not Found - Producto no existe

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 999"
}
```

#### ❌ Response 503 Service Unavailable - RabbitMQ no disponible

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Message broker is temporarily unavailable"
}
```

**Códigos de Estado Posibles:**
- ✅ `201 Created` - Pedido creado correctamente
- ❌ `400 Bad Request` - Datos de entrada inválidos
- ❌ `404 Not Found` - Producto no encontrado o inactivo
- ❌ `503 Service Unavailable` - Error de base de datos o RabbitMQ

---

### 3️⃣ GET /orders/{id} - Obtener Pedido por ID

**Descripción:** Retorna los detalles completos de un pedido específico.

**Request:**
```http
GET http://localhost:8080/orders/550e8400-e29b-41d4-a716-446655440000
```

**Path Parameters:**
- `id` (UUID, requerido): Identificador único del pedido

**Headers:**
```
(ninguno requerido)
```

#### ✅ Response 200 OK - Pedido encontrado

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "tableId": 5,
  "status": "IN_PREPARATION",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 2,
      "note": "Sin cebolla"
    },
    {
      "id": 2,
      "productId": 3,
      "quantity": 1,
      "note": null
    }
  ],
  "createdAt": "2026-02-24T10:30:00",
  "updatedAt": "2026-02-24T10:35:00"
}
```

**Estados posibles del pedido:**
- `PENDING` - Pedido recién creado, pendiente de preparación
- `IN_PREPARATION` - En proceso de preparación en cocina
- `READY` - Listo para servir

#### ❌ Response 404 Not Found - Pedido no existe

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Códigos de Estado Posibles:**
- ✅ `200 OK` - Pedido encontrado
- ❌ `404 Not Found` - Pedido no existe

---

### 4️⃣ GET /orders - Listar Todos los Pedidos

**Descripción:** Retorna todos los pedidos sin filtro (acceso público).

**Request:**
```http
GET http://localhost:8080/orders
```

**Headers:**
```
(ninguno requerido)
```

#### ✅ Response 200 OK - Lista de pedidos

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "tableId": 5,
    "status": "PENDING",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "quantity": 2,
        "note": "Sin cebolla"
      }
    ],
    "createdAt": "2026-02-24T10:30:00",
    "updatedAt": "2026-02-24T10:30:00"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "tableId": 3,
    "status": "IN_PREPARATION",
    "items": [
      {
        "id": 2,
        "productId": 2,
        "quantity": 1,
        "note": null
      }
    ],
    "createdAt": "2026-02-24T10:25:00",
    "updatedAt": "2026-02-24T10:28:00"
  }
]
```

**Nota:** Si no hay pedidos, retorna array vacío `[]`

**Códigos de Estado Posibles:**
- ✅ `200 OK` - Lista obtenida correctamente (puede ser vacía)

---

### 5️⃣ GET /orders?status=... - Filtrar Pedidos por Estado (Cocina)

**Descripción:** Retorna pedidos filtrados por uno o más estados. Endpoint protegido para cocina.

**Request:**
```http
GET http://localhost:8080/orders?status=PENDING,IN_PREPARATION,READY
X-Kitchen-Token: cocina123
```

**Query Parameters:**
- `status` (string, requerido): Estados separados por coma
  - Valores válidos: `PENDING`, `IN_PREPARATION`, `READY`

**Headers:**
```
X-Kitchen-Token: cocina123
```

**Ejemplos de uso:**

```http
# Solo pedidos pendientes
GET http://localhost:8080/orders?status=PENDING
X-Kitchen-Token: cocina123

# Pedidos activos en cocina
GET http://localhost:8080/orders?status=IN_PREPARATION,READY
X-Kitchen-Token: cocina123

# Un solo estado
GET http://localhost:8080/orders?status=READY
X-Kitchen-Token: cocina123
```

#### ✅ Response 200 OK - Pedidos filtrados

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "tableId": 5,
    "status": "PENDING",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "quantity": 2,
        "note": "Sin cebolla"
      }
    ],
    "createdAt": "2026-02-24T10:30:00",
    "updatedAt": "2026-02-24T10:30:00"
  }
]
```

#### ❌ Response 400 Bad Request - Estado inválido

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid status value. Must be one of: PENDING, IN_PREPARATION, READY"
}
```

#### ❌ Response 401 Unauthorized - Token faltante o inválido

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Kitchen authentication required"
}
```

**Códigos de Estado Posibles:**
- ✅ `200 OK` - Pedidos filtrados correctamente
- ❌ `400 Bad Request` - Valor de estado inválido
- ❌ `401 Unauthorized` - Token de cocina faltante o inválido

---

### 6️⃣ PATCH /orders/{id}/status - Actualizar Estado de Pedido (Cocina)

**Descripción:** Actualiza únicamente el estado de un pedido existente. Endpoint protegido para cocina.

**Request:**
```http
PATCH http://localhost:8080/orders/550e8400-e29b-41d4-a716-446655440000/status
Content-Type: application/json
X-Kitchen-Token: cocina123
```

**Path Parameters:**
- `id` (UUID, requerido): Identificador del pedido

**Headers:**
```
Content-Type: application/json
X-Kitchen-Token: cocina123
```

**Body:**
```json
{
  "status": "IN_PREPARATION"
}
```

**Valores válidos para status:**
- `PENDING`
- `IN_PREPARATION`
- `READY`

**Transiciones recomendadas:**
```
PENDING → IN_PREPARATION → READY
```

**Ejemplos de uso:**

```http
# Marcar como en preparación
PATCH http://localhost:8080/orders/550e8400-e29b-41d4-a716-446655440000/status
Content-Type: application/json
X-Kitchen-Token: cocina123

{
  "status": "IN_PREPARATION"
}
```

```http
# Marcar como listo
PATCH http://localhost:8080/orders/550e8400-e29b-41d4-a716-446655440000/status
Content-Type: application/json
X-Kitchen-Token: cocina123

{
  "status": "READY"
}
```

#### ✅ Response 200 OK - Estado actualizado correctamente

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "tableId": 5,
  "status": "READY",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 2,
      "note": "Sin cebolla"
    }
  ],
  "createdAt": "2026-02-24T10:30:00",
  "updatedAt": "2026-02-24T10:45:00"
}
```

**Nota:** El campo `updatedAt` se actualiza automáticamente.

#### ❌ Response 400 Bad Request - Estado inválido

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid status value. Must be one of: PENDING, IN_PREPARATION, READY"
}
```

#### ❌ Response 400 Bad Request - Transición inválida

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 400,
  "error": "Invalid Status Transition",
  "message": "Cannot transition from READY to PENDING"
}
```

#### ❌ Response 401 Unauthorized - Token faltante o inválido

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Kitchen authentication required"
}
```

#### ❌ Response 404 Not Found - Pedido no existe

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Códigos de Estado Posibles:**
- ✅ `200 OK` - Estado actualizado correctamente
- ❌ `400 Bad Request` - Estado inválido o transición no permitida
- ❌ `401 Unauthorized` - Token de cocina faltante o inválido
- ❌ `404 Not Found` - Pedido no encontrado

---

### 7️⃣ DELETE /orders/{id} - Eliminar un Pedido (Cocina)

**Descripción:** Elimina (soft delete) un pedido específico. Endpoint protegido para cocina.

**Request:**
```http
DELETE http://localhost:8080/orders/550e8400-e29b-41d4-a716-446655440000
X-Kitchen-Token: cocina123
```

**Path Parameters:**
- `id` (UUID, requerido): Identificador del pedido a eliminar

**Headers:**
```
X-Kitchen-Token: cocina123
```

#### ✅ Response 204 No Content - Pedido eliminado correctamente

```
(sin cuerpo de respuesta)
```

**Nota:** El código 204 indica éxito sin contenido en el cuerpo.

#### ❌ Response 401 Unauthorized - Token faltante o inválido

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Kitchen authentication required"
}
```

#### ❌ Response 404 Not Found - Pedido no existe

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Códigos de Estado Posibles:**
- ✅ `204 No Content` - Pedido eliminado correctamente
- ❌ `401 Unauthorized` - Token de cocina faltante o inválido
- ❌ `404 Not Found` - Pedido no encontrado

---

### 8️⃣ DELETE /orders - Eliminar Todos los Pedidos (Cocina)

**Descripción:** Elimina todos los pedidos del sistema. Útil para resetear el estado. Endpoint protegido para cocina.

**Request:**
```http
DELETE http://localhost:8080/orders
X-Kitchen-Token: cocina123
```

**Headers:**
```
X-Kitchen-Token: cocina123
```

⚠️ **PRECAUCIÓN:** Esta operación elimina TODOS los pedidos sin confirmación.

#### ✅ Response 204 No Content - Todos los pedidos eliminados

```
(sin cuerpo de respuesta)
```

#### ❌ Response 401 Unauthorized - Token faltante o inválido

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Kitchen authentication required"
}
```

**Códigos de Estado Posibles:**
- ✅ `204 No Content` - Todos los pedidos eliminados correctamente
- ❌ `401 Unauthorized` - Token de cocina faltante o inválido

---

## 📊 Resumen de Códigos de Estado HTTP

### Códigos de Éxito (2xx)

| Código | Significado | Uso en la API |
|--------|-------------|---------------|
| `200 OK` | Operación exitosa con contenido | GET, PATCH |
| `201 Created` | Recurso creado exitosamente | POST /orders |
| `204 No Content` | Operación exitosa sin contenido | DELETE |

### Códigos de Error del Cliente (4xx)

| Código | Significado | Uso en la API |
|--------|-------------|---------------|
| `400 Bad Request` | Datos de entrada inválidos | Validaciones, estados inválidos |
| `401 Unauthorized` | Autenticación requerida o inválida | Token de cocina faltante/inválido |
| `404 Not Found` | Recurso no encontrado | Pedido o producto no existe |

### Códigos de Error del Servidor (5xx)

| Código | Significado | Uso en la API |
|--------|-------------|---------------|
| `500 Internal Server Error` | Error inesperado del servidor | Excepciones no controladas |
| `503 Service Unavailable` | Servicio temporalmente no disponible | Base de datos o RabbitMQ caídos |

---

## 🧪 Colección de Postman - Casos de Prueba

### Test Suite 1: Flujo Completo de Pedido (Happy Path)

#### 1. Obtener menú disponible
```http
GET http://localhost:8080/menu
```
**Resultado esperado:** `200 OK` con lista de productos

#### 2. Crear pedido para mesa 5
```http
POST http://localhost:8080/orders
Content-Type: application/json

{
  "tableId": 5,
  "items": [
    {"productId": 1, "quantity": 2, "note": "Sin cebolla"},
    {"productId": 5, "quantity": 1}
  ]
}
```
**Resultado esperado:** `201 Created` con el pedido creado  
**Acción:** Guardar el `id` del pedido en variable `{{order_id}}`

#### 3. Consultar el pedido creado
```http
GET http://localhost:8080/orders/{{order_id}}
```
**Resultado esperado:** `200 OK` con status `PENDING`

#### 4. Cocina cambia a "En Preparación"
```http
PATCH http://localhost:8080/orders/{{order_id}}/status
Content-Type: application/json
X-Kitchen-Token: cocina123

{
  "status": "IN_PREPARATION"
}
```
**Resultado esperado:** `200 OK` con status `IN_PREPARATION`

#### 5. Cocina marca como "Listo"
```http
PATCH http://localhost:8080/orders/{{order_id}}/status
Content-Type: application/json
X-Kitchen-Token: cocina123

{
  "status": "READY"
}
```
**Resultado esperado:** `200 OK` con status `READY`

#### 6. Listar pedidos listos
```http
GET http://localhost:8080/orders?status=READY
X-Kitchen-Token: cocina123
```
**Resultado esperado:** `200 OK` con el pedido en la lista

---

### Test Suite 2: Validaciones y Errores

#### Test 2.1: Crear pedido con tableId inválido
```http
POST http://localhost:8080/orders
Content-Type: application/json

{
  "tableId": -1,
  "items": [
    {"productId": 1, "quantity": 2}
  ]
}
```
**Resultado esperado:** `400 Bad Request`

#### Test 2.2: Crear pedido sin items
```http
POST http://localhost:8080/orders
Content-Type: application/json

{
  "tableId": 5,
  "items": []
}
```
**Resultado esperado:** `400 Bad Request`

#### Test 2.3: Crear pedido con producto inexistente
```http
POST http://localhost:8080/orders
Content-Type: application/json

{
  "tableId": 5,
  "items": [
    {"productId": 9999, "quantity": 1}
  ]
}
```
**Resultado esperado:** `404 Not Found`

#### Test 2.4: Obtener pedido inexistente
```http
GET http://localhost:8080/orders/00000000-0000-0000-0000-000000000000
```
**Resultado esperado:** `404 Not Found`

#### Test 2.5: Actualizar estado sin token
```http
PATCH http://localhost:8080/orders/{{order_id}}/status
Content-Type: application/json

{
  "status": "READY"
}
```
**Resultado esperado:** `401 Unauthorized`

#### Test 2.6: Actualizar con token inválido
```http
PATCH http://localhost:8080/orders/{{order_id}}/status
Content-Type: application/json
X-Kitchen-Token: token_incorrecto

{
  "status": "READY"
}
```
**Resultado esperado:** `401 Unauthorized`

#### Test 2.7: Actualizar con estado inválido
```http
PATCH http://localhost:8080/orders/{{order_id}}/status
Content-Type: application/json
X-Kitchen-Token: cocina123

{
  "status": "INVALID_STATUS"
}
```
**Resultado esperado:** `400 Bad Request`

---

### Test Suite 3: Operaciones de Cocina

#### Test 3.1: Filtrar solo pedidos pendientes
```http
GET http://localhost:8080/orders?status=PENDING
X-Kitchen-Token: cocina123
```
**Resultado esperado:** `200 OK` con pedidos en estado PENDING

#### Test 3.2: Filtrar múltiples estados
```http
GET http://localhost:8080/orders?status=PENDING,IN_PREPARATION
X-Kitchen-Token: cocina123
```
**Resultado esperado:** `200 OK` con pedidos en ambos estados

#### Test 3.3: Eliminar un pedido específico
```http
DELETE http://localhost:8080/orders/{{order_id}}
X-Kitchen-Token: cocina123
```
**Resultado esperado:** `204 No Content`

#### Test 3.4: Verificar que el pedido fue eliminado
```http
GET http://localhost:8080/orders/{{order_id}}
```
**Resultado esperado:** `404 Not Found`

---

## 🔧 Configuración de Postman

### Variables de Entorno

Crear un entorno llamado "Restaurant Local" con estas variables:

```json
{
  "base_url": "http://localhost:8080",
  "kitchen_token": "cocina123",
  "order_id": "",
  "product_id_empanadas": "1",
  "product_id_bife": "5",
  "table_number": "5"
}
```

### Scripts Pre-request útiles

#### Generar tableId aleatorio:
```javascript
pm.environment.set("random_table", Math.floor(Math.random() * 12) + 1);
```

#### Timestamp actual:
```javascript
pm.environment.set("timestamp", new Date().toISOString());
```

### Scripts de Test útiles

#### Guardar order_id de la respuesta:
```javascript
if (pm.response.code === 201) {
    const response = pm.response.json();
    pm.environment.set("order_id", response.id);
    console.log("Order ID saved:", response.id);
}
```

#### Validar status code:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});
```

#### Validar estructura de respuesta:
```javascript
pm.test("Response has required fields", function () {
    const response = pm.response.json();
    pm.expect(response).to.have.property("id");
    pm.expect(response).to.have.property("tableId");
    pm.expect(response).to.have.property("status");
    pm.expect(response).to.have.property("items");
});
```

---

## 📝 Formato de Respuestas de Error

Todas las respuestas de error siguen esta estructura consistente:

```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción específica del error"
}
```

**Campos:**
- `timestamp` (string, ISO 8601): Momento en que ocurrió el error
- `status` (number): Código de estado HTTP
- `error` (string): Nombre del error HTTP
- `message` (string): Descripción detallada del problema

---

## 🎯 Mejores Prácticas para Testing con Postman

### 1. Orden de Ejecución de Tests
```
1. GET /menu (validar que hay productos)
2. POST /orders (crear pedido y guardar ID)
3. GET /orders/{id} (verificar creación)
4. PATCH /orders/{id}/status (actualizar estado)
5. GET /orders?status=... (verificar filtrado)
6. DELETE /orders/{id} (limpiar)
```

### 2. Verificaciones Recomendadas

Para cada request, verificar:
- ✅ Status code correcto
- ✅ Headers de respuesta (`Content-Type: application/json`)
- ✅ Estructura del JSON de respuesta
- ✅ Tipos de datos (UUID, números, strings)
- ✅ Valores dentro de rangos esperados

### 3. Headers Importantes

**Para requests con body:**
```
Content-Type: application/json
```

**Para endpoints protegidos:**
```
X-Kitchen-Token: cocina123
```

### 4. Manejo de IDs

- Los `order_id` son UUIDs v4 (ej: `550e8400-e29b-41d4-a716-446655440000`)
- Los `product_id` son números enteros (ej: `1`, `5`, `8`)
- Guardar IDs en variables de entorno para reutilización

---

## 🚨 Troubleshooting

### Error: "Connection refused" o timeout

**Causa:** El servidor no está ejecutándose.

**Solución:**
```bash
docker compose -f infrastructure/docker/docker-compose.yml ps
# Verificar que order-service está "Up"
```

### Error 401 en todos los endpoints protegidos

**Causa:** Token de cocina incorrecto o header mal formado.

**Solución:**
```
Verificar header exacto:
X-Kitchen-Token: cocina123
```

### Error 503 al crear pedidos

**Causa:** RabbitMQ o PostgreSQL no disponibles.

**Solución:**
```bash
docker compose -f infrastructure/docker/docker-compose.yml logs order-service
docker compose -f infrastructure/docker/docker-compose.yml logs rabbitmq
docker compose -f infrastructure/docker/docker-compose.yml logs postgres
```

### Response vacío en GET /menu

**Causa:** Base de datos sin datos iniciales.

**Solución:**
```bash
# Verificar que Flyway ejecutó las migraciones
docker logs restaurant-order-service | grep Flyway
```

---

## 📚 Referencias Adicionales

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs
- **RabbitMQ Admin:** http://localhost:15672 (guest/guest)
- **Documentación completa:** `/docs/GUIA_ENDPOINTS_Y_DB.md`

---

## 📋 Checklist de Testing

- [ ] GET /menu retorna productos activos
- [ ] POST /orders crea pedido con status PENDING
- [ ] POST /orders valida tableId positivo
- [ ] POST /orders valida items no vacíos
- [ ] POST /orders valida productId existente
- [ ] GET /orders/{id} retorna pedido existente
- [ ] GET /orders/{id} retorna 404 para ID inexistente
- [ ] GET /orders lista todos los pedidos
- [ ] GET /orders?status filtra correctamente
- [ ] GET /orders?status requiere token de cocina
- [ ] PATCH /orders/{id}/status actualiza estado
- [ ] PATCH /orders/{id}/status requiere token de cocina
- [ ] PATCH /orders/{id}/status valida transiciones
- [ ] DELETE /orders/{id} elimina pedido
- [ ] DELETE /orders/{id} requiere token de cocina
- [ ] DELETE /orders elimina todos los pedidos
- [ ] Todos los errores retornan estructura consistente
- [ ] timestamps se actualizan correctamente

---

**Documento generado:** Febrero 2026  
**Versión API:** 1.0.0  
**Mantenido por:** Equipo de Desarrollo
