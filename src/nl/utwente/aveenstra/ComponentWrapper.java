package nl.utwente.aveenstra;

import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableValueBase;
import net.java.games.input.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by antoine on 02/08/15.
 */
public class ComponentWrapper extends ObservableValueBase<Number> implements ObservableFloatValue {
    public static final ArrayList<ComponentWrapper> buttons = new ArrayList<>();
    public static final ArrayList<ComponentWrapper> axes = new ArrayList<>();
    public static final ComponentWrapper[] componentWrappers = new ComponentWrapper[]{new ComponentWrapper(6, true) {
        public boolean setData(float data) {
            JoystickWrapper.getInstance().startPressed(data >= 1);
            return super.setData(data);
        }
    }, new ComponentWrapper(0, true), new ComponentWrapper(1, true), new ComponentWrapper(11), new ComponentWrapper(12), new ComponentWrapper(14)};

    private int componentNumber;
    private Component component;
    private boolean isButton;
    private int index;
    private static int maxIndex = 0;
    private float data;
    private int averageCount = 0;
    private float averageTotal = 0;
    private static Map<Component, ComponentWrapper> lookupTable;
    private java.util.function.Consumer<Number> updateFunction;

    public ComponentWrapper(int i) {
        this(i, false);
    }

    public ComponentWrapper(int i, boolean isButton) {
        componentNumber = i;
        this.isButton = isButton;
        index = getMaxIndex();
        if (isButton) {
            buttons.add(this);
        } else {
            axes.add(this);
        }
    }

    public int getComponentNumber() {
        return componentNumber;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

    public boolean isButton() {
        return isButton;
    }

    private static int getMaxIndex() {
        return maxIndex++;
    }

    public boolean setData() {
        return setData(component.getPollData());
    }

    public boolean setDataAverage() {
        boolean result = setData(component.getPollData());
        averageCount++;
        averageTotal += data;
        return result;
    }

    public boolean setData(float data) {
        data = Math.max(0, data);
        boolean result = this.data != data;
        this.data = data;
        if (result && updateFunction != null) {
            updateFunction.accept(data*1000);
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
        float result = averageTotal / averageCount;
        averageCount = 0;
        averageTotal = 0;
        return result;
    }

    public int getIndex() {
        return index;
    }

    public static ComponentWrapper getComponentWrapper(Component component) {
        if (lookupTable == null) {
            lookupTable = new HashMap<>();
            for (int i = 0; i < componentWrappers.length; i++) {
                lookupTable.put(componentWrappers[i].getComponent(), componentWrappers[i]);
            }
        }
        return lookupTable.get(component);
    }

    @Override
    public float get() {
        return data;
    }

    @Override
    public int intValue() {
        return (int) data;
    }

    @Override
    public long longValue() {
        return (long) data;
    }

    @Override
    public float floatValue() {
        return data;
    }

    @Override
    public double doubleValue() {
        return data;
    }

    @Override
    public Number getValue() {
        return data;
    }

    public void setUpdateFunction(Consumer<Number> updateFunction) {
        this.updateFunction = updateFunction;
    }
}
