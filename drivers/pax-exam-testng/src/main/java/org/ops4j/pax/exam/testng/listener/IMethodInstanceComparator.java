package org.ops4j.pax.exam.testng.listener;

import java.util.Comparator;

import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;

public class IMethodInstanceComparator implements Comparator<IMethodInstance>
{

    @Override
    public int compare( IMethodInstance left, IMethodInstance right )
    {
        ITestNGMethod m1 = left.getMethod();
        ITestNGMethod m2 = right.getMethod();
        int result = m1.getRealClass().getName().compareTo( m2.getRealClass().getName() );
        if (result == 0)
        {
            result = m1.getMethodName().compareTo( m2.getMethodName() );
        }
        return result;
    }
}
