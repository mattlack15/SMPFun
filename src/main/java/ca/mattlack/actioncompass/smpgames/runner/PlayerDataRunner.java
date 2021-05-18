package ca.mattlack.actioncompass.smpgames.runner;

public class PlayerDataRunner {
    private int lives = 3;
    private boolean runner = false;

    public int getLives() {
        return lives;
    }

    public int decrementAndGetLives() {
        return lives--;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void setRunner(boolean value) {
        this.runner = value;
        lives = runner ? 1 : 3;
    }

    public boolean isRunner() {
        return this.runner;
    }
}
