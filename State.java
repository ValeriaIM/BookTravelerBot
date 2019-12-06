public class State {
    enum state {
        Main,
        Library,
        Read,
        Quiz
    }

    private state currentState = state.Main;

    public state getCurrentState() {
        return currentState;
    }

    public void setCurrentState(state newState) {
        currentState = newState;
    }
}
