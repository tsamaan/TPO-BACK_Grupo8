# Implementación de JWT en Haversack API

## Descripción

Se ha implementado autenticación JWT (JSON Web Tokens) en el proyecto Haversack para proteger las rutas de la API.

## Componentes Implementados

### 1. Dependencias (pom.xml)
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

### 2. Clases Creadas

#### `JwtUtil.java`
Utilidad para generar y validar tokens JWT.
- Genera tokens con email, userId y role
- Valida tokens y extrae información
- Expiración configurable (24 horas por defecto)

#### `JwtAuthenticationFilter.java`
Filtro que intercepta peticiones HTTP para validar tokens JWT en el header `Authorization`.

#### `CustomUserDetailsService.java`
Servicio que carga detalles del usuario para Spring Security.

#### `AuthResponseDTO.java`
DTO para respuestas de autenticación con token.

### 3. Configuración

#### `SecurityConfig.java`
- Sesiones STATELESS (sin estado en servidor)
- Rutas públicas: `/api/users/login`, `/api/users/register`
- GET en productos y categorías: público
- POST/PUT/DELETE en productos y categorías: requiere autenticación
- Rutas de carrito y órdenes: requieren autenticación
- Filtro JWT agregado antes del filtro de autenticación

#### `application.properties`
```properties
jwt.secret=haversack-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long-and-secure
jwt.expiration=86400000  # 24 horas
```

## Uso de la API

### 1. Registro
```bash
POST /api/users/register
Content-Type: application/json

{
  "email": "usuario@example.com",
  "password": "password123",
  "confirmPassword": "password123",
  "nombre": "Juan",
  "apellido": "Pérez",
  "usuario": "juanp"
}
```

### 2. Login (Obtener Token)
```bash
POST /api/users/login
Content-Type: application/json

{
  "email": "usuario@example.com",
  "password": "password123"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}
```

### 3. Usar Token en Peticiones
```bash
GET /api/users/me
Authorization: Bearer {tu_token_aqui}
```

## Endpoints

### Públicos (sin autenticación)
- `POST /api/users/register` - Registro
- `POST /api/users/login` - Login
- `GET /api/products/**` - Listar productos
- `GET /api/categories/**` - Listar categorías
- `GET /api` - Info de API

### Protegidos (requieren token)
- `GET /api/users` - Listar usuarios
- `GET /api/users/me` - Obtener usuario actual
- `GET /api/users/{id}` - Obtener usuario por ID
- `GET /api/users/validate-token` - Validar token
- `PUT /api/users/{id}` - Actualizar usuario
- `DELETE /api/users/{id}` - Eliminar usuario
- `POST/PUT/DELETE /api/products/**` - Gestión de productos
- `POST/PUT/DELETE /api/categories/**` - Gestión de categorías
- `/api/cart/**` - Operaciones de carrito
- `/api/orders/**` - Operaciones de órdenes

## Ejemplos de Uso

### JavaScript (Fetch)
```javascript
// Login
const loginResponse = await fetch('http://localhost:8080/api/users/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'usuario@example.com',
    password: 'password123'
  })
});
const { token } = await loginResponse.json();

// Usar token
const usersResponse = await fetch('http://localhost:8080/api/users/me', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const currentUser = await usersResponse.json();
```

### Curl
```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@example.com","password":"password123"}' \
  | jq -r '.token')

# Usar token
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

## Seguridad

### ⚠️ IMPORTANTE para Producción
1. **Cambiar la clave secreta** en `application.properties`
2. **Usar HTTPS** en producción
3. **Configurar tiempo de expiración** apropiado
4. **Considerar implementar refresh tokens**

### Configuración Segura
```properties
# Generar una clave segura de al menos 256 bits
jwt.secret=${JWT_SECRET:tu-clave-muy-segura-aqui}
# 1 hora = 3600000, 12 horas = 43200000, 24 horas = 86400000
jwt.expiration=3600000
```

## Manejo de Errores

- **401 Unauthorized**: Token inválido, expirado o ausente
- **403 Forbidden**: Token válido pero sin permisos
- **400 Bad Request**: Datos de login incorrectos

## Testing

### Con Postman
1. Hacer login en `/api/users/login`
2. Copiar el token de la respuesta
3. En otras peticiones: Authorization → Type: "Bearer Token" → Pegar token

### Con Thunder Client (VS Code)
1. Crear variable de entorno `token`
2. En la petición de login, agregar en "Tests":
   ```javascript
   tc.setVar("token", tc.response.json.token);
   ```
3. En otras peticiones usar: `{{token}}`

## Notas Técnicas

- **Algoritmo**: HS256 (HMAC con SHA-256)
- **Claims incluidos**: email (subject), userId, role, iat (issued at), exp (expiration)
- **Validación**: Automática en cada petición via `JwtAuthenticationFilter`
- **Contexto de seguridad**: Usuario autenticado disponible en `SecurityContextHolder`

## Estructura del Token JWT

Un token JWT consiste en tres partes separadas por puntos:
```
header.payload.signature
```

**Payload decodificado (ejemplo):**
```json
{
  "sub": "usuario@example.com",
  "userId": 1,
  "role": "USER",
  "iat": 1698765432,
  "exp": 1698851832
}
```
