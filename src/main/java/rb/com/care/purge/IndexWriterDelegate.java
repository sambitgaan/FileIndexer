package rb.com.care.purge;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

public class IndexWriterDelegate {

    private final IndexWriter indexWriter;
    private final Lock readLock;
    private final Lock writeLock;

    public IndexWriterDelegate(final IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
        ReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }


    public void addDocument(final Document document) throws IOException {
        //This is now equivalent to the old "addDocument" method:
        //updateDocument( null);
    }

    @Deprecated
    public IndexWriter getIndexWriter() {
        return indexWriter;
    }
}
