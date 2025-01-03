package ch.admin.foph.epr.policies;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.herasaf.xacml.core.api.PolicyRetrievalPoint;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.herasaf.xacml.core.policy.EvaluatableID;
import org.herasaf.xacml.core.policy.PolicyMarshaller;
import org.herasaf.xacml.core.policy.impl.PolicyType;
import org.openehealth.ipf.commons.ihe.xacml20.Xacml20Utils;
import org.openehealth.ipf.commons.xml.XmlUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Dmytro Rud
 */
@Slf4j
public class PolicyRepository implements PolicyRetrievalPoint {

    static {
        Xacml20Utils.initializeHerasaf();
    }

    private static final String ORIGINAL_DIR_NAME = "src/main/resources/policy-stack/original";
    private static final String MODIFIED_DIR_NAME = "src/main/resources/policy-stack/modified";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final Map<EvaluatableID, Evaluatable> basisPolicies = new HashMap<>();
    private final Map<EvaluatableID, Evaluatable> patientPolicies = new HashMap<>();
    private final Set<String> ruleIds = new HashSet<>();

    public PolicyRepository() throws Exception {
        loadBasePolicies(ORIGINAL_DIR_NAME);
        loadBasePolicies(MODIFIED_DIR_NAME);
    }

    public void addOriginal201PolicySet(String eprSpid) throws Exception {
        addPatientPolicy(ORIGINAL_DIR_NAME, 201, eprSpid, xml -> {
            xml = xml.replace(">\"epd-spid-goes-here\"<", ">" + eprSpid + "<");
            return xml;
        });
    }

    public void addModified201PolicySet(String eprSpid) throws Exception {
        addPatientPolicy(MODIFIED_DIR_NAME, 201, eprSpid, xml -> {
            xml = xml.replace(">\"epd-spid-goes-here\"<", ">" + eprSpid + "<");
            return xml;
        });
    }

    public void addOriginal202PolicySet(String eprSpid, String emergencyAccessLevel) throws Exception {
        addPatientPolicy(ORIGINAL_DIR_NAME, 202, eprSpid, xml -> {
            xml = xml.replace("urn:e-health-suisse:2015:policies:access-level:normal", emergencyAccessLevel);
            return xml;
        });
    }

    public void addModified202PolicySet(String eprSpid, String emergencyAccessLevel) throws Exception {
        addPatientPolicy(MODIFIED_DIR_NAME, 202, eprSpid, xml -> {
            xml = xml.replace("urn:e-health-suisse:2015:policies:access-level:normal", emergencyAccessLevel);
            return xml;
        });
    }

    public void addOriginal203PolicySet(String eprSpid, String provideAccessLevel) throws Exception {
        addPatientPolicy(ORIGINAL_DIR_NAME, 203, eprSpid, xml -> {
            xml = xml.replace("urn:e-health-suisse:2015:policies:provide-level:normal", provideAccessLevel);
            return xml;
        });
    }

    public void addModified203PolicySet(String eprSpid, String provideAccessLevel) throws Exception {
        addPatientPolicy(MODIFIED_DIR_NAME, 203, eprSpid, xml -> {
            xml = xml.replace("urn:e-health-suisse:2015:policies:provide-level:normal", provideAccessLevel);
            return xml;
        });
    }

    public void addOriginal301PolicySet(String eprSpid, String gln, Date toDate, String hcpReadAccessLevel) throws Exception {
        addPatientPolicy(ORIGINAL_DIR_NAME, 301, eprSpid, xml -> {
            xml = xml.replace("2.999", gln);
            xml = xml.replace("2016-02-07", DATE_FORMAT.format(toDate));
            xml = xml.replace("urn:e-health-suisse:2015:policies:exclusion-list", hcpReadAccessLevel);
            return xml;
        });
    }

    public void addModified301PolicySet(String eprSpid, String gln, Date toDate, String hcpReadAccessLevel) throws Exception {
        addPatientPolicy(MODIFIED_DIR_NAME, 301, eprSpid, xml -> {
            xml = xml.replace("2.999", gln);
            xml = xml.replace("2016-02-07", DATE_FORMAT.format(toDate));
            xml = xml.replace("urn:e-health-suisse:2015:policies:exclusion-list", hcpReadAccessLevel);
            return xml;
        });
    }

    public void addOriginal302PolicySet(String eprSpid, String groupOid, Date toDate, String groupReadAccessLevel) throws Exception {
        addPatientPolicy(ORIGINAL_DIR_NAME, 302, eprSpid, xml -> {
            xml = xml.replace("urn:oid:2.999", groupOid);
            xml = xml.replace("2016-02-07", DATE_FORMAT.format(toDate));
            xml = xml.replace("urn:e-health-suisse:2015:policies:access-level:normal", groupReadAccessLevel);
            return xml;
        });
    }

    public void addModified302PolicySet(String eprSpid, String groupOid, Date toDate, String groupReadAccessLevel) throws Exception {
        addPatientPolicy(MODIFIED_DIR_NAME, 302, eprSpid, xml -> {
            xml = xml.replace("urn:oid:2.999", groupOid);
            xml = xml.replace("2016-02-07", DATE_FORMAT.format(toDate));
            xml = xml.replace("urn:e-health-suisse:2015:policies:access-level:normal", groupReadAccessLevel);
            return xml;
        });
    }

