package nl.utwente.aveenstra;

import java.util.Observable;

/**
 * Created by antoine on 02/08/15.
 */
public class ViewCLI implements View {

    public ViewCLI(){
        initView();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg instanceof Boolean) {
            System.out.print(arg);
            for (ComponentWrapper c : ComponentWrapper.componentWrappers) {
                System.out.print(String.format(", %+2.2f", c.getData()));
            }
            System.out.print("\r");
        }
    }
}
