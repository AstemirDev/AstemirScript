package org.astemir.ascript.core.libs;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.GlobalVariables;
import org.astemir.ascript.core.values.AValue;
import org.astemir.ascript.core.values.Instance;
import org.astemir.ascript.core.values.Type;
import java.util.ArrayList;

public class Library extends Instance {

    private String version;
    private String name;
    public Library(AContext context) {
        super(context, Type.LIBRARY);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name+"-v"+version;
    }

    @Override
    public String toString() {
        return asString();
    }
}
