package rb.com.care.purge;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Indexing implements Runnable{

    public static AtomicInteger threadsInWritingBlock = new AtomicInteger();
    public static AtomicInteger pendingCommits = new AtomicInteger();
    public static int PENDING_COMMIT_THRESHOLD = 1000;

    private IndexWriter iw;
    private List<File> f;
    private Integer count;

    public Indexing(IndexWriter iw, List<File> f, Integer count) {
        this.iw = iw;
        this.f = f;
        this.count = count;
    }

    @Override
    public void run()
    {
        try {
            for (int i = 0; i < f.size(); i++) {
                File file = f.get(i);
                indexFileWithIndexWriter(iw, file);
            }
            System.out.println(count);
        }
        catch (Exception e) {
            System.out.println("Exception is caught");
        }
    }

    private static void indexFileWithIndexWriter(IndexWriter iw, File f) throws IOException, ParseException {
        if (f.isHidden() || f.isDirectory() || !f.canRead() || !f.exists()) {
            return;
        }
        Document doc = new Document();
        addToIndex(iw, f, doc);
    }

    private static void addToIndex(IndexWriter iw, File f, Document doc) throws IOException, ParseException {
        doc.add(new LongPoint("id", f.hashCode()));
        doc.add(new Field("filename", f.getName(), TextField.TYPE_STORED));
        doc.add(new Field("filepath", f.getCanonicalPath(), TextField.TYPE_STORED));
        iw.addDocument(doc);
        threadsInWritingBlock.incrementAndGet();
        if (threadsInWritingBlock.decrementAndGet() == 0 || pendingCommits.incrementAndGet() > PENDING_COMMIT_THRESHOLD) {
            pendingCommits.set(0);
            iw.commit();
        }
    }
}
