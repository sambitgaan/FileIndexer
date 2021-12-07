package rb.com.care.purge;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.ThreadInterruptedException;

public class ConcurrentMergeScheduler extends org.apache.lucene.index.ConcurrentMergeScheduler {

    private final String indexName;

    public ConcurrentMergeScheduler(String indexName) {
        this.indexName = indexName;
    }

    public static void mergeScheduler(Object o) {

    }

//    public ConcurrentMergeScheduler.MergeThread(IndexWriter writer, MergePolicy.
//    org.apache.lucene.index.MergePolicy.OneMerge merge){
//
//    }


    protected synchronized MergeThread getMergeThread(IndexWriter writer, MergePolicy.OneMerge merge) throws IOException {
        final MergeThread thread = new MergeThread((MergeSource) writer, merge );
        thread.setDaemon( true );
        thread.setName( "Lucene Merge Thread #" + mergeThreadCount++ + " for index " + indexName );
        return thread;
    }

}
