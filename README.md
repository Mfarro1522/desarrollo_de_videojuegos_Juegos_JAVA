# 🎮 Desarrollo de Videojuegos en Java

Repositorio que documenta el proceso de aprendizaje y desarrollo de un videojuego 2D en Java, desde los fundamentos hasta la implementación de un proyecto completo.

![Preview del Juego](preview/Videocaptura%20de%20pantalla_20251221_010449.gif)

## 📋 Descripción

Este repositorio contiene el desarrollo de **Arena Survivors**, un juego 2D de acción tipo roguelite desarrollado en Java Swing. El proyecto comenzó como un ejercicio de aprendizaje siguiendo tutoriales, pero evolucionó hacia un juego completo con sistemas y mecánicas propias.

**Documentación técnica completa:** [Notion](https://www.notion.so/Aprender-a-Crear-Videojuegos-2a4253f86646801ab051d5f96b890c1b?source=copy_link)

---

## 🎯 Arena Survivors

Juego de supervivencia donde el jugador enfrenta oleadas continuas de enemigos mientras recolecta mejoras y equipamiento para aumentar sus capacidades.

### ⚙️ Características Técnicas

**Sistema de Combate**
- Proyectiles automáticos con detección de colisiones optimizada
- Múltiples tipos de enemigos (Murciélagos, Slimes, Orcos, Ghouls)
- Sistema de daño y experiencia

**Sistema de Progresión**
- Cofres con botín progresivo
- Sistema de items con niveles de rareza
- Equipamiento y consumibles

**Optimizaciones**
- Object Pooling para 1000+ entidades simultáneas
- Spatial Hash Grid para detección de colisiones eficiente
- Game Loop a 60 FPS constantes
- Generación procedural de enemigos

**Arquitectura**
```
PrimerJuego2D/
├── src/
│   ├── nucleo/          # Game Loop, Main
│   ├── mundo/           # Lógica del mundo, generación procedural
│   ├── entidad/         # Jugador, NPCs, proyectiles
│   ├── items/           # Sistema de objetos y cofres
│   ├── tiles/           # Sistema de mapas
│   ├── colision/        # Detección de colisiones
│   ├── interfaz/        # UI y menús
│   ├── audio/           # Sistema de sonido
│   └── configuracion/   # Constantes del juego
└── res/                 # Recursos (sprites, mapas, audio)
```

### 🛠️ Tecnologías

- Java 17
- Java Swing para gráficos
- Arquitectura MVC
- Patrones: Object Pool, State Machine

### ▶️ Ejecutar

```bash
cd PrimerJuego2D
./run.sh
```

**Requisitos:** Java 17 o superior

---

## 📁 Estructura del Repositorio

```
desarrollo_de_videojuegos_Juegos_JAVA/
├── PrimerJuego2D/     # Proyecto principal
├── versiones/         # Versiones anteriores
├── preview/           # Capturas y GIFs
└── README.md
```

---

## 📚 Recursos de Aprendizaje

**Tutorial Base:**  
"Java 2D Game Development" por RyiSnow  
[YouTube Playlist](https://www.youtube.com/watch?v=om59cwR7psI&list=PL_QPQmz5C6WUF-pOQDsbsKbaBZqXj4qSq)

El proyecto comenzó siguiendo este tutorial para aprender los fundamentos del desarrollo de videojuegos 2D en Java, incluyendo conceptos como Game Loop, renderizado, tiles, colisiones y gestión de estados.

---

## 💡 Conceptos Implementados

**Fundamentos**
- Game Loop y control de FPS
- Renderizado de sprites y animaciones
- Sistema de tiles y mapas
- Detección de colisiones (AABB)
- Gestión de entrada de usuario
- Máquina de estados

**Técnicas Avanzadas**
- Object Pooling
- Spatial Hash Grid
- Generación procedural
- Arquitectura modular
- Optimización de rendimiento
- Persistencia de datos

---

## 👥 Créditos

**Desarrolladores:**
- [@Mfarro1522](https://github.com/Mfarro1522) - Mauricio Farro (Desarrollo principal)
- [@jackhfernandez](https://github.com/jackhfernandez) - Jack Hernández (NPCs, sistema de personajes)

**Recursos:**
- Tutorial base: [RyiSnow - Java 2D Game Development](https://www.youtube.com/c/RyiSnow)
- Sprites y assets: Diversos recursos de dominio público y propios

---

## 🔗 Enlaces

- [Documentación completa (Notion)](https://www.notion.so/Aprender-a-Crear-Videojuegos-2a4253f86646801ab051d5f96b890c1b?source=copy_link)
- [Informe técnico del proyecto](PrimerJuego2D/.guias/informe_proyecto.md)

---

## 📄 Licencia

Proyecto educativo. El código del tutorial base pertenece a sus respectivos autores. Las extensiones y mejoras propias están disponibles para referencia educativa.