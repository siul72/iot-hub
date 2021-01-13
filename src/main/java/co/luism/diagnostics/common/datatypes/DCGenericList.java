package co.luism.diagnostics.common.datatypes;

import java.util.ArrayList;

/**
 * Created by luis on 12.11.14.
 */


public class DCGenericList <T> extends ArrayList<T>
{
    private Class<T> type;

    public DCGenericList(Class<T> c)
    {
        this.type = c;
    }

    public Class<T> getType()
    {
        return type;
    }
}
