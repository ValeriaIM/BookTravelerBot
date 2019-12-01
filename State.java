import java.util.HashMap;

public class State {
    enum state{
        Main,
        Library,
        Read
    }

    private state currentState = state.Main;

    public state getCurrentState(){
        return currentState;
    }

    public void setCurrentState(state newState){
        currentState = newState;
    }

    public static void main(String[] args) {

    }
}
