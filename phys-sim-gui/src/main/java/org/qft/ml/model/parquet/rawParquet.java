package org.qft.ml.model.parquet;
import org.apache.parquet.tools.read.SimpleReadSupport;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.tools.read.SimpleRecord;

public class rawParquet {

    public static void main(String[] args){
        try (ParquetReader<SimpleRecord> reader =
                     ParquetReader.builder(new SimpleReadSupport(),
                                     new Path("C:\\Datasets\\parquet_output\\Jets_test.parquet"))
                             .withConf(new Configuration())
                             .build()) {

            SimpleRecord group;
            while ((group = reader.read()) != null) {
                System.out.println(group.toString());
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
