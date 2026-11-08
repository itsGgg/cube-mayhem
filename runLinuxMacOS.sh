#!/bin/bash

# Se placer à la racine du projet (là où se trouve ce script)
cd "$(dirname "$0")"

# Compilation de tous les fichiers .java du projet dans out/
find src -name "*.java" > sources.txt
javac -d out -cp src @sources.txt

if [ $? -ne 0 ]; then
    rm -f sources.txt
    read -p "Erreur de compilation. Appuyez sur Entrée pour quitter..."
    exit 1
fi

rm -f sources.txt

# Exécution du programme
java -cp out Main

read -p "Appuyez sur Entrée pour quitter..."