    public void addOriginal303PolicySet(String eprSpid, String representativeId, Date toDate) throws Exception {
        addPatientPolicy(ORIGINAL_DIR_NAME, 303, eprSpid, xml -> {
            xml = xml.replace("2.999", representativeId);
            xml = xml.replace("2016-02-07", DATE_FORMAT.format(toDate));
            return xml;
        });
    }

    public void addModified303PolicySet(String eprSpid, String representativeId, Date toDate) throws Exception {
        addPatientPolicy(MODIFIED_DIR_NAME, 303, eprSpid, xml -> {
            xml = xml.replace("2.999", representativeId);
            xml = xml.replace("2016-02-07", DATE_FORMAT.format(toDate));
            return xml;
        });
    }

    public void addOriginal304PolicySet(String eprSpid, String gln, Date fromDate, Date toDate, String hcpReadAccessLevel) throws Exception {
        addPatientPolicy(ORIGINAL_DIR_NAME, 304, eprSpid, xml -> {
            xml = xml.replace("2.999", gln);
            xml = xml.replace("2023-02-01", DATE_FORMAT.format(fromDate));
            xml = xml.replace("2023-02-28", DATE_FORMAT.format(toDate));
            xml = xml.replace("urn:e-health-suisse:2015:policies:access-level:delegation-and-normal", hcpReadAccessLevel);
            return xml;
        });
    }

    public void addModified304PolicySet(String eprSpid, String gln, Date fromDate, Date toDate, String hcpReadAccessLevel, String homeCommunityId) throws Exception {
        addPatientPolicy(ORIGINAL_DIR_NAME, 304, eprSpid, xml -> {
            xml = xml.replace("2.999", gln);
            xml = xml.replace("2023-02-01", DATE_FORMAT.format(fromDate));
            xml = xml.replace("2023-02-28", DATE_FORMAT.format(toDate));
            xml = xml.replace("urn:e-health-suisse:2015:policies:access-level:delegation-and-normal", hcpReadAccessLevel);
            return xml;
        });
    }

    private void registerPolicy(String fn, String xml, Map<EvaluatableID, Evaluatable> targetMap) throws Exception {
        Evaluatable evaluatable = PolicyMarshaller.unmarshal(XmlUtils.source(xml));
        if (evaluatable instanceof PolicyType policy) {
            String ruleId = policy.getOrderedRules().getFirst().getRuleId();
            log.debug("file {} --> rule ID {}", fn, ruleId);
            if (!ruleIds.add(ruleId)) {
                log.warn("Duplicate rule ID {}", ruleId);
            }
        }
        boolean replace = (targetMap.put(evaluatable.getId(), evaluatable) != null);
        log.info("{} policy '{}' from {}", replace ? "Replaced" : "Loaded", evaluatable.getId(), fn);
    }

    private void loadBasePolicies(String dirName) throws Exception {
        List<String> policyFileNames;
        try (Stream<Path> stream = Files.walk(Paths.get(dirName + "/Privacy Policy Stack/EPD Policy Stack").toAbsolutePath())) {
            policyFileNames = stream
                    .map(path -> path.toString().toLowerCase(Locale.ROOT))
                    .filter(fn -> fn.endsWith(".xml"))
                    .toList();

            for (String fn : policyFileNames) {
                log.debug("Read base policy from {}", fn);
                try (InputStream inputStream = new FileInputStream(fn)) {
                    String xml = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    registerPolicy(fn, xml, basisPolicies);
                }
            }
        }
    }

    private void addPatientPolicy(String dirName, int templateId, String eprSpid, Function<String, String> placeholderFiller) throws Exception {
        List<String> policyFileNames;
        try (Stream<Path> stream = Files.walk(Paths.get(dirName + "/Privacy Policy Stack/Patient Specific via Policy Manager").toAbsolutePath())) {
            policyFileNames = stream
                    .map(path -> path.toString().toLowerCase(Locale.ROOT))
                    .filter(fn -> {
                        int pos = fn.lastIndexOf(File.separatorChar);
                        return fn.substring(pos + 1).startsWith(Integer.toString(templateId) + '-') && fn.endsWith(".xml");
                    })
                    .toList();

            if (policyFileNames.size() != 1) {
                throw new Exception("Expected exactly one template with the ID " + templateId + ", got " + policyFileNames.size());
            }

            String fn = policyFileNames.getFirst();
            try (InputStream inputStream = new FileInputStream(fn)) {
                log.debug("Read patient policy from {}", fn);
                String xml = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                xml = xml.replaceAll("=\"epr-spid-goes-here\"", "=\"" + eprSpid + "\"");
                xml = placeholderFiller.apply(xml);
                registerPolicy(fn, xml, patientPolicies);
            }
        }
    }

    @Override
    public Evaluatable getEvaluatable(EvaluatableID evaluatableId) {
        return basisPolicies.getOrDefault(evaluatableId, patientPolicies.get(evaluatableId));
    }

    @Override
    public List<Evaluatable> getEvaluatables(RequestType request) {
        return new ArrayList<>(patientPolicies.values());
    }

}