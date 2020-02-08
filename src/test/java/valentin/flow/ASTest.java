package valentin.flow;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ASTest {


    @Test
    public void test1() {
        AS as = new AS("234.0.2.0");

        assertEquals(as.ASN,680);
        assertEquals(as.prefix,"2001:638::/32");
        assertEquals(as.holder,"DFN Verein zur Foerderung eines Deutschen Forschungsnetzes e.V.,DE");
    }
