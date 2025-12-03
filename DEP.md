Documento de Especificación del Proyecto: App de Gestión para Electricistas

1. Visión General del Producto

Objetivo: Desarrollar una aplicación móvil vertical (SaaS) diseñada para electricistas independientes y pequeñas cuadrillas (PyMEs). La app resuelve la desorganización administrativa, la pérdida de evidencia de trabajos realizados (fotos de tableros, cableados) y la gestión de clientes y presupuestos.

Propuesta de Valor:

Orden: Centralizar toda la información de la obra en un solo lugar.

Evidencia: Registro cronológico inmutable para protección ante reclamos.

Profesionalismo: Generación de reportes y presupuestos que elevan la imagen del profesional.

2. Arquitectura de Navegación y UI

A. Pantalla Principal (Home)

El centro de comando del electricista.

Layout: LazyGrid con scroll vertical.

Componente Principal: Tarjeta de Obra (Card).

Visualización: Nombre del Cliente, Título de la Obra, Fecha de Creación.

Indicadores: Estado (Presupuestado / En Proceso / En Pausa / Finalizado).

Funcionalidad de Archivado:

Las obras no se borran, se archivan.

Botón inferior "Ver Obras Archivadas" para limpiar la vista principal sin perder datos históricos.

B. Detalle de Obra (El Núcleo)

Al ingresar a una tarjeta, se accede al detalle con cabecera informativa (Nombre, Descripción, Estado) y navegación por Pestañas (Tabs):

Tab 1: Registro (Bitácora de Obra)

Función: Feed cronológico de avances.

Input: Foto (Cámara/Galería) + Descripción de texto.

Regla de Negocio Crítica: La fecha del registro es inmutable y automática (basada en servidor/metadatos). El usuario no puede alterar la fecha para evitar fraudes en garantías.

UX: Scroll de tarjetas cronológicas (Timeline).

Tab 2: Archivos (Documentación)

Función: Repositorio de documentos estáticos.

Tipos de Archivo:

Imágenes: Tickets de compra, remitos, fotos de materiales.

Documentos: PDFs (Planos, Certificados DCI, Normativas).

Visualización: Grilla o Lista de miniaturas con nombres de archivo.

Tab 3: Tareas (Gestión)

Función: Lista de Checklists o To-Dos pendientes para la obra (ej: "Comprar cables", "Ranurar pared", "Amurar caja").

Tab 4: Presupuesto (Económico)

Función: Generación y visualización del presupuesto asociado a la obra. Capacidad de exportar a PDF para enviar al cliente.

C. Módulo de Clientes (Barra Inferior)

Listado: Lista de clientes con buscador superior (Search Bar).

Detalle de Cliente:

Datos de contacto (Teléfono, Dirección, CUIL/CUIT).

Historial: Lista de todas las obras asociadas a este cliente (Activas y Archivadas).

D. Configuración y Ajustes

Modo Oscuro / Claro (Dark/Light Mode).

Configuración del Perfil Profesional (Logo, Nombre, Datos para el membrete del PDF).

3. Estrategia Técnica (Firebase & Backend)

Estructura de Datos (Escalabilidad)

Para permitir la futura expansión a "Equipos de Trabajo", la base de datos no se centra en el User, sino en la Organización.

Modelo: Organization -> Projects (Obras).

Permisos: El usuario "Dueño" paga la suscripción y puede invitar "Miembros" a su organización para que vean/editen las mismas obras.

Almacenamiento (Storage Strategy)

Dado que el costo principal será el almacenamiento, se aplican reglas estrictas de optimización en el cliente (App).

Compresión Obligatoria:

Imágenes (Bitácora/Tickets): Se comprimen en el dispositivo antes de subir.

Target: JPG 80% calidad, max width 1280px.

Peso objetivo: ~150KB - 200KB por foto.

Videos: Limitados por duración (ej. 30 seg) y resolución (720p).

Manejo de Archivos Pesados (PDFs):

Límite duro por archivo (ej. 20MB) para evitar saturación con planos crudos de CAD.

Offline First:

La app debe permitir crear registros y sacar fotos sin conexión (sótanos, obras sin señal).

Sincronización automática (background sync) cuando se recupera la conectividad.

4. Modelo de Negocio y Monetización (Tiered Pricing)

El modelo es Freemium / Suscripción Mensual, basado en almacenamiento y funcionalidades de equipo.

Nivel 1: Plan "Chispa" (Gratuito / Prueba)

Objetivo: Adquisición de usuarios.

Límites:

Máximo 2 Obras activas.

Almacenamiento: 200 MB.

Sin exportación de PDF profesional (marca de agua).

Nivel 2: Plan "Oficial" (Independiente) - ~$10 USD/mes

Target: Electricista que trabaja solo o con ayudante informal.

Beneficios:

Obras ilimitadas.

Almacenamiento: 3 GB a 5 GB (Suficiente para años de historial con compresión).

Generación de PDFs profesionales.

Soporte para videos cortos en bitácora.

Nivel 3: Plan "Cuadrilla" (PyME) - ~$25-30 USD/mes (Futuro)

Target: Pequeña empresa con 1 jefe y 2-3 empleados.

Beneficios:

Multi-usuario: El jefe ve lo que suben los empleados en tiempo real.

Almacenamiento: 20 GB a 50 GB.

Permisos avanzados (quién puede borrar, quién puede editar).

Reportes de rendimiento por empleado.

5. Hoja de Ruta (Roadmap) - Próximos Pasos

Fase 1 (MVP Actual):

Consolidar Home, Creación de Obras y Pestaña Registro (Bitácora).

Implementar compresión de imágenes y lógica Offline.

Integrar Firebase Auth y Firestore básico.

Fase 2 (Gestión):

Pestaña Archivos (Subida de PDF) y Pestaña Tareas.

Módulo Clientes.

Sistema de Archivado de obras.

Fase 3 (Monetización):

Generador de PDF (Presupuestos).

Implementación de límites de almacenamiento (Lógica de conteo de bytes).

Integración de pasarela de pagos / Suscripciones.

Fase 4 (Expansión):

Funcionalidad "Equipo" (Invitar usuarios).

Soporte de Video.