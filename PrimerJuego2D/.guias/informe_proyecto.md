# Informe Técnico del Proyecto: PrimerJuego2D

## 1. Arquitectura del Proyecto

El proyecto ha evolucionado hacia una arquitectura robusta y modular, separando claramente la vista (rendering/loop) de la lógica y los datos (estado del mundo).

### Estructura General

*   **Núcleo (`main` / `nucleo`):**
    *   **`PanelJuego.java` (VISTA/LOOP):** Es el contenedor gráfico (`JPanel`) y responsable exclusivamente del **Game Loop**.
        *   **Game Loop**: Mantiene la ejecución a **60 FPS** usando "Delta Time".
        *   **Responsabilidad**: Delegar el `update()` a `MundoJuego` y el `draw()` a los componentes visuales. No mantiene estado del juego.
    *   **`MundoJuego.java` (MODELO/LÓGICA):** Es el **corazón** del juego. Contiene:
        *   El estado actual (`gameState`: menú, jugando, pausa, game over).
        *   Los arrays de entidades (`npcs`, `proyectiles`, `objs`).
        *   Referencias a todos los subsistemas (`TileManager`, `DetectorColisiones`, `GestorRecursos`, `Estadisticas`).
        *   Lógica central de actualización (`update()`) y orquestación.

*   **Sistema de Entidades (`entidad`):**
    *   Usa herencia y polimorfismo (`Entidad` -> `NPC` -> `Bat`, `Slime`, `Orco`, `Ghoul`).
    *   **`Jugador`**: Gestiona entrada, movimiento y estados del héroe.
    *   **`NPC`**: Clase base abstracta con IA básica, máquinas de estados y **Object Pooling**.

*   **Gestión de Recursos (`mundo` / `tiles`):**
    *   **`TileManager`**: Dibuja el mapa estático basado en archivos de texto.
    *   **`GestorRecursos`**: Responsable de la **generación procedural** de enemigos y objetos (sustituye al antiguo `AssetSetter`).

---

## 2. Clases y Métodos Principales

### `mundo.MundoJuego`
Es el contenedor de memoria y estado.
*   **Arrays de Gestión**: Arrays estáticos para evitar GC (Garbage Collection).
    *   `public NPC[] npcs = new NPC[1000];` (Object Pool global).
    *   `public Proyectil[] proyectiles = new Proyectil[MAX];`
*   **Método `update()`**: Orquesta la lógica frame a frame (notificaciones, grid espacial, NPCs, respawn, proyectiles).
*   **Método `iniciarJuego()`**: Resetea el estado y prepara una nueva partida.

### `mundo.GestorRecursos`
El "Director de Escena" y generador de contenido.
*   **`inicializarPool()`**: Pre-instancia 1000 enemigos en memoria al inicio.
*   **`respawnearEnemigos()`**: Mantiene la población de enemigos activa basándose en el nivel del jugador.
*   **`spawnearEnAnillo()`**: Genera enemigos constantemente en los bordes de la cámara ("El Cerco") para mantener la presión.
*   **`elegirTipoEnemigo()`**: Decide qué enemigo spawnear (Bat, Slime, Orco, Ghoul) según probabilidades y nivel.

### `nucleo.PanelJuego`
*   **`paintComponent(Graphics g)`**: Método de renderizado optimizado.
    *   Implementa **Frustum Culling**: Solo dibuja entidades dentro de la pantalla visible.

---

## 3. Generación y Gestión de Enemigos (Análisis Profundo)

El sistema ha sido reescrito para maximizar el rendimiento y escalar la dificultad.

### A. Gestión de Memoria (Object Pooling)
El juego utiliza un **Object Pool** masivo de **1000 NPCs** (`GestorRecursos.POOL_TOTAL`).
*   **Separación Suave (Soft Collisions)**:
    *   Se eliminaron las colisiones rígidas (cajas invisibles) entre enemigos, que causaban bloqueos y cuellos de botella.
    *   Nuevo sistema de **Vectores de Repulsión**: Cada enemigo calcula un vector para alejarse de sus vecinos cercanos (usando la `GrillaEspacial`) y otro para perseguir al jugador.
    *   El resultado es un movimiento fluido tipo "enjambre" que permite cientos de unidades sin atascos.

1.  **Cero `new` en Gameplay**:
    *   Al inicio (`setupJuego`), se crean 250 murciélagos, 250 slimes, 250 orcos y 250 ghouls.
    *   Durante el juego, **NUNCA** se instancia un nuevo NPC.
2.  **Ciclo de Vida (Activación/Desactivación)**:
    *   **Spawn**: Se busca un slot inactivo del tipo deseado y se llama a `activar(x, y)`. Esto resetea su vida y estado.
    *   **Muerte**: Al morir, se reproduce la animación y se llama a `desactivar()`, devolviendo el slot al pool (marcando `activo = false`).
3.  **Beneficio**: El Garbage Collector de Java no tiene trabajo durante la partida, eliminando los tirones (lag spikes).

### B. Algoritmo de Dificultad Dinámica

La lógica en `GestorRecursos` ajusta la dificultad en tiempo real:

