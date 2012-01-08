package org.ops4j.pax.exam;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class TestDirectory
{
    private static final TestDirectory instance = new TestDirectory();
    
    private Map<TestAddress, TestInstantiationInstruction> map = new HashMap<TestAddress, TestInstantiationInstruction>();
    private URI accessPoint;
    
    private TestDirectory()
    {
    }
    
    public static TestDirectory getInstance() {
        return instance;
    }
    
    
    public void add(TestAddress address, TestInstantiationInstruction instruction) {
        map.put(address, instruction);
    }
    
    public TestInstantiationInstruction lookup(TestAddress address) {
        return map.get(address);
    }
    
    public void clear() {
        map.clear();
    }

    public URI getAccessPoint()
    {
        return accessPoint;
    }

    public void setAccessPoint( URI accessPoint )
    {
        this.accessPoint = accessPoint;
    }
}
