# Informe Técnico del Proyecto: PrimerJuego2D

## 1. Arquitectura del Proyecto

El proyecto sigue una arquitectura clásica de **Game Loop** (Bucle de Juego) centralizado, típico en desarrollos 2D con Java Swing/AWT.

### Estructura General
*   **Núcleo (`main`):**
    *   `PanelJuego.java`: Es la clase la "Dueña" del estado del juego. Hereda de `JPanel` e implementa `Runnable` para gestionar el hilo principal.
    *   **Game Loop**: Implementado en el método `run()` usando el patrón "Delta Time" para asegurar una ejecución estable a **60 FPS**.
    *   **Ciclo de actualización**:
        1.  **UPDATE**: Actualiza la lógica (movimiento, colisiones, IA) de todas las entidades (`jugador.update()`, `npcs[i].update()`).
        2.  **DRAW**: Renderiza todo en pantalla (`paintComponent`).

*   **Sistema de Entidades (`entidad`):**
    *   Usa herencia y polimorfismo.
    *   `Entidad` (Clase Base): Define coordenadas (`worldx`, `worldy`), velocidad, vida y sprites.
    *   `NPC` (Abstracta): Extiende `Entidad`. Añade IA básica, rangos de detección y lógica de combate.
    *   `Jugador`, `Orco`, `Slime`, `Bat`: Implementaciones concretas.

*   **Gestión del Mundo (`tiles` / `main`):**
    *   `TileManager`: Dibuja el mapa estático.
    *   `AssetSetter`: Responsable de "popular" el mundo (poner objetos y enemigos).

---

## 2. Clases y Métodos Principales

### `main.PanelJuego`
Es el contenedor de memoria de todos los objetos vivos.
*   **Arrays de Gestión**: Mantiene arrays fijos para gestionar las entidades en memoria.
    *   `public NPC[] npcs = new NPC[100];` (Límite duro de 100 enemigos).
    *   `public Proyectil[] proyectiles = new Proyectil[100];`
    *   `public superObjeto[] objs = new superObjeto[15];`
*   **Método `update()`**: Orquesta el comportamiento de todos los elementos frame a frame.

### `main.AssetSetter`
El "Director de Escena".
*   `setNPCs()`: Colocación inicial.
*   `respawnearEnemigos()`: Lógica de regeneración continua.
*   `verificarYSpawnearCercanos()`: Sistema de presión al jugador (spawn forzado).

### `entidad.Entidad` y `entidad.NPC`
*   **Estado**: Manejan máquinas de estados simples (`IDLE`, `MOVIENDO`, `ATACANDO`, `MURIENDO`).
*   **`actualizarIA()`**: Método abstracto en `NPC` que define el comportamiento único de cada monstruo.

---

## 3. Generación y Gestión de Enemigos (Análisis Profundo)

Aquí es donde ocurre la "magia" de la dificultad y el rendimiento del juego.

### A. Gestión de Memoria (El Array `npcs`)
El juego **NO** utiliza listas dinámicas (`ArrayList`) para los enemigos durante el ciclo de juego, sino un **Array Estático de tamaño fijo (100)**: `NPC[] npcs`.

1.  **¿Por qué?**: Esto evita la sobrecarga de redimensionar listas en tiempo de ejecución y es más rápido de iterar, algo crítico para un game loop de 60 FPS.
2.  **Slots de Memoria**: El array actúa como un conjunto de "slots". Un slot puede contener una referencia a un objeto `Orco` (ocupado) o ser `null` (vacío).
    *   **Lectura**: El juego recorre el array. Si `npcs[i] != null`, ejecuta su lógica. Si es `null`, lo salta.

### B. El Ciclo de Vida de un Enemigo (En Memoria)

1.  **Nacimiento (Instanciación)**:
    *   Cuando el `AssetSetter` decide crear un enemigo, busca el **primer slot vacío (`null`)** en el array `npcs`.
    *   Ejecuta `npcs[i] = new Orco(pj);`. Java asigna memoria en el *Heap* para el nuevo objeto.
2.  **Vida (Update)**:
    *   Frame a frame, `PanelJuego` accede a `npcs[i]` y modifica sus atributos (`x`, `y`, `vida`).
3.  **Muerte y Limpieza (Garbage Collection)**:
    *   Cuando la vida llega a 0, se activa un flag `estaVivo = false`.
    *   Se reproduce la animación de muerte. Al finalizar, `PanelJuego` ejecuta:
        ```java
        npcs[i] = null; // El slot queda libre
        ```
    *   **¿Qué pasa en memoria?**: Al eliminar la referencia del array, el objeto `Orco` queda "huérfano" (sin referencias). El **Garbage Collector (GC)** de Java detectará esto y liberará la memoria RAM automáticamente en su próxima pasada.
    *   *Nota*: El juego instancia objetos nuevos constantemente (`new Orco()`) en lugar de reutilizar los viejos (Object Pooling). Dado el número bajo (100), el GC puede manejarlo, pero en escalas mayores esto causaría tirones (lag spikes).

