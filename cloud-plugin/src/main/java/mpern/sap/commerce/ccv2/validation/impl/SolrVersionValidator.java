package mpern.sap.commerce.ccv2.validation.impl;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.Validator;

public class SolrVersionValidator implements Validator {
    private static final Pattern SOLR_VERSION = Pattern.compile("^\\d+\\.\\d+$");

    @Override
    public List<Error> validate(Manifest manifest) throws Exception {
        if (!manifest.solrVersion.isEmpty()) {
            final Matcher matcher = SOLR_VERSION.matcher(manifest.solrVersion);
            if (!matcher.matches()) {
                return Collections.singletonList(new Error.Builder().setLocation("solrVersion").setCode("E-018")
                        .setMessage("Invalid Solr version").createError());
            }
        }
        return Collections.emptyList();
    }
}
