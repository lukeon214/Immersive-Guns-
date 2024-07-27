package com.imguns.guns.api.client.animation.statemachine;

import com.imguns.guns.api.client.animation.AnimationController;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.LinkedList;
import java.util.function.Supplier;

public class LuaStateMachineFactory<T extends AnimationStateContext> {
    private AnimationController controller;
    private LuaFunction initializeFunc;
    private LuaFunction exitFunc;
    private LuaFunction statesFunc;

    public LuaAnimationStateMachine<T> build() {
        checkNullPointer();
        var stateMachine = new LuaAnimationStateMachine<T>(controller);
        stateMachine.initializeFunc = (context) -> {
            if (initializeFunc != null) {
                initializeFunc.call(context.getLuaContext());
            }
        };
        stateMachine.exitFunc = (context) -> {
            if (exitFunc != null) {
                exitFunc.call(context.getLuaContext());
            }
        };
        stateMachine.setStatesSupplier(getStatesSupplier());
        return stateMachine;
    }

    public LuaStateMachineFactory<T> setController(AnimationController controller) {
        this.controller = controller;
        return this;
    }

    public LuaStateMachineFactory<T> setLuaScripts(LuaTable table) {
        this.initializeFunc = checkFunction("initialize", table);
        this.exitFunc = checkFunction("exit", table);
        this.statesFunc = checkFunction("states", table);
        return this;
    }

    private LuaFunction checkFunction(String funcName, LuaTable table) {
        LuaValue value = table.get(funcName);
        if (value.isnil()) {
            return null;
        }
        return value.checkfunction();
    }

    private void checkNullPointer() {
        if (controller == null) {
            throw new IllegalStateException("controller must not be null before build");
        }
    }

    private Supplier<Iterable<? extends AnimationState<LuaContextWrapper<T>>>> getStatesSupplier() {
        if (statesFunc == null) {
            return null;
        }
        return () -> {
            LuaTable statesTable = statesFunc.call().checktable();
            LinkedList<LuaAnimationState<T>> states = new LinkedList<>();
            for (int f = 1; f <= statesTable.length(); f++) {
                LuaTable stateTable = statesTable.get(f).checktable();
                states.add(new LuaAnimationState<>(stateTable));
            }
            return states;
        };
    }
}
