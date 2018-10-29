import cwts.networkanalysis.AlgorithmWrapper;
import cwts.networkanalysis.Clustering;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlgorithmWrapperTest {
    private AlgorithmWrapper subject;

    @BeforeClass
    void setup() {
        subject = new AlgorithmWrapper(true, 0.2, true, 10);
    }

    @AfterClass
    void tearDown() {}

    @Test
    void tesAlgorithm() throws IOException {
        Clustering clustering = subject.process(this.getData());

        for (int i = 0; i < clustering.getNNodes(); i++) {
            System.out.print(i + " "  + clustering.getCluster(i));
            System.out.println();
        }
    }

    List<List> getData() throws IOException {
        List<List> data = new ArrayList<>();

        Reader in = new InputStreamReader(getClass().getResourceAsStream("data.csv"));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("id1", "id2", "weight").parse(in);

        for (CSVRecord record : records) {
            int id1 = Integer.parseInt(record.get("id1"));
            int id2 = Integer.parseInt(record.get("id2"));
            double weight = Double.parseDouble(record.get("weight"));

            data.add(Arrays.asList(id1, id2, weight));
        }

        return data;
    }
}
