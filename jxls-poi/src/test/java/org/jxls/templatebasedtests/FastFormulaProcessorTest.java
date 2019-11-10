package org.jxls.templatebasedtests;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.jxls.common.CellData;
import org.jxls.common.CellRef;
import org.jxls.formula.FastFormulaProcessor;
import org.jxls.transform.Transformer;
import org.jxls.util.TransformerFactory;
import org.mockito.ArgumentCaptor;

/**
 * @author Michał Kępkowski
 */
public class FastFormulaProcessorTest {

    @Test
    public void FastFormulaProcessorIfFormulaTest() throws IOException {
        // BEFORE
        Locale.setDefault(Locale.ENGLISH); // Makes the testcase work in a German environment where "IF" is called "WENN" in Excel.
        InputStream template = getTestSheet();
        OutputStream outputStream = new ByteArrayOutputStream();
        Transformer transformer = spy(getTransformer(template, outputStream));
        ArgumentCaptor<String> firstFooCaptor = ArgumentCaptor.forClass(String.class);
        String expected = "IF(AE201=0,\"\",AE211/AE201)";

        // TEST
        FastFormulaProcessor fastFormulaProcessor = new FastFormulaProcessor();
        fastFormulaProcessor.processAreaFormulas(transformer, null);

        // ASSERT
        verify(transformer).setFormula(any(CellRef.class),firstFooCaptor.capture());
        Assert.assertEquals(expected,firstFooCaptor.getAllValues().get(0));
    }

    private Transformer getTransformer(InputStream template, OutputStream outputStream) {
        Transformer transformer = TransformerFactory.createTransformer(template, outputStream);
        transformer.getTargetCellRef(new CellRef("Arkusz1", 20, 4)).add(new CellRef("Arkusz1", 200, 30));
        transformer.getTargetCellRef(new CellRef("Arkusz1", 21, 4)).add(new CellRef("Arkusz1", 210, 30));

        List<CellData> cellDataList = new ArrayList<>(transformer.getFormulaCells());
        getIFFormula(cellDataList).addTargetPos(new CellRef("Arkusz1", 12, 12));
        return transformer;
    }

    private CellData getIFFormula(List<CellData> cellData) {
        for (CellData cell : cellData) {
            if (cell.getFormula().startsWith("IF")) {
                return cell;
            }
        }
        return null;
    }

    private InputStream getTestSheet() {
        return getClass().getResourceAsStream("test_formula.xls");
    }
}