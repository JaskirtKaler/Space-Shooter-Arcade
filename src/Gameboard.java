import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Gameboard {
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;

    public void init() {
        JFrame frame = new JFrame("Space Shooter Arcade");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new GamePanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Gameboard().init());
    }

    @SuppressWarnings("serial")
    private static final class GamePanel extends JPanel {
        private static final int FPS_DELAY = 16;
        private static final int SHIP_WIDTH = 72;
        private static final int SHIP_HEIGHT = 72;
        private static final int SHIP_Y = WINDOW_HEIGHT - 105;
        private static final int SHIP_SPEED = 7;
        private static final int MAX_BULLETS = 8;
        private static final int SHOT_COOLDOWN_FRAMES = 8;

        private final Random random = new Random();
        private final List<Bullet> bullets = new ArrayList<>();
        private final List<Asteroid> asteroids = new ArrayList<>();
        private final List<Particle> particles = new ArrayList<>();
        private final Star[] stars = new Star[95];
        private final Timer timer;

        private final BufferedImage shipImage;
        private final BufferedImage smallAsteroidImage;
        private final BufferedImage mediumAsteroidImage;
        private final BufferedImage largeAsteroidImage;

        private boolean movingLeft;
        private boolean movingRight;
        private boolean firing;
        private boolean paused;
        private boolean gameOver;
        private double shipX = (WINDOW_WIDTH - SHIP_WIDTH) / 2.0;
        private int score;
        private int lives = 3;
        private int elapsedFrames;
        private int spawnCountdown = 35;
        private int shotCooldown;
        private int invulnerabilityFrames;

        private GamePanel() {
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            setBackground(new Color(3, 7, 20));
            setFocusable(true);

            shipImage = loadImage("assets/sprites/player-ship.png");
            smallAsteroidImage = loadImage("assets/sprites/asteroid-small.png");
            mediumAsteroidImage = loadImage("assets/sprites/asteroid-medium.png");
            largeAsteroidImage = loadImage("assets/sprites/asteroid-large.png");

            for (int i = 0; i < stars.length; i++) {
                stars[i] = new Star(random.nextInt(WINDOW_WIDTH), random.nextInt(WINDOW_HEIGHT),
                        1 + random.nextInt(3), 0.35 + random.nextDouble() * 1.45);
            }

            installControls();
            timer = new Timer(FPS_DELAY, this::tick);
            timer.start();
        }

        private void installControls() {
            InputMap input = getInputMap(WHEN_IN_FOCUSED_WINDOW);
            ActionMap actions = getActionMap();

            bind(input, actions, "pressed LEFT", "left-pressed", () -> movingLeft = true);
            bind(input, actions, "released LEFT", "left-released", () -> movingLeft = false);
            bind(input, actions, "pressed A", "a-pressed", () -> movingLeft = true);
            bind(input, actions, "released A", "a-released", () -> movingLeft = false);
            bind(input, actions, "pressed RIGHT", "right-pressed", () -> movingRight = true);
            bind(input, actions, "released RIGHT", "right-released", () -> movingRight = false);
            bind(input, actions, "pressed D", "d-pressed", () -> movingRight = true);
            bind(input, actions, "released D", "d-released", () -> movingRight = false);
            bind(input, actions, "pressed SPACE", "fire-pressed", () -> {
                firing = true;
                shoot();
            });
            bind(input, actions, "released SPACE", "fire-released", () -> firing = false);
            bind(input, actions, "pressed P", "pause", () -> {
                if (!gameOver) paused = !paused;
            });
            bind(input, actions, "pressed ENTER", "restart", () -> {
                if (gameOver) restart();
            });
        }

        private void bind(InputMap input, ActionMap actions, String stroke, String name, Runnable action) {
            input.put(KeyStroke.getKeyStroke(stroke), name);
            actions.put(name, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    action.run();
                }
            });
        }

        private void tick(ActionEvent event) {
            updateStars();
            if (!paused && !gameOver) {
                updateGame();
            }
            repaint();
        }

        private void updateGame() {
            elapsedFrames++;
            if (shotCooldown > 0) shotCooldown--;
            if (invulnerabilityFrames > 0) invulnerabilityFrames--;

            if (movingLeft) shipX -= SHIP_SPEED;
            if (movingRight) shipX += SHIP_SPEED;
            shipX = Math.max(12, Math.min(WINDOW_WIDTH - SHIP_WIDTH - 12, shipX));
            if (firing) shoot();

            updateBullets();
            updateAsteroids();
            updateParticles();
            detectBulletHits();
            detectShipHits();

            if (--spawnCountdown <= 0) {
                spawnAsteroid();
                int difficulty = Math.min(28, elapsedFrames / 900);
                spawnCountdown = Math.max(20, 54 - difficulty) + random.nextInt(23);
            }
        }

        private void updateStars() {
            for (Star star : stars) {
                star.y += paused ? star.speed * 0.2 : star.speed;
                if (star.y > WINDOW_HEIGHT) {
                    star.y = 0;
                    star.x = random.nextInt(WINDOW_WIDTH);
                }
            }
        }

        private void updateBullets() {
            Iterator<Bullet> iterator = bullets.iterator();
            while (iterator.hasNext()) {
                Bullet bullet = iterator.next();
                bullet.y -= 11;
                if (bullet.y < -20) iterator.remove();
            }
        }

        private void updateAsteroids() {
            Iterator<Asteroid> iterator = asteroids.iterator();
            while (iterator.hasNext()) {
                Asteroid asteroid = iterator.next();
                asteroid.y += asteroid.speed;
                asteroid.x += Math.sin(asteroid.y * 0.018 + asteroid.phase) * asteroid.drift;
                asteroid.rotation += asteroid.rotationSpeed;
                if (asteroid.y > WINDOW_HEIGHT + asteroid.size) {
                    iterator.remove();
                }
            }
        }

        private void updateParticles() {
            Iterator<Particle> iterator = particles.iterator();
            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                particle.x += particle.dx;
                particle.y += particle.dy;
                particle.dy += 0.015;
                if (--particle.life <= 0) iterator.remove();
            }
        }

        private void detectBulletHits() {
            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                Asteroid hit = null;
                for (Asteroid asteroid : asteroids) {
                    double radius = asteroid.size * 0.38;
                    double dx = bullet.x - (asteroid.x + asteroid.size / 2.0);
                    double dy = bullet.y - (asteroid.y + asteroid.size / 2.0);
                    if (dx * dx + dy * dy < radius * radius) {
                        hit = asteroid;
                        break;
                    }
                }
                if (hit != null) {
                    bulletIterator.remove();
                    hit.hitPoints--;
                    makeExplosion(bullet.x, bullet.y, 5, new Color(79, 225, 255));
                    if (hit.hitPoints <= 0) {
                        score += hit.scoreValue;
                        makeExplosion(hit.x + hit.size / 2.0, hit.y + hit.size / 2.0,
                                10 + hit.size / 4, new Color(255, 174, 65));
                        asteroids.remove(hit);
                    }
                }
            }
        }

        private void detectShipHits() {
            if (invulnerabilityFrames > 0) return;
            double shipCenterX = shipX + SHIP_WIDTH / 2.0;
            double shipCenterY = SHIP_Y + SHIP_HEIGHT / 2.0;
            Asteroid hit = null;
            for (Asteroid asteroid : asteroids) {
                double dx = shipCenterX - (asteroid.x + asteroid.size / 2.0);
                double dy = shipCenterY - (asteroid.y + asteroid.size / 2.0);
                double combinedRadius = SHIP_WIDTH * 0.28 + asteroid.size * 0.34;
                if (dx * dx + dy * dy < combinedRadius * combinedRadius) {
                    hit = asteroid;
                    break;
                }
            }
            if (hit != null) {
                asteroids.remove(hit);
                makeExplosion(shipCenterX, shipCenterY, 28, new Color(63, 219, 255));
                damagePlayer();
            }
        }

        private void shoot() {
            if (paused || gameOver || shotCooldown > 0 || bullets.size() >= MAX_BULLETS) return;
            bullets.add(new Bullet(shipX + SHIP_WIDTH / 2.0, SHIP_Y + 5));
            shotCooldown = SHOT_COOLDOWN_FRAMES;
        }

        private void spawnAsteroid() {
            double roll = random.nextDouble();
            AsteroidKind kind = roll < 0.48 ? AsteroidKind.SMALL
                    : roll < 0.82 ? AsteroidKind.MEDIUM : AsteroidKind.LARGE;
            int size = kind.size;
            double x = 10 + random.nextDouble() * (WINDOW_WIDTH - size - 20);
            double difficultyBoost = Math.min(2.4, elapsedFrames / 1800.0);
            double speed = kind.baseSpeed + random.nextDouble() * 0.8 + difficultyBoost;
            asteroids.add(new Asteroid(x, -size, size, speed,
                    0.15 + random.nextDouble() * 0.55,
                    random.nextDouble() * Math.PI * 2,
                    (random.nextDouble() - 0.5) * 0.035,
                    kind.hitPoints, kind.score, imageFor(kind)));
        }

        private BufferedImage imageFor(AsteroidKind kind) {
            return switch (kind) {
                case SMALL -> smallAsteroidImage;
                case MEDIUM -> mediumAsteroidImage;
                case LARGE -> largeAsteroidImage;
            };
        }

        private void damagePlayer() {
            if (gameOver || invulnerabilityFrames > 0) return;
            lives--;
            invulnerabilityFrames = 100;
            if (lives <= 0) gameOver = true;
        }

        private void restart() {
            bullets.clear();
            asteroids.clear();
            particles.clear();
            shipX = (WINDOW_WIDTH - SHIP_WIDTH) / 2.0;
            score = 0;
            lives = 3;
            elapsedFrames = 0;
            spawnCountdown = 35;
            shotCooldown = 0;
            invulnerabilityFrames = 90;
            movingLeft = false;
            movingRight = false;
            firing = false;
            paused = false;
            gameOver = false;
        }

        private void makeExplosion(double x, double y, int count, Color color) {
            for (int i = 0; i < count; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double speed = 0.7 + random.nextDouble() * 3.5;
                particles.add(new Particle(x, y, Math.cos(angle) * speed,
                        Math.sin(angle) * speed, 20 + random.nextInt(25), color));
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            paintBackground(g);
            paintBullets(g);
            paintAsteroids(g);
            paintParticles(g);
            paintShip(g);
            paintHud(g);

            if (paused) paintOverlay(g, "PAUSED", "Press P to continue");
            if (gameOver) paintOverlay(g, "GAME OVER", "Press ENTER to fly again");
            g.dispose();
        }

        private void paintBackground(Graphics2D g) {
            g.setColor(new Color(3, 7, 20));
            g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            for (Star star : stars) {
                int alpha = 100 + star.size * 45;
                g.setColor(new Color(135, 204, 255, Math.min(255, alpha)));
                g.fill(new Ellipse2D.Double(star.x, star.y, star.size, star.size));
            }
            g.setColor(new Color(30, 112, 164, 25));
            g.fillOval(-180, 100, 500, 360);
            g.setColor(new Color(91, 30, 145, 20));
            g.fillOval(610, -70, 390, 400);
        }

        private void paintBullets(Graphics2D g) {
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Bullet bullet : bullets) {
                g.setColor(new Color(54, 226, 255, 70));
                g.fillRoundRect((int) bullet.x - 6, (int) bullet.y - 9, 12, 24, 8, 8);
                g.setColor(new Color(189, 250, 255));
                g.drawLine((int) bullet.x, (int) bullet.y - 7, (int) bullet.x, (int) bullet.y + 8);
            }
        }

        private void paintAsteroids(Graphics2D g) {
            for (Asteroid asteroid : asteroids) {
                Graphics2D ag = (Graphics2D) g.create();
                ag.translate(asteroid.x + asteroid.size / 2.0, asteroid.y + asteroid.size / 2.0);
                ag.rotate(asteroid.rotation);
                ag.drawImage(asteroid.image, -asteroid.size / 2, -asteroid.size / 2,
                        asteroid.size, asteroid.size, null);
                ag.dispose();
            }
        }

        private void paintParticles(Graphics2D g) {
            for (Particle particle : particles) {
                int alpha = Math.min(255, particle.life * 8);
                g.setColor(new Color(particle.color.getRed(), particle.color.getGreen(),
                        particle.color.getBlue(), alpha));
                int size = Math.max(2, particle.life / 8);
                g.fillOval((int) particle.x - size / 2, (int) particle.y - size / 2, size, size);
            }
        }

        private void paintShip(Graphics2D g) {
            if (invulnerabilityFrames > 0 && (invulnerabilityFrames / 6) % 2 == 0) return;
            g.drawImage(shipImage, (int) shipX, SHIP_Y, SHIP_WIDTH, SHIP_HEIGHT, null);
        }

        private void paintHud(Graphics2D g) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            g.setColor(new Color(217, 247, 255));
            g.drawString(String.format("SCORE  %06d", score), 24, 34);

            String livesText = "SHIPS  " + lives;
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(livesText, WINDOW_WIDTH - metrics.stringWidth(livesText) - 24, 34);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            g.setColor(new Color(130, 196, 222));
            g.drawString("A / D or ← / → to move    SPACE to fire    P to pause", 24, WINDOW_HEIGHT - 18);
        }

        private void paintOverlay(Graphics2D g, String title, String subtitle) {
            g.setColor(new Color(1, 5, 15, 190));
            g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 56));
            g.setColor(new Color(82, 225, 255));
            drawCentered(g, title, WINDOW_HEIGHT / 2 - 20);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
            g.setColor(new Color(220, 244, 255));
            drawCentered(g, subtitle, WINDOW_HEIGHT / 2 + 28);
            if (gameOver) {
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
                drawCentered(g, "FINAL SCORE  " + score, WINDOW_HEIGHT / 2 + 65);
            }
        }

        private void drawCentered(Graphics2D g, String text, int y) {
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, (WINDOW_WIDTH - metrics.stringWidth(text)) / 2, y);
        }

        private static BufferedImage loadImage(String path) {
            try {
                File file = new File(path);
                if (file.isFile()) return ImageIO.read(file);
                var resource = Gameboard.class.getClassLoader().getResource(path);
                if (resource != null) return ImageIO.read(resource);
                throw new IOException("Asset not found: " + path);
            } catch (IOException exception) {
                throw new IllegalStateException("Could not load game asset " + path, exception);
            }
        }
    }

    private enum AsteroidKind {
        SMALL(38, 1, 100, 3.0),
        MEDIUM(58, 2, 250, 2.25),
        LARGE(84, 3, 500, 1.55);

        private final int size;
        private final int hitPoints;
        private final int score;
        private final double baseSpeed;

        AsteroidKind(int size, int hitPoints, int score, double baseSpeed) {
            this.size = size;
            this.hitPoints = hitPoints;
            this.score = score;
            this.baseSpeed = baseSpeed;
        }
    }

    private static final class Bullet {
        private final double x;
        private double y;

        private Bullet(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final class Asteroid {
        private double x;
        private double y;
        private final int size;
        private final double speed;
        private final double drift;
        private final double phase;
        private final double rotationSpeed;
        private int hitPoints;
        private final int scoreValue;
        private final Image image;
        private double rotation;

        private Asteroid(double x, double y, int size, double speed, double drift,
                         double phase, double rotationSpeed, int hitPoints,
                         int scoreValue, Image image) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
            this.drift = drift;
            this.phase = phase;
            this.rotationSpeed = rotationSpeed;
            this.hitPoints = hitPoints;
            this.scoreValue = scoreValue;
            this.image = image;
        }
    }

    private static final class Particle {
        private double x;
        private double y;
        private final double dx;
        private double dy;
        private int life;
        private final Color color;

        private Particle(double x, double y, double dx, double dy, int life, Color color) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.life = life;
            this.color = color;
        }
    }

    private static final class Star {
        private double x;
        private double y;
        private final int size;
        private final double speed;

        private Star(double x, double y, int size, double speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
        }
    }
}
