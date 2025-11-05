# Sistema de Variantes de Producto - Documentación

## 📋 Resumen de Cambios

Se implementó un sistema completo de **variantes de producto** que permite manejar stock por color, talla, y otros atributos de manera profesional.

## 🏗️ Arquitectura

### Modelos Principales

#### ProductVariant
```java
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    private Long id;
    private Product product;      // Relación ManyToOne
    private String sku;           // Código único (ej: "MOCHILA-001-NEGRO")
    private String color;         // Color de la variante
    private String size;          // Talla (opcional: S, M, L, XL)
    private Integer stock;        // Stock específico de esta variante
    private Double priceModifier; // Modificador de precio (+/- del precio base)
    private String imageUrl;      // Imagen específica de la variante
    private Boolean available;    // Si está disponible para venta
}
```

#### Product (Actualizado)
- Se eliminaron campos: `stock`, `quantity`, `colores`
- Se agregó relación `@OneToMany` con ProductVariant
- Métodos calculados:
  - `getTotalStock()`: Suma el stock de todas las variantes
  - `getAvailableColors()`: Lista colores únicos disponibles
  - `getMinPrice()` / `getMaxPrice()`: Rango de precios de variantes

### DTOs

#### ProductDTO
```json
{
  "id": "1",
  "name": "Mochila Urbana",
  "price": 15000.0,
  "minPrice": 15000.0,
  "maxPrice": 15000.0,
  "totalStock": 15,
  "colores": ["Negro", "Rojo", "Azul"],
  "variants": [
    {
      "id": 1,
      "sku": "MOCHILA-001-NEGRO",
      "color": "Negro",
      "stock": 5,
      "finalPrice": 15000.0,
      "imageUrl": "/img/mochila-negro.jpg",
      "available": true
    },
    {
      "id": 2,
      "sku": "MOCHILA-001-ROJO",
      "color": "Rojo",
      "stock": 5,
      "finalPrice": 15000.0,
      "imageUrl": "/img/mochila-rojo.jpg",
      "available": true
    }
  ]
}
```

## 🔄 Migración de Datos

El `DataInitializer` ahora:
1. Lee el array `colores` de cada producto en `data.json`
2. Calcula el stock total y lo divide entre los colores
3. Crea una variante por cada color con SKU único
4. Asigna imágenes específicas a cada variante (si están disponibles)

**Ejemplo:**
```json
{
  "id": "1",
  "name": "Mochila Urbana",
  "stock": "15",
  "colores": ["Negro", "Rojo", "Azul"]
}
```

**Resultado:**
- 3 variantes creadas
- Stock distribuido: 5 + 5 + 5 = 15 total
- SKUs generados: `1-NEGRO`, `1-ROJO`, `1-AZUL`

## 📡 API Endpoints

### GET /api/products
Devuelve todos los productos con sus variantes incluidas:
```json
[
  {
    "id": "1",
    "name": "Mochila Urbana",
    "totalStock": 15,
    "variants": [...]
  }
]
```

### GET /api/products/{id}
Devuelve un producto específico con todas sus variantes.

## 🎨 Frontend Integration

### Cambios Necesarios en el Frontend

1. **ProductDetail.jsx** debe mostrar:
   - Selector de color (radio buttons o botones)
   - Stock disponible del color seleccionado
   - Precio de la variante seleccionada
   - Imagen de la variante seleccionada

2. **ProductCard.jsx** puede mostrar:
   - Rango de precios si hay variantes con precios diferentes
   - Indicador de colores disponibles
   - Stock total

3. **Cart** debe guardar:
   - `variantId` en lugar de solo `productId`
   - SKU de la variante seleccionada
   - Color seleccionado

### Ejemplo de Uso en Frontend

```jsx
// En ProductDetail.jsx
const [selectedVariant, setSelectedVariant] = useState(null);

useEffect(() => {
  if (product?.variants && product.variants.length > 0) {
    // Seleccionar primera variante disponible
    const firstAvailable = product.variants.find(v => v.available && v.stock > 0);
    setSelectedVariant(firstAvailable);
  }
}, [product]);

const handleColorSelect = (color) => {
  const variant = product.variants.find(v => v.color === color);
  setSelectedVariant(variant);
};

// Mostrar selector de colores
<div className="color-selector">
  {product.variants.map(variant => (
    <button
      key={variant.id}
      onClick={() => handleColorSelect(variant.color)}
      disabled={!variant.available || variant.stock === 0}
      className={selectedVariant?.id === variant.id ? 'selected' : ''}
    >
      {variant.color} ({variant.stock} disponibles)
    </button>
  ))}
</div>

// Al agregar al carrito
addToCart({
  productId: product.id,
  variantId: selectedVariant.id,
  sku: selectedVariant.sku,
  color: selectedVariant.color,
  price: selectedVariant.finalPrice,
  quantity: cantidad
});
```

## ✅ Ventajas del Sistema

1. **Stock Preciso**: Cada color tiene su propio stock
2. **Escalable**: Fácil agregar tallas u otros atributos
3. **Precios Flexibles**: Variantes pueden tener precios diferentes
4. **Imágenes Específicas**: Cada variante puede mostrar su imagen
5. **SKU Único**: Mejor control de inventario
6. **Profesional**: Estándar de la industria e-commerce

## 🔄 Compatibilidad

El sistema mantiene **compatibilidad hacia atrás**:
- `colores` en ProductDTO para frontend antiguo
- `stock` y `totalStock` calculados automáticamente
- Frontend antiguo seguirá funcionando con adaptaciones mínimas

## 🚀 Próximos Pasos

1. **Actualizar Frontend** para mostrar selector de variantes
2. **Actualizar Carrito** para guardar `variantId`
3. **Actualizar Checkout** para validar stock por variante
4. **Agregar Admin Panel** para gestionar variantes
5. **Implementar Tallas** si es necesario

## 📝 Notas Técnicas

- La relación es `@OneToMany` con `CascadeType.ALL` y `orphanRemoval = true`
- Las variantes se cargan con `FetchType.EAGER` para simplificar
- Los métodos `@Transient` calculan valores dinámicamente
- `@JsonManagedReference` y `@JsonBackReference` evitan referencias circulares en JSON

## 🧪 Testing

Para probar el sistema:
1. Borrar la base de datos anterior
2. Iniciar el backend (DataInitializer cargará variantes)
3. Verificar en logs: "Loaded X products with Y variants"
4. Probar endpoint: `GET http://localhost:8080/api/products`
5. Verificar que cada producto tenga array `variants` poblado
