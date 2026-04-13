package vacuum;

public class RowVacuumPercept {

    private final boolean dirty;
    private final boolean canLeft;
    private final boolean canRight;

    public RowVacuumPercept(boolean dirty, boolean canLeft, boolean canRight) {
        this.dirty = dirty;
        this.canLeft = canLeft;
        this.canRight = canRight;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean canMoveLeft() {
        return canLeft;
    }

    public boolean canMoveRight() {
        return canRight;
    }
}