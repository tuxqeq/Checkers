package Game;

public class RepaintEvent {
    private final Object source;

    public RepaintEvent(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return source;
    }
}
