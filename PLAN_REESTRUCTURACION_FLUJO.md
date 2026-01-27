# Plan de Reestructuración de Flujo y Navegación "Enchu"

## Objetivo
Implementar un nuevo flujo central de creación de "Presupuesto" antes de "Obra", con una navegación renovada (FAB central) y soporte Offline-First para borradores.

## 1. Navegación y UI Principal (MainScreen)
**Cambio:** Reemplazar la `NavigationBar` actual de 3 ítems por un `BottomAppBar` con `FloatingActionButton` (FAB) incrustado.
- **Acción:** Modificar `MainScreen.kt`.
- **Estructura Nueva:**
  - 5 espacios en la barra inferior.
  - Íconos: Home, Clientes, (FAB: Crear), [Espacio], Perfil (u otro).
  - **FAB Central:** Al hacer clic, abre un `ModalBottomSheet`.
  - **Bottom Sheet:** Opciones "Nuevo Presupuesto" y "Nueva Obra".

## 2. Nueva Entidad y Persistencia (Room)
**Cambio:** Introducir el concepto de `Presupuesto` persistente localmente antes de subir a Firestore como `Obra`.
- **Acción:**
  - Crear `PresupuestoEntity` (Room) para guardar borradores.
  - Crear `PresupuestoItemEntity` (Room) vinculado al presupuesto.
  - Crear `PresupuestoDao` y `PresupuestoRepository`.
- **Justificación:** Permite trabajar offline, guardar borradores ("Pendiente") y evita crear "Obras basura" en Firestore hasta que se aprueben.

## 3. Flujo "Nuevo Presupuesto" (Wizard)
**Paso 1: Selección de Cliente**
- Reutilizar `ClientsScreen` con un nuevo argumento `isSelectionMode: Boolean`.
- Al seleccionar, navegar al paso 2 con el `clientId`.
- Botón "Nuevo Cliente Manual" que abre un diálogo simple (Nombre + Dirección).

**Paso 2: Datos Generales**
- Pantalla `BudgetInfoScreen`.
- Muestra cliente seleccionado.
- Campos: Título, Fecha (Default Hoy), Validez.

**Paso 3: Carga de Items (Core)**
- Pantalla `BudgetItemsScreen`.
- **Reutilización:** Usar el `MaterialSearchDialog` existente para agregar items desde el catálogo local.
- **Lista:** `LazyColumn` con items agregados. Soporte para editar cantidad/precio inline.
- **Totales:** Footer "sticky" con Subtotal/Total.
- **Acciones:**
  - "Guardar Borrador": Guarda en Room (Estado PENDIENTE).
  - "Guardar y Aprobar":
    1. Crea la `Obra` en Firestore.
    2. Sube los items a la subcolección de la Obra.
    3. Borra el borrador local (o lo marca como sincronizado).
    4. Navega a `Home`.

## 4. Adaptación de Código Existente
- **Obra:** Modificar `Obra.kt` para aceptar un `presupuestoId` opcional (link de trazabilidad).
- **Home:** Debe seguir mostrando "Obras", pero podríamos añadir una pestaña de "Presupuestos Pendientes" (Borradores locales) en el futuro. Por ahora, el foco es crear Obras.

## Ejecución Inmediata
1. Crear las entidades de base de datos (Pilar fundamental).
2. Modificar la navegación principal (`MainScreen`) para incluir el FAB.
3. Conectar el flujo de pantallas.