### 3.4 Sistema de Oleadas (Fase 4 y 5)
Implementamos un sistema de **amenaza progresiva** que utiliza una **Función Escalonada (Step Function)** y composición ponderada.

#### Curva de Cantidad (Step Function)
En lugar de una fórmula matemática continua, definimos escalones de dificultad diseñados a mano para controlar el ritmo:
*   **Nivel 1 (Tutorial)**: Máx 12 enemigos. Espacio para aprender controles.
*   **Nivel 2-3 (Calentamiento)**: Máx 35 enemigos.
*   **Nivel 4-6 (La Horda)**: Máx 90 enemigos.
*   **Nivel 7-9 (Presión)**: Máx 180 enemigos.
*   **Nivel 10+ (El Diluvio)**: `min(POOL_TOTAL - 10, 250 + ((Nivel - 10) * 20))`.

#### Composición de Enemigos
La mezcla de enemigos cambia según el nivel para introducir mecánicas nuevas:
*   **Nv 1-4**: 100% Murciélagos (Rápidos, débiles).
*   **Nv 5-9**: 85% Murciélagos + 15% Orcos (Tanques).
*   **Nv 10+**: 40% Murciélagos, 40% Slimes, 20% Orcos.

### 3.5 Escalado del Jugador (Fase 5)
Para mantener el interés, el jugador recibe mejoras tangibles al subir de nivel:
*   **Salud**: +10 HP Máx.
*   **Curación**: +30% de Salud Máxima (Recompensa parcial).
*   **Daño**: +2 Ataque (Escalado conservador).
*   **Implementación**: Patrón `Observer` ligera. `Estadisticas` notifica a `Jugador` vía callback `onLevelUp`.

## 4. Estadísticas de Entidades (Balanceo)

| Entidad | HP | Ataque | Vel | Rol |
| :--- | :---: | :---: | :---: | :--- |
| **Jugador (Doom)** | 25 (+10/lvl) | 10 (+2/lvl) | 5 | DPS Melee |
| **Murciélago (Bat)** | 20 | 2 | 2 | Enjambre débil |
| **Slime** | 30 | 3 | 1 | Masa media |
| **Orco** | 50 | 5 | 1 | Tanque temprano |
| **Ghoul** | 80 | 8 | 2 | Tanque agresivo |

### C. Optimizaciones de Rendimiento (Engine)

1.  **Spatial Hash Grid (`GrillaEspacial`)**:
    *   Divide el mundo en celdas grandes. Las colisiones solo se comprueban contra entidades en la misma celda o vecinas. Reduce la complejidad de O(N²) a casi O(N).
2.  **Logic Culling**:
    *   Los enemigos lejanos (> 20 tiles) actualizan su IA solo 1 de cada 10 frames. Ahorra muchísimo CPU.
3.  **Frustum Culling**:
    *   El renderizado (`PanelJuego.paintComponent`) ignora completamente cualquier entidad fuera de la cámara.
4.  **Rectángulos Pre-allocados**:
    *   Las clases `NPC` usan `Rectangle` reutilizables para cálculos de colisión, evitando crear miles de objetos temporales por segundo.
5.  **Reciclaje Agresivo (Desaparición)**:
    *   **Concepto**: Enemigos que quedan muy atrás ("Retaguardia") son inútiles para el jugador.
    *   **Implementación**: Si la distancia al cuadrado entre NPC y Jugador supera `DISTANCIA_DESAPARICION_SQ` (aprox 1.5x ancho de pantalla), el NPC llama a `desactivar()` inmediatamente.
    *   **Beneficio**: Libera slots del Pool instantáneamente para que `GestorRecursos` pueda usarlos en generar enemigos nuevos *frente* al jugador.

---

## 4. Estructura de Paquetes y Componentes

El proyecto está organizado en 12 paquetes especializados:

| Paquete | Descripción | Contenido Clave |
| :--- | :--- | :--- |
| **`nucleo`** | Motor principal | `PanelJuego` (Loop), `Main` (Entry) |
| **`mundo`** | Estado y datos | `MundoJuego` (State), `GestorRecursos` (Spawning) |
| **`entidad`** | Actores del juego | `Jugador`, `NPC` (IA), `Orco`, `Slime`, `Bat` |
| **`interfaz`** | GUI y Menús | `InterfazUsuario`, `HUD`, `PantallaSeleccion` |
| **`tiles`** | Mapa | `TileManager` (Renderizado del mundo) |
| **`entrada`** | Input | `GestorEntrada` (Teclado/Mouse unificado) |
| **`colision`** | Física | `DetectorColisiones` (Hitbox logic) |
| **`objetos`** | Items interactivos | `SuperObjeto`, `Cofres`, `PowerUps` |
| **`audio`** | Sonido | `GestorAudio` |
| **`configuracion`** | Constantes | `Configuracion` (Resolución, Flags) |
| **`estadisticas`** | Meta-juego | `Estadisticas` (Nivel, XP, Highscore) |
| **`utilidades`** | Tools | `Herramientas` (Escalado de imagen), `Notificacion` |
