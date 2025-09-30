# Tic Tac Toe – Java Bilbioteca Swing con Arquitectura MVC

Juego de **3 en raya (Tic-Tac-Toe)** en **Java Swing** con arquitectura **MVC**.  
Incluye **IA (minimax + poda)**, elección de **quién empieza** y de **qué lado juega la CPU** (X u O), **temas de color** y **sonidos** generados por código.

<img width="1366" height="732" alt="image" src="https://github.com/user-attachments/assets/5056fa82-21ed-44f2-b8e6-647cff3e15fe" />
<img width="1366" height="730" alt="image" src="https://github.com/user-attachments/assets/c6bb0091-00c9-45a6-be22-09f3eb98d131" />
<img width="1366" height="729" alt="image" src="https://github.com/user-attachments/assets/5f34b81b-0a8b-4706-8c8d-f29232d08800" />


## ✨ Características
- **Modos**: Humano vs Humano o **Humano vs CPU**.
- **IA**: Minimax con **poda alfa-beta** (perfecta para 3×3).
- **Opciones en la UI**:
  - Quién empieza: **X / O**.
  - Lado de la CPU: **CPU: X / CPU: O**.
  - Temas: **Claro / Oscuro / Océano**.
  - **Sonido** ON/OFF (beeps sin archivos externos).
- **UX**:
  - Resalta la **línea ganadora**.
  - **Marcador**: victorias de X, O y **empates**.
  - **Nueva partida** en un clic.
- **Single-file friendly**: todo en **un solo archivo** para facilidad de uso.

## 🧱 Requisitos
- **Java 17+** (recomendado; funciona en 11+).
- Cualquier IDE (VS Code/IntelliJ/Eclipse) o terminal.

## 🚀 Ejecución
Compila y ejecuta desde consola:
```bash
javac TicTacToe.java
java TicTacToeApp
