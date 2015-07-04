package ut.com.dataember.stash.plugin;

import org.junit.Test;
import com.dataember.stash.plugin.MyPluginComponent;
import com.dataember.stash.plugin.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}