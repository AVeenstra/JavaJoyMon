package nl.utwente.aveenstra;

import net.java.games.input.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Antoine on 02/08/15.
 */
public class ComponentWrapper {
    public static final ArrayList<ComponentWrapper> buttons = new ArrayList<>();
    public static final ArrayList<ComponentWrapper> axes = new ArrayList<>();
    private static int maxIndex = 0;
    private static Map<Component, ComponentWrapper> lookupTable;
    private int componentNumber;
    private Component component;
    private boolean isButton;
    private int index;
    private float data;
    private int averageCount = 0;
    private float averageTotal = 0;
    private LinkedList<Consumer<Number>> updateFunctions;
    public static final ComponentWrapper[] componentWrappers = new ComponentWrapper[]{new ComponentWrapper("Start", 6, true) {
        public boolean setData(float data) {
            JoystickWrapper.getInstance().startPressed(data >= 1);
            return super.setData(data);
        }

        protected void addToType(boolean ignored) {
        }
    }, new ComponentWrapper("Stress", 1, true), new ComponentWrapper("Child missing", 0, true), new ComponentWrapper("Sad", 13), new ComponentWrapper("Anger", 14, -1), new ComponentWrapper("Contempt", 17, -1)};
    private String name;
    private int inverse;

    public ComponentWrapper(String name, int i) {
        this(name, i, false, 5);
    }

    public ComponentWrapper(String name, int i, boolean isButton) {
        this(name, i, isButton, 1);
    }

    public ComponentWrapper(String name, int i, int inverse) {
        this(name, i, false, inverse * 5);
    }

    public ComponentWrapper(String name, int i, boolean isButton, int inverse) {
        this.name = name;
        componentNumber = i;
        this.isButton = isButton;
        index = getMaxIndex();
        addToType(isButton);
        this.inverse = inverse;
        updateFunctions = new LinkedList<>();
    }

    private static int getMaxIndex() {
        return maxIndex++;
    }

    public static ComponentWrapper getComponentWrapper(Component component) {
        if (lookupTable == null) {
            lookupTable = new HashMap<>();
            for (ComponentWrapper componentWrapper : componentWrappers) {
                lookupTable.put(componentWrapper.getComponent(), componentWrapper);
            }
        }
        return lookupTable.get(component);
    }

    public int getComponentNumber() {
        return componentNumber;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public boolean isButton() {
        return isButton;
    }

    public boolean setData() {
        return setData(component.getPollData() * inverse);
    }

    public boolean setDataAverage() {
        boolean result = setData(component.getPollData() * inverse);
        averageCount++;
        averageTotal += data;
        return result;
    }

    public boolean setData(float data) {
        data = Math.max(0, data);
        boolean result = this.data != data;
        this.data = data;
        if (result) {
            final float finalData = data;
            updateFunctions.forEach(e -> e.accept(finalData));
        }
        return result;
    }

    public float getData() {
        return data;
    }

    public void isPressed() {
        data = 1;
        averageCount++;
        averageTotal += 1;
    }

    public float getAverage() {
        float result;
        if (0 < averageCount) {
            result = averageTotal / averageCount;
        } else {
            result = data;
        }
        averageCount = 0;
        averageTotal = 0;
        return result;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public void addUpdateFunction(Consumer<Number> updateFunction) {
        this.updateFunctions.add(updateFunction);
    }

    protected void addToType(boolean isButton) {
        if (isButton) {
            buttons.add(this);
        } else {
            axes.add(this);
        }
    }
}
