#!/bin/bash
# Script para compilar y ejecutar el juego con compatibilidad Java 17

echo "🧹 Limpiando archivos compilados anteriores..."
rm -rf bin/*

echo "🔨 Compilando el proyecto..."
javac -source 17 -target 17 -d bin -cp res \
    src/configuracion/*.java \
    src/utilidades/*.java \
    src/audio/*.java \
    src/tiles/*.java \
    src/estadisticas/*.java \
    src/mundo/*.java \
    src/entidad/*.java \
    src/items/*.java \
    src/colision/*.java \
    src/entrada/*.java \
    src/interfaz/*.java \
    src/nucleo/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa!"
    echo ""
    echo "🎮 Ejecutando PrimerJuego2D..."
    java -cp bin:res nucleo.Main
else
    echo "❌ Error en la compilación"
    exit 1
fi
