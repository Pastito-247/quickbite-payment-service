# Guía de Contribución - GitHub Flow

Este proyecto utiliza **GitHub Flow** como modelo de branching para el desarrollo.

## Flujo de Trabajo

### 1. Rama `main`
La rama `main` siempre debe estar en un estado **desplegable**. No se debe hacer commits directos a esta rama.

### 2. Ramas de Feature
Para cada nueva funcionalidad o corrección de bug:
- Crear una rama desde `main` con el prefijo `feature/`
- Nombre descriptivo: `feature/payment-service`, `feature/add-refund`, `fix/circuit-breaker`

```bash
git checkout main
git pull origin main
git checkout -b feature/nombre-de-la-funcionalidad
```

### 3. Commits
Hacer commits frecuentes con mensajes claros siguiendo las convenciones de [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: descripción de la nueva funcionalidad
fix: descripción de la corrección
docs: actualización de documentación
style: cambios de formato
refactor: refactorización de código
test: agregar o actualizar pruebas
chore: tareas de mantenimiento
```

### 4. Pull Request
Cuando la funcionalidad esté completa:
- Crear un Pull Request desde la rama de feature hacia `main`
- El PR debe ser revisado por al menos una persona
- El PR debe pasar todos los tests y revisiones de código
- El título del PR debe seguir el formato: `feat: descripción`

### 5. Fusión y Despliegue
- Una vez aprobado el PR, fusionar a `main`
- La fusión debe ser mediante "Squash and merge" para mantener un historial limpio
- Automáticamente se despliega desde `main` a producción

## Ejemplo de Flujo de Trabajo

```bash
# 1. Actualizar main
git checkout main
git pull origin main

# 2. Crear rama de feature
git checkout -b feature/add-webpay-integration

# 3. Hacer cambios y commits
git add .
git commit -m "feat: add Webpay integration"

# 4. Push al remoto
git push origin feature/add-webpay-integration

# 5. Crear Pull Request en GitHub
# 6. Esperar revisión y aprobación
# 7. Fusionar a main
```

## Reglas Importantes

- **Nunca** hacer commits directos a `main`
- **Nunca** fusionar sin revisión de código
- **Siempre** mantener `main` en estado desplegable
- **Siempre** hacer tests antes de crear un PR
- **Siempre** escribir mensajes de commits claros y descriptivos

## Resolución de Conflictos

Si hay conflictos al fusionar:
```bash
git checkout main
git pull origin main
git checkout feature/tu-rama
git rebase main
# Resolver conflictos
git add .
git rebase --continue
git push origin feature/tu-rama --force
```

## Ramas Actuales

- `main`: Rama principal de producción
- `feature/payment-service`: Rama actual para desarrollo del microservicio de pagos
