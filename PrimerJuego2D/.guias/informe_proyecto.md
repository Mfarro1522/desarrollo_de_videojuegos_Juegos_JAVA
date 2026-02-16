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

1.  **Población Máxima (Escalado Exponencial)**:
    *   Fórmula antigua: `60 + (Nivel * 10)` (Tope 300).
    *   **Fórmula nueva (Cuadrática)**: `80 + (2 * Nivel^2)`.
    *   **Progreso**: Comienza suave (Lvl 1 ≈ 82) y escala agresivamente al final (Lvl 20 ≈ 880).
    *   **Tope**: `POOL_TOTAL - 10`.
    *   Esto permite oleadas masivas en niveles altos, aprovechando las optimizaciones de colisiones y reciclaje.
2.  **Progresión de Tipos**:
    *   **Nivel 1-2**: Solo Murciélagos.
    *   **Nivel 3-4**: Murciélagos y Slimes.
    *   **Nivel 5-9**: Se suman Orcos.
    *   **Nivel 15+**: Aparecen Ghouls y aumenta la densidad de Orcos.
3.  **Spawn de Anillo (El Cerco)**:
    *   **Concepto**: Una oleada constante y dinámica que rodea al jugador, reemplazando el respawn por intervalos o proximidad estática.
    *   **Implementación**: `GestorRecursos.spawnearEnAnillo()` se ejecuta constantemente. Calcula el rectángulo de la cámara y define un margen de 2 tiles hacia afuera.
    *   **Lógica**: Elige aleatoriamente uno de los 4 lados (Arriba, Abajo, Izquierda, Derecha) y spawnea un enemigo en una posición válida de ese borde.
    *   **Objetivo**: Mantener la población siempre cercana al máximo permitido (`maxNPCsActivos`), generando presión constante desde fuera de la pantalla.

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
