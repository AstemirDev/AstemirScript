package org.astemir.ascript.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class AFuncArguments implements Iterable<AFuncArgument> {

    private final List<AFuncArgument> arguments;
    private int requiredArgumentsCount;

    public AFuncArguments() {
        arguments = new ArrayList<>();
        requiredArgumentsCount = 0;
    }

    public void addRequired(String name) {
        arguments.add(new AFuncArgument(name));
        requiredArgumentsCount++;
    }

    public void addOptional(String name, AExpression expr) {
        arguments.add(new AFuncArgument(name, expr));
    }

    public AFuncArgument get(int index) {
        return arguments.get(index);
    }

    public int getRequiredArgumentsCount() {
        return requiredArgumentsCount;
    }

    public int size() {
        return arguments.size();
    }

    @Override
    public Iterator<AFuncArgument> iterator() {
        return arguments.iterator();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append('(');
        final Iterator<AFuncArgument> it = arguments.iterator();
        if (it.hasNext()) {
            result.append(it.next());
            while (it.hasNext()) {
                result.append(", ").append(it.next());
            }
        }
        result.append(')');
        return result.toString();
    }
}