# Implementación de JWT en Haversack API

## Descripción

Se ha implementado autenticación JWT (JSON Web Tokens) en el proyecto Haversack para proteger las rutas de la API.

## Componentes Agregados

### 1. Dependencias (pom.xml)
- `jjwt-api` (0.12.6)
- `jjwt-impl` (0.12.6)
- `jjwt-jackson` (0.12.6)

### 2. Clases Nuevas

#### JwtUtil.java
Utilidad para generar y validar tokens JWT. Incluye:
- Generación de tokens con información del usuario (email, userId, role)
- Validación de tokens
- Extracción de información del token
- Configuración de expiración (24 horas por defecto)

#### JwtAuthenticationFilter.java
Filtro que intercepta todas las peticiones HTTP para validar el token JWT en el header `Authorization`.

#### CustomUserDetailsService.java
Servicio que carga los detalles del usuario para la autenticación de Spring Security.

#### AuthResponseDTO.java
DTO para la respuesta de autenticación que incluye el token JWT.

### 3. Configuraciones

#### SecurityConfig.java
- Configurado para usar autenticación JWT
- Política de sesiones: STATELESS
- Rutas públicas: `/api/users/login`, `/api/users/register`, `/h2-console/**`, `/api/products/**`
- Todas las demás rutas requieren autenticación

#### application.properties
```properties
jwt.secret=haversack-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long-and-secure
jwt.expiration=86400000  # 24 horas en milisegundos
```

## Uso de la API

### 1. Registro de Usuario
```bash
POST /api/users/register
Content-Type: application/json

{
  "email": "usuario@example.com",
  "password": "password123",
  "confirmPassword": "password123",
  "nombre": "Juan",
  "apellido": "Pérez",
  "usuario": "juanp",
  "name": "Juan Pérez",
  "address": "Calle Falsa 123",
  "phone": "1234567890"
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
  "user": {
    "id": 1,
    "email": "usuario@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "role": "USER"
  }
}
```

### 3. Usar el Token en Peticiones Protegidas
Para acceder a rutas protegidas, incluye el token en el header `Authorization`:

```bash
GET /api/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Ejemplo con curl:**
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

**Ejemplo con JavaScript (fetch):**
```javascript
fetch('http://localhost:8080/api/users', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

## Rutas de la API

### Rutas Públicas (No requieren token)
- `POST /api/users/register` - Registro de usuarios
- `POST /api/users/login` - Login de usuarios
- `GET /api/products/**` - Consulta de productos (opcional, puede cambiarse)
- `GET /h2-console/**` - Consola H2 (solo desarrollo)

### Rutas Protegidas (Requieren token JWT)
- `GET /api/users` - Obtener todos los usuarios
- `GET /api/users/{id}` - Obtener usuario por ID
- `GET /api/users/email/{email}` - Obtener usuario por email
- `PUT /api/users/{id}` - Actualizar usuario
- `DELETE /api/users/{id}` - Eliminar usuario
- `POST /api/cart/**` - Operaciones del carrito
- `POST /api/orders/**` - Operaciones de órdenes

## Configuración de Seguridad

### Cambiar la Clave Secreta
⚠️ **IMPORTANTE:** Antes de desplegar en producción, cambia la clave secreta en `application.properties`:

```properties
jwt.secret=TU_CLAVE_SECRETA_MUY_SEGURA_DE_AL_MENOS_256_BITS
```

### Cambiar el Tiempo de Expiración
El token expira en 24 horas por defecto. Para cambiar:

```properties
jwt.expiration=3600000  # 1 hora en milisegundos
```

## Manejo de Errores

### Token Inválido o Expirado
Si el token es inválido o ha expirado, la API devolverá un error 401 (Unauthorized).

### Sin Token
Si no se proporciona el token en una ruta protegida, la API devolverá un error 401 (Unauthorized).

## Testing con Postman

1. **Login**: Realiza una petición POST a `/api/users/login` y guarda el token de la respuesta
2. **Usar Token**: En las siguientes peticiones, ve a la pestaña "Authorization" → Tipo "Bearer Token" → Pega el token
3. Alternativamente, agrega el header manualmente: `Authorization: Bearer TU_TOKEN`

## Notas de Desarrollo

- Los tokens JWT contienen: email del usuario, userId y role
- El token se valida en cada petición a través del `JwtAuthenticationFilter`
- La información del usuario se carga automáticamente en el contexto de seguridad de Spring
- CORS está configurado para permitir el header `Authorization`

## Próximos Pasos Recomendados

1. Implementar refresh tokens para renovar tokens expirados
2. Agregar blacklist de tokens para logout
3. Implementar rate limiting para prevenir ataques de fuerza bruta
4. Agregar validación adicional de roles para diferentes endpoints
5. Configurar HTTPS en producción
