package rb.com.care.purge;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexingExecutor implements Runnable{

    public static AtomicInteger threadsInWritingBlock = new AtomicInteger();
    public static AtomicInteger pendingCommits = new AtomicInteger();
    public static int PENDING_COMMIT_THRESHOLD = 1000;

    private List<File> f;
    private Integer count;

    public IndexingExecutor(List<File> f, Integer count) {
        this.f = f;
        this.count = count;
    }

    @Override
    public void run()
    {
        IndexWriter iw = null;
        try {
            iw = getIndexWriter("IndexDir"+count);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static IndexWriter getIndexWriter(String indexDirectory) throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        Directory index = getIndexDirectory(indexDirectory);
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setCommitOnClose(true);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        iwc.setMergeScheduler(new org.apache.lucene.index.SerialMergeScheduler());
        iwc.setRAMBufferSizeMB(1024);
        IndexWriter iw = new IndexWriter(index, iwc);
        return iw;
    }

    private static Directory getIndexDirectory(String dirPath) throws IOException {
        File idxDirectory = new File(dirPath);
        Directory index = FSDirectory.open(idxDirectory.toPath());
        return index;
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
