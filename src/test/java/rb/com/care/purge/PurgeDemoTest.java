package rb.com.care.purge;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertNotNull;

import org.junit.runners.Suite;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import rb.com.care.purge.GetPurgeProperties;
import rb.com.care.purge.PurgeDemo;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Suite.SuiteClasses({PurgeDemo.class, GetPurgeProperties.class, Keyin.class})
public class PurgeDemoTest {
    public PurgeDemoTest(){}


    public static AtomicInteger threadsInWritingBlock = new AtomicInteger();
    public static AtomicInteger pendingCommits = new AtomicInteger();
    public static int PENDING_COMMIT_THRESHOLD = 1000;

    @InjectMocks
    private PurgeDemo purgeDemo = new PurgeDemo();

    @InjectMocks
    public GetPurgeProperties properties = new GetPurgeProperties();

    @InjectMocks
    Properties prop = new Properties();

    FileInputStream fileInputStream = null;

    @Before
    public void setUp() throws Exception {
        fileInputStream = new FileInputStream(new File("target\\classes\\config.properties"));
        properties.setProp();
        prop.load(fileInputStream);
    }

    @Ignore
    @Test(expected = NullPointerException.class)
    public void testStartIndex() throws IOException, ParseException {
        System.out.println("Inside testPrintMessage()");
        File dataDirectory = new File(properties.getDataDirectory());
        //IndexWriter iw = getIndexWriter(properties.getIndexDirectory());
         purgeDemo.startIndex();
        //when(properties.getInputFile()).thenReturn("target\\\\classes\\\\Input\\\\List.txt");
        ///ass(properties.getIndexDirectory(), "indexDir");
        //assertEquals(35, indexedDocCount);
    }

    @Test
    public void createIndex() throws IOException, ParseException {
        BufferedReader br = purgeDemo.getFIleBufferedReader();
        purgeDemo.createIndex(br);
        //when(properties.getInputFile()).thenReturn("indexDir");
    }

    @Test
    public void getFIleBufferedReader() throws IOException {
        properties = new GetPurgeProperties();
        properties.setProp();
        prop = new Properties();
        prop.load(fileInputStream);
        BufferedReader br = purgeDemo.getFIleBufferedReader();
//        doReturn("indexDir").when(properties).getInputFile();
//        when(properties.getInputFile()).thenReturn("indexDir");
//        when(this.prop.getProperty("LIST_INPUT_FILE")).thenReturn("indexDir");
        assertNotNull(br);
    }
}