### C. Algoritmos de Generación (Spawning Logic)

La "inteligencia" de aparición reside en `AssetSetter.java` y se divide en tres capas:

#### 1. Spawn Inicial
Al arrancar, crea **60 enemigos** en posiciones aleatorias de todo el mapa (`setNPCs`).

#### 2. Respawn Continuo (Mantenimiento de Población)
Cada segundo (60 frames), el juego verifica cuántos enemigos quedan:
*   Si hay **< 50 enemigos**: Spawnea grupos grandes (8-15).
*   Si hay **< 80 enemigos**: Spawnea grupos pequeños (2-4).
*   **Resultado**: El jugador nunca puede "limpiar" el mapa por completo; siempre habrá presión constante.

#### 3. Spawn de Proximidad (Sistema Anti-Aburrimiento)
Cada 3 segundos, verifica si el jugador está "solo" (radio de 10 tiles):
*   Si hay **< 10 enemigos cerca**: Fuerza la aparición inmediata de una oleada de refuerzo **MUY CERCA** (4-8 tiles de distancia).
*   Esto asegura que el jugador no pueda quedarse quieto en una zona segura; el juego le enviará enemigos activamente.

#### 4. Selección de Tipo de Enemigo (Progresión)
La clase de enemigo instanciado depende del **Nivel del Jugador** (`pj.stats.nivel`):
*   Nivel 1: 100% Murciélagos (`Bat`).
*   Nivel 5-9: Slimes y Orcos.
*   Nivel 15+: Todos (incluyendo `Ghoul`).
Esto se gestiona mediante probabilidades simples (`Math.random()`) en `crearEnemigoAleatorio()`.

### D. Optimizaciones de Rendimiento

#### 1. Object Pooling
*   **1,000 NPCs pre-instanciados** al inicio (250 por tipo: `Bat`, `Slime`, `Orco`, `Ghoul`).
*   Ciclo de vida `activar(x,y)` / `desactivar()` — cero `new` durante el gameplay.
*   Cada tipo de NPC implementa `resetearEstado()` para reutilización limpia.

#### 2. Spatial Hashing + Logic Culling
*   `SpatialHashGrid.java` — Consultas O(1) de NPCs cercanos en lugar de O(N²).
*   Utilizado por `Proyectil` y `Jugador.atacarMelee()` para detección de colisiones.
*   **Logic Culling**: NPCs a > 20 tiles actualizan solo cada 10º frame.

#### 3. Frustum Culling
*   `PanelJuego.paintComponent()` — NPCs y proyectiles fuera del viewport se saltan (sin llamada a `draw()`).

#### 4. Renderizado de Tiles por Rango Visible
*   `TileManager.draw()` — Calcula el rango visible de 16×12 tiles y solo dibuja esos (192 tiles en lugar de 10,000).

#### Optimizaciones Adicionales
*   Cachés estáticos de sprites en `Bat`, `Slime`, `Orco`, `Ghoul` — imágenes cargadas una vez, compartidas entre instancias.
*   Rectángulos pre-asignados en verificaciones de colisión de NPCs — cero presión GC por frame.
*   Cálculos de distancia al cuadrado en todo el código (sin `Math.sqrt()`).
*   Bounds checking en `detectorColisiones` para prevenir `ArrayIndexOutOfBounds`.


## 4. Refactorización y Optimizaciones Implementadas

### Estructura Nueva (12 paquetes, 35 archivos)

Se realizó una refactorización completa del proyecto para mejorar mantenibilidad, rendimiento y escalabilidad.

#### Problemas Resueltos

| Problema | Solución |
|----------|----------|
| God Object `PanelJuego` (458 líneas) | Dividido en `PanelJuego` (140 líneas) + `MundoJuego` (220 líneas) |
| UI monolítica (792 líneas) | Dividida en 9 clases: `InterfazUsuario` + `HUD` + 7 pantallas |
| Input duplicado (`keyHandler` + `MouseHandler`) | Unificado en `GestorEntrada` |
| Constantes dispersas | Centralizadas en `Configuracion` (estáticas) |
| Dependencia circular (entidades → `PanelJuego`) | Entidades referencian `MundoJuego` (dirección única) |
| Naming conventions violadas | Clases renombradas (PascalCase) con paquetes en español |
| Capas sin definir | 12 paquetes con responsabilidades claras |

#### Optimizaciones Preservadas

*   **Object Pool**: 1000 NPCs pre-asignados (cero `new` en gameplay).
*   **Spatial Hash Grid**: Colisiones O(N).
*   **Frustum Culling + Logical Culling**: Renderizado optimizado.
*   **Pre-allocated rectangles**: Cero generación de basura (GC).
*   **Sprite cache estático**: Por tipo de NPC.
*   **Mapa procedural**: 100×100 tiles.
