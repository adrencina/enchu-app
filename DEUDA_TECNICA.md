# Deuda Técnica y Pendientes de Seguridad

## 1. Reglas de Firestore - Organizaciones (Prioridad: Alta)
**Fecha:** 18 de enero de 2026
**Ubicación:** `firestore.rules` -> `match /organizations/{orgId}`

**Situación Actual:**
Se han abierto los permisos de `read` y `update` a `if isSignedIn()` para permitir que nuevos usuarios se unan a equipos mediante el flujo actual de la app (cliente escribe en array `members`).

**Riesgo:**
Cualquier usuario autenticado podría técnicamente modificar datos de cualquier organización si conoce su ID (nombre, o borrar miembros), aunque la UI no lo expone.

**Solución Futura (v2.0):**
1. Implementar Cloud Functions para la lógica de "Unirse a Equipo".
   - El cliente llama a la función `joinTeam(teamId)`.
   - La función (con privilegios de admin) valida y actualiza el array `members`.
2. O bien, refinar las reglas `.write` para usar `request.resource.data.diff` y permitir SOLO actualizaciones donde:
   - El único campo cambiado es `members`.
   - El valor añadido es únicamente `request.auth.uid`.

---
