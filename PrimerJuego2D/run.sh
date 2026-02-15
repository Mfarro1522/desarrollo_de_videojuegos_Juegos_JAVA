#!/bin/bash
# Script para compilar y ejecutar el juego con compatibilidad Java 17

echo "🧹 Limpiando archivos compilados anteriores..."
rm -rf bin/*

echo "🔨 Compilando el proyecto..."
javac -source 17 -target 17 -d bin -cp res \
    src/entidad/*.java \
    src/main/*.java \
    src/objetos/*.java \
    src/tiles/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa!"
    echo ""
    echo "🎮 Ejecutando PrimerJuego2D..."
    java -cp bin:res main.Main
else
    echo "❌ Error en la compilación"
    exit 1
fi
