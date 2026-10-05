# Space Shooter Arcade

A dependency-free 2D space shooter built with Java Swing.

## Play

From the project root:

```bash
javac -d out src/Gameboard.java
java -cp out Gameboard
```

## Controls

- Move: `A` / `D` or left / right arrows
- Fire: hold `Space`
- Pause: `P`
- Restart after game over: `Enter`

Small, medium, and large asteroids take one, two, and three hits respectively. Larger asteroids are slower and worth more points. You only lose a ship when an asteroid collides with your ship; asteroids that pass the bottom of the screen simply disappear.
