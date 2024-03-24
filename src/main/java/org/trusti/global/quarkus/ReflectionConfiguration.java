package org.trusti.global.quarkus;

import io.quarkus.runtime.annotations.RegisterForReflection;
import schemas.csaf.*;
import schemas.cve.v5.CveV5;
import schemas.cve.v5.tags.AdpTags;
import schemas.cve.v5.tags.CnaTags;
import schemas.cve.v5.tags.ReferenceTags;
import schemas.osv.*;
import schemas.osv.Package;

@RegisterForReflection(targets = {
        Csaf.class,
        AcknowledgmentsT.class,
        AggregateSeverity.class,
        BranchesT.class,
        CvssV20.class,
        Cwe.class,
        Distribution.class,
        Document.class,
        Engine.class,
        FileHash.class,
        Flag.class,
        FullProductNameT.class,
        Generator.class,
        Hash.class,
        Id.class,
        Involvement.class,
        NotesT.class,
        ProductGroup.class,
        ProductIdentificationHelper.class,
        ProductStatus.class,
        ProductTree.class,
        Publisher.class,
        ReferencesT.class,
        Relationship.class,
        Remediation.class,
        RestartRequired.class,
        RevisionHistory.class,
        Score.class,
        Thread.class,
        Tlp.class,
        Tracking.class,
        Vulnerability.class,
        XGenericUri.class,

        Osv.class,
        Affected.class,
        Credit.class,
        DatabaseSpecific.class,
        DatabaseSpecific__1.class,
        DatabaseSpecific__2.class,
        EcosystemSpecific.class,
        Event.class,
        Package.class,
        Range.class,
        Reference.class,
        Severity.class,

        CveV5.class,
        schemas.cve.v5.imports.cvss.CvssV20.class,
        schemas.cve.v5.imports.cvss.CvssV30.class,
        schemas.cve.v5.imports.cvss.CvssV31.class,
        AdpTags.class,
        CnaTags.class,
        ReferenceTags.class,
})
public class ReflectionConfiguration {
}
