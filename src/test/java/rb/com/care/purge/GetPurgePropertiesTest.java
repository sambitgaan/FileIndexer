package rb.com.care.purge;

import org.junit.Before;
import org.junit.Test;
import rb.com.care.purge.GetPurgeProperties;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

public class GetPurgePropertiesTest {

    GetPurgeProperties properties = new GetPurgeProperties();
    @Before
    public void setUp() throws Exception {
        properties.setProp();
    }

    @Test
    public void getProp() {
        Properties prop = properties.getProp();
        assertNotNull(prop);
    }

    @Test
    public void setProp() throws IOException {
        properties.setProp();
        assertEquals("", "");
    }

    @Test
    public void getIndexDirectory() {
        assertEquals("IndexDir", properties.getIndexDirectory());
    }

    @Test
    public void getDataDirectory() {
        assertEquals("purgeData", properties.getDataDirectory());
    }

    @Test
    public void getInputFile() {
        assertEquals("target\\classes\\Input\\List.txt", properties.getInputFile());
    }

    @Test
    public void getSearchedFile() {
        assertEquals("target\\classes\\Search\\Searchedfiles.txt", properties.getSearchedFile());
    }

    @Test
    public void getDeleteLogFile() {
        assertEquals("target\\classes\\Input\\List.txt", properties.getDeleteLogFile());
    }
